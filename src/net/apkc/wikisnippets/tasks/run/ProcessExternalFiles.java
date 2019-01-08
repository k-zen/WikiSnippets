package net.apkc.wikisnippets.tasks.run;

// Apache Commons
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.apkc.wikisnippets.tasks.Task;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Task to process images embedded in the text.
 *
 * @author Andreas P. Koenzen
 * @see Builder Pattern
 */
public class ProcessExternalFiles extends Task
{

    static final Logger LOG = Logger.getLogger(ProcessExternalFiles.class.getName());
    String text;
    HashMap<Integer, String> result;

    private ProcessExternalFiles()
    {
    }

    public static ProcessExternalFiles newBuild()
    {
        return new ProcessExternalFiles();
    }

    public ProcessExternalFiles setText(String text)
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
            // PROCESS IMAGES:
            // [[Archivo:Andorra-coa-old.jpg|thumb|left|Escudo de Andorra en la [[Casa de la Vall]].]]
            // [[Imagen:Mona Lisa.jpg|thumb|right|220px|La Mona Lisa es uno de los cuadros más reconocidos de Occidente.]]
            // [[Image:Wikipedesketch1.png|thumb|left|alt=A cartoon centipede reads books and types on a laptop.|The Wikipede edits ''[[Myriapoda]]''.]]
            // PROBLEMS:
            // 1.
            // CORRECTION:
            // TODO:
            // 1. Hay algunas imagenes que no estan siendo procesadas debido a que se encuentran dentro de templates y
            //    etiquetas HTML, las cuales son removidas sin ser procesadas.
            //    Ej. - {{wide image|Helsinki z00.jpg|1800px|alt=Panorama of city with mixture of five- to ten-story buildings|[[Helsinki]] has many buildings.}}
            //        - {|
            //           | [[Image:Philipp Veit 008.jpg|thumb|upright|alt=Robed woman, seated, with sword on her lap|Philipp Veit, ''Germania'', 1834–36]]
            //           | [[Image:Philipp Veit 009.jpg|thumb|upright|alt=Robed woman, standing, holding a sword|Philipp Veit, ''Germania'', 1848]]
            //           | [[Image:Niederwald memorial 2.JPG|thumb|upright|alt=Monument of robed woman, standing, holding a crown in one hand and a partly sheathed sword in another|Johannes Schilling, ''Germania'', 1871–83]]
            //           |}
            //        - [[Image:Westminstpalace.jpg|right|thumbnail|alt=A large clock tower and other buildings line a great river.|<div align="center">This is <span style="color: green">the </span><br /> [[Palace of Westminster]]<br /> '''in <span style="color: red">London</span>'''</div>]]
            String r
                   = ""
                    + "\\[\\[                                   "
                    + "  [:]?(?:archivo|image[n]?|media|file):  "
                    + "  (.*?)                                  " // Grupo 1. El nombre del archivo de la imagen.
                    + "\\]\\]                                   ";
            p = Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
            m = p.matcher(text);
        }
        catch (PatternSyntaxException e)
        {
            // #TODO: DO something!
            return null;
        }

        int fileCounter = 0;
        while (m.find())
        {
            // El texto dentro de los corchetes completo.
            String match = m.group(1);
            // Separar el texto por pipes (|).
            String[] tokens = match.split("\\|");
            // Procesar nombre de archivo.
            String fileName;
            if (tokens.length > 0 && tokens[0] != null)
            {
                fileName = tokens[0]; // El texto antes del primer pipe.
            }
            else
            {
                fileName = match; // No contiene pipes, entonces contiene solo el nombre del archivo.
            }
            // Procesar formato de archivo.
            String fileFormat = "";
            List<String> availableFormats = new ArrayList<>();
            availableFormats.add("jpg");
            availableFormats.add("png");
            availableFormats.add("gif");
            availableFormats.add("svg");
            Pattern pTmp3;
            Matcher mTmp3;
            String regexTmp3 = "\\A(?:.*)[.](.*)\\z";
            pTmp3 = Pattern.compile(regexTmp3, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            mTmp3 = pTmp3.matcher(fileName);
            if (mTmp3.find())
            {
                fileFormat = mTmp3.group(1).trim().toLowerCase();
            }
            // Continuar solo si es un formato de imagen valida, si es otro tipo de archivo, ya sea: video, musica, u otro --
            // entonces saltear el proceso y no devolver nada.
            if (availableFormats.contains(fileFormat))
            {
                // Procesar etiqueta ALT.
                String altTag = "";
                Pattern pTmp1;
                Matcher mTmp1;
                String regexTmp1 = "(?=alt[=])(.*)"; // Positive Lookahead.
                pTmp1 = Pattern.compile(regexTmp1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                for (int i = 0; i < tokens.length; i++)
                {
                    mTmp1 = pTmp1.matcher(tokens[i]);
                    if (mTmp1.find())
                    {
                        altTag = mTmp1.group(1).trim();
                    }
                }
                // Procesar texto de imagen.
                String caption = "";
                Pattern pTmp2;
                Matcher mTmp2;
                String regexTmp2
                       = ""
                        + "(?!                                 "
                        + "\\A\\s*thumb\\s*\\z|                "
                        + "\\A\\s*thumbnail\\s*\\z|            "
                        + "\\A\\s*frame\\s*\\z|                "
                        + "\\A\\s*framed\\s*\\z|               "
                        + "\\A\\s*frameless\\s*\\z|            "
                        + "\\A\\s*border\\s*\\z|               "
                        + "\\A\\s*right\\s*\\z|                "
                        + "\\A\\s*left\\s*\\z|                 "
                        + "\\A\\s*center\\s*\\z|               "
                        + "\\A\\s*none\\s*\\z|                 "
                        + "\\A\\s*baseline\\s*\\z|             "
                        + "\\A\\s*middle\\s*\\z|               "
                        + "\\A\\s*sub\\s*\\z|                  "
                        + "\\A\\s*super\\s*\\z|                "
                        + "\\A\\s*text-top\\s*\\z|             "
                        + "\\A\\s*text-bottom\\s*\\z|          "
                        + "\\A\\s*top\\s*\\z|                  "
                        + "\\A\\s*bottom\\s*\\z|               "
                        + "\\A\\s*alt[=]|                      "
                        + "\\A\\s*link[=]|                     "
                        + "\\A\\s*\\d{1,4}px\\s*\\z|           "
                        + "\\A\\s*x\\d{1,4}px\\s*\\z|          "
                        + "\\A\\s*\\d{1,4}x\\d{1,4}px\\s*\\z|  "
                        + "\\A\\s*upright\\s*\\z|              "
                        + "\\A\\s*upright=Factor\\s*\\z        "
                        + ")                                   " // Negative Lookahead.
                        + "(.*)                                ";
                pTmp2 = Pattern.compile(regexTmp2, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                for (String token : tokens)
                {
                    mTmp2 = pTmp2.matcher(token);
                    if (mTmp2.find())
                    {
                        caption = mTmp2.group(1).trim();
                        // Procesar el texto en el caption antes de guardarlo en el XML.
                        caption = StringUtils.replace(caption, "\"", "&quot;");
                    }
                }
                // Crear entrada en archivo XML para dicha imagen.
                String images = "<image altTag=\"" + altTag + "\" caption=\"" + caption + "\" fileFormat=\"" + fileFormat + "\">" + fileName + "</image>\n";
                // Asignar los enlaces al HashMap.
                result.put(fileCounter, images);
                // Incrementar el contador.
                fileCounter++;
            }
        }

        return result;
    }
}
