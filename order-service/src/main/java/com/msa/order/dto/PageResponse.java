package com.msa.order.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 페이지네이션 응답 공통 DTO.
 *
 * @param <T>          컨텐츠 요소 타입
 * @param content      현재 페이지 데이터 목록
 * @param totalElements 전체 데이터 수
 * @param totalPages   전체 페이지 수
 * @param pageNumber   현재 페이지 번호 (0-based)
 * @param pageSize     페이지당 데이터 수
 * @param first        첫 번째 페이지 여부
 * @param last         마지막 페이지 여부
 */
public record PageResponse<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int pageNumber,
    int pageSize,
    boolean first,
    boolean last
) {

    /**
     * Spring Data JPA {@link Page} 객체로부터 {@code PageResponse}를 생성한다.
     *
     * @param <T>  컨텐츠 요소 타입
     * @param page Spring Data JPA Page 객체
     * @return PageResponse 인스턴스
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize(),
            page.isFirst(),
            page.isLast()
        );
    }
}
