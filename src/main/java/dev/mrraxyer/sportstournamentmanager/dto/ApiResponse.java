package dev.mrraxyer.sportstournamentmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase Genérica ApiResponse<T>
 *
 * @param <T> Tipo de datos que contiene la respuesta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * Código de estado HTTP (200, 201, 400, 404, 500, etc.)
     */
    private int status;
    
    /**
     * Mensaje descriptivo de la respuesta
     */
    private String message;
    
    /**
     * Datos payload de la respuesta (puede ser un objeto, lista, mapa, etc.)
     */
    private T data;
    
    /**
     * Timestamp de cuándo se generó la respuesta
     */
    private LocalDateTime timestamp;
    
    /**
     * Lista de errores de validación (cuando hay fallos)
     */
    private List<String> errors;
    
    /**
     * Path del endpoint que generó la respuesta (para debugging)
     */
    private String path;
    
    /**
     * Constructor simplificado para respuestas exitosas
     *
     * @param status código de estado
     * @param message mensaje descriptivo
     * @param data los datos a devolver
     */
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor para respuestas con errores
     *
     * @param status código de estado
     * @param message mensaje descriptivo
     * @param errors lista de errores
     * @param path path del endpoint
     */
    public ApiResponse(int status, String message, List<String> errors, String path) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor para respuesta de éxito con path para debugging
     *
     * @param status código de estado
     * @param message mensaje descriptivo
     * @param data los datos a devolver
     * @param path path del endpoint
     */
    public ApiResponse(int status, String message, T data, String path) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}

