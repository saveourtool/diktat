package org.cqfn.diktat.test.framework.common;

import mu.KLogger;
import mu.KotlinLogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class StreamGobbler extends Thread {
    private static final KLogger log = KotlinLogging.INSTANCE.logger(StreamGobbler.class.getName());
    private final InputStream inputStream;
    private final String streamType;
    private final ArrayList<String> result;
    private volatile boolean isStopped = false;

    /**
     * @param inputStream the InputStream to be consumed
     * @param streamType  the stream type (should be OUTPUT or ERROR)
     */
    public StreamGobbler(final InputStream inputStream, final String streamType) {
        this.inputStream = inputStream;
        this.streamType = streamType;
        this.result = new ArrayList<>();
    }

    /**
     * Consumes the output from the input stream and displays the lines consumed
     * if configured to do so.
     */
    @Override
    synchronized public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.defaultCharset())
            );
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                this.result.add(line);
            }
        } catch (IOException ex) {
                log.error("Failed to consume and display the input stream of type " + streamType + ".", ex);
        } finally {
            this.isStopped = true;
            notify();
        }
    }

    synchronized public List<String> getContent() {
        if (!this.isStopped) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    log.error("Cannot get content of output stream", e);
                }
        }
        return this.result;
    }
}
