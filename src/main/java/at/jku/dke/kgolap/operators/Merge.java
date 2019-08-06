package at.jku.dke.kgolap.operators;

import java.util.HashMap;
import java.util.Map;

import at.jku.dke.kgolap.repo.Repo;

public class Merge extends Statement {  
  private Method method = Method.UNION;
  private Map<String,String> granularity = new HashMap<String, String>();
  
  public enum Method {
    UNION, INTERSECT
  }
  
  public Merge(Repo repository, String prefixes) {
    this.setSourceRepository(repository);
    this.setTargetRepository(repository);
    this.setPrefixes(prefixes);
  }
  
  public void setMethod(Method method) {
    this.method = method;
  }
  
  public Method getMethod() {
    return method;
  }
  
  public void setGranularity(String dimension, String level) {        
    this.granularity.put(dimension, level);
  }
  
  public String getGranularity(String dimension) {
    return granularity.get(dimension);
  }
  
  @Override
  public String prepareStatement() {
    StringBuilder sparql = new StringBuilder();
    
    sparql.append(
           this.getPrefixes()
        + "\n"
        + "SELECT DISTINCT ?s ?p ?o ?g ?op WHERE {\n"
        +      prepareGenerateContexts()
        +      prepareMergeUnionModules()
        +      prepareCleanNullContexts()
        + "}\n"
        + "\n"
    );
    
    return sparql.toString();
  }
  
  private String prepareGenerateContexts() {
    StringBuilder generateContext1 = new StringBuilder();
    StringBuilder generateContext2 = new StringBuilder();
    StringBuilder generateContext3 = new StringBuilder();
    StringBuilder generateContext4 = new StringBuilder();
    StringBuilder generateContextHasChild1 = new StringBuilder();
    StringBuilder generateContextHasChild2 = new StringBuilder();
    StringBuilder variablesForDimensions = new StringBuilder();
    StringBuilder calculateCoverage1 = new StringBuilder();
    StringBuilder calculateCoverage2 = new StringBuilder();
    StringBuilder calculateCoverage3 = new StringBuilder();
    StringBuilder calculateCoverage4 = new StringBuilder();
    StringBuilder calculateNull1 = new StringBuilder();
    StringBuilder calculateNull2 = new StringBuilder();
    StringBuilder calculateNull3 = new StringBuilder();
    
    {
      int counter = 1;
      
      for(String dimension : granularity.keySet()) {
        
        if(counter > 1) {
          generateContext4.append(", ");
        }
        
        generateContext1.append(
            "      } UNION {\n"
          + "        BIND(ckr:global AS ?g)\n"
          + "        BIND(?ctx AS ?s)\n"
          + "        BIND(" + dimension + " AS ?p)\n"
          + "        BIND(?d" + counter + " AS ?o)\n"
        );
        
        generateContext2.append(
            "            ?d" + counter + " olap:atLevel " + granularity.get(dimension) + " .\n"
        );
        
        generateContext3.append(
            "              ?ctx1 " + dimension + " ?d" + counter + " .\n"
        );
        
        variablesForDimensions.append("?d" + counter + " ");
        
        generateContext4.append(
            "'-', IF(STRAFTER(STR(?d" + counter + "), '#') != '', STRAFTER(STR(?d" + counter + "), '#'), STRAFTER(STR(?d" + counter + "), 'urn:uuid:'))"
        );
        
        generateContextHasChild1.append(
            "              ?covered " + dimension + " ?y" + counter + " .\n"
        );
        
        generateContextHasChild2.append(
            "              ?y" + counter + " olap:rollsUpTo ?d" + counter + " .\n"
        );
        
        calculateCoverage1.append(
            "            ?ctx " + dimension + " ?x" + counter + " .\n" +
            "            ?d" + counter + " olap:atLevel " + granularity.get(dimension) + " .\n"
        );
        
        calculateCoverage2.append(
            "              ?ctx1 " + dimension + " ?d" + counter + " .\n"
        );
        
        calculateCoverage3.append(
            "          ?d" + counter + " olap:rollsUpTo ?x" + counter + " .\n"
        );
        
        calculateCoverage4.append(
            "          ?x" + counter + " olap:rollsUpTo ?d" + counter + " .\n"
        );
        
        calculateNull1.append(
            "          ?s " + dimension + "/olap:atLevel ?l" + counter + " .\n"
        );
        
        calculateNull2.append(
            "          ?l" + counter + " olap:rollsUpTo " + granularity.get(dimension) + " .\n"
        );
        
        calculateNull3.append(
            "          ?s " + dimension + "/olap:atLevel " + granularity.get(dimension) + " .\n"
        );
        
        counter++;
      }
    }    
    
    StringBuilder sparql = new StringBuilder();
    
    sparql.append(
          "  {\n"
               // Select and, if necessary, generate the contexts and knowledge modules
               // at the argument granularity level.
        + "    SELECT ?g ?s ?p ?o ?op WHERE {\n"
        + "      {\n"
        + "        BIND(ckr:global AS ?g)\n"
        + "        BIND(?ctx AS ?s)\n"
        + "        BIND(rdf:type AS ?p)\n"
        + "        BIND(olap:Cell AS ?o)\n"
        + "      } UNION {\n"
        + "        BIND(ckr:global AS ?g)\n"
        + "        BIND(?ctx AS ?s)\n"
        + "        BIND(ckr:hasAssertedModule AS ?p)\n"
        + "        BIND(?mod AS ?o)\n"
        +        generateContext1
        + "      } UNION {\n"
        + "        BIND(<ckr:global-inf> AS ?g)\n"
        + "        BIND(?ctx AS ?s)\n"
        + "        BIND(ckr:hasModule AS ?p)\n"
        + "        BIND(?mod AS ?o)\n"
        + "      } UNION {\n"
        + "        BIND(<ckr:global-inf> AS ?g)\n"
        + "        BIND(?ctx AS ?s)\n"
        + "        BIND(ckr:hasModule AS ?p)\n"
        + "        BIND(?inf AS ?o)\n"
        + "      } UNION {\n"
        + "        BIND(<ckr:global-inf> AS ?g)\n"
        + "        BIND(?inf AS ?s)\n"
        + "        BIND(ckr:closureOf AS ?p)\n"
        + "        BIND(?ctx AS ?o)\n"
        + "      } UNION {\n"
        + "        BIND(?inf AS ?g)\n"
        + "        BIND(?inf AS ?s)\n"
        + "        BIND(ckr:derivedFrom AS ?p)\n"
        + "        BIND(ckr:global AS ?o)\n"
        + "      } UNION {\n"
        + "        BIND(?inf AS ?g)\n"
        + "        BIND(?inf AS ?s)\n"
        + "        BIND(ckr:derivedFrom AS ?p)\n"
        + "        BIND(<ckr:global-inf> AS ?o)\n"
        + "      } UNION {\n"
        + "        BIND(?inf AS ?g)\n"
        + "        BIND(?inf AS ?s)\n"
        + "        BIND(ckr:derivedFrom AS ?p)\n"
        + "        BIND(?mod AS ?o)\n"
        + "      }\n"
        + "      {\n"
        + "        SELECT ?ctx ?mod ?inf " + variablesForDimensions + "WHERE {\n"
        + "          GRAPH ckr:global {\n"
        +              generateContext2
        + "          }\n"
                     // We only want to add a context if there's anything to merge at all.
                     // For coordinates where no covered context exists, we do not add any context.
        + "          FILTER EXISTS {\n"
        + "            GRAPH ckr:global {\n"
        +                generateContextHasChild1
        + "            }\n"
        + "            GRAPH <ckr:global-inf> {\n"
        +                generateContextHasChild2
        + "            }\n"
        + "          }\n"
                     // We must allow for the possibility that a context at the roll-up level
                     // already exists. In that case we keep the context but we might have
                     // to generate a module (and inference module).
        + "          OPTIONAL {\n"
        + "            GRAPH ckr:global {\n"
        + "              ?ctx1 ckr:hasAssertedModule ?mod1 .\n"
        +                generateContext3
        + "            }\n"
        + "            OPTIONAL {\n"
        + "              GRAPH <ckr:global-inf> {\n"
        + "                ?ctx1 ckr:hasModule ?inf1 .\n"
        + "                ?inf1 ckr:closureOf ?ctx1 .\n"
        + "              }\n"
        + "            }\n"
        + "          }\n"
        + "          BIND(IF(!BOUND(?ctx1), IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext4 + ")), ?ctx1) AS ?ctx)\n"
        + "          BIND(IF(!BOUND(?mod1), IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext4 + ", '-mod')), ?mod1) AS ?mod)\n"
        + "          BIND(IF(!BOUND(?inf1), IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext4 + ", '-inf')), ?inf1) AS ?inf)\n"
        + "        }\n"
        + "      }\n"
        + "      BIND(\"+\" AS ?op)\n"
        + "    }\n"
        + "  } UNION {\n"
               // Calculate coverage relationships of the contexts.
        + "    SELECT ?g ?s ?p ?o ?op WHERE {\n"
        + "      {\n"
        + "        SELECT ?ctx " + variablesForDimensions + variablesForDimensions.toString().replaceAll("d", "x") + "WHERE {\n"
        + "          GRAPH ckr:global {\n"
                       // ?ctx DIMENSIONn ?xn
                       // ?dn  olap:atLevel LEVELn
        +              calculateCoverage1
        + "          }\n"
        + "          FILTER NOT EXISTS {\n"
        + "            GRAPH ckr:global {\n"
                         // ?ctx1 DIMENSIONn ?dn
        +                calculateCoverage2
        + "            }\n"
        + "          }\n"
        + "          FILTER EXISTS {\n"
        + "            GRAPH ckr:global {\n"
        +                generateContextHasChild1
        + "            }\n"
        + "            GRAPH <ckr:global-inf> {\n"
        +                generateContextHasChild2
        + "            }\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "      {\n"
        + "        GRAPH <ckr:global-inf> {\n"
                     // ?dn  olap:rollsUpTo ?xn
        +            calculateCoverage3
        + "        }\n"
        + "        \n"
        + "        BIND(<ckr:global-inf> AS ?g)\n"
        + "        BIND(?ctx AS ?s)\n"
        + "        BIND(olap:covers AS ?p)\n"
        + "        BIND(IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext4 + ")) AS ?o)\n"
        + "      } UNION {\n"
        + "        GRAPH <ckr:global-inf> {\n"
                     // ?xn  olap:rollsUpTo ?dn
        +            calculateCoverage4
        + "        }\n"
        + "        \n"
        + "        BIND(<ckr:global-inf> AS ?g)\n"
        + "        BIND(IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext4 + ")) AS ?s)\n"
        + "        BIND(olap:covers AS ?p)\n"
        + "        BIND(?ctx AS ?o)\n"
        + "      } UNION {\n"
        + "        GRAPH <ckr:global-inf> {\n"
                     // ?dn  olap:rollsUpTo ?xn
        +            calculateCoverage3
        + "        }\n"
        + "        \n"
        + "        GRAPH ckr:global {\n"
        + "          ?ctx ckr:hasAssertedModule ?mod .\n"
        + "        }\n"
        + "        \n"
        + "        BIND(<ckr:global-inf> AS ?g)\n"
        + "        BIND(IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext4 + ", '-inf')) AS ?s)\n"
        + "        BIND(ckr:derivedFrom AS ?p)\n"
        + "        BIND(?mod AS ?o)\n"
        + "      }\n"
        + "      BIND(\"+\" AS ?op)\n"
        + "    }\n"
        + "  } UNION {\n"
               // Set the merged contexts null
        + "    SELECT ?g ?s ?p ?o ?op WHERE {\n"
        + "      {\n"
        + "        GRAPH ckr:global {\n"
        +            calculateNull1
        + "        }\n"
        + "        GRAPH <ckr:global-inf> {\n"
        +            calculateNull2
        + "        }\n"
        + "      } MINUS {\n"
        + "        GRAPH ckr:global {\n"
        +            calculateNull3
        + "        }\n"
        + "      }\n"
        + "      BIND(ckr:global AS ?g)\n"
        + "      BIND(rdf:type AS ?p)\n"
        + "      BIND(ckr:Null AS ?o)\n"
        + "      BIND(\"+\" AS ?op)\n"
        + "    }\n"
        + "  } UNION {\n"
    );
    
    return sparql.toString();
  }
  
  private String prepareMergeUnionModules() {
    StringBuilder generateContext = new StringBuilder();
    StringBuilder generateContextHasChild1 = new StringBuilder();
    StringBuilder generateContextHasChild2 = new StringBuilder();
    StringBuilder variablesForDimensions = new StringBuilder();
    StringBuilder mergeCondition1 = new StringBuilder();
    StringBuilder mergeCondition2 = new StringBuilder();
    StringBuilder calculateCoverage1 = new StringBuilder();
    StringBuilder calculateCoverage2 = new StringBuilder();
    StringBuilder calculateCoverage3 = new StringBuilder();
    
    {
      int counter = 1;
      
      for(String dimension : granularity.keySet()) {
        
        if(counter > 1) {
          generateContext.append(", ");
        }
        
        variablesForDimensions.append("?d" + counter + " ");
        
        generateContext.append(
            "'-', IF(STRAFTER(STR(?d" + counter + "), '#') != '', STRAFTER(STR(?d" + counter + "), '#'), STRAFTER(STR(?d" + counter + "), 'urn:uuid:'))"
        );
        
        generateContextHasChild1.append(
            "                    ?covered " + dimension + " ?y" + counter + " .\n"
        );
        
        generateContextHasChild2.append(
            "                    ?y" + counter + " olap:rollsUpTo ?d" + counter + " .\n"
        );
        
        mergeCondition1.append(
            "                ?ctx " + dimension + " ?d" + counter + " .\n"
        );
        
        mergeCondition2.append(
            "                ?d" + counter + " olap:atLevel " + granularity.get(dimension) + " .\n"
        );        

        calculateCoverage1.append(
            "                    ?d" + counter + " olap:atLevel " + granularity.get(dimension) + " .\n"
        );
        
        calculateCoverage2.append(
            "                      ?ctx " + dimension + " ?d" + counter + " .\n"
        );
        
        calculateCoverage3.append(
            "                  ?x" + counter + " olap:rollsUpTo ?d" + counter + " .\n"
        );
        
        
        counter++;
      }
    }
    
    String mergeCondition =
        "          {\n"
      + "            SELECT ?ctx WHERE {\n"
      + "              GRAPH ckr:global {\n"
      +                  mergeCondition1
      + "              }\n"
      + "              GRAPH ckr:global {\n"
      +                  mergeCondition2
      + "              }\n"
      + "            }\n"
      + "          }\n";
    
    StringBuilder sparql = new StringBuilder();
    
    sparql.append(
           // merge the (asserted and inferred) knowledge modules
          "    SELECT DISTINCT ?s ?p ?o ?g ?op WHERE {\n"
        + "      GRAPH ?m {\n"
        + "        ?s ?p ?o .\n"
        + "      }\n"
        + "      FILTER(?p != ckr:derivedFrom)\n"
        + "      {\n"
        + "        SELECT ?g ?m WHERE {\n"
        + "          {\n"
        + "            SELECT ?ctx ?g ?m WHERE {\n"
        + "              GRAPH <ckr:global-inf> {\n"
        + "                ?ctx olap:covers? ?ctx1 .\n"
        + "              }\n"
        + "              GRAPH ckr:global {\n"
        + "                ?ctx1 ckr:hasAssertedModule ?m .\n"
        + "              }\n"
                         // We must allow for the possibility that a context at the roll-up level
                         // already exists. In that case we keep the context but we might have
                         // to generate a module (and inference module).
        + "              OPTIONAL {\n"
        + "                GRAPH ckr:global {\n"
        + "                  ?ctx ckr:hasAssertedModule ?mod .\n"
        + "                }\n"
        + "              }\n"
        + "              BIND(IF(!BOUND(?mod), IRI(CONCAT(STR(?ctx), '-mod')), ?mod) AS ?g)\n"
        + "            }\n"
        + "          } UNION {\n"
        + "            SELECT ?ctx ?g ?m WHERE {\n"
        + "              GRAPH <ckr:global-inf> {\n"
        + "                ?ctx olap:covers? ?ctx1 .\n"
        + "              }\n"
        + "              MINUS {\n"
        + "                GRAPH ckr:global {\n"
        + "                  ?ctx1 ckr:hasAssertedModule ?m .\n"
        + "                }\n"
        + "              }\n"
                         // We must allow for the possibility that a context at the roll-up level
                         // already exists. In that case we keep the context but we might have
                         // to generate a module (and inference module).
        + "              OPTIONAL {\n"
        + "                GRAPH <ckr:global-inf> {\n"
        + "                  ?ctx ckr:hasModule ?inf .\n"
        + "                }\n"
        + "                MINUS {\n"
        + "                  GRAPH ckr:global {\n"
        + "                    ?ctx ckr:hasAssertedModule ?inf .\n"
        + "                  }\n"
        + "                }\n"
        + "              }\n"
        + "              BIND(IF(!BOUND(?inf), IRI(CONCAT(STR(?ctx), '-inf')), ?inf) AS ?g)\n"
        + "            }\n"
        + "          }\n"
        +            mergeCondition
        + "        }\n"
        + "      } UNION {\n"
        + "        SELECT ?g ?m WHERE {\n"
        + "          {\n"
        + "            SELECT ?ctx ?g ?m WHERE {\n"
        + "              {\n"
        + "                SELECT ?m " + variablesForDimensions + "WHERE {\n"
        + "                  GRAPH ckr:global {\n"
                               // ?dn  olap:atLevel LEVELn
        +                      calculateCoverage1
        + "                  }\n"
        + "                  FILTER NOT EXISTS {\n"
        + "                    GRAPH ckr:global {\n"
                                 // ?ctx DIMENSIONn ?dn
        +                        calculateCoverage2
        + "                    }\n"
        + "                  }\n"
        + "                  GRAPH ckr:global {\n"
        + "                    ?covered ckr:hasAssertedModule ?m .\n"
        +                      generateContextHasChild1
        + "                  }\n"
        + "                  GRAPH <ckr:global-inf> {\n"
        +                      generateContextHasChild2
        + "                  }\n"
        + "                }\n"
        + "              }\n"
        + "              \n"
        + "              BIND(IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext + ")) AS ?ctx)\n"
        + "              BIND(IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext + ", '-mod')) AS ?g)\n"
        + "            }\n"
        + "          } UNION {\n"
        + "            SELECT ?ctx ?g ?m WHERE {\n"
        + "              {\n"
        + "                SELECT ?m " + variablesForDimensions + "WHERE {\n"
        + "                  GRAPH ckr:global {\n"
                               // ?dn  olap:atLevel LEVELn
        +                      calculateCoverage1
        + "                  }\n"
        + "                  FILTER NOT EXISTS {\n"
        + "                    GRAPH ckr:global {\n"
                                 // ?ctx DIMENSIONn ?dn
        +                        calculateCoverage2
        + "                    }\n"
        + "                  }\n"
        + "                  GRAPH ckr:global {\n"
        +                      generateContextHasChild1
        + "                  }\n"
        + "                  GRAPH <ckr:global-inf> {\n"
        +                      generateContextHasChild2
        + "                    ?covered ckr:hasModule ?m .\n"
        + "                  }\n"
        + "                  MINUS {\n"
        + "                    GRAPH ckr:global {\n"
        + "                      ?covered ckr:hasAssertedModule ?m .\n"
        + "                    }\n"
        + "                  }\n"
        + "                }\n"
        + "              }\n"
        + "              \n"
        + "              BIND(IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext + ")) AS ?ctx)\n"
        + "              BIND(IRI(CONCAT(STR(cube:), 'Ctx', " + generateContext + ", '-inf')) AS ?g)\n"
        + "            }\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "      BIND(\"+\" AS ?op)\n"
        + "    }\n"
        + "  } UNION {\n"
    );
    
    return sparql.toString();
  }

  private String prepareCleanNullContexts() {
    StringBuilder sparql = new StringBuilder();
    StringBuilder calculateNull1 = new StringBuilder();
    StringBuilder calculateNull2 = new StringBuilder();
    StringBuilder calculateNull3 = new StringBuilder();
    
    {
      int counter = 1;
      
      for(String dimension : granularity.keySet()) {
        calculateNull1.append(
            "          ?ctx " + dimension + "/olap:atLevel ?l" + counter + " .\n"
        );
        
        calculateNull2.append(
            "          ?l" + counter + " olap:rollsUpTo " + granularity.get(dimension) + " .\n"
        );
        
        calculateNull3.append(
            "          ?ctx " + dimension + "/olap:atLevel " + granularity.get(dimension) + " .\n"
        );
        
        counter++;
      }
    }    
    
    sparql.append(
        "    SELECT ?g ?s ?p ?o ?op WHERE {\n"
      + "      {\n"
      + "        GRAPH ckr:global {\n"
      +            calculateNull1
      + "        }\n"
      + "        GRAPH <ckr:global-inf> {\n"
      +            calculateNull2
      + "        }\n"
      + "      } MINUS {\n"
      + "        GRAPH ckr:global {\n"
      +            calculateNull3
      + "        }\n"
      + "      }\n"
      + "      {"
      + "        GRAPH ckr:global {\n"
      + "          ?ctx ckr:hasAssertedModule ?g .\n"
      + "        }\n"
      + "      } UNION {\n"
      + "        GRAPH <ckr:global-inf> {\n"
      + "          ?ctx ckr:hasModule ?g .\n"
      + "        }\n"
      + "      }\n"
      + "      GRAPH ?g {\n"
      + "        ?s ?p ?o"
      + "      }\n"
      + "      BIND(\"-\" AS ?op)\n"
      + "    }\n"
      + "  } UNION {\n"
      + "    SELECT ?g ?s ?p ?o ?op WHERE {\n"
      + "      {\n"
      + "        GRAPH ckr:global {\n"
      +            calculateNull1
      + "        }\n"
      + "        GRAPH <ckr:global-inf> {\n"
      +            calculateNull2
      + "        }\n"
      + "      } MINUS {\n"
      + "        GRAPH ckr:global {\n"
      +            calculateNull3
      + "        }\n"
      + "      }\n"
      + "      {\n"
      + "        GRAPH ckr:global {\n"
      + "          ?ctx ckr:hasAssertedModule ?m .\n"
      + "        }\n"
      + "      } UNION {\n"
      + "        GRAPH <ckr:global-inf> {\n"
      + "          ?ctx ckr:hasModule ?m .\n"
      + "        }\n"
      + "      }\n"
      + "      {\n"
      + "        GRAPH ?g {\n"
      + "          ?m ?p ?o\n"
      + "        }\n"
      + "        BIND(?m AS ?s)\n"
      + "      } UNION {\n"
      + "        GRAPH ?g {\n"
      + "          ?s ?p ?m\n"
      + "        }\n"
      + "        BIND(?m AS ?o)\n"
      + "      }\n"
      + "      BIND(\"-\" AS ?op)\n"
      + "    }\n"
      + "  }\n"
    );
    
    return sparql.toString();
  }
}
