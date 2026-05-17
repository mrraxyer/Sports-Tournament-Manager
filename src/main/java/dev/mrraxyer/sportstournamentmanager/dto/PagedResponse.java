package dev.mrraxyer.sportstournamentmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Clase Genérica PagedResponse<T>
 *
 * @param <T> Tipo de elementos en la página
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {
    
    /**
     * Código de estado HTTP
     */
    private int status;
    
    /**
     * Mensaje descriptivo
     */
    private String message;
    
    /**
     * Lista de elementos en la página actual
     */
    private java.util.List<T> data;
    
    /**
     * Información de paginación
     */
    private PageInfo pageInfo;
    
    /**
     * Timestamp de la respuesta
     */
    private LocalDateTime timestamp;
    
    /**
     * Información de paginación - clase interna
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        /**
         * Número de página actual (comenzando en 1)
         */
        private int pageNumber;
        
        /**
         * Tamaño de la página (elementos por página)
         */
        private int pageSize;
        
        /**
         * Total de elementos disponibles
         */
        private long totalElements;
        
        /**
         * Total de páginas disponibles
         */
        private int totalPages;
        
        /**
         * ¿Es la primera página?
         */
        private boolean isFirst;
        
        /**
         * ¿Es la última página?
         */
        private boolean isLast;
        
        /**
         * ¿Hay página anterior?
         */
        private boolean hasNext;
        
        /**
         * ¿Hay página anterior?
         */
        private boolean hasPrevious;
    }
    
    /**
     * Constructor para respuestas paginadas exitosas
     *
     * @param status código de estado
     * @param message mensaje descriptivo
     * @param data elementos de la página
     * @param pageInfo información de paginación
     */
    public PagedResponse(int status, String message, java.util.List<T> data, PageInfo pageInfo) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.pageInfo = pageInfo;
        this.timestamp = LocalDateTime.now();
    }
}

