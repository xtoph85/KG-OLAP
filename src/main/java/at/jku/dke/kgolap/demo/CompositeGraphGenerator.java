package at.jku.dke.kgolap.demo;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class CompositeGraphGenerator extends GraphGenerator {
  private List<GraphGenerator> generators = new ArrayList<GraphGenerator>();
  
  
  public void addGenerator(GraphGenerator generator) {
    this.generators.add(generator);
  }
  
  @Override
  public void generateGraph(String name, File outputFile) {
    for(GraphGenerator gen : generators) {
      gen.generateGraph(name, outputFile);
    }
  }

}
