package at.jku.dke.kgolap.demo.analyses;

import at.jku.dke.kgolap.operators.AggregatePropertyValues.AggregateFunction;

public class DemoAnalysisBaselineAbstractionValueGenerating extends DemoAnalysis {

  @Override
  public void run() {
    String sparql = this.getCube().getSparqlPrefixes() +
        "SELECT ?ctx WHERE {\r\n" + 
        "    GRAPH ckr:global {\r\n" + 
        "        ?ctx rdf:type olap:Cell .\r\n" + 
        "    }\r\n" + 
        "}";
    
    String result = this.getCube().getTempRepository().executeTupleQuery(sparql);
    
    String context = "<" + result.split("\n")[1].trim() + ">";
    
    this.getCube().setBenchmarkingMode(true);
    this.getCube().setExecuteInMemory(true);
    
    this.getCube().aggregateLiterals(context, 
                                     "obj:wingspan", 
                                     AggregateFunction.AVG, 
                                     "obj:AircraftCharacteristic");
  }

}
