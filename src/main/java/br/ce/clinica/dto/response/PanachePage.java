package br.ce.clinica.dto.response;

import io.quarkus.panache.common.Page;
import lombok.*;


import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PanachePage<T> {

    private List<T> content;

    private Page page;

    private long totalCount;
    
    
    public int getTotalPages() {
        return (int) Math.ceil((double) totalCount / page.size);
    }
    
    public int getCurrentPage() {
        return page.index + 1;
    }

    public int getSize() {
        return page.size;
    }

    public boolean hasNextPage() {
        return getCurrentPage() < getTotalPages();
    }

    public boolean hasPreviousPage() {
        return getCurrentPage() > 1;
    }

}
