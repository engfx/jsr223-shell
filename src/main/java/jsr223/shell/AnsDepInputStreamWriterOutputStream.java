package jsr223.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class AnsDepInputStreamWriterOutputStream extends OutputStream {
	/** Initial buffer size. */
    private static final int INITIAL_SIZE = 132;

    /** Carriage return */
    private static final int CR = 0x0d;

    /** Linefeed */
    private static final int LF = 0x0a;

    /** the internal buffer */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(
            INITIAL_SIZE);

    private boolean skip = false;
    private boolean shouldFlush = false;

    private List<Writer> destinations = new ArrayList<Writer>();

    private final String charsetName;


    public AnsDepInputStreamWriterOutputStream(Writer writer, String charsetName) {
    	this.destinations.add(writer);
    	this.charsetName = charsetName;
	}
    
    public AnsDepInputStreamWriterOutputStream(List<Writer> writers, String charsetName) {
    	this.destinations.addAll(writers);
    	this.charsetName = charsetName;
	}

    /**
     * Write the data to the buffer and flush the buffer, if a line separator is
     * detected.
     *
     * @param cc data to log (byte).
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public synchronized void write(final int cc) throws IOException {
        final byte c = (byte) cc;
        if (c == '\n' || c == '\r') {
            if (!skip) {
                processBuffer();
            }
        } else {
            buffer.write(cc);
        }
        skip = c == '\r';
    }

    /**
     * Flush this log stream.
     * @throws IOException 
     *
     * @see java.io.OutputStream#flush()
     */
    @Override
    public synchronized void flush() throws IOException {
        if (buffer.size() > 0) {
            processBuffer();
        }
        for (Writer writer : destinations)
        	writer.flush();
    }

    /**
     * Writes all remaining data from the buffer.
     * first flush then close this stream.
     *
     * @see java.io.OutputStream#close()
     */
    @Override
    public synchronized void close() throws IOException {
        if (buffer.size() > 0) {
            processBuffer();
        }
        try {
        	for (Writer writer : destinations) {
	        	if (writer == null)
	                return;
	            writer.close();
	            writer = null;
        	}
        }
        catch (IOException x) {
            
        }
    }

    /**
     * Write a block of characters to the output stream
     *
     * @param b the array containing the data
     * @param off the offset into the array where data starts
     * @param len the length of block
     * @throws java.io.IOException if the data cannot be written into the stream.
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public synchronized void write(final byte[] b, final int off, final int len)
            throws IOException {
        // find the line breaks and pass other chars through in blocks
        int offset = off;
        int blockStartOffset = offset;
        int remaining = len;
        while (remaining > 0) {
            while (remaining > 0 && b[offset] != LF && b[offset] != CR) {
                offset++;
                remaining--;
                shouldFlush = true; //skip blank lines.
            }
            // either end of buffer or a line separator char
            final int blockLength = offset - blockStartOffset;
            if (blockLength > 0) {
                buffer.write(b, blockStartOffset, blockLength);
            }
            while (remaining > 0 && (b[offset] == LF || b[offset] == CR)) {
                write(b[offset]);
                offset++;
                remaining--;
            }
            blockStartOffset = offset;
        }
        
        //just flush when enough. last one should be done outside.
        if (shouldFlush) {
        	for (Writer writer : destinations)
        		writer.flush();
        }
    }

    /**
     * Converts the buffer to a string and sends it to {@code processLine}.
     * @throws IOException 
     */
    private void processBuffer() throws IOException {
        processLine(buffer.toString(charsetName));
        buffer.reset();
    }

    /**
     * Logs a line to the log system of the user.
     *
     * @param line
     *            the line to log.
     * @throws IOException 
     */
    private void processLine(final String line) throws IOException {
    	for (Writer writer : destinations) {
    		writer.append(line).append("\n");
    	}
    }

}
