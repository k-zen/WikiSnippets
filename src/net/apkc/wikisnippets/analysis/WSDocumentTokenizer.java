package net.apkc.wikisnippets.analysis;

// Apache Lucene
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

// IO
import java.io.IOException;
import java.io.Reader;

/**
 * Default tokenizer for the application.
 *
 * @author K-Zen
 */
public final class WSDocumentTokenizer extends Tokenizer implements AnalysisConstants {

    private final WSAnalysisTokenManager tokenManager;
    private final TermAttribute termAtt;
    private final PositionIncrementAttribute posIncrAtt;
    private final TypeAttribute typeAtt;
    private final OffsetAttribute offsetAtt;

    /**
     * Construct a tokenizer for the text in a Reader.
     *
     * @param reader The reader to be used.
     */
    public WSDocumentTokenizer(Reader reader) {
        super(reader);
        this.tokenManager = new WSAnalysisTokenManager(reader);
        this.termAtt = addAttribute(TermAttribute.class);
        this.offsetAtt = addAttribute(OffsetAttribute.class);
        this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
        this.typeAtt = addAttribute(TypeAttribute.class);
    }

    /**
     * Returns the next token in the stream, or null at EOF.
     */
    private Token next() throws IOException {
        net.apkc.wikisnippets.analysis.Token t;

        try {
            loop:
            while (true) {
                t = tokenManager.getNextToken();
                switch (t.kind) { // skip query syntax tokens
                    case EOF:
                    case WORD:
                    case ACRONYM:
                    case SIGRAM:
                        break loop;
                    default:
                }
            }
        }
        catch (TokenMgrError e) { // translate exceptions
            throw new IOException("Tokenizer error:" + e);
        }

        return (t.kind == EOF) ? null : new Token(t.image, t.beginColumn, t.endColumn, tokenImage[t.kind]);
    }

    @Override
    public boolean incrementToken() throws IOException {
        this.clearAttributes();
        final Token t = next();
        if (t != null) {
            this.termAtt.setTermBuffer(t.termBuffer(), 0, t.termLength());
            this.offsetAtt.setOffset(t.startOffset(), t.endOffset());
            this.posIncrAtt.setPositionIncrement(t.getPositionIncrement());
            this.typeAtt.setType(t.type());

            return true;
        }
        else {
            return false;
        }
    }
}
