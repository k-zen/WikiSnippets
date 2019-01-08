package net.apkc.wikisnippets;
// ---------------------------

// --- Lucene
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

// --- I/O
import java.io.File;
import java.io.IOException;

// --- Util
import java.util.*;

// --- MBS:WikiSnippets
import net.apkc.wikisnippets.model.analysis.analyzers.SpanishAnalyzer;

// --- Apache Commons Lang
import org.apache.commons.lang.*;

/**
 * Esta clase busca el indice invertido utilizando las librerias Lucene v2.4.1.
 *
 * @author  Andreas P. Koenzen
 */
public class Test {

  /**
   * Este método busca la frase introducida en el indice indicado.
   *
   * @param  queryPhrase La frase a buscar en el indice.
   * @param  indexLoc
   * @param  hitsPerPage
   * @throws IOException
   * @throws ParseException
   */
  public void find ( String queryPhrase, String indexLoc, String hitsPerPage ) throws IOException, ParseException, ClassNotFoundException {
    long startTime = 0; // Timer del inicio del proceso.
    long endTime = 0; // Timer del termino del proceso.
    double totalTime = 0.0; // Tiempo total transcurrido.
    // Crear el buscador del indice.
    IndexSearcher searcher = new IndexSearcher ( new SimpleFSDirectory ( new File ( indexLoc ) ) );
    // Casillas a buscar.
    String[] fields = new String[]{ "title", "header", "text", "images" };
    // Sistema de pesos entre casillas.
    HashMap<String, Float> boosts = new HashMap<String, Float> ();
    boosts.put ( "title", new Float ( 20 ) );
    boosts.put ( "header", new Float ( 15 ) );
    boosts.put ( "text", new Float ( 10 ) );
    boosts.put ( "images", new Float ( 5 ) );
    // Crear un parseador de casillas multiples.
    MultiFieldQueryParser queryParser = new MultiFieldQueryParser ( Version.LUCENE_30, fields, new SpanishAnalyzer ( true ), boosts );
    // Parsear la pregunta.
    Query query = queryParser.parse ( queryPhrase );
    // Buscar la pregunta.
    // Medir el tiempo de parseado. (Comienzo)
    startTime = System.currentTimeMillis ();
    ScoreDoc[] hits = searcher.search ( query, null, Integer.parseInt ( hitsPerPage ) ).scoreDocs;
    // --- Buscar sugerencias del diccionario.
    /*
    // Para el diccionario Lucene.
    Directory spellDir = FSDirectory.getDirectory ( indexLoc + "/luceneDictionary/index" );
    SpellChecker spellChecker = new SpellChecker ( spellDir );
    String[] suggestionsLucene = spellChecker.suggestSimilar ( queryPhrase, 2 );
    // Para el diccionario LingPipe.
    CompiledSpellChecker sc = readModel ( new File ( indexLoc + "/lingpipeDictionary/SpellCheck.model" ) ); // # TODO: Aqui es donde tarda mucho tiempo. Leyendo el modelo con cada query. Corregir.
    String suggestionLingpipe = sc.didYouMean ( queryPhrase );
    // Medir el tiempo de parseado. (Final)
    endTime = System.currentTimeMillis ();
    totalTime = ( ( endTime - startTime ) / 1000.0 );
     */
    String[] suggestionsLucene = null;
    String suggestionLingpipe = null;
    // Construir la respuesta.
    this.buildResponse ( query, searcher, hits, hits.length, totalTime, suggestionsLucene, suggestionLingpipe );
  } // End of find()

  /**
   * Este método construye la respuesta.
   *
   * @param  query El objeto que contiene la pregunta.
   * @param  searcher El objeto buscador del indice.
   * @param  hits El objeto que contiene los resultados.
   * @param  tHits El numero total de hits.
   * @param searchTime
   * @param suggestionsLucene
   * @throws IOException
   */
  public void buildResponse ( Query query, Searcher searcher, ScoreDoc[] hits, int tHits, double searchTime, String[] suggestions, String suggestion ) throws IOException {
    String queryString = query.toString (); // La pregunta remitida al indice.
    int totalHits = tHits; // La cantidad de hits.
    double sTime = searchTime; // El tiempo que tardo la busqueda.
    String title = ""; // El titulo del articulo.
    String header = ""; // La cabecera del articulo.
    String text = ""; // El texto del articulo.
    String[] images; // Arreglo de imagenes encontradas.
    // Construir la respuesta.
    System.out.println ( "# Frase:                " + queryString );
    System.out.println ( "# Concordancias:        " + totalHits );
    System.out.println ( "# Tiempo de busqueda:   " + sTime );
    System.out.println ( "# Sugerencias Lucene:   " + StringUtils.join ( suggestions, " " ) );
    System.out.println ( "# Sugerencias LingPipe: " + suggestion );
    System.out.println ( "" );
    System.out.println ( "-----------------------" );
    System.out.println ( "" );

    // Iterar a traves de los resultados.
    for ( int i = 0; i < hits.length; i++ ) {
      int docId = hits[i].doc;
      Document doc = searcher.doc ( docId );
      // --- Titulo.
      title = doc.get ( "title" );
      // --- Cabecera.
      header = doc.get ( "header" );
      // --- Texto.
      text = doc.get ( "text" );
      // --- Imagenes.
      images = doc.get ( "images" ).split ( "\\@" );
      // Construir la respuesta.
      System.out.println ( "# Titulo:   " + title );
      System.out.println ( "# Cabecera: " + header );
      if ( !doc.get ( "images" ).equals ( "" ) ) {
        System.out.println ( "# Imagenes:    " );
        for ( String value : images ) {
          System.out.println ( "   - " + value );
        } // End of for loop
      } // End of if statement
      System.out.println ( "# Texto:    " + text );
      System.out.println ( "" );
      System.out.println ( "------" );
      System.out.println ( "" );
    } // End of for loop
    // Cerrar el buscador del indice.
    searcher.close ();
  }
}
