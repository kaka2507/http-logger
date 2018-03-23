package me.vcoder.httplogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author baodn
 * Created on 23 Mar 2018
 */
@Component
public class LogRequestFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogRequestFilter.class);

    private static AtomicLong requestId = new AtomicLong(1L);

    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("RequestLoggingFilter initialized");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        Thread t = Thread.currentThread();

        if (LOGGER.isInfoEnabled() && request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            MultiReadHttpServletRequest requestWrapper = new MultiReadHttpServletRequest(
                    httpServletRequest);
            try {
                t.setName("Request-" + requestId.getAndIncrement());
            } catch (SecurityException e) {
            }
            StringBuilder sb = new StringBuilder();
            sb.append(System.lineSeparator());
            sb.append(
                    "....................................................................")
                    .append(System.lineSeparator());
            sb.append("URI : " + httpServletRequest.getRequestURI()).append(
                    System.lineSeparator());
            sb.append("IP : " + httpServletRequest.getRemoteAddr()).append(
                    System.lineSeparator());
            sb.append("Method : " + httpServletRequest.getMethod()).append(
                    System.lineSeparator());
            sb.append("----Query String----").append(System.lineSeparator());
            if (requestWrapper.getQueryString() != null)
                sb.append(requestWrapper.getQueryString()).append(
                        System.lineSeparator());
            sb.append("----Params----").append(System.lineSeparator());
            logPostData(requestWrapper, sb);
            sb.append("----Header----").append(System.lineSeparator());
            Enumeration<String> headerNames = httpServletRequest
                    .getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement();
                String value = httpServletRequest.getHeader(key);
                sb.append(key + " : " + value).append(System.lineSeparator());
            }
            sb.append(
                    "....................................................................")
                    .append(System.lineSeparator());
            LOGGER.info(sb.toString());

            chain.doFilter(requestWrapper, response);
            return;
        }

        // pass the request along the filter chain
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    private void logPostData(HttpServletRequest req, StringBuilder sb) {
        try {
            BufferedReader reader = req.getReader();
            reader.mark(10000);

            String line;
            do {
                line = reader.readLine();
                if (line != null)
                    sb.append(line).append(System.lineSeparator());
            } while (line != null);
            reader.reset();
            reader.close();
        } catch (IOException e) {
            LOGGER.warn("getPostData couldn't.. get the post data", e);
        }
    }

}

