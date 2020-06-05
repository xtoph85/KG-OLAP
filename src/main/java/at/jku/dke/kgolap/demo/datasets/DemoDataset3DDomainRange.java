
package at.jku.dke.kgolap.demo.datasets;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.demo.BaseGraphGenerator;
import at.jku.dke.kgolap.demo.CompositeGraphGenerator;
import at.jku.dke.kgolap.demo.DatasetGenerator;
import at.jku.dke.kgolap.demo.GroupingGraphGenerator;
import at.jku.dke.kgolap.demo.LinearStrategy;
import at.jku.dke.kgolap.demo.ResourceFactory;
import at.jku.dke.kgolap.demo.ResourceFileUpdater;
import at.jku.dke.kgolap.demo.TemplateGraphGenerator;

public abstract class DemoDataset3DDomainRange extends DemoDatasetDomainRange {
  private static final Logger logger = LoggerFactory.getLogger(DemoDataset3DDomainRange.class);
  
  public DemoDataset3DDomainRange() {
    this.setDimensionalSize(Size.MEDIUM);
  }
  
  private int descendantsAtRegionGranularity;
  private int descendantsAtRegionYearGranularity;
  private int descendantsAtSegmentMonthTypeGranularity;
  private int descendantsAtSegmentDayTypeGranularity;
  private int descendantsAtSegmentDayModelGranularity;
  
  public int getDescendantsAtRegionGranularity() {
    return descendantsAtRegionGranularity;
  }



  public void setDescendantsAtRegionGranularity(int descendantsAtRegionGranularity) {
    this.descendantsAtRegionGranularity = descendantsAtRegionGranularity;
  }



  public int getDescendantsAtRegionYearGranularity() {
    return descendantsAtRegionYearGranularity;
  }



  public void setDescendantsAtRegionYearGranularity(int descendantsAtRegionYearGranularity) {
    this.descendantsAtRegionYearGranularity = descendantsAtRegionYearGranularity;
  }



  public int getDescendantsAtSegmentMonthTypeGranularity() {
    return descendantsAtSegmentMonthTypeGranularity;
  }



  public void setDescendantsAtSegmentMonthTypeGranularity(int descendantsAtSegmentMonthTypeGranularity) {
    this.descendantsAtSegmentMonthTypeGranularity = descendantsAtSegmentMonthTypeGranularity;
  }



  public int getDescendantsAtSegmentDayTypeGranularity() {
    return descendantsAtSegmentDayTypeGranularity;
  }



  public void setDescendantsAtSegmentDayTypeGranularity(int descendantsAtSegmentDayTypeGranularity) {
    this.descendantsAtSegmentDayTypeGranularity = descendantsAtSegmentDayTypeGranularity;
  }



  public int getDescendantsAtSegmentDayModelGranularity() {
    return descendantsAtSegmentDayModelGranularity;
  }



  public void setDescendantsAtSegmentDayModelGranularity(int descendantsAtSegmentDayModelGranularity) {
    this.descendantsAtSegmentDayModelGranularity = descendantsAtSegmentDayModelGranularity;
  }



  private int noOfFillerIterations;
  private int noOfAirports;
  private int noOfRunways;
  private int noOfTaxiways;
  private int noOfVors;
  
  
  
  public int getNoOfFillerIterations() {
    return noOfFillerIterations;
  }



  public void setNoOfFillerIterations(int noOfFillerIterations) {
    this.noOfFillerIterations = noOfFillerIterations;
  }



  public int getNoOfAirports() {
    return noOfAirports;
  }



  public void setNoOfAirports(int noOfAirports) {
    this.noOfAirports = noOfAirports;
  }



  public int getNoOfRunways() {
    return noOfRunways;
  }



  public void setNoOfRunways(int noOfRunways) {
    this.noOfRunways = noOfRunways;
  }



  public int getNoOfTaxiways() {
    return noOfTaxiways;
  }



  public void setNoOfTaxiways(int noOfTaxiways) {
    this.noOfTaxiways = noOfTaxiways;
  }



  public int getNoOfVors() {
    return noOfVors;
  }



  public void setNoOfVors(int noOfVors) {
    this.noOfVors = noOfVors;
  }



  @Override
  public void generateAndSave(File outputFile) {
    this.writeBasicCubeKnowledge(outputFile);
    
    DatasetGenerator gen = new DatasetGenerator(outputFile);
    
    /**
     * Initialize the descendant strategy
     */
    LinearStrategy descendantStrategy = new LinearStrategy();
    
    descendantStrategy.setDimensionProperty("cube:Aircraft", "cube:hasAircraft");
    descendantStrategy.setDimensionProperty("cube:Location", "cube:hasLocation");
    descendantStrategy.setDimensionProperty("cube:Date", "cube:hasDate");
    
    descendantStrategy.setLevelHierarchy("cube:Aircraft", this.getAircraftHierarchy());
    descendantStrategy.setLevelHierarchy("cube:Location", this.getLocationHierarchy());
    descendantStrategy.setLevelHierarchy("cube:Date", this.getDateHierarchy());

    /**
     * Define the dimension properties and root coordinates and granularity
     */
    Map<String, String> dimensionProperties = new HashMap<String,String>();
    dimensionProperties.put("cube:Aircraft", "cube:hasAircraft");
    dimensionProperties.put("cube:Location", "cube:hasLocation");
    dimensionProperties.put("cube:Date", "cube:hasDate");
    
    Map<String, String> rootContextCoordinates = new HashMap<String,String>();
    rootContextCoordinates.put("cube:Aircraft", "cube:Level_Aircraft_All-All");
    rootContextCoordinates.put("cube:Location", "cube:Level_Location_All-All");
    rootContextCoordinates.put("cube:Date", "cube:Level_Date_All-All");
    
    Map<String, String> rootContextGranularity = new HashMap<String,String>();
    rootContextGranularity.put("cube:Aircraft", "cube:Level_Aircraft_All");
    rootContextGranularity.put("cube:Location", "cube:Level_Location_All");
    rootContextGranularity.put("cube:Date", "cube:Level_Date_All");
    
    /**
     * Define other granularity levels for contexts
     */
    Map<String, String> regionGranularity = new HashMap<String, String>();
    regionGranularity.put("cube:Location", "cube:Level_Location_Region");
    regionGranularity.put("cube:Date", "cube:Level_Date_All");
    regionGranularity.put("cube:Aircraft", "cube:Level_Aircraft_All");
    
    Map<String, String> regionYearGranularity = new HashMap<String, String>();
    regionYearGranularity.put("cube:Location", "cube:Level_Location_Region");
    regionYearGranularity.put("cube:Date", "cube:Level_Date_Year");
    regionYearGranularity.put("cube:Aircraft", "cube:Level_Aircraft_All");
        
    Map<String, String> segmentMonthTypeGranularity = new HashMap<String, String>();
    segmentMonthTypeGranularity.put("cube:Location", "cube:Level_Location_Segment");
    segmentMonthTypeGranularity.put("cube:Date", "cube:Level_Date_Month");
    segmentMonthTypeGranularity.put("cube:Aircraft", "cube:Level_Aircraft_Type");
    
    Map<String, String> segmentDayTypeGranularity = new HashMap<String, String>();
    segmentDayTypeGranularity.put("cube:Location", "cube:Level_Location_Segment");
    segmentDayTypeGranularity.put("cube:Date", "cube:Level_Date_Day");
    segmentDayTypeGranularity.put("cube:Aircraft", "cube:Level_Aircraft_Type");
    
    Map<String, String> segmentDayModelGranularity = new HashMap<String, String>();
    segmentDayModelGranularity.put("cube:Location", "cube:Level_Location_Segment");
    segmentDayModelGranularity.put("cube:Date", "cube:Level_Date_Day");
    segmentDayModelGranularity.put("cube:Aircraft", "cube:Level_Aircraft_Model");
    
    /**
     * Set up files for shared resources and configure size
     */
    
    File airportFile = null;
    File runwayFile = null;    
    File taxiwayFile = null;
    File vorFile = null;
    
    File contaminationGroupFile = null;
    int noOfContaminationGroups = 5;
    
    File contaminationTypeFile = null;
    int noOfContaminationTypes = 15;
    
    File warningTypeFile = null;
    int noOfWarningTypes = 5;
    
    File operationalStatusFile = null;
    int noOfOperationalStatuses = 3;
    
    File operationFile = null;
    int noOfOperations = 5;
    
    File usageTypeFile = null;
    int noOfUsageTypes = 2;
    
    File interpretationFile = null;
    int noOfInterpretations = 2;
    
    try {
      airportFile = ResourceFactory.createResourceFile(0);
      runwayFile = ResourceFactory.createResourceFile(0);
      taxiwayFile = ResourceFactory.createResourceFile(0);
      vorFile = ResourceFactory.createResourceFile(0);
      contaminationGroupFile = ResourceFactory.createResourceFile(noOfContaminationGroups);
      contaminationTypeFile = ResourceFactory.createResourceFile(noOfContaminationTypes);
      warningTypeFile = ResourceFactory.createResourceFile(noOfWarningTypes);
      operationalStatusFile = ResourceFactory.createResourceFile(noOfOperationalStatuses);
      operationFile = ResourceFactory.createResourceFile(noOfOperations);
      usageTypeFile = ResourceFactory.createResourceFile(noOfUsageTypes);
      interpretationFile = ResourceFactory.createResourceFile(noOfInterpretations);
    } catch (IOException e) {
      logger.error("Error creating resources", e);
    }

    
    ResourceFileUpdater regionResourceFileUpdater = new ResourceFileUpdater();
    
    regionResourceFileUpdater.addResourceFile(airportFile);
    regionResourceFileUpdater.setNoOfInstances(airportFile, noOfAirports);
    
    regionResourceFileUpdater.addResourceFile(runwayFile);
    regionResourceFileUpdater.setNoOfInstances(runwayFile, noOfRunways);
    
    regionResourceFileUpdater.addResourceFile(taxiwayFile);
    regionResourceFileUpdater.setNoOfInstances(taxiwayFile, noOfTaxiways);
    
    regionResourceFileUpdater.addResourceFile(vorFile);
    regionResourceFileUpdater.setNoOfInstances(vorFile, noOfVors);
        
    
    /**
     * Set up the graph generator for the root context
     */
    CompositeGraphGenerator rootContextGraphGenerator = new CompositeGraphGenerator();    
        
    TemplateGraphGenerator rootContextGraphBaseVocabularyGenerator = new TemplateGraphGenerator();
    rootContextGraphBaseVocabularyGenerator.setTemplate(this.getBasicObjectKnowledgeTemplate());
    
    GroupingGraphGenerator rootContextGraphGroupingGenerator = new GroupingGraphGenerator();
    rootContextGraphGroupingGenerator.setAssertClassMembership(false);
    rootContextGraphGroupingGenerator.addGroupingProperty("obj:contaminantGrouping");
    rootContextGraphGroupingGenerator.setDomain("obj:contaminantGrouping", "obj:ContaminationType");
    rootContextGraphGroupingGenerator.setRange("obj:contaminantGrouping", "obj:ContaminationType");
    rootContextGraphGroupingGenerator.setGroupResourcesFile("obj:contaminantGrouping", contaminationGroupFile);
    rootContextGraphGroupingGenerator.setInstanceResourcesFile("obj:contaminantGrouping", contaminationTypeFile);
    rootContextGraphGroupingGenerator.setNoOfGroupsPerGrouping("obj:contaminantGrouping", noOfContaminationGroups);
    rootContextGraphGroupingGenerator.setNoOfInstancesPerGroup("obj:contaminantGrouping", noOfContaminationTypes / noOfContaminationGroups);
    
    // add graph generators to composite generator
    rootContextGraphGenerator.addGenerator(rootContextGraphBaseVocabularyGenerator);
    rootContextGraphGenerator.addGenerator(rootContextGraphGroupingGenerator);
    
    // generate the root context
    gen.generateContextAtCoordinates(rootContextCoordinates, rootContextGraphGenerator, dimensionProperties);
    
    
    /**
     * Set up the graph generator for contexts at the region level
     */
    CompositeGraphGenerator regionContextGraphGenerator = new CompositeGraphGenerator();
        
    GroupingGraphGenerator regionContextGraphRunwayGenerator = new GroupingGraphGenerator();
    
    regionContextGraphRunwayGenerator.setAssertClassMembership(false);
    
    regionContextGraphRunwayGenerator.setDomain("obj:isSituatedAt", "obj:Runway");
    regionContextGraphRunwayGenerator.setRange("obj:isSituatedAt", "obj:Airport");
      
    regionContextGraphRunwayGenerator.addGroupingProperty("obj:isSituatedAt");
    regionContextGraphRunwayGenerator.setGroupResourcesFile("obj:isSituatedAt", airportFile);
    regionContextGraphRunwayGenerator.setInstanceResourcesFile("obj:isSituatedAt", runwayFile);
    regionContextGraphRunwayGenerator.setNoOfGroupsPerGrouping("obj:isSituatedAt", noOfAirports);
    regionContextGraphRunwayGenerator.setNoOfInstancesPerGroup("obj:isSituatedAt", noOfRunways / noOfAirports);
    
    GroupingGraphGenerator regionContextGraphTaxiwayGenerator = new GroupingGraphGenerator();
    
    regionContextGraphTaxiwayGenerator.setAssertClassMembership(false);
      
    regionContextGraphTaxiwayGenerator.setDomain("obj:isSituatedAt", "obj:Taxiway");
      
    regionContextGraphTaxiwayGenerator.addGroupingProperty("obj:isSituatedAt");
    regionContextGraphTaxiwayGenerator.setGroupResourcesFile("obj:isSituatedAt", airportFile);
    regionContextGraphTaxiwayGenerator.setInstanceResourcesFile("obj:isSituatedAt", taxiwayFile);
    regionContextGraphTaxiwayGenerator.setNoOfGroupsPerGrouping("obj:isSituatedAt", noOfAirports);
    regionContextGraphTaxiwayGenerator.setNoOfInstancesPerGroup("obj:isSituatedAt", noOfTaxiways / noOfAirports);
    
    BaseGraphGenerator regionContextGraphVORGenerator = new BaseGraphGenerator();
    regionContextGraphVORGenerator.setAssertClassMembership(false);
    regionContextGraphVORGenerator.addFactClass("obj:VOR");
    regionContextGraphVORGenerator.addDefineRdfType("obj:VOR");
    regionContextGraphVORGenerator.setResourceFile("obj:VOR", vorFile);
    regionContextGraphVORGenerator.setNoOfFacts("obj:VOR", noOfVors);
    regionContextGraphVORGenerator.setDataPropertiesForFactClass("obj:VOR", new String[] {"obj:longitude", "obj:latitude"});
    regionContextGraphVORGenerator.setNoOfProperties("obj:longitude", 1);
    regionContextGraphVORGenerator.setNoOfProperties("obj:latitude", 1);
    
    
    regionContextGraphGenerator.addGenerator(regionContextGraphRunwayGenerator);
    regionContextGraphGenerator.addGenerator(regionContextGraphTaxiwayGenerator);
    regionContextGraphGenerator.addGenerator(regionContextGraphVORGenerator);

    descendantStrategy.addGranularity(regionGranularity);
    
    /**
     * Set up the graph generator for contexts at the region-year level
     */
    BaseGraphGenerator regionYearContextGraphGenerator = new BaseGraphGenerator();
    regionYearContextGraphGenerator.setAssertClassMembership(false);
    regionYearContextGraphGenerator.addFactClass("obj:VOR");
    regionYearContextGraphGenerator.setResourceFile("obj:VOR", vorFile);
    regionYearContextGraphGenerator.setNoOfFacts("obj:VOR", noOfVors);
    regionYearContextGraphGenerator.setDataPropertiesForFactClass("obj:VOR", new String[] {"obj:frequency"});
    regionYearContextGraphGenerator.setNoOfProperties("obj:frequency", 1);

    descendantStrategy.addGranularity(regionYearGranularity);
    descendantStrategy.setGraphGeneratorForGranularity(regionYearGranularity, regionYearContextGraphGenerator);
    descendantStrategy.setNoOfDescendantsAtGranularity(regionYearGranularity, descendantsAtRegionYearGranularity);
    
    /**
     * Set up the graph generator for contexts at the segment-month-type level
     */
    CompositeGraphGenerator segmentMonthTypeContextGraphGenerator = new CompositeGraphGenerator();
    
    BaseGraphGenerator segmentMonthTypeContextGraphConstructionGenerator = new BaseGraphGenerator();
    
    segmentMonthTypeContextGraphConstructionGenerator.setAssertClassMembership(false);
    
    segmentMonthTypeContextGraphConstructionGenerator.addFactClass("obj:Taxiway");
    segmentMonthTypeContextGraphConstructionGenerator.setResourceFile("obj:Taxiway", taxiwayFile);
    segmentMonthTypeContextGraphConstructionGenerator.setNoOfFacts("obj:Taxiway", noOfTaxiways / 2);
    segmentMonthTypeContextGraphConstructionGenerator.setObjectPropertiesForFactClass(
        "obj:Taxiway", new String[] {"obj:availability"}
    );
    segmentMonthTypeContextGraphConstructionGenerator.setObjectPropertyRange(
        "obj:availability", "obj:ManoeuvringAreaAvailability"
    );
    segmentMonthTypeContextGraphConstructionGenerator.setNoOfProperties("obj:availability", 2);
    
    segmentMonthTypeContextGraphConstructionGenerator.addFactClass("obj:ManoeuvringAreaAvailability");
    segmentMonthTypeContextGraphConstructionGenerator.setNoOfFacts("obj:ManoeuvringAreaAvailability", (noOfTaxiways / 2) * 2);
    segmentMonthTypeContextGraphConstructionGenerator.setObjectPropertiesForFactClass(
        "obj:ManoeuvringAreaAvailability", new String[] {"obj:warning"}
    );
    segmentMonthTypeContextGraphConstructionGenerator.setNoOfProperties("obj:warning", 1);    
    segmentMonthTypeContextGraphConstructionGenerator.setObjectPropertyRange("obj:warning", "obj:Warning");
    segmentMonthTypeContextGraphConstructionGenerator.setResourceFile("obj:Warning", warningTypeFile);
    
    segmentMonthTypeContextGraphConstructionGenerator.setDataPropertiesForFactClass(
        "obj:ManoeuvringAreaAvailability", new String[] {"obj:warningAdjacent"}
    );
    segmentMonthTypeContextGraphConstructionGenerator.setDataPropertyRange("obj:warningAdjacent", "xsd:boolean");
    segmentMonthTypeContextGraphConstructionGenerator.setNoOfProperties("obj:warningAdjacent", 1);
    
    
    BaseGraphGenerator segmentMonthTypeContextGraphFillerGenerator = new BaseGraphGenerator();
    
    segmentMonthTypeContextGraphFillerGenerator.setAssertClassMembership(false);
    
    for(int i = 0; i < noOfFillerIterations; i++) {
      String factClass = "<urn:uuid:" + UUID.randomUUID() + ">";
      
      segmentMonthTypeContextGraphFillerGenerator.addFactClass(factClass);
      segmentMonthTypeContextGraphFillerGenerator.setNoOfFacts(factClass, 10);
      
      String[] factClassDimensionProperties = new String[10];
      
      for(int j = 0; j < factClassDimensionProperties.length; j++) {
        factClassDimensionProperties[j] = "<urn:uuid:" + UUID.randomUUID() + ">";
        segmentMonthTypeContextGraphFillerGenerator.setObjectPropertyRange(
            factClassDimensionProperties[j], "<urn:uuid:" + UUID.randomUUID() + ">"
        );
        segmentMonthTypeContextGraphFillerGenerator.setNoOfProperties(factClassDimensionProperties[j], 1);
      }
      
      String[] factClassDataProperties = new String[10];
      
      for(int j = 0; j < factClassDataProperties.length; j++) {
        factClassDataProperties[j] = "<urn:uuid:" + UUID.randomUUID() + ">";
        segmentMonthTypeContextGraphFillerGenerator.setNoOfProperties(factClassDataProperties[j], 1);
      }
      
      segmentMonthTypeContextGraphFillerGenerator.setObjectPropertiesForFactClass(
          factClass, factClassDimensionProperties
      );
    }
    
    
    segmentMonthTypeContextGraphGenerator.addGenerator(segmentMonthTypeContextGraphConstructionGenerator);
    segmentMonthTypeContextGraphGenerator.addGenerator(segmentMonthTypeContextGraphFillerGenerator);
    
    descendantStrategy.addGranularity(segmentMonthTypeGranularity);
    descendantStrategy.setGraphGeneratorForGranularity(segmentMonthTypeGranularity, segmentMonthTypeContextGraphGenerator);
    descendantStrategy.setNoOfDescendantsAtGranularity(segmentMonthTypeGranularity, descendantsAtSegmentMonthTypeGranularity);
    
    /**
     * Set up the graph generator for contexts at the segment-day level
     */
    CompositeGraphGenerator segmentDayTypeContextGraphGenerator = new CompositeGraphGenerator();
    
    BaseGraphGenerator segmentDayTypeContextGraphRunwayContaminationGenerator = new BaseGraphGenerator();
    
    segmentDayTypeContextGraphRunwayContaminationGenerator.setAssertClassMembership(false);
    
    segmentDayTypeContextGraphRunwayContaminationGenerator.addFactClass("obj:Runway");
    segmentDayTypeContextGraphRunwayContaminationGenerator.setResourceFile("obj:Runway", runwayFile);
    segmentDayTypeContextGraphRunwayContaminationGenerator.setNoOfFacts("obj:Runway", noOfRunways / 2);
    
    segmentDayTypeContextGraphRunwayContaminationGenerator.setObjectPropertiesForFactClass("obj:Runway", new String[] {"obj:contaminant"});
    segmentDayTypeContextGraphRunwayContaminationGenerator.setNoOfProperties("obj:contaminant", 1);
    segmentDayTypeContextGraphRunwayContaminationGenerator.setObjectPropertyRange("obj:contaminant", "obj:SurfaceContamination");
    
    segmentDayTypeContextGraphRunwayContaminationGenerator.addFactClass("obj:SurfaceContamination");
    segmentDayTypeContextGraphRunwayContaminationGenerator.setNoOfFacts("obj:SurfaceContamination", noOfRunways / 2);
    
    segmentDayTypeContextGraphRunwayContaminationGenerator.setDataPropertiesForFactClass("obj:SurfaceContamination", new String[] {"obj:depth"});
    segmentDayTypeContextGraphRunwayContaminationGenerator.setNoOfProperties("obj:depth", 1);
    
    segmentDayTypeContextGraphRunwayContaminationGenerator.setObjectPropertiesForFactClass("obj:SurfaceContamination", new String[] {"obj:layer"});
    segmentDayTypeContextGraphRunwayContaminationGenerator.setObjectPropertyRange("obj:layer", "obj:SurfaceContaminationLayer");
    segmentDayTypeContextGraphRunwayContaminationGenerator.setNoOfProperties("obj:layer", 3);
    
    segmentDayTypeContextGraphRunwayContaminationGenerator.addFactClass("obj:SurfaceContaminationLayer");
    segmentDayTypeContextGraphRunwayContaminationGenerator.setNoOfFacts("obj:SurfaceContaminationLayer", (noOfRunways / 2) * 3);

    segmentDayTypeContextGraphRunwayContaminationGenerator.setObjectPropertiesForFactClass("obj:SurfaceContaminationLayer", new String[] {"obj:contaminationType"});
    segmentDayTypeContextGraphRunwayContaminationGenerator.setObjectPropertyRange("obj:contaminationType", "obj:ContaminationType");
    segmentDayTypeContextGraphRunwayContaminationGenerator.setResourceFile("obj:ContaminationType", contaminationTypeFile);
    segmentDayTypeContextGraphRunwayContaminationGenerator.setNoOfProperties("obj:contaminationType", 1);
    

    BaseGraphGenerator segmentDayTypeContextGraphTaxiwayContaminationGenerator = new BaseGraphGenerator();
    
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setAssertClassMembership(false);
    
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.addFactClass("obj:Taxiway");
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setResourceFile("obj:Taxiway", taxiwayFile);
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setNoOfFacts("obj:Taxiway", noOfTaxiways / 2);
    
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setObjectPropertiesForFactClass("obj:Taxiway", new String[] {"obj:contaminant"});
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setNoOfProperties("obj:contaminant", 1);
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setObjectPropertyRange("obj:contaminant", "obj:SurfaceContamination");
    
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.addFactClass("obj:SurfaceContamination");
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setNoOfFacts("obj:SurfaceContamination", noOfTaxiways / 2);
    
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setDataPropertiesForFactClass("obj:SurfaceContamination", new String[] {"obj:depth"});
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setNoOfProperties("obj:depth", 1);
    
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setObjectPropertiesForFactClass("obj:SurfaceContamination", new String[] {"obj:layer"});
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setObjectPropertyRange("obj:layer", "obj:SurfaceContaminationLayer");
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setNoOfProperties("obj:layer", 3);
    
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.addFactClass("obj:SurfaceContaminationLayer");
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setNoOfFacts("obj:SurfaceContaminationLayer", (noOfTaxiways / 2) * 3);
    
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setObjectPropertiesForFactClass("obj:SurfaceContaminationLayer", new String[] {"obj:contaminationType"});
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setObjectPropertyRange("obj:contaminationType", "obj:ContaminationType");
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setResourceFile("obj:ContaminationType", contaminationTypeFile);
    segmentDayTypeContextGraphTaxiwayContaminationGenerator.setNoOfProperties("obj:contaminationType", 1);

    
    segmentDayTypeContextGraphGenerator.addGenerator(segmentDayTypeContextGraphRunwayContaminationGenerator);
    segmentDayTypeContextGraphGenerator.addGenerator(segmentDayTypeContextGraphTaxiwayContaminationGenerator);
    
    descendantStrategy.addGranularity(segmentDayTypeGranularity);
    descendantStrategy.setGraphGeneratorForGranularity(segmentDayTypeGranularity, segmentDayTypeContextGraphGenerator);
    descendantStrategy.setNoOfDescendantsAtGranularity(segmentDayTypeGranularity, descendantsAtSegmentDayTypeGranularity);
    
    /**
     * Set up the graph generator for contexts at the segment-day-model level
     */
    CompositeGraphGenerator segmentDayModelContextGraphGenerator = new CompositeGraphGenerator();
    
    
    BaseGraphGenerator segmentDayModelContextGraphWingspanClosureGenerator = new BaseGraphGenerator();
    
    segmentDayModelContextGraphWingspanClosureGenerator.setAssertClassMembership(false);
    
    segmentDayModelContextGraphWingspanClosureGenerator.addFactClass("obj:Taxiway");
    segmentDayModelContextGraphWingspanClosureGenerator.setResourceFile("obj:Taxiway", taxiwayFile);
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfFacts("obj:Taxiway", noOfTaxiways);
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertiesForFactClass(
        "obj:Taxiway", new String[] {"obj:availability"}
    );
    
    segmentDayModelContextGraphWingspanClosureGenerator.addFactClass("obj:Runway");
    segmentDayModelContextGraphWingspanClosureGenerator.setResourceFile("obj:Runway", runwayFile);
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfFacts("obj:Runway", noOfRunways);
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertiesForFactClass(
        "obj:Runway", new String[] {"obj:availability"}
    );
    
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfProperties("obj:availability", 1);
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertyRange(
        "obj:availability", "obj:ManoeuvringAreaAvailability"
    );
    
    segmentDayModelContextGraphWingspanClosureGenerator.addFactClass("obj:ManoeuvringAreaAvailability");
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfFacts(
        "obj:ManoeuvringAreaAvailability", noOfTaxiways + noOfRunways
    );
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertiesForFactClass(
        "obj:ManoeuvringAreaAvailability", new String[] {"obj:operationalStatus", "obj:usage"}
    );
    
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertyRange("obj:operationalStatus", "obj:Status");
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfProperties("obj:operationalStatus", 1);
    segmentDayModelContextGraphWingspanClosureGenerator.setResourceFile("obj:Status", operationalStatusFile);
    
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertyRange("obj:usage", "obj:ManoeuvringAreaUsage");
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfProperties("obj:usage", 1);
    
    segmentDayModelContextGraphWingspanClosureGenerator.addFactClass("obj:ManoeuvringAreaUsage");
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfFacts("obj:ManoeuvringAreaUsage", (noOfTaxiways) + (noOfRunways));
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertiesForFactClass(
        "obj:ManoeuvringAreaUsage", new String[] {"obj:usageType", "obj:operation", "obj:aircraft"}
    );
    
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertyRange("obj:usageType", "obj:UsageType");
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfProperties("obj:usageType", 1);
    segmentDayModelContextGraphWingspanClosureGenerator.setResourceFile("obj:UsageType", usageTypeFile);
    
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertyRange("obj:operation", "obj:Operation");
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfProperties("obj:operation", 1);
    segmentDayModelContextGraphWingspanClosureGenerator.setResourceFile("obj:Operation", operationFile);
    
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertyRange("obj:aircraft", "obj:AircraftCharacteristic");
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfProperties("obj:aircraft", 1);
    
    segmentDayModelContextGraphWingspanClosureGenerator.addFactClass("obj:AircraftCharacteristic");
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfFacts("obj:AircraftCharacteristic", (noOfTaxiways) + (noOfRunways));
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertiesForFactClass(
        "obj:AircraftCharacteristic", new String[] {"obj:wingspanInterpretation"}
    );
    segmentDayModelContextGraphWingspanClosureGenerator.setDataPropertiesForFactClass(
        "obj:AircraftCharacteristic", new String[] {"obj:wingspan"}
    );

    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfProperties("obj:wingspan", 1);
    
    segmentDayModelContextGraphWingspanClosureGenerator.setObjectPropertyRange(
        "obj:wingspanInterpretation", "obj:Interpretation"
    );
    segmentDayModelContextGraphWingspanClosureGenerator.setNoOfProperties("obj:wingspanInterpretation", 1);
    segmentDayModelContextGraphWingspanClosureGenerator.setResourceFile("obj:Interpretation", interpretationFile);
    
    
    BaseGraphGenerator segmentDayModelContextGraphWeightClosureGenerator = new BaseGraphGenerator();
    
    segmentDayModelContextGraphWeightClosureGenerator.setAssertClassMembership(false);
    
    segmentDayModelContextGraphWeightClosureGenerator.addFactClass("obj:Taxiway");
    segmentDayModelContextGraphWeightClosureGenerator.setResourceFile("obj:Taxiway", taxiwayFile);
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfFacts("obj:Taxiway", noOfTaxiways);
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertiesForFactClass(
        "obj:Taxiway", new String[] {"obj:availability"}
    );
    
    segmentDayModelContextGraphWeightClosureGenerator.addFactClass("obj:Runway");
    segmentDayModelContextGraphWeightClosureGenerator.setResourceFile("obj:Runway", runwayFile);
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfFacts("obj:Runway", noOfRunways);
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertiesForFactClass(
        "obj:Runway", new String[] {"obj:availability"}
    );
    
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfProperties("obj:availability", 1);
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertyRange(
        "obj:availability", "obj:ManoeuvringAreaAvailability"
    );
    
    segmentDayModelContextGraphWeightClosureGenerator.addFactClass("obj:ManoeuvringAreaAvailability");
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfFacts(
        "obj:ManoeuvringAreaAvailability", (noOfTaxiways) + (noOfRunways)
    );
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertiesForFactClass(
        "obj:ManoeuvringAreaAvailability", new String[] {"obj:operationalStatus", "obj:usage"}
    );
    
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertyRange("obj:operationalStatus", "obj:Status");
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfProperties("obj:operationalStatus", 1);
    segmentDayModelContextGraphWeightClosureGenerator.setResourceFile("obj:Status", operationalStatusFile);
    
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertyRange("obj:usage", "obj:ManoeuvringAreaUsage");
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfProperties("obj:usage", 1);
    
    segmentDayModelContextGraphWeightClosureGenerator.addFactClass("obj:ManoeuvringAreaUsage");
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfFacts("obj:ManoeuvringAreaUsage", (noOfTaxiways) + (noOfRunways));
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertiesForFactClass(
        "obj:ManoeuvringAreaUsage", new String[] {"obj:usageType", "obj:operation", "obj:aircraft"}
    );
    
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertyRange("obj:usageType", "obj:UsageType");
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfProperties("obj:usageType", 1);
    segmentDayModelContextGraphWeightClosureGenerator.setResourceFile("obj:UsageType", usageTypeFile);
    
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertyRange("obj:operation", "obj:Operation");
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfProperties("obj:operation", 1);
    segmentDayModelContextGraphWeightClosureGenerator.setResourceFile("obj:Operation", operationFile);
    
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertyRange("obj:aircraft", "obj:AircraftCharacteristic");
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfProperties("obj:aircraft", 1);
    
    segmentDayModelContextGraphWeightClosureGenerator.addFactClass("obj:AircraftCharacteristic");
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfFacts("obj:AircraftCharacteristic", (noOfTaxiways) + (noOfRunways));
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertiesForFactClass(
        "obj:AircraftCharacteristic", new String[] {"obj:weightInterpretation"}
    );
    segmentDayModelContextGraphWeightClosureGenerator.setDataPropertiesForFactClass(
        "obj:AircraftCharacteristic", new String[] {"obj:weight"}
    );
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfProperties("obj:weight", 1);
    
    segmentDayModelContextGraphWeightClosureGenerator.setObjectPropertyRange(
        "obj:weightInterpretation", "obj:Interpretation"
    );
    segmentDayModelContextGraphWeightClosureGenerator.setNoOfProperties("obj:weightInterpretation", 1);
    segmentDayModelContextGraphWeightClosureGenerator.setResourceFile("obj:Interpretation", interpretationFile);
    
    
    BaseGraphGenerator segmentDayModelContextGraphFillerGenerator = new BaseGraphGenerator();
    
    segmentDayModelContextGraphFillerGenerator.setAssertClassMembership(false);
    
    for(int i = 0; i < noOfFillerIterations; i++) {
      String factClass = "<urn:uuid:" + UUID.randomUUID() + ">";
      
      segmentDayModelContextGraphFillerGenerator.addFactClass(factClass);
      segmentDayModelContextGraphFillerGenerator.setNoOfFacts(factClass, 10);
      
      String[] factClassDimensionProperties = new String[10];
      
      for(int j = 0; j < factClassDimensionProperties.length; j++) {
        factClassDimensionProperties[j] = "<urn:uuid:" + UUID.randomUUID() + ">";
        segmentDayModelContextGraphFillerGenerator.setObjectPropertyRange(
            factClassDimensionProperties[j], "<urn:uuid:" + UUID.randomUUID() + ">"
        );
        segmentDayModelContextGraphFillerGenerator.setNoOfProperties(factClassDimensionProperties[j], 1);
      }
      
      String[] factClassDataProperties = new String[10];
      
      for(int j = 0; j < factClassDataProperties.length; j++) {
        factClassDataProperties[j] = "<urn:uuid:" + UUID.randomUUID() + ">";
        segmentDayModelContextGraphFillerGenerator.setNoOfProperties(factClassDataProperties[j], 1);
      }
      
      segmentDayModelContextGraphFillerGenerator.setObjectPropertiesForFactClass(
          factClass, factClassDimensionProperties
      );
    }
    

    segmentDayModelContextGraphGenerator.addGenerator(segmentDayModelContextGraphWingspanClosureGenerator);
    segmentDayModelContextGraphGenerator.addGenerator(segmentDayModelContextGraphWeightClosureGenerator);
    segmentDayModelContextGraphGenerator.addGenerator(segmentDayModelContextGraphFillerGenerator);
    
    descendantStrategy.addGranularity(segmentDayModelGranularity);
    descendantStrategy.setGraphGeneratorForGranularity(segmentDayModelGranularity, segmentDayModelContextGraphGenerator);
    descendantStrategy.setNoOfDescendantsAtGranularity(segmentDayModelGranularity, descendantsAtSegmentDayModelGranularity);
    
    gen.generateDescendantsAtGranularity(
      rootContextCoordinates, 
      rootContextGranularity, 
      descendantsAtRegionGranularity, 
      regionGranularity,
      null,
      regionContextGraphGenerator, 
      regionResourceFileUpdater,
      descendantStrategy, 
      dimensionProperties
    );
  }  
}
