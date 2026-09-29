package com.nttdata.catalog.exception;

/** A catalog must load completely or fail with its source and diagnostic context. */
public class CatalogLoadException extends RuntimeException {

    public CatalogLoadException(String message) {
        super(message);
    }

    public CatalogLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
