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
 * This task removes references from the text. It should be convenient to extract the
 * references before deleting them.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class RemoveReferences extends Task {

    static final Logger LOG = Logger.getLogger(RemoveReferences.class.getName());
    String text;
    HashMap<String, String> result;

    private RemoveReferences() {
    }

    public static RemoveReferences newBuild() {
        return new RemoveReferences();
    }

    public RemoveReferences setText(String text) {
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

        try {
            // <ref>...</ref>
            // PROBLEMS:
            // 1. Sometimes tags open and close in one tag. i.e. - &lt;ref name=&quot;Capital&quot; /&gt;
            //      Correction: PLace a character class on the first tag to include everything except a bar (/). (WORKS)
            String r1 = "(?:\\<|&lt;)ref(?:[^\\/]*?)(?:\\>|&gt;)(?:.*?)(?:\\<|&lt;)\\/ref(?:\\>|&gt;)";
            p = Pattern.compile(r1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            m1 = p.matcher(text);
            p = Pattern.compile(r1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("");

            // <ref />
            String regex2 = "(?:\\<|&lt;)ref(?:.*?)(?:\\>|&gt;)";
            p = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            m2 = p.matcher(text);
            p = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
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
