package me.vcoder.httplogger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * @author baodn
 * Created on 11 Apr 2018
 */
final class TraceableHttpServletResponse implements TraceableResponse {

    private final CaptureBodyHttpServletResponse delegate;

    TraceableHttpServletResponse(CaptureBodyHttpServletResponse response) {
        this.delegate = response;
    }

    @Override
    public int getStatus() {
        return this.delegate.getStatus();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return extractHeaders();
    }

    private Map<String, List<String>> extractHeaders() {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (String name : this.delegate.getHeaderNames()) {
            headers.put(name, new ArrayList<>(this.delegate.getHeaders(name)));
        }
        return headers;
    }

    @Override
    public String getBody() {
        try {
            return this.delegate.getBody();
        } catch (UnsupportedEncodingException ex) {
            return "";
        }
    }
}
