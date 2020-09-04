package com.pylon.pylonservice.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class ShardRule implements Serializable {
    private static final long serialVersionUID = 0L;

    String ruleName;
    String ruleDescription;
}
