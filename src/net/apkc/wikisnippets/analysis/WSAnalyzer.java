package net.apkc.wikisnippets.analysis;

// Apache Lucene
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

// IO
import java.io.Reader;

/**
 * Analyzer class for the application. All analyzers available in the
 * application must extend this class.
 *
 * @author K-Zen
 */
public abstract class WSAnalyzer extends Analyzer {

    /**
     * If the analyzer should stem the text.
     *
     * @param stemming If stemming is enabled.
     */
    public abstract void enableStemming(boolean stemming);

    /**
     * If the analyzer should filter the text.
     *
     * @param filtering If filtering is enabled.
     */
    public abstract void enableFiltering(boolean filtering);

    /**
     * Creates a TokenStream which tokenizes all the text in the provided
     * Reader.
     *
     * @param fieldName The name of the Lucene field.
     * @param reader    The Lucene reader for the token stream.
     *
     * @return The text tokenized/analyzed/filtered.
     */
    @Override
    public abstract TokenStream tokenStream(String fieldName, Reader reader);
}
