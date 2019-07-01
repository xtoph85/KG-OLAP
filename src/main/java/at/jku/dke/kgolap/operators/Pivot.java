package at.jku.dke.kgolap.operators;
import java.util.HashMap;
import java.util.Map;

import at.jku.dke.kgolap.repo.Repo;

public class Pivot extends Statement {
  private String context = null; // optional -- set either context or granularity level
  
  private Map<String,String> granularityLevel = new HashMap<String, String>();
  
  private String dimensionProperty = null;
  
  private String selectionProperty = null;
  private String selectionResource = null;
  
  private String pivotProperty = null;

  public Pivot(Repo repository, String prefixes) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
  }

  public Pivot(Repo repository, String prefixes, String context) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
    this.context = context;
  }
  
  public void setContext(String context) {
    this.context = context;
  }
  
  public void addGranularityLevel(String property, String level) {
    this.granularityLevel.put(property, level);
  }
  
  public void setDimensionProperty(String property) {
    this.dimensionProperty = property;
  }
  
  public void setPivotProperty(String property) {
    this.pivotProperty = property;
  }
  
  public void setSelectionCondition(String selectionProperty, String selectionResource) {
    this.selectionProperty = selectionProperty;
    this.selectionResource = selectionResource;
  }
  
  @Override
  public String prepareStatement() {
    StringBuilder sparql = new StringBuilder();
    
    StringBuilder granularityLevelBindings = new StringBuilder();
    
    {
      int counter = 1;
      
      for(String property : granularityLevel.keySet()) {
        granularityLevelBindings.append("            ?ctx " + property + " ?x" + counter + " .\n");
        granularityLevelBindings.append("            ?x" + counter + " olap:atLevel " + granularityLevel.get(property) + " .\n");
        
        counter++;
      }
    }

    sparql.append(
        this.getPrefixes()
      + "\n"
      + "SELECT ?g ?s ?p ?o ?op WHERE {\n"
      + "  {\n"
      + "    SELECT ?m ?r ?x WHERE {\n"
      + "      {\n"
      + "        SELECT ?ctx ?m ?x WHERE {\n"
      + "          GRAPH ckr:global {\n"
      + "            ?ctx ckr:hasAssertedModule ?m .\n"
      + "            ?ctx " + this.dimensionProperty + " ?x .\n"
    );

    if(!granularityLevel.isEmpty()) {
      sparql.append(
          granularityLevelBindings
        + "          }\n"
      );
    }

    if(context != null) {
      sparql.append(
          "      FILTER(?ctx = " + context + ")\n"
      );
    }
    
    sparql.append(
        "        }\n"
      + "      }\n"
    );
    
    sparql.append(
        "      {\n"
      + "        SELECT ?ctx ?m ?r WHERE {\n"
      + "          GRAPH <ckr:global-inf> {\n"
      + "            ?inf ckr:closureOf ?ctx .\n"
      + "          }\n"
      + "          GRAPH ?inf {\n"
      + "            ?inf ckr:derivedFrom ?d .\n"
      + "          }\n"
    );
    
    if(this.selectionProperty != null && this.selectionResource != null) {
      sparql.append(
          "          GRAPH ?d {\n"
        + "            ?r " + this.selectionProperty + " " + this.selectionResource + " .\n"
        + "          }\n"
      );
    }
    
    sparql.append(
        "          {\n"
      + "            GRAPH ?m {\n"
      + "              ?r ?p ?o .\n"
      + "            }\n"
      + "          } UNION {\n"
      + "            GRAPH ?m {\n"
      + "              ?o ?p ?r .\n"
      + "            }\n"
      + "          }\n"
      + "        }\n"
      + "      }\n"
      + "    }\n"
      + "  }\n"
      + "  BIND(?m AS ?g)\n"
      + "  BIND(?r AS ?s)\n"
      + "  BIND(" + this.pivotProperty + " AS ?p)\n"
      + "  BIND(?x AS ?o)\n"
      + "  BIND(\"+\" AS ?op)\n"
      + "}\n"
    );
    
    return sparql.toString();
  }
}
