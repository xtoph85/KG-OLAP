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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupingGraphGenerator extends GraphGenerator {
  private static final Logger logger = LoggerFactory.getLogger(GroupingGraphGenerator.class);
  
  private List<String> groupingProperties = new ArrayList<String>();
  private Map<String,Integer> noOfGroupsPerGrouping = new HashMap<String,Integer>();
  private Map<String,Integer> noOfInstancesPerGroup = new HashMap<String,Integer>();
  private Map<String,File> groupResourcesFiles = new HashMap<String,File>();  
  private Map<String,File> instanceResourcesFiles = new HashMap<String,File>();
  private Map<String,String> domains = new HashMap<String,String>();
  private Map<String,String> ranges = new HashMap<String,String>();
  
  private Map<File,BufferedReader> resourceFileReaders = new HashMap<File,BufferedReader>();

  
  private boolean assertClassMembership = true;
  
  public void setAssertClassMembership(boolean assertClassMembership) {
    this.assertClassMembership = assertClassMembership;
  }
  
  public boolean isAssertClassMemberhip() {
    return this.assertClassMembership;
  }
  
  public void setGroupResourcesFile(String groupingProperty, File resourceFile) {
    this.groupResourcesFiles.put(groupingProperty, resourceFile);
  }
  
  public void setInstanceResourcesFile(String groupingProperty, File resourceFile) {
    this.instanceResourcesFiles.put(groupingProperty, resourceFile);
  }
  
  public String getGroupResource(String groupingProperty) {
    String resource = null;
    File resourceFile = groupResourcesFiles.get(groupingProperty);
    
    if(resourceFile != null) {
      try {
        BufferedReader bReader = resourceFileReaders.get(resourceFile);
        
        if(bReader != null) {
          resource = bReader.readLine();
          
          if(resource == null) {
            logger.debug("Reached end of file. Close reader and reopen.");
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
      }
    }
    
    if(resource == null) {
      resource = "<urn:uuid:" + UUID.randomUUID() + ">";
    }
    
    return resource;
  }
  
  public String getInstanceResource(String groupingProperty) {
    String resource = null;
    File resourceFile = instanceResourcesFiles.get(groupingProperty);
    
    if(resourceFile != null) {
      try {
        BufferedReader bReader = resourceFileReaders.get(resourceFile);
        
        if(bReader != null) {
          resource = bReader.readLine();
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
      }
    }
    
    if(resource == null) {
      resource = "<urn:uuid:" + UUID.randomUUID() + ">";
    }
    
    return resource;
  }
  
  public void addGroupingProperty(String groupingProperty) {
    this.groupingProperties.add(groupingProperty);
  }
  
  public void setNoOfInstancesPerGroup(String groupingProperty, int noOfInstances) {
    this.noOfInstancesPerGroup.put(groupingProperty, noOfInstances);
  }
  
  public void setNoOfGroupsPerGrouping(String groupingProperty, int noOfGroups) {
    this.noOfGroupsPerGrouping.put(groupingProperty, noOfGroups);
  }
  
  @Override
  public void generateGraph(String moduleName, File outputFile) {
    try (
        OutputStream out = new FileOutputStream(outputFile, true);
        Writer writer = new OutputStreamWriter(out);
        BufferedWriter bWriter = new BufferedWriter(writer);
    ){      
      bWriter.write(moduleName + " {\n");
      
      for(String groupingProperty : this.groupingProperties) {
        logger.debug("Generating graph for grouping property: " + groupingProperty);

        String domain = this.domains.get(groupingProperty);
        String range = this.ranges.get(groupingProperty);
        
        int noOfGroups = this.noOfGroupsPerGrouping.get(groupingProperty);
        int noOfInstances = this.noOfInstancesPerGroup.get(groupingProperty);
        
        for(int i = 0; i < noOfGroups; i++) {
          String group = this.getGroupResource(groupingProperty);
          
          if(range != null) {
            if(this.assertClassMembership) {
              bWriter.write("  " + group + " rdf:type " + range + " .\n");
            } else {
              //bWriter.write("  " + group + " rdf:type owl:Thing .\n");
            }
          }
                    
          for(int j = 0; j < noOfInstances; j++) {
            String instance = this.getInstanceResource(groupingProperty);
            
            if(domain != null) {
              if(this.assertClassMembership) {
                bWriter.write("  " + instance + " rdf:type " + domain + " .\n");
              } else {
                //bWriter.write("  " + instance + " rdf:type owl:Thing .\n");
              }
            }
            
            bWriter.write("  " + instance + " " + groupingProperty + " " + group + " .\n");
          }
        }
      }
      
      // close the buffered readers
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
  }

  public void setDomain(String groupingProperty, String domain) {
    this.domains.put(groupingProperty, domain);    
  }

  public void setRange(String groupingProperty, String range) {
    this.ranges.put(groupingProperty, range);    
  }

}
