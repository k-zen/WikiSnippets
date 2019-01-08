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
 * Task to remove math formulas embedded in the text.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class RemoveMathFormulas extends Task {

    static final Logger LOG = Logger.getLogger(RemoveMathFormulas.class.getName());
    String text;
    HashMap<String, String> result;

    private RemoveMathFormulas() {
    }

    public static RemoveMathFormulas newBuild() {
        return new RemoveMathFormulas();
    }

    public RemoveMathFormulas setText(String text) {
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
        Matcher m;

        try {
            // <math>\rho(\theta) = \frac{a(1-e^2)}{1+e\cos\theta}</math>
            String r =
                   ""
                    + "(?:\\<|&lt;|&amp;lt;)math(?:\\>|&gt;|&amp;gt;)     " // TODO: Colocado una opcion mas que es para etiquetas &amp;lt; (Las cuales deben ser corregidas en pasos anteriores.
                    + "  (.*?)                                            " // Grupo 1. El nombre del archivo de la imagen.
                    + "(?:\\<|&lt;|&amp;lt;)\\/math(?:\\>|&gt;|&amp;gt;)  ";
            p = Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m = p.matcher(text);
            p = Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("");
        }
        catch (PatternSyntaxException e) {
            // #TODO: DO something!
            return null;
        }

        int ocurrences = 0;
        while (m.find()) {
            ocurrences++;
        }

        result.put("ocurrences", new Integer(ocurrences).toString());
        result.put("processedText", text);
        result.put("pattern", p.pattern());

        return result;
    }
}
