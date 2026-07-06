package com.xhbookstore.api.filter;

import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.model.ApiResponse;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    @Test
    void shouldRejectTokenFromQueryString() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter("secret");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/mp/v1/user/home");
        request.setParameter("token", "fake-token-from-url");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw new AssertionError("request should not reach controller");
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        ApiResponse<?> body = JSON.parseObject(response.getContentAsString(), ApiResponse.class);
        assertThat(body.getCode()).isEqualTo(ApiErrorCode.UNAUTHORIZED);
    }
}
