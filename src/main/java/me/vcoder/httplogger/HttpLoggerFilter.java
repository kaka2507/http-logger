package me.vcoder.httplogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author baodn
 * Created on 11 Apr 2018
 */
@Component
public class HttpLoggerFilter extends OncePerRequestFilter implements Ordered {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpLoggerFilter.class);

    private int order = Ordered.HIGHEST_PRECEDENCE;
    private final HttpExchangeTracer tracer;

    @Value("${http.logger.filter.include:}")
    private String logIncludeFilter;
    private List<String> filterIncludeRegexes = new ArrayList<>();

    @Value("${http.logger.filter.exclude:}")
    private String logExcludeFilter;
    private List<String> filterExcludeRegexes = new ArrayList<>();

    @Value("${http.logger.enable:true}")
    private Boolean logEnable;

    public HttpLoggerFilter() {
        this.tracer = new HttpExchangeTracer(Include.defaultIncludes());
    }

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        if(logIncludeFilter.length() > 0) {
            String[] tmpRegexs = logIncludeFilter.split(",");
            for (String regex :
                    tmpRegexs) {
                filterIncludeRegexes.add(regex.trim());
            }
        }

        if(logExcludeFilter.length() > 0) {
            String[] tmpRegexs = logExcludeFilter.split(",");
            for (String regex :
                    tmpRegexs) {
                filterExcludeRegexes.add(regex.trim());
            }
        }
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    private boolean matchInclude(String uri) {
        if(filterIncludeRegexes.size() == 0) {
            return false;
        }
        for (String regex :
                filterIncludeRegexes) {
            if (uri.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchExclude(String uri) {
        if(filterExcludeRegexes.size() == 0) {
            return false;
        }
        for (String regex :
                filterExcludeRegexes) {
            if (uri.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldLog(String uri) {
        if(!logEnable)
            return false;

        if(matchInclude(uri)) {
            return true;
        }

        if(matchExclude(uri)) {
            return false;
        }

        if(filterIncludeRegexes.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if(!shouldLog(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        TraceableHttpServletRequest traceableRequest = new TraceableHttpServletRequest(request);
        HttpTrace trace = this.tracer.receivedRequest(traceableRequest);
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        }
        finally {
            TraceableHttpServletResponse traceableResponse = new TraceableHttpServletResponse(
                    status == response.getStatus() ? response
                            : new CustomStatusResponseWrapper(response, status));
            this.tracer.sendingResponse(trace, traceableResponse,
                    request::getUserPrincipal, () -> getSessionId(request));
            LOGGER.info(trace.toString());
        }
    }

    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : session.getId();
    }

    private static final class CustomStatusResponseWrapper
            extends HttpServletResponseWrapper {

        private final int status;

        private CustomStatusResponseWrapper(HttpServletResponse response, int status) {
            super(response);
            this.status = status;
        }

        @Override
        public int getStatus() {
            return this.status;
        }

    }

}
