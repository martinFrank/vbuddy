package com.github.martinfrank.vbuddy.controller.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, Long id) {
        super(entityName + " not found: " + id);
    }
}
