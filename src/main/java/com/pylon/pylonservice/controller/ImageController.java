package com.pylon.pylonservice.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.pylon.pylonservice.model.responses.ImageUploadResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Log4j2
@RestController
public class ImageController {
    private static final String IMAGE_ROUTE = "/image";

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${environment.url}")
    private String environmentUrl;

    private final String imageBucketName;

    ImageController(@Value("${environment.name}") final String environmentName,
                    @Value("${image.bucket.name}") final String imageBucketName) {
        this.imageBucketName = String.format("%s-%s", environmentName, imageBucketName);
    }

    /**
     * Call to upload an image.
     *
     * @param multipartFile A multipart file.
     *
     * @return HTTP 201 Created - If the image was uploaded successfully. Returns a response with body like
     *                            {
     *                                "imageId": "a88fed16-330a-4b64-a704-eb9c81e33e10"
     *                            }
     *                            and a header like
     *                            "Location": "http://localhost:8080/image/a88fed16-330a-4b64-a704-eb9c81e33e10"
     *
     *         HTTP 422 Unprocessable Entity - If the submitted file is not an image.
     */
    @PostMapping(value = IMAGE_ROUTE)
    public ResponseEntity<?> postImage(@RequestParam("file") final MultipartFile multipartFile) {
        final String imageId = UUID.randomUUID().toString();
        final File file = new File(imageId);
        try {
            final FileOutputStream fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();
            if (!isImage(file)) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            amazonS3.putObject(imageBucketName, imageId, file);
        } catch (final Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            file.delete();
        }

        return ResponseEntity.created(
            URI.create(
                String.format(
                    "%s%s/%s",
                    environmentUrl,
                    IMAGE_ROUTE,
                    imageId
                )
            )
        ).body(
            ImageUploadResponse.builder()
                .imageId(imageId)
                .build()
        );
    }

    private static boolean isImage(final File file) {
        try {
            final Image image = ImageIO.read(file);
            return image != null;
        } catch(IOException ex) {
            return false;
        }
    }
}
