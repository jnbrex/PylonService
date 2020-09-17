package com.pylon.pylonservice.pojo;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PageRange {
    long low;
    long high;
}
