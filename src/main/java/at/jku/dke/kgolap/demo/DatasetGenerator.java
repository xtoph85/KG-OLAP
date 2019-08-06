package at.jku.dke.kgolap.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetGenerator {
  private static final Logger logger = LoggerFactory.getLogger(DatasetGenerator.class);
    
  private String latestContext = null;
  private String latestContextModule = null;
  private Map<String,String> latestContextCoordinates = new HashMap<String,String>();
  private Map<String,String> latestContextGranularity = new HashMap<String,String>();

  private File outputFile = null;
  
  public DatasetGenerator(File outputFile) {
    this.outputFile = outputFile;
  }
  
  public File getOutputFile() {
    return outputFile;
  }
  
  public String getLatestContext() {
    return latestContext;
  }
  
  public String getLatestContextModule() {
    return latestContextModule;
  }

  public Map<String, String> getLatestContextCoordinates() {
    return latestContextCoordinates;
  }

  public Map<String, String> getLatestContextGranularity() {
    return latestContextGranularity;
  }
  
  public void generateContextAtCoordinates(
      final Map<String,String> contextCoordinates,
      final GraphGenerator graphGenerator,
      final Map<String,String> dimensionProperties
  ) {
    // generate a name for the context
    String uuid = "urn:uuid:" + UUID.randomUUID();
    String contextName = "<" + uuid + ">";
    String moduleName = "<" + uuid + "-mod>";
    
    try(
      OutputStream out = new FileOutputStream(outputFile , true);
      Writer writer = new OutputStreamWriter(out);
      BufferedWriter bWriter = new BufferedWriter(writer);
    ){  
      bWriter.write("ckr:global {\n");
      
      bWriter.write("  " + contextName + " rdf:type olap:Cell ;\n");
      bWriter.write("    " + "ckr:hasAssertedModule " + moduleName + " .\n");
      
      for(String dimension: dimensionProperties.keySet()) {
        String dimensionProperty = dimensionProperties.get(dimension);
        String dimensionAttributeValueName = contextCoordinates.get(dimension);
        
        // associate the dimension attribute value with the generated context
        bWriter.write("  " + contextName + " " + dimensionProperty + " " + dimensionAttributeValueName + " .\n");
      }
      
      bWriter.write("}\n\n\n");
    } catch (FileNotFoundException e) {
      logger.error("Could not find output file.", e);
    } catch (IOException e) {
      logger.error("Error writing to output stream.", e);
    }

      
    if(graphGenerator != null) {
      logger.debug("Populate the module with facts.");
      graphGenerator.generateGraph(moduleName, outputFile);
    }
  }
    
  public void generateDescendantsAtGranularity(
    final Map<String,String> rootContextCoordinates,
    final Map<String,String> rootContextGranularity,
    final int noOfDescendants,
    final Map<String,String> descendantGranularity,
    final Map<String,List<String>> skipLevels,
    final GraphGenerator graphGenerator,
    final ResourceFileUpdater resourceFileUpdater,
    final Strategy descendantStrategy,
    final Map<String,String> dimensionProperties
  ) {    
    // create i descendants of the root context with the argument coordinates
    for(int i = 0; i < noOfDescendants; i++) {
      // update resource files in the beginning
      if(resourceFileUpdater != null) {
        resourceFileUpdater.updateResourceFiles();
      }
      
      // generate a name for the descendant
      String uuid = "urn:uuid:" + UUID.randomUUID();
      String contextName = "<" + uuid + ">";
      String moduleName = "<" + uuid + "-mod>";
        
      this.latestContext = contextName;
      this.latestContextModule = moduleName;
      this.latestContextCoordinates.clear(); // reset
      this.latestContextGranularity.clear(); // reset

      try(
        OutputStream out = new FileOutputStream(outputFile , true);
        Writer writer = new OutputStreamWriter(out);
        BufferedWriter bWriter = new BufferedWriter(writer);
      ){  
        bWriter.write("ckr:global {\n");
        
        bWriter.write("  " + contextName + " rdf:type olap:Cell ;\n");
        bWriter.write("    " + "ckr:hasAssertedModule " + moduleName + " .\n");
        
        for(String dimension: dimensionProperties.keySet()) {
          String dimensionAttributeValueName = null;
          
          if(!rootContextGranularity.get(dimension).equals(descendantGranularity.get(dimension))) {
            // generate a dimension attribute value
            dimensionAttributeValueName = "<urn:uuid:" + UUID.randomUUID() + ">";
            
            // for each dimension, generate a dimension attribute value
            bWriter.write("  " + dimensionAttributeValueName + " rdf:type " + dimension + " , owl:NamedIndividual ;\n");
            bWriter.write("    olap:atLevel " + descendantGranularity.get(dimension) + " .\n");
            
            if(skipLevels == null || skipLevels.isEmpty() || skipLevels.get(dimension) == null || skipLevels.get(dimension).isEmpty()) {
              bWriter.write("  " + dimensionAttributeValueName + " olap:directlyRollsUpTo " + rootContextCoordinates.get(dimension) + " .\n");
            } else if (skipLevels.get(dimension) != null && !skipLevels.get(dimension).isEmpty()){
              List<String> skipLevelsInDimension = skipLevels.get(dimension);
              String childDimensionAttributeValueName = dimensionAttributeValueName;
              
              for(String skipLevel : skipLevelsInDimension) {
                String skipLevelAttributeValueName = "<urn:uuid:" + UUID.randomUUID() + ">";
                
                bWriter.write("  " + childDimensionAttributeValueName + " olap:directlyRollsUpTo " + skipLevelAttributeValueName + " .\n");
                bWriter.write("  " + skipLevelAttributeValueName + " rdf:type " + dimension + " , owl:NamedIndividual ;\n");
                bWriter.write("    olap:atLevel " + skipLevel + " .\n");
                
                childDimensionAttributeValueName = skipLevelAttributeValueName;
              }
              
              bWriter.write("  " + childDimensionAttributeValueName + " olap:directlyRollsUpTo " + rootContextCoordinates.get(dimension) + " .\n");
            }
          } else {
            // take the parent's dimension attribute value
            dimensionAttributeValueName = rootContextCoordinates.get(dimension);
          }
          
          // associate the dimension attribute value with the generated context
          bWriter.write("  " + contextName + " " + dimensionProperties.get(dimension) + " " + dimensionAttributeValueName + " .\n");
          
          // set current context coordinates and granularity
          this.latestContextCoordinates.put(dimension, dimensionAttributeValueName);
          this.latestContextGranularity.put(dimension, descendantGranularity.get(dimension));
        }
        
        bWriter.write("}\n\n");
      } catch (FileNotFoundException e) {
        logger.error("Could not find output file.", e);
      } catch (IOException e) {
        logger.error("Error writing to output stream.", e);
      }

      
      if(graphGenerator != null) {
        logger.debug("Populate the module with facts.");
        graphGenerator.generateGraph(moduleName, outputFile);
      }
      
      if(descendantStrategy != null && descendantStrategy.hasNext(this)) {
        logger.debug("Generate descendants according to argument descendant strategy.");
        descendantStrategy.resumeDescendantGeneration(this);
      }
    }
  }
}
