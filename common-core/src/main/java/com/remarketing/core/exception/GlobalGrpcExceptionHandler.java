package com.remarketing.core.exception;

import io.grpc.Status;
import io.grpc.StatusException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalGrpcExceptionHandler.class);

    @GrpcExceptionHandler(ResourceNotFoundException.class)
    public StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e).asException();
    }

    @GrpcExceptionHandler(ValidationException.class)
    public StatusException handleValidationException(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusException handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusException handleException(Exception e) {
        log.error("Internal server error: ", e);
        return Status.INTERNAL.withDescription("An unexpected internal error occurred: " + e.getMessage()).withCause(e).asException();
    }
}
