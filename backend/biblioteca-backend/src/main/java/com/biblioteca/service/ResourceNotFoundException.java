package com.biblioteca.service;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String risorsa, Object id) {
        super(risorsa + " non trovato/a (id: " + id + ")");
    }
}
