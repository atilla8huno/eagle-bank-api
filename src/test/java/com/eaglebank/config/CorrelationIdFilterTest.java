package com.eaglebank.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Test suite of the class: CorrelationIdFilter")
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    @DisplayName("Should propagate incoming X-Correlation-Id header to MDC and response header and clear MDC afterwards")
    void givenRequestWithCorrelationId_whenDoFilter_thenShouldPropagateAndClearMdc() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "req-corr-12345");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcInsideChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcInsideChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        //when
        filter.doFilter(request, response, chain);

        //then
        assertEquals("req-corr-12345", mdcInsideChain.get());
        assertEquals("req-corr-12345", response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    @ParameterizedTest
    @MethodSource("missingOrBlankHeaders")
    @DisplayName("Should generate a UUID X-Correlation-Id when header is missing or blank")
    void givenMissingOrBlankHeader_whenDoFilter_thenShouldGenerateCorrelationId(String headerValue) throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (headerValue != null) {
            request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, headerValue);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcInsideChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcInsideChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        //when
        filter.doFilter(request, response, chain);

        //then
        String responseHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertNotNull(responseHeader);
        assertFalse(responseHeader.isBlank());
        assertEquals(responseHeader, mdcInsideChain.get());
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    private static Stream<Arguments> missingOrBlankHeaders() {
        return Stream.of(
                arguments((String) null),
                arguments(""),
                arguments("   ")
        );
    }
}
