package net.apkc.wikisnippets.analysis;

// IO
import java.io.IOException;
import java.io.Reader;

/**
 * An efficient implementation of JavaCC's CharStream interface.
 *
 * <p>Note that this does not do line-number counting, but instead keeps track
 * of the character position of the token in the input, as required by Lucene's
 * {@link org.apache.lucene.analysis.Token} API.</p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
final class FastCharStream implements CharStream {

    char[] buffer = null;
    int bufferLength = 0; // end of valid chars
    int bufferPosition = 0; // next char to read
    int tokenStart = 0; // offset in buffer
    int bufferStart = 0; // position in file of buffer
    Reader input; // source of chars

    public FastCharStream(Reader r) {
        this.input = r;
    }

    @Override
    public final char readChar() throws IOException {
        if (this.bufferPosition >= this.bufferLength) {
            refill();
        }

        return this.buffer[this.bufferPosition++];
    }

    private void refill() throws IOException {
        int newPosition = this.bufferLength - this.tokenStart;

        if (this.tokenStart == 0) { // token won't fit in buffer
            if (this.buffer == null) { // first time: alloc buffer
                this.buffer = new char[2048];
            }
            else if (this.bufferLength == this.buffer.length) { // grow buffer
                char[] newBuffer = new char[this.buffer.length * 2];
                System.arraycopy(this.buffer, 0, newBuffer, 0, this.bufferLength);
                this.buffer = newBuffer;
            }
        }
        else { // shift token to front
            System.arraycopy(this.buffer, this.tokenStart, this.buffer, 0, newPosition);
        }

        this.bufferLength = newPosition; // update state
        this.bufferPosition = newPosition;
        this.bufferStart += this.tokenStart;
        this.tokenStart = 0;

        // fill space in buffer
        int charsRead = this.input.read(this.buffer, newPosition, this.buffer.length - newPosition);

        if (charsRead == -1) {
            throw new IOException("Read past EOF!");
        }
        else {
            this.bufferLength += charsRead;
        }
    }

    @Override
    public final char BeginToken() throws IOException {
        this.tokenStart = this.bufferPosition;
        return readChar();
    }

    @Override
    public final void backup(int amount) {
        this.bufferPosition -= amount;
    }

    @Override
    public final String GetImage() {
        return new String(this.buffer, this.tokenStart, this.bufferPosition - this.tokenStart);
    }

    @Override
    public final char[] GetSuffix(int len) {
        char[] value = new char[len];
        System.arraycopy(this.buffer, this.bufferPosition - len, value, 0, len);

        return value;
    }

    @Override
    public final void Done() {
        try {
            this.input.close();
        }
        catch (IOException e) {
        }
    }

    @Override
    public final int getColumn() {
        return this.bufferStart + this.bufferPosition;
    }

    @Override
    public final int getLine() {
        return 1;
    }

    @Override
    public final int getEndColumn() {
        return this.bufferStart + this.bufferPosition;
    }

    @Override
    public final int getEndLine() {
        return 1;
    }

    @Override
    public final int getBeginColumn() {
        return this.bufferStart + this.tokenStart;
    }

    @Override
    public final int getBeginLine() {
        return 1;
    }
}
