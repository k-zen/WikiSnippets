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
 * Task to remove links to images embedded in the text.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class RemoveImages extends Task {

    static final Logger LOG = Logger.getLogger(RemoveImages.class.getName());
    String text;
    HashMap<String, String> result;

    private RemoveImages() {
    }

    public static RemoveImages newBuild() {
        return new RemoveImages();
    }

    public RemoveImages setText(String text) {
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
            // [[Archivo:Andorra-coa-old.jpg|thumb|left|Escudo de Andorra en la [[Casa de la Vall]].]]
            // [[Imagen:Mona Lisa.jpg|thumb|right|220px|La Mona Lisa es uno de los cuadros más reconocidos de Occidente.]]
            // # Problemas:
            // 1. Hay veces que las imagenes no se encuentran solas en una linea y el greedy star remueve toda la linea,
            // removiendo mas informacion que la necesaria.
            // Ej.
            // [[Archivo:Bodega-Mendoza-453065177.jpg|thumb|200px|Bodega vitivinícola en [[Mendoza]]. En [[Cuyo]] se destaca una considerable
            // producción agroindustrial del [[Olea europaea|olivo]], la [[uva]] y sobre todo el [[vino]], siendo el primer productor de vinos
            // de América Latina y el quinto productor del mundo con 16 millones de hectolitros por año.
            //      Correccion: Realizar el removido de las imagenes luego de reemplazar los enlaces y cambiar el greedy
            //                  star por un lazy star. (CORREGIDO CON ESTA OPCION)
            // # TODO: Hay otros tipos de imagenes que no estan siendo procesadas, tipo:
            //         - [[:Imagen:LaTeX_logo.png|logo]] - La cual debemos reemplazar por el valor que viene luego del | (pipe).
            //         - [[:Media:{{{2}}}|{{{1}}}]] - De estas no hay mucho pero de igual manera debemos procesarlas.
            String regex =
                   ""
                    + "\\[\\[                                   "
                    + "  [:]?(?:archivo|image[n]?|media|file):  "
                    + "  .*?                                    "
                    + "\\]\\]                                   ";
            p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m = p.matcher(text);
            p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
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
