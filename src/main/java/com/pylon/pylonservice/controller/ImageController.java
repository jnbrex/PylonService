package com.pylon.pylonservice.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
     * Call to retrieve an image.
     *
     * @param imageId A String containing the imageId of the image to return.
     * @return HTTP 200 OK - If the image was retrieved successfully. Returns a byte array.
     *         HTTP 404 Not Found - If the requested image does not exist.
     */
    @GetMapping(value = IMAGE_ROUTE + "/{imageId}")
    public ResponseEntity<?> getImage(@PathVariable final String imageId) {
        final S3Object s3Object;
        try {
            s3Object = amazonS3.getObject(imageBucketName, imageId);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == 404) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }

        final S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        final byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(s3ObjectInputStream);
            s3Object.close();
        } catch (final Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        final ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity
            .ok()
            .contentLength(bytes.length)
            .header("Content-type", "application/octet-stream")
            .header("Content-disposition", "attachment; filename=\"" + imageId + "\"")
            .body(resource);
    }

    /**
     * Call to upload an image.
     *
     * @param multipartFile A multipart file.
     *
     * @return HTTP 201 Created - If the image was uploaded successfully. Returns a response like
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
        ).build();
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
