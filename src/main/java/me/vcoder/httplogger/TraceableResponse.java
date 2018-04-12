package me.vcoder.httplogger;

import java.util.List;
import java.util.Map;

/**
 * @author baodn
 * Created on 11 Apr 2018
 */
public interface TraceableResponse {
    /**
     * The status of the response.
     * @return the status
     */
    int getStatus();

    /**
     * Returns a modifiable copy of the headers of the response.
     * @return the headers
     */
    Map<String, List<String>> getHeaders();
}
