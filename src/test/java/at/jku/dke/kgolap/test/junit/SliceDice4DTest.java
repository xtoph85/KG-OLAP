package at.jku.dke.kgolap.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.KGOLAPCubeProperties;
import at.jku.dke.kgolap.repo.Repo;
import at.jku.dke.kgolap.repo.RepoProperties;

public class SliceDice4DTest extends OperatorTest {
  private static final Logger logger = LoggerFactory.getLogger(SliceDice4DTest.class);

  public SliceDice4DTest() {
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
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("knowledge/atm-4D-cube.trig")) {
      logger.info("Adding cube knowledge ...");
      this.getKGOLAPCube().add(in);
      logger.info("Cube knowledge added.");
    } catch (IOException e) {
      logger.error("Error reading file.", e);
    }

    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("knowledge/atm-3D-object-slicedice.trig")) {
      logger.info("Adding object knowledge ...");
      this.getKGOLAPCube().add(in);
      logger.info("Object knowledge added.");
    } catch (IOException e) {
      logger.error("Error reading file.", e);
    }
  }
  
  @Test
  public void testRulesetRdfsLocalReasoning() {
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();
    
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-8-inf", "obj:x", "rdf:type", "obj:c"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-8-inf", "obj:x", "rdf:type", "obj:a"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-8-inf", "obj:y", "rdf:type", "obj:d"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-8-inf", "obj:y", "rdf:type", "obj:b"));

    
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-9-inf", "obj:q", "rdf:type", "obj:c"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-9-inf", "obj:q", "rdf:type", "obj:a"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-9-inf", "obj:s", "rdf:type", "obj:d"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-9-inf", "obj:s", "rdf:type", "obj:b"));

    
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-10-inf", "obj:q", "rdf:type", "obj:c"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-10-inf", "obj:q", "rdf:type", "obj:a"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-10-inf", "obj:s", "rdf:type", "obj:d"));
    assertFalse(this.repositoryContainsQuad(baseRepo, "cube:Ctx-10-inf", "obj:s", "rdf:type", "obj:b"));
  }

  @Test
  /**
   * When using the root granularity as parameter for slice-and-dice operation,
   * the input and output cube should have the same number of triples.
   */
  public void testFullSelection() {
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();

    HashMap<String, String> allSlice = new HashMap<String, String>();
    allSlice.put("cube:hasAircraft", "cube:Level_Aircraft_All-All");
    allSlice.put("cube:hasLocation", "cube:Level_Location_All-All");
    allSlice.put("cube:hasDate", "cube:Level_Date_All-All");
    allSlice.put("cube:hasImportance", "cube:Level_Importance_All-All");
    
    this.getKGOLAPCube().setExecuteInMemory(true);
    
    this.getKGOLAPCube().sliceDice(allSlice);

    assertTrue(repositoriesContainSameNumberOfTriples(baseRepo, tempRepo));
  }

  @Test
  public void testAircraftFixedWingRegionLOVVSelection() {
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();

    HashMap<String, String> fixedWingLovvSlice = new HashMap<String, String>();
    fixedWingLovvSlice.put("cube:hasAircraft", "cube:Level_Aircraft_Type-FixedWing");
    fixedWingLovvSlice.put("cube:hasLocation", "cube:Level_Location_Region-LOVV");
    fixedWingLovvSlice.put("cube:hasDate", "cube:Level_Date_All-All");
    fixedWingLovvSlice.put("cube:hasImportance", "cube:Level_Importance_All-All");

    this.getKGOLAPCube().sliceDice(fixedWingLovvSlice);

    // The target repository should contain the definitions of
    // cube:Ctx-0, cube:Ctx-1, cube:Ctx-2, cube:Ctx-3, cube:Ctx-4, cube:Ctx-5, cube:Ctx-6
    {
      String[] contexts = new String[] { "cube:Ctx-0", "cube:Ctx-1", "cube:Ctx-2", "cube:Ctx-3", 
          "cube:Ctx-4", "cube:Ctx-5", "cube:Ctx-6" };

      Map<String, String> map;
      List<Map<String, String>> coordinateMaps = new ArrayList<Map<String, String>>();

      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_All-All ");
      map.put("cube:hasLocation", "cube:Level_Location_All-All");
      map.put("cube:hasDate", "cube:Level_Date_All-All");
      map.put("cube:hasImportance", "cube:Level_Importance_All-All");
      coordinateMaps.add(map);

      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_All-All ");
      map.put("cube:hasLocation", "cube:Level_Location_Region-LOVV");
      map.put("cube:hasDate", "cube:Level_Date_Year-2020");
      map.put("cube:hasImportance", "cube:Level_Importance_All-All");
      coordinateMaps.add(map);

      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_Type-FixedWing ");
      map.put("cube:hasLocation", "cube:Level_Location_Region-LOVV");
      map.put("cube:hasDate", "cube:Level_Date_Month-February2020");
      map.put("cube:hasImportance", "cube:Level_Importance_Package-Supplementary");
      coordinateMaps.add(map);

      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_All-All ");
      map.put("cube:hasLocation", "cube:Level_Location_Segment-LOWW");
      map.put("cube:hasDate", "cube:Level_Date_Year-2020");
      map.put("cube:hasImportance", "cube:Level_Importance_All-All");
      coordinateMaps.add(map);

      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_Type-FixedWing ");
      map.put("cube:hasLocation", "cube:Level_Location_Segment-LOWW");
      map.put("cube:hasDate", "cube:Level_Date_Day-12February2020");
      map.put("cube:hasImportance", "cube:Level_Importance_Package-Essential");
      coordinateMaps.add(map);

      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_Model-A380");
      map.put("cube:hasLocation", "cube:Level_Location_Segment-LOWW");
      map.put("cube:hasDate", "cube:Level_Date_Day-12February2020");
      map.put("cube:hasImportance", "cube:Level_Importance_Importance-FlightCritical");
      coordinateMaps.add(map);

      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_Model-A380");
      map.put("cube:hasLocation", "cube:Level_Location_Segment-LOWW");
      map.put("cube:hasDate", "cube:Level_Date_Day-12February2020");
      map.put("cube:hasImportance", "cube:Level_Importance_Importance-Restriction");
      coordinateMaps.add(map);

      String[] modules = new String[] { "cube:Ctx-0-mod", "cube:Ctx-1-mod", "cube:Ctx-2-mod", "cube:Ctx-3-mod", 
          "cube:Ctx-4-mod", "cube:Ctx-5-mod", "cube:Ctx-6-mod" };

      String[] inferredModules = new String[] { "cube:Ctx-0-inf", "cube:Ctx-1-inf", "cube:Ctx-2-inf", 
          "cube:Ctx-3-inf", "cube:Ctx-4-inf", "cube:Ctx-5-inf", "cube:Ctx-6-inf" };

      for (int i = 0; i < contexts.length; i++) {
        String context = contexts[i];
        Map<String, String> coordinates = coordinateMaps.get(i);
        String module = modules[i];
        String inferred = inferredModules[i];
        
        assertTrue(repositoryContainsContext(tempRepo, context, coordinates, module, inferred));
      }
    }

    {
      // Modules of selected contexts should contain same number of triples that
      // they also contain in the source repository.
      String[] modules = new String[] { 
          "cube:Ctx-0-mod", "cube:Ctx-0-inf", 
          "cube:Ctx-1-mod", "cube:Ctx-1-inf", 
          "cube:Ctx-2-mod", "cube:Ctx-2-inf", 
          "cube:Ctx-3-mod", "cube:Ctx-3-inf", 
          "cube:Ctx-4-mod", "cube:Ctx-4-inf", 
          "cube:Ctx-5-mod", "cube:Ctx-5-inf", 
          "cube:Ctx-6-mod", "cube:Ctx-6-inf" 
      };

      for (String module : modules) {
        assertTrue(this.moduleContainsSameNumberOfTriplesInRepositories(module, baseRepo, tempRepo));
      }
    }

    {
      // The other contexts shouldn't be contained in the result.
      String[] contexts = new String[] { 
          "cube:Ctx-7", "cube:Ctx-7-mod", "cube:Ctx-7-inf",
          "cube:Ctx-8", "cube:Ctx-8-mod", "cube:Ctx-8-inf",
          "cube:Ctx-9", "cube:Ctx-9-mod", "cube:Ctx-9-inf",
          "cube:Ctx-10", "cube:Ctx-10-mod", "cube:Ctx-10-inf",
          "cube:Ctx-11", "cube:Ctx-11-mod", "cube:Ctx-11-inf",
          "cube:Ctx-12", "cube:Ctx-12-mod", "cube:Ctx-12-inf",
      };

      for (String context : contexts) {
        assertFalse(repositoryContainsResource(tempRepo, context));
      }
    }

    {
      String[] levels = new String[] { 
          "cube:Level_Aircraft_All", "cube:Level_Aircraft_Type", 
          "cube:Level_Aircraft_Model", "cube:Level_Location_All", "cube:Level_Location_Region", 
          "cube:Level_Location_Segment", "cube:Level_Date_All", "cube:Level_Date_Year", 
          "cube:Level_Date_Month" 
      };

      for (String level : levels) {
        assertTrue(repositoryContainsLevel(tempRepo, level));
      }
    }

    {
      String[] members = new String[] { 
          "cube:Level_Aircraft_All-All", "cube:Level_Aircraft_Type-FixedWing", 
          "cube:Level_Aircraft_Model-A380",
          "cube:Level_Location_All-All", "cube:Level_Location_Region-LOVV", "cube:Level_Location_Segment-LOWW",
          "cube:Level_Location_Segment-LOWL", 
          "cube:Level_Date_All-All", "cube:Level_Date_Year-2020",
          "cube:Level_Date_Month-January2020", "cube:Level_Date_Month-February2020", 
          "cube:Level_Date_Day-12January2020", "cube:Level_Date_Day-13January2020", 
          "cube:Level_Date_Day-12February2020", "cube:Level_Date_Day-13February2020" 
      };

      String[] dimensions = new String[] { 
          "cube:Aircraft", "cube:Aircraft", "cube:Aircraft",
          "cube:Location", "cube:Location", "cube:Location", "cube:Location",
          "cube:Date", "cube:Date", "cube:Date", "cube:Date", "cube:Date", "cube:Date", "cube:Date", "cube:Date" 
      };

      for (int i = 0; i < members.length; i++) {
        String member = members[i];
        String dimension = dimensions[i];
        assertTrue(repositoryContainsDimensionMember(tempRepo, member, dimension));
      }
    }

    {
      String[] resources = new String[] { 
          "cube:Level_Aircraft_Type-RotaryWing", "cube:Level_Aircraft_Model-EC145",
          "cube:Level_Location_Region-LIMM", "cube:Level_Location_Segment-LIMC", 
          "cube:Level_Location_Segment-LIMF" 
      };

      for (String resource : resources) {
        assertFalse(repositoryContainsResource(tempRepo, resource));
      }
    }

    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-0"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-1"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-2"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-3"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-4"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-5"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-6"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-1", "olap:covers", "cube:Ctx-1"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-1", "olap:covers", "cube:Ctx-2"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-1", "olap:covers", "cube:Ctx-3"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-1", "olap:covers", "cube:Ctx-4"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-1", "olap:covers", "cube:Ctx-5"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-1", "olap:covers", "cube:Ctx-6"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-2", "olap:covers", "cube:Ctx-2"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-3", "olap:covers", "cube:Ctx-3"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-3", "olap:covers", "cube:Ctx-4"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-3", "olap:covers", "cube:Ctx-5"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-3", "olap:covers", "cube:Ctx-6"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-4", "olap:covers", "cube:Ctx-4"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-4", "olap:covers", "cube:Ctx-5"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-5", "olap:covers", "cube:Ctx-5"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-6", "olap:covers", "cube:Ctx-6"));
  }

  
  @Test 
  public void testAircraftRotaryWingSegmentLIMCDate12February2020Selection() { 
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository(); 
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
    
    HashMap<String,String> rotaryWingLimc12February2020Slice = new HashMap<String,String>();
    rotaryWingLimc12February2020Slice.put("cube:hasAircraft","cube:Level_Aircraft_Type-RotaryWing");
    rotaryWingLimc12February2020Slice.put("cube:hasLocation", "cube:Level_Location_Segment-LIMC");
    rotaryWingLimc12February2020Slice.put("cube:hasDate", "cube:Level_Date_Day-12February2020");
    rotaryWingLimc12February2020Slice.put("cube:hasImportance", "cube:Level_Importance_All-All");
    
    this.getKGOLAPCube().sliceDice(rotaryWingLimc12February2020Slice);
    
    
    // The target repository should contain the definitions of 
    // cube:Ctx-0, cube:Ctx-7, and cube:Ctx-9 contexts. 
    { 
      String[] contexts = new String[] { 
        "cube:Ctx-0", "cube:Ctx-7", "cube:Ctx-9"  
      };
    
      Map<String, String> map; List<Map<String, String>> coordinateMaps = new
      ArrayList<Map<String,String>>();
      
      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_All-All ");
      map.put("cube:hasLocation", "cube:Level_Location_All-All");
      map.put("cube:hasDate", "cube:Level_Date_All-All");
      map.put("cube:hasImportance", "cube:Level_Importance_All-All");
      coordinateMaps.add(map);
  
      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_All-All ");
      map.put("cube:hasLocation", "cube:Level_Location_Region-LIMM");
      map.put("cube:hasDate", "cube:Level_Date_Year-2020");
      map.put("cube:hasImportance", "cube:Level_Importance_All-All");
      coordinateMaps.add(map);
  
      map = new HashMap<String, String>();
      map.put("cube:hasAircraft", "cube:Level_Aircraft_All-All ");
      map.put("cube:hasLocation", "cube:Level_Location_Segment-LIMC");
      map.put("cube:hasDate", "cube:Level_Date_Year-2020");
      map.put("cube:hasImportance", "cube:Level_Importance_All-All");
      coordinateMaps.add(map);
      
      String[] modules = new String[] { "cube:Ctx-0-mod", "cube:Ctx-7-mod", "cube:Ctx-9-mod" };
      
      String[] inferredModules = new String[] { "cube:Ctx-0-inf", "cube:Ctx-7-inf", "cube:Ctx-9-inf" };
      
      for(int i = 0; i < contexts.length; i++) { 
        String context = contexts[i];
        Map<String, String> coordinates = coordinateMaps.get(i); String module =
        modules[i]; String inferred = inferredModules[i];
        
        assertTrue(repositoryContainsContext(tempRepo, context, coordinates, module, inferred)); 
      }
    }
    
    { 
      // Modules of selected contexts should contain same number of triples that
      // they also contain in the source repository. 
      String[] modules = new String[] { 
          "cube:Ctx-0-mod", "cube:Ctx-0-inf", 
          "cube:Ctx-7-mod", "cube:Ctx-7-inf" , 
          "cube:Ctx-9-mod", "cube:Ctx-9-inf" 
      };
      
      for(String module : modules) { 
        assertTrue(this.moduleContainsSameNumberOfTriplesInRepositories(module, baseRepo, tempRepo)); 
      } 
    }
    
    { 
      // The other contexts shouldn't be contained in the result. 
      String[] contexts = new String[] { 
          "cube:Ctx-1", "cube:Ctx-1-mod", "cube:Ctx-1-inf",
          "cube:Ctx-2", "cube:Ctx-2-mod", "cube:Ctx-2-inf",
          "cube:Ctx-3", "cube:Ctx-3-mod", "cube:Ctx-3-inf",
          "cube:Ctx-4", "cube:Ctx-4-mod", "cube:Ctx-4-inf",
          "cube:Ctx-5", "cube:Ctx-5-mod", "cube:Ctx-5-inf",
          "cube:Ctx-6", "cube:Ctx-6-mod", "cube:Ctx-6-inf",
          "cube:Ctx-8", "cube:Ctx-8-mod", "cube:Ctx-8-inf",
          "cube:Ctx-10", "cube:Ctx-10-mod", "cube:Ctx-10-inf",
          "cube:Ctx-11", "cube:Ctx-11-mod", "cube:Ctx-11-inf",
          "cube:Ctx-12", "cube:Ctx-12-mod", "cube:Ctx-12-inf",
      };
    
      for(String context : contexts) {
        assertFalse(repositoryContainsResource(tempRepo, context)); 
      } 
    }
    
    {
      String[] levels = new String[] { 
          "cube:Level_Aircraft_All", "cube:Level_Aircraft_Type", 
          "cube:Level_Aircraft_Model", "cube:Level_Location_All", "cube:Level_Location_Region", 
          "cube:Level_Location_Segment", "cube:Level_Date_All", "cube:Level_Date_Year", 
          "cube:Level_Date_Month" 
      };

      for (String level : levels) {
        assertTrue(repositoryContainsLevel(tempRepo, level));
      }
    }
    
    {
      String[] members = new String[] { 
          "cube:Level_Aircraft_All-All", "cube:Level_Aircraft_Type-RotaryWing", "cube:Level_Aircraft_Model-EC145",
          "cube:Level_Location_All-All", "cube:Level_Location_Region-LIMM", "cube:Level_Location_Segment-LIMC", 
          "cube:Level_Date_All-All", "cube:Level_Date_Year-2020",
           "cube:Level_Date_Month-February2020", "cube:Level_Date_Day-12February2020" 
      };

      String[] dimensions = new String[] { 
          "cube:Aircraft", "cube:Aircraft", "cube:Aircraft",
          "cube:Location", "cube:Location", "cube:Location",
          "cube:Date", "cube:Date", "cube:Date", "cube:Date"
      };

      for (int i = 0; i < members.length; i++) {
        String member = members[i];
        String dimension = dimensions[i];
        assertTrue(repositoryContainsDimensionMember(tempRepo, member, dimension));
      }
    }
    
    { 
      String[] resources = new String[] { 
          "cube:Level_Aircraft_Type-FixedWing", "cube:Level_Aircraft_Model-A380",
          "cube:Level_Location_Region-LOVV", "cube:Level_Location_Segment-LOWW", 
          "cube:Level_Location_Segment-LOWL", "cube:Level_Date_Month-January2020", 
          "cube:Level_Date_Day-13February2020",
          "cube:Level_Date_Day-12January2020",
          "cube:Level_Date_Day-13January2020",
      };
    
      for(String resource : resources) {
        assertFalse(repositoryContainsResource(tempRepo, resource)); 
      } 
    }

    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-0"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-7"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-0", "olap:covers", "cube:Ctx-9"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-7", "olap:covers", "cube:Ctx-7"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-7", "olap:covers", "cube:Ctx-9"));
    assertTrue(repositoryContainsQuad(tempRepo, "<ckr:global-inf>", "cube:Ctx-9", "olap:covers", "cube:Ctx-9"));
  }
}
