package com.pylon.pylonservice.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class ShardAdditionalLink implements Serializable {
    private static final long serialVersionUID = 0L;

    String title;
    String url;
}
