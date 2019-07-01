package at.jku.dke.kgolap.demo.analyses;

import java.util.HashMap;
import java.util.Map;

import at.jku.dke.kgolap.operators.AggregatePropertyValues.AggregateFunction;

public class DemoAnalysis3DAbstractionValueGenerating extends DemoAnalysis {

  @Override
  public void run() {
    Map<String, String> granularity = new HashMap<String,String>();

    granularity.put("cube:hasAircraft", "cube:Level_Aircraft_Model");
    granularity.put("cube:hasLocation", "cube:Level_Location_Segment");
    granularity.put("cube:hasDate", "cube:Level_Date_Day");
    
    this.getCube().setBenchmarkingMode(true);
    this.getCube().setExecuteInMemory(true);
    
    this.getCube().aggregateLiterals(granularity, 
                                     "obj:wingspan", 
                                     AggregateFunction.AVG, 
                                     "obj:AircraftCharacteristic");
  }

}
