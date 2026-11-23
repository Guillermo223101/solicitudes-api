package com.gestionsolicitudes.api.service;

public class CorreoDuplicadoException extends RuntimeException{
    public CorreoDuplicadoException(){
        super("El correo ya esta registrado");
    }
}
