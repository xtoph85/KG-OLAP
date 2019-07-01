package at.jku.dke.kgolap.demo.analyses;

import java.util.HashMap;
import java.util.Map;

public class DemoAnalysis4DAbstractionTripleGenerating extends DemoAnalysis {

  @Override
  public void run() {
    Map<String, String> granularity = new HashMap<String,String>();

    granularity.put("cube:hasAircraft", "cube:Level_Aircraft_Model");
    granularity.put("cube:hasLocation", "cube:Level_Location_Segment");
    granularity.put("cube:hasDate", "cube:Level_Date_Day");
    granularity.put("cube:hasImportance", "cube:Level_Importance_Importance");
    
    this.getCube().setBenchmarkingMode(true);
    this.getCube().setExecuteInMemory(true);
    
    this.getCube().replaceByGrouping(granularity, "obj:usageType", "obj:ManoeuvringAreaUsage");
  }

}
