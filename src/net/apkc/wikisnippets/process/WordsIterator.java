package net.apkc.wikisnippets.process;

// Apache Lucene
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

// I/O
import java.io.IOException;

// Util
import java.util.Iterator;

public class WordsIterator {

    private IndexReader reader;
    private String field;

    /**
     *
     * @param reader
     * @param field
     */
    public WordsIterator(IndexReader reader, String field) {
        this.reader = reader;
        this.field = field.intern();
    }

    /**
     *
     * @return
     */
    public final Iterator getWordsIterator() {
        return new LuceneIterator();
    }

    /**
     * Check whether the word exists in the index.
     * @param word
     * @throws IOException
     * @return true iff the word exists in the index
     */
    public boolean exist(String word, String fieldName) throws IOException {
        return reader.docFreq(new Term(fieldName, word)) > 0;
    }

    /**
     *
     */
    final class LuceneIterator implements Iterator {

        private TermEnum termEnum;
        private Term actualTerm;
        private boolean hasNextCalled;

        LuceneIterator() {
            try {
                termEnum = reader.terms(new Term(field));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         *
         * @return
         */
        @Override
        public Object next() {
            if (!hasNextCalled) {
                hasNext();
            }
            hasNextCalled = false;

            try {
                termEnum.next();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            return (actualTerm != null) ? actualTerm.text() : null;
        }

        /**
         *
         * @return
         */
        @Override
        public boolean hasNext() {
            if (hasNextCalled) {
                return actualTerm != null;
            }
            hasNextCalled = true;
            actualTerm = termEnum.term();
            // if there are no words return false
            if (actualTerm == null) {
                return false;
            }
            String currentField = actualTerm.field();
            // if the next word doesn't have the same field return false
            if (currentField == null ? field != null : !currentField.equals(field)) {
                actualTerm = null;
                return false;
            }

            return true;
        }

        /**
         *
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
