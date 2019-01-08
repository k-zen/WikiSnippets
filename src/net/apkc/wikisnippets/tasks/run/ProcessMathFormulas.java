package net.apkc.wikisnippets.tasks.run;

// Log4j
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.apkc.wikisnippets.tasks.Task;
import org.apache.log4j.Logger;

/**
 * Task to process math formulas embedded in the text.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class ProcessMathFormulas extends Task
{

    static final Logger LOG = Logger.getLogger(ProcessMathFormulas.class.getName());
    String text;
    HashMap<Integer, String> result;

    private ProcessMathFormulas()
    {
    }

    public static ProcessMathFormulas newBuild()
    {
        return new ProcessMathFormulas();
    }

    public ProcessMathFormulas setText(String text)
    {
        this.text = text;
        return this;
    }

    public String getText()
    {
        return text;
    }

    public HashMap<Integer, String> getResult()
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
        Matcher m;

        try
        {
            // <math>\rho(\theta) = \frac{a(1-e^2)}{1+e\cos\theta}</math>
            String r
                   = ""
                    + "(?:\\<|&lt;|&amp;lt;)math(?:\\>|&gt;|&amp;gt;)     " // TODO: Colocado una opcion mas que es para etiquetas &amp;lt; (Las cuales deben ser corregidas en pasos anteriores.
                    + "  (.*?)                                            " // Grupo 1. El nombre del archivo de la imagen.
                    + "(?:\\<|&lt;|&amp;lt;)\\/math(?:\\>|&gt;|&amp;gt;)  ";
            p = Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m = p.matcher(text);

            // TODO
            // {{math|<VAR>&alpha;</VAR>}}
            // {{math|{{radical|2}}}}
            // {{math|{{radical|1 − ''e''²}}}}
        }
        catch (PatternSyntaxException e)
        {
            // #TODO: DO something!
            return null;
        }

        int fileCounter = 0;
        while (m.find())
        {
            String formula = m.group(1);
            String formulas = "<math>" + formula + "</math>\n";
            result.put(fileCounter, formulas);
            fileCounter++;
        }

        return result;
    }
}
