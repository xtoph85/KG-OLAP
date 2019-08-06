package at.jku.dke.kgolap.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateGraphGenerator extends GraphGenerator {
  private static final Logger logger = LoggerFactory.getLogger(TemplateGraphGenerator.class);
  
  private String template;
  
  public String getTemplate() {
    return template;
  }
  
  public void setTemplate(String template) {
    this.template = template;
  }
  
  @Override
  public void generateGraph(String moduleName, File outputFile) {
    String output = template.replaceAll("!moduleName!", moduleName);
    
    try(
      OutputStream out = new FileOutputStream(outputFile, true);
      Writer writer = new OutputStreamWriter(out);
      BufferedWriter bWriter = new BufferedWriter(writer);
    ) {
      logger.debug("Write knowledge module contents to file.");
      bWriter.write(output);
      bWriter.write("\n\n");
    } catch (FileNotFoundException e) {
      logger.error("Could not find output file.", e);
    } catch (IOException e) {
      logger.error("Error writing to output file.", e);
    }
  }

}
