package net.apkc.wikisnippets.tasks.run;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

// WikiSnippets
import net.apkc.wikisnippets.tasks.Task;

/**
 * Task to escape characters. i.e. & =&gt; &amp;
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class ReplaceCharacters extends Task {

    static final Logger LOG = Logger.getLogger(ReplaceCharacters.class.getName());
    String text;
    HashMap<String, String> result;

    private ReplaceCharacters() {
    }

    public static ReplaceCharacters newBuild() {
        return new ReplaceCharacters();
    }

    public ReplaceCharacters setText(String text) {
        this.text = text;
        return this;
    }

    public String getText() {
        return text;
    }

    public HashMap<String, String> getResult() {
        return result;
    }

    @Override
    public void reportProgress(int progress) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object doInBackground() throws Exception {
        result = new HashMap<>();
        Pattern p;
        Matcher m1;
        Matcher m2;
        Matcher m3;
        Matcher m4;

        try {
            // "&" que deben ser reemplazados por "&amp;".
            String regex3 = "&(?!amp\\;)"; // Negative Lookahead
            p = Pattern.compile(regex3, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            m3 = p.matcher(text);
            p = Pattern.compile(regex3, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("&amp;");

            // "<" que deben ser reemplazados por "&lt;".
            String regex1 = "(\\<)";
            p = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            m1 = p.matcher(text);
            p = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("&lt;");

            // ">" que deben ser reemplazados por "&gt;".
            String regex2 = "(\\>)";
            p = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            m2 = p.matcher(text);
            p = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("&gt;");

            // "=" que no este acompañado de otro "=". Debe ser reemplazado por su
            // codigo de entidad HTML (&#61;).
            String regex4 = "(?<!\\=)\\=(?!\\=)"; // Negative Lookahead & Lookbehind
            p = Pattern.compile(regex4, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            m4 = p.matcher(text);
            p = Pattern.compile(regex4, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("&#61;");
        }
        catch (PatternSyntaxException e) {
            // #TODO: DO something!
            return null;
        }

        int ocurrences = 0;
        while (m1.find()) {
            ocurrences++;
        }
        while (m2.find()) {
            ocurrences++;
        }
        while (m3.find()) {
            ocurrences++;
        }
        while (m4.find()) {
            ocurrences++;
        }

        result.put("ocurrences", new Integer(ocurrences).toString());
        result.put("processedText", text);
        result.put("pattern", p.pattern());

        return result;
    }
}
