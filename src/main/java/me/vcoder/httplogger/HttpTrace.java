package me.vcoder.httplogger;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author baodn
 * Created on 11 Apr 2018
 */
public final class HttpTrace {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpTrace.class);

    private final Instant timestamp = Instant.now();

    private volatile Principal principal;

    private volatile Session session;

    private final Request request;

    private volatile Response response;

    private volatile Long timeTaken;

    HttpTrace(TraceableRequest request) {
        this.request = new Request(request);
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    void setPrincipal(java.security.Principal principal) {
        if (principal != null) {
            this.principal = new Principal(principal.getName());
        }
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public Session getSession() {
        return this.session;
    }

    void setSessionId(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            this.session = new Session(sessionId);
        }
    }

    public Request getRequest() {
        return this.request;
    }

    public Response getResponse() {
        return this.response;
    }

    void setResponse(Response response) {
        this.response = response;
    }

    public Long getTimeTaken() {
        return this.timeTaken;
    }

    void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    @Override
    public String toString() {
        String output = "\n=========================================================\n" + request +
                "\nTimestamp:" + timestamp +
                "\nTimeTaken:" + timeTaken;
        if(this.principal != null) {
            output += "\n" + principal;
        }
        if(this.session != null) {
            output += "\n" + session;
        }
        return output + "\n" + response + "\n=========================================================\n";
    }

    /**
     * Trace of an HTTP request.
     */
    public static final class Request {

        private final String method;

        private final URI uri;

        private final Map<String, List<String>> headers;

        private final String remoteAddress;

        private final String body;

        private Request(TraceableRequest request) {
            this.method = request.getMethod();
            this.uri = request.getUri();
            this.headers = request.getHeaders();
            this.remoteAddress = request.getRemoteAddress();
            this.body = request.getBody();
        }

        @Override
        public String toString() {
            String out = "Request:" +
                    "\n\tMethod:" + method +
                    "\n\tURI:" + uri +
                    "\n\tRemoteAddress:" + remoteAddress +
                    "\n\tHeaders:";
            for (Map.Entry<String, List<String>> entry :
                    headers.entrySet()) {
                out += "\n\t\t" + entry.getKey() + ":" + entry.getValue();
            }
            out += "\n\tRequest Body:\n--------------------------------------\n" + this.body + "\n--------------------------------------";
            return out;
        }
    }

    /**
     * Trace of an HTTP response.
     */
    public static final class Response {

        private final int status;

        private final Map<String, List<String>> headers;

        private final String body;

        Response(TraceableResponse response) {
            this.status = response.getStatus();
            this.headers = response.getHeaders();
            this.body = response.getBody();
        }

        @Override
        public String toString() {
            String out = "Response:" +
                    "\n\tStatus:" + status +
                    "\n\tHeaders:";
            for (Map.Entry<String, List<String>> entry :
                    headers.entrySet()) {
                out += "\n\t\t" + entry.getKey() + ":" + entry.getValue();
            }
            out += "\n\tResponse Body:\n--------------------------------------\n" + this.body + "\n--------------------------------------";
            return out;
        }
    }

    /**
     * Session associated with an HTTP request-response exchange.
     */
    public static final class Session {

        private final String id;

        private Session(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        @Override
        public String toString() {
            return "Session Id:" + id + "\n";
        }
    }

    /**
     * Principal associated with an HTTP request-response exchange.
     */
    public static final class Principal {

        private final String name;

        private Principal(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return "Principal Name:" + name + "\n";
        }
    }

}
