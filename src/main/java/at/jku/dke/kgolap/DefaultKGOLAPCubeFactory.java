package at.jku.dke.kgolap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.repo.Repo;
import at.jku.dke.kgolap.repo.RepoFactory;
import eu.fbk.rdfpro.RuleEngine;
import eu.fbk.rdfpro.Ruleset;

public class DefaultKGOLAPCubeFactory extends KGOLAPCubeFactory {
  private static final Logger logger = LoggerFactory.getLogger(DefaultKGOLAPCubeFactory.class);

  @Override
  public KGOLAPCube createKGOLAPCube(KGOLAPCubeProperties properties) {
    Repo baseRepository = null;
    Repo tempRepository = null;
    RuleEngine ruleEngine = null;
    Map<String, String> prefixes = properties.getPrefixes();
    
    try {
      RepoFactory repoFactory = (RepoFactory) Class.forName(properties.getBaseRepoFactoryClass()).newInstance();
      logger.info("Instantiated repository class " + repoFactory.getClass() + " for base repository.");
      
      baseRepository = repoFactory.createRepo(properties.getBaseRepoProperties());
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      logger.error("Error instantiating RepoFactory class for base repository.", e);
    }
    
    try {
      RepoFactory repoFactory = (RepoFactory) Class.forName(properties.getTempRepoFactoryClass()).newInstance();
      logger.info("Instantiated repository class " + repoFactory.getClass() + " for temp repository.");
      
      tempRepository = repoFactory.createRepo(properties.getTempRepoProperties());
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      logger.error("Error instantiating RepoFactory class for temp repository.", e);
    }

    try(
      InputStream in = new ByteArrayInputStream(properties.getRulesetTtl().getBytes())
    ) {
      // parse a model from the string
      RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
      Model model = new LinkedHashModel();
      rdfParser.setRDFHandler(new StatementCollector(model));
      
      rdfParser.parse(in, "");
      
      // create a ruleset from the model
      logger.info("Create a ruleset from the string.");
      Ruleset rs = Ruleset.fromRDF(model);
      
      // add bindings
      URI inf = ValueFactoryImpl.getInstance().createURI("ckr:global-inf");   
      URI log = ValueFactoryImpl.getInstance().createURI("ckr:changelog");    
      MapBindingSet bindMap = new MapBindingSet();
      bindMap.addBinding("global_inf", inf);
      bindMap.addBinding("changelog", log);
      
      // rewrite variables with provided bindings
      rs = rs.rewriteVariables(bindMap);
      
      // create the rule engine for the ruleset
      ruleEngine = RuleEngine.create(rs);
    } catch (IOException e) {
      logger.error("Error reading input stream of ruleset.", e);
    } catch (RDFParseException e) {
      logger.error("Error parsing ruleset.", e);
    } catch (RDFHandlerException e) {
      logger.error("Error parsing ruleset.", e);
    }

    KGOLAPCube cube = null;
    
    try {
      cube = new KGOLAPCube(baseRepository, tempRepository, ruleEngine, prefixes);
    } catch (MissingPrefixException e) {
      logger.error("A prefix definition is missing.", e);
    } catch (URISyntaxException e) {
      logger.error("Invalid prefix definition", e);
    }
    
    return cube;
  }

}
