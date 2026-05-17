package dev.mrraxyer.sportstournamentmanager.dto;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Clase Utilitaria Genérica ApiResponseBuilder<T>
 *
 * @param <T> Tipo de datos de la respuesta
 */
public class ApiResponseBuilder<T> {

    private int status;
    private String message;
    private T data;
    private String path;
    private List<String> errors;

    /**
     * Crea un builder para una respuesta exitosa
     */
    public static <T> ApiResponseBuilder<T> success(T data) {
        ApiResponseBuilder<T> builder = new ApiResponseBuilder<>();
        builder.status = HttpStatus.OK.value();
        builder.data = data;
        builder.message = "Operación exitosa";
        return builder;
    }

    /**
     * Crea un builder para una respuesta de creación (201)
     */
    public static <T> ApiResponseBuilder<T> created(T data) {
        ApiResponseBuilder<T> builder = new ApiResponseBuilder<>();
        builder.status = HttpStatus.CREATED.value();
        builder.data = data;
        builder.message = "Recurso creado exitosamente";
        return builder;
    }

    /**
     * Crea un builder para una respuesta de error
     */
    public static <T> ApiResponseBuilder<T> error(String message, int status) {
        ApiResponseBuilder<T> builder = new ApiResponseBuilder<>();
        builder.status = status;
        builder.message = message;
        return builder;
    }

    /**
     * Crea un builder para una respuesta de error con lista de errores
     */
    public static <T> ApiResponseBuilder<T> error(List<String> errors, int status) {
        ApiResponseBuilder<T> builder = new ApiResponseBuilder<>();
        builder.status = status;
        builder.errors = errors;
        builder.message = "Se encontraron errores";
        return builder;
    }

    /**
     * Crea un builder para una respuesta sin contenido (204)
     */
    public static <Void> ApiResponseBuilder<Void> noContent() {
        ApiResponseBuilder<Void> builder = new ApiResponseBuilder<>();
        builder.status = HttpStatus.NO_CONTENT.value();
        builder.message = "Operación completada sin contenido";
        return builder;
    }

    // Métodos de construcción

    public ApiResponseBuilder<T> status(int status) {
        this.status = status;
        return this;
    }

    public ApiResponseBuilder<T> message(String message) {
        this.message = message;
        return this;
    }

    public ApiResponseBuilder<T> data(T data) {
        this.data = data;
        return this;
    }

    public ApiResponseBuilder<T> path(String path) {
        this.path = path;
        return this;
    }

    public ApiResponseBuilder<T> errors(List<String> errors) {
        this.errors = errors;
        return this;
    }

    /**
     * Construye la respuesta final
     */
    public ApiResponse<T> build() {
        if (errors != null && !errors.isEmpty()) {
            return new ApiResponse<>(status, message, errors, path);
        } else {
            return new ApiResponse<>(status, message, data, path);
        }
    }
}

