package com.pylon.pylonservice.model.responses;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

/**
 * {
 *     "filename": "123456.png"
 * }
 */
@Builder
@Value
public class ImageUploadResponse implements Serializable {
    private static final long serialVersionUID = 0L;

    String filename;
}
