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
 * Task to remove HTML tags embedded in the text.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class RemoveHTMLTags extends Task {

    static final Logger LOG = Logger.getLogger(RemoveHTMLTags.class.getName());
    String text;
    HashMap<String, String> result;

    private RemoveHTMLTags() {
    }

    public static RemoveHTMLTags newBuild() {
        return new RemoveHTMLTags();
    }

    public RemoveHTMLTags setText(String text) {
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
            // <div align=center>, <br/>
            String regex1 = "(?:\\<|&lt;)(?:.*?)(?:\\>|&gt;)";
            p = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            m1 = p.matcher(text);
            p = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("");

            // </div>
            String regex2 = "(?:\\<|&lt;)\\/\\w+(?:\\>|&gt;)";
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
