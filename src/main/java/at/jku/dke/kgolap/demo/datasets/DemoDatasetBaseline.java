package at.jku.dke.kgolap.demo.datasets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DemoDatasetBaseline extends DemoDataset {
  private static final Logger logger = LoggerFactory.getLogger(DemoDatasetBaseline.class);
  
  public abstract DemoDataset getBaseDataset();
  
  @Override
  public void generateAndSave(File outputFile) {
    File tmpOutputFile = null;
    try {
      tmpOutputFile = File.createTempFile("tmp", ".trig");
      tmpOutputFile.deleteOnExit();
    } catch (IOException e) {
      logger.error("Could not create temporary output file for dataset creation.");
    }
    
    DemoDataset dataset = this.getBaseDataset();
        
    // generate the original dataset
    dataset.generateAndSave(tmpOutputFile);
    
    // alter the dataset to have all facts in a single context
    try(FileReader reader = new FileReader(tmpOutputFile);
        BufferedReader bReader = new BufferedReader(reader);
        FileWriter writer = new FileWriter(outputFile);
        BufferedWriter bWriter = new BufferedWriter(writer)) {
      boolean contextFound = false;
      boolean removeLine = false;
      String context = null;

      int i = 0;
      String line;

      logger.info("Process the generated file line-by-line."); 
      while((line = bReader.readLine()) != null) {       
        if(!contextFound) {
          if(line.matches("\\<.*?\\>\\s\\{")) {
            context = line.substring(line.indexOf("<"), line.indexOf(">") + 1);
            contextFound = true;
            logger.info("Found context: " + context);
          }
          
          bWriter.write(line + "\n");
        } else {
          if(!removeLine) {
            if(line.startsWith("ckr:global")) {
              removeLine = true;
            } else if (line.startsWith("<")) {
              String newLine = line.replaceFirst("\\<.*?\\>", context);

              bWriter.write(newLine + "\n");
            } else {
              bWriter.write(line + "\n");
            }
          } else if (removeLine && line.startsWith("}")) {              
            removeLine = false;
          }
        }

        i++;
        if(i > 0 && i % 100000 == 0) {
          logger.info(i + " lines processed.");
        }
      }
    } catch (FileNotFoundException e) {
      logger.error("TriG file with statements not found.", e);
    } catch (IOException e) {
      logger.error("Error reading TriG file with statements.", e);
    }
  }

}
