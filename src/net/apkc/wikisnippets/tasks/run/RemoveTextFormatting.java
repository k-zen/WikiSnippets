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
 * Task to remove WikiMarkup in the text.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class RemoveTextFormatting extends Task {

    static final Logger LOG = Logger.getLogger(RemoveTextFormatting.class.getName());
    String text;
    HashMap<String, String> result;

    private RemoveTextFormatting() {
    }

    public static RemoveTextFormatting newBuild() {
        return new RemoveTextFormatting();
    }

    public RemoveTextFormatting setText(String text) {
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
            // ''manufactura de origen agropecuario''
            String regex =
                   ""
                    + "[']{2,5}    "
                    + "  (?:\\s)?  "
                    + "    (.*?)   "
                    + "  (?:\\s)?  "
                    + "[']{2,5}    ";
            p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m = p.matcher(text);
            p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            text = p.matcher(text).replaceAll("$1");
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
