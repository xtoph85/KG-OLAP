package at.jku.dke.kgolap.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.jku.dke.kgolap.repo.Repo;

public class ReplaceByGrouping extends Statement {  
  private String context = null;
  private Map<String, String> granularity = null;
  
  private String groupingProperty = null;
  private String groupingResource = null;
  private String groupingResourceClass = null;
  
  private List<String> groupedProperties = new ArrayList<String>();
  
  public ReplaceByGrouping(Repo repository, String prefixes, String context) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
    this.context = context;
  }
  
  public ReplaceByGrouping(Repo repository, String prefixes, Map<String, String> granularityLevel) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
    this.granularity = granularityLevel;
  }
  
  public void addGroupedProperty(String property) {
    this.groupedProperties.add(property);
  }
  
  
  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }
  
  public Map<String, String> getGranularity() {
    return this.granularity;
  }
  
  public void setGranularity(Map<String, String> granularityLevel) {
    this.granularity = granularityLevel;
  }

  public String getGroupingProperty() {
    return groupingProperty;
  }
  
  public void setGroupingProperty(String groupingProperty) {
    this.groupingProperty = groupingProperty;
  }

  public String getGroupingResource() {
    return groupingResource;
  }
  
  public void setGroupingResource(String groupingResource) {
    this.groupingResource = groupingResource;
  }

  public String getGroupingResourceClass() {
    return groupingResourceClass;
  }
  
  public void setGroupingResourceClass(String groupingResourceClass) {
    this.groupingResourceClass = groupingResourceClass;
  }
  
  @Override
  public String prepareStatement() {
    StringBuilder sparql = new StringBuilder();
    
    if(this.context != null) {

      StringBuilder groupedPropertyFilters = new StringBuilder();
      
      for(String property : groupedProperties) {
        groupedPropertyFilters.append("            FILTER(?c = " + property + ")\n");
      }
      
      sparql.append(
            this.getPrefixes()
          + "\n"
          + "SELECT DISTINCT ?g ?s ?p ?o ?op WHERE {\n"
          + "  {\n"
          + "    SELECT ?m ?a ?b ?c ?d ?e ?op WHERE {\n"
          + "      {\n"
          + "        SELECT ?m ?a ?x ?c ?d ?y WHERE {\n"
          + "          GRAPH ckr:global {\n"
          + "            " + this.context + " ckr:hasAssertedModule ?m .\n"
          + "          }\n"
          + "          GRAPH ?m {\n"
          + "            ?a ?c ?d .\n"
          + "            FILTER(?c != rdf:type)\n"
          + (            this.groupingResource != null 
          ? "            FILTER(?c != " + this.groupingProperty + " || ?d != " + this.groupingResource + ")\n"
          : "            FILTER(?c != " + this.groupingProperty + ")\n")
          +              groupedPropertyFilters
          + "          }\n"
          + "          OPTIONAL {\n"
          + "            SELECT ?a ?x WHERE {\n"
          + "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf1 ckr:closureOf " + this.context + " .\n"
          + "              }\n"
          + "              GRAPH ?inf1 {\n"
          + "                ?inf1 ckr:derivedFrom ?m1 .\n"
          + "              }\n"
          + "              GRAPH ?m1 {\n"
          + "                ?a " + this.groupingProperty + " ?x .\n"
          + (                this.groupingResource != null
          ? "                FILTER(?x = " + this.groupingResource + ")\n" : "")
          + "              }\n"
          + (              this.groupingResourceClass != null
          ? "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf3 ckr:closureOf " + this.context + " .\n"
          + "              }\n"
          + "              GRAPH ?inf3 {\n"
          + "                ?inf3 ckr:derivedFrom ?m3 .\n"
          + "              }\n"
          + "              GRAPH ?m3 {\n"
          + "                ?a rdf:type " + this.groupingResourceClass  + " .\n"
          + "              }\n" : "")
          + "            }\n"
          + "          }\n"
          + "          OPTIONAL {\n"
          + "            SELECT ?d ?y WHERE {\n"
          + "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf2 ckr:closureOf " + this.context + " .\n"
          + "              }\n"
          + "              GRAPH ?inf2 {\n"
          + "                ?inf2 ckr:derivedFrom ?m2 .\n"
          + "              }\n"
          + "              GRAPH ?m2 {\n"
          + "                ?d " + this.groupingProperty + " ?y .\n"
          + (                this.groupingResource != null
          ? "                FILTER(?y = " + this.groupingResource + ")\n" : "")
          + "              }\n"
          + (              this.groupingResourceClass != null
          ? "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf4 ckr:closureOf " + this.context + " .\n"
          + "              }\n"
          + "              GRAPH ?inf4 {\n"
          + "                ?inf4 ckr:derivedFrom ?m4 .\n"
          + "              }\n"
          + "              GRAPH ?m4 {\n"
          + "                ?d rdf:type " + this.groupingResourceClass  + " .\n"
          + "              }\n" : "")
          + "            }\n"
          + "          }\n"
          + "        }\n"
          + "      }\n"
          + "      FILTER(BOUND(?x) || BOUND(?y))\n"
          + "      BIND(IF(!BOUND(?x), ?a, ?x) AS ?b)\n"
          + "      BIND(IF(!BOUND(?y), ?d, ?y) AS ?e)\n"
          + "      VALUES ?op { \"-\" \"+\" }\n"
          + "    }\n"
          + "  }\n"
          + "  BIND(?m AS ?g)\n"
          + "  BIND(IF(?op = \"-\", ?a, ?b) AS ?s)\n"
          + "  BIND(?c AS ?p)\n"
          + "  BIND(IF(?op = \"-\", ?d, ?e) AS ?o)\n"
          + "}\n"
      );
    } else if(this.context == null && granularity != null) {
      StringBuilder contextSelection = new StringBuilder();
      
      for(String dimension : granularity.keySet()) {
        contextSelection.append(
            "            ?ctx " + dimension + "/olap:atLevel " + granularity.get(dimension) + " .\n"
        );
      }

      StringBuilder groupedPropertyFilters = new StringBuilder();
      
      for(String property : groupedProperties) {
        groupedPropertyFilters.append("            FILTER(?c = " + property + ")\n");
      }

      sparql.append(
            this.getPrefixes()
          + "\n"
          + "SELECT DISTINCT ?g ?s ?p ?o ?op WHERE {\n"
          + "  {\n"
          + "    SELECT ?m ?a ?b ?c ?d ?e ?op ?ctx WHERE {\n"
          + "      {\n"
          + "        SELECT ?m ?a ?x ?c ?d ?y ?ctx WHERE {\n"
          + "          GRAPH ckr:global {\n"
          + "            ?ctx ckr:hasAssertedModule ?m .\n"
          + "          }\n"
          + "          GRAPH ckr:global {\n"
          +              contextSelection
          + "          }\n"
          + "          GRAPH ?m {\n"
          + "            ?a ?c ?d .\n"
          + "            FILTER(?c != rdf:type)\n"
          + (            this.groupingResource != null 
          ? "            FILTER(?c != " + this.groupingProperty + " || ?d != " + this.groupingResource + ")\n"
          : "            FILTER(?c != " + this.groupingProperty + ")\n")
          +              groupedPropertyFilters
          + "          }\n"
          + "          OPTIONAL {\n"
          + "            SELECT ?a ?x ?ctx WHERE {\n"
          + "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf1 ckr:closureOf ?ctx .\n"
          + "              }\n"
          + "              GRAPH ?inf1 {\n"
          + "                ?inf1 ckr:derivedFrom ?m1 .\n"
          + "              }\n"
          + "              GRAPH ?m1 {\n"
          + "                ?a " + this.groupingProperty + " ?x .\n"
          + (                this.groupingResource != null
          ? "                FILTER(?x = " + this.groupingResource + ")\n" : "")
          + "              }\n"
          + (              this.groupingResourceClass != null
          ? "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf3 ckr:closureOf ?ctx .\n"
          + "              }\n"
          + "              GRAPH ?inf3 {\n"
          + "                ?inf3 ckr:derivedFrom ?m3 .\n"
          + "              }\n"
          + "              GRAPH ?m3 {\n"
          + "                ?a rdf:type " + this.groupingResourceClass  + " .\n"
          + "              }\n" : "")
          + "            }\n"
          + "          }\n"
          + "          OPTIONAL {\n"
          + "            SELECT ?d ?y ?ctx WHERE {\n"
          + "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf2 ckr:closureOf ?ctx .\n"
          + "              }\n"
          + "              GRAPH ?inf2 {\n"
          + "                ?inf2 ckr:derivedFrom ?m2 .\n"
          + "              }\n"
          + "              GRAPH ?m2 {\n"
          + "                ?d " + this.groupingProperty + " ?y .\n"
          + (                this.groupingResource != null
          ? "                FILTER(?x = " + this.groupingResource + ")\n" : "")
          + "              }\n"
          + (              this.groupingResourceClass != null
          ? "              GRAPH <ckr:global-inf> {\n"
          + "                ?inf4 ckr:closureOf ?ctx .\n"
          + "              }\n"
          + "              GRAPH ?inf4 {\n"
          + "                ?inf4 ckr:derivedFrom ?m4 .\n"
          + "              }\n"
          + "              GRAPH ?m4 {\n"
          + "                ?d rdf:type " + this.groupingResourceClass  + " .\n"
          + "              }\n" : "")
          + "            }\n"
          + "          }\n"
          + "        }\n"
          + "      }\n"
          + "      FILTER(BOUND(?x) || BOUND(?y))\n"
          + "      BIND(IF(!BOUND(?x), ?a, ?x) AS ?b)\n"
          + "      BIND(IF(!BOUND(?y), ?d, ?y) AS ?e)\n"
          + "      VALUES ?op { \"-\" \"+\" }\n"
          + "    }\n"
          + "  }\n"
          + "  BIND(?m AS ?g)\n"
          + "  BIND(IF(?op = \"-\", ?a, ?b) AS ?s)\n"
          + "  BIND(?c AS ?p)\n"
          + "  BIND(IF(?op = \"-\", ?d, ?e) AS ?o)\n"
          + "}\n"
      );
    }
        
    return sparql.toString();
  }
}
