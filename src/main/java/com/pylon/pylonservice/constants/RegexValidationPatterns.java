package com.pylon.pylonservice.constants;

import java.util.regex.Pattern;

public class RegexValidationPatterns {
    // Filenames are UUIDs ending in .png, .jpg, or .gif
    public static final Pattern FILENAME_REGEX_PATTERN =
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|gif|png)$");
}
