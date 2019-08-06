package at.jku.dke.kgolap.repo.sesame;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SesameSailRepo extends SesameRepo {
  private static final Logger logger = LoggerFactory.getLogger(SesameSailRepo.class);
  
  private Sail sail = null;
  
  public SesameSailRepo(Sail sail) {
    super(new SailRepository(sail));
    
    this.sail = sail;
  }
  
  @Override
  public void shutDown() {
    super.shutDown();
    
    try {
      sail.shutDown();
    } catch (SailException e) {
      logger.error("Error shutting down Sail.", e);
    }
  }
}
