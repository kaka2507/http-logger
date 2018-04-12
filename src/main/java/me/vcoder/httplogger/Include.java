package me.vcoder.httplogger;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author baodn
 * Created on 11 Apr 2018
 */
public enum Include {

    /**
     * Include request headers.
     */
    REQUEST_HEADERS,

    /**
     * Include response headers.
     */
    RESPONSE_HEADERS,

    /**
     * Include "Cookie" header (if any) in request headers and "Set-Cookie" (if any) in
     * response headers.
     */
    COOKIE_HEADERS,

    /**
     * Include authorization header (if any).
     */
    AUTHORIZATION_HEADER,

    /**
     * Include the principal.
     */
    PRINCIPAL,

    /**
     * Include the remote address.
     */
    REMOTE_ADDRESS,

    /**
     * Include the session ID.
     */
    SESSION_ID,

    /**
     * Include the time taken to service the request in milliseconds.
     */
    TIME_TAKEN;

    private static final Set<Include> DEFAULT_INCLUDES;

    static {
        Set<Include> defaultIncludes = new LinkedHashSet<>();
        defaultIncludes.add(Include.REQUEST_HEADERS);
        defaultIncludes.add(Include.RESPONSE_HEADERS);
        defaultIncludes.add(Include.COOKIE_HEADERS);
        defaultIncludes.add(Include.AUTHORIZATION_HEADER);
        defaultIncludes.add(Include.PRINCIPAL);
        defaultIncludes.add(Include.REMOTE_ADDRESS);
        defaultIncludes.add(Include.SESSION_ID);
        defaultIncludes.add(Include.TIME_TAKEN);
        DEFAULT_INCLUDES = Collections.unmodifiableSet(defaultIncludes);
    }

    /**
     * Return the default {@link Include}.
     * @return the default include.
     */
    public static Set<Include> defaultIncludes() {
        return DEFAULT_INCLUDES;
    }

}
