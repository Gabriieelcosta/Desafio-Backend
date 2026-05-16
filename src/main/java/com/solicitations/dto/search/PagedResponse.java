package com.solicitations.dto.search;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagedResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long total;
}
