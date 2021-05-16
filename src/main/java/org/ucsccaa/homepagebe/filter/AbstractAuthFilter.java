package org.ucsccaa.homepagebe.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.ucsccaa.homepagebe.authentication.Authentication;
import org.ucsccaa.homepagebe.exceptions.ExceptionHandler;
import org.ucsccaa.homepagebe.exceptions.GenericServiceException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractAuthFilter implements Filter {
    protected abstract boolean filterable(HttpServletRequest request, HttpServletResponse response);

    @Autowired
    protected Authentication authentication;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        if (!filterable(httpServletRequest, httpServletResponse)) chain.doFilter(request, response);

        try {
            String token = httpServletRequest.getHeader("authorization");
            authentication.validateToken(token);
            chain.doFilter(request, response);
        } catch (GenericServiceException e) {
            ExceptionHandler exceptionHandler = e.getExceptionHandler();
            httpServletResponse.setStatus(exceptionHandler.getStatusCodeValue());
            httpServletResponse.setContentType("application/json");
            response.getOutputStream().write(new ObjectMapper().writeValueAsBytes(exceptionHandler.getResponseEntity().getBody()));
        }
    }
}