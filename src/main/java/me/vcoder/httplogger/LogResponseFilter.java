package me.vcoder.httplogger;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.io.output.ProxyOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author baodn
 * Created on 23 Mar 2018
 */
@Component
public class LogResponseFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LogResponseFilter.class);

    @Value("${log.response.enable:true}")
    private Boolean enableResponseLog;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {

        ServletResponse responseWrapper = response;
        if (enableResponseLog) {
            responseWrapper = loggingResponseWrapper((HttpServletResponse) response);
        }
        filterChain.doFilter(request, responseWrapper);
    }

    private HttpServletResponse loggingResponseWrapper(
            HttpServletResponse response) {
        return new HttpServletResponseWrapper(response) {
            @Override
            public ServletOutputStream getOutputStream() throws IOException {

                return new DelegatingServletOutputStream(
                        new ConditionalProxyOutputStream(
                                super.getOutputStream(), loggingOutputStream(),
                                getResponse()));
            }
        };
    }

    private OutputStream loggingOutputStream() throws IOException {
        return System.out;
    }

    public static class DelegatingServletOutputStream extends ServletOutputStream {

        private final OutputStream targetStream;


        /**
         * Create a DelegatingServletOutputStream for the given target stream.
         * @param targetStream the target stream (never {@code null})
         */
        public DelegatingServletOutputStream(OutputStream targetStream) {
            Assert.notNull(targetStream, "Target OutputStream must not be null");
            this.targetStream = targetStream;
        }

        /**
         * Return the underlying target stream (never {@code null}).
         */
        public final OutputStream getTargetStream() {
            return this.targetStream;
        }


        @Override
        public void write(int b) throws IOException {
            this.targetStream.write(b);
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            this.targetStream.flush();
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.targetStream.close();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException();
        }
    }

    public static class ConditionalProxyOutputStream extends ProxyOutputStream {
        /** the second OutputStream to write to */
        protected OutputStream branch;
        protected ServletResponse response;
        private boolean testedHeaders;
        private OutputStream useThisBranch;

        /**
         * Constructs a TeeOutputStream.
         *
         * @param out
         *            the main OutputStream
         * @param branch
         *            the second OutputStream
         */
        public  ConditionalProxyOutputStream(OutputStream out,
                                             OutputStream branch, ServletResponse response) {
            super(out);
            this.branch = branch;
            this.response = response;
            testedHeaders = false;
            String responseType = response.getContentType();
            System.out.println();
            System.out.println("..................." + "Return type:" + responseType + ".................................");
        }

        protected OutputStream accessBranch() {
            if (!testedHeaders) {
                useThisBranch = branch;
                testedHeaders = true;
            }
            return useThisBranch;
        }

//        private boolean returningImage() {
//            String responseType = response.getContentType();
//            System.out.println();
//            System.out.print("..................." + "Return type:" + responseType + ".................................");
//            return false;
//        }

        /** @see java.io.OutputStream#write(byte[]) */
        public synchronized void write(byte[] b) throws IOException {
            super.write(b);
            OutputStream branch = accessBranch();
            if (branch == null)
                return;
            branch.write(b);
        }

        /** @see java.io.OutputStream#write(byte[], int, int) */
        public synchronized void write(byte[] b, int off, int len)
                throws IOException {
            super.write(b, off, len);
            OutputStream branch = accessBranch();
            if (branch == null)
                return;
            branch.write(b, off, len);
        }

        /** @see java.io.OutputStream#write(int) */
        public synchronized void write(int b) throws IOException {
            super.write(b);
            OutputStream branch = accessBranch();
            if (branch == null)
                return;
            branch.write(b);
        }

        /**
         * Flushes both streams.
         *
         * @see java.io.OutputStream#flush()
         */
        public void flush() throws IOException {
            super.flush();
            OutputStream branch = accessBranch();
            if (branch == null)
                return;
            branch.flush();
        }

        /**
         * Closes both streams.
         *
         * @see java.io.OutputStream#close()
         */
        public void close() throws IOException {
            super.close();
            OutputStream branch = accessBranch();
            if (branch == null)
                return;
            branch.close();
        }
    }
}

