package at.jku.dke.kgolap.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseGraphGenerator extends GraphGenerator {
  private static final Logger logger = LoggerFactory.getLogger(BaseGraphGenerator.class);
  
  private List<String> factClasses = new ArrayList<String>();
  private Map<String,Integer> noOfFacts = new HashMap<String,Integer>();

  private Map<String,String[]> dataProperties = new HashMap<String,String[]>(); 
  private Map<String,String[]> objectProperties = new HashMap<String,String[]>(); 
  private Map<String,String> objectPropertyRanges = new HashMap<String,String>();  
  private Map<String,String> dataPropertyRanges = new HashMap<String,String>();
  private Map<String,Integer> noOfProperties = new HashMap<String,Integer>();
  
  private List<String> defineRdfType = new ArrayList<String>();
  
  private Map<String,File> resourceFiles = new HashMap<String,File>();
  private Map<File,BufferedReader> resourceFileReaders = new HashMap<File,BufferedReader>();
  
  private boolean assertClassMembership = true;
  
  public void setAssertClassMembership(boolean assertClassMembership) {
    this.assertClassMembership = assertClassMembership;
  }
  
  public boolean isAssertClassMemberhip() {
    return this.assertClassMembership;
  }
  
  public void setResourceFile(String resourceClass, File resourceFile) {
    this.resourceFiles.put(resourceClass, resourceFile);
  }
  
  public void addFactClass(String factClass) {
    this.factClasses.add(factClass);
  }
  
  public void addDefineRdfType(String factClass) {
    this.defineRdfType.add(factClass);
  }
  
  public void setDataPropertiesForFactClass(String factClass, String[] measureProperties) {
    this.dataProperties.put(factClass, measureProperties);
  }
  
  public void setDataPropertyRange(String propertyClass, String range) {
    this.dataPropertyRanges.put(propertyClass, range);
  }
  
  public void setObjectPropertiesForFactClass(String factClass, String[] dimensionProperties) {
    this.objectProperties.put(factClass, dimensionProperties);
  }
  
  public void setObjectPropertyRange(String property, String rangeClassName) {
    this.objectPropertyRanges.put(property, rangeClassName);
  }
  
  public void setNoOfFacts(String factClass, int noOfFacts) {
    this.noOfFacts.put(factClass, noOfFacts);
  }

  public void setNoOfProperties(String propertyClass, int noOfProperties) {
    this.noOfProperties.put(propertyClass, noOfProperties);
  }
  
  public String getResourceForClass(String resourceClass) {
    String resource = null;
    File resourceFile = resourceFiles.get(resourceClass);
    
    if(resourceFile != null) {
      try {
        BufferedReader bReader = resourceFileReaders.get(resourceFile);
        
        if(bReader != null) {
          resource = bReader.readLine();
          
          if(resource == null) {
            logger.debug("Reached end of file for resource class " + resourceClass  + ". Close reader and reopen.");
            bReader.close();
            
            InputStream in = new FileInputStream(resourceFile);
            Reader reader = new InputStreamReader(in);
            bReader = new BufferedReader(reader);
            
            resource = bReader.readLine();

            resourceFileReaders.put(resourceFile, bReader);
          }
        } else {
          InputStream in = new FileInputStream(resourceFile);
          Reader reader = new InputStreamReader(in);
          bReader = new BufferedReader(reader);
          
          resource = bReader.readLine();
          
          resourceFileReaders.put(resourceFile, bReader);
        }
      } catch (FileNotFoundException e) {
        logger.error("Could not find input file.", e);
      } catch (IOException e) {
        logger.error("Error reading from input stream.", e);
        
        this.resourceFileReaders.remove(resourceFile);
      }
    }

    
    if(resource == null) {
      resource = "<urn:uuid:" + UUID.randomUUID() + ">";
    }
    
    return resource;
  }
  
  @Override
  public void generateGraph(String moduleName, File outputFile) {
    List<String> defineRdfTypeTemp = new ArrayList<String>();
    
    // create resources for each fact class
    for(String factClass: factClasses) {
      if(this.resourceFiles.get(factClass) == null) {
        try {
          File resourceFile = ResourceFactory.createResourceFile(this.noOfFacts.get(factClass));
          this.resourceFiles.put(factClass, resourceFile);
          defineRdfTypeTemp.add(factClass);
        } catch (IOException e) {
          logger.error("Could not create temporary file for resources.", e);
        }
      }
    }
    
    for(String rangeClass: this.objectPropertyRanges.values()) {
      if(this.resourceFiles.get(rangeClass) == null && !this.factClasses.contains(rangeClass)) {
        defineRdfTypeTemp.add(rangeClass);
      }
    }
    
    try (
        OutputStream out = new FileOutputStream(outputFile, true);
        Writer writer = new OutputStreamWriter(out);
        BufferedWriter bWriter = new BufferedWriter(writer);
    ){      
      bWriter.write(moduleName + " {\n");
      
      for(String factClass: factClasses) {        
        for(int i = 0; i < noOfFacts.get(factClass); i++) {
          String factInstance = this.getResourceForClass(factClass);
          
          if(defineRdfType.contains(factClass) || defineRdfTypeTemp.contains(factClass)) {
            if(this.assertClassMembership) {
              bWriter.write("  " + factInstance + " rdf:type " + factClass + " .\n");
            } else {
              //bWriter.write("  " + factInstance + " rdf:type owl:Thing .\n");
            }
          }
          
          String[] objectPropertiesForFactClass = objectProperties.get(factClass);
          
          if(objectPropertiesForFactClass != null) {
            for(String dimensionProperty : objectPropertiesForFactClass) {              
              for(int j = 0; j < noOfProperties.get(dimensionProperty); j++) {
                String rangeClass = this.objectPropertyRanges.get(dimensionProperty);
                String resource = this.getResourceForClass(rangeClass);
                
                if((defineRdfType.contains(rangeClass) || defineRdfTypeTemp.contains(rangeClass)) 
                    && !factClasses.contains(rangeClass)) {
                  if(this.assertClassMembership) {
                    bWriter.write("  " + resource + " rdf:type " + rangeClass + " .\n");
                  } else {
                    //bWriter.write("  " + resource + " rdf:type owl:Thing .\n");
                  }
                }
                
                bWriter.write("  " + factInstance + " " + dimensionProperty + " " + resource + " .\n");
              }
            }
          }
          
          
          String[] dataPropertiesForFactClass = dataProperties.get(factClass);
          
          if(dataPropertiesForFactClass != null) {
            for(String dataProperty : dataProperties.get(factClass)) {
              for(int j = 0; j < noOfProperties.get(dataProperty); j++) {
                String value = "";
                
                if(this.dataPropertyRanges.get(dataProperty) != null && 
                   this.dataPropertyRanges.get(dataProperty).equals("xsd:boolean")) {
                  value = "\"" + (new Random()).nextBoolean() + "\"^^xsd:boolean";
                } else {
                  value = "\"" + ((new Random()).nextInt(2000000000) + 1) + "\"^^xsd:int";
                }
                
                bWriter.write("  " + factInstance + " " + dataProperty + " " + value + " .\n");
              }
            }
          }
        }
      }
      
      for(File resourceFile : this.resourceFileReaders.keySet().toArray(new File[resourceFileReaders.keySet().size()])) {
        try {
          BufferedReader bReader = this.resourceFileReaders.get(resourceFile);
          
          bReader.close();
          
          this.resourceFileReaders.remove(resourceFile);
        } catch (IOException e) {
          logger.error("Could not close input stream.", e);
        }
      }
      
      bWriter.write("}\n\n");
    } catch (FileNotFoundException e) {
      logger.error("Could not find output file.", e);
    } catch (IOException e) {
      logger.error("Error writing to output stream.", e);
    }
    
    this.resourceFileReaders.clear();
    
    for(String className : defineRdfTypeTemp) {
      this.resourceFiles.remove(className);
    }
  }

}
