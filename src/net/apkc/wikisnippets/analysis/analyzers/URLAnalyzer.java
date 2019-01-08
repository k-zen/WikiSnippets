package net.apkc.wikisnippets.analysis.analyzers;

// WikiSnippets
import net.apkc.wikisnippets.analysis.WSAnalyzer;
import net.apkc.wikisnippets.analysis.WSDocumentTokenizer;

// Apache Lucene
import org.apache.lucene.analysis.TokenStream;

// I/O
import java.io.Reader;

/**
 * Default URL analyzer for the application.
 *
 * @author K-Zen
 */
public class URLAnalyzer extends WSAnalyzer {

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new WSDocumentTokenizer(reader);
        return result;
    }

    @Override
    public void enableStemming(boolean stemming) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void enableFiltering(boolean filtering) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
