package me.vcoder.httplogger;

import java.net.URI;
import java.security.Principal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.springframework.http.HttpHeaders;

/**
 * @author baodn
 * Created on 11 Apr 2018
 */
public class HttpExchangeTracer {

    private final Set<Include> includes;

    /**
     * Creates a new {@code HttpExchangeTracer} that will use the given {@code includes}
     * to determine the contents of its traces.
     * @param includes the includes
     */
    public HttpExchangeTracer(Set<Include> includes) {
        this.includes = includes;
    }

    /**
     * Begins the tracing of the exchange that was initiated by the given {@code request}
     * being received.
     * @param request the received request
     * @return the HTTP trace for the
     */
    public final HttpTrace receivedRequest(TraceableRequest request) {
        return new HttpTrace(new FilteredTraceableRequest(request));
    }

    /**
     * Ends the tracing of the exchange that is being concluded by sending the given
     * {@code response}.
     * @param trace the trace for the exchange
     * @param response the response that concludes the exchange
     * @param principal a supplier for the exchange's principal
     * @param sessionId a supplier for the id of the exchange's session
     */
    public final void sendingResponse(HttpTrace trace, TraceableResponse response,
                                      Supplier<Principal> principal, Supplier<String> sessionId) {
        setIfIncluded(Include.TIME_TAKEN,
                () -> System.currentTimeMillis() - trace.getTimestamp().toEpochMilli(),
                trace::setTimeTaken);
        setIfIncluded(Include.SESSION_ID, sessionId, trace::setSessionId);
        setIfIncluded(Include.PRINCIPAL, principal, trace::setPrincipal);
        trace.setResponse(
                new HttpTrace.Response(new FilteredTraceableResponse(response)));
    }

    /**
     * Post-process the given mutable map of request {@code headers}.
     * @param headers the headers to post-process
     */
    protected void postProcessRequestHeaders(Map<String, List<String>> headers) {

    }

    private <T> T getIfIncluded(Include include, Supplier<T> valueSupplier) {
        return this.includes.contains(include) ? valueSupplier.get() : null;
    }

    private <T> void setIfIncluded(Include include, Supplier<T> supplier,
                                   Consumer<T> consumer) {
        if (this.includes.contains(include)) {
            consumer.accept(supplier.get());
        }
    }

    private Map<String, List<String>> getHeadersIfIncluded(Include include,
                                                           Supplier<Map<String, List<String>>> headersSupplier,
                                                           Predicate<String> headerPredicate) {
        if (!this.includes.contains(include)) {
            return new LinkedHashMap<>();
        }
        Map<String, List<String>> headers = headersSupplier.get();
        Iterator<String> keys = headers.keySet().iterator();
        while (keys.hasNext()) {
            if (!headerPredicate.test(keys.next())) {
                keys.remove();
            }
        }
        return headers;
    }

    private final class FilteredTraceableRequest implements TraceableRequest {

        private final TraceableRequest delegate;

        private FilteredTraceableRequest(TraceableRequest delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getMethod() {
            return this.delegate.getMethod();
        }

        @Override
        public URI getUri() {
            return this.delegate.getUri();
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return getHeadersIfIncluded(Include.REQUEST_HEADERS,
                    this.delegate::getHeaders, this::includedHeader);
        }

        private boolean includedHeader(String name) {
            if (name.equalsIgnoreCase(HttpHeaders.COOKIE)) {
                return HttpExchangeTracer.this.includes.contains(Include.COOKIE_HEADERS);
            }
            if (name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                return HttpExchangeTracer.this.includes
                        .contains(Include.AUTHORIZATION_HEADER);
            }
            return true;
        }

        @Override
        public String getRemoteAddress() {
            return getIfIncluded(Include.REMOTE_ADDRESS, this.delegate::getRemoteAddress);
        }

        @Override
        public Map<String, String[]> getParams() {
            return this.delegate.getParams();
        }
    }

    private final class FilteredTraceableResponse implements TraceableResponse {

        private final TraceableResponse delegate;

        private FilteredTraceableResponse(TraceableResponse delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getStatus() {
            return this.delegate.getStatus();
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return getHeadersIfIncluded(Include.RESPONSE_HEADERS,
                    this.delegate::getHeaders, this::includedHeader);
        }

        private boolean includedHeader(String name) {
            if (name.equalsIgnoreCase(HttpHeaders.SET_COOKIE)) {
                return HttpExchangeTracer.this.includes.contains(Include.COOKIE_HEADERS);
            }
            return true;
        }

    }

}

