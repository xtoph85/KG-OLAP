package at.jku.dke.kgolap.demo.datasets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.DefaultKGOLAPCubeFactory;
import at.jku.dke.kgolap.KGOLAPCube;
import at.jku.dke.kgolap.KGOLAPCubeProperties;

public abstract class DemoDataset {
  private static final Logger logger = LoggerFactory.getLogger(DemoDataset.class);
  
  public enum Size {TINY, SMALL, MEDIUM, LARGE, HUGE};

  private Size dimensionalSize = Size.MEDIUM;
  private Size contextSize = Size.TINY;
  private Size factSize = Size.TINY;

  public Size getDimensionalSize() {
    return dimensionalSize;
  }
  
  public void setDimensionalSize(Size size) {
    this.dimensionalSize = size;
  }
  
  public Size getContextSize() {
    return contextSize;
  }
  
  public void setContextSize(Size size) {
    this.contextSize = size;
  }
  
  public Size getFactSize() {
    return factSize;
  }
  
  public void setFactSize(Size size) {
    this.factSize = size;
  }
  
  public String getBasicObjectKnowledgeTemplate() {
    String basicObjectKnowledgeTemplate = null;
    String fileName = "knowledge/atm-object-basic.trig";
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
      logger.info("Read the TriG file with basic knowledge.");
      basicObjectKnowledgeTemplate = IOUtils.toString(in, StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      logger.error("Error reading TriG file.", e);
    }
    
    return basicObjectKnowledgeTemplate;
  }
  
  public void writeBasicCubeKnowledge(File outputFile) {
    String fileName = null; 
    String basicCubeKnowledge = null;
    
    if(!outputFile.exists()) {
      try {
        logger.info("Create output file.");
        outputFile.createNewFile();
      } catch (IOException e) {
        logger.error("Could not create output file.", e);
      }
    }
    
    switch(this.getDimensionalSize()) {
      case TINY:
        fileName = "knowledge/atm-1D-cube-basic.trig";
        break;
      case SMALL:
        fileName = "knowledge/atm-2D-cube-basic.trig";
        break;
      case MEDIUM:
        fileName = "knowledge/atm-3D-cube-basic.trig";
        break;
      case LARGE: 
        fileName = "knowledge/atm-4D-cube-basic.trig";
        break;
      default:
        fileName = "knowledge/atm-3D-cube-basic.trig";
        break;
    }
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
      logger.info("Read the TriG file with basic knowledge.");
      basicCubeKnowledge = IOUtils.toString(in, StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      logger.error("Error reading TriG file.", e);
    }
    
    try(
      OutputStream out = new FileOutputStream(outputFile, false);
      Writer writer = new OutputStreamWriter(out);
      BufferedWriter bWriter = new BufferedWriter(writer);
    ) {
      logger.info("Write basic knowledge to file.");
      bWriter.write(basicCubeKnowledge);
      bWriter.write("\n\n");
    } catch (FileNotFoundException e) {
      logger.error("Could not find output file.", e);
    } catch (IOException e) {
      logger.error("Error writing to output file.", e);
    }
  }
  
  public List<String> getAircraftHierarchy() {
    List<String> aircraftHierarchy = new ArrayList<String>();
    
    aircraftHierarchy.add("cube:Level_Aircraft_All");
    aircraftHierarchy.add("cube:Level_Aircraft_Type");
    aircraftHierarchy.add("cube:Level_Aircraft_Model");
    
    return aircraftHierarchy;
  }
  
  public List<String> getLocationHierarchy() {
    List<String> locationHierarchy = new ArrayList<String>();
    
    locationHierarchy.add("cube:Level_Location_All");
    locationHierarchy.add("cube:Level_Location_Region");
    locationHierarchy.add("cube:Level_Location_Segment");
    
    return locationHierarchy;
  }
  
  public List<String> getDateHierarchy() {
    List<String> dateHierarchy = new ArrayList<String>();
    
    dateHierarchy.add("cube:Level_Date_All");
    dateHierarchy.add("cube:Level_Date_Year");
    dateHierarchy.add("cube:Level_Date_Month");
    dateHierarchy.add("cube:Level_Date_Day");
    
    return dateHierarchy;
  }
  
  public List<String> getImportanceHierarchy() {
    List<String> dateHierarchy = new ArrayList<String>();
    
    dateHierarchy.add("cube:Level_Importance_All");
    dateHierarchy.add("cube:Level_Importance_Package");
    dateHierarchy.add("cube:Level_Importance_Importance");
    
    return dateHierarchy;
  }
    
  public abstract void generateAndSave(File outputFile);  

  public KGOLAPCube getKGOLAPCube(KGOLAPCubeProperties properties) {
    String rulesetFileName = null;
    
    switch(this.getDimensionalSize()) {
      case TINY: 
        rulesetFileName = "rulesets/atm-1D-ruleset.ttl";
        break;
      case SMALL: 
        rulesetFileName = "rulesets/atm-2D-ruleset.ttl";
        break;
      default:
      case MEDIUM:
        rulesetFileName = "rulesets/atm-3D-ruleset.ttl";
        break;
      case LARGE: 
        rulesetFileName = "rulesets/atm-4D-ruleset.ttl";
        break;
    }
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(rulesetFileName)) {
      logger.info("Read the ruleset file.");
      String rulesetTtl = IOUtils.toString(in, StandardCharsets.UTF_8.name());
      properties.setRulesetTtl(rulesetTtl);
    } catch (IOException e) {
      logger.error("Error reading ruleset file.", e);
    }
    
    return new DefaultKGOLAPCubeFactory().createKGOLAPCube(properties);
  }
}
