package at.jku.dke.kgolap.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceFileUpdater {
  private List<File> resourceFiles = new ArrayList<File>();
  private Map<File, Integer> noOfInstancesPerResource = new HashMap<File, Integer>();
  
  public void addResourceFile(File file) {
    this.resourceFiles.add(file);
  }
  
  public void setNoOfInstances(File file, int noOfInstances) {
    this.noOfInstancesPerResource.put(file, noOfInstances);
  }
  
  public void updateResourceFiles() {
    for(File file : resourceFiles) {
      ResourceFactory.writeResourcesToFile(
          this.noOfInstancesPerResource.get(file), 
          file
      );
    }
  }
}
