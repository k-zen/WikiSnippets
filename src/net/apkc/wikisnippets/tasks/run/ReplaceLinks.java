package net.apkc.wikisnippets.tasks.run;

// Log4j
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.apkc.wikisnippets.tasks.Task;
import org.apache.log4j.Logger;

/**
 * Task to replace links for anchor text. Also removes useless links, like Categories or other languages.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class ReplaceLinks extends Task
{

    static final Logger LOG = Logger.getLogger(ReplaceLinks.class.getName());
    String text;
    HashMap<String, String> result;

    private ReplaceLinks()
    {
    }

    public static ReplaceLinks newBuild()
    {
        return new ReplaceLinks();
    }

    public ReplaceLinks setText(String text)
    {
        this.text = text;
        return this;
    }

    public String getText()
    {
        return text;
    }

    public HashMap<String, String> getResult()
    {
        return result;
    }

    @Override
    public void reportProgress(int progress)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object doInBackground() throws Exception
    {
        result = new HashMap<>();
        Pattern p;
        Matcher m1;
        Matcher m2;
        Matcher m3;

        try
        {

            // [[pms:Andorra]], [[Categoría:Miembros de la ONU]]
            // # Problemas:
            // 1. Remueve enlaces validos del tipo: - [[Historia de la Union Civica Radical: 1955-1972|UCRI]]
            //                                      - [[Anexo:Paises por indice de desarrollo humano|Indice de Desarrollo Humano (IDH)]]
            //      Correccion: Colocar una proteccion de no remover enlaces que posean palabras de mas de
            //                  9 caracteres antes del doble punto. No es a prueba de fallos pero reduce las chances.
            //                  (NO FUNCIONA ESTA CORRECCION - El parseado funciona extremadamente lento.)
            //      Correccion: Colocar el pipe en la segunda clase de caracteres a excluir, de esa manera obviamos dichos enlaces. Tampoco es a prueba
            //                  de fallos pero reduce drasticamente los errores. (CORREGIDO CON ESTA OPCION)
            String r1
                   = ""
                    + "\\[\\[                                             "
                    + "  (?![:]?(?:archivo|image[n]?|media|file|anexo):)  " // Negative Lookahead
                    + "  (?:[^\\[\\:\\]]*?)                               " // Incluir negacion para "]]", dado el sgte. caso: [[Erupción solar]]; '''CUADRO C''': (sin leyenda numérica).]]
                    + "    \\:                                            "
                    + "  (?:[^\\[]*?)                                     "
                    + "\\]\\]                                             ";
            p = Pattern.compile(r1, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m1 = p.matcher(text);
            p = Pattern.compile(r1, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("");

            // [[Rally Dakar|Rally Paris-Dakar]]
            String r2
                   = ""
                    + "(?:\\w*)                                       "
                    + "  \\[\\[                                       "
                    + "    (?![:]?(?:archivo|image[n]?|media|file):)  " // Negative Lookahead
                    + "    ([^\\[\\]]*?)                              "
                    + "       \\|                                     "
                    + "    ([^\\[]*?)                                 "
                    + "  \\]\\]                                       "
                    + "(?:\\w*)                                       ";
            p = Pattern.compile(r2, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m2 = p.matcher(text);
            p = Pattern.compile(r2, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("$2");

            // [[Federación Andorrana de Fútbol]]
            String r3
                   = ""
                    + "(?:\\w*)                                       "
                    + "  \\[\\[                                       "
                    + "    (?![:]?(?:archivo|image[n]?|media|file):)  " // Negative Lookahead
                    + "    ([^\\[\\|]*?)                              "
                    + "  \\]\\]                                       "
                    + "(?:\\w*)                                       ";
            p = Pattern.compile(r3, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m3 = p.matcher(text);
            p = Pattern.compile(r3, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("$1");
        }
        catch (PatternSyntaxException e)
        {
            // #TODO: DO something!
            return null;
        }

        int ocurrences = 0; // Numero de ocurrencias que encontramos.
        while (m1.find())
        {
            ocurrences++;
        }
        while (m2.find())
        {
            ocurrences++;
        }
        while (m3.find())
        {
            ocurrences++;
        }

        result.put("ocurrences", Integer.toString(ocurrences));
        result.put("processedText", text);
        result.put("pattern", p.pattern());

        return result;
    }
}
