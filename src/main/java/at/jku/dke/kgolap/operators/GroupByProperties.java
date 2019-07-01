package at.jku.dke.kgolap.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.jku.dke.kgolap.repo.Repo;

public class GroupByProperties extends Statement {
  private String context = null;
  private Map<String, String> granularity = null;
  
  private List<String> groupingProperties = new ArrayList<String>();
  private List<String> groupedProperties = new ArrayList<String>();
  private String generatedGrouping = null;
  private String groupedResourceClass = null;
  
  public GroupByProperties(Repo repository, String prefixes, String context) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
    this.context = context;
  }
  
  public GroupByProperties(Repo repository, String prefixes, Map<String, String> granularityLevel) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
    this.granularity = granularityLevel;
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
  
  public void setGeneratedGrouping(String grouping) {
    this.generatedGrouping = grouping;
  }

  public void addGroupingProperty(String property) {
    this.groupingProperties.add(property);
  }
  
  public void setGroupedResourceClass(String resourceClass) {
    this.groupedResourceClass = resourceClass;
  }
  
  public String getGroupedResourceClass() {
    return this.groupedResourceClass;
  }
  
  @Override
  public String prepareStatement() {
    StringBuilder sparql = new StringBuilder();
    
    if(this.context != null) {
      StringBuilder groupingPropertyBindings_x = new StringBuilder();
      StringBuilder groupingPropertyBindings_y = new StringBuilder();
      StringBuilder groupingPropertyHash_x = new StringBuilder();
      StringBuilder groupingPropertyHash_y = new StringBuilder();
  
      StringBuilder generatedGroupingBindings = new StringBuilder();
      StringBuilder generatedGroupingHash = new StringBuilder();
      
      StringBuilder groupedPropertyFilters = new StringBuilder();
  
      {
        int counter = 1;
        
        for(String property : groupingProperties) {
          if(counter > 1) {
            groupingPropertyHash_x.append(", ");
            groupingPropertyHash_y.append(", ");
            generatedGroupingHash.append(", ");
          }
  
          groupingPropertyHash_x.append(
            "STR(?x" + counter + ")"
          );
  
          groupingPropertyHash_y.append(
            "STR(?y" + counter + ")"
          );
          
          generatedGroupingHash.append(
            "STR(?gr" + counter + ")"
          );
          
          groupingPropertyBindings_x.append(
              "                  {\n"
            + "                    SELECT ?a ?x" + counter + " WHERE {\n"
            + "                      GRAPH <ckr:global-inf> {\n"
            + "                        ?inf1 ckr:closureOf " + this.context + " .\n"
            + "                      }\n"
            + "                      GRAPH ?inf1 {\n"
            + "                        ?inf1 ckr:derivedFrom ?m1 .\n"
            + "                      }\n"
            + "                      GRAPH ?m1 {\n"
            + "                        ?a " + property + " ?x" + counter + " .\n"
            + "                      }\n"
            + "                    }\n"
            + "                  }\n"
          );
          
          groupingPropertyBindings_y.append(
              "                  {\n"
            + "                    SELECT ?d ?y" + counter + " WHERE {\n"
            + "                      GRAPH <ckr:global-inf> {\n"
            + "                        ?inf3 ckr:closureOf " + this.context + " .\n"
            + "                      }\n"
            + "                      GRAPH ?inf3 {\n"
            + "                        ?inf3 ckr:derivedFrom ?m3 .\n"
            + "                      }\n"
            + "                      GRAPH ?m3 {\n"
            + "                        ?d " + property + " ?y" + counter + " .\n"
            + "                      }\n"
            + "                    }\n"
            + "                  }\n"
          );
          
          generatedGroupingBindings.append(
              "          {\n"
            + "            SELECT ?r ?gr" + counter + " WHERE {\n"
            + "              GRAPH <ckr:global-inf> {\n"
            + "                ?inf1 ckr:closureOf " + this.context + " .\n"
            + "              }\n"
            + "              GRAPH ?inf1 {\n"
            + "                ?inf1 ckr:derivedFrom ?m1 .\n"
            + "              }\n"
            + "              GRAPH ?m1 {\n"
            + "                ?r " + property + " ?gr" + counter + " .\n"
            + "              }\n"
            + "            }\n"
            + "          }\n"
          );
          
          counter++;
        }
      }
      
      for(String property : groupedProperties) {
        groupedPropertyFilters.append("                FILTER(?c = " + property + ")\n");
      }
      
      sparql.append(
            this.getPrefixes()
          + "SELECT DISTINCT ?g ?s ?p ?o ?op WHERE {\n"
      );
      
      sparql.append(
            "  {\n"  
          + "    SELECT ?g ?s ?p ?o ?op WHERE {\n"
          + "      {\n"
          + "        SELECT ?m ?r ?gr WHERE {\n"
          + "          GRAPH ckr:global {\n"
          + "            " + this.context + " ckr:hasAssertedModule ?m .\n"
          + "          }\n"
          +            generatedGroupingBindings
          + (          this.groupedResourceClass != null
          ? "          GRAPH <ckr:global-inf> {\n"
          + "            ?inf2 ckr:closureOf " + this.context + " .\n"
          + "          }\n"
          + "          GRAPH ?inf2 {\n"
          + "            ?inf2 ckr:derivedFrom ?m2 .\n"
          + "          }\n"
          + "          GRAPH ?m2 {\n"
          + "            ?r rdf:type " + this.groupedResourceClass  + " .\n"
          + "          }\n" : "")
          + "          BIND(IRI(CONCAT(STR(obj:), SHA512(CONCAT(" + generatedGroupingHash + ")))) AS ?gr)\n"
          + "        }\n"
          + "      }\n"
          + "      BIND(?m AS ?g)\n"
          + "      BIND(?r AS ?s)\n"
          + "      BIND(" + this.generatedGrouping + " AS ?p)\n"
          + "      BIND(?gr AS ?o)\n"
          + "      BIND(\"+\" AS ?op)\n"
          + "    }\n"
          + "  } UNION {\n"
          + "    SELECT ?g ?s ?p ?o ?op WHERE {\n"
          + "      {\n"
          + "        SELECT ?m ?a ?b ?c ?d ?e ?op WHERE {\n"
          + "          {\n"
          + "            SELECT ?m ?a ?x ?c ?d ?y WHERE {\n"
          + "              GRAPH ckr:global {\n"
          + "                " + this.context + " ckr:hasAssertedModule ?m .\n"
          + "              }\n"
          + "              GRAPH ?m {\n"
          + "                ?a ?c ?d .\n"
          + "                FILTER(?c != rdf:type)\n"
          + "                FILTER(?c != " + this.generatedGrouping + ")\n"
          +                  groupedPropertyFilters
          + "              }\n"
          + "              OPTIONAL {\n"
          + "                SELECT ?a ?x WHERE {\n"
          +                    groupingPropertyBindings_x
          + (                  this.groupedResourceClass != null
          ? "                  GRAPH <ckr:global-inf> {\n"
          + "                    ?inf2 ckr:closureOf " + this.context + " .\n"
          + "                  }\n"
          + "                  GRAPH ?inf2 {\n"
          + "                    ?inf2 ckr:derivedFrom ?m2 .\n"
          + "                  }\n"
          + "                  GRAPH ?m2 {\n"
          + "                    ?a rdf:type " + this.groupedResourceClass  + " .\n"
          + "                  }\n" : "")
          + "                  BIND(IRI(CONCAT(STR(obj:), SHA512(CONCAT(" + groupingPropertyHash_x + ")))) AS ?x)\n"
          + "                }\n"
          + "              }\n"
          + "              OPTIONAL {\n"
          + "                SELECT ?d ?y WHERE {\n"
          +                    groupingPropertyBindings_y
          + (                  this.groupedResourceClass != null
          ? "                  GRAPH <ckr:global-inf> {\n"
          + "                    ?inf4 ckr:closureOf " + this.context + " .\n"
          + "                  }\n"
          + "                  GRAPH ?inf4 {\n"
          + "                    ?inf4 ckr:derivedFrom ?m4 .\n"
          + "                  }\n"
          + "                  GRAPH ?m4 {\n"
          + "                    ?d rdf:type " + this.groupedResourceClass  + " .\n"
          + "                  }\n" : "")
          + "                  BIND(IRI(CONCAT(STR(obj:), SHA512(CONCAT(" + groupingPropertyHash_y + ")))) AS ?y)\n"
          + "                }\n"
          + "              }\n"
          + "            }\n"
          + "          }\n"
          + "          FILTER(BOUND(?x) || BOUND(?y))\n"
          + "          BIND(IF(!BOUND(?x), ?a, ?x) AS ?b)\n"
          + "          BIND(IF(!BOUND(?y), ?d, ?y) AS ?e)\n"
          + "          VALUES ?op { \"-\" \"+\" }\n"
          + "        }\n"
          + "      }\n"
          + "      BIND(?m AS ?g)\n"
          + "      BIND(IF(?op = \"-\", ?a, ?b) AS ?s)\n"
          + "      BIND(?c AS ?p)\n"
          + "      BIND(IF(?op = \"-\", ?d, ?e) AS ?o)\n"
          + "    }\n"
          + "  }\n"
          + "}\n"
      );
    } else if(this.context == null && granularity != null) {
      StringBuilder contextSelection1 = new StringBuilder();
      StringBuilder contextSelection2 = new StringBuilder();
      
      for(String dimension : granularity.keySet()) {
        contextSelection1.append(
            "            ?ctx " + dimension + "/olap:atLevel " + granularity.get(dimension) + " .\n"
          );
        contextSelection2.append(
            "                ?ctx " + dimension + "/olap:atLevel " + granularity.get(dimension) + " .\n"
          );
      }
      
      StringBuilder groupingPropertyBindings_x = new StringBuilder();
      StringBuilder groupingPropertyBindings_y = new StringBuilder();
      StringBuilder groupingPropertyHash_x = new StringBuilder();
      StringBuilder groupingPropertyHash_y = new StringBuilder();
  
      StringBuilder generatedGroupingBindings = new StringBuilder();
      StringBuilder generatedGroupingHash = new StringBuilder();
      
      StringBuilder groupedPropertyFilters = new StringBuilder();
  
      {
        int counter = 1;
        
        for(String property : groupingProperties) {
          if(counter > 1) {
            groupingPropertyHash_x.append(", ");
            groupingPropertyHash_y.append(", ");
            generatedGroupingHash.append(", ");
          }
  
          groupingPropertyHash_x.append(
            "STR(?x" + counter + ")"
          );
  
          groupingPropertyHash_y.append(
            "STR(?y" + counter + ")"
          );
          
          generatedGroupingHash.append(
            "STR(?gr" + counter + ")"
          );
          
          groupingPropertyBindings_x.append(
              "                  {\n"
            + "                    SELECT ?a ?x" + counter + " ?ctx WHERE {\n"
            + "                      GRAPH <ckr:global-inf> {\n"
            + "                        ?inf1 ckr:closureOf ?ctx .\n"
            + "                      }\n"
            + "                      GRAPH ?inf1 {\n"
            + "                        ?inf1 ckr:derivedFrom ?m1 .\n"
            + "                      }\n"
            + "                      GRAPH ?m1 {\n"
            + "                        ?a " + property + " ?x" + counter + " .\n"
            + "                      }\n"
            + "                    }\n"
            + "                  }\n"
          );
          
          groupingPropertyBindings_y.append(
              "                  {\n"
            + "                    SELECT ?d ?y" + counter + " ?ctx WHERE {\n"
            + "                      GRAPH <ckr:global-inf> {\n"
            + "                        ?inf3 ckr:closureOf ?ctx .\n"
            + "                      }\n"
            + "                      GRAPH ?inf3 {\n"
            + "                        ?inf3 ckr:derivedFrom ?m3 .\n"
            + "                      }\n"
            + "                      GRAPH ?m3 {\n"
            + "                        ?d " + property + " ?y" + counter + " .\n"
            + "                      }\n"
            + "                    }\n"
            + "                  }\n"
          );
          
          generatedGroupingBindings.append(
              "          {\n"
            + "            SELECT ?r ?gr" + counter + " ?ctx WHERE {\n"
            + "              GRAPH <ckr:global-inf> {\n"
            + "                ?inf1 ckr:closureOf ?ctx .\n"
            + "              }\n"
            + "              GRAPH ?inf1 {\n"
            + "                ?inf1 ckr:derivedFrom ?m1 .\n"
            + "              }\n"
            + "              GRAPH ?m1 {\n"
            + "                ?r " + property + " ?gr" + counter + " .\n"
            + "              }\n"
            + "            }\n"
            + "          }\n"
          );
          
          counter++;
        }
      }
      
  
      for(String property : groupedProperties) {
        groupedPropertyFilters.append("                FILTER(?c = " + property + ")\n");
      }
      
      sparql.append(
            this.getPrefixes()
          + "SELECT DISTINCT ?g ?s ?p ?o ?op WHERE {\n"
      );
      
      sparql.append(
            "  {\n"  
          + "    SELECT ?g ?s ?p ?o ?op ?ctx WHERE {\n"
          + "      {\n"
          + "        SELECT ?m ?r ?gr ?ctx WHERE {\n"
          + "          GRAPH ckr:global {\n"
          + "            ?ctx ckr:hasAssertedModule ?m .\n"
          + "          }\n"
          + "          GRAPH ckr:global {\n"
          +              contextSelection1
          + "          }\n"
          +            generatedGroupingBindings
          + (          this.groupedResourceClass != null
          ? "          GRAPH <ckr:global-inf> {\n"
          + "            ?inf2 ckr:closureOf ?ctx .\n"
          + "          }\n"
          + "          GRAPH ?inf2 {\n"
          + "            ?inf2 ckr:derivedFrom ?m2 .\n"
          + "          }\n"
          + "          GRAPH ?m2 {\n"
          + "            ?r rdf:type " + this.groupedResourceClass  + " .\n"
          + "          }\n" : "")
          + "          BIND(IRI(CONCAT(STR(obj:), SHA512(CONCAT(" + generatedGroupingHash + ")))) AS ?gr)\n"
          + "        }\n"
          + "      }\n"
          + "      BIND(?m AS ?g)\n"
          + "      BIND(?r AS ?s)\n"
          + "      BIND(" + this.generatedGrouping + " AS ?p)\n"
          + "      BIND(?gr AS ?o)\n"
          + "      BIND(\"+\" AS ?op)\n"
          + "    }\n"
          + "  } UNION {\n"
          + "    SELECT ?g ?s ?p ?o ?op ?ctx WHERE {\n"
          + "      {\n"
          + "        SELECT ?m ?a ?b ?c ?d ?e ?op ?ctx WHERE {\n"
          + "          {\n"
          + "            SELECT ?m ?a ?x ?c ?d ?y ?ctx WHERE {\n"
          + "              GRAPH ckr:global {\n"
          + "                ?ctx ckr:hasAssertedModule ?m .\n"
          + "              }\n"
          + "              GRAPH ckr:global {\n"
          +                  contextSelection2
          + "              }\n"
          + "              GRAPH ?m {\n"
          + "                ?a ?c ?d .\n"
          + "                FILTER(?c != rdf:type)\n"
          + "                FILTER(?c != " + this.generatedGrouping + ")\n"
          +                  groupedPropertyFilters
          + "              }\n"
          + "              OPTIONAL {\n"
          + "                SELECT ?a ?x ?ctx WHERE {\n"
          +                    groupingPropertyBindings_x
          + (                  this.groupedResourceClass != null
          ? "                  GRAPH <ckr:global-inf> {\n"
          + "                    ?inf2 ckr:closureOf ?ctx .\n"
          + "                  }\n"
          + "                  GRAPH ?inf2 {\n"
          + "                    ?inf2 ckr:derivedFrom ?m2 .\n"
          + "                  }\n"
          + "                  GRAPH ?m2 {\n"
          + "                    ?a rdf:type " + this.groupedResourceClass  + " .\n"
          + "                  }\n" : "")
          + "                  BIND(IRI(CONCAT(STR(obj:), SHA512(CONCAT(" + groupingPropertyHash_x + ")))) AS ?x)\n"
          + "                }\n"
          + "              }\n"
          + "              OPTIONAL {\n"
          + "                SELECT ?d ?y ?ctx  WHERE {\n"
          +                    groupingPropertyBindings_y
          + (                  this.groupedResourceClass != null
          ? "                  GRAPH <ckr:global-inf> {\n"
          + "                    ?inf4 ckr:closureOf ?ctx .\n"
          + "                  }\n"
          + "                  GRAPH ?inf4 {\n"
          + "                    ?inf4 ckr:derivedFrom ?m4 .\n"
          + "                  }\n"
          + "                  GRAPH ?m4 {\n"
          + "                    ?d rdf:type " + this.groupedResourceClass  + " .\n"
          + "                  }\n" : "")
          + "                  BIND(IRI(CONCAT(STR(obj:), SHA512(CONCAT(" + groupingPropertyHash_y + ")))) AS ?y)\n"
          + "                }\n"
          + "              }\n"
          + "            }\n"
          + "          }\n"
          + "          FILTER(BOUND(?x) || BOUND(?y))\n"
          + "          BIND(IF(!BOUND(?x), ?a, ?x) AS ?b)\n"
          + "          BIND(IF(!BOUND(?y), ?d, ?y) AS ?e)\n"
          + "          VALUES ?op { \"-\" \"+\" }\n"
          + "        }\n"
          + "      }\n"
          + "      BIND(?m AS ?g)\n"
          + "      BIND(IF(?op = \"-\", ?a, ?b) AS ?s)\n"
          + "      BIND(?c AS ?p)\n"
          + "      BIND(IF(?op = \"-\", ?d, ?e) AS ?o)\n"
          + "    }\n"
          + "  }\n"
          + "}\n"
      );
    }
    
    return sparql.toString();
  }
}
