package com.pylon.pylonservice.config.auth;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

@Component
public class CSRFProtectionRequestFilter extends OncePerRequestFilter {
    private static final Collection<String> CSRF_PROTECTED_HTTP_METHODS = Set.of(
        HttpMethod.POST.name(),
        HttpMethod.DELETE.name(),
        HttpMethod.PUT.name(),
        HttpMethod.PATCH.name()
    );

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        if (CSRF_PROTECTED_HTTP_METHODS.contains(request.getMethod())) {
            final String xRequestedWithHeaderValue = request.getHeader("X-Requested-With");

            if (xRequestedWithHeaderValue == null || !xRequestedWithHeaderValue.equals("XMLHttpRequest")) {
                throw new RuntimeException("hi jason");
            }
        }

        chain.doFilter(request, response);
    }
}
