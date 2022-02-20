package no.nav.pto.veilarbfilter.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private Integer status;
    private T content;
    private String error;
}
