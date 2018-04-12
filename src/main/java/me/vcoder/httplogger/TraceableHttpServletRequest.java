package me.vcoder.httplogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

/**
 * @author baodn
 * Created on 11 Apr 2018
 */
final class TraceableHttpServletRequest implements TraceableRequest {

    private final HttpServletRequest request;

    TraceableHttpServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getMethod() {
        return this.request.getMethod();
    }

    @Override
    public URI getUri() {
        StringBuffer urlBuffer = this.request.getRequestURL();
        if (StringUtils.hasText(this.request.getQueryString())) {
            urlBuffer.append("?");
            urlBuffer.append(this.request.getQueryString());
        }
        return URI.create(urlBuffer.toString());
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return extractHeaders();
    }

    @Override
    public String getRemoteAddress() {
        return this.request.getRemoteAddr();
    }

    private Map<String, List<String>> extractHeaders() {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Enumeration<String> names = this.request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, toList(this.request.getHeaders(name)));
        }
        return headers;
    }

    private List<String> toList(Enumeration<String> enumeration) {
        List<String> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    @Override
    public Map<String, String[]> getParams() {
        return request.getParameterMap();
    }
}

