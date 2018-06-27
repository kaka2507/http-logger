package me.vcoder.httplogger;

import org.apache.commons.io.output.ProxyOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author baodn
 * Created on 26 Jun 2018
 */
public class CaptureBodyHttpServletResponse extends HttpServletResponseWrapper {
    private final static Logger LOGGER = LoggerFactory.getLogger(CaptureBodyHttpServletResponse.class);
    private ByteArrayOutputStream byteArrayOutputStream;
    public CaptureBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {

        return new DelegatingServletOutputStream(
                new ConditionalProxyOutputStream(
                        super.getOutputStream(), byteArrayOutputStream,
                        getResponse()));
    }

    public String getBody() throws UnsupportedEncodingException {
        return byteArrayOutputStream.toString("UTF-8");
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
         * @return the target output stream
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

        /**
         * Constructs a TeeOutputStream.
         *
         * @param out
         *            the main OutputStream
         * @param branch
         *            the second OutputStream
         * @param response
         *            the response
         */
        public  ConditionalProxyOutputStream(OutputStream out,
                                             OutputStream branch, ServletResponse response) {
            super(out);
            this.branch = branch;
            this.response = response;
        }

        /** @see java.io.OutputStream#write(byte[]) */
        public synchronized void write(byte[] b) throws IOException {
            super.write(b);
            if (branch == null)
                return;
            branch.write(b);
        }

        /** @see java.io.OutputStream#write(byte[], int, int) */
        public synchronized void write(byte[] b, int off, int len)
                throws IOException {
            super.write(b, off, len);
            if (branch == null)
                return;
            branch.write(b, off, len);
        }

        /** @see java.io.OutputStream#write(int) */
        public synchronized void write(int b) throws IOException {
            super.write(b);
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
            if (branch == null)
                return;
            branch.close();
        }
    }
}
