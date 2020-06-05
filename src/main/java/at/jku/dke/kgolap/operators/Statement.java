package at.jku.dke.kgolap.operators;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.management.OperatingSystemMXBean;

import at.jku.dke.kgolap.repo.Repo;

@SuppressWarnings("restriction")
public abstract class Statement {
  private static final Logger logger = LoggerFactory.getLogger(Statement.class);
  private static final Logger benchmarking = LoggerFactory.getLogger("benchmarking");
  
  private boolean inBenchmarkingMode = false;
  
  private Repo sourceRepository = null;
  private Repo targetRepository = null;
  
  private String prefixes = null;

  public String getPrefixes() {
    return prefixes;
  }

  public void setPrefixes(String prefixes) {
    this.prefixes = prefixes;
  }
  
  public Repo getSourceRepository() {
    return sourceRepository;
  }

  public void setSourceRepository(Repo sourceRepository) {
    this.sourceRepository = sourceRepository;
  }

  public Repo getTargetRepository() {
    return targetRepository;
  }

  public void setTargetRepository(Repo targetRepository) {
    this.targetRepository = targetRepository;
  }
  
  public boolean isInBenchmarkingMode() {
    return inBenchmarkingMode;
  }
  
  public void setBenchmarkingMode(boolean benchmarkingMode) {
    this.inBenchmarkingMode = benchmarkingMode;
  }
  
  public abstract String prepareStatement();
  
  public String prepareUpdateStatement() {
    String sparql = this.prepareStatement();
    
    String query = sparql.substring(sparql.indexOf("SELECT"));
    
    StringBuilder updateQuery = new StringBuilder();
    
    updateQuery.append(
        this.getPrefixes()
      + "DELETE {\n"
      + "  GRAPH ?g_d {\n"
      + "    ?s_d ?p_d ?o_d .\n"
      + "  }\n"
      + "}\n"
      + "INSERT {\n"
      + "  GRAPH ?g_i {\n"
      + "    ?s_i ?p_i ?o_i .\n"
      + "  }\n"
      + "} WHERE {\n"
      + "  {\n"
             // Prepend four blank spaces to every line
      +      Arrays.asList(query.split("\n")).stream().map(line -> "    " + line + "\n").reduce("", String::concat)
      + "  }\n"
      + "  {\n"
      + "    BIND(?g AS ?g_d)\n"
      + "    BIND(?s AS ?s_d)\n"
      + "    BIND(?p AS ?p_d)\n"
      + "    BIND(?o AS ?o_d)\n"
      + "    FILTER(?op = \"-\")\n"
      + "  } UNION {\n"
      + "    BIND(?g AS ?g_i)\n"
      + "    BIND(?s AS ?s_i)\n"
      + "    BIND(?p AS ?p_i)\n"
      + "    BIND(?o AS ?o_i)\n"
      + "    FILTER(?op = \"+\")\n"
      + "  }\n"
      + "}\n"
    );
    
    return updateQuery.toString();
  }
  
  public String prepareBenchmarkingStatement() {
    String sparql = this.prepareStatement();
    
    String query = sparql.substring(sparql.indexOf("SELECT"));
    
    StringBuilder benchmarkingQuery = new StringBuilder();
    
    benchmarkingQuery.append(
        this.getPrefixes()
      + "SELECT (COUNT(*) AS ?cnt) WHERE {\n"
      + "  {\n"
             // Prepend four blank spaces to every line
      +      Arrays.asList(query.split("\n")).stream().map(line -> "    " + line + "\n").reduce("", String::concat)
      + "  }\n"
      + "}\n"
    );
    
    return benchmarkingQuery.toString();
  }
  
  public String prepareExplainStatement() {
    String sparql = this.prepareStatement();
    return sparql.replaceFirst("\\?op", "?op\nFROM onto:explain\n");
  }
  
  public String prepareExplainBenchmarkingStatement() {
    String sparql = this.prepareBenchmarkingStatement();
    return sparql.replaceFirst("WHERE \\{", "\nFROM onto:explain\nWHERE {\n");
  }
  
  public void executeUpdate() {
    OperatingSystemMXBean osMxBean = null;
    
    logger.info("Prepare update statement.");
    String sparql = this.prepareUpdateStatement();

    logger.info("Executing update...");
    logger.info("Using SPARQL statement for update:\n" + sparql);

    if(isInBenchmarkingMode()) {
        osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      
        benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + this.getClass().getSimpleName() + "-ExecuteUpdate,"
            + "begin"
        );
    }
    
    this.getTargetRepository().executeUpdate(sparql);

    if(isInBenchmarkingMode()) {
        benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + this.getClass().getSimpleName() + "-ExecuteUpdate,"
            + "end"
        );  
    }
    
    if(isInBenchmarkingMode()) {
      String benchmarkId = System.getProperty("at.jku.dke.kgolap.demo.benchmark.id");
      String benchmarkDirectory = System.getProperty("at.jku.dke.kgolap.demo.benchmark.dir");
      
      File filePersistQuery = new File(
          benchmarkDirectory + File.separator + benchmarkId + ".update"
      );
      
      if(filePersistQuery.exists()) {
          filePersistQuery.delete();
      }
        
      try {
        filePersistQuery.createNewFile();
      } catch (IOException e) {
          benchmarking.error("Could not create file to store the query in benchmark directory.");
      }
      
      try(PrintWriter out = new PrintWriter(filePersistQuery)) {
          out.write(sparql);
      } catch (FileNotFoundException e) {
          benchmarking.error("Error storing query in benchmark directory.");
      }
    }
  }
  
  public void executeInMemory() {
    OperatingSystemMXBean osMxBean = null;
    
    String sparql = prepareStatement();
    
    if(isInBenchmarkingMode()) {
      logger.info("Getting query plans and running benchmarking query ...");
      
      osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
      
      String benchmarkId = System.getProperty("at.jku.dke.kgolap.demo.benchmark.id");
      String benchmarkDirectory = System.getProperty("at.jku.dke.kgolap.demo.benchmark.dir");
      String iteration = System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration");

      String explainBenchmarkingSparql = this.prepareExplainBenchmarkingStatement();
        
      File filePersistBenchmarkingQueryPlan = new File(
        benchmarkDirectory + File.separator + benchmarkId + "." + iteration + ".queryplan.benchmarking"
      );
        
      if(!filePersistBenchmarkingQueryPlan.exists()) {
        try {
          filePersistBenchmarkingQueryPlan.createNewFile();
        } catch (IOException e) {
            benchmarking.error("Could not create file to store the benchmarking query plan in benchmark directory.");
        }
        
        try(OutputStream out = new FileOutputStream(filePersistBenchmarkingQueryPlan)) {
          this.getSourceRepository().executeTupleQuery(explainBenchmarkingSparql, out);
        } catch (FileNotFoundException e) {
          benchmarking.error("Error storing benchmarking query plan in benchmark directory.");
        } catch (IOException e) {
          benchmarking.error("Error writing query plan.");
        }
      }
      
      // execute benchmarking query
      String benchmarkingSparql = prepareBenchmarkingStatement();

      File filePersistBenchmarkingResult = new File(
        benchmarkDirectory + File.separator + benchmarkId + "." + iteration + ".delta.count"
      );
        
      if(!filePersistBenchmarkingResult.exists()) {
        try {
          filePersistBenchmarkingResult.createNewFile();
        } catch (IOException e) {
            benchmarking.error("Could not create file to store the benchmarking query result in benchmark directory.");
        }
        
        try(OutputStream out = new FileOutputStream(filePersistBenchmarkingResult)) {
          benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + this.getClass().getSimpleName() + "-ExecuteBenchmarkingQuery,"
            + "begin"
          );
          
          this.getSourceRepository().executeTupleQuery(benchmarkingSparql, out);
          
          benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + this.getClass().getSimpleName() + "-ExecuteBenchmarkingQuery,"
            + "end"
          );
        } catch (FileNotFoundException e) {
          benchmarking.error("Error storing benchmarking query result in benchmark directory.");
        } catch (IOException e) {
          benchmarking.error("Error writing query plan.");
        }
      }
      
      // store query plan of delta query      
      File filePersistQueryPlan = new File(
        benchmarkDirectory + File.separator + benchmarkId + "." + iteration + ".queryplan"
      );
        
      if(!filePersistQueryPlan.exists()) {
        try {
          filePersistQueryPlan.createNewFile();
        } catch (IOException e) {
          benchmarking.error("Could not create file to store the query plan in benchmark directory.");
        }
          
        try(OutputStream out = new FileOutputStream(filePersistQueryPlan)) {
          this.getSourceRepository().executeTupleQuery(this.prepareExplainStatement(), out);
        } catch (FileNotFoundException e) {
          benchmarking.error("Error storing query plan in benchmark directory.");
        } catch (IOException e) {
          benchmarking.error("Error writing query plan.");
        }
      }

      File filePersistQuery = new File(
        benchmarkDirectory + File.separator + benchmarkId + ".query"
      );
        
      if(!filePersistQuery.exists()) {
        try {
          filePersistQuery.createNewFile();
        } catch (IOException e) {
          benchmarking.error("Could not create file to store the query in benchmark directory.");
        }
          
        try(PrintWriter out = new PrintWriter(filePersistQuery)) {
          out.write(sparql);
        } catch (FileNotFoundException e) {
          benchmarking.error("Error storing query in benchmark directory.");
        }
      }

      logger.info("Done.");
    }
    
    logger.info("Executing in memory ...");
    logger.info("Using SPARQL statement for delta query:\n" + sparql);
    
    // benchmarking measurements are taken by the executeDeltaQuery function    
    this.getSourceRepository().executeDeltaQuery(sparql, this.getTargetRepository());
    
    logger.info("Executed.");
  }
  
  public void execute() {
    OperatingSystemMXBean osMxBean = null;
    
    logger.info("Prepare statement.");    
    String sparql = prepareStatement();
    String benchmarkingSparql = prepareBenchmarkingStatement();
    
    String fileTmpName = System.getProperty("java.io.tmpdir") + UUID.randomUUID() + ".query.tmp";

    logger.info("Query output temporarily stored in file: " + fileTmpName);
    File fileTmp = new File(fileTmpName);
    
    if(fileTmp.exists()) {
      logger.info("Delete existing temp file for query output.");
      fileTmp.delete();
    }
    
    try {
      fileTmp.createNewFile();
    } catch (IOException e) {
      logger.error("Could not create temporary file for query output.", e);
    }
    
    
    try(OutputStream out = new FileOutputStream(fileTmp)) {
      logger.info("Executing ...");
      logger.info("Using SPARQL statement for tuple query:\n" + sparql);

      if(isInBenchmarkingMode()) {
        osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        
        benchmarking.info(
            osMxBean.getProcessCpuTime() + ","
          + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
          + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
          + this.getClass().getSimpleName() + "-ExecuteQuery,"
          + "begin"
        );
      }
      
      getSourceRepository().executeTupleQuery(sparql, out);

      if(isInBenchmarkingMode()) {
          benchmarking.info(
                osMxBean.getProcessCpuTime() + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
              + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
              + this.getClass().getSimpleName() + "-ExecuteQuery,"
              + "end"
          );  
      }
      
      if(isInBenchmarkingMode()) {
        sparql = this.prepareBenchmarkingStatement();
        
        benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + this.getClass().getSimpleName() + "-ExecuteBenchmarkingQuery,"
            + "begin"
        );
        
        getSourceRepository().executeTupleQuery(benchmarkingSparql);
        
        benchmarking.info(
            osMxBean.getProcessCpuTime() + ","
          + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
          + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
          + this.getClass().getSimpleName() + "-ExecuteBenchmarkingQuery,"
          + "end"
        );
      }
    } catch (FileNotFoundException e) {
      logger.error("Could not find temporary file for query output.", e);
    } catch (IOException e) {
      logger.error("Could not access temporary file for query output.", e);
    }
    
    try(InputStream in = new FileInputStream(fileTmp)) {
      logger.info("Load the query result into the target repository.");
      
      if(isInBenchmarkingMode()) {
        benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + this.getClass().getSimpleName() + "-ProcessDeltaFile,"
            + "begin"
        );  
      }
        
      getTargetRepository().processDelta(in);
      
      if(isInBenchmarkingMode()) {
        benchmarking.info(
              osMxBean.getProcessCpuTime() + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.id") + ","
            + System.getProperty("at.jku.dke.kgolap.demo.benchmark.iteration") + ","
            + this.getClass().getSimpleName() + "-ProcessDeltaFile,"
            + "end"
        );  
      }
    } catch (FileNotFoundException e) {
      logger.error("Could not find temporary file with query output.", e);
      e.printStackTrace();
    } catch (IOException e) {
      logger.error("Could not read temporary file with query output.", e);
    }

    if(isInBenchmarkingMode()) {
      String benchmarkId = System.getProperty("at.jku.dke.kgolap.demo.benchmark.id");
      String benchmarkDirectory = System.getProperty("at.jku.dke.kgolap.demo.benchmark.dir");
        
      File filePersistQuery = new File(
        benchmarkDirectory + File.separator + benchmarkId + ".query"
      );
        
      if(!filePersistQuery.exists()) {
        try {
          filePersistQuery.createNewFile();
        } catch (IOException e) {
          benchmarking.error("Could not create file to store the query in benchmark directory.");
        }
          
        try(PrintWriter out = new PrintWriter(filePersistQuery)) {
          out.write(sparql);
        } catch (FileNotFoundException e) {
          benchmarking.error("Error storing query in benchmark directory.");
        }
      }
      
      File filePersistQueryPlan = new File(
        benchmarkDirectory + File.separator + benchmarkId + ".queryplan"
      );
        
      if(!filePersistQueryPlan.exists()) {
        try {
          filePersistQueryPlan.createNewFile();
        } catch (IOException e) {
          benchmarking.error("Could not create file to store the query plan in benchmark directory.");
        }
          
        try(OutputStream out = new FileOutputStream(filePersistQueryPlan)) {
          this.getSourceRepository().executeTupleQuery(this.prepareExplainStatement(), out);
        } catch (FileNotFoundException e) {
          benchmarking.error("Error storing query plan in benchmark directory.");
        } catch (IOException e) {
          benchmarking.error("Error writing query plan.");
        }
      }
        
      File filePersistBenchmarkingQueryPlan = new File(
        benchmarkDirectory + File.separator + benchmarkId + ".benchmarking.queryplan"
      );
        
      if(!filePersistBenchmarkingQueryPlan.exists()) {
        try {
          filePersistBenchmarkingQueryPlan.createNewFile();
        } catch (IOException e) {
            benchmarking.error("Could not create file to store the benchmarking query plan in benchmark directory.");
        }
        
        try(OutputStream out = new FileOutputStream(filePersistBenchmarkingQueryPlan)) {
          this.getSourceRepository().executeTupleQuery(this.prepareExplainBenchmarkingStatement(), out);
        } catch (FileNotFoundException e) {
          benchmarking.error("Error storing benchmarking query plan in benchmark directory.");
        } catch (IOException e) {
          benchmarking.error("Error writing query plan.");
        }
      }
        
      try {
        File filePersistResult = new File(
          benchmarkDirectory + File.separator + benchmarkId + ".delta"
        );
            
        if(!filePersistResult.exists()) {
          Files.copy(fileTmp.toPath(), filePersistResult.toPath(), REPLACE_EXISTING);
        }
      } catch (IOException e) {
        benchmarking.error("Error copying query output to benchmark directory.");
      }
    }
    
    if(fileTmp.exists()) {
      fileTmp.delete();
    }
    
    logger.info("Executed.");
  }
}
