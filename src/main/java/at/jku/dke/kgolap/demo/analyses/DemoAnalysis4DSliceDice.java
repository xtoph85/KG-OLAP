package at.jku.dke.kgolap.demo.analyses;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.KGOLAPCube;

public class DemoAnalysis4DSliceDice extends DemoAnalysis {
  private static final Logger logger = LoggerFactory.getLogger(DemoAnalysis4DSliceDice.class);

  @Override
  public void run() {
    KGOLAPCube cube = this.getCube();
    
    logger.info("Getting a dice coordinate.");
    
    String sparql = cube.getSparqlPrefixes()
        + "SELECT ?aircraft ?location ?date ?importance WHERE {\n"
        + "  GRAPH ckr:global{\n"
        + "    ?ctx cube:hasAircraft ?aircraft .\n"
        + "    ?aircraft olap:atLevel cube:Level_Aircraft_All .\n"
        + "    \n"
        + "    ?ctx cube:hasLocation ?location .\n"
        + "    ?location olap:atLevel cube:Level_Location_Region .\n"
        + "    \n"
        + "    ?ctx cube:hasDate ?date .\n"
        + "    ?date olap:atLevel cube:Level_Date_All .\n"
        + "    \n"
        + "    ?ctx cube:hasImportance ?importance .\n"
        + "    ?importance olap:atLevel cube:Level_Importance_All .\n"
        + "  }\n"
        + "}\n"
        + "LIMIT 1";
    
    String result = cube.getBaseRepository().executeTupleQuery(sparql);
    
    String[] coordinates = result.split("\n")[1].split(",");
    
    Map<String, String> diceCoordinates = new HashMap<String,String>();
    
    diceCoordinates.put("cube:hasAircraft", "<" + coordinates[0].trim() + ">");
    diceCoordinates.put("cube:hasLocation", "<" + coordinates[1].trim() + ">");
    diceCoordinates.put("cube:hasDate", "<" + coordinates[2].trim() + ">");
    diceCoordinates.put("cube:hasImportance", "<" + coordinates[3].trim() + ">");
    
    this.getCube().setBenchmarkingMode(true);
    this.getCube().setExecuteInMemory(true);
    
    cube.sliceDice(diceCoordinates);
  }

}
