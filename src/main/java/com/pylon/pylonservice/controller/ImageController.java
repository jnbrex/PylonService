package com.pylon.pylonservice.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.pylon.pylonservice.model.responses.ImageUploadResponse;
import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.MetricsService;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.Tika;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.USER_UPLOADED_IMAGES_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.set;

@Log4j2
@RestController
public class ImageController {
    private static final String IMAGE_METRIC_NAME = "Image";
    private static final Map<String, String> MIME_TYPE_TO_FILE_EXTENSION_MAPPING = Map.of(
        "image/png", "png",
        "image/jpeg", "jpg",
        "image/gif", "gif"
    );

    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private AccessTokenService accessTokenService;
    @Qualifier("writer")
    @Autowired
    private GraphTraversalSource wG;
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private Tika tika;

    private final String imageBucketName;

    ImageController(@Value("${environment.name}") final String environmentName,
                    @Value("${image.bucket.name}") final String imageBucketName) {
        this.imageBucketName = String.format("%s-%s", environmentName, imageBucketName);
    }

    /**
     * Call to upload an image. Only supports png, jpeg/jpg, and gif.
     *
     * @param multipartFile A MultipartFile of mime type image/png, image/jpeg, or image/gif.
     *
     * @return HTTP 201 Created - An {@link ImageUploadResponse}.
     *         HTTP 422 Unprocessable Entity - If the submitted file is not of supported type.
     */
    @PostMapping(value = "/image")
    public ResponseEntity<?> postImage(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                       @RequestParam("file") final MultipartFile multipartFile) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(IMAGE_METRIC_NAME);

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        if (multipartFile.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String fileExtension = MIME_TYPE_TO_FILE_EXTENSION_MAPPING.get(multipartFile.getContentType());
        if (fileExtension == null) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String tikaDetectedFileExtension;
        try {
            tikaDetectedFileExtension = MIME_TYPE_TO_FILE_EXTENSION_MAPPING.get(tika.detect(multipartFile.getBytes()));
        } catch (final IOException e) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (!fileExtension.equals(tikaDetectedFileExtension)) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String filename = String.format("%s.%s", UUID.randomUUID().toString(), fileExtension);
        final File file = new File(filename);
        try {
            final FileOutputStream fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();

            final ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setCacheControl("max-age=31536000");
            amazonS3.putObject(
                new PutObjectRequest(imageBucketName, filename, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead)
                    .withMetadata(objectMetadata)
            );

            wG.V()
                .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
                .property(set, USER_UPLOADED_IMAGES_PROPERTY, filename)
                .iterate();
        } catch (final Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            file.delete();
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
            ImageUploadResponse.builder()
                .filename(filename)
                .build(),
            HttpStatus.CREATED
        );

        metricsService.addSuccessMetric(IMAGE_METRIC_NAME);
        metricsService.addLatencyMetric(IMAGE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
