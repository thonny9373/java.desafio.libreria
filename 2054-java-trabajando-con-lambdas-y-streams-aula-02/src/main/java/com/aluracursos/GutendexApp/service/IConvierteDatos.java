package com.aluracursos.GutendexApp.service;

public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
