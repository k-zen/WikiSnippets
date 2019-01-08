package net.apkc.wikisnippets.tasks.run;

// Apache Commons Lang
import org.apache.commons.lang.*;

// Apache Lucene
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

// I/O
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

// WikiSnippets
import net.apkc.wikisnippets.analysis.analyzers.SpanishAnalyzer;

// Util
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// XML Parsing
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Esta clase construye el indice invertido utilizando las librerias Lucene v3.0.
 *
 * @author Andreas P. Koenzen
 */
public class Indexer extends DefaultHandler {

    private String currentStartTag = ""; // Etiqueta inicial.
    private String currentEndTag = ""; // Etiqueta final
    private String title = "";
    private String header = "";
    private String text = "";
    private String images = "";
    private StringBuffer accumulator = new StringBuffer(); // Acumulador de caracteres parseados
    private Map<String, String> imageArray = new HashMap<>();
    private List<String> imageAttributes = new ArrayList<>();
    private static IndexWriter writer;
    private long startTime = 0; // Timer del inicio del proceso.
    private long endTime = 0; // Timer del termino del proceso.
    private double totalTime = 0.0; // Tiempo total transcurrido.
    private static boolean continueParsing = false; // Controla que el parseado se realize solo 2 veces.
    private static boolean isFirstIteration = false; // Si es la primera iteracion.
    private static boolean isSecondIteration = false; // Si es la segunda iteracion.
    private static long totalArticles = 0; // Total de articulos contados durante el analisis.
    private static long articleCounter = 0; // Contador de articulos.
    private static long validArticlesCounter = 0; // Contador de articulos validos.
    private static long indexedArticles = 0; // Total articulos indexados.
    // Variables de desicion.
    private String origin = ""; // Origen del archivo XML
    private String destination = ""; // Destino del archivo XML
    private static String indexNoValidArticles = ""; // Si debemos indexar articulos no validos o no.
    private boolean showHeaderControl = true; // Establece si mostrar las primeras 15 palabras de la cabecera con problemas.

    public Indexer() {
    }

    public Indexer(String origin, String destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public void init() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            Utilities.logToConsole("<<<<<<<<< WikiSnippets:Index >>>>>>>>>");
            Utilities.logToConsole("#> Archivo a indexar:");
            // Listar los archivos. Carpeta por defecto -> /tmp
            File f = new File("/tmp");
            String[] fs = f.list();
            // Iterar los archivos y mostrar solo los XML.
            short counter = 1;
            String file1 = "";
            String file2 = "";
            String file3 = "";
            String file4 = "";
            for (String value : fs) {
                if (value.toLowerCase().endsWith("xml")) {
                    switch (counter) {
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
                    if (counter > 4) {
                        break;
                    }
                }
            }
            System.out.print("#> ");
            String ask1 = br.readLine();
            if (ask1.equals("1")) {
                this.origin = file1;
            }
            else if (ask1.equals("2")) {
                this.origin = file2;
            }
            else if (ask1.equals("3")) {
                this.origin = file2;
            }
            else if (ask1.equals("4")) {
                this.origin = file2;
            }
            else {
                this.origin = ask1;
            }

            Utilities.logToConsole("#> Destino del indice:");
            Utilities.logToConsole("#> Ejemplo: /mbs/crawljobs/<timestamp>/wikisnippets");
            System.out.print("#> ");
            String ask2 = br.readLine();
            if (!ask2.equals("")) {
                this.destination = ask2;
            }

            Utilities.logToConsole("#> Indexar articulos no validos? [s][n]");
            System.out.print("#> ");
            String ask3 = br.readLine();
            Indexer.indexNoValidArticles = ask3.equals("s") || ask3.equals("n") ? ask3 : "n";
        }
        catch (IOException ioe) {
            Utilities.logToConsole("#> Mensaje: Error al tratar de leer los datos.");
            Utilities.logToConsole("#> Mensaje de excepcion: " + ioe.getMessage());
            Utilities.logToConsole("#> StackTrace:");
            ioe.printStackTrace();
        }

        try {
            this.index();
        }
        catch (SAXException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void index() throws SAXException, IOException {
        try {
            // Inicializar el parseado.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false); // No validar
            factory.setNamespaceAware(false); // No spacios

            SAXParser parser = factory.newSAXParser();
            parser.parse(new File(this.origin), new Indexer(this.origin, this.destination)); // Parsear
        }
        catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    private void addDocument() {
        // Preparar el arreglo de imagenes.
        Iterator<Map.Entry<String, String>> itr = this.imageArray.entrySet().iterator();
        StringBuffer sb = new StringBuffer();
        if (this.imageArray.size() == 1) {
            // Formato: fileName|altTag|caption|format
            while (itr.hasNext()) {
                Map.Entry element = (Map.Entry) itr.next();
                sb.append(element.getKey() + "|" + element.getValue());
            }
            this.images = sb.toString();
        }
        else if (this.imageArray.size() > 1) {
            // Formato: fileName|altTag|caption|format@fileName|altTag|caption|format
            while (itr.hasNext()) {
                Map.Entry element = (Map.Entry) itr.next();
                sb.append(element.getKey() + "|" + element.getValue());
                sb.append("@");
            }
            this.images = StringUtils.chop(sb.toString());
        }
        // Preparar las casillas.
        Document doc = new Document();
        Field titleField = new Field("title", this.title, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO);
        Field titleFilterField = new Field("titlefilter", this.title, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO);
        Field headerField = new Field("header", this.header, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO);
        Field headerFilterField = new Field("headerfilter", this.header, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO);
        Field textField = new Field("text", this.text, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        Field textFilterField = new Field("textfilter", this.text, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
        // Guardar aqui las imagenes. Separados por comas (,)
        // Si existen, ya que las imagenes son opcionales.
        Field imagesField = new Field("images", this.images, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO);
        doc.add(titleField);
        doc.add(titleFilterField);
        doc.add(headerField);
        doc.add(headerFilterField);
        doc.add(textField);
        doc.add(textFilterField);
        doc.add(imagesField);
        try {
            writer.addDocument(doc);
        }
        catch (CorruptIndexException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Incrementar el contador de articulos indexados.
        Indexer.indexedArticles++;

        // Reiniciar las variables.
        this.images = "";
        this.imageArray.clear();
        this.imageAttributes.clear();
        sb.setLength(0);

        // Variables temporales.
        double estimateRemainingPercentage = 0.0;

        // Mostrar informacion.
        // Calculos.
        // Porcentaje <------> Total Articulos
        //          x <------> Articulos Indexados
        // Ej. 100 <------> 53232
        //       x <------> ~2500
        estimateRemainingPercentage = (((Indexer.indexedArticles) * (100)) / Indexer.totalArticles);

        // Mostrar el progreso.
        System.out.print("#> Estimado Restante: " + Utilities.roundTwoDecimals(estimateRemainingPercentage) + "%   \r");
    }

    /**
     * Este método sobreescribe al método de la clase DefaultHandler, y es llamado
     * al iniciarse el parseado, osea al comienzo del documento.
     * Obs.: Aqui debemos escribir la cabecera del archivo XML resultante del parseado.
     * # TODO: Mejorar el manejo de excepciones.
     */
    @Override
    public void startDocument() {
        // --- Primera Iteración.
        // Medir el tiempo de parseado. (Comienzo)
        this.startTime = System.currentTimeMillis();
        // Resetear el contador de articulos.
        Indexer.articleCounter = 0;
        // Resetear el contador de articulos validos.
        Indexer.validArticlesCounter = 0;
        // Autorizar la primera iteracion.
        if (!Indexer.isSecondIteration) {
            Indexer.isFirstIteration = true;
            Indexer.continueParsing = true;
        }
        // ----------------------

        // --- Segunda Iteración.
        if (Indexer.isSecondIteration) {
            // Resetear el contador de articulos.
            Indexer.articleCounter = 0;
            // Resetear el contador de articulos validos.
            Indexer.validArticlesCounter = 0;
            Indexer.indexedArticles = 0;
            // Reiniciar las variables.
            this.startTime = 0;
            this.endTime = 0;
            this.totalTime = 0.0;
            // Medir el tiempo de parseado. (Comienzo)
            this.startTime = System.currentTimeMillis();
            // Abrir el escritor del indice.
            try {
                final PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new SpanishAnalyzer(false, true));
                analyzer.addAnalyzer("title", new StandardAnalyzer(Version.LUCENE_30));
                analyzer.addAnalyzer("header", new StandardAnalyzer(Version.LUCENE_30));
                analyzer.addAnalyzer("text", new StandardAnalyzer(Version.LUCENE_30));
                writer = new IndexWriter(new SimpleFSDirectory(new File(this.destination)), analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
            }
            catch (CorruptIndexException ex) {
                Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (LockObtainFailedException ex) {
                Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex) {
                Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
            }
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
    public void endDocument() {
        // --- Primera Iteración.
        if (Indexer.isFirstIteration) {
            // Asignar el total de articulos a la variable que lo corresponde.
            Indexer.totalArticles = Indexer.articleCounter;
            // Medir el tiempo de parseado. (Final)
            this.endTime = System.currentTimeMillis();
            this.totalTime = ((this.endTime - this.startTime) / 1000.0);
            // --- Informaciones.
            Utilities.logToConsole("#> Analisis del archivo a indexar:");
            Utilities.logToConsole("   - Total Articulos: " + Indexer.articleCounter);
            Utilities.logToConsole("   - Total Articulos Validos: " + Indexer.validArticlesCounter);
            Utilities.logToConsole("   - Tiempo de ejecucion:");
            Utilities.logToConsole("     - Segundos: " + (this.totalTime));
            Utilities.logToConsole("     - Minutos:  " + (this.totalTime / 60));
            // ------------------
            // Presentar al usuario la opcion de continuar.
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String createIndex = null;
            try {
                // Preguntas.
                Utilities.logToConsole("#> Crear el indice? [s][n]");
                System.out.print("#> ");
                String ask = br.readLine();
                createIndex = ask.equals("s") || ask.equals("n") ? ask : "n";
            }
            catch (IOException ioe) {
                Utilities.logToConsole("#> Mensaje: Error al tratar de leer los datos.");
                Utilities.logToConsole("#> Mensaje de excepcion: " + ioe.getMessage());
                Utilities.logToConsole("#> StackTrace:");
                ioe.printStackTrace();
            }
            if (createIndex.equals("s")) {
                // Autorizar la segunda iteracion.
                Indexer.isFirstIteration = false;
                Indexer.isSecondIteration = true;
                Indexer.continueParsing = true;
                // LLamar a la segunda iteracion.
                try {
                    this.index();
                }
                catch (SAXException ex) {
                    Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (IOException ex) {
                    Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else {
                Utilities.logToConsole("#> Proceso terminado!");
            }
        }
        // ----------------------

        // --- Segunda Iteración.
        if (Indexer.isSecondIteration) {
            // Cerrar las iteraciones.
            Indexer.isSecondIteration = false;
            // Cerrar el escritor del indice.
            try {
                writer.optimize();
                writer.close();
            }
            catch (CorruptIndexException ex) {
                Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex) {
                Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Medir el tiempo de parseado. (Final)
            this.endTime = System.currentTimeMillis();
            this.totalTime = ((this.endTime - this.startTime) / 1000.0);

            // --- Informaciones.
            Utilities.logToConsole("#> Informacion de Indexamiento:");
            Utilities.logToConsole("   - Total Articulos: " + Indexer.articleCounter);
            Utilities.logToConsole("   - Total Articulos Validos: " + Indexer.validArticlesCounter);
            Utilities.logToConsole("   - Total Articulos Indexados: " + Indexer.indexedArticles);
            Utilities.logToConsole("   - Tiempo de ejecucion:");
            Utilities.logToConsole("     - Segundos: " + (this.totalTime));
            Utilities.logToConsole("     - Minutos:  " + (this.totalTime / 60));
            // ------------------
        }
        // ----------------------
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        // --- Primera Iteración.
        if (Indexer.isFirstIteration) {
            // Contar los articulos.
            // Obs.: Los articulos comienzan con la etiqueta <page>
            if (qName.equals("page")) {
                Indexer.articleCounter++;
            }
            // Reiniciar el acumulador de texto cada vez que encontramos una etiqueta nueva.
            this.accumulator.setLength(0);
            // Guardar la etiqueta que abre.
            this.currentStartTag = qName.trim();
        }
        // ----------------------

        // --- Segunda Iteración.
        if (Indexer.isSecondIteration) {
            // Contar los articulos.
            // Obs.: Los articulos comienzan con la etiqueta <page>
            if (qName.equals("page")) {
                Indexer.articleCounter++;
            }
            // Reiniciar el acumulador de texto cada vez que encontramos una etiqueta nueva.
            this.accumulator.setLength(0);
            // Guardar la etiqueta que abre.
            this.currentStartTag = qName.trim();
            // Guardar los atributos de las imagenes.
            // Formato: fileName|altTag|caption|format
            String altTag = "null";
            String caption = "null";
            String fileFormat = "null";
            if (this.currentStartTag.equals("image")) {
                if (!atts.getValue("altTag").equals("")) {
                    altTag = atts.getValue("altTag");
                }
                if (!atts.getValue("caption").equals("")) {
                    caption = atts.getValue("caption");
                }
                if (!atts.getValue("fileFormat").equals("")) {
                    fileFormat = atts.getValue("fileFormat");
                }
                this.imageAttributes.add(altTag + "|" + caption + "|" + fileFormat);
            }
        }
        // ----------------------
    }

    @Override
    public void characters(char ch[], int start, int length) {
        // --- Primera Iteración.
        if (Indexer.isFirstIteration) {
            // Acumular los textos.
            this.accumulator.append(ch, start, length);
        }
        // ----------------------

        // --- Segunda Iteración.
        if (Indexer.isSecondIteration) {
            // Acumular los textos.
            this.accumulator.append(ch, start, length);
        }
        // ----------------------
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        // --- Primera Iteración.
        if (Indexer.isFirstIteration) {
            // Guardar la etiqueta que cierra.
            this.currentEndTag = qName.trim();
            // Ver que etiqueta estamos parseando y en base a eso realizar el procesado de validacion y conteo de articulos validos.
            if (this.currentEndTag.equals("header")) {
                String headerTmp = this.accumulator.toString().trim();
                // Realizar el proceso de conteo de articulos validos.
                String[] tmpArray = StringUtils.split(headerTmp, "/"); // Separa la cabecera del titulo.
                if (tmpArray.length > 1) { // Entonces posee cabecera.
                    String[] tokenizedText = StringUtils.split(tmpArray[1], " "); // La cabecera tokenizada.
                    if (tokenizedText.length <= 20) { // Si la cabecera contiene menos o 20 palabras.
                        Indexer.validArticlesCounter++; // Incrementar el contador de articulos validos.
                    }
                    else { // Si es una cabecera no valida, o con errores.
                        // TAREAS:
                        // - Contar exactamente la cantidad de palabras que posee la cabecera.
                        // - Ver que articulo es el que se trata.
                        // - Presentar la opcion de mostrar las primeras 15 palabras de dicha cabecera, junto con el titulo del articulo.
                        String wordCount = String.valueOf(tokenizedText.length); // Cantidad de palabras que posee la cabecera.
                        String invalidArticleTitle = tmpArray[0]; // Titulo del articulo invalido.
                        // Mostrar el titulo del articulo no valido.
                        Utilities.logToConsole("#> Cantidad de palabras que posee la cabecera: " + wordCount);
                        Utilities.logToConsole("#> Articulo: " + invalidArticleTitle);

                        // Presentar al usuario la opcion de mostrar las primeras 15 palabras del articulo con errores.
                        if (this.showHeaderControl) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                            String showHeader = null;
                            try {
                                Utilities.logToConsole("#> Mostrar las primeras 15 palabras de la cabecera? [s][n][nmm]");
                                System.out.print("#> ");
                                String ask = br.readLine();
                                showHeader = ask.equals("s") || ask.equals("n") ? ask : "nmm";
                            }
                            catch (IOException ioe) {
                                Utilities.logToConsole("#> Mensaje: Error al tratar de leer los datos.");
                                Utilities.logToConsole("#> Mensaje de excepcion: " + ioe.getMessage());
                                Utilities.logToConsole("#> StackTrace:");
                                ioe.printStackTrace();
                            }

                            if (showHeader.equals("s")) {
                                String[] compiledHeaderArray = new String[15];

                                for (int i = 0; i < 15; i++) {
                                    compiledHeaderArray[i] = tokenizedText[i];
                                }

                                String compiledHeader = StringUtils.join(compiledHeaderArray, " ");

                                Utilities.logToConsole(compiledHeader + " ... ");
                            }
                            else if (showHeader.equals("n")) {
                                // Hacer nada.
                            }
                            else {
                                this.showHeaderControl = false; // No mostrar mas ese mensaje.
                            }
                        } // End of if statement
                    } // End of if statement
                }
                // Si llega aqui, entonces es la cabecera principal del articulo.
                else if (tmpArray.length <= 1) {
                    Indexer.validArticlesCounter++; // Incrementar el contador de articulos validos.
                }
            } // End of if statement
        } // End of if statement
        // ----------------------

        // --- Segunda Iteración.
        if (Indexer.isSecondIteration) {
            // Guardar la etiqueta que cierra.
            this.currentEndTag = qName.trim();

            // Ver que etiqueta estamos parseando y en base a eso asignar el titulo y el texto.
            if (this.currentEndTag.equals("title")) {
                this.title = this.accumulator.toString().trim();
            }
            else if (this.currentEndTag.equals("header")) {
                this.header = this.accumulator.toString().trim();
            }
            else if (this.currentEndTag.equals("text")) {
                this.text = this.accumulator.toString().trim();
            }
            else if (this.currentEndTag.equals("image")) {
                // Formato: fileName|altTag|caption|format
                this.imageArray.put(this.accumulator.toString().trim(), this.imageAttributes.get(0));
            }

            // Una vez que encontramos la etiqueta que cierra cada articulo (</page>), entonces indexamos los datos.
            if (this.currentEndTag.equals("page")) {
                // Si el articulo es valido.
                String[] tmpArray = StringUtils.split(this.header, "/"); // Separa la cabecera del titulo.
                if (tmpArray.length > 1) { // Entonces posee cabecera.
                    String[] tokenizedText = StringUtils.split(tmpArray[1], " "); // La cabecera tokenizada.
                    boolean isValid = false;
                    if (tokenizedText.length <= 20) { // Si la cabecera contiene menos o 20 palabras.
                        Indexer.validArticlesCounter++; // Incrementar el contador de articulos validos.
                        isValid = true;
                    }
                    else {
                        isValid = false;
                    }
                    // Indexar articulos no validos.
                    if (Indexer.indexNoValidArticles.equals("s")) {
                        this.addDocument();
                    }
                    // Indexar solo articulos validos.
                    else {
                        if (isValid) { // Solo si el articulo es valido.
                            this.addDocument();
                        }
                    }
                }
                // Si llega aqui, entonces es la cabecera principal del articulo.
                else if (tmpArray.length <= 1) {
                    Indexer.validArticlesCounter++; // Incrementar el contador de articulos validos.
                    this.addDocument(); // Indexar este articulo.
                }
            } // End of if statement
        } // End of if statement
        // ----------------------
    }
}
