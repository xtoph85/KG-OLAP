package at.jku.dke.kgolap.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import at.jku.dke.kgolap.repo.HTTPConnectable;
import at.jku.dke.kgolap.repo.Repo;
import at.jku.dke.kgolap.repo.RepoProperties;

public class GroupByProperties3DDemoDatasetTest extends OperatorTest {
  private static final Logger logger = LoggerFactory.getLogger(GroupByProperties3DDemoDatasetTest.class);
  
  public GroupByProperties3DDemoDatasetTest() {
    super();
  }

  @Override
  public KGOLAPCubeProperties getProperties() {
    KGOLAPCubeProperties properties = new KGOLAPCubeProperties();

    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("rulesets/atm-3D-ruleset.ttl")) {
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
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("knowledge/demo-3D.trig")) {
      logger.info("Adding knowledge to base repository ...");
      this.getKGOLAPCube().add(in);
      logger.info("Object knowledge added.");
    } catch (IOException e) {
      logger.error("Error reading file.", e);
    }
    
    logger.info("Load all data from the base repository into the temp repository.");
    HashMap<String, String> allSlice = new HashMap<String, String>();
    allSlice.put("cube:hasAircraft", "cube:Level_Aircraft_All-All");
    allSlice.put("cube:hasLocation", "cube:Level_Location_All-All");
    allSlice.put("cube:hasDate", "cube:Level_Date_All-All");

    this.getKGOLAPCube().sliceDice(allSlice);

    assertTrue(repositoriesContainSameNumberOfTriples(baseRepo, tempRepo));
  }
  
  @Test
  public void testGroupBy() {
    Repo baseRepository = this.getKGOLAPCube().getBaseRepository();
    Repo tempRepository = this.getKGOLAPCube().getTempRepository();
    
    HashMap<String,String> granularity = new HashMap<String,String>();
    granularity.put("cube:hasAircraft", "cube:Level_Aircraft_Model");
    granularity.put("cube:hasLocation", "cube:Level_Location_Segment");
    granularity.put("cube:hasDate", "cube:Level_Date_Day");
    
    this.getKGOLAPCube().groupByProperties(granularity, new String[] { "obj:operationalStatus" }, "obj:grouping");
    
    if(this.getKGOLAPCube().getBaseRepository() instanceof HTTPConnectable) {
      {
        String sparql = this.getKGOLAPCube().getSparqlPrefixes() + 
            "ASK WHERE {\r\n" + 
            "    GRAPH ckr:global {\r\n" + 
            "        ?ctx ckr:hasAssertedModule ?mod .\r\n" + 
            "        ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Model .\r\n" + 
            "        ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
            "        ?ctx cube:hasDate/olap:atLevel cube:Level_Date_Day .\r\n" + 
            "    }\r\n" + 
            "    GRAPH ?mod {\r\n" + 
            "        ?availability obj:grouping ?grouping .\r\n" + 
            "        ?grouping obj:operationalStatus ?s1 .\r\n" + 
            "    }\r\n" + 
            "    {\r\n" + 
            "        SELECT ?mod ?availability ?s2 WHERE {\r\n" + 
            "            SERVICE <http://localhost:7201/repositories/Base> {\r\n" + 
            "                GRAPH ?mod {\r\n" + 
            "                    ?availability obj:operationalStatus ?s2 .\r\n" + 
            "                }\r\n" + 
            "            }\r\n" + 
            "        }\r\n" + 
            "    }\r\n" + 
            "    FILTER(?s1 != ?s2)\r\n" + 
            "} ORDER BY ?availability";
        
        boolean result = tempRepository.ask(sparql);
        
        assertFalse(result);            
      }
    }
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT (COUNT(*) AS ?cnt) WHERE {\r\n" + 
          "    GRAPH ?mod {\r\n" + 
          "        ?availability obj:usage ?s .\r\n" + 
          "    }\r\n" + 
          "}";
      
      String baseResult = baseRepository.executeTupleQuery(sparql);
      String tempResult = tempRepository.executeTupleQuery(sparql);
      
      assertEquals(baseResult, tempResult);
    }
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(DISTINCT ?g) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ?mod {\r\n" + 
          "                ?x obj:grouping ?g .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt != 3)\r\n" + 
          "}";
      
      boolean result = tempRepository.ask(sparql);
      
      assertFalse(result);
    }
  }
}
