package com.pylon.pylonservice.model.responses;

import com.pylon.pylonservice.model.tables.Profile;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Builder
@Value
public class ProfileResponse implements Serializable {
    private static final long serialVersionUID = 0L;

    Profile profile;
}
