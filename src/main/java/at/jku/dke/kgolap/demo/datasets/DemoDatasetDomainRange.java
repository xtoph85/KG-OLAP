package at.jku.dke.kgolap.demo.datasets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.DefaultKGOLAPCubeFactory;
import at.jku.dke.kgolap.KGOLAPCube;
import at.jku.dke.kgolap.KGOLAPCubeProperties;

public abstract class DemoDatasetDomainRange extends DemoDataset {
  private static final Logger logger = LoggerFactory.getLogger(DemoDatasetDomainRange.class);
  
  @Override
  public KGOLAPCube getKGOLAPCube(KGOLAPCubeProperties properties) {
    String rulesetFileName = null;
    
    switch(this.getDimensionalSize()) {
      case TINY: 
        rulesetFileName = "rulesets/atm-1D-ruleset-domain_range.ttl";
        break;
      case SMALL: 
        rulesetFileName = "rulesets/atm-2D-ruleset-domain_range.ttl";
        break;
      default:
      case MEDIUM:
        rulesetFileName = "rulesets/atm-3D-ruleset-domain_range.ttl";
        break;
      case LARGE: 
        rulesetFileName = "rulesets/atm-4D-ruleset-domain_range.ttl";
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
