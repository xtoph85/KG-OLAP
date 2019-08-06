package at.jku.dke.kgolap.repo.sesame;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultWriterBase;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * Based on Sesame's SPARQLResultsCSVHandler written by Jeen Broekstra.
 *
 */
public class QuadQueryResultsNQuadsWriter extends QueryResultWriterBase implements TupleQueryResultWriter {

  private Writer writer;

  /**
   * @param out
   */
  public QuadQueryResultsNQuadsWriter(OutputStream out) {
    Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
    writer = new BufferedWriter(w, 1024);
  }

  @Override
  public void startQueryResult(List<String> bindingNames)
      throws TupleQueryResultHandlerException {
  }

  @Override
  public void endQueryResult() throws TupleQueryResultHandlerException {
    try {
      writer.flush();
    }
    catch (IOException e) {
      throw new TupleQueryResultHandlerException(e);
    }
  }

  @Override
  public void handleBoolean(boolean value) throws QueryResultHandlerException {
    // ignored by QuadQueryResultHandler
  }

  @Override
  public void handleLinks(List<String> linkUrls)
      throws QueryResultHandlerException {
    // ignored by QuadQueryResultHandler    
  }

  @Override
  public void handleSolution(BindingSet bindingSet)
      throws TupleQueryResultHandlerException {
    try {
      Value graph = bindingSet.getValue("g");
      Value subject = bindingSet.getValue("s");
      Value predicate = bindingSet.getValue("p");
      Value object = bindingSet.getValue("o");

      writeValue(subject);
      
      writer.write(" ");
      
      writeValue(predicate);
      
      writer.write(" ");
      writeValue(object);
      
      writer.write(" ");
      writeValue(graph);
      
      writer.write(" .\r\n");
    }
    catch (IOException e) {
      throw new TupleQueryResultHandlerException(e);
    }
  }
  
  private void writeValue(Value val) throws IOException
    {
      if (val instanceof Resource) {
        writeResource((Resource) val);
      }
      else {
        writeLiteral((Literal) val);
      }
    }
  
  private void writeResource(Resource res) throws IOException {
    if (res instanceof URI) {
      writeURI((URI) res);
    }
    else {
      writeBNode((BNode) res);
    }
  }
  
  protected void writeURI(URI uri) throws IOException {
    String uriString = uri.toString();
    boolean quoted = uriString.contains(",");

    writer.write("<");
    
    if (quoted) {
      // write opening quote for entire value
      writer.write("\"");
    }

    writer.write(uriString);

    if (quoted) {
      // write closing quote for entire value
      writer.write("\"");
    }
    
    writer.write(">");
  }

  protected void writeBNode(BNode bNode) throws IOException {
    writer.write("_:");
    writer.write(bNode.getID());
  }

  private void writeLiteral(Literal literal) throws IOException {
    String label = literal.getLabel();
    URI datatype = literal.getDatatype();

    boolean quoted = false;

    if (XMLDatatypeUtil.isIntegerDatatype(datatype) || 
        XMLDatatypeUtil.isDecimalDatatype(datatype) || 
        XMLSchema.DOUBLE.equals(datatype)) {
      try {
        String normalized = XMLDatatypeUtil.normalize(label, datatype);
        writer.write(normalized);
        return; // done
      }
      catch (IllegalArgumentException e) {
        // not a valid numeric datatyped literal. ignore error and write as
        // (optionally quoted) string instead.
      }
    }

    if (label.contains(",") || label.contains("\r") || label.contains("\n") || label.contains("\"")) {
      quoted = true;

      // escape quotes inside the string
      label = label.replaceAll("\"", "\"\"");

      // add quotes around the string (escaped with a second quote for the
      // CSV parser)
      // label = "\"\"" + label + "\"\"";
    }

    if (quoted) {
      // write opening quote for entire value
      writer.write("\"");
    }

    writer.write("\"" + label + "\"^^<" + datatype.toString() + ">");

    if (quoted) {
      // write closing quote for entire value
      writer.write("\"");
    }

  }

  @Override
  public void handleNamespace(String prefix, String uri)
      throws QueryResultHandlerException {
    // ignored by QuadQueryResultHandler    
  }

  @Override
  public void startDocument() throws QueryResultHandlerException {
    // ignored by QuadQueryResultHandler    
  }

  @Override
  public void handleStylesheet(String stylesheetUrl)
      throws QueryResultHandlerException {
    // ignored by QuadQueryResultHandler    
  }

  @Override
  public void startHeader() throws QueryResultHandlerException {
    // ignored by QuadQueryResultHandler    
  }

  @Override
  public void endHeader() throws QueryResultHandlerException {
    // ignored by QuadQueryResultHandler    
  }

  @Override
  public TupleQueryResultFormat getTupleQueryResultFormat() {
    return TupleQueryResultFormat.BINARY;
  }

  @Override
  public final TupleQueryResultFormat getQueryResultFormat() {
    return getTupleQueryResultFormat();
  }

}
