package at.jku.dke.kgolap.demo;

import java.util.List;
import java.util.Map;

public abstract class Strategy {

  public void resumeDescendantGeneration(DatasetGenerator datasetGenerator) {
    datasetGenerator.generateDescendantsAtGranularity(
      this.nextRootContextCoordinates(datasetGenerator), 
      this.nextRootContextGranularity(datasetGenerator),
      this.nextNoOfDescendants(datasetGenerator), 
      this.nextDescendantGranularity(datasetGenerator), 
      this.nextSkipLevels(datasetGenerator),
      this.nextGraphGenerator(datasetGenerator),
      this.nextResourceFileUpdater(datasetGenerator),
      this.nextDescendantStrategy(datasetGenerator), 
      this.nextDimensionProperties(datasetGenerator)
    );
  }
  
  protected abstract Map<String, List<String>> nextSkipLevels(DatasetGenerator datasetGenerator);
  protected abstract boolean hasNext(DatasetGenerator datasetGenerator);
  protected abstract Map<String, String> nextRootContextCoordinates(DatasetGenerator datasetGenerator);
  protected abstract Map<String, String> nextRootContextGranularity(DatasetGenerator datasetGenerator);
  protected abstract int nextNoOfDescendants(DatasetGenerator datasetGenerator);
  protected abstract Map<String, String> nextDescendantGranularity(DatasetGenerator datasetGenerator);
  protected abstract GraphGenerator nextGraphGenerator(DatasetGenerator datasetGenerator);
  protected abstract Strategy nextDescendantStrategy(DatasetGenerator datasetGenerator);
  protected abstract Map<String, String> nextDimensionProperties(DatasetGenerator datasetGenerator);
  protected abstract ResourceFileUpdater nextResourceFileUpdater(DatasetGenerator datasetGenerator);
}
