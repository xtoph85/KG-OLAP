package at.jku.dke.kgolap.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.KGOLAPCube;
import at.jku.dke.kgolap.KGOLAPCube.Repository;
import at.jku.dke.kgolap.KGOLAPCubeProperties;
import at.jku.dke.kgolap.demo.analyses.DemoAnalysis;
import at.jku.dke.kgolap.demo.datasets.DemoDataset;
import at.jku.dke.kgolap.repo.RepoProperties;

public class DemoRunner {  
  private static final Logger logger = LoggerFactory.getLogger(DemoRunner.class);
  private static final Logger benchmarkingStatistics = LoggerFactory.getLogger("benchmarking-statistics");
  
  public static void main(String[] args) {  
    // create the command line parser
    Options options = getCommandLineOptions();
    CommandLineParser parser = new DefaultParser();
    
    try {
      CommandLine line = parser.parse(options, args);
      
      if(line.hasOption("h")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("kgolap", options);
        return;
      }
      
      logger.info("Assign a benchmark id and set the directory.");
      
      String benchmarkId = UUID.randomUUID().toString();
      System.setProperty("at.jku.dke.kgolap.demo.benchmark.id", benchmarkId);
      System.setProperty("at.jku.dke.kgolap.demo.benchmark.iteration", "0");
      
      KGOLAPCubeProperties properties = new KGOLAPCubeProperties();
      
      try (InputStream in = DemoRunner.class.getClassLoader().getResourceAsStream("config/prefixes.properties")) {
        logger.info("Read the prefixes properties from file.");

        Properties prefixProperties = new Properties();
        prefixProperties.load(in);

        for (String prefix : prefixProperties.stringPropertyNames()) {
          properties.addPrefix(prefix, prefixProperties.getProperty(prefix));
        }
      } catch (IOException e) {
        logger.error("Error reading prefixes properties from file.", e);
      }
      
      if(line.hasOption("fb") || line.hasOption("ft")) {
        String factoryClassBase = line.getOptionValue("fb");
        String factoryClassTemp = line.getOptionValue("ft");

        RepoProperties baseRepoProperties = new RepoProperties();
        RepoProperties tempRepoProperties = new RepoProperties();
        
        if(factoryClassBase != null) {
          properties.setBaseRepoFactoryClass(factoryClassBase);
          
          if(line.hasOption("ub")) {
            baseRepoProperties.setRepositoryURL(line.getOptionValue("ub"));
          } else if (line.hasOption("db")) {
            baseRepoProperties.setDataDir(line.getOptionValue("db"));
          }
        } else {
          properties.setBaseRepoFactoryClass(factoryClassTemp);
          
          if(line.hasOption("ut")) {
            baseRepoProperties.setRepositoryURL(line.getOptionValue("ut"));
          } else if (line.hasOption("dt")) {
            baseRepoProperties.setDataDir(line.getOptionValue("dt"));
          }
        }
        
        if(factoryClassTemp != null) {
          properties.setTempRepoFactoryClass(factoryClassTemp);
          
          if(line.hasOption("ut")) {
            tempRepoProperties.setRepositoryURL(line.getOptionValue("ut"));
          } else if (line.hasOption("dt")) {
            tempRepoProperties.setDataDir(line.getOptionValue("dt"));
          }
        } else {
          properties.setTempRepoFactoryClass(factoryClassBase);
          
          if(line.hasOption("ub")) {
            tempRepoProperties.setRepositoryURL(line.getOptionValue("ub"));
          } else if (line.hasOption("db")) {
            tempRepoProperties.setDataDir(line.getOptionValue("db"));
          }
        }
        
        properties.setBaseRepoProperties(baseRepoProperties);
        properties.setTempRepoProperties(tempRepoProperties);
        
        if(line.hasOption("s")) {
          String datasetClassName = line.getOptionValue("s");
          
          benchmarkingStatistics.info(
              System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + "Dataset-ClassName,"
            + datasetClassName
          );
          
          try {
            DemoDataset dataset = 
              (DemoDataset) Class.forName(datasetClassName).getDeclaredConstructor().newInstance();
            
            String datasetFilename = 
              System.getProperty("at.jku.dke.kgolap.demo.benchmark.dir") + File.separator + benchmarkId + ".trig";
            
            File datasetFile = new File(datasetFilename);
            
            dataset.generateAndSave(datasetFile);
            
            KGOLAPCube cube = dataset.getKGOLAPCube(properties);
            cube.setBenchmarkingMode(true);
            cube.startUp();

            cube.purge(Repository.BASE);
            cube.purge(Repository.TEMP);
            
            cube.add(datasetFilename);
            
            if(line.hasOption("a")) {
              String analysisClassName = line.getOptionValue("a");
              
              benchmarkingStatistics.info(
                  System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + "Analysis-ClassName,"
                + analysisClassName
              );
              
              DemoAnalysis analysis = 
                (DemoAnalysis) Class.forName(analysisClassName).getDeclaredConstructor().newInstance();
              
              analysis.setCube(cube);

              File fileBackupBaseRepo = null;
              
              int iterations;
              
              if(line.hasOption("i")) {
                iterations = Integer.parseInt(line.getOptionValue("i"));

                fileBackupBaseRepo = File.createTempFile("tmp", ".backup.trig");
                fileBackupBaseRepo.deleteOnExit();
                
                try(OutputStream out = new FileOutputStream(fileBackupBaseRepo)) {
                  cube.getBaseRepository().export(out);
                } catch (IOException e) {
                  logger.error("Error creating backup file.", e);
                }
              } else {
                iterations = 1;
              }
              
              for(int i = 1; i <= iterations; i++) {
                if (i > 1) {
                  cube.purge(Repository.BASE);
                  cube.purge(Repository.TEMP);
                  
                  logger.info("Load backup into base repository.");
                  
                  try(InputStream in = new FileInputStream(fileBackupBaseRepo)) {
                    cube.getBaseRepository().loadTriG(in);
                  } catch (FileNotFoundException e) {
                    logger.error("Could not find backup file.", e);
                  } catch (IOException e) {
                    logger.error("Error reading from backup file.", e);
                  }
                }
                
                System.setProperty("at.jku.dke.kgolap.demo.benchmark.iteration", i + "");
                
                analysis.run();
              }
            } else if (line.hasOption("i")) {
              int iterations = Integer.parseInt(line.getOptionValue("i"));
              
              for(int i = 1 ; i < iterations; i++) {                
                System.setProperty("at.jku.dke.kgolap.demo.benchmark.iteration", i + "");
                
                cube.purge(Repository.BASE);
                cube.purge(Repository.TEMP);
                
                cube.add(datasetFilename);
              }
            }
            
            cube.shutDown();
          } catch (
            InstantiationException | IllegalAccessException | IllegalArgumentException | 
            InvocationTargetException | NoSuchMethodException | SecurityException | 
            ClassNotFoundException e) {
            logger.error("Could not instantiate class.", e);
          } catch (IOException e1) {
            logger.error("Could not create backup file for multiple iterations.");
          }
        }
      }
    } catch(ParseException e) {
      logger.error("Error parsing input parameters.", e);
    }
  }
  
  private static Options getCommandLineOptions() {
    Options options = new Options();
    
    Option help = new Option("h", "help", false, "print this message");
    Option dataset = new Option("s", "dataset", true, "the DemoDataset class to load into the KG-OLAP cube");
    Option analysis = new Option("a", "analysis", true, "the DemoAnalysis class that performs analytical operations");
    Option repoBase = new Option("fb", "factory-base", true, "the RepoFactory class to use for instantiating the source repository");
    Option repoTemp = new Option("ft", "factory-temp", true, "the RepoFactory class to use for instantiating the target repository");
    Option urlBase = new Option("ub", "url-base", true, "the source repo's URL in case that the RepoFactory creates an HTTP repo");
    Option urlTemp = new Option("ut", "url-temp", true, "the target repo's URL in case that the RepoFactory creates an HTTP repo");
    Option datadirBase = new Option("db", "datadir-base", true, "the source repo's data directory in case the RepoFactory creates a native repo");
    Option datadirTemp = new Option("dt", "datadir-temp", true, "the target repo's data directory in case the RepoFactory creates a native repo");
    Option iterations = new Option("i", "iterations", true, "the number of iterations for the analysis");
    
    options.addOption(help);
    options.addOption(dataset);
    options.addOption(analysis);
    options.addOption(repoBase);
    options.addOption(repoTemp);
    options.addOption(urlBase);
    options.addOption(urlTemp);
    options.addOption(datadirBase);
    options.addOption(datadirTemp);
    options.addOption(iterations);
    
    return options;
  }
}
