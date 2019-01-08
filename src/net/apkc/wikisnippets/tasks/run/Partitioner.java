package net.apkc.wikisnippets.tasks.run;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.apkc.utilities.Utilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Esta clase realiza el truncado de un dump de Wikipedia en otros dumps más pequeños, que
 * son más faciles de parsear por la clase WikiDumpClean.class
 * Ejemplo:
 * - Archivo: eswiki-20090519-pages-articles.xml
 * a...
 * - Archivo: eswiki-20090519-pages-articles-part1.xml
 * eswiki-20090519-pages-articles-part2.xml
 * eswiki-20090519-pages-articles-part3.xml
 * .
 * .
 * .
 * eswiki-20090519-pages-articles-partn.xml
 *
 * @author Andreas P. Koenzen
 */
public class Partitioner extends DefaultHandler
{

    String origin = ""; // Origen del archivo XML.
    String destination = ""; // Destino del archivo XML.
    long startTime = 0; // Tiempo de inicio.
    long endTime = 0; // Tiempo del final.
    double totalTime = 0.0; // Tiempo transcurrido.
    static boolean continueParsing = false; // Controla que el parseado se realize solo 2 veces.
    static boolean isFirstIteration = false; // Si es la primera iteracion.
    static boolean isSecondIteration = false; // Si es la segunda iteracion.
    static long articleAmount = 4000; // Cantidad de articulos por dump.
    static long articleCounter = 0; // Contador de articulos.
    static long faseCounter = 0; // Contador de fases.
    static long totalArticles = 0; // Cantidad total de articulos.
    StringBuffer accumulator = new StringBuffer(); // Acumulador de caracteres parseados.

    public void init() throws ClassNotFoundException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            Utilities.logToConsole("<<<<<<<<< WikiSnippets:Truncate >>>>>>>>>");
            Utilities.logToConsole("#> Origen:");
            // Listar los archivos. Carpeta por defecto -> /tmp
            File f = new File("/tmp");
            String[] fs = f.list();
            // Iterar los archivos y mostrar solo los XML.
            short counter = 1;
            String file1 = "";
            String file2 = "";
            String file3 = "";
            String file4 = "";
            for (String value : fs)
            {
                if (value.toLowerCase().endsWith("xml"))
                {
                    switch (counter)
                    {
                        case 1:
                            file1 = "/tmp/" + value;
                            Utilities.logToConsole("   " + counter + " : " + file1);
                            break;
                        case 2:
                            file2 = "/tmp/" + value;
                            Utilities.logToConsole("   " + counter + " : " + file2);
                            break;
                        case 3:
                            file3 = "/tmp/" + value;
                            Utilities.logToConsole("   " + counter + " : " + file3);
                            break;
                        case 4:
                            file4 = "/tmp/" + value;
                            Utilities.logToConsole("   " + counter + " : " + file4);
                            break;
                    }

                    // Incrementar el contador.
                    counter++;

                    // Si excede a 4 entonces cortar.
                    if (counter > 4)
                    {
                        break;
                    }
                }
            }
            System.out.print("#> ");
            String ask1 = br.readLine();
            if (ask1.equals("1"))
            {
                this.origin = file1;
            }
            else if (ask1.equals("2"))
            {
                this.origin = file2;
            }
            else if (ask1.equals("3"))
            {
                this.origin = file2;
            }
            else if (ask1.equals("4"))
            {
                this.origin = file2;
            }
            else
            {
                this.origin = ask1;
            }

            Utilities.logToConsole("#> Destino:");
            if (!file1.equals(""))
            {
                Utilities.logToConsole("   1 : " + file1 + "_truncate.xml");
            }
            if (!file2.equals(""))
            {
                Utilities.logToConsole("   2 : " + file2 + "_truncate.xml");
            }
            if (!file3.equals(""))
            {
                Utilities.logToConsole("   3 : " + file3 + "_truncate.xml");
            }
            if (!file4.equals(""))
            {
                Utilities.logToConsole("   4 : " + file4 + "_truncate.xml");
            }
            System.out.print("#> ");
            String ask2 = br.readLine();
            if (ask2.equals("1"))
            {
                this.destination = file1 + "_truncate.xml";
            }
            else if (ask2.equals("2"))
            {
                this.destination = file2 + "_truncate.xml";
            }
            else if (ask2.equals("3"))
            {
                this.destination = file3 + "_truncate.xml";
            }
            else if (ask2.equals("4"))
            {
                this.destination = file4 + "_truncate.xml";
            }
            else
            {
                this.destination = ask2;
            }
        }
        catch (IOException ioe)
        {
            Utilities.logToConsole("#> Mensaje: Error al tratar de leer los datos que ha ingresado el usuario.");
            Utilities.logToConsole("#> Mensaje de excepcion: " + ioe.getMessage());
            Utilities.logToConsole("#> StackTrace:");
            ioe.printStackTrace();
        }

        // Comenzar.
        try
        {
            this.parseXML();
        }
        catch (SAXException ex)
        {
            Logger.getLogger(Partitioner.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Partitioner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Constructor por defecto.
     */
    public Partitioner()
    {
    }

    /**
     * Constructor de la clase. Pasamos 2 valores, el archivo de origen y el archivo de destino.
     *
     * @param origin      - El archivo XML de origen.
     * @param destination - El archivo XML de destino.
     */
    public Partitioner(String origin, String destination)
    {
        this.origin = origin;
        this.destination = destination;
    }

    /**
     * Este método inicia el parseado del archivo XML. Desde aqui debemos llamar a los demás
     * métodos.
     * @throws IOException
     * @throws SAXException
     */
    private void parseXML() throws SAXException, IOException
    {
        try
        {
            // Inicializar el parseado.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false); // No validar
            factory.setNamespaceAware(false); // No spacios

            SAXParser parser = factory.newSAXParser();
            parser.parse(new File(this.origin), new Partitioner(this.origin, this.destination)); // Parsear
        }
        catch (ParserConfigurationException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Este método sobreescribe al método de la clase DefaultHandler, y es llamado
     * al iniciarse el parseado, osea al comienzo del documento.
     * Obs.: Aqui debemos escribir la cabecera del archivo XML resultante del parseado.
     * # TODO: Mejorar el manejo de excepciones.
     */
    @Override
    public void startDocument()
    {
        // --- Primera Iteración.
        // Medir el tiempo de parseado. (Comienzo)
        this.startTime = System.currentTimeMillis();
        // Resetear el contador de articulos y de fases.
        Partitioner.articleCounter = 0;
        Partitioner.faseCounter = 0;
        // Autorizar la primera iteracion.
        if (!Partitioner.isSecondIteration)
        {
            Partitioner.isFirstIteration = true;
            Partitioner.continueParsing = true;
        }
        // ----------------------

        // --- Segunda Iteración.
        if (Partitioner.isSecondIteration)
        {
            // Medir el tiempo de parseado. (Comienzo)
            this.startTime = System.currentTimeMillis();
            // Reiniciar las variables.
            Partitioner.articleCounter = 0;
            Partitioner.faseCounter = 0;
            Partitioner.totalArticles = 0;
            this.startTime = 0;
            this.endTime = 0;
            this.totalTime = 0.0;
        }
        // ----------------------
    }

    /**
     * Este método sobreescribe al método de la clase DefaultHandler, y es llamado
     * al terminar el parseado, osea al termino del documento.
     * Obs.: Aqui debemos escribir el pie del archivo XML resultante del parseado.
     * # TODO: Mejorar el manejo de excepciones.
     */
    @Override
    public void endDocument()
    {
        // --- Primera Iteración.
        if (Partitioner.isFirstIteration)
        {
            // Autorizar la segunda iteracion.
            Partitioner.isFirstIteration = false;
            Partitioner.isSecondIteration = true;
            Partitioner.continueParsing = true;
            // Incrementar el contador de fases para los articulos que sobraron.
            Partitioner.faseCounter++;
            // Medir el tiempo de parseado. (Final)
            this.endTime = System.currentTimeMillis();
            this.totalTime = ((this.endTime - this.startTime) / 1000.0);
            // --- Informaciones.
            Utilities.logToConsole("#> Informacion de 1era Iteracion:");
            Utilities.logToConsole("   - Total Articulos: " + (Partitioner.totalArticles + Partitioner.articleCounter));
            Utilities.logToConsole("   - Numero de fases: " + Partitioner.faseCounter);
            Utilities.logToConsole("   - Tiempo de parseado:");
            Utilities.logToConsole("     #> Segundos: " + (this.totalTime));
            Utilities.logToConsole("     #> Minutos:  " + (this.totalTime / 60));
            // ------------------
            // LLamar a la segunda iteracion.
            try
            {
                this.parseXML();
            }
            catch (SAXException ex)
            {
                ex.printStackTrace();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        // ----------------------

        // --- Segunda Iteración.
        if (Partitioner.isSecondIteration)
        {
            // Volver a reemplazar las entidades que el parseador ha convertido.
            String text = this.accumulator.toString();
            Pattern pattern = null;
            String regex1 = "\\<";
            pattern = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = pattern.matcher(text).replaceAll("&lt;");
            String regex2 = "\\>";
            pattern = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = pattern.matcher(text).replaceAll("&gt;");
            String regex3 = "&(?!amp\\;)"; // Negative Lookahead
            pattern = Pattern.compile(regex3, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = pattern.matcher(text).replaceAll("&amp;");
            String regex4 = "@startTag@";
            pattern = Pattern.compile(regex4, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = pattern.matcher(text).replaceAll("<");
            String regex5 = "@endTag@";
            pattern = Pattern.compile(regex5, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            text = pattern.matcher(text).replaceAll(">");
            // Escribir el ultimo archivo.
            OutputStream f = null;
            try
            {
                f = new FileOutputStream(new File(this.destination + "-part_" + (Partitioner.faseCounter + 1) + ".xml"), false);
                OutputStream b = new BufferedOutputStream(f);
                OutputStreamWriter out = new OutputStreamWriter(b, "UTF8");
                out.write(text);
                out.flush();
                out.close();
            }
            catch (UnsupportedEncodingException uex)
            {
                uex.printStackTrace();
            }
            catch (FileNotFoundException ex)
            {
                Utilities.logToConsole("#> Mensaje: No se ha podido crear/abrir el archivo -> " + this.destination + "-part_" + (Partitioner.faseCounter + 1) + ".xml");
                Utilities.logToConsole("#> Mensaje de excepcion: " + ex.getMessage());
                Utilities.logToConsole("#> StackTrace:");
                ex.printStackTrace();
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
            finally
            {
                try
                {
                    f.close();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            } // End of try-catch statement.
            // Medir el tiempo de parseado. (Final)
            this.endTime = System.currentTimeMillis();
            this.totalTime = ((this.endTime - this.startTime) / 1000.0);
            // --- Informaciones.
            Utilities.logToConsole("");
            Utilities.logToConsole("#> El archivo se ha truncado exitosamente!");
            Utilities.logToConsole("#> Informacion de 2da Iteracion:");
            Utilities.logToConsole("   - Total Articulos:            " + (Partitioner.totalArticles + Partitioner.articleCounter));
            Utilities.logToConsole("   - Numero de archivos creados: " + (Partitioner.faseCounter + 1));
            Utilities.logToConsole("   - Tiempo de parseado:");
            Utilities.logToConsole("     #> Segundos: " + (this.totalTime));
            Utilities.logToConsole("     #> Minutos:  " + (this.totalTime / 60));
            // ------------------
        }
        // ----------------------
    }

    /**
     * Este método es llamado cuando el parseador abre una etiqueta.
     * @param uri
     * @param name
     * @param qName
     * @param atts
     */
    @Override
    public void startElement(String uri, String name, String qName, Attributes atts)
    {
        // --- Primera Iteración.
        if (Partitioner.isFirstIteration)
        {
            // Contar los articulos.
            // Obs.: Los articulos comienzan con la etiqueta <page>
            if (qName.equals("page"))
            {
                Partitioner.articleCounter++;
            }
            // Incrementar las fases.
            if (Partitioner.articleCounter == Partitioner.articleAmount)
            {
                // Incrementar el contador de fases.
                Partitioner.faseCounter++;
                // Sumar al total de articulos.
                Partitioner.totalArticles += Partitioner.articleCounter;
                // Reiniciar el contador de articulos.
                Partitioner.articleCounter = 0;
            }
        }
        // ----------------------

        // --- Segunda Iteración.
        if (Partitioner.isSecondIteration)
        {
            // Guardar la etiqueta.
            this.accumulator.append("@startTag@" + qName + "@endTag@");
            // Incrementar el contador de articulos.
            if (qName.equals("page"))
            {
                Partitioner.articleCounter++;
            }
        }
        // ----------------------
    }

    /**
     * Este método hemos sobreescribido de la clase DefaultHandler y es llamado
     * cuando el parseador encuentra texto #PCDATA entre las etiquetas. Aqui es donde
     * debemos guardar el texto encontrado en variables de clase que luego serán
     * procesadas en endElement() por el parseador.
     * @param ch
     * @param start
     * @param length
     */
    @Override
    public void characters(char ch[], int start, int length)
    {
        // --- Primera Iteración.
        // Hacer nada.
        // ----------------------

        // --- Segunda Iteración.
        if (Partitioner.isSecondIteration)
        {
            // Acumular los textos dentro de las etiquetas.
            this.accumulator.append(ch, start, length);
        }
        // ----------------------
    }

    /**
     * Este método es llamado cuando el parseador cierra una etiqueta.
     * @param uri
     * @param name
     * @param qName
     */
    @Override
    public void endElement(String uri, String name, String qName)
    {
        // --- Primera Iteración.
        // Hacer nada.
        // ----------------------

        // --- Segunda Iteración.
        if (Partitioner.isSecondIteration)
        {
            // Guardar la etiqueta.
            this.accumulator.append("@startTag@/" + qName + "@endTag@");
            if (qName.equals("page") && Partitioner.articleCounter == Partitioner.articleAmount)
            {
                // Sumar al total de articulos.
                Partitioner.totalArticles += Partitioner.articleCounter;
                // Reiniciar el contador de articulos.
                Partitioner.articleCounter = 0;
                // Incrementar la fase.
                Partitioner.faseCounter++;
                // Guardar la etiqueta final.
                this.accumulator.append("@startTag@/mediawiki@endTag@");
                // Volver a reemplazar las entidades que el parseador ha convertido.
                String text = this.accumulator.toString();
                Pattern pattern = null;
                String regex1 = "\\<";
                pattern = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                text = pattern.matcher(text).replaceAll("&lt;");
                String regex2 = "\\>";
                pattern = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                text = pattern.matcher(text).replaceAll("&gt;");
                String regex3 = "&(?!amp\\;)";
                pattern = Pattern.compile(regex3, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                text = pattern.matcher(text).replaceAll("&amp;");
                String regex4 = "@startTag@";
                pattern = Pattern.compile(regex4, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                text = pattern.matcher(text).replaceAll("<");
                String regex5 = "@endTag@";
                pattern = Pattern.compile(regex5, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                text = pattern.matcher(text).replaceAll(">");
                // Escribir el archivo.
                OutputStream f = null;
                try
                {
                    f = new FileOutputStream(new File(this.destination + "-part_" + Partitioner.faseCounter + ".xml"), false);
                    OutputStream b = new BufferedOutputStream(f);
                    OutputStreamWriter out = new OutputStreamWriter(b, "UTF8");
                    out.write(text);
                    out.flush();
                    out.close();
                }
                catch (UnsupportedEncodingException uex)
                {
                    uex.printStackTrace();
                }
                catch (FileNotFoundException ex)
                {
                    Utilities.logToConsole("#> Mensaje: No se ha podido crear/abrir el archivo -> " + this.destination + "-part_" + Partitioner.faseCounter + ".xml");
                    Utilities.logToConsole("#> Mensaje de excepcion: " + ex.getMessage());
                    Utilities.logToConsole("#> StackTrace:");
                    ex.printStackTrace();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
                finally
                {
                    try
                    {
                        f.close();
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                } // End of try-catch statement.
                // Reiniciar el acumulador.
                this.accumulator.setLength(0);
                // Añadir la etiqueta inicial, para el proximo archivo.
                this.accumulator.append("@startTag@mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" "
                        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                        + "xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ "
                        + "http://www.mediawiki.org/xml/export-0.3.xsd\" "
                        + "version=\"0.3\" xml:lang=\"es\"@endTag@");
            }
        }
        // ----------------------
    }
}
