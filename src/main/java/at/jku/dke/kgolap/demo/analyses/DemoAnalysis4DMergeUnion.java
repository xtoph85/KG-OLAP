package at.jku.dke.kgolap.demo.analyses;

import java.util.HashMap;
import java.util.Map;

public class DemoAnalysis4DMergeUnion extends DemoAnalysis {

  @Override
  public void run() {
    Map<String, String> granularity = new HashMap<String,String>();

    granularity.put("cube:hasAircraft", "cube:Level_Aircraft_Type");
    granularity.put("cube:hasLocation", "cube:Level_Location_Region");
    granularity.put("cube:hasDate", "cube:Level_Date_All");
    granularity.put("cube:hasImportance", "cube:Level_Importance_All");
    
    this.getCube().setBenchmarkingMode(true);
    this.getCube().setExecuteInMemory(true);
    
    this.getCube().mergeUnion(granularity);
  }

}
