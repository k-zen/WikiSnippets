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
 * Task to remove tables embedded in the text. Support up to 2 levels.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class RemoveTables extends Task {

    static final Logger LOG = Logger.getLogger(RemoveTables.class.getName());
    String text;
    HashMap<String, String> result;

    private RemoveTables() {
    }

    public static RemoveTables newBuild() {
        return new RemoveTables();
    }

    public RemoveTables setText(String text) {
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
            // {| border=&quot;0&quot; style=&quot;border: 1px solid #999; background-color:#FFFFFF&quot;
            //  |-align=&quot;center&quot;
            //  ! &amp;nbsp;
            //  |-align=&quot;center&quot;
            //  | colspan=&quot;3&quot; | &lt;small&gt;Fuente: [http://www.iea.ad/cres/observatori/temes/llengua3trimestre2005.htm. Encuesta de usos lingüísticos en Andorra - 2004]&lt;/small&gt;
            //  |}
            // # Problemas:
            // 1. Hay algunas tablas anidadas que no estan siendo removidas. Ej. Articulo: Torre Eiffel - Datos técnicos
            String r =
                   ""
                    + "\\{\\|                "
                    + "   (?:(?!\\{\\|).)*?  " // Negative Lookahead
                    + "\\|\\}                ";
            p = Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m = p.matcher(text);
            // 1er Nivel.
            p = Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("");
            // 2do Nivel.
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
