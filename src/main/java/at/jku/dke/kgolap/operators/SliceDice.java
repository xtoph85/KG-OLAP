package at.jku.dke.kgolap.operators;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.repo.HTTPConnectable;
import at.jku.dke.kgolap.repo.Repo;

public class SliceDice extends Statement { 
  private static final Logger logger = LoggerFactory.getLogger(SliceDice.class);
  
  private List<Map<String,String>> diceCoordinateVectors = new LinkedList<Map<String, String>>();
  
  public SliceDice(Repo baseRepository, Repo tempRepository, String prefixes) {
    this.setSourceRepository(baseRepository);
    this.setTargetRepository(tempRepository);
    this.setPrefixes(prefixes);
  }
  
  public void addDiceCoordinates(Map<String,String> vector) {        
    this.diceCoordinateVectors.add(vector);
  }
  
  @Override
  public String prepareUpdateStatement() {
    String sparql = super.prepareUpdateStatement();
    
    if(this.getSourceRepository() instanceof HTTPConnectable) {
      sparql = sparql.replaceFirst("WHERE \\{\n  \\{\n", "WHERE {\n  SERVICE <" + ((HTTPConnectable)this.getSourceRepository()).getRepositoryURL() + "> {\n");
    } else {
      logger.warn("The source repository does not support HTTP connections, which is required in order for the update query to return any results.");
    }
    
    return sparql;
  }
  
  @Override
  public String prepareStatement() {
    StringBuilder sparql = new StringBuilder();

    sparql.append(
          this.getPrefixes()
        + "\n"
    );
    
    {
      int i = 0;
      for(Map<String,String> diceCoordinates : diceCoordinateVectors) {
        StringBuilder diceCondition1 = new StringBuilder();
        StringBuilder diceCondition2 = new StringBuilder();
        StringBuilder valuesDiceLevel = new StringBuilder();
        
        {
          int counter = 1;
          
          for(String dimension : diceCoordinates.keySet()) {
            if (counter > 1) {
              valuesDiceLevel.append(" ");
            }
            
            diceCondition1.append(
                "        ?ctx " + dimension + " ?d" + counter + " .\n"
            );
            
            diceCondition2.append(
                "        {\n"
              + "          ?d" + counter + " olap:rollsUpTo " + diceCoordinates.get(dimension) + " .\n"
              + "        } UNION {\n"
              + "          " + diceCoordinates.get(dimension) + " olap:rollsUpTo ?d" + counter + " .\n"
              + "        }\n"
            );
            
            valuesDiceLevel.append(diceCoordinates.get(dimension));
            
            counter++;
          }
        }
        
        
        StringBuilder contextInformation = new StringBuilder();
        
        contextInformation.append(
              ""
                 // Retrieve the triples from ckr:global and <ckr:global-inf> that concern
                 // the selected contexts.
            + "  {\n"
            + "    SELECT DISTINCT ?g ?s ?p ?o WHERE {\n"
            + "      GRAPH ?g {\n"
            + "        ?ctx ?p ?o .\n"
            + "        FILTER(?p != olap:covers)\n" // olap:covers is taken care of from the other side.
                                                    // if not filtered the coverage relationships with disregarded
                                                    // contexts would also be included.
            + "      } VALUES ?g {ckr:global <ckr:global-inf>}\n"
            + "      BIND(?ctx AS ?s)\n"
            + "      GRAPH ckr:global {\n"
            +          diceCondition1
            + "      }\n"
            + "      GRAPH <ckr:global-inf> {\n"
            +          diceCondition2
            + "      }\n"
            + "    }\n"
            + "  } UNION {\n"
            + "    SELECT DISTINCT ?g ?s ?p ?o WHERE {\n"
            + "      GRAPH ?g {\n"
            + "        ?s ?p ?ctx .\n"
            + "    } VALUES ?g {ckr:global <ckr:global-inf>}\n"
            + "      BIND(?ctx AS ?o)\n"
            + "      GRAPH ckr:global {\n"
            +          diceCondition1
            + "      }\n"
            + "      GRAPH <ckr:global-inf> {\n"
            +          diceCondition2
            + "      }\n"
            + "    }\n"
            + "  }"
        );
        
        
        StringBuilder moduleInformation = new StringBuilder();
        
        moduleInformation.append(
              " UNION {\n"
                   // Retrieve the data about the modules associated with\n"
                   // the selected contexts.\n"
            + "    SELECT DISTINCT ?g ?s ?p ?o WHERE {\n"
            + "      {\n"
            + "        GRAPH ?g {\n"
            + "          ?m ?p ?o .\n"
            + "        } VALUES ?g {ckr:global <ckr:global-inf>}\n"
            + "        BIND(?m AS ?s)\n"
            + "      } UNION {\n"
            + "        GRAPH ?g {\n"
            + "          ?s ?p ?m .\n"
            + "        } VALUES ?g {ckr:global <ckr:global-inf>}\n"
            + "        BIND(?m AS ?o)\n"
            + "      }\n"
            + "      GRAPH ckr:global {\n"
            +          diceCondition1
            + "      }\n"
            + "      GRAPH <ckr:global-inf> {\n"
            +          diceCondition2
            + "      }\n"
            + "      # Take either asserted or derived modules.\n"
            + "      GRAPH <ckr:global-inf> {\n"
            + "        ?ctx ckr:hasModule ?m .\n"
            + "      }\n"
            + "    }\n"
            + "  }"
        );
        
        
        StringBuilder modules = new StringBuilder();
        
        modules.append(
              " UNION {\n"
                   // Retrieve the data from the modules associated with
                   // the selected contexts.
            + "    SELECT DISTINCT ?g ?s ?p ?o WHERE {\n"
            + "      GRAPH ?g {\n"
            + "        ?s ?p ?o .\n"
            + "      }\n"
            + "      GRAPH ckr:global {\n"
            +          diceCondition1
            + "      }\n"
            + "      GRAPH <ckr:global-inf> {\n"
            +          diceCondition2
            + "      }\n"
                     // Take either asserted or derived modules.
            + "      GRAPH <ckr:global-inf> {\n"
            + "        ?ctx ckr:hasModule ?g .\n"
            + "      }\n"
            + "    }\n"
            + "  }"
        );
        
        
        StringBuilder dimensionalModel = new StringBuilder();      
        
        dimensionalModel.append(
              " UNION {\n"
                   // Retrieve the dimensional model for the selected dice coordinate.
            + "    SELECT DISTINCT ?g ?s ?p ?o WHERE {\n"
            + "      {\n"
            + "        GRAPH ?g {\n"
            + "          ?d ?p ?o .\n"
            + "        } VALUES ?g {ckr:global <ckr:global-inf>}\n"
            + "        BIND(?d AS ?s)\n"
            + "      } UNION {\n"
            + "        GRAPH ?g {\n"
            + "          ?l ?p ?o .\n"
            + "        } VALUES ?g {ckr:global <ckr:global-inf>}\n"
            + "        BIND(?l AS ?s)\n"
            + "      }\n"
            + "      {\n"
            + "        GRAPH <ckr:global-inf> {\n"
            + "          {\n"
            + "            ?d olap:rollsUpTo ?r .\n"
            + "          } UNION {\n"
            + "            ?r olap:rollsUpTo ?d .\n"
            + "          }\n"
            + "        }\n"
            + "        GRAPH ckr:global {\n"
            + "          ?d olap:atLevel ?l . \n"
            + "        }\n"
            + "      } VALUES ?r {" + valuesDiceLevel + "}\n"
            + "    }\n"
            + "  }"
        );
        
        StringBuilder metaVocabulary = new StringBuilder();
        
        metaVocabulary.append(
              " UNION {\n" // TODO: Revise the meta-vocabulary, but good for now.
                   // Select the meta-vocabulary from ckr:global.
            + "    SELECT ?g ?s ?p ?o WHERE {\n"
            + "      GRAPH ckr:global {\n"
            + "        ?s ?p ?o .\n"
            + "      }\n"
            + "      VALUES ?p {\n"
            + "        owl:allValuesFrom\n"
            + "        owl:onProperty\n"
            + "        owl:disjointWith\n"
            + "        owl:hasSelf\n"
            + "        owl:propertyChainAxiom\n"
            + "        rdf:first\n"
            + "        rdf:rest\n"
            + "        rdfs:subClassOf\n"
            + "        rdfs:subPropertyOf\n"
            + "        rdfs:range\n"
            + "        rdfs:domain\n"
            + "        rdfs:comment\n"
            + "      }\n"
            + "      BIND(ckr:global AS ?g)\n"
            + "    }\n"
            + "  } UNION {\n"
            + "    SELECT ?g ?s ?p ?o WHERE {\n"
            + "      GRAPH ckr:global {\n"
            + "        ?s ?p ?o .\n"
            + "      }\n"
            + "      VALUES ?o {\n"
            + "        owl:Ontology\n"
            + "        owl:Class\n"
            + "        owl:ObjectProperty\n"
            + "        owl:FunctionalProperty\n"
            + "      }\n"
            + "      BIND(ckr:global AS ?g)\n"
            + "    }\n"
            + "  } UNION {\n"
            + "    SELECT ?g ?s ?p ?o WHERE {\n"
            + "      BIND(<ckr:global-inf> AS ?g)\n"
            + "      BIND(<ckr:global-inf> AS ?s)\n"
            + "      BIND(ckr:derivedFrom AS ?p)\n"
            + "      BIND(<ckr:global-inf> AS ?o)\n"
            + "    }\n"
            + "  } UNION {\n"
            + "    SELECT ?g ?s ?p ?o WHERE {\n"
            + "      BIND(<ckr:global-inf> AS ?g)\n"
            + "      BIND(<ckr:global-inf> AS ?s)\n"
            + "      BIND(ckr:derivedFrom AS ?p)\n"
            + "      BIND(ckr:global AS ?o)\n"
            + "    }\n"
            + "  }\n"
        );
        
        if(diceCoordinateVectors.size() == 1) {
          sparql.append(
                "\n"
              + "SELECT DISTINCT ?s ?p ?o ?g ?op WHERE {\n"
              +    contextInformation
              +    moduleInformation
              +    modules
              +    dimensionalModel
              +    metaVocabulary
              + "  BIND(\"+\" AS ?op)\n"
              + "}\n"
              + "\n"
          );
        } else {
          if(i == 0) {
            sparql.append(
                  "\n"
                + "SELECT DISTINCT ?s ?p ?o ?g ?op WHERE {\n"
                + "  {\n"
                + "    SELECT DISTINCT ?s ?p ?o ?g ?op WHERE {\n"
                +        contextInformation.toString().replaceAll("\n", "\n  ")
                +        moduleInformation.toString().replaceAll("\n", "\n  ")
                +        modules.toString().replaceAll("\n", "\n  ")
                +        dimensionalModel.toString().replaceAll("\n", "\n  ")
                +        metaVocabulary.toString().replaceAll("\n", "\n  ")
                + "      BIND(\"+\" AS ?op)\n"
                + "    }\n"
                + "  }"
           );
          } else {
            sparql.append(
                "\n"
                + "UNION {\n"
                + "    SELECT DISTINCT ?s ?p ?o ?g ?op WHERE {\n"
                +        contextInformation.toString().replaceAll("\n", "\n  ")
                +        moduleInformation.toString().replaceAll("\n", "\n  ")
                +        modules.toString().replaceAll("\n", "\n  ")
                +        dimensionalModel.toString().replaceAll("\n", "\n  ")
                +        metaVocabulary.toString().replaceAll("\n", "\n  ")
                + "      BIND(\"+\" AS ?op)\n"
                + "    }\n"
                + "  }"
            );
          }
        }
        
        i++;
      }
      
      if(diceCoordinateVectors.size() > 1) {
        sparql.append(
            "\n"
          + "}\n"
        );
      }
    }
    
    return sparql.toString();
  }
  
}
