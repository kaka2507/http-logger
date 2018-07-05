package me.vcoder.httplogger;

import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;
import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author baodn
 * Created on 23 Mar 2018
 */
public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
    public static final String UTF8 = "UTF-8";
    public static final Charset UTF8_CHARSET = Charset.forName(UTF8);

    private ByteArrayOutputStream cachedBytes;
    private Map<String, String[]> parameterMap;

    public MultiReadHttpServletRequest(HttpServletRequest httpServletRequest)
            throws IOException {
        super(httpServletRequest);
        // Read the request body and save it as a byte array output stream
        cachedBytes = new ByteArrayOutputStream();
        IOUtils.copy(super.getInputStream(), cachedBytes);
    }

    public static void toMap(Iterable<NameValuePair> inputParams, Map<String, String[]> toMap) {
        for (NameValuePair e : inputParams) {
            String key = e.getName();
            String value = e.getValue();
            if (toMap.containsKey(key)) {
                String[] newValue = ObjectArrays.concat(toMap.get(key), value);
                toMap.remove(key);
                toMap.put(key, newValue);
            } else {
                toMap.put(key, new String[]{value});
            }
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStreamImpl(new ByteArrayInputStream(cachedBytes.toByteArray()));
    }

    @Override
    public String getParameter(String key) {
        Map<String, String[]> parameterMap = getParameterMap();
        String[] values = parameterMap.get(key);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public String[] getParameterValues(String key) {
        Map<String, String[]> parameterMap = getParameterMap();
        return parameterMap.get(key);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (parameterMap == null) {
            Map<String, String[]> result = new LinkedHashMap<>();
            decode(getQueryString(), result);
            decode(getPostBodyAsString(), result);
            parameterMap = Collections.unmodifiableMap(result);
        }
        return parameterMap;
    }

    private void decode(String queryString, Map<String, String[]> result) {
        if (queryString != null) toMap(decodeParams(queryString), result);
    }

    private Iterable<NameValuePair> decodeParams(String body) {
        Iterable<NameValuePair> params = URLEncodedUtils.parse(body, UTF8_CHARSET);
        try {
            String cts = getContentType();
            if (cts != null) {
                ContentType ct = ContentType.parse(cts);
                if (ct.getMimeType().equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
                    List<NameValuePair> postParams = URLEncodedUtils.parse(IOUtils.toString(getReader()), UTF8_CHARSET);
                    params = Iterables.concat(params, postParams);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return params;
    }

    public String getPostBodyAsString() {
        try {
            String enc = getCharacterEncoding();
            if (enc == null)
                enc = "UTF-8";
            return cachedBytes.toString(enc);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String enc = getCharacterEncoding();
        if (enc == null)
            enc = "UTF-8";
        return new BufferedReader(new InputStreamReader(getInputStream(), enc));
    }

    private class ServletInputStreamImpl extends ServletInputStream {

        private final ByteArrayInputStream is;

        public ServletInputStreamImpl(ByteArrayInputStream is) {
            this.is = is;
        }

        @Override
        public int read() throws IOException {
            return is.read();
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public synchronized void mark(int i) {
            throw new RuntimeException(new IOException(
                    "mark/reset not supported"));
        }

        @Override
        public synchronized void reset() throws IOException {
            throw new IOException("mark/reset not supported");
        }

        @Override
        public boolean isFinished() {
            return is.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new RuntimeException("ReadListener is not implemented");
        }
    }

}
