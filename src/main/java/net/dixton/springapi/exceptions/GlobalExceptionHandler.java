package net.dixton.springapi.exceptions;

import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DixtonRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleDixtonRuntimeException(DixtonRuntimeException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getExceptionError(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity
                .status(HttpStatus.valueOf(ex.getHttpStatus().getCode()))
                .body(errorResponse);
    }
}