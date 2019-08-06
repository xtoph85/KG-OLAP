package at.jku.dke.kgolap.repo.sesame;

import java.io.File;
import java.io.IOException;

import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.dke.kgolap.repo.Repo;
import at.jku.dke.kgolap.repo.RepoFactory;
import at.jku.dke.kgolap.repo.RepoProperties;

public class SesameNativeStoreRepoFactory extends RepoFactory {
  private static final Logger logger = LoggerFactory.getLogger(SesameNativeStoreRepoFactory.class);

  @Override
  public Repo createRepo(RepoProperties properties) {
    SesameRepo repo = null;
    
    NativeStore sail = null;
    
    String dataDir = properties.getDataDir();
    
    if(dataDir != null) {
      File dataDirFile = new File(dataDir);
      
      if(!dataDirFile.exists()) {
        try {
          dataDirFile.createNewFile();
        } catch (IOException e) {
          logger.error("Error creating dataDir.", e);
        }
      }
      
      sail = new NativeStore(dataDirFile);
      
      repo = new SesameSailRepo(sail);
    }
    
    return repo;
  }

}
