package at.jku.dke.kgolap.test.junit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.DefaultKGOLAPCubeFactory;
import at.jku.dke.kgolap.KGOLAPCube;
import at.jku.dke.kgolap.KGOLAPCubeProperties;
import at.jku.dke.kgolap.repo.Repo;

public abstract class OperatorTest {
  private static final Logger logger = LoggerFactory.getLogger(OperatorTest.class);
  
  private KGOLAPCube cube = null;
  
  public OperatorTest() {
    KGOLAPCubeProperties properties = this.getProperties();
    
    this.cube = new DefaultKGOLAPCubeFactory().createKGOLAPCube(properties);
    
    this.getKGOLAPCube().startUp();
  }
  
  public abstract KGOLAPCubeProperties getProperties();
  public abstract void loadTestdata();

  public KGOLAPCube getKGOLAPCube() {
    return cube;
  }
  
  @Before
  public void setUp() throws Exception {
    this.getKGOLAPCube().startUp();
    
    this.getKGOLAPCube().reset();
    
    logger.info("Loading the testdata ...");
    this.loadTestdata();
    
    logger.info("Testdata loaded.");
  }


  @After
  public void tearDown() throws Exception {
//    this.getKGOLAPCube().reset();
    this.getKGOLAPCube().shutDown();
  }
  
  public boolean moduleContainsSameNumberOfTriplesInRepositories(String module, Repo repo1, Repo repo2) {
    String sparql =
        this.getKGOLAPCube().getSparqlPrefixes() +
        "\n"
      + "SELECT (COUNT(*) AS ?count) WHERE {\n"
      + "  GRAPH " + module + " {\n"
      + "    ?s ?p ?o .\n"
      + "  }\n"
      + "}\n";
    
    // get the count from both base and temporary repository
    int countRepo1 = -1;
    int countRepo2 = -2;
    
    try (
      ByteArrayOutputStream outRepo1 = new ByteArrayOutputStream();
      ByteArrayOutputStream outRepo2 = new ByteArrayOutputStream();
    ) {        
      repo1.executeTupleQuery(sparql, outRepo1);
      repo2.executeTupleQuery(sparql, outRepo2);

      String resultRepo1 = outRepo1.toString();
      String resultRepo2 = outRepo2.toString();
      
      countRepo1 = Integer.parseInt(resultRepo1.replaceAll("[^0-9]", ""));
      countRepo2 = Integer.parseInt(resultRepo2.replaceAll("[^0-9]", ""));

      logger.info("Count in repo1 " + module + " is " + countRepo1 + ".");
      logger.info("Count in repo2 " + module + " is " + countRepo2 + ".");
    } catch (IOException e) {
      logger.error("Error writing to binary output stream.", e);
    }
    
    return countRepo1 == countRepo2;
  }
  
  public boolean repositoriesContainSameNumberOfTriples(Repo repo1, Repo repo2) {
    String sparql =
        "SELECT (COUNT(*) AS ?count) WHERE {\n"
      + "  GRAPH ?g {\n"
      + "    ?s ?p ?o .\n"
      + "  }\n"
      + "}\n";
    
    // get the count from both base and temporary repository
    int countRepo1 = -1;
    int countRepo2 = -2;
    
    try (
      ByteArrayOutputStream outRepo1 = new ByteArrayOutputStream();
      ByteArrayOutputStream outRepo2 = new ByteArrayOutputStream();
    ) {        
      repo1.executeTupleQuery(sparql, outRepo1);
      repo2.executeTupleQuery(sparql, outRepo2);

      String resultRepo1 = outRepo1.toString();
      String resultRepo2 = outRepo2.toString();

      countRepo1 = Integer.parseInt(resultRepo1.replaceAll("[^0-9]", ""));
      countRepo2 = Integer.parseInt(resultRepo2.replaceAll("[^0-9]", ""));

      logger.info("Count in first repo is " + countRepo1 + " triples.");
      logger.info("Count in second repo is " + countRepo2 + " triples.");
    } catch (IOException e) {
      logger.error("Error writing to binary output stream.", e);
    }
    
    return countRepo1 == countRepo2;
  }
  
  public boolean repositoryContainsContext(Repo repo, 
                                           String context, 
                                           Map<String, String> coordinates,
                                           String assertedModule,
                                           String inferredModule) {
    StringBuilder contextHasDimAttributes1 = new StringBuilder();
    StringBuilder contextHasDimAttributes2 = new StringBuilder();
    
    for(String dimension : coordinates.keySet()) {
      contextHasDimAttributes1.append(
        "    " + context + " " + dimension + " " + coordinates.get(dimension) + " .\n"
      );
      
      contextHasDimAttributes2.append(
        "    " + context + " olap:hasDimensionAttributeValue " + coordinates.get(dimension) + " .\n"
      );
    }
    
    String query =
        this.getKGOLAPCube().getSparqlPrefixes()
      + "\n"
      + "ASK {\n"
      + "  GRAPH ckr:global {\n"
      + "    " + context + " rdf:type olap:Cell .\n"
      + "    " + context + " ckr:hasAssertedModule " + assertedModule + " .\n"
      +      contextHasDimAttributes1
      + "  }\n"
      + "  GRAPH <ckr:global-inf> {\n"
      + "    " + context + " ckr:hasModule " + assertedModule + " .\n"
      + "    " + context + " ckr:hasModule " + inferredModule + " .\n"
      +      contextHasDimAttributes2
      + "  }\n"
      + "}\n";
    
    boolean result = repo.ask(query);
    
    return result;
  }
  
  public boolean repositoryContainsResource(Repo repo, 
                                            String resource) {
    String query =
        this.getKGOLAPCube().getSparqlPrefixes()
      + "\n"
      + "ASK {\n"
      + "  GRAPH ?g {\n"
      + "    { ?s ?p " + resource + " . } UNION\n"
      + "    { " + resource + " ?p ?o . }\n"
      + "  }\n"
      + "}\n";
    
    boolean result = repo.ask(query);
    
    logger.info("Repository " + repo + " contains resource " + resource + ": " + result);
    
    return result;
  }
  
  public boolean repositoryContainsLevel(Repo repo,
                                         String level) {
    String query =
        this.getKGOLAPCube().getSparqlPrefixes()
      + "\n"
      + "ASK {\n"
      + "  GRAPH ckr:global {\n"
      + "    " + level + " rdf:type olap:Level .\n"
      + "    { " + level + " olap:directlyRollsUpTo ?l . } UNION \n"
      + "    { ?l olap:directlyRollsUpTo " + level + " . }\n"
      + "  }\n"
      + "  GRAPH <ckr:global-inf> {\n"
      + "    { " + level + " olap:rollsUpTo ?m . } UNION \n"
      + "    { ?m olap:rollsUpTo " + level + " . }\n"
      + "  }\n"
      + "}\n";
    
    boolean result = repo.ask(query);
    
    return result;
    
  }
  
  public boolean repositoryContainsDimensionMember(Repo repo,
                                                   String member,
                                                   String dimension) {
    String query =
        this.getKGOLAPCube().getSparqlPrefixes()
      + "\n"
      + "ASK {\n"
      + "  GRAPH ckr:global {\n"
      + "    " + member + " rdf:type " + dimension + " .\n"
      + "    { " + member + " olap:directlyRollsUpTo ?l . } UNION \n"
      + "    { ?l olap:directlyRollsUpTo " + member + " . }\n"
      + "  }\n"
      + "  GRAPH <ckr:global-inf> {\n"
      + "    { " + member + " olap:rollsUpTo ?m . } UNION \n"
      + "    { ?m olap:rollsUpTo " + member + " . }\n"
      + "  }\n"
      + "}\n";
    
    boolean result = repo.ask(query);
    
    return result; 
  }
  
  public boolean repositoryContainsQuad(Repo repo, 
                                        String graph,
                                        String subject,
                                        String predicate,
                                        String object) {    
    String query =
        this.getKGOLAPCube().getSparqlPrefixes()
      + "\n"
      + "ASK {\n"
      + "  GRAPH " + graph + " {\n"
      + "    " + subject + " " + predicate + " " + object + " .\n"
      + "  }\n"
      + "}\n";
    
    boolean result = repo.ask(query);
    
    return result;
  }
}
