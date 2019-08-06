package at.jku.dke.kgolap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.operators.AggregatePropertyValues;
import at.jku.dke.kgolap.operators.GroupByProperties;
import at.jku.dke.kgolap.operators.Merge;
import at.jku.dke.kgolap.operators.Pivot;
import at.jku.dke.kgolap.operators.Reification;
import at.jku.dke.kgolap.operators.ReplaceByGrouping;
import at.jku.dke.kgolap.operators.SliceDice;
import at.jku.dke.kgolap.repo.Repo;

import eu.fbk.rdfpro.RuleEngine;

/**
 * 
 * @author Christoph G. Schuetz
 */
public class KGOLAPCube {
  private static final Logger logger = LoggerFactory.getLogger(KGOLAPCube.class);
  private static final Logger benchmarkingStatistics = LoggerFactory.getLogger("benchmarking-statistics");
  
  private boolean inBenchmarkingMode = false;
  
  private boolean useSPARQLUpdateStatements = false;
    
  private boolean executeInMemory = true;
  
  public enum Repository {
    BASE, TEMP
  }
  
  private Repo baseRepository;
  private Repo tempRepository;
  
  private Map<String,String> prefixes;
  private RuleEngine ruleEngine;
  
  public KGOLAPCube(Repo baseRepository,
                    Repo tempRepository,
                    RuleEngine ruleEngine,
                    Map<String,String> prefixes) throws MissingPrefixException, URISyntaxException {
    this.baseRepository = baseRepository;
    this.tempRepository = tempRepository;
    this.ruleEngine = ruleEngine;
    this.setPrefixes(prefixes);
  }
  
  public boolean isExecuteInMemory() {
    return this.executeInMemory;
  }
  
  public void setExecuteInMemory(boolean executeInMemory) {
    this.executeInMemory = executeInMemory;
  }
  
  public boolean isUseSPARQLUpdateStatements() {
    return this.useSPARQLUpdateStatements;
  }
  
  public void setUseSPARQLUpdateStatments(boolean useSPARQLUpdateStatements) {
    this.useSPARQLUpdateStatements = useSPARQLUpdateStatements;
  }
  
  public boolean isInBenchmarkingMode() {
    return inBenchmarkingMode;
  }
  
  public void toggleBenchmarkingMode() {
    this.setBenchmarkingMode(!this.inBenchmarkingMode);
  }
  
  public void setBenchmarkingMode(boolean benchmarkingMode) {
    this.inBenchmarkingMode = benchmarkingMode;
    
    if(this.baseRepository != null) {
    	this.baseRepository.setBenchmarkingMode(benchmarkingMode);
    }
    
    if(this.tempRepository != null) {
    	this.tempRepository.setBenchmarkingMode(benchmarkingMode);
    }
  }
  
  public String getSparqlPrefixes() {
    StringBuilder prefixesBuilder = new StringBuilder();
    
    for(String prefix : this.prefixes.keySet()) {
      prefixesBuilder.append("PREFIX " + prefix + ": <" + this.prefixes.get(prefix) + ">\n");
    }
    
    return prefixesBuilder.toString();
  }
  
  public void setPrefixes(Map<String,String> prefixes) throws MissingPrefixException,URISyntaxException {
    UrlValidator urlValidator = new UrlValidator();
        
    if(!prefixes.containsKey("ckr")) {
      throw new MissingPrefixException("ckr");
    }
    
    if(!prefixes.containsKey("olap")) {
      throw new MissingPrefixException("olap");
    }
    
    this.prefixes = new HashMap<String,String>();

    for(String prefix : prefixes.keySet()) {
      String uri = prefixes.get(prefix);
      
      if(urlValidator.isValid(uri)) {
        this.prefixes.put(prefix, uri);
      } else {
        throw new URISyntaxException(uri, "Was expecting a different URI.");
      }
    }
  }
  
  public void setPrefix(String prefix, String uri) throws URISyntaxException {
    UrlValidator urlValidator = new UrlValidator();
    
    if(urlValidator.isValid(uri)) {
      this.prefixes.put(prefix, uri);
    } else {
      throw new URISyntaxException(uri, "Was expecting a different URI.");
    }
  }
  
  public Repo getBaseRepository() {
    return this.baseRepository;
  }
  
  public Repo getTempRepository() {
    return this.tempRepository;
  }
  
  public Repo getRepository(Repository repository) {
    if(repository == Repository.TEMP) {
      return this.tempRepository;
    } else {
      return this.baseRepository;
    }
  }
  
  public KGOLAPCube startUp(Repository repository) {
    logger.info("Start up repository " + repository + ".");
    
    this.getRepository(repository).startUp();
    
    return this;
  }
  
  public KGOLAPCube shutDown(Repository repository) {
    logger.info("Shut down repository " + repository + ".");
    
    this.getRepository(repository).shutDown();
    
    return this;
  }
  
  public KGOLAPCube startUp() {
    logger.info("Starting cube up ...");
    
    this.baseRepository.startUp();
    this.tempRepository.startUp();
    
    logger.info("Cube started.");
    
    return this;
  }
  
  public void shutDown() {
    logger.info("Shutting cube down ...");
    
    this.baseRepository.shutDown();
    this.tempRepository.shutDown();
    
    logger.info("Cube shut down.");
  }
  
  public KGOLAPCube add(InputStream in) {
    logger.info("Load TriG from input stream.");
    baseRepository.loadTriG(in);
    
    baseRepository.evaluateRules(ruleEngine);

    if(this.isInBenchmarkingMode()) {
      benchmarkingStatistics.info(
            System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
          + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
          + "ModelSize-Contexts,"
          + this.getNumberOfContexts(KGOLAPCube.Repository.BASE)
      );
      
      benchmarkingStatistics.info(
            System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
          + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
          + "ModelSize-Individuals,"
          + this.getNumberOfIndividuals(KGOLAPCube.Repository.BASE)
      );
      
      benchmarkingStatistics.info(
            System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
          + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
          + "ModelSize-Literals,"
          + this.getNumberOfLiterals(KGOLAPCube.Repository.BASE)
      );
      
      benchmarkingStatistics.info(
            System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
          + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
          + "ModelSize-Predicates,"
          + this.getNumberOfPredicates(KGOLAPCube.Repository.BASE)
      );
    }
    
    return this;
  }
  
  public KGOLAPCube add(String filename) {
    logger.info("Open file input stream for " + filename + " to load into base repository.");
    
    try(InputStream in = new FileInputStream(filename)) {
      this.add(in);
    } catch (FileNotFoundException e) {
      logger.error("Could not find file.", e);
    } catch (IOException e) {
      logger.error("Error reading file.", e);
    }
    
    return this;
  }
  
  public KGOLAPCube evaluateRules() {
    logger.info("Evaluate the ruleset on the base repository.");
    baseRepository.evaluateRules(ruleEngine);
    
    return this;
  }
  
  public KGOLAPCube evaluateRules(Repository repository) {
    logger.info("Evaluate the ruleset.");
    Repo repo = null;
    
    if(repository.equals(Repository.TEMP)) {
      repo = tempRepository;
    } else {
      repo = baseRepository;
    }
    
    repo.evaluateRules(ruleEngine);
    
    return this;
  }
  
  public KGOLAPCube reset() {
    logger.info("Resetting the cube ...");
    
    logger.info("Clear the base repository.");
    baseRepository.clearRepository();
    
    logger.info("Clear the temporary repository.");
    tempRepository.clearRepository();

    logger.info("Cube reset.");
    
    return this;
  }
  
  public KGOLAPCube newAnalysis() {
    tempRepository.clearRepository();
    
    return this;
  }
  
  public KGOLAPCube purge(Repository repository) {
    if(repository == Repository.BASE) {
      logger.info("Clear the base repository.");
      baseRepository.clearRepository();
    } else if (repository == Repository.TEMP) {
      logger.info("Clear the temporary repository.");
      tempRepository.clearRepository();
    }
    
    return this;
  }
  
  public KGOLAPCube export(Repository repository, String filename) {
    if(repository == Repository.BASE) {
      baseRepository.export(filename);
    } else if (repository == Repository.TEMP) {
      tempRepository.export(filename);
    }
    
    return this;
  }
  
  public KGOLAPCube sliceDice(Map<String,String> diceCoordinates) {
    SliceDice sliceDice = new SliceDice(baseRepository, tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    sliceDice.setBenchmarkingMode(inBenchmarkingMode);
    
    sliceDice.addDiceCoordinates(diceCoordinates);
    
    if(this.useSPARQLUpdateStatements) {
      sliceDice.executeUpdate();
    } else {
      if(this.executeInMemory) {
        sliceDice.executeInMemory();
      } else {
        sliceDice.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube sliceDice(Map<String,String>[] diceCoordinateVectors) {
    SliceDice sliceDice = new SliceDice(baseRepository, tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    sliceDice.setBenchmarkingMode(inBenchmarkingMode);
    
    for(Map<String,String> vector : diceCoordinateVectors) {
      sliceDice.addDiceCoordinates(vector);
    }
    
    if(this.useSPARQLUpdateStatements) {
      sliceDice.executeUpdate();
    } else {
      if(this.executeInMemory) {
        sliceDice.executeInMemory();
      } else {
        sliceDice.execute();
      }
    }
    
    return this;
  }

  public KGOLAPCube merge(Map<String,String> granularity, Merge.Method method) {
    Merge merge = new Merge(tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    merge.setBenchmarkingMode(inBenchmarkingMode);
    
    merge.setMethod(method);
    
    for(String dimension : granularity.keySet()) {
      merge.setGranularity(dimension, granularity.get(dimension));
    }

    if(this.useSPARQLUpdateStatements) {
      merge.executeUpdate();
    } else {
      if(this.executeInMemory) {
        merge.executeInMemory();
      } else {
        merge.execute();
      }
    }
    
    if(!this.isInBenchmarkingMode()) {
      this.evaluateRules(Repository.TEMP);
    }
    
    return this;
  }
  
  public KGOLAPCube mergeUnion(Map<String,String> granularity) {
    return merge(granularity, Merge.Method.UNION);
  }
  
  public KGOLAPCube mergeIntersect(Map<String,String> granularity) {
    return merge(granularity, Merge.Method.INTERSECT);
  }
  
  public KGOLAPCube replaceByGrouping(String context, String groupingProperty, String groupingResourceClass) {
    ReplaceByGrouping replace = new ReplaceByGrouping(tempRepository, this.getSparqlPrefixes(), context);
    
    // operation's benchmarking mode is equal to cube's
    replace.setBenchmarkingMode(inBenchmarkingMode);
    
    replace.setGroupingProperty(groupingProperty);
    replace.setGroupingResourceClass(groupingResourceClass);

    if(this.useSPARQLUpdateStatements) {
      replace.executeUpdate();
    } else {
      if(this.executeInMemory) {
        replace.executeInMemory();
      } else {
        replace.execute();
      }
    }
    
    if(!this.isInBenchmarkingMode()) {
      this.evaluateRules(Repository.TEMP);
    }
    
    return this;
  }
  
  public KGOLAPCube replaceByGrouping(String context, String groupingProperty) {
    ReplaceByGrouping replace = new ReplaceByGrouping(tempRepository, this.getSparqlPrefixes(), context);
    
    // operation's benchmarking mode is equal to cube's
    replace.setBenchmarkingMode(inBenchmarkingMode);
    
    replace.setGroupingProperty(groupingProperty);

    if(this.useSPARQLUpdateStatements) {
      replace.executeUpdate();
    } else {
      if(this.executeInMemory) {
        replace.executeInMemory();
      } else {
        replace.execute();
      }
    }
    
    if(!this.isInBenchmarkingMode()) {
      this.evaluateRules(Repository.TEMP);
    }
    
    return this;
  }
  
  public KGOLAPCube replaceByGrouping(Map<String,String> granularity, String groupingProperty) {
    ReplaceByGrouping replace = new ReplaceByGrouping(tempRepository, this.getSparqlPrefixes(), granularity);
    
    // operation's benchmarking mode is equal to cube's
    replace.setBenchmarkingMode(inBenchmarkingMode);
    
    replace.setGroupingProperty(groupingProperty);

    if(this.useSPARQLUpdateStatements) {
      replace.executeUpdate();
    } else {
      if(this.executeInMemory) {
        replace.executeInMemory();
      } else {
        replace.execute();
      }
    }
    
    if(!this.isInBenchmarkingMode()) {
      this.evaluateRules(Repository.TEMP);
    }
    
    return this;
  }
  
  public KGOLAPCube replaceByGrouping(Map<String,String> granularity, 
                                      String groupingProperty, 
                                      String groupingResourceClass) {
    ReplaceByGrouping replace = new ReplaceByGrouping(tempRepository, this.getSparqlPrefixes(), granularity);
    
    // operation's benchmarking mode is equal to cube's
    replace.setBenchmarkingMode(inBenchmarkingMode);
    
    replace.setGroupingProperty(groupingProperty);
    replace.setGroupingResourceClass(groupingResourceClass);

    if(this.useSPARQLUpdateStatements) {
      replace.executeUpdate();
    } else {
      if(this.executeInMemory) {
        replace.executeInMemory();
      } else {
        replace.execute();
      }
    }
    
    if(!this.isInBenchmarkingMode()) {
      this.evaluateRules(Repository.TEMP);
    }
    
    return this;
  }
  
  public KGOLAPCube groupByProperties(String context, String[] groupingProperties, String generatedGrouping) {
    return this.groupByProperties(context, groupingProperties, generatedGrouping, null);
  }
  
  public KGOLAPCube groupByProperties(String context, String[] groupingProperties, String generatedGrouping, String groupedResourceClass) {
    GroupByProperties group = new GroupByProperties(tempRepository, this.getSparqlPrefixes(), context);
    
    // operation's benchmarking mode is equal to cube's
    group.setBenchmarkingMode(inBenchmarkingMode);
    
    for(String property : groupingProperties) {
      group.addGroupingProperty(property);
    }
    
    group.setGeneratedGrouping(generatedGrouping);
    
    group.setGroupedResourceClass(groupedResourceClass);

    if(this.useSPARQLUpdateStatements) {
      group.executeUpdate();
    } else {
      if(this.executeInMemory) {
        group.executeInMemory();
      } else {
        group.execute();
      }
    }
    
    if(!this.isInBenchmarkingMode()) {
      this.evaluateRules(Repository.TEMP);
    }
    
    return this;
  }
  
  public KGOLAPCube groupByProperties(Map<String,String> granularity, 
                                      String[] groupingProperties, 
                                      String generatedGrouping) {
    return this.groupByProperties(granularity, groupingProperties, generatedGrouping, null);
  }
  
  public KGOLAPCube groupByProperties(Map<String,String> granularity, 
                                      String[] groupingProperties, 
                                      String generatedGrouping,
                                      String groupedResourceClass) {
    GroupByProperties group = new GroupByProperties(tempRepository, this.getSparqlPrefixes(), granularity);
    
    // operation's benchmarking mode is equal to cube's
    group.setBenchmarkingMode(inBenchmarkingMode);
    
    for(String property : groupingProperties) {
      group.addGroupingProperty(property);
    }
    
    group.setGeneratedGrouping(generatedGrouping);
    
    group.setGroupedResourceClass(groupedResourceClass);

    if(this.useSPARQLUpdateStatements) {
      group.executeUpdate();
    } else {
      if(this.executeInMemory) {
        group.executeInMemory();
      } else {
        group.execute();
      }
    }
    
    if(!this.isInBenchmarkingMode()) {
      this.evaluateRules(Repository.TEMP);
    }
    
    return this;
  }
  
  public KGOLAPCube aggregateLiterals(String context, 
                                      String aggregatedProperty, 
                                      AggregatePropertyValues.AggregateFunction aggregateFunction,
                                      Map<String,String> selectionMap) {
    AggregatePropertyValues aggregate = new AggregatePropertyValues(tempRepository, this.getSparqlPrefixes(), context);
    
    // operation's benchmarking mode is equal to cube's
    aggregate.setBenchmarkingMode(inBenchmarkingMode);
    
    aggregate.setAggregatedProperty(aggregatedProperty);
    aggregate.setAggregateFunction(aggregateFunction);
    
    for(String property : selectionMap.keySet()) {
      aggregate.addSelectionCondition(property, selectionMap.get(property));
    }

    if(this.useSPARQLUpdateStatements) {
      aggregate.executeUpdate();
    } else {
      if(this.executeInMemory) {
        aggregate.executeInMemory();
      } else {
        aggregate.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube aggregateLiterals(String context, 
                                      String aggregatedProperty, 
                                      AggregatePropertyValues.AggregateFunction aggregateFunction,
                                      String groupedResourceClass) {
    AggregatePropertyValues aggregate = new AggregatePropertyValues(tempRepository, this.getSparqlPrefixes(), context);
    
    // operation's benchmarking mode is equal to cube's
    aggregate.setBenchmarkingMode(inBenchmarkingMode);
    
    aggregate.setAggregatedProperty(aggregatedProperty);
    aggregate.setAggregateFunction(aggregateFunction);
    aggregate.setGroupedResourceClass(groupedResourceClass);

    if(this.useSPARQLUpdateStatements) {
      aggregate.executeUpdate();
    } else {
      if(this.executeInMemory) {
        aggregate.executeInMemory();
      } else {
        aggregate.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube aggregateLiterals(Map<String, String> granularity, 
                                      String aggregatedProperty, 
                                      AggregatePropertyValues.AggregateFunction aggregateFunction,
                                      String groupedResourceClass) {
    AggregatePropertyValues aggregate = new AggregatePropertyValues(tempRepository, this.getSparqlPrefixes(), granularity);
    
    // operation's benchmarking mode is equal to cube's
    aggregate.setBenchmarkingMode(inBenchmarkingMode);
    
    aggregate.setAggregatedProperty(aggregatedProperty);
    aggregate.setAggregateFunction(aggregateFunction);
    aggregate.setGroupedResourceClass(groupedResourceClass);

    if(this.useSPARQLUpdateStatements) {
      aggregate.executeUpdate();
    } else {
      if(this.executeInMemory) {
        aggregate.executeInMemory();
      } else {
        aggregate.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube pivot(Map<String,String> aggregationLevel,
                          String dimensionProperty,
                          String selectionProperty,
                          String selectionResource,
                          String pivotProperty) {
    Pivot pivot = new Pivot(tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    pivot.setBenchmarkingMode(inBenchmarkingMode);
    
    for(String property : aggregationLevel.keySet()) {
      pivot.addGranularityLevel(property, aggregationLevel.get(property));
    }
    
    pivot.setDimensionProperty(dimensionProperty);
    pivot.setSelectionCondition(selectionProperty, selectionResource);
    pivot.setPivotProperty(pivotProperty);

    if(this.useSPARQLUpdateStatements) {
      pivot.executeUpdate();
    } else {
      if(this.executeInMemory) {
        pivot.executeInMemory();
      } else {
        pivot.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube pivot(String context,
                          String dimensionProperty,
                          String selectionProperty,
                          String selectionResource,
                          String pivotProperty) {
    Pivot pivot = new Pivot(tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    pivot.setBenchmarkingMode(inBenchmarkingMode);
    
    pivot.setContext(context);
    pivot.setDimensionProperty(dimensionProperty);
    pivot.setSelectionCondition(selectionProperty, selectionResource);
    pivot.setPivotProperty(pivotProperty);

    if(this.useSPARQLUpdateStatements) {
      pivot.executeUpdate();
    } else {
      if(this.executeInMemory) {
        pivot.executeInMemory();
      } else {
        pivot.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube reify(String context, String subjectSelectionProperty, String subjectSelectionResource) {
    Reification reification = new Reification(tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    reification.setBenchmarkingMode(inBenchmarkingMode);
    
    reification.setContext(context);
      
    reification.setSubjectSelectionCondition(subjectSelectionProperty, subjectSelectionResource);

    if(this.useSPARQLUpdateStatements) {
      reification.executeUpdate();
    } else {
      if(this.executeInMemory) {
        reification.executeInMemory();
      } else {
        reification.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube reify(Map<String,String> aggregationLevel, String subjectSelectionProperty, String subjectSelectionResource) {
    Reification reification = new Reification(tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    reification.setBenchmarkingMode(inBenchmarkingMode);
    
    for(String property : aggregationLevel.keySet()) {
      reification.addGranularityLevel(property, aggregationLevel.get(property));
    }
      
    reification.setSubjectSelectionCondition(subjectSelectionProperty, subjectSelectionResource);

    if(this.useSPARQLUpdateStatements) {
      reification.executeUpdate();
    } else {
      if(this.executeInMemory) {
        reification.executeInMemory();
      } else {
        reification.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube reify(String context, String reifiedProperty) {
    Reification reification = new Reification(tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    reification.setBenchmarkingMode(inBenchmarkingMode);
    
    reification.setContext(context);
      
    reification.setReificationPredicate(reifiedProperty);
    
    if(this.useSPARQLUpdateStatements) {
      reification.executeUpdate();
    } else {
      if(this.executeInMemory) {
        reification.executeInMemory();
      } else {
        reification.execute();
      }
    }
    
    return this;
  }
  
  public KGOLAPCube reify(Map<String,String> aggregationLevel, String reifiedProperty) {
    Reification reification = new Reification(tempRepository, this.getSparqlPrefixes());
    
    // operation's benchmarking mode is equal to cube's
    reification.setBenchmarkingMode(inBenchmarkingMode);
    
    for(String property : aggregationLevel.keySet()) {
      reification.addGranularityLevel(property, aggregationLevel.get(property));
    }
      
    reification.setReificationPredicate(reifiedProperty);
    
    if(this.useSPARQLUpdateStatements) {
      reification.executeUpdate();
    } else {
      if(this.executeInMemory) {
        reification.executeInMemory();
      } else {
        reification.execute();
      }
    }
    
    return this;
  }

  public int getNumberOfContexts(Repository repository) {
    Repo repo = this.getRepository(repository);
    
    String sparql = 
        this.getSparqlPrefixes()
      + "SELECT (COUNT(DISTINCT ?ctx) AS ?cnt) WHERE {\n"
      + "  GRAPH ckr:global {\n"
      + "    ?ctx rdf:type olap:Cell .\n"
      + "  }\n"
      + "}\n";
    
    String result = repo.executeTupleQuery(sparql);
    int count = Integer.parseInt(result.substring(result.indexOf("\n") + 1, result.lastIndexOf("\n") - 1));
    
    return count;
  }

  public int getNumberOfPredicates(Repository repository) {
    Repo repo = this.getRepository(repository);
    
    String sparql = 
        this.getSparqlPrefixes()
      + "SELECT (COUNT(DISTINCT ?p) AS ?cnt) WHERE {\n"
      + "  GRAPH ?g {\n"
      + "    ?s ?p ?o .\n"
      + "  }\n"
      + "}\n";
    
    String result = repo.executeTupleQuery(sparql);
    int count = Integer.parseInt(result.substring(result.indexOf("\n") + 1, result.lastIndexOf("\n") - 1));
    
    return count;
  }

  public int getNumberOfIndividuals(Repository repository) {
    Repo repo = this.getRepository(repository);
    
    String sparql = 
        this.getSparqlPrefixes()
      + "SELECT (COUNT(DISTINCT ?x) AS ?cnt) WHERE {\n"
      + "  {\n"
      + "    GRAPH ?g {\n"
      + "      ?x ?p ?o .\n"
      + "    }\n"
      + "  } UNION {\n"
      + "    GRAPH ?g {\n"
      + "      ?s ?p ?x .\n"
      + "    }\n"
      + "  }\n"
      + "}\n";
    
    String result = repo.executeTupleQuery(sparql);
    int count = Integer.parseInt(result.substring(result.indexOf("\n") + 1, result.lastIndexOf("\n") - 1));
    
    return count;
  }

  public int getNumberOfLiterals(Repository repository) {
    Repo repo = this.getRepository(repository);
    
    String sparql = 
        this.getSparqlPrefixes()
      + "SELECT (COUNT(DISTINCT ?x) AS ?cnt) WHERE {\n"
      + "  GRAPH ?g {\n"
      + "    ?s ?p ?x .\n"
      + "    FILTER isLiteral(?x)\n"
      + "  }\n"
      + "}\n";
    
    String result = repo.executeTupleQuery(sparql);
    int count = Integer.parseInt(result.substring(result.indexOf("\n") + 1, result.lastIndexOf("\n") - 1));
    
    return count;
  }
}
