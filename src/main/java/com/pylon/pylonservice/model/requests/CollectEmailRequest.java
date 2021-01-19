package com.pylon.pylonservice.model.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * {
 *     "email": "jason@gmail.com"
 * }
 */
@Data
@NoArgsConstructor
public class CollectEmailRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    String email;
}
