package net.apkc.wikisnippets.analysis.analyzers;

// AIME
import net.apkc.wikisnippets.analysis.WSAnalyzer;

// Apache Lucene
import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

// IO
import java.io.Reader;

// Util
import java.util.Set;

/**
 * Spanish language text analyzer.
 *
 * @author K-Zen
 */
public class SpanishAnalyzer extends WSAnalyzer {

    private Set<Object> stopWords;
    private boolean stemming = true;  // Always enabled unless explicitly disabled.
    private boolean filtering = true; // Always enabled unless explicitly disabled.

    public SpanishAnalyzer() {
        this.stopWords = StopFilter.makeStopSet(StopWords.SPANISH_STOP_WORDS);
    }

    @Override
    public void enableStemming(boolean stemming) {
        this.stemming = stemming;
    }

    @Override
    public void enableFiltering(boolean filtering) {
        this.filtering = filtering;
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new StandardTokenizer(Version.LUCENE_30, reader);

        // First filter with a very standard filter/analyzer.
        result = new StandardFilter(result);

        // Remove stop words.
        result = new StopFilter(false, result, stopWords);

        // Stem the words.
        if (this.stemming) {
            result = new SpanishLightStemFilter(result);
        }

        // Filter the words.
        if (this.filtering) {
            result = new ASCIIFoldingFilter(result);
        }

        // Always to lowercase.
        result = new LowerCaseFilter(result);

        return result;
    }

    @Override
    public String toString() {
        return "Spanish Language Analyzer";
    }
}
