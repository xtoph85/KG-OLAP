package at.jku.dke.kgolap.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.KGOLAPCubeProperties;
import at.jku.dke.kgolap.repo.Repo;
import at.jku.dke.kgolap.repo.RepoProperties;

public class SliceDice3DDemoDatasetTest extends OperatorTest {
  private static final Logger logger = LoggerFactory.getLogger(SliceDice3DDemoDatasetTest.class);

  public SliceDice3DDemoDatasetTest() {
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
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("knowledge/demo-3D.trig")) {
      logger.info("Adding demo dataset ...");
      this.getKGOLAPCube().add(in);
      logger.info("Demo dataset added.");
    } catch (IOException e) {
      logger.error("Error reading file.", e);
    }
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
    
    this.getKGOLAPCube().setExecuteInMemory(true);
    
    this.getKGOLAPCube().sliceDice(allSlice);

    assertTrue(repositoriesContainSameNumberOfTriples(baseRepo, tempRepo));
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?air ?loc ?dat (COUNT(?ctx) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx cube:hasAircraft ?air .\r\n" + 
          "                ?ctx cube:hasLocation ?loc .\r\n" + 
          "                ?ctx cube:hasDate ?dat .\r\n" + 
          "            }\r\n" + 
          "        } \r\n" + 
          "        GROUP BY ?air ?loc ?dat\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt > 1)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?val ?superLvl (COUNT(?superVal) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?val olap:atLevel ?lvl .\r\n" + 
          "            }\r\n" + 
          "            OPTIONAL {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?lvl olap:directlyRollsUpTo* ?superLvl .\r\n" + 
          "                    ?val olap:directlyRollsUpTo* ?superVal .\r\n" + 
          "                    ?superVal olap:atLevel ?superLvl .\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        } GROUP BY ?val ?superLvl\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt != 1)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(DISTINCT ?val) AS ?cnt1) WHERE {\r\n" + 
          "            {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasAircraft ?val .\r\n" + 
          "                }\r\n" + 
          "            } UNION {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasLocation ?val .\r\n" + 
          "                }\r\n" + 
          "            } UNION {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasDate ?val .\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(DISTINCT ?val) AS ?cnt2) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?val olap:atLevel ?lvl .\r\n" + 
          "            }\r\n" + 
          "            OPTIONAL {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?lvl olap:directlyRollsUpTo* ?superLvl .\r\n" + 
          "                    ?val olap:directlyRollsUpTo* ?superVal .\r\n" + 
          "                    ?superVal olap:atLevel ?superLvl .\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt1 = 0 || ?cnt2 = 0 || ?cnt1 != ?cnt2)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }

    // 
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(DISTINCT ?val) AS ?cnt1) WHERE {\r\n" + 
          "            {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasAircraft ?val .\r\n" + 
          "                }\r\n" + 
          "            } UNION {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasLocation ?val .\r\n" + 
          "                }\r\n" + 
          "            } UNION {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasDate ?val .\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(DISTINCT ?val) AS ?cnt2) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?val olap:atLevel ?lvl .\r\n" + 
          "            }\r\n" + 
          "            OPTIONAL {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?lvl olap:directlyRollsUpTo* ?superLvl .\r\n" + 
          "                    ?val olap:directlyRollsUpTo* ?superVal .\r\n" + 
          "                    ?superVal olap:atLevel ?superLvl .\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt1 = 0 || ?cnt2 = 0 || ?cnt1 != ?cnt2)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT DISTINCT ?runwayTaxiway WHERE { \r\n" + 
          "        GRAPH ?g {\r\n" + 
          "            ?runwayTaxiway obj:isSituatedAt ?airport .\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            SELECT ?runwayTaxiway WHERE {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                    ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                    ?ctx cube:hasDate/olap:atLevel cube:Level_Date_All .\r\n" + 
          "                    ?ctx ckr:hasAssertedModule ?mod .\r\n" + 
          "                }\r\n" + 
          "                {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?runwayTaxiway rdf:type obj:Runway .\r\n" + 
          "                    }\r\n" + 
          "                } UNION {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?runwayTaxiway rdf:type obj:Taxiway .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT DISTINCT ?runwayTaxiway WHERE { \r\n" + 
          "        GRAPH ?g {\r\n" + 
          "            ?runwayTaxiway obj:isSituatedAt ?airport .\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            SELECT ?airport WHERE {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                    ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                    ?ctx cube:hasDate/olap:atLevel cube:Level_Date_All .\r\n" + 
          "                    ?ctx ckr:hasAssertedModule ?mod .\r\n" + 
          "                }\r\n" + 
          "                {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?airport rdf:type obj:Airport .\r\n" + 
          "                    }\r\n" + 
          "                } UNION {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?airport rdf:type obj:Airport .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?runwayTaxiway (COUNT(?airport) AS ?cntAirport) WHERE {\r\n" + 
          "            GRAPH ?g {\r\n" + 
          "                ?runwayTaxiway obj:isSituatedAt ?airport .\r\n" + 
          "            }\r\n" + 
          "        } GROUP BY ?runwayTaxiway\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cntAirport != 1)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT ?vor WHERE {\r\n" + 
          "        {\r\n" + 
          "            GRAPH ?g1 {\r\n" + 
          "                ?vor obj:longitude ?long .\r\n" + 
          "            }\r\n" + 
          "        } UNION {\r\n" + 
          "            GRAPH ?g2 {\r\n" + 
          "                ?vor obj:latitude ?lat .\r\n" + 
          "            }\r\n" + 
          "        } UNION {\r\n" + 
          "            GRAPH ?g3 {\r\n" + 
          "                ?vor obj:frequency ?freq .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            SELECT ?vor WHERE {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                    ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                    ?ctx cube:hasDate/olap:atLevel cube:Level_Date_All .\r\n" + 
          "                    ?ctx ckr:hasAssertedModule ?mod .\r\n" + 
          "                }\r\n" + 
          "                {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?vor rdf:type obj:VOR .\r\n" + 
          "                    }\r\n" + 
          "                } UNION {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?vor rdf:type obj:VOR .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            } \r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?vor ?loc ?date (COUNT(?freq) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx cube:hasAircraft ?air .\r\n" + 
          "                ?air olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                ?ctx cube:hasLocation ?loc .\r\n" + 
          "                ?loc olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                ?ctx cube:hasDate ?date .\r\n" + 
          "                ?date olap:atLevel cube:Level_Date_Year .\r\n" + 
          "                ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "            }\r\n" + 
          "            GRAPH ?m {\r\n" + 
          "                ?vor obj:frequency ?freq .\r\n" + 
          "            }\r\n" + 
          "        } GROUP BY ?vor ?loc ?date\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt != 1)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?vor ?loc (COUNT(?freq) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx cube:hasAircraft ?air .\r\n" + 
          "                ?air olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                ?ctx cube:hasLocation ?loc .\r\n" + 
          "                ?loc olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                ?ctx cube:hasDate ?date .\r\n" + 
          "                ?date olap:atLevel cube:Level_Date_Year .\r\n" + 
          "                ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "            }\r\n" + 
          "            GRAPH ?m {\r\n" + 
          "                ?vor obj:frequency ?freq .\r\n" + 
          "            }\r\n" + 
          "        } GROUP BY ?vor ?loc\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt != 2)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT DISTINCT ?vor WHERE {\r\n" + 
          "        GRAPH ?g {\r\n" + 
          "            ?vor rdf:type obj:VOR .\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            GRAPH ?g1 {\r\n" + 
          "                ?vor obj:frequency ?freq .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT DISTINCT ?vor WHERE {\r\n" + 
          "        GRAPH ?g {\r\n" + 
          "            ?vor obj:frequency ?freq .\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            GRAPH ?g1 {\r\n" + 
          "                ?vor rdf:type ?x .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      assertFalse(result);
      
      result = baseRepo.ask(sparql);
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?vor ?air1 ?air2 ?loc1 ?loc2 ?date1 ?date2 WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx1 ckr:hasAssertedModule ?m1 .\r\n" + 
          "                ?ctx2 ckr:hasAssertedModule ?m2 .\r\n" + 
          "                ?ctx1 cube:hasAircraft ?air1 .\r\n" + 
          "                ?ctx1 cube:hasLocation ?loc1 .\r\n" + 
          "                ?ctx1 cube:hasDate ?date1 .\r\n" + 
          "                ?ctx2 cube:hasAircraft ?air2 .\r\n" + 
          "                ?ctx2 cube:hasLocation ?loc2 .\r\n" + 
          "                ?ctx2 cube:hasDate ?date2 .\r\n" + 
          "                ?air2 olap:directlyRollsUpTo* ?air1 .\r\n" + 
          "                ?loc2 olap:directlyRollsUpTo* ?loc1 .\r\n" + 
          "                ?date2 olap:directlyRollsUpTo* ?date1 .\r\n" + 
          "            }\r\n" + 
          "            GRAPH ?m1 {\r\n" + 
          "                ?vor rdf:type obj:VOR .\r\n" + 
          "                ?vor obj:longitude ?long .\r\n" + 
          "                ?vor obj:latitude ?lat .\r\n" + 
          "            }\r\n" + 
          "            GRAPH ?m2 {\r\n" + 
          "                ?vor obj:frequency ?freq .\r\n" + 
          "            }\r\n" + 
          "        } ORDER BY ?vor\r\n" + 
          "    }\r\n" + 
          "    FILTER(?air1 != ?air2)\r\n" + 
          "    FILTER(?loc1 != ?loc2)\r\n" + 
          "    FILTER(?date1 = ?date2)\r\n" + 
          "}";
        
      boolean result = baseRepo.ask(sparql);
      assertFalse(result);
      
      result = tempRepo.ask(sparql);
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT DISTINCT ?runwayTaxiway WHERE {\r\n" + 
          "            GRAPH ?g {\r\n" + 
          "                ?runwayTaxiway obj:availability ?availability .\r\n" + 
          "            }\r\n" + 
          "            MINUS {\r\n" + 
          "                {\r\n" + 
          "                    GRAPH ?g1 {\r\n" + 
          "                        ?runwayTaxiway rdf:type obj:Runway .\r\n" + 
          "                    }\r\n" + 
          "                } UNION {\r\n" + 
          "                    GRAPH ?g2 {\r\n" + 
          "                        ?runwayTaxiway rdf:type obj:Taxiway .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = baseRepo.ask(sparql);
      assertFalse(result);
      
      result = tempRepo.ask(sparql);
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ((?cntTaxiway + ?cntRunway) AS ?cntRunwayTaxiway1) ?cntRunwayTaxiway WHERE {\r\n" + 
          "            {\r\n" + 
          "                SELECT (COUNT(*) AS ?cntTaxiway) {\r\n" + 
          "                    {\r\n" + 
          "                        SELECT DISTINCT ?taxiway ?availability ?air1 ?air2 ?loc1 ?loc2 ?date1 ?date2 WHERE {\r\n" + 
          "                            GRAPH ckr:global {\r\n" + 
          "                                ?ctx1 ckr:hasAssertedModule ?m1 .\r\n" + 
          "                                ?ctx2 ckr:hasAssertedModule ?m2 .\r\n" + 
          "                                ?ctx1 cube:hasAircraft ?air1 .\r\n" + 
          "                                ?ctx1 cube:hasLocation ?loc1 .\r\n" + 
          "                                ?ctx1 cube:hasDate ?date1 .\r\n" + 
          "                                ?ctx2 cube:hasAircraft ?air2 .\r\n" + 
          "                                ?ctx2 cube:hasLocation ?loc2 .\r\n" + 
          "                                ?ctx2 cube:hasDate ?date2 .\r\n" + 
          "                                ?air2 olap:directlyRollsUpTo* ?air1 .\r\n" + 
          "                                ?loc2 olap:directlyRollsUpTo* ?loc1 .\r\n" + 
          "                                ?date2 olap:directlyRollsUpTo* ?date1 .\r\n" + 
          "                            }\r\n" + 
          "                            GRAPH ?m1 {\r\n" + 
          "                                ?taxiway rdf:type obj:Taxiway .\r\n" + 
          "                            }\r\n" + 
          "                            GRAPH ?m2 {\r\n" + 
          "                                ?taxiway obj:availability ?availability .\r\n" + 
          "                            }\r\n" + 
          "                        } ORDER BY ?taxiway\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "            {\r\n" + 
          "                SELECT (COUNT(*) AS ?cntRunway) {\r\n" + 
          "                    {\r\n" + 
          "                        SELECT DISTINCT ?runway ?availability ?air1 ?air2 ?loc1 ?loc2 ?date1 ?date2 WHERE {\r\n" + 
          "                            GRAPH ckr:global {\r\n" + 
          "                                ?ctx1 ckr:hasAssertedModule ?m1 .\r\n" + 
          "                                ?ctx2 ckr:hasAssertedModule ?m2 .\r\n" + 
          "                                ?ctx1 cube:hasAircraft ?air1 .\r\n" + 
          "                                ?ctx1 cube:hasLocation ?loc1 .\r\n" + 
          "                                ?ctx1 cube:hasDate ?date1 .\r\n" + 
          "                                ?ctx2 cube:hasAircraft ?air2 .\r\n" + 
          "                                ?ctx2 cube:hasLocation ?loc2 .\r\n" + 
          "                                ?ctx2 cube:hasDate ?date2 .\r\n" + 
          "                                ?air2 olap:directlyRollsUpTo* ?air1 .\r\n" + 
          "                                ?loc2 olap:directlyRollsUpTo* ?loc1 .\r\n" + 
          "                                ?date2 olap:directlyRollsUpTo* ?date1 .\r\n" + 
          "                            }\r\n" + 
          "                            GRAPH ?m1 {\r\n" + 
          "                                ?runway rdf:type obj:Runway .\r\n" + 
          "                            }\r\n" + 
          "                            GRAPH ?m2 {\r\n" + 
          "                                ?runway obj:availability ?availability .\r\n" + 
          "                            }\r\n" + 
          "                        } ORDER BY ?runway\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "            {\r\n" + 
          "                SELECT (COUNT(*) AS ?cntRunwayTaxiway) {\r\n" + 
          "                    {\r\n" + 
          "                        SELECT DISTINCT ?runwayTaxiway ?availability WHERE {\r\n" + 
          "                            GRAPH ?m2 {\r\n" + 
          "                                ?runwayTaxiway obj:availability ?availability .\r\n" + 
          "                            }\r\n" + 
          "                        } ORDER BY ?runwayTaxiway\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cntRunwayTaxiway1 != ?cntRunwayTaxiway)\r\n" + 
          "}";
        
      boolean result = baseRepo.ask(sparql);
      assertFalse(result);
      
      result = tempRepo.ask(sparql);
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT * WHERE {\r\n" + 
          "        GRAPH ?g {\r\n" + 
          "            ?runwayTaxiway obj:isSituatedAt ?airport .\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            GRAPH ?g {\r\n" + 
          "                ?airport rdf:type obj:Airport .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = baseRepo.ask(sparql);
      assertFalse(result);
      
      result = tempRepo.ask(sparql);
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(*) AS ?cntRunwayTaxiway1) WHERE {\r\n" + 
          "            {\r\n" + 
          "                SELECT DISTINCT ?runwayTaxiway WHERE {\r\n" + 
          "                    GRAPH ckr:global {\r\n" + 
          "                        ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "                        ?ctx cube:hasAircraft ?air .\r\n" + 
          "                        ?ctx cube:hasLocation ?loc .\r\n" + 
          "                        ?ctx cube:hasDate ?dat .\r\n" + 
          "                        ?air olap:atLevel ?airLevel .\r\n" + 
          "                        ?loc olap:atLevel ?locLevel .\r\n" + 
          "                        ?dat olap:atLevel ?datLevel .\r\n" + 
          "                        ?ctx1 ckr:hasAssertedModule ?m1 .\r\n" + 
          "                        ?ctx1 cube:hasAircraft ?air1 .\r\n" + 
          "                        ?ctx1 cube:hasLocation ?loc1 .\r\n" + 
          "                        ?ctx1 cube:hasDate ?dat1 .\r\n" + 
          "                        ?air olap:directlyRollsUpTo* ?air1 .\r\n" + 
          "                        ?loc olap:directlyRollsUpTo* ?loc1 .\r\n" + 
          "                        ?dat olap:directlyRollsUpTo* ?dat1 .\r\n" + 
          "                    }\r\n" + 
          "                    GRAPH ?m {\r\n" + 
          "                        ?runwayTaxiway obj:contaminant ?contaminant .\r\n" + 
          "                    }\r\n" + 
          "                    GRAPH ?m1 {\r\n" + 
          "                        {\r\n" + 
          "                            ?runwayTaxiway rdf:type obj:Runway .\r\n" + 
          "                        } UNION {\r\n" + 
          "                            ?runwayTaxiway rdf:type obj:Taxiway .\r\n" + 
          "                        }\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(*) AS ?cntRunwayTaxiway2) WHERE {\r\n" + 
          "            {\r\n" + 
          "                SELECT DISTINCT ?runwayTaxiway WHERE {\r\n" + 
          "                    GRAPH ?m {\r\n" + 
          "                        ?runwayTaxiway obj:contaminant ?contaminarrnt .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cntRunwayTaxiway1 != ?cntRunwayTaxiway2)\r\n" + 
          "}";
        
      boolean result = baseRepo.ask(sparql);
      assertFalse(result);
      
      result = tempRepo.ask(sparql);
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(*) AS ?cnt1) WHERE {\r\n" + 
          "            GRAPH <ckr:global-inf> {\r\n" + 
          "                ?ctx1 olap:covers ?ctx2 .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    {\r\n" + 
          "        SELECT (COUNT(*) AS ?cnt2) WHERE {\r\n" + 
          "            GRAPH <ckr:global-inf> {\r\n" + 
          "                ?ctx1 olap:covers ?ctx2 .\r\n" + 
          "            }\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx1 cube:hasAircraft ?air1 .\r\n" + 
          "                ?ctx1 cube:hasLocation ?loc1 .\r\n" + 
          "                ?ctx1 cube:hasDate ?dat1 .\r\n" + 
          "                ?ctx2 cube:hasAircraft ?air2 .\r\n" + 
          "                ?ctx2 cube:hasLocation ?loc2 .\r\n" + 
          "                ?ctx2 cube:hasDate ?dat2 .\r\n" + 
          "                ?air2 olap:directlyRollsUpTo* ?air1 .\r\n" + 
          "                ?loc2 olap:directlyRollsUpTo* ?loc1 .\r\n" + 
          "                ?dat2 olap:directlyRollsUpTo* ?dat1 .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt1 != ?cnt2)\r\n" + 
          "}";
        
      boolean result = baseRepo.ask(sparql);
      assertFalse(result);
      
      result = tempRepo.ask(sparql);
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT ?ctx (COUNT(?coveredCtx) AS ?cnt) WHERE {\r\n" + 
          "        GRAPH ckr:global {\r\n" + 
          "            ?ctx rdf:type olap:Cell .\r\n" + 
          "            ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "            ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "            ?ctx cube:hasDate/olap:atLevel cube:Level_Date_All .\r\n" + 
          "            ?coveredCtx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "            ?coveredCtx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "            ?coveredCtx cube:hasDate/olap:atLevel cube:Level_Date_Year .        \r\n" + 
          "        }\r\n" + 
          "        GRAPH <ckr:global-inf> {\r\n" + 
          "            ?ctx olap:covers ?coveredCtx .\r\n" + 
          "        }\r\n" + 
          "    } GROUP BY ?ctx\r\n" + 
          "    HAVING (COUNT(?coveredCtx) != 2)\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT ?ctx (COUNT(?coveredCtx) AS ?cnt) WHERE {\r\n" + 
          "        GRAPH ckr:global {\r\n" + 
          "            ?ctx rdf:type olap:Cell .\r\n" + 
          "            ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "            ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "            ?ctx cube:hasDate/olap:atLevel cube:Level_Date_Year .\r\n" + 
          "            ?coveredCtx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "            ?coveredCtx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "            ?coveredCtx cube:hasDate/olap:atLevel cube:Level_Date_Month .        \r\n" + 
          "        }\r\n" + 
          "        GRAPH <ckr:global-inf> {\r\n" + 
          "            ?ctx olap:covers ?coveredCtx .\r\n" + 
          "        }\r\n" + 
          "    } GROUP BY ?ctx\r\n" + 
          "    HAVING (COUNT(?coveredCtx) != 2)\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT ?ctx (COUNT(?coveredCtx) AS ?cnt) WHERE {\r\n" + 
          "        GRAPH ckr:global {\r\n" + 
          "            ?ctx rdf:type olap:Cell .\r\n" + 
          "            ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "            ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "            ?ctx cube:hasDate/olap:atLevel cube:Level_Date_Month .\r\n" + 
          "            ?coveredCtx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "            ?coveredCtx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "            ?coveredCtx cube:hasDate/olap:atLevel cube:Level_Date_Day .        \r\n" + 
          "        }\r\n" + 
          "        GRAPH <ckr:global-inf> {\r\n" + 
          "            ?ctx olap:covers ?coveredCtx .\r\n" + 
          "        }\r\n" + 
          "    } GROUP BY ?ctx\r\n" + 
          "    HAVING (COUNT(?coveredCtx) != 2)\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT ?ctx (COUNT(?coveredCtx) AS ?cnt) WHERE {\r\n" + 
          "        GRAPH ckr:global {\r\n" + 
          "            ?ctx rdf:type olap:Cell .\r\n" + 
          "            ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Type .\r\n" + 
          "            ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "            ?ctx cube:hasDate/olap:atLevel cube:Level_Date_Day .\r\n" + 
          "            ?coveredCtx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_Model .\r\n" + 
          "            ?coveredCtx cube:hasLocation/olap:atLevel cube:Level_Location_Segment .\r\n" + 
          "            ?coveredCtx cube:hasDate/olap:atLevel cube:Level_Date_Day .        \r\n" + 
          "        }\r\n" + 
          "        GRAPH <ckr:global-inf> {\r\n" + 
          "            ?ctx olap:covers ?coveredCtx .\r\n" + 
          "        }\r\n" + 
          "    } GROUP BY ?ctx\r\n" + 
          "    HAVING (COUNT(?coveredCtx) != 2)\r\n" + 
          "}";
      
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
  }
  
  @Test
  public void testRegionSelection() {
    Repo baseRepo = this.getKGOLAPCube().getBaseRepository();
    Repo tempRepo = this.getKGOLAPCube().getTempRepository();
    
    String[] diceCoordinates = null;
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes()
          + "SELECT ?aircraft ?location ?date WHERE {\n"
          + "  GRAPH ckr:global{\n"
          + "    ?ctx cube:hasAircraft ?aircraft .\n"
          + "    ?aircraft olap:atLevel cube:Level_Aircraft_All .\n"
          + "    \n"
          + "    ?ctx cube:hasLocation ?location .\n"
          + "    ?location olap:atLevel cube:Level_Location_Region .\n"
          + "    \n"
          + "    ?ctx cube:hasDate ?date .\n"
          + "    ?date olap:atLevel cube:Level_Date_All .\n"
          + "  }\n"
          + "}\n"
          + "LIMIT 1";
      
      String result = baseRepo.executeTupleQuery(sparql);
      
      diceCoordinates = result.split("\n")[1].split(",");
      
      Map<String, String> regionSlice = new HashMap<String,String>();
      
      regionSlice.put("cube:hasAircraft", "<" + diceCoordinates[0].trim() + ">");
      regionSlice.put("cube:hasLocation", "<" + diceCoordinates[1].trim() + ">");
      regionSlice.put("cube:hasDate", "<" + diceCoordinates[2].trim() + ">");
      
      this.getKGOLAPCube().setExecuteInMemory(true);
      
      this.getKGOLAPCube().sliceDice(regionSlice);
    }
        
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?air ?loc ?dat (COUNT(?ctx) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx cube:hasAircraft ?air .\r\n" + 
          "                ?ctx cube:hasLocation ?loc .\r\n" + 
          "                ?ctx cube:hasDate ?dat .\r\n" + 
          "            }\r\n" + 
          "        } \r\n" + 
          "        GROUP BY ?air ?loc ?dat\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt > 1)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?val ?superLvl (COUNT(?superVal) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?val olap:atLevel ?lvl .\r\n" + 
          "            }\r\n" + 
          "            OPTIONAL {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?lvl olap:directlyRollsUpTo* ?superLvl .\r\n" + 
          "                    ?val olap:directlyRollsUpTo* ?superVal .\r\n" + 
          "                    ?superVal olap:atLevel ?superLvl .\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        } GROUP BY ?val ?superLvl\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt != 1)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT DISTINCT ?runwayTaxiway WHERE { \r\n" + 
          "        GRAPH ?g {\r\n" + 
          "            ?runwayTaxiway obj:isSituatedAt ?airport .\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            SELECT ?runwayTaxiway WHERE {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                    ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                    ?ctx cube:hasDate/olap:atLevel cube:Level_Date_All .\r\n" + 
          "                    ?ctx ckr:hasAssertedModule ?mod .\r\n" + 
          "                }\r\n" + 
          "                {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?runwayTaxiway rdf:type obj:Runway .\r\n" + 
          "                    }\r\n" + 
          "                } UNION {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?runwayTaxiway rdf:type obj:Taxiway .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT DISTINCT ?runwayTaxiway WHERE { \r\n" + 
          "        GRAPH ?g {\r\n" + 
          "            ?runwayTaxiway obj:isSituatedAt ?airport .\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            SELECT ?airport WHERE {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                    ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                    ?ctx cube:hasDate/olap:atLevel cube:Level_Date_All .\r\n" + 
          "                    ?ctx ckr:hasAssertedModule ?mod .\r\n" + 
          "                }\r\n" + 
          "                {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?airport rdf:type obj:Airport .\r\n" + 
          "                    }\r\n" + 
          "                } UNION {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?airport rdf:type obj:Airport .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?runwayTaxiway (COUNT(?airport) AS ?cntAirport) WHERE {\r\n" + 
          "            GRAPH ?g {\r\n" + 
          "                ?runwayTaxiway obj:isSituatedAt ?airport .\r\n" + 
          "            }\r\n" + 
          "        } GROUP BY ?runwayTaxiway\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cntAirport != 1)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT ?vor WHERE {\r\n" + 
          "        {\r\n" + 
          "            GRAPH ?g1 {\r\n" + 
          "                ?vor obj:longitude ?long .\r\n" + 
          "            }\r\n" + 
          "        } UNION {\r\n" + 
          "            GRAPH ?g2 {\r\n" + 
          "                ?vor obj:latitude ?lat .\r\n" + 
          "            }\r\n" + 
          "        } UNION {\r\n" + 
          "            GRAPH ?g3 {\r\n" + 
          "                ?vor obj:frequency ?freq .\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            SELECT ?vor WHERE {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasAircraft/olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                    ?ctx cube:hasLocation/olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                    ?ctx cube:hasDate/olap:atLevel cube:Level_Date_All .\r\n" + 
          "                    ?ctx ckr:hasAssertedModule ?mod .\r\n" + 
          "                }\r\n" + 
          "                {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?vor rdf:type obj:VOR .\r\n" + 
          "                    }\r\n" + 
          "                } UNION {\r\n" + 
          "                    GRAPH ?mod {\r\n" + 
          "                        ?vor rdf:type obj:VOR .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            } \r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?vor ?loc ?date (COUNT(?freq) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx cube:hasAircraft ?air .\r\n" + 
          "                ?air olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                ?ctx cube:hasLocation ?loc .\r\n" + 
          "                ?loc olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                ?ctx cube:hasDate ?date .\r\n" + 
          "                ?date olap:atLevel cube:Level_Date_Year .\r\n" + 
          "                ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "            }\r\n" + 
          "            GRAPH ?m {\r\n" + 
          "                ?vor obj:frequency ?freq .\r\n" + 
          "            }\r\n" + 
          "        } GROUP BY ?vor ?loc ?date\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt != 1)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
    
    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    {\r\n" + 
          "        SELECT ?vor ?loc (COUNT(?freq) AS ?cnt) WHERE {\r\n" + 
          "            GRAPH ckr:global {\r\n" + 
          "                ?ctx cube:hasAircraft ?air .\r\n" + 
          "                ?air olap:atLevel cube:Level_Aircraft_All .\r\n" + 
          "                ?ctx cube:hasLocation ?loc .\r\n" + 
          "                ?loc olap:atLevel cube:Level_Location_Region .\r\n" + 
          "                ?ctx cube:hasDate ?date .\r\n" + 
          "                ?date olap:atLevel cube:Level_Date_Year .\r\n" + 
          "                ?ctx ckr:hasAssertedModule ?m .\r\n" + 
          "            }\r\n" + 
          "            GRAPH ?m {\r\n" + 
          "                ?vor obj:frequency ?freq .\r\n" + 
          "            }\r\n" + 
          "        } GROUP BY ?vor ?loc\r\n" + 
          "    }\r\n" + 
          "    FILTER(?cnt != 2)\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }

    {
      String sparql = this.getKGOLAPCube().getSparqlPrefixes() +
          "ASK {\r\n" + 
          "    SELECT ?ctx WHERE {\r\n" + 
          "        GRAPH ckr:global {\r\n" + 
          "            ?ctx cube:hasLocation ?location .\r\n" + 
          "        }\r\n" + 
          "        MINUS {\r\n" + 
          "            SELECT ?ctx WHERE {\r\n" + 
          "                GRAPH ckr:global {\r\n" + 
          "                    ?ctx cube:hasLocation ?location .\r\n" + 
          "                    {\r\n" + 
          "                        ?location olap:directlyRollsUpTo* <" + diceCoordinates[1] + "> .\r\n" + 
          "                    } UNION {\r\n" + 
          "                        <" + diceCoordinates[1] + "> olap:directlyRollsUpTo* ?location .\r\n" + 
          "                    }\r\n" + 
          "                }\r\n" + 
          "            }\r\n" + 
          "        }\r\n" + 
          "    }\r\n" + 
          "}";
        
      boolean result = tempRepo.ask(sparql);
      
      assertFalse(result);
    }
  }
}
