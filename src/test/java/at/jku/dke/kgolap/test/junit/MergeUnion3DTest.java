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
import at.jku.dke.kgolap.repo.Repo;
import at.jku.dke.kgolap.repo.RepoProperties;

public class MergeUnion3DTest extends OperatorTest {
  private static final Logger logger = LoggerFactory.getLogger(MergeUnion3DTest.class);
  
  public MergeUnion3DTest() {
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
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("knowledge/atm-3D-cube.trig")) {
      logger.info("Adding cube knowledge to base repository ...");
      this.getKGOLAPCube().add(in);
      logger.info("Cube knowledge added.");
    } catch (IOException e) {
      logger.error("Error reading file.", e);
    }

    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("knowledge/atm-3D-object-slicedice.trig")) {
      logger.info("Adding object knowledge to base repository ...");
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
  public void testNoRollup() {
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
    
    HashMap<String,String> baseLevel = new HashMap<String,String>();
    baseLevel.put("cube:hasAircraft", "cube:Level_Aircraft_Model");
    baseLevel.put("cube:hasLocation", "cube:Level_Location_Segment");
    baseLevel.put("cube:hasDate", "cube:Level_Date_Day");

    this.getKGOLAPCube().mergeUnion(baseLevel);
    
    assertTrue(repositoriesContainSameNumberOfTriples(baseRepo, tempRepo));
  }
  
//  @Test
//  public void testRegionMerge() {    
//    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
//    
//    HashMap<String,String> regionLevel = new HashMap<String,String>();
//    regionLevel.put("cube:hasAircraft", "cube:Level_Aircraft_All");
//    regionLevel.put("cube:hasLocation", "cube:Level_Location_Region");
//    regionLevel.put("cube:hasDate", "cube:Level_Date_All");
//
//    this.getKGOLAPCube().mergeUnion(regionLevel);
//    
//    {
//      String[] contexts = new String[] { 
//          "cube:Ctx-Level_Aircraft_All-All-Level_Location_Region-LIMM-Level_Date_All-All", 
//          "cube:Ctx-Level_Aircraft_All-All-Level_Location_Region-LOVV-Level_Date_All-All" 
//      };
//
//      Map<String, String> map;
//      List<Map<String, String>> coordinateMaps = new ArrayList<Map<String, String>>();
//
//      map = new HashMap<String, String>();
//      map.put("cube:hasAircraft", "cube:Level_Aircraft_All-All");
//      map.put("cube:hasLocation", "cube:Level_Location_Region-LIMM");
//      map.put("cube:hasDate", "cube:Level_Date_All-All");
//      coordinateMaps.add(map);
//
//      map = new HashMap<String, String>();
//      map.put("cube:hasAircraft", "cube:Level_Aircraft_All-All");
//      map.put("cube:hasLocation", "cube:Level_Location_Region-LOVV");
//      map.put("cube:hasDate", "cube:Level_Date_All-All");
//      coordinateMaps.add(map);
//
//      String[] modules = new String[] { 
//          "cube:Ctx-Level_Aircraft_All-All-Level_Location_Region-LIMM-Level_Date_All-All-mod",
//          "cube:Ctx-Level_Aircraft_All-All-Level_Location_Region-LOVV-Level_Date_All-All-mod"
//      };
//
//      String[] inferredModules = new String[] {
//          "cube:Ctx-Level_Aircraft_All-All-Level_Location_Region-LIMM-Level_Date_All-All-inf",
//          "cube:Ctx-Level_Aircraft_All-All-Level_Location_Region-LOVV-Level_Date_All-All-inf"
//      };
//
//      for (int i = 0; i < contexts.length; i++) {
//        String context = contexts[i];
//        Map<String, String> coordinates = coordinateMaps.get(i);
//        String module = modules[i];
//        String inferred = inferredModules[i];
//        
//        assertTrue(repositoryContainsContext(tempRepo, context, coordinates, module, inferred));
//      }
//      
//      
//      // Ctx-0 should still be there unchanged
//      assertFalse(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-0",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      
//      // the rolled up contexts should be ckr:Null
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-1",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-1-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-1-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-2",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-2-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-2-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-3",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-3-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-3-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-4",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-4-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-4-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-5",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-5-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-5-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-6",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-6-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-6-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-7",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-7-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-7-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-8",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-8-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-8-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-9",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-9-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-9-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-10",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-10-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-10-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-11",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-11-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-11-inf"));
//      
//      
//      assertTrue(
//          repositoryContainsQuad(
//            tempRepo,
//            "ckr:global",
//            "cube:Ctx-12",
//            "rdf:type",
//            "ckr:Null"
//          )
//      );
//      
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-12-mod"));
//      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-12-inf"));
//    }
//    {
//      String sparqlBase = this.getKGOLAPCube().getSparqlPrefixes() +
//          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
//          "    GRAPH ckr:global {\r\n" + 
//          "       ?ctx cube:hasLocation/olap:directlyRollsUpTo* cube:Level_Location_Region-LIMM.\r\n" + 
//          "    }\r\n" + 
//          "    GRAPH ckr:global {\r\n" + 
//          "       ?ctx ckr:hasAssertedModule ?m .\r\n" + 
//          "    }\r\n" + 
//          "    GRAPH ?m {\r\n" + 
//          "       ?s ?p ?o .\r\n" + 
//          "    }\r\n" + 
//          "} ORDER BY ?s ?p ?o";
//      
//      String resultBase = this.getKGOLAPCube().getBaseRepository().executeTupleQuery(sparqlBase);
//      
//      String sparqlTemp = this.getKGOLAPCube().getSparqlPrefixes() +
//          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
//          "    GRAPH ckr:global {\r\n" + 
//          "        cube:Ctx-Level_Aircraft_All-All-Level_Location_Region-LIMM-Level_Date_All-All ckr:hasAssertedModule ?m .\r\n" + 
//          "    }\r\n" + 
//          "    GRAPH ?m {\r\n" + 
//          "        ?s ?p ?o .\r\n" + 
//          "    }\r\n" + 
//          "} ORDER BY ?s ?p ?o";
//      
//      String resultTemp = this.getKGOLAPCube().getTempRepository().executeTupleQuery(sparqlTemp);
//      
//      assertEquals(resultBase, resultTemp);
//    }
//    {
//      String sparqlBase = this.getKGOLAPCube().getSparqlPrefixes() +
//          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
//          "    GRAPH ckr:global {\r\n" + 
//          "       ?ctx cube:hasLocation/olap:directlyRollsUpTo* cube:Level_Location_Region-LOVV.\r\n" + 
//          "    }\r\n" + 
//          "    GRAPH ckr:global {\r\n" + 
//          "       ?ctx ckr:hasAssertedModule ?m .\r\n" + 
//          "    }\r\n" + 
//          "    GRAPH ?m {\r\n" + 
//          "       ?s ?p ?o .\r\n" + 
//          "    }\r\n" + 
//          "} ORDER BY ?s ?p ?o";
//      
//      String resultBase = this.getKGOLAPCube().getBaseRepository().executeTupleQuery(sparqlBase);
//      
//      
//      String sparqlTemp = this.getKGOLAPCube().getSparqlPrefixes() +
//          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
//          "    GRAPH ckr:global {\r\n" + 
//          "        cube:Ctx-Level_Aircraft_All-All-Level_Location_Region-LOVV-Level_Date_All-All ckr:hasAssertedModule ?m .\r\n" + 
//          "    }\r\n" + 
//          "    GRAPH ?m {\r\n" + 
//          "        ?s ?p ?o .\r\n" + 
//          "    }\r\n" + 
//          "} ORDER BY ?s ?p ?o";
//      
//      String resultTemp = this.getKGOLAPCube().getTempRepository().executeTupleQuery(sparqlTemp);
//      
//      
//      assertEquals(resultBase, resultTemp);
//    }
//  }
  
  @Test
  public void testTypeRegionMonthMerge() {  
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();  
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
    
    HashMap<String,String> regionLevel = new HashMap<String,String>();
    regionLevel.put("cube:hasAircraft", "cube:Level_Aircraft_Type");
    regionLevel.put("cube:hasLocation", "cube:Level_Location_Region");
    regionLevel.put("cube:hasDate", "cube:Level_Date_Month");

    this.getKGOLAPCube().mergeUnion(regionLevel);
    
    {      
      // Ctx-0, Ctx-1, Ctx-3, Ctx-9 should still be there unchanged
      assertFalse(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-0",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-1",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-3",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-9",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      
      // the rolled up contexts should be ckr:Null
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-4",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-4-mod"));
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-4-inf"));
      
      
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-5",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-5-mod"));
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-5-inf"));
      
      
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-6",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-6-mod"));
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-6-inf"));
      
      
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-10",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-10-mod"));
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-10-inf"));
      
      
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-11",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-11-mod"));
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-11-inf"));
      
      
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-12",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-12-mod"));
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-12-inf"));
      
      
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-Level_Aircraft_Type-FixedWing-Level_Location_Region-EDUU-Level_Date_Month-February2020",
            "rdf:type",
            "olap:Cell"
          )
      );
      
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "<ckr:global-inf>",
            "cube:Ctx-Level_Aircraft_Type-FixedWing-Level_Location_Region-EDUU-Level_Date_Month-February2020",
            "rdf:type",
            "ckr:Context"
          )
      );
      
      assertTrue(
          repositoryContainsQuad(
            tempRepo,
            "ckr:global",
            "cube:Ctx-13",
            "rdf:type",
            "ckr:Null"
          )
      );
      
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-13-mod"));
      assertFalse(this.repositoryContainsResource(tempRepo, "cube:Ctx-13-inf"));
    }
    {
      String sparqlBase = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "       ?ctx cube:hasAircraft/olap:directlyRollsUpTo* cube:Level_Aircraft_Type-FixedWing .\r\n" + 
          "       ?ctx cube:hasLocation/olap:directlyRollsUpTo* cube:Level_Location_Region-LOVV .\r\n" + 
          "       ?ctx cube:hasDate/olap:directlyRollsUpTo* cube:Level_Date_Month-February2020 .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "       ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "       ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "} ORDER BY ?s ?p ?o";
      
      String resultBase = this.getKGOLAPCube().getBaseRepository().executeTupleQuery(sparqlBase);
      
      String sparqlTemp = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        cube:Ctx-2 ckr:hasAssertedModule ?m .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "} ORDER BY ?s ?p ?o";
      
      String resultTemp = this.getKGOLAPCube().getTempRepository().executeTupleQuery(sparqlTemp);
      
      assertEquals(resultBase, resultTemp);
    }
    {
      String sparqlBase = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "       ?ctx cube:hasAircraft/olap:directlyRollsUpTo* cube:Level_Aircraft_Type-FixedWing .\r\n" + 
          "       ?ctx cube:hasLocation/olap:directlyRollsUpTo* cube:Level_Location_Region-LIMM .\r\n" + 
          "       ?ctx cube:hasDate/olap:directlyRollsUpTo* cube:Level_Date_Month-February2020 .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "       ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "       ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "} ORDER BY ?s ?p ?o";
            
      String resultBase = this.getKGOLAPCube().getBaseRepository().executeTupleQuery(sparqlBase);
      
      
      String sparqlTemp = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        cube:Ctx-8 ckr:hasAssertedModule ?m .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "} ORDER BY ?s ?p ?o";
      
      String resultTemp = this.getKGOLAPCube().getTempRepository().executeTupleQuery(sparqlTemp);
      
      
      assertEquals(resultBase, resultTemp);
    }
    {
      String sparqlBase = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "       ?ctx cube:hasAircraft/olap:directlyRollsUpTo* cube:Level_Aircraft_Type-FixedWing .\r\n" + 
          "       ?ctx cube:hasLocation/olap:directlyRollsUpTo* cube:Level_Location_Region-EDUU .\r\n" + 
          "       ?ctx cube:hasDate/olap:directlyRollsUpTo* cube:Level_Date_Month-February2020 .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "       ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "       ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "} ORDER BY ?s ?p ?o";
            
      String resultBase = this.getKGOLAPCube().getBaseRepository().executeTupleQuery(sparqlBase);
      
      
      String sparqlTemp = this.getKGOLAPCube().getSparqlPrefixes() +
          "SELECT DISTINCT ?s ?p ?o WHERE {\r\n" + 
          "    GRAPH ckr:global {\r\n" + 
          "        cube:Ctx-Level_Aircraft_Type-FixedWing-Level_Location_Region-EDUU-Level_Date_Month-February2020 ckr:hasAssertedModule ?m .\r\n" + 
          "    }\r\n" + 
          "    GRAPH ?m {\r\n" + 
          "        ?s ?p ?o .\r\n" + 
          "    }\r\n" + 
          "} ORDER BY ?s ?p ?o";
      
      String resultTemp = this.getKGOLAPCube().getTempRepository().executeTupleQuery(sparqlTemp);
      
      
      assertEquals(resultBase, resultTemp);
    }

    // Check for new inferences after merge
    assertFalse(
        repositoryContainsQuad(
          baseRepo,
          "cube:Ctx-8-inf",
          "obj:t",
          "rdf:type",
          "obj:a"
        )
    );

    assertTrue(
        repositoryContainsQuad(
          tempRepo,
          "cube:Ctx-8-inf",
          "obj:t",
          "rdf:type",
          "obj:a"
        )
    );

    assertFalse(
        repositoryContainsQuad(
          baseRepo,
          "cube:Ctx-8-inf",
          "obj:u",
          "rdf:type",
          "obj:b"
        )
    );

    assertTrue(
        repositoryContainsQuad(
          tempRepo,
          "cube:Ctx-8-inf",
          "obj:u",
          "rdf:type",
          "obj:b"
        )
    );
    
    {
      String sparqlBase = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(*) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx cube:hasAircraft ?aircraft .\r\n" + 
          "                ?aircraft olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "\r\n" + 
          "                ?ctx cube:hasLocation ?location .\r\n" + 
          "                ?location olap:atLevel cube:Level_Location_Region .\r\n" + 
          "\r\n" + 
          "                ?ctx cube:hasDate ?date .\r\n" + 
          "                ?date olap:atLevel cube:Level_Date_Month .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt = 2)\r\n" + 
          "}";
      
      assertTrue(baseRepo.ask(sparqlBase));
      

      String sparqlTemp = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(*) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx cube:hasAircraft ?aircraft .\r\n" + 
          "                ?aircraft olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "\r\n" + 
          "                ?ctx cube:hasLocation ?location .\r\n" + 
          "                ?location olap:atLevel cube:Level_Location_Region .\r\n" + 
          "\r\n" + 
          "                ?ctx cube:hasDate ?date .\r\n" + 
          "                ?date olap:atLevel cube:Level_Date_Month .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt = 3)\r\n" + 
          "}";
      
      assertTrue(tempRepo.ask(sparqlTemp));
    }
  }
}
