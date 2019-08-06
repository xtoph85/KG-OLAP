package at.jku.dke.kgolap.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceFactory {
  private static final Logger logger = LoggerFactory.getLogger(ResourceFactory.class);
  
  public static File createResourceFile(int noOfResources) throws IOException {
    File file = File.createTempFile("resources-", ".tmp");
    file.deleteOnExit();
    
    ResourceFactory.writeResourcesToFile(noOfResources, file);
    
    return file;
  }
  
  public static void writeResourcesToFile(int noOfResources, File file) {
    try (
        OutputStream out = new FileOutputStream(file, false);
        Writer writer = new OutputStreamWriter(out);
        BufferedWriter bWriter = new BufferedWriter(writer);
    ){  
      for(int i = 0; i < noOfResources; i++) {
        bWriter.write("<urn:uuid:" + UUID.randomUUID() + ">\n");
      }
    } catch (FileNotFoundException e) {
      logger.error("Could not find output file.", e);
    } catch (IOException e) {
      logger.error("Error writing to file.", e);
    }
    
  }
}
