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
 * This task removes templates from the text. Supports up to 15 nested templates.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class RemoveTemplates extends Task {

    static final Logger LOG = Logger.getLogger(RemoveTemplates.class.getName());
    String text;
    HashMap<String, String> result;

    private RemoveTemplates() {
    }

    public static RemoveTemplates newBuild() {
        return new RemoveTemplates();
    }

    public RemoveTemplates setText(String text) {
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

        try {
            // {{otros usos|Andorra (Teruel)|la localidad de la [[provincia de Teruel]] ([[España]])}}
            String r1 =
                   ""
                    + "\\{\\{               "
                    + "  (?:(?!\\{\\{).)*?  " // Negative Lookahead
                    + "\\}\\}               ";
            p = Pattern.compile(r1, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m1 = p.matcher(text);
            // Soporte para 15 niveles de etiquetas anidadas.
            for (int i = 0; i < 15; i++) {
                // Borra los templates hasta 15 niveles.
                p = Pattern.compile(r1, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                text = p.matcher(text).replaceAll("");
            }

            // {otros usos|Andorra (Teruel)|la localidad de la [[provincia de Teruel]] ([[España]])}
            String r2 = "\\{[^\\{]*?\\}";
            p = Pattern.compile(r2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            m2 = p.matcher(text);
            // 1er Nivel.
            p = Pattern.compile(r2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("");
            // 2do Nivel.
            p = Pattern.compile(r2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("");
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

        result.put("ocurrences", new Integer(ocurrences).toString());
        result.put("processedText", text);
        result.put("pattern", p.pattern());

        return result;
    }
}
