package net.apkc.wikisnippets.tasks.run;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.apkc.wikisnippets.tasks.Task;
import net.apkc.wikisnippets.tasks.TasksHandler;
import net.apkc.wikisnippets.util.Timer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * WikiMarkup remover class.
 *
 * <p>
 * Tasks:
 * <ul>
 * <li>WikiMarkup remover</li>
 * <li>Article division by paragraph</li>
 * </ul>
 *
 * <pre>
 * i.e.
 * Title:  "Tom Cruise"
 * Header: "Tom Cruise/Relationships and personal life/Katie Holmes"
 * Text:   "In april ... Italy on November 18, 2006."
 * </pre>
 *
 * @author Andreas Koenzen
 * @see Builder Pattern
 */
public class WikiMarkupProcessor extends Task
{

    static final Logger LOG = Logger.getLogger(WikiMarkupProcessor.class.getName());
    String origin = "";
    String destination = "";

    private WikiMarkupProcessor()
    {
    }

    public static WikiMarkupProcessor newBuild()
    {
        return new WikiMarkupProcessor();
    }

    public WikiMarkupProcessor setOrigin(String origin)
    {
        this.origin = origin;
        return this;
    }

    public WikiMarkupProcessor setDestination(String destination)
    {
        this.destination = destination;
        return this;
    }

    public String getOrigin()
    {
        return origin;
    }

    public String getDestination()
    {
        return destination;
    }

    @Override
    public void reportProgress(int progress)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object doInBackground() throws Exception
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            SAXParser parser = factory.newSAXParser();
            parser.parse(new File(origin), new XMLHandler());
        }
        catch (ParserConfigurationException ex)
        {
            // #TODO: DO something!
        }

        return null;
    }

    class XMLHandler extends DefaultHandler
    {

        String currentStartTag = "";
        String currentEndTag = "";
        StringBuilder title = new StringBuilder();
        StringBuilder text = new StringBuilder();
        StringBuilder accumulator = new StringBuilder();
        Timer t = new Timer();

        @Override
        public void startDocument()
        {
            t.starTimer();

            OutputStream f = null;
            try
            {
                f = new FileOutputStream(new File(destination), true);
                OutputStream b = new BufferedOutputStream(f);
                try (OutputStreamWriter out = new OutputStreamWriter(b, "UTF8"))
                {
                    out.write(
                            ""
                            + "<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                            + "xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"es\">\n");
                    out.flush();
                }
            }
            catch (UnsupportedEncodingException uex)
            {
                // #TODO: DO something!
            }
            catch (FileNotFoundException ex)
            {
                // #TODO: DO something!
            }
            catch (IOException ioe)
            {
                // #TODO: DO something!
            }
            finally
            {
                try
                {
                    if (f != null)
                    {
                        f.close();
                    }
                }
                catch (IOException ex)
                {
                    // #TODO: DO something!
                }
            }
        }

        @Override
        public void endDocument()
        {
            OutputStream f = null;
            try
            {
                f = new FileOutputStream(new File(destination), true);
                OutputStream b = new BufferedOutputStream(f);
                try (OutputStreamWriter out = new OutputStreamWriter(b, "UTF8"))
                {
                    out.write("</mediawiki>");
                    out.flush();
                }
            }
            catch (UnsupportedEncodingException uex)
            {
                // #TODO: DO something!
            }
            catch (FileNotFoundException ex)
            {
                // #TODO: DO something!
            }
            catch (IOException ioe)
            {
                // #TODO: DO something!
            }
            finally
            {
                try
                {
                    if (f != null)
                    {
                        f.close();
                    }
                }
                catch (IOException ex)
                {
                    // #TODO: DO something!
                }
            }

            t.endTimer();
        }

        @Override
        public void startElement(String uri, String name, String qName, Attributes atts)
        {
            accumulator.setLength(0); // Reset acummulator every time we find a new tag.
            currentStartTag = qName; // Save tag's name.
        }

        @Override
        public void characters(char ch[], int start, int length)
        {
            accumulator.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String name, String qName)
        {
            String cleanTitle;
            String cleanText;
            ProcessExternalFiles pef = ProcessExternalFiles.newBuild();
            RemoveImages ri = RemoveImages.newBuild();

            currentEndTag = qName; // Save closing's tag.
            switch (qName)
            {
                case "title":
                    title.append(accumulator.toString().trim());
                    break;
                case "text":
                    text.append(accumulator.toString().trim());
                    break;
            }

            if (qName.equals("text"))
            {
                cleanTitle = title.toString();
                cleanText = text.toString();

                HashMap<Integer, String> imagesTmp = null;

                // ARTICLE FILTER:
                // FIlter articles that we musn't add to final XML.
                // Exclude articles of internal use like:
                // 1. Articles that start with Wikipedia:, Mediawiki:, Ayuda:, etc.
                // 2. Internal redirecctions among articles. i.e. #REDIRECT, #REDIRECCIóN, etc.
                String r1
                       = ""
                        + "(?:\\s)?       \n"
                        + "  (?:.*?)      \n"
                        + "    \\:        \n"
                        + "  (?:.*?)      \n"
                        + "(?:\\s)?       \n";
                Pattern p1 = Pattern.compile(r1, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                Matcher m1 = p1.matcher(cleanTitle);

                String r2 = "#redirec(?:.*?)[nsrotd]";
                Pattern p2 = Pattern.compile(r2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                Matcher m2 = p2.matcher(cleanText);

                if (!m1.find())
                {
                    if (!m2.find())
                    {
                        // TEXT PROCESSING:
                        // Obs.: Primero procesar todo lo que debamos remover y luego lo que debemos reemplazar.
                        // Pasos:
                        // 1. Remover las lineas nuevas.
                        // 2. Remover las referencias. Del tipo: <ref> ... </ref>
                        // 3. Procesar las formulas matematicas.
                        // 4. Remover las formulas matematicas.
                        // 5. Remover los templates.
                        // 6. Remover las tablas.
                        // 7. Remover etiquetas HTML. Del tipo: <div align="center"> ... </div>
                        // 8. Reemplazar/remover los enlaces por su "anchor text".
                        // 9. Remover el formateado a los caracteres. (Negritas, itálicas, etc.)
                        // 10. Reemplazar caracteres especiales.
                        // 11. Remover espacios dobles.

                        RemoveNewLines _1 = RemoveNewLines.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_1.setText(cleanText)).get()) ? _1.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        RemoveReferences _2 = RemoveReferences.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_2.setText(cleanText)).get()) ? _2.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        RemoveMathFormulas _3 = RemoveMathFormulas.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_3.setText(cleanText)).get()) ? _3.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        RemoveTemplates _4 = RemoveTemplates.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_4.setText(cleanText)).get()) ? _4.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        RemoveTables _5 = RemoveTables.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_5.setText(cleanText)).get()) ? _5.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        RemoveHTMLTags _6 = RemoveHTMLTags.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_6.setText(cleanText)).get()) ? _6.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        ReplaceLinks _7 = ReplaceLinks.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_7.setText(cleanText)).get()) ? _7.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        RemoveTextFormatting _8 = RemoveTextFormatting.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_8.setText(cleanText)).get()) ? _8.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        ReplaceCharacters _9 = ReplaceCharacters.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_9.setText(cleanText)).get()) ? _9.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        RemoveDoubleSpaces _10 = RemoveDoubleSpaces.newBuild();
                        try
                        {
                            cleanText = (TasksHandler
                                         .getInstance()
                                         .submitFiniteTask(_10.setText(cleanText)).get()) ? _10.getResult().get("processedText") : cleanText;
                        }
                        catch (InterruptedException | ExecutionException e)
                        {
                            // #TODO: DO something!
                        }

                        // Reemplazar caracteres de los titulos.
                        cleanTitle = StringUtils.replace(cleanTitle, "&", "&amp;");

                        // --- División de artículos por párrafo. --- //
                        // # Problemas:
                        // 1. En algunos casos del proceso de separacion de parrafos, los parrafos estarian siendo colocados en la cabecera.
                        //      Correcion: Esto se debe a que en algunos casos los sub-titulos se encuentran al comienzo del texto, como en el caso del
                        //                 articulo "Agente de viajes", en especifico... <text>== Sobre Dalton2 y Lampsako == del dump del 12-08-09. Para
                        //                 solucionar el problema debemos hacer opcional el espacio antes del comienzo del sub-titulo. (NO FUNCIONA - Causa
                        //                 mas errores.)
                        //      Correccion: Probar colocando una sentencia OR para los casos en que ocurre esto. Colocar esta sentencia con "\\s" y
                        //                  "^" y "\\A" que viene pegado al sub-titulo. Si no es un espacio entonces es un comienzo de input o linea. (AL PARECER FUNCIONA)
                        // 2. Hay veces en que en el titulo se encuentra un signo de igualdad (=) y esto causa errores en el quiebre de parrafos. Como en el caso del
                        //    articulo "Rentabilidad financiera".
                        //      Correccion: La solucion es reemplazar este caracter por su nombre de entidad (&#61;). (AL PARECER FUNCIONA)
                        // 3. Existen otros errores no detectados que ocasionan que los articulos no sean separados exitosamente. Para ver mas ejemplos
                        //    sobre ellos, ver articulos: - Minotauro
                        //                                - Manzanares (Ciudad Real)
                        //                                - Albert Einstein
                        //      Correccion: Agregado a la lista de palabras que no deben ser tomados en cuenta, estan: referencias, enlaces externos. (AL PARECER FUNCIONA)
                        // 4. Siguen habiendo articulos que no estan siendo quebrados correctamente. Ej. - Pronombres en español
                        //                                                                               - Falangismo en Hispanoamérica
                        //                                                                               - Becuadro
                        // Variables temporales.
                        String categoryTextTmp;
                        StringBuilder brokenHeaderTmp = new StringBuilder();
                        StringBuilder brokenText = new StringBuilder();
                        StringBuffer brokenTextTmp = new StringBuffer();
                        ////////////////////////////////////////////
                        // Fase #1: Texto que no posee categoria. //
                        ////////////////////////////////////////////
                        String regex3 = "(.*?)(?:[=]{2,6}?)";
                        Pattern p3 = Pattern.compile(regex3, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                        Matcher m3 = p3.matcher(cleanText);

                        // Si hay un match.
                        if (m3.find())
                        {
                            // Guardar el texto que corresponde a esta categoria en una variable temporal, en lugar de usar "m.group(n)".
                            categoryTextTmp = m3.group(1);
                            // Buscar y procesar imagenes en el texto encontrado.
                            try
                            {
                                imagesTmp = (TasksHandler.getInstance().submitFiniteTask(pef.setText(categoryTextTmp)).get()) ? pef.getResult() : null;
                            }
                            catch (InterruptedException | ExecutionException e)
                            {
                                // #TODO: DO something!
                            }
                            // Ultimo procesado antes de escribir el XML.
                            try
                            {
                                categoryTextTmp = (TasksHandler.getInstance().submitFiniteTask(ri.setText(categoryTextTmp.trim())).get()) ? ri.getResult().get("processedText") : null;
                            }
                            catch (InterruptedException | ExecutionException e)
                            {
                                // #TODO: DO something!
                            }
                            // Asignar el texto nuevo al buffer.
                            brokenTextTmp.append("  <page>\n");
                            brokenTextTmp.append("    <title>").append(cleanTitle).append("</title>\n");
                            brokenTextTmp.append("    <header>").append(cleanTitle).append("</header>\n");
                            brokenTextTmp.append("    <text>").append(categoryTextTmp.trim()).append("</text>\n");

                            // Asignar las imagenes, si hay.
                            if (!imagesTmp.isEmpty())
                            {
                                brokenTextTmp.append("    <images>\n");
                                for (String value : imagesTmp.values())
                                {
                                    brokenTextTmp.append("      ").append(value);
                                }
                                brokenTextTmp.append("    </images>\n");
                            }
                            // Asignar las formulas, si hay.
                            // if ( !mathFormulaeTmp.isEmpty () ) {
                            //   brokenTextTmp.append ( "    <mathFormulae>\n" );
                            //   for ( String value : mathFormulaeTmp.values () ) {
                            //     brokenTextTmp.append ( "      " + value );
                            //   }
                            //   brokenTextTmp.append ( "    </mathFormulae>\n" );
                            // }
                            brokenTextTmp.append("  </page>\n");
                            // Asignar.
                            brokenText.append(brokenTextTmp);
                            // Reiniciar el buffer.
                            brokenTextTmp.setLength(0);
                        }

                        // Reiniciar el HashMap de imagenes.
                        imagesTmp.clear();
                        // Reiniciar el texto que corresponde a esta categoria.
                        categoryTextTmp = null;

                        /////////////////////////////////////////////////////////////////////
                        // Fase #2: Texto que pertenece al nivel 1. Ej. == Introducción == //
                        /////////////////////////////////////////////////////////////////////
                        // Reiniciar los buffers temporales.
                        brokenTextTmp.setLength(0);
                        brokenHeaderTmp.setLength(0);
                        // 2.1) Buscar los nombres de los sub-titulos.
                        String regex4
                               = ""
                                + "(?:\\s|^|\\A)  "
                                + "(?:[=]{2})     "
                                + "  (?:\\s)      "
                                + "  ([^=]*?)     " // Grupo 1 (Sub-titulo). Nivel 1.
                                + "  (?:\\s)      "
                                + "(?:[=]{2})     "
                                + "(?:\\s)        "
                                + "  (?![=])      " // Negative Lookahead.
                                + "  (?:[^=]*?)   ";
                        Pattern p4 = Pattern.compile(regex4, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                        Matcher m4 = p4.matcher(cleanText);
                        // Asignar el texto nuevo al buffer.
                        while (m4.find())
                        {
                            // Construir el header.
                            brokenHeaderTmp.append(cleanTitle);
                            brokenHeaderTmp.append("/");
                            brokenHeaderTmp.append(m4.group(1));
                            // 2.2) Buscar el texto de cada sub-titulo.
                            // Sub-titulo del parrafo a encontrar.
                            // Realizar tareas:
                            // 1. Reemplazar los espacios por "\\s"
                            // 2. Escapar los caracteres.
                            String subTitle = m4.group(1);
                            String regex5
                                   = ""
                                    + "(?:\\s|^|\\A)                                                                      "
                                    + "(?:[=]{2})                                                                         "
                                    + "  (?:\\s)                                                                          "
                                    + "    (?!(?:bibliograf.a|v.ase\\s{1,3}tambi.n|enlaces\\s{1,3}externos|referencias))  " // Negative Lookahead
                                    + "    (?:" + Pattern.quote(subTitle) + ")                                            "
                                    + "  (?:\\s)                                                                          "
                                    + "(?:[=]{2})                                                                         "
                                    + "(?:\\s)                                                                            "
                                    + "  (.*?)                                                                            "
                                    + "  (?:[=]{2,6})                                                                     ";
                            Pattern p5 = Pattern.compile(regex5, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                            Matcher m5 = p5.matcher(cleanText);
                            // --- Debug.
                            // System.out.println ( "> Patron5: " + p5.pattern () );
                            // System.out.println ( "> Texto:   " + textTmp );
                            if (m5.find())
                            {
                                // Guardar el texto que corresponde a esta categoria en una variable temporal, en lugar de usar "m.group(n)".
                                categoryTextTmp = m5.group(1);
                                // Buscar y procesar imagenes en el texto encontrado.
                                try
                                {
                                    imagesTmp = (TasksHandler.getInstance().submitFiniteTask(pef.setText(categoryTextTmp)).get()) ? pef.getResult() : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Ultimo procesado antes de escribir el XML.
                                try
                                {
                                    categoryTextTmp = (TasksHandler.getInstance().submitFiniteTask(ri.setText(categoryTextTmp.trim())).get()) ? ri.getResult().get("processedText") : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Asignar el texto nuevo al buffer.
                                brokenTextTmp.append("  <page>\n");
                                brokenTextTmp.append("    <title>").append(cleanTitle).append("</title>\n");
                                brokenTextTmp.append("    <header>").append(brokenHeaderTmp.toString().trim()).append("</header>\n");
                                brokenTextTmp.append("    <text>").append(categoryTextTmp.trim()).append("</text>\n");
                                // Asignar las imagenes, si hay.
                                if (!imagesTmp.isEmpty())
                                {
                                    brokenTextTmp.append("    <images>\n");
                                    for (String value : imagesTmp.values())
                                    {
                                        brokenTextTmp.append("      ").append(value);
                                    }
                                    brokenTextTmp.append("    </images>\n");
                                }
                                brokenTextTmp.append("  </page>\n");
                                // Asignar.
                                brokenText.append(brokenTextTmp);
                                // Reiniciar los buffers.
                                brokenTextTmp.setLength(0);
                                brokenHeaderTmp.setLength(0);
                            }
                        }

                        // Reiniciar el HashMap de imagenes.
                        imagesTmp.clear();
                        // Reiniciar el texto que corresponde a esta categoria.
                        categoryTextTmp = null;

                        ///////////////////////////////////////////////////////////////////////
                        // Fase #3: Texto que pertenece al nivel 2. Ej. === Introducción === //
                        ///////////////////////////////////////////////////////////////////////
                        // Reiniciar los buffers temporales.
                        brokenTextTmp.setLength(0);
                        brokenHeaderTmp.setLength(0);
                        // 3.1) Buscar los nombres de los sub-titulos.
                        String regex6
                               = ""
                                + "(?:\\s|^|\\A)  "
                                + "(?:[=]{3})     "
                                + "  (?:\\s)      "
                                + "  ([^=]*?)     " // Grupo 1 (Sub-titulo). Nivel 2.
                                + "  (?:\\s)      "
                                + "(?:[=]{3})     "
                                + "(?:\\s)        "
                                + "  (?![=])      " // Negative Lookahead.
                                + "  (?:[^=]*?)   ";
                        Pattern p6 = Pattern.compile(regex6, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                        Matcher m6 = p6.matcher(cleanText);
                        // Asignar el texto nuevo al buffer.
                        while (m6.find())
                        {
                            // Construir el header.
                            brokenHeaderTmp.append(cleanTitle);
                            brokenHeaderTmp.append("/");
                            brokenHeaderTmp.append(m6.group(1));
                            // 3.2) Buscar el texto de cada sub-titulo.
                            // Sub-titulo del parrafo a encontrar.
                            // Realizar tareas:
                            // 1. Reemplazar los espacios por "\\s"
                            // 2. Escapar los caracteres.
                            String subTitle = m6.group(1);
                            String regex7
                                   = ""
                                    + "(?:\\s|^|\\A)                                                                      "
                                    + "(?:[=]{3})                                                                         "
                                    + "  (?:\\s)                                                                          "
                                    + "    (?!(?:bibliograf.a|v.ase\\s{1,3}tambi.n|enlaces\\s{1,3}externos|referencias))  " // Negative Lookahead
                                    + "    (?:" + Pattern.quote(subTitle) + ")                                            "
                                    + "  (?:\\s)                                                                          "
                                    + "(?:[=]{3})                                                                         "
                                    + "(?:\\s)                                                                            "
                                    + "  (.*?)                                                                            "
                                    + "  (?:[=]{2,6})                                                                     ";
                            Pattern p7 = Pattern.compile(regex7, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                            Matcher m7 = p7.matcher(cleanText);
                            // --- Debug.
                            // System.out.println ( "> Patron7: " + p7.pattern () );
                            // System.out.println ( "> Texto:   " + textTmp );
                            if (m7.find())
                            {
                                // Guardar el texto que corresponde a esta categoria en una variable temporal, en lugar de usar "m.group(n)".
                                categoryTextTmp = m7.group(1);
                                // Buscar y procesar imagenes en el texto encontrado.
                                try
                                {
                                    imagesTmp = (TasksHandler.getInstance().submitFiniteTask(pef.setText(categoryTextTmp)).get()) ? pef.getResult() : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Ultimo procesado antes de escribir el XML.
                                try
                                {
                                    categoryTextTmp = (TasksHandler.getInstance().submitFiniteTask(ri.setText(categoryTextTmp.trim())).get()) ? ri.getResult().get("processedText") : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Asignar el texto nuevo al buffer.
                                brokenTextTmp.append("  <page>\n");
                                brokenTextTmp.append("    <title>").append(cleanTitle).append("</title>\n");
                                brokenTextTmp.append("    <header>").append(brokenHeaderTmp.toString().trim()).append("</header>\n");
                                brokenTextTmp.append("    <text>").append(categoryTextTmp.trim()).append("</text>\n");
                                // Asignar las imagenes, si hay.
                                if (!imagesTmp.isEmpty())
                                {
                                    brokenTextTmp.append("    <images>\n");
                                    for (String value : imagesTmp.values())
                                    {
                                        brokenTextTmp.append("      ").append(value);
                                    }
                                    brokenTextTmp.append("    </images>\n");
                                }
                                brokenTextTmp.append("  </page>\n");
                                // Asignar.
                                brokenText.append(brokenTextTmp);
                                // Reiniciar los buffers.
                                brokenTextTmp.setLength(0);
                                brokenHeaderTmp.setLength(0);
                            }
                        }

                        // Reiniciar el HashMap de imagenes.
                        imagesTmp.clear();
                        // Reiniciar el texto que corresponde a esta categoria.
                        categoryTextTmp = null;

                        /////////////////////////////////////////////////////////////////////////
                        // Fase #4: Texto que pertenece al nivel 3. Ej. ==== Introducción ==== //
                        /////////////////////////////////////////////////////////////////////////
                        // Reiniciar los buffers temporales.
                        brokenTextTmp.setLength(0);
                        brokenHeaderTmp.setLength(0);
                        // 4.1) Buscar los nombres de los sub-titulos.
                        String regex8
                               = ""
                                + "(?:\\s|^|\\A)  "
                                + "(?:[=]{4})     "
                                + "  (?:\\s)      "
                                + "  ([^=]*?)     " // Grupo 1 (Sub-titulo). Nivel 3.
                                + "  (?:\\s)      "
                                + "(?:[=]{4})     "
                                + "(?:\\s)        "
                                + "  (?![=])      " // Negative Lookahead.
                                + "  (?:[^=]*?)   ";
                        Pattern p8 = Pattern.compile(regex8, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                        Matcher m8 = p8.matcher(cleanText);
                        // Asignar el texto nuevo al buffer.
                        while (m8.find())
                        {
                            // Construir el header.
                            brokenHeaderTmp.append(cleanTitle);
                            brokenHeaderTmp.append("/");
                            brokenHeaderTmp.append(m8.group(1));
                            // 4.2) Buscar el texto de cada sub-titulo.
                            // Sub-titulo del parrafo a encontrar.
                            // Realizar tareas:
                            // 1. Reemplazar los espacios por "\\s"
                            // 2. Escapar los caracteres.
                            String subTitle = m8.group(1);
                            String regex9
                                   = ""
                                    + "(?:\\s|^|\\A)                                                                      "
                                    + "(?:[=]{4})                                                                         "
                                    + "  (?:\\s)                                                                          "
                                    + "    (?!(?:bibliograf.a|v.ase\\s{1,3}tambi.n|enlaces\\s{1,3}externos|referencias))  " // Negative Lookahead
                                    + "    (?:" + Pattern.quote(subTitle) + ")                                            "
                                    + "  (?:\\s)                                                                          "
                                    + "(?:[=]{4})                                                                         "
                                    + "(?:\\s)                                                                            "
                                    + "  (.*?)                                                                            "
                                    + "  (?:[=]{2,6})                                                                     ";
                            Pattern p9 = Pattern.compile(regex9, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                            Matcher m9 = p9.matcher(cleanText);
                            // --- Debug.
                            // System.out.println ( "> Patron9: " + p9.pattern () );
                            // System.out.println ( "> Texto:   " + textTmp );
                            if (m9.find())
                            {
                                // Guardar el texto que corresponde a esta categoria en una variable temporal, en lugar de usar "m.group(n)".
                                categoryTextTmp = m9.group(1);
                                // Buscar y procesar imagenes en el texto encontrado.
                                try
                                {
                                    imagesTmp = (TasksHandler.getInstance().submitFiniteTask(pef.setText(categoryTextTmp)).get()) ? pef.getResult() : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Ultimo procesado antes de escribir el XML.
                                try
                                {
                                    categoryTextTmp = (TasksHandler.getInstance().submitFiniteTask(ri.setText(categoryTextTmp.trim())).get()) ? ri.getResult().get("processedText") : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Asignar el texto nuevo al buffer.
                                brokenTextTmp.append("  <page>\n");
                                brokenTextTmp.append("    <title>").append(cleanTitle).append("</title>\n");
                                brokenTextTmp.append("    <header>").append(brokenHeaderTmp.toString().trim()).append("</header>\n");
                                brokenTextTmp.append("    <text>").append(categoryTextTmp.trim()).append("</text>\n");
                                // Asignar las imagenes, si hay.
                                if (!imagesTmp.isEmpty())
                                {
                                    brokenTextTmp.append("    <images>\n");
                                    for (String value : imagesTmp.values())
                                    {
                                        brokenTextTmp.append("      ").append(value);
                                    }
                                    brokenTextTmp.append("    </images>\n");
                                }
                                brokenTextTmp.append("  </page>\n");
                                // Asignar.
                                brokenText.append(brokenTextTmp);
                                // Reiniciar los buffers.
                                brokenTextTmp.setLength(0);
                                brokenHeaderTmp.setLength(0);
                            }
                        }

                        // Reiniciar el HashMap de imagenes.
                        imagesTmp.clear();
                        // Reiniciar el texto que corresponde a esta categoria.
                        categoryTextTmp = null;

                        ///////////////////////////////////////////////////////////////////////////
                        // Fase #5: Texto que pertenece al nivel 4. Ej. ===== Introducción ===== //
                        ///////////////////////////////////////////////////////////////////////////
                        // Reiniciar los buffers temporales.
                        brokenTextTmp.setLength(0);
                        brokenHeaderTmp.setLength(0);
                        // 5.1) Buscar los nombres de los sub-titulos.
                        String regex10
                               = ""
                                + "(?:\\s|^|\\A)  "
                                + "(?:[=]{5})     "
                                + "  (?:\\s)      "
                                + "  ([^=]*?)     " // Grupo 1 (Sub-titulo). Nivel 4.
                                + "  (?:\\s)      "
                                + "(?:[=]{5})     "
                                + "(?:\\s)        "
                                + "  (?![=])      " // Negative Lookahead.
                                + "  (?:[^=]*?)   ";
                        Pattern p10 = Pattern.compile(regex10, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                        Matcher m10 = p10.matcher(cleanText);
                        // Asignar el texto nuevo al buffer.
                        while (m10.find())
                        {
                            // Construir el header.
                            brokenHeaderTmp.append(cleanTitle);
                            brokenHeaderTmp.append("/");
                            brokenHeaderTmp.append(m10.group(1));
                            // 5.2) Buscar el texto de cada sub-titulo.
                            // Sub-titulo del parrafo a encontrar.
                            // Realizar tareas:
                            // 1. Reemplazar los espacios por "\\s"
                            // 2. Escapar los caracteres.
                            String subTitle = m10.group(1);
                            String regex11
                                   = ""
                                    + "(?:\\s|^|\\A)                                                                      "
                                    + "(?:[=]{5})                                                                         "
                                    + "  (?:\\s)                                                                          "
                                    + "    (?!(?:bibliograf.a|v.ase\\s{1,3}tambi.n|enlaces\\s{1,3}externos|referencias))  " // Negative Lookahead
                                    + "    (?:" + Pattern.quote(subTitle) + ")                                            "
                                    + "  (?:\\s)                                                                          "
                                    + "(?:[=]{5})                                                                         "
                                    + "(?:\\s)                                                                            "
                                    + "  (.*?)                                                                            "
                                    + "  (?:[=]{2,6})                                                                     ";
                            Pattern p11 = Pattern.compile(regex11, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                            Matcher m11 = p11.matcher(cleanText);
                            // --- Debug.
                            // System.out.println ( "> Patron11: " + p11.pattern () );
                            // System.out.println ( "> Texto:    " + textTmp );
                            if (m11.find())
                            {
                                // Guardar el texto que corresponde a esta categoria en una variable temporal, en lugar de usar "m.group(n)".
                                categoryTextTmp = m11.group(1);
                                // Buscar y procesar imagenes en el texto encontrado.
                                try
                                {
                                    imagesTmp = (TasksHandler.getInstance().submitFiniteTask(pef.setText(categoryTextTmp)).get()) ? pef.getResult() : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Ultimo procesado antes de escribir el XML.
                                try
                                {
                                    categoryTextTmp = (TasksHandler.getInstance().submitFiniteTask(ri.setText(categoryTextTmp.trim())).get()) ? ri.getResult().get("processedText") : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Asignar el texto nuevo al buffer.
                                brokenTextTmp.append("  <page>\n");
                                brokenTextTmp.append("    <title>").append(cleanTitle).append("</title>\n");
                                brokenTextTmp.append("    <header>").append(brokenHeaderTmp.toString().trim()).append("</header>\n");
                                brokenTextTmp.append("    <text>").append(categoryTextTmp.trim()).append("</text>\n");
                                // Asignar las imagenes, si hay.
                                if (!imagesTmp.isEmpty())
                                {
                                    brokenTextTmp.append("    <images>\n");
                                    for (String value : imagesTmp.values())
                                    {
                                        brokenTextTmp.append("      ").append(value);
                                    }
                                    brokenTextTmp.append("    </images>\n");
                                }
                                brokenTextTmp.append("  </page>\n");
                                // Asignar.
                                brokenText.append(brokenTextTmp);
                                // Reiniciar los buffers.
                                brokenTextTmp.setLength(0);
                                brokenHeaderTmp.setLength(0);
                            }
                        }

                        // Reiniciar el HashMap de imagenes.
                        imagesTmp.clear();
                        // Reiniciar el texto que corresponde a esta categoria.
                        categoryTextTmp = null;

                        /////////////////////////////////////////////////////////////////////////////
                        // Fase #6: Texto que pertenece al nivel 5. Ej. ====== Introducción ====== //
                        /////////////////////////////////////////////////////////////////////////////
                        // Reiniciar los buffers temporales.
                        brokenTextTmp.setLength(0);
                        brokenHeaderTmp.setLength(0);
                        // 6.1) Buscar los nombres de los sub-titulos.
                        String regex12
                               = ""
                                + "(?:\\s|^|\\A)  "
                                + "(?:[=]{6})     "
                                + "  (?:\\s)      "
                                + "  ([^=]*?)     " // Grupo 1 (Sub-titulo). Nivel 5.
                                + "  (?:\\s)      "
                                + "(?:[=]{6})     "
                                + "(?:\\s)        "
                                + "  (?![=])      " // Negative Lookahead.
                                + "  (?:[^=]*?)   ";
                        Pattern p12 = Pattern.compile(regex12, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                        Matcher m12 = p12.matcher(cleanText);
                        // Asignar el texto nuevo al buffer.
                        while (m12.find())
                        {
                            // Construir el header.
                            brokenHeaderTmp.append(cleanTitle);
                            brokenHeaderTmp.append("/");
                            brokenHeaderTmp.append(m12.group(1));
                            // 6.2) Buscar el texto de cada sub-titulo.
                            // Sub-titulo del parrafo a encontrar.
                            // Realizar tareas:
                            // 1. Reemplazar los espacios por "\\s"
                            // 2. Escapar los caracteres.
                            String subTitle = m12.group(1);
                            String regex13
                                   = ""
                                    + "(?:\\s|^|\\A)                                                                      "
                                    + "(?:[=]{6})                                                                         "
                                    + "  (?:\\s)                                                                          "
                                    + "    (?!(?:bibliograf.a|v.ase\\s{1,3}tambi.n|enlaces\\s{1,3}externos|referencias))  " // Negative Lookahead
                                    + "    (?:" + Pattern.quote(subTitle) + ")                                            "
                                    + "  (?:\\s)                                                                          "
                                    + "(?:[=]{6})                                                                         "
                                    + "(?:\\s)                                                                            "
                                    + "  (.*?)                                                                            "
                                    + "  (?:[=]{2,6})                                                                     ";
                            Pattern p13 = Pattern.compile(regex13, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);
                            Matcher m13 = p13.matcher(cleanText);
                            // --- Debug.
                            // System.out.println ( "> Patron13: " + p13.pattern () );
                            // System.out.println ( "> Texto:    " + textTmp );
                            if (m13.find())
                            {
                                // Guardar el texto que corresponde a esta categoria en una variable temporal, en lugar de usar "m.group(n)".
                                categoryTextTmp = m13.group(1);
                                // Buscar y procesar imagenes en el texto encontrado.
                                try
                                {
                                    imagesTmp = (TasksHandler.getInstance().submitFiniteTask(pef.setText(categoryTextTmp)).get()) ? pef.getResult() : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Ultimo procesado antes de escribir el XML.
                                try
                                {
                                    categoryTextTmp = (TasksHandler.getInstance().submitFiniteTask(ri.setText(categoryTextTmp.trim())).get()) ? ri.getResult().get("processedText") : null;
                                }
                                catch (InterruptedException | ExecutionException e)
                                {
                                    // #TODO: DO something!
                                }
                                // Asignar el texto nuevo al buffer.
                                brokenTextTmp.append("  <page>\n");
                                brokenTextTmp.append("    <title>").append(cleanTitle).append("</title>\n");
                                brokenTextTmp.append("    <header>").append(brokenHeaderTmp.toString().trim()).append("</header>\n");
                                brokenTextTmp.append("    <text>").append(categoryTextTmp.trim()).append("</text>\n");
                                // Asignar las imagenes, si hay.
                                if (!imagesTmp.isEmpty())
                                {
                                    brokenTextTmp.append("    <images>\n");
                                    for (String value : imagesTmp.values())
                                    {
                                        brokenTextTmp.append("      ").append(value);
                                    }
                                    brokenTextTmp.append("    </images>\n");
                                }
                                brokenTextTmp.append("  </page>\n");
                                // Asignar.
                                brokenText.append(brokenTextTmp);
                                // Reiniciar los buffers.
                                brokenTextTmp.setLength(0);
                                brokenHeaderTmp.setLength(0);
                            }
                        }

                        // Reiniciar el HashMap de imagenes.
                        imagesTmp.clear();
                        // Reiniciar el texto que corresponde a esta categoria.
                        categoryTextTmp = null;

                        // --- Escribir el cuerpo del archivo XML resultante. --- //
                        OutputStream f = null;
                        try
                        {
                            f = new FileOutputStream(new File(destination), true);
                            OutputStream b = new BufferedOutputStream(f);
                            OutputStreamWriter out = new OutputStreamWriter(b, "UTF8");
                            out.write(brokenText.toString());
                            out.flush();
                            out.close();
                        }
                        catch (UnsupportedEncodingException uex)
                        {
                            // #TODO: DO something!
                        }
                        catch (FileNotFoundException ex)
                        {
                            // #TODO: DO something!
                        }
                        catch (IOException ioe)
                        {
                            // #TODO: DO something!
                        }
                        finally
                        {
                            try
                            {
                                if (f != null)
                                {
                                    f.close();
                                }
                            }
                            catch (IOException ex)
                            {
                                // #TODO: DO something!
                            }
                        }
                    }
                }
            }
        }
    }
}
