package at.jku.dke.kgolap.operators;

import java.util.HashMap;
import java.util.Map;

import at.jku.dke.kgolap.repo.Repo;

public class AggregatePropertyValues extends Statement {  
  private String context = null;
  private Map<String, String> granularity = null;
  
  private String aggregatedProperty = null;
  private AggregateFunction aggregateFunction = AggregateFunction.SUM;
  
  private Map<String, String> selectionMap = new HashMap<String, String>();
  
  public enum AggregateFunction {
    SUM, MIN, MAX, AVG, COUNT
  }
  
  public AggregatePropertyValues(Repo repository, String prefixes, String context) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
    this.context = context;
  }
  
  public AggregatePropertyValues(Repo repository, String prefixes, Map<String, String> granularityLevel) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
    this.granularity = granularityLevel;
  }

  public Map<String, String> getGranularity() {
    return this.granularity;
  }
  
  public void setGranularity(Map<String, String> granularityLevel) {
    this.granularity = granularityLevel;
  }
  
  
  public void setAggregatedProperty(String property) {
    this.aggregatedProperty = property;
  }
  
  public void setAggregateFunction(AggregateFunction function) {
    this.aggregateFunction = function;
  }
  
  public void addSelectionCondition(String selectionProperty, String resource) {
    this.selectionMap.put(selectionProperty, resource);
  }
  
  public String getGroupedResourceClass() {
    return this.selectionMap.get("rdf:type");
  }
  
  public void setGroupedResourceClass(String resourceClass) {
    this.selectionMap.put("rdf:type", resourceClass);
  }
  
  @Override
  public String prepareStatement() {
    StringBuilder sparql = new StringBuilder();
    
    String aggregationFunction = null;
    
    switch(this.aggregateFunction) {
      case SUM:
        aggregationFunction = "SUM";
        break;
      case MIN:
        aggregationFunction = "MIN";
        break;
      case MAX:
        aggregationFunction = "MAX";
        break;
      case AVG:
        aggregationFunction = "AVG";
        break;
      default:
        aggregationFunction = "SUM";
    }
    
    if(this.context != null) {
      StringBuilder selectionConditions = new StringBuilder();
      
      for(String property : this.selectionMap.keySet()) {
        selectionConditions.append(
            "          {\n"
          + "            SELECT ?s WHERE {\n"
          + "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf ckr:closureOf " + this.context + " .\n"
          + "              }\n"
          + "              GRAPH ?inf {\n"
          + "                ?inf ckr:derivedFrom ?m .\n"
          + "              }\n"
          + "              GRAPH ?m {\n"
          + "                ?s " + property + " " + this.selectionMap.get(property) + " .\n"
          + "              }\n"
          + "            }\n"
          + "          }\n"
        );
      }
      
      sparql.append(
          this.getPrefixes()
        + "\n"
        + "SELECT DISTINCT ?g ?s ?p ?o ?op WHERE {\n"
        + "  {\n"
        + "    SELECT ?m ?s ?d ?e ?op WHERE {\n"
        + "      {\n"
        + "        SELECT ?m ?s ?d ?e WHERE {\n"
        + "          GRAPH ckr:global {\n"
        + "            " + this.context + " ckr:hasAssertedModule ?m .\n"
        + "          }\n"
        + "          GRAPH ?m {\n"
        + "            ?s " + this.aggregatedProperty + " ?d .\n"
        + "          }\n"
        + "          {\n"
        + "            SELECT ?s (" + aggregationFunction + "(?d) AS ?e) WHERE {\n"
        + "              GRAPH ckr:global {\n"
        + "                " + this.context + " ckr:hasAssertedModule ?m .\n"
        + "              }\n"
        + "              GRAPH ?m {\n"
        + "                ?s " + this.aggregatedProperty + " ?d .\n"
        + "              }\n"
        + "            } GROUP BY ?s\n"
        + "          }\n"
        +            selectionConditions
        + "        }\n"
        + "      }\n"
        + "      VALUES ?op { \"-\" \"+\" }\n"
        + "    }\n"
        + "  }\n"
        + "  BIND(?m AS ?g)\n"
        + "  BIND(" + this.aggregatedProperty + " AS ?p)\n"
        + "  BIND(IF(?op = \"-\", ?d, ?e) AS ?o)\n"
        + "}\n"
      );
    }  else if(this.context == null && granularity != null) {
      StringBuilder contextSelection = new StringBuilder();
      
      for(String dimension : granularity.keySet()) {
        contextSelection.append(
          "      ?ctx " + dimension + "/olap:atLevel " + granularity.get(dimension) + " .\n"
        );
      }
      
      StringBuilder selectionConditions = new StringBuilder();
      
      for(String property : this.selectionMap.keySet()) {
        selectionConditions.append(
            "          {\n"
          + "            SELECT ?s ?ctx WHERE {\n"
          + "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf ckr:closureOf ?ctx .\n"
          + "              }\n"
          + "              GRAPH ?inf {\n"
          + "                ?inf ckr:derivedFrom ?m .\n"
          + "              }\n"
          + "              GRAPH ?m {\n"
          + "                ?s " + property + " " + this.selectionMap.get(property) + " .\n"
          + "              }\n"
          + "            }\n"
          + "          }\n"
        );
      }
      
      sparql.append(
          this.getPrefixes()
        + "\n"
        + "SELECT DISTINCT ?g ?s ?p ?o ?op WHERE {\n"
        + "  {\n"
        + "    SELECT ?m ?s ?d ?e ?op ?ctx WHERE {\n"
        + "      {\n"
        + "        SELECT ?m ?s ?d ?e ?ctx WHERE {\n"
        + "          GRAPH <ckr:global-inf> {\n"
        + "            ?inf ckr:closureOf ?ctx .\n"
        + "          }\n"
        + "          GRAPH ?inf {\n"
        + "            ?inf ckr:derivedFrom ?m .\n"
        + "          }\n"
        + "          GRAPH ?m {\n"
        + "            ?s " + this.aggregatedProperty + " ?d .\n"
        + "          }\n"
        + "          {\n"
        + "            SELECT ?s (" + aggregationFunction + "(?d) AS ?e) ?ctx WHERE {\n"
        + "              GRAPH <ckr:global-inf> {\n"
        + "                ?inf ckr:closureOf ?ctx .\n"
        + "              }\n"
        + "              GRAPH ?inf1 {\n"
        + "                ?inf ckr:derivedFrom ?m .\n"
        + "              }\n"
        + "              GRAPH ?m {\n"
        + "                ?s " + this.aggregatedProperty + " ?d .\n"
        + "              }\n"
        + "            } GROUP BY ?s ?ctx\n"
        + "          }\n"
        +            selectionConditions
        + "        }\n"
        + "      }\n"
        + "      VALUES ?op { \"-\" \"+\" }\n"
        + "    }\n"
        + "  }\n"
        + "  GRAPH ckr:global {\n"
        + "    ?ctx ckr:hasAssertedModule ?n .\n"
        + "  }\n"
        + "  BIND(IF(?op = \"-\", ?m, ?n) AS ?g)\n"
        + "  BIND(" + this.aggregatedProperty + " AS ?p)\n"
        + "  BIND(IF(?op = \"-\", ?d, ?e) AS ?o)\n"
        + "  {\n"
        + "    SELECT ?ctx WHERE {\n"
        +        contextSelection
        + "    }\n"
        + "  }\n"
        + "}\n"
      );
    }
    
    return sparql.toString();
  }

}
