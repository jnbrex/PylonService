package com.pylon.pylonservice.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.pylon.pylonservice.model.responses.ImageUploadResponse;
import com.pylon.pylonservice.util.MetricsUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;

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
    private MetricsUtil metricsUtil;

    private final String imageBucketName;

    ImageController(@Value("${environment.name}") final String environmentName,
                    @Value("${image.bucket.name}") final String imageBucketName) {
        this.imageBucketName = String.format("%s-%s", environmentName, imageBucketName);
    }

    /**
     * Call to upload an image. Only supports png, jpeg/jpg, and gif.
     *
     * @param multipartFile A multipart file of mime type image/png, image/jpeg, or image/gif.
     *
     * @return HTTP 201 Created - If the image was uploaded successfully. Returns a response with body like
     *                            {
     *                                "filename": "a88fed16-330a-4b64-a704-eb9c81e33e10.png"
     *                            }
     *         HTTP 422 Unprocessable Entity - If the submitted file is not of supported type.
     */
    @PostMapping(value = "/image")
    public ResponseEntity<?> postImage(@RequestParam("file") final MultipartFile multipartFile) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(IMAGE_METRIC_NAME);

        final String fileExtension = MIME_TYPE_TO_FILE_EXTENSION_MAPPING.get(multipartFile.getContentType());
        if (fileExtension == null) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String filename = String.format("%s.%s", UUID.randomUUID().toString(), fileExtension);
        final File file = new File(filename);
        try {
            final FileOutputStream fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();
            amazonS3.putObject(
                new PutObjectRequest(imageBucketName, filename, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead)
            );
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

        metricsUtil.addSuccessMetric(IMAGE_METRIC_NAME);
        metricsUtil.addLatencyMetric(IMAGE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
