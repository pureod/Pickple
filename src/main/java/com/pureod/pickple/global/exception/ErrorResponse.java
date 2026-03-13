package com.pureod.pickple.global.exception;

import java.util.Map;

public record ErrorResponse(
    String exceptionName,
    String message,
    Map<String, String> details
) {

    public static ErrorResponse of(String exceptionName, String message) {
        return new ErrorResponse(exceptionName, message, null);
    }
}
