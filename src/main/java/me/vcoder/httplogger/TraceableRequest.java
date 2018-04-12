package me.vcoder.httplogger;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author baodn
 * Created on 11 Apr 2018
 */
public interface TraceableRequest {
    /**
     * Returns the method (GET, POST, etc) of the request.
     * @return the method
     */
    String getMethod();

    /**
     * Returns the URI of the request.
     * @return the URI
     */
    URI getUri();

    /**
     * Returns a modifiable copy of the headers of the request.
     * @return the headers
     */
    Map<String, List<String>> getHeaders();

    /**
     * Returns the remote address from which the request was sent, if available.
     * @return the remote address or {@code null}
     */
    String getRemoteAddress();

    /**
     * Returns the params map
     * @return Request Body
     */
    public Map<String, String[]> getParams();
}
