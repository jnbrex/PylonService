package com.pylon.pylonservice.model.requests;

import java.util.regex.Pattern;

public interface Request {
    // UUID ending in .png, .jpg, or .gif
    Pattern FILENAME_REGEX_PATTERN =
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|gif|png)$");

    boolean isValid();
}
