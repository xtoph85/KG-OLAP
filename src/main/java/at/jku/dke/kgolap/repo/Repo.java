package at.jku.dke.kgolap.repo;

import java.io.InputStream;
import java.io.OutputStream;

import eu.fbk.rdfpro.RuleEngine;
import eu.fbk.rdfpro.util.QuadModel;

public abstract class Repo {
  // shutdown method
  public abstract void startUp();
  public abstract void shutDown();
  
  // query and management methods
  public abstract void executeUpdate(String sparql);
  public abstract void loadTriG(String fileName);
  public abstract void loadNQuads(String fileName);
  public abstract void loadTriG(InputStream in);
  public abstract void loadNQuads(InputStream in);
  public abstract void loadTriGFromString(String triG);
  public abstract void processDelta(InputStream in);
  public abstract void executeTupleQuery(String sparql, OutputStream out);
  public abstract String executeTupleQuery(String sparql);
  public abstract void executeQuadQuery(String sparql, OutputStream out);
  public abstract void executeGraphQuery(String sparql, OutputStream out);
  public abstract void executeDeltaQuery(String sparql, Repo target);
  public abstract boolean ask(String sparql);
  public abstract void evaluateRules(RuleEngine ruleEngine);
  public abstract void clearRepository();
  public abstract void export(String fileName);
  public abstract void export(OutputStream out);
  public abstract void add(QuadModel model);
  public abstract void remove(QuadModel model);
  
  // implementation
  private boolean benchmarkingMode = false;
  
  public boolean isInBenchmarkingMode() {
    return benchmarkingMode;
  }
  
  public void setBenchmarkingMode(boolean benchmarkingMode) {
    this.benchmarkingMode = benchmarkingMode;
  }
}
