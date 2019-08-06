package at.jku.dke.kgolap.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinearStrategy extends Strategy {
  
  private Map<Map<String,String>, GraphGenerator> graphGenerators = new HashMap<Map<String,String>, GraphGenerator>();
  private Map<Map<String,String>, ResourceFileUpdater> resourceFileUpdaters = new HashMap<Map<String, String>, ResourceFileUpdater>();
  private Map<String,String> dimensionProperties = new HashMap<String,String>();
  private List<Map<String,String>> granularityChain = new ArrayList<Map<String,String>>();
  private Map<Map<String,String>,Integer> noOfDescendants = new HashMap<Map<String,String>,Integer>();
  private Map<String,List<String>> levelHierarchies = new HashMap<String,List<String>>();
  
  
  
  public void setLevelHierarchy(String dimension, List<String> levelHierarchy) {
    this.levelHierarchies.put(dimension, levelHierarchy);
  }
  
  public void setDimensionProperty(String dimension, String property) {
    this.dimensionProperties.put(dimension, property);
  }
  
  public void addGranularity(Map<String,String> granularity) {
    this.granularityChain.add(granularity);
  }
  
  public void setNoOfDescendantsAtGranularity(Map<String,String> granularity, int noOfDescendants) {
    this.noOfDescendants.put(granularity, noOfDescendants);
  }
  
  public void setGraphGeneratorForGranularity(Map<String,String> granularity, GraphGenerator generator) {
    this.graphGenerators.put(granularity, generator);
  }
  
  public void setResourceFileUpdaterForGranularity(Map<String, String> granularity, ResourceFileUpdater refresher) {
    this.resourceFileUpdaters.put(granularity, refresher);
  }
  
  @Override
  protected Map<String, String> nextRootContextCoordinates(DatasetGenerator datasetGenerator) {
    Map<String,String> latestContextCoordinates = datasetGenerator.getLatestContextCoordinates();
    Map<String,String> newRootContextCoordinates = new HashMap<String,String>();
    
    for(String dimension: latestContextCoordinates.keySet()) {
      newRootContextCoordinates.put(dimension, latestContextCoordinates.get(dimension));
    }
    
    return newRootContextCoordinates;
  }

  @Override
  protected int nextNoOfDescendants(DatasetGenerator datasetGenerator) {
    return noOfDescendants.get(this.nextDescendantGranularity(datasetGenerator));
  }

  @Override
  protected Map<String, String> nextDescendantGranularity(DatasetGenerator datasetGenerator) {
    final Map<String,String> latestGranularity = datasetGenerator.getLatestContextGranularity();
    final int indexOfLatestGranularity = granularityChain.indexOf(latestGranularity);
    
    Map<String,String> nextGranularity = null;
    
    if(indexOfLatestGranularity + 1 < granularityChain.size()) {
      nextGranularity = granularityChain.get(indexOfLatestGranularity + 1);
    }
    
    return nextGranularity;
  }

  @Override
  protected Map<String, String> nextDimensionProperties(DatasetGenerator datasetGenerator) {
    return this.dimensionProperties;
  }

  @Override
  protected boolean hasNext(DatasetGenerator datasetGenerator) {
    return nextDescendantGranularity(datasetGenerator) != null;
  }

  @Override
  protected Strategy nextDescendantStrategy(DatasetGenerator datasetGenerator) {
    return this;
  }

  @Override
  protected GraphGenerator nextGraphGenerator(DatasetGenerator datasetGenerator) {
    GraphGenerator graphGenerator = 
        this.graphGenerators.get(this.nextDescendantGranularity(datasetGenerator));
    
    return graphGenerator;
  }

  @Override
  protected Map<String, String> nextRootContextGranularity(DatasetGenerator datasetGenerator) {
    return granularityChain.get(granularityChain.indexOf(datasetGenerator.getLatestContextGranularity()));
  }

  @Override
  protected Map<String, List<String>> nextSkipLevels(DatasetGenerator datasetGenerator) {
    Map<String, List<String>> skipLevels = new HashMap<String, List<String>>();
    
    for(String dimension: this.levelHierarchies.keySet()) {
      List<String> levelsInDimension = this.levelHierarchies.get(dimension);
      List<String> skipLevelsInDimension = new ArrayList<String>();
      String ancestorLevel = this.nextRootContextGranularity(datasetGenerator).get(dimension);
      String descendantLevel = this.nextDescendantGranularity(datasetGenerator).get(dimension);
      
      for(
        int index = levelsInDimension.indexOf(descendantLevel) - 1; 
        index > levelsInDimension.indexOf(ancestorLevel) && index > 0; 
        index--
      ) {
        skipLevelsInDimension.add(levelsInDimension.get(index));
      }
      
      skipLevels.put(dimension, skipLevelsInDimension);
    }
    
    return skipLevels;
  }

  @Override
  protected ResourceFileUpdater nextResourceFileUpdater(DatasetGenerator datasetGenerator) {
    ResourceFileUpdater resourceFileUpdater = 
        this.resourceFileUpdaters.get(this.nextDescendantGranularity(datasetGenerator));
    
    return resourceFileUpdater;
  }
}
