package rs.pgdavidov.erp.common.pagination;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {

    public static <T, R> PagedResponse<R> from(
            Page<T> source,
            Function<T, R> mapper
    ) {
        return new PagedResponse<>(
                source.getContent()
                        .stream()
                        .map(mapper)
                        .toList(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isFirst(),
                source.isLast(),
                source.isEmpty()
        );
    }
}