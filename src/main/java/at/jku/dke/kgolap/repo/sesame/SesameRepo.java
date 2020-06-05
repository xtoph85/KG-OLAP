package at.jku.dke.kgolap.repo.sesame;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import com.sun.management.OperatingSystemMXBean;

import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.trig.TriGWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.repo.Repo;
import eu.fbk.rdfpro.RDFHandlers;
import eu.fbk.rdfpro.RuleEngine;
import eu.fbk.rdfpro.util.QuadModel;

@SuppressWarnings("restriction")
public abstract class SesameRepo extends Repo {
  private static final Logger logger = LoggerFactory.getLogger(SesameRepo.class);
  private static final Logger benchmarking = LoggerFactory.getLogger("benchmarking");
  private static final Logger benchmarkingStatistics = LoggerFactory.getLogger("benchmarking-statistics");
  
  private Repository repository = null;
  
  public SesameRepo(Repository repository) {
    this.repository = repository;
  }
  
  @Override
  public void startUp() {
    try {
      repository.initialize();
    } catch (RepositoryException e) {
      logger.error("Error initializing Sesame repository.");
    }
  }

  @Override
  public void shutDown() {
    try {
      repository.shutDown();
    } catch (RepositoryException e) {
      logger.error("Error shutting down Sesame repository.");
    }
  }

  @Override
  public void executeUpdate(String sparql) {
    RepositoryConnection con = null;
    
    try {
      con = repository.getConnection();
      
      try {
        Update update = con.prepareUpdate(QueryLanguage.SPARQL, sparql);
        
        update.execute();
      } catch (RepositoryException | MalformedQueryException e) {
        logger.error("Error preparing query.\n" + sparql, e);
      } catch (UpdateExecutionException e) {
        logger.error("Error executing query.\n" + sparql, e);
      }
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }

  @Override
  public void loadNQuads(String fileName) {
    File inFile = new File(fileName);
    
    try(
      InputStream in = new FileInputStream(inFile)
    ){
      this.loadNQuads(in);
    } catch (FileNotFoundException e) {
      logger.error("File not found.", e);
    } catch (IOException e) {
      logger.error("Error reading input file.", e);
    }
  }

  @Override
  public void loadNQuads(InputStream in) {
    RepositoryConnection con = null;
      
    try {      
      con = repository.getConnection();
      
      con.add(in, "", RDFFormat.NQUADS);
      
      logger.info("RDF triples added.");
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } catch (RDFParseException e) {
      logger.error("Error parsing input RDF data.", e);
    } catch (IOException e) {
      logger.error("Error reading input stream.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }

  @Override
  public void loadTriG(String fileName) {
    File inFile = new File(fileName);
    
    try(
      InputStream in = new FileInputStream(inFile)
    ){
      this.loadTriG(in);
    } catch (FileNotFoundException e) {
      logger.error("File not found.", e);
    } catch (IOException e) {
      logger.error("Error reading input file.", e);
    }
  }

  @Override
  public void loadTriG(InputStream in) {
    RepositoryConnection con = null;
      
    try {      
      con = repository.getConnection();
      
      con.add(in, "", RDFFormat.TRIG);
      
      logger.info("RDF triples added.");
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } catch (RDFParseException e) {
      logger.error("Error parsing input RDF data.", e);
    } catch (IOException e) {
      logger.error("Error reading input stream.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }

  @Override
  public void loadTriGFromString(String triG) {
    try(
      InputStream in = new ByteArrayInputStream(triG.getBytes())
    ) {
      this.loadTriG(in);
    } catch (IOException e) {
      logger.error("Error reading input stream.", e);
    }
  }

  @Override
  public String executeTupleQuery(String sparql) {
    RepositoryConnection con = null;
    String result = null;
    
    try {      
      con = repository.getConnection();
      
      try {
        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        
        try (
          ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {          
          TupleQueryResultHandler handler = new SPARQLResultsCSVWriter(out);
          
          query.evaluate(handler);
          
          result = out.toString();
        } catch (QueryEvaluationException e) {
          logger.error("Error evaluating query.\n" + sparql, e);
        } catch (TupleQueryResultHandlerException e) {
          logger.error("Error handling the result set.\n", e);
        } catch (IOException e) {
          logger.error("Error writing result to main memory.", e);
        }
      } catch (RepositoryException | MalformedQueryException e) {
        logger.error("Error preparing query.\n" + sparql, e);
      } 
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }   
    
    return result;
  }

  @Override
  public void executeTupleQuery(String sparql, OutputStream out) {
    RepositoryConnection con = null;
    
    try {      
      con = repository.getConnection();
      
      try {
        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        
        try {          
          TupleQueryResultHandler handler = new SPARQLResultsCSVWriter(out);
          
          query.evaluate(handler);
        } catch (QueryEvaluationException e) {
          logger.error("Error evaluating query.\n" + sparql, e);
        } catch (TupleQueryResultHandlerException e) {
          logger.error("Error handling the result set.\n", e);
        }
      } catch (RepositoryException | MalformedQueryException e) {
        logger.error("Error preparing query.\n" + sparql, e);
      } 
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }    
  }

  @Override
  public void executeQuadQuery(String sparql, OutputStream out) {
    RepositoryConnection con = null;
    
    try {      
      con = repository.getConnection();
      
      try {
        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        
        try {          
          TupleQueryResultHandler handler = new QuadQueryResultsNQuadsWriter(out);
          
          query.evaluate(handler);
        } catch (QueryEvaluationException e) {
          logger.error("Error evaluating query.\n" + sparql, e);
        } catch (TupleQueryResultHandlerException e) {
          logger.error("Error handling the result set.\n", e);
        }
      } catch (RepositoryException | MalformedQueryException e) {
        logger.error("Error preparing query.\n" + sparql, e);
      } 
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }    
  }
  
  @Override
  public void processDelta(InputStream in) {
    RepositoryConnection con = null;
    OperatingSystemMXBean osMxBean = null;
    
    try {
      con = repository.getConnection();
      
      QuadModel insertStatements = QuadModel.create();
      QuadModel deleteStatements = QuadModel.create();


      if(isInBenchmarkingMode()) {
        osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        
        benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + this.getClass().getSimpleName() + "-ProcessDelta-ReadDelta,"
            + "begin"
        );
      }
      
      try(
        Reader reader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(reader);
      ) {
        String[] headers = bufferedReader.readLine().split(",");
        Map<String,Integer> positions = new HashMap<String,Integer>();
        
        for(int i = 0; i < headers.length; i++) {
          positions.put(headers[i], i);
        }
        
        UrlValidator urlValidator = new UrlValidator();
        IntegerValidator intValidator = new IntegerValidator();
        DoubleValidator doubleValidator = new DoubleValidator();
        
        ValueFactory factory = ValueFactoryImpl.getInstance();
        
        for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
          String[] values = line.split(",");
          
          Resource subject = null;
          String subjectString = values[positions.get("s")];
          
          URI predicate = null;
          String predicateString = values[positions.get("p")];
          
          Value object = null;
          String objectString = values[positions.get("o")];
          
          Resource context = null;
          String contextString = values[positions.get("g")];
          
          if(subjectString.startsWith("_:")) {
            subject = factory.createBNode(subjectString);
          } else if(urlValidator.isValid(subjectString)) {
            subject = factory.createURI(subjectString);
          } else {
            subject = factory.createURI(subjectString);
          };
          
          if(urlValidator.isValid(predicateString)) {
            predicate = factory.createURI(predicateString);
          } else if (predicateString.startsWith("urn:uuid:")) {
            predicate = factory.createURI(predicateString);
          }
          
          if(objectString.equals("ckr:global-inf")) {
            object = factory.createURI(objectString);
          } else if(objectString.startsWith("_:")) {
            object = factory.createBNode(objectString);
          } else if(objectString.startsWith("urn:uuid:")) {
            object = factory.createURI(objectString);
          } else if(urlValidator.isValid(objectString)) {
            object = factory.createURI(objectString);
          } else if(intValidator.isValid(objectString)) {
            object = factory.createLiteral(Integer.parseInt(objectString));
          } else if(doubleValidator.isValid(objectString)) {
            object = factory.createLiteral(Double.parseDouble(objectString));
          } else if(objectString.equals("true") || objectString.equals("false")) {
            object = factory.createLiteral(Boolean.parseBoolean(objectString));  
          } else {
            object = factory.createLiteral("\"" + objectString + "\"");
          }
          
          context = factory.createURI(contextString);
          
          if(values[positions.get("op")].equals("+")) {
            insertStatements.add(subject, predicate, object, context);
          } else {
            deleteStatements.add(subject, predicate, object, context);
          }
        }
        
        if(isInBenchmarkingMode()) {          
          benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + this.getClass().getSimpleName() + "-ProcessDelta-ReadDelta,"
              + "end"
          );
                    
          benchmarkingStatistics.info(
                System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + "DeltaSize-StatementsInserted,"
              + insertStatements.size()
          );
          
          benchmarkingStatistics.info(
                System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + "DeltaSize-StatementsDeleted,"
              + deleteStatements.size()
          );
          
          benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + this.getClass().getSimpleName() + "-ProcessDelta-DeleteStatements,"
              + "begin"
          );
        }
        
        logger.info("Remove statements (delta delete).");
        con.remove(deleteStatements);

        if(isInBenchmarkingMode()) {
          benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + this.getClass().getSimpleName() + "-ProcessDelta-DeleteStatements,"
              + "end"
          );
          
          benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + this.getClass().getSimpleName() + "-ProcessDelta-InsertStatements,"
              + "begin"
          );
        }

        logger.info("Add statements (delta insert).");
        con.add(insertStatements);

        if(isInBenchmarkingMode()) {
          benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + this.getClass().getSimpleName() + "-ProcessDelta-InsertStatements,"
              + "end"
          );
        }
      } catch (IOException e) {
        logger.error("Error reading input stream.", e);
      }
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository " + repository, e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }

  @Override
  public void executeGraphQuery(String sparql, OutputStream out) {
    RepositoryConnection con = null;
    
    try {
      con = repository.getConnection();
      
      try {
        GraphQuery query = con.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
        
        try {
          TriGWriter handler = new TriGWriter(out);
          
          query.evaluate(handler);
        } catch (QueryEvaluationException e) {
          logger.error("Error evaluating query.\n" + sparql, e);
        } catch (RDFHandlerException e) {
          logger.error("Error handling the result set.\n", e);
        }
      } catch (RepositoryException | MalformedQueryException e) {
        logger.error("Error preparing query.\n" + sparql, e);
      } 
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository " + repository, e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }

  @Override
  public boolean ask(String sparql) {
    RepositoryConnection con = null;
    
    boolean result = false;
    
    try {      
      con = repository.getConnection();
      
      try {
        BooleanQuery query = con.prepareBooleanQuery(QueryLanguage.SPARQL, sparql);
        
        try {
          result = query.evaluate();
        } catch (QueryEvaluationException e) {
          logger.error("Error evaluating query.\n" + sparql, e);
        }
      } catch (RepositoryException | MalformedQueryException e) {
        logger.error("Error preparing query.\n" + sparql, e);
      } 
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
    
    return result;
  }

  @Override
  public void evaluateRules(RuleEngine ruleEngine) {
    RepositoryConnection con = null;
    OperatingSystemMXBean osMxBean = null;
    
    try {
      // connect to repository
      con = repository.getConnection();
      
      // download all data to a local index
      logger.info("Load data to local main memory.");
      
      if(this.isInBenchmarkingMode()) {
          osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
          
          benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
    	      + "RuleEvaluation-LoadModel,"
    	      + "begin"
    	  );
      }
      
      QuadModel model = QuadModel.create();
      con.exportStatements(null, null, null, true, RDFHandlers.wrap(model));
      
      if(this.isInBenchmarkingMode()) {
    	  benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
    	      + "RuleEvaluation-LoadModel,"
    	      + "end"
    	  );
          
    	  benchmarkingStatistics.info(
                System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + "ModelSize-StatementsAsserted,"
              + model.size()
          );
      }

      
      // perform inference; model is augmented with results
      logger.info("Perform inferencing on local model.");

      if(this.isInBenchmarkingMode()) {        
    	  benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
    	      + "RuleEvaluation-Evaluate,"
    	      + "begin"
    	  );
      }
      
      ruleEngine.eval(model);

      if(this.isInBenchmarkingMode()) {
    	  benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
    	      + "RuleEvaluation-Evaluate,"
    	      + "end"
    	  );
    	  
    	  benchmarkingStatistics.info(
                System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + "ModelSize-StatementsAssertedInferred,"
              + model.size()
    	  );
      }
      
      // write everything back to the repository (including input
      // statements which are silently ignored on the other side)
      logger.info("Write back inferred model with " + model.size() + " statements to the repository.");

      
      
      if(this.isInBenchmarkingMode()) {
    	  benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
    	      + "RuleEvaluation-AddInferences,"
    	      + "begin"
    	  );
      }
      
      con.add(model);

      if(this.isInBenchmarkingMode()) {
    	  benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
    	      + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
    	      + "RuleEvaluation-AddInferences,"
    	      + "end"
    	  );
      }
      
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } catch (RDFHandlerException e) {
      logger.error("Error handling the repository data.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }

  @Override
  public void clearRepository() {
    RepositoryConnection con = null;
    
    try {      
      con = repository.getConnection();
      
      con.clear();
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }

  @Override
  public void export(OutputStream out) {
    RepositoryConnection con = null;
    
    try {
      con = repository.getConnection();
      
      TriGWriter handler = new TriGWriter(out);
      
      con.export(handler);
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository " + repository + ".", e);
    } catch (RDFHandlerException e) {
      logger.error("Error handling RDF output.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }
  
  @Override
  public void export(String fileName) {
    File outFile = new File(fileName);
    
    try(
      OutputStream out = new FileOutputStream(outFile)
    ){
      this.export(out);
    } catch (FileNotFoundException e) {
      logger.error("File not found.", e);
    } catch (IOException e) {
      logger.error("Error writing to output file.", e);
    }
  }
  
  @Override
  public void add(QuadModel statements) {
    RepositoryConnection con = null;
    
    try {
      con = repository.getConnection();
      
      con.add(statements);
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository " + repository + ".", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }
  
  @Override
  public void remove(QuadModel statements) {
    RepositoryConnection con = null;
    
    try {
      con = repository.getConnection();
      
      con.remove(statements);
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository " + repository + ".", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }
  }
  
  @Override
  public void executeDeltaQuery(String sparql, Repo target) {
    RepositoryConnection con = null;
    OperatingSystemMXBean osMxBean = null;
    
    try {      
      con = repository.getConnection();
      
      try {
        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
        
        try {
          QuadModel insertStatements = QuadModel.create();
          QuadModel deleteStatements = QuadModel.create();
          
          DeltaQueryResultHandler handler = new DeltaQueryResultHandler(insertStatements, deleteStatements);
          
          if(isInBenchmarkingMode()) {
            osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            
            benchmarking.info(
                  osMxBean.getProcessCpuTime() + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + this.getClass().getSimpleName() + "-ExecuteDeltaQuery-Evaluate,"
                + "begin"
            );
          }
          
          query.evaluate(handler);
          
          if(isInBenchmarkingMode()) {          
            benchmarking.info(
                  osMxBean.getProcessCpuTime() + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + this.getClass().getSimpleName() + "-ExecuteDeltaQuery-Evaluate,"
                + "end"
            );
            
            benchmarking.info(
                  osMxBean.getProcessCpuTime() + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + this.getClass().getSimpleName() + "-ProcessDelta-DeleteStatements,"
                + "begin"
            );
          }

          logger.info("Remove statements (delta delete).");
          target.remove(deleteStatements);

          if(isInBenchmarkingMode()) {          
            benchmarking.info(
                  osMxBean.getProcessCpuTime() + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + this.getClass().getSimpleName() + "-ProcessDelta-DeleteStatements,"
                + "end"
            );
            
            benchmarking.info(
                  osMxBean.getProcessCpuTime() + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + this.getClass().getSimpleName() + "-ProcessDelta-InsertStatements,"
                + "begin"
            );
          }

          logger.info("Add statements (delta insert).");
          target.add(insertStatements);
          
          if(isInBenchmarkingMode()) {          
            benchmarking.info(
                  osMxBean.getProcessCpuTime() + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + this.getClass().getSimpleName() + "-ProcessDelta-InsertStatements,"
                + "end"
            );
            
            benchmarkingStatistics.info(
                  System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + "DeltaSize-StatementsInserted,"
                + insertStatements.size()
            );
            
            benchmarkingStatistics.info(
                  System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
                + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
                + "DeltaSize-StatementsDeleted,"
                + deleteStatements.size()
            );
            
            String benchmarkId = System.getProperty("at.jku.dke.kgolap.demo.benchmark.id");
            String benchmarkDirectory = System.getProperty("at.jku.dke.kgolap.demo.benchmark.dir");
              
            File filePersistInsertStatements = new File(
              benchmarkDirectory + File.separator + benchmarkId + ".delta.insert.nq"
            );
            
            if(!filePersistInsertStatements.exists()) {
              try {
                filePersistInsertStatements.createNewFile();
              } catch (IOException e) {
                  benchmarking.error("Could not create file to store the delta (inserts).");
              }
              
              try(OutputStream out = new FileOutputStream(filePersistInsertStatements)) {
                RDFWriter writer = Rio.createWriter(RDFFormat.NQUADS, out);
                
                writer.startRDF();
                
                for (Statement statement: insertStatements) {
                   writer.handleStatement(statement);
                }
                
                writer.endRDF();
              } catch (FileNotFoundException e) {
                benchmarking.error("Error storing delta (inserts) in benchmark directory.");
              } catch (IOException e) {
                benchmarking.error("Error writing query plan.");
              } catch (RDFHandlerException e) {
                benchmarking.error("Error writing delta (inserts) in benchmark directory.");
              }
            }

            
            File filePersistDeleteStatements = new File(
              benchmarkDirectory + File.separator + benchmarkId + ".delta.delete.nq"
            );
            
            if(!filePersistDeleteStatements.exists()) {
              try {
                filePersistDeleteStatements.createNewFile();
              } catch (IOException e) {
                  benchmarking.error("Could not create file to store the delta (deletes).");
              }
              
              try(OutputStream out = new FileOutputStream(filePersistDeleteStatements)) {
                RDFWriter writer = Rio.createWriter(RDFFormat.NQUADS, out);
                
                writer.startRDF();
                
                for (Statement statement: deleteStatements) {
                   writer.handleStatement(statement);
                }
                
                writer.endRDF();
              } catch (FileNotFoundException e) {
                benchmarking.error("Error storing delta (deletes) in benchmark directory.");
              } catch (IOException e) {
                benchmarking.error("Error writing query plan.");
              } catch (RDFHandlerException e) {
                benchmarking.error("Error writing delta (deletes) in benchmark directory.");
              }
            }
          }
        } catch (QueryEvaluationException e) {
          logger.error("Error evaluating query.\n" + sparql, e);
        } catch (TupleQueryResultHandlerException e) {
          logger.error("Error handling the result set.\n", e);
        }
      } catch (RepositoryException | MalformedQueryException e) {
        logger.error("Error preparing query.\n" + sparql, e);
      } 
    } catch (RepositoryException e) {
      logger.error("Error connecting to repository.", e);
    } finally {
      try {
        if (con != null) {
          con.close();
        }
      } catch (RepositoryException e) {
        logger.error("Error closing connection.", e);
      }
    }    
  }
}
