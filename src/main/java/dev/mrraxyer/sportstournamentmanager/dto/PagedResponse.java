package dev.mrraxyer.sportstournamentmanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/** Respuesta paginada genérica. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private int status;
    private String message;
    private java.util.List<T> data;
    private PageInfo pageInfo;
    private LocalDateTime timestamp;

    /** Metadatos de paginación. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean isFirst;
        private boolean isLast;
        private boolean hasNext;
        private boolean hasPrevious;
    }

    public PagedResponse(int status, String message, java.util.List<T> data, PageInfo pageInfo) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.pageInfo = pageInfo;
        this.timestamp = LocalDateTime.now();
    }
}
