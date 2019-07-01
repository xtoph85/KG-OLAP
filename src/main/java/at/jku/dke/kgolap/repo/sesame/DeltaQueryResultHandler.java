package at.jku.dke.kgolap.repo.sesame;

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

import eu.fbk.rdfpro.util.QuadModel;

public class DeltaQueryResultHandler implements TupleQueryResultHandler {
  private List<String> bindingNames = null;
  
  private QuadModel insertStatements = null;
  private QuadModel deleteStatements = null;
  
  public DeltaQueryResultHandler(QuadModel insertStatements,
                                 QuadModel deleteStatements) {
    this.insertStatements = insertStatements;
    this.deleteStatements = deleteStatements;
  }
    
  public QuadModel getInsertStatements() {
    return insertStatements;
  }

  public void setInsertStatements(QuadModel insertStatements) {
    this.insertStatements = insertStatements;
  }

  public QuadModel getDeleteStatements() {
    return deleteStatements;
  }

  public void setDeleteStatements(QuadModel deleteStatements) {
    this.deleteStatements = deleteStatements;
  }

  @Override
  public void handleBoolean(boolean value) throws QueryResultHandlerException {
    // ignore
  }

  @Override
  public void handleLinks(List<String> linkUrls) throws QueryResultHandlerException {
    // ignore
  }

  @Override
  public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
    this.bindingNames = bindingNames;
    
    if(!bindingNames.contains("g") ||
       !bindingNames.contains("s") ||
       !bindingNames.contains("p") ||
       !bindingNames.contains("o") ||
       !bindingNames.contains("op")) {
      throw new TupleQueryResultHandlerException("Delta query must contain bindings ?g, ?s, ?p, ?o, and ?op.");
    }
  }

  @Override
  public void endQueryResult() throws TupleQueryResultHandlerException {
    if (bindingNames == null) {
        throw new IllegalStateException(
                "Could not end query result as startQueryResult was not called first.");
    }
  }

  @Override
  public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
    if (bindingNames == null) {
        throw new IllegalStateException("Must call startQueryResult before handleSolution");
    }
    
    Value g = bindingSet.getValue("g");
    Value s = bindingSet.getValue("s");
    Value p = bindingSet.getValue("p");
    Value o = bindingSet.getValue("o");
    
    if(s instanceof Resource &&
       p instanceof URI &&
       g instanceof Resource) {
      
      Value op = bindingSet.getValue("op");
      
      if(op instanceof Literal) {
        String opLabel = ((Literal) op).getLabel();
        
        if(opLabel.contains("+")) {
          this.insertStatements.add((Resource) s, (URI) p, o, (Resource) g);
        } else {
          this.deleteStatements.add((Resource) s, (URI) p, o, (Resource) g);
        }
      } else {
        throw new TupleQueryResultHandlerException("The delta operation is not properly specified.");
      }
    } else {
      throw new TupleQueryResultHandlerException("The delta statement is not properly specified.");
    }
  }

}
