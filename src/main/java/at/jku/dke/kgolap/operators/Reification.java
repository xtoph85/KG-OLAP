package at.jku.dke.kgolap.operators;

import java.util.HashMap;
import java.util.Map;

import at.jku.dke.kgolap.repo.Repo;

public class Reification extends Statement {
  private String context = null; // optional -- set either context or granularity level

  private Map<String, String> granularityLevel = new HashMap<String, String>();

  private String reificationPredicate = null;

  private String subjectSelectionProperty = null;
  private String subjectSelectionResource = null;

  public Reification(Repo repository, String prefixes) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
  }

  public Reification(Repo repository, String prefixes, String context) {
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

  public void setReificationPredicate(String property) {
    this.reificationPredicate = property;
  }

  public void setSubjectSelectionCondition(String sourceSelectionProperty, String sourceSelectionResource) {
    this.subjectSelectionProperty = sourceSelectionProperty;
    this.subjectSelectionResource = sourceSelectionResource;
  }

  @Override
  public String prepareStatement() {
    StringBuilder sparql = new StringBuilder();

    StringBuilder granularityLevelBindings = new StringBuilder();

    {
      int counter = 1;

      for (String property : granularityLevel.keySet()) {
        granularityLevelBindings.append("          ?ctx " + property + " ?x" + counter + " .\n");
        granularityLevelBindings
            .append("          ?x" + counter + " olap:atLevel " + granularityLevel.get(property) + " .\n");

        counter++;
      }
    }

    sparql.append(
          this.getPrefixes() 
        + "\n" 
        + "SELECT ?g ?s ?p ?o ?op WHERE {\n" 
        + "  {\n"
        + "    {\n" 
        + "      BIND(?m AS ?g)\n"
        + "      BIND(?x AS ?s)\n" 
        + "      BIND(rdf:subject AS ?p)\n" 
        + "      BIND(?subj AS ?o)\n"
        + "      BIND(\"+\" AS ?op)\n" 
        + "    } UNION {\n" 
        + "      BIND(?m AS ?g)\n"
        + "      BIND(?x AS ?s)\n"
        + "      BIND(rdf:type AS ?p)\n" 
        + "      BIND(IRI(CONCAT(STR(?pred), '-type')) AS ?o)\n" 
        + "      BIND(\"+\" AS ?op)\n"
        + "    } UNION {\n" 
        + "      BIND(?m AS ?g)\n" 
        + "      BIND(?x AS ?s)\n" 
        + "      BIND(rdf:object AS ?p)\n"
        + "      BIND(?obj AS ?o)\n" 
        + "      BIND(\"+\" AS ?op)\n" 
        + "    }\n" 
        + "  }\n" 
        + "  {\n"
        + "    SELECT ?m ?x ?subj ?pred ?obj WHERE {\n" 
        + "      {\n"
        + "        SELECT ?ctx ?m WHERE {\n"
        + "          ?ctx ckr:hasAssertedModule ?m .\n");

    if (!granularityLevel.isEmpty()) {
      sparql.append(granularityLevelBindings);
    }

    if (context != null) {
      sparql.append("          FILTER(?ctx = " + context + ")\n");
    }

    sparql.append(
        "        }\n" 
      + "      }\n");

    sparql.append(
          "      {\n" 
        + "        SELECT ?ctx ?m ?subj ?pred ?obj WHERE {\n"
        + "          GRAPH ckr:global {\n"
        + "            ?ctx ckr:hasAssertedModule ?m .\n"
        + "          }\n"
    );
    
    if(this.subjectSelectionProperty != null && this.subjectSelectionResource != null) {
      sparql.append(
            "          GRAPH <ckr:global-inf> {\n"
          + "            ?inf ckr:closureOf ?ctx .\n"
          + "          }\n"
          + "          GRAPH ?inf {\n"
          + "            ?inf ckr:derivedFrom ?d .\n"
          + "          }\n" 
          + "          GRAPH ?d {\n"
          + "            ?subj " + this.subjectSelectionProperty + " " + this.subjectSelectionResource + " .\n"
          + "          }\n" 
      );
    }

    sparql.append(
          "          GRAPH ?m {\n" 
        + "            ?subj ?pred ?obj .\n"
    );

    if (this.reificationPredicate != null) {
      sparql.append("            FILTER(?pred = " + this.reificationPredicate + ")\n");
    } else {
      sparql.append("            FILTER(?pred != rdf:type)\n");
    }

    sparql.append(
          "          }\n"
        + "        }\n" 
        + "      }\n"
        + "      BIND(UUID() AS ?x)\n" 
        + "    }\n" 
        + "  }\n" 
        + "}\n");

    return sparql.toString();
  }
}
