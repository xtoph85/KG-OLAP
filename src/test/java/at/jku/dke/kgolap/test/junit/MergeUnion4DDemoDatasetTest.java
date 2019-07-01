package at.jku.dke.kgolap.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.KGOLAPCubeProperties;
import at.jku.dke.kgolap.repo.Repo;
import at.jku.dke.kgolap.repo.RepoProperties;

public class MergeUnion4DDemoDatasetTest extends OperatorTest {
  private static final Logger logger = LoggerFactory.getLogger(MergeUnion4DDemoDatasetTest.class);
  
  public MergeUnion4DDemoDatasetTest() {
    super();
  }

  @Override
  public KGOLAPCubeProperties getProperties() {
    KGOLAPCubeProperties properties = new KGOLAPCubeProperties();

    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("rulesets/atm-4D-ruleset.ttl")) {
      logger.info("Read the ruleset file.");
      String rulesetTtl = IOUtils.toString(in, StandardCharsets.UTF_8.name());
      properties.setRulesetTtl(rulesetTtl);
    } catch (IOException e) {
      logger.error("Error reading ruleset file.", e);
    }

    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config/prefixes.properties")) {
      logger.info("Read the prefixes properties from file.");

      Properties prefixProperties = new Properties();
      prefixProperties.load(in);

      for (String prefix : prefixProperties.stringPropertyNames()) {
        properties.addPrefix(prefix, prefixProperties.getProperty(prefix));
      }
    } catch (IOException e) {
      logger.error("Error reading prefixes properties from file.", e);
    }

    properties.setBaseRepoFactoryClass("at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory");
    properties.setTempRepoFactoryClass("at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory");

    RepoProperties baseRepoProperties = new RepoProperties();
    baseRepoProperties.setRepositoryURL("http://localhost:7201/repositories/Base");

    RepoProperties tempRepoProperties = new RepoProperties();
    tempRepoProperties.setRepositoryURL("http://localhost:7201/repositories/Temp");

    properties.setBaseRepoProperties(baseRepoProperties);
    properties.setTempRepoProperties(tempRepoProperties);

    return properties;
  }

  @Override
  public void loadTestdata() {
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("knowledge/demo-4D.trig")) {
      logger.info("Adding demo dataset to base repository ...");
      this.getKGOLAPCube().add(in);
      logger.info("Demo dataset added.");
    } catch (IOException e) {
      logger.error("Error reading file.", e);
    }
    
    logger.info("Load all data from the base repository into the temp repository.");
    HashMap<String, String> allSlice = new HashMap<String, String>();
    allSlice.put("cube:hasAircraft", "cube:Level_Aircraft_All-All");
    allSlice.put("cube:hasLocation", "cube:Level_Location_All-All");
    allSlice.put("cube:hasDate", "cube:Level_Date_All-All");
    allSlice.put("cube:hasImportance", "cube:Level_Importance_All-All");

    this.getKGOLAPCube().sliceDice(allSlice);

    assertTrue(repositoriesContainSameNumberOfTriples(baseRepo, tempRepo));
  }
  
  @Test
  public void testNoRollup() {
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
    
    HashMap<String,String> baseLevel = new HashMap<String,String>();
    baseLevel.put("cube:hasAircraft", "cube:Level_Aircraft_Model");
    baseLevel.put("cube:hasLocation", "cube:Level_Location_Segment");
    baseLevel.put("cube:hasDate", "cube:Level_Date_Day");
    baseLevel.put("cube:hasImportance", "cube:Level_Importance_Importance");

    this.getKGOLAPCube().mergeUnion(baseLevel);
    
    assertTrue(repositoriesContainSameNumberOfTriples(baseRepo, tempRepo));
  }
  
  @Test
  public void testTypeRegionMonthMerge() {  
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();  
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
    
    HashMap<String,String> regionLevel = new HashMap<String,String>();
    regionLevel.put("cube:hasAircraft", "cube:Level_Aircraft_Type");
    regionLevel.put("cube:hasLocation", "cube:Level_Location_Region");
    regionLevel.put("cube:hasDate", "cube:Level_Date_Month");
    regionLevel.put("cube:hasImportance", "cube:Level_Importance_All");

    this.getKGOLAPCube().mergeUnion(regionLevel);
    
    {
      String sparqlCountBase = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT (COUNT(*) AS ?cnt) WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "        ?ctx cube:hasAircraft ?air .\r\n" + 
          "        ?ctx cube:hasLocation ?loc .\r\n" + 
          "        ?ctx cube:hasDate ?dat .\r\n" + 
          "        ?ctx cube:hasImportance ?imp .\r\n" + 
          "        ?air olap:atLevel/olap:directlyRollsUpTo* cube:Level_Aircraft_Type .\r\n" + 
          "        ?loc olap:atLevel/olap:directlyRollsUpTo* cube:Level_Location_Region .\r\n" + 
          "        ?dat olap:atLevel/olap:directlyRollsUpTo* cube:Level_Date_Month .\r\n" + 
          "        ?imp olap:atLevel/olap:directlyRollsUpTo* cube:Level_Importance_All .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "}";
      
      String resultCountBase = baseRepo.executeTupleQuery(sparqlCountBase);
      
      
      String sparqlCountTemp = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT (COUNT(*) AS ?cnt) WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "        ?ctx cube:hasAircraft ?air .\r\n" + 
          "        ?ctx cube:hasLocation ?loc .\r\n" + 
          "        ?ctx cube:hasDate ?dat .\r\n" + 
          "        ?ctx cube:hasImportance ?imp .\r\n" + 
          "        ?air olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "        ?loc olap:atLevel cube:Level_Location_Region .\r\n" + 
          "        ?dat olap:atLevel cube:Level_Date_Month .\r\n" + 
          "        ?imp olap:atLevel cube:Level_Importance_All .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "}";
      
      String resultCountTemp = tempRepo.executeTupleQuery(sparqlCountTemp);
      
      
      assertEquals(resultCountBase, resultCountTemp);
      
      
      
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "        ?ctx cube:hasAircraft ?air .\r\n" + 
          "        ?ctx cube:hasLocation ?loc .\r\n" + 
          "        ?ctx cube:hasDate ?dat .\r\n" + 
          "        ?ctx cube:hasImportance ?imp .\r\n" + 
          "        ?air olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "        ?loc olap:atLevel cube:Level_Location_Region .\r\n" + 
          "        ?dat olap:atLevel cube:Level_Date_Month .\r\n" + 
          "        ?imp olap:atLevel cube:Level_Importance_All .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = baseRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "        ?ctx cube:hasAircraft ?air .\r\n" + 
          "        ?ctx cube:hasLocation ?loc .\r\n" + 
          "        ?ctx cube:hasDate ?dat .\r\n" + 
          "        ?ctx cube:hasImportance ?imp .\r\n" + 
          "        ?air olap:directlyRollsUpTo*/olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "        ?loc olap:directlyRollsUpTo*/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "        ?dat olap:directlyRollsUpTo*/olap:atLevel cube:Level_Date_Month .\r\n" +  
          "        ?imp olap:directlyRollsUpTo*/olap:atLevel cube:Level_Importance_All .\r\n" + 
          "        ?air olap:atLevel ?airLevel .\r\n" + 
          "        ?loc olap:atLevel ?locLevel .\r\n" + 
          "        ?dat olap:atLevel ?datLevel .\r\n" + 
          "        ?imp olap:atLevel ?impLevel .\r\n" + 
          "        FILTER(!(?airLevel = cube:Level_Aircraft_Type && ?locLevel = cube:Level_Location_Region && ?datLevel = cube:Level_Date_Month && ?impLevel = cube:Level_Importance_All))\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean resultBase = baseRepo.ask(sparql);
      boolean resultTemp = tempRepo.ask(sparql);
      
      assertTrue(resultBase);
      assertFalse(resultTemp);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx rdf:type ckr:Null .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean resultBase = baseRepo.ask(sparql);
      boolean resultTemp = tempRepo.ask(sparql);
      
      assertFalse(resultBase);
      assertTrue(resultTemp);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx rdf:type ckr:Null .\r\n" + 
          "    }\r\n" + 
          "    GRAPH <ckr:global-inf> {\r\n" + 
          "        ?ctx ckr:hasModule ?mod .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx rdf:type ckr:Null .\r\n" + 
          "        ?ctx cube:hasAircraft/olap:atLevel ?airLevel .\r\n" + 
          "        ?ctx cube:hasLocation/olap:atLevel ?locLevel .\r\n" + 
          "        ?ctx cube:hasDate/olap:atLevel ?datLevel .\r\n" + 
          "        ?ctx cube:hasImportance/olap:atLevel ?impLevel .\r\n" + 
          "        {\r\n" + 
          "            FILTER(\r\n" + 
          "                ?airLevel = cube:Level_Aircraft_Type &&\r\n" + 
          "                ?locLevel = cube:Level_Location_Region &&\r\n" + 
          "                ?datLevel = cube:Level_Date_Month\r\n" +
          "                ?impLevel = cube:Level_Importance_All\r\n" + 
          "            )\r\n" + 
          "        } UNION {\r\n" + 
          "            FILTER(\r\n" + 
          "                ?airLevel = cube:Level_Aircraft_All &&\r\n" + 
          "                ?locLevel = cube:Level_Location_Region &&\r\n" + 
          "                ?datLevel = cube:Level_Date_Year\r\n" + 
          "                ?impLevel = cube:Level_Importance_Package\r\n" + 
          "            )\r\n" + 
          "        } UNION {\r\n" + 
          "            FILTER(\r\n" + 
          "                ?airLevel = cube:Level_Aircraft_All &&\r\n" + 
          "                ?locLevel = cube:Level_Location_Region &&\r\n" + 
          "                ?datLevel = cube:Level_Date_All\r\n" + 
          "                ?impLevel = cube:Level_Importance_All\r\n" + 
          "            )\r\n" + 
          "        } UNION {\r\n" + 
          "            FILTER(\r\n" + 
          "                ?airLevel = cube:Level_Aircraft_All &&\r\n" + 
          "                ?locLevel = cube:Level_Location_All &&\r\n" + 
          "                ?datLevel = cube:Level_Date_All\r\n" + 
          "                ?impLevel = cube:Level_Importance_All\r\n" + 
          "            )\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx rdf:type ckr:Null .\r\n" + 
          "        ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Model .\r\n" + 
          "        ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "        ?ctx cube:hasDate/olap:atLevel cube:Level_Date_Day .\r\n" + 
          "        ?ctx cube:hasImportance/olap:atLevel cube:Level_Importance_Importance .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertTrue(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx rdf:type ckr:Null .\r\n" + 
          "        ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "        ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "        ?ctx cube:hasDate/olap:atLevel cube:Level_Date_Day .\r\n" + 
          "        ?ctx cube:hasImportance/olap:atLevel cube:Level_Importance_Importance .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertTrue(result);
    }

    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx rdf:type ckr:Null .\r\n" + 
          "        ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "        ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "        ?ctx cube:hasDate/olap:atLevel cube:Level_Date_Month .\r\n" + 
          "        ?ctx cube:hasImportance/olap:atLevel cube:Level_Importance_Package .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
        
      assertTrue(result);
    }
  }
  
  @Test
  public void testTypeRegionMerge() {  
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();  
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
    
    HashMap<String,String> regionLevel = new HashMap<String,String>();
    regionLevel.put("cube:hasAircraft", "cube:Level_Aircraft_Type");
    regionLevel.put("cube:hasLocation", "cube:Level_Location_Region");
    regionLevel.put("cube:hasDate", "cube:Level_Date_All");
    regionLevel.put("cube:hasImportance", "cube:Level_Importance_All");

    this.getKGOLAPCube().mergeUnion(regionLevel);
    
    {
      String sparqlCount = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT (COUNT(*) AS ?cnt) WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "        ?ctx cube:hasAircraft ?air .\r\n" + 
          "        ?ctx cube:hasLocation ?loc .\r\n" + 
          "        ?ctx cube:hasDate ?dat .\r\n" + 
          "        ?ctx cube:hasImportance ?imp .\r\n" + 
          "        ?air olap:atLevel/olap:directlyRollsUpTo* cube:Level_Aircraft_Type .\r\n" + 
          "        ?loc olap:atLevel/olap:directlyRollsUpTo* cube:Level_Location_Segment .\r\n" + 
          "        ?dat olap:atLevel/olap:directlyRollsUpTo* cube:Level_Date_Month .\r\n" + 
          "        ?imp olap:atLevel/olap:directlyRollsUpTo* cube:Level_Importance_Package .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "}";
      
      String resultCountBase = baseRepo.executeTupleQuery(sparqlCount);      
      String resultCountTemp = tempRepo.executeTupleQuery(sparqlCount);
      
      
      assertNotEquals(resultCountBase, resultCountTemp);
    } 
    { 
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "        ?ctx cube:hasAircraft ?air .\r\n" + 
          "        ?ctx cube:hasLocation ?loc .\r\n" + 
          "        ?ctx cube:hasDate ?dat .\r\n" + 
          "        ?ctx cube:hasImportance ?imp .\r\n" + 
          "        ?air olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "        ?loc olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "        ?dat olap:atLevel cube:Level_Date_Month .\r\n" + 
          "        ?imp olap:atLevel cube:Level_Importance_Package .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "        ?ctx cube:hasAircraft ?air .\r\n" + 
          "        ?ctx cube:hasLocation ?loc .\r\n" + 
          "        ?ctx cube:hasDate ?dat .\r\n" + 
          "        ?ctx cube:hasImportance ?imp .\r\n" + 
          "        ?air olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "        ?loc olap:atLevel cube:Level_Location_Region .\r\n" + 
          "        ?dat olap:atLevel cube:Level_Date_All .\r\n" + 
          "        ?imp olap:atLevel cube:Level_Importance_All .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = baseRepo.ask(sparql);
      
      assertFalse(result);
    }
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "        ?ctx cube:hasAircraft ?air .\r\n" + 
          "        ?ctx cube:hasLocation ?loc .\r\n" + 
          "        ?ctx cube:hasDate ?dat .\r\n" + 
          "        ?ctx cube:hasImportance ?imp .\r\n" + 
          "        ?air olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "        ?loc olap:atLevel cube:Level_Location_Region .\r\n" + 
          "        ?dat olap:atLevel cube:Level_Date_All .\r\n" + 
          "        ?imp olap:atLevel cube:Level_Importance_All .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertTrue(result);
    }
  }
}
