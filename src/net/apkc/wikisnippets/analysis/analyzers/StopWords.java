package net.apkc.wikisnippets.analysis.analyzers;

// Apache Commons
import org.apache.commons.lang.ArrayUtils;

/**
 * List of stop words in all languages supported by AIME.
 *
 * @author K-Zen
 */
public class StopWords {

    public static final String[] SPANISH_STOP_WORDS = {
        "a", "al", "algun", "alguno", "alguna", "algunos", "algunas", "ambos", "ampleamos", "ante", "antes", "aquel", "aquellos",
        "aquellas", "aqui", "arriba", "atras", "bajo", "bastante", "bien", "cada", "cierto", "ciertos", "cierta", "ciertas",
        "como", "con", "conseguir", "consigo", "consigue", "consigues", "conseguimos", "consiguen", "cual", "cuando", "de",
        "del", "dentro", "desde", "donde", "dos", "el", "ellos", "ellas", "emplear", "empleo", "empleais", "empleas",
        "emplean", "en", "encima", "entonces", "entre", "era", "eras", "eramos", "eran", "eres", "es", "ese", "estoy",
        "esta", "estaba", "estado", "estamos", "estais", "estan", "este", "fin", "fue", "fueron", "fui", "fuimos", "ha",
        "han", "hace", "haceis", "hacemos", "hacen", "hacer", "haces", "hacia", "hago", "incluso", "intentar", "intento",
        "intenta", "intentas", "intentamos", "intentais", "intentan", "ir", "la", "largo", "las", "lo", "los", "mas", "mientras",
        "mio", "modo", "muchos", "muy", "no", "nos", "nosotros", "o", "otro", "otros", "para", "porque", "por", "que", "quien",
        "pero", "por", "poder", "podemos", "podeis", "podria", "podrias", "podriamos", "podrian", "podriais", "primero", "puede",
        "pueden", "puedo", "saber", "sabes", "sabe", "sabemos", "sabeis", "saben", "se", "ser", "si", "sido", "sin", "siendo",
        "sobre", "sois", "solamente", "solo", "somos", "son", "soy", "su", "sus", "tambien", "te", "tenemos", "teneis", "tengo",
        "tener", "tiene", "tienen", "todo", "trabajo", "trabajar", "trabajas", "trabaja", "trabajamos", "trabajais", "trabajan",
        "tras", "tus", "tuyo", "ultimo", "un", "una", "unas", "unos", "uno", "usa", "usais", "usamos", "usan", "usar", "usas",
        "uso", "va", "vais", "valor", "vamos", "van", "vaya", "verdad", "verdadero", "verdadera", "vosotros", "vosotras", "voy",
        "y", "yo"
    };
    public static final String[] GERMAN_STOP_WORDS = {
        "einer", "eine", "eines", "einem", "einen", "der", "die", "das", "dass", "daß", "du", "er", "sie", "es", "was", "wer",
        "wie", "wir", "und", "oder", "ohne", "mit", "am", "im", "in", "aus", "auf", "ist", "sein", "war", "wird", "ihr", "ihre",
        "ihres", "als", "für", "von", "mit", "dich", "dir", "mich", "mir", "mein", "sein", "kein", "durch", "wegen", "wird"
    };
    public static final String[] ENGLISH_STOP_WORDS = {
        "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on",
        "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with"
    };

    /**
     * Given the word and language (2 letter code), this method will return if
     * the given word is a stop word or not.
     *
     * @param word The word to check if it's a stop word.
     * @param lang The language of the word.
     *
     * @return TRUE if the given word is a stop word, FALSE otherwise.
     */
    public static boolean itsStopWord(String word, String lang) {
        if (lang.equalsIgnoreCase("en")) {
            return ArrayUtils.contains(ENGLISH_STOP_WORDS, word.toLowerCase());
        }
        else if (lang.equalsIgnoreCase("es")) {
            return ArrayUtils.contains(SPANISH_STOP_WORDS, word.toLowerCase());
        }
        else {
            return ArrayUtils.contains(GERMAN_STOP_WORDS, word.toLowerCase());
        }
    }
}
