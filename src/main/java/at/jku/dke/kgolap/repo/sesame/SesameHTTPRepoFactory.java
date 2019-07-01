package at.jku.dke.kgolap.repo.sesame;

import at.jku.dke.kgolap.repo.Repo;
import at.jku.dke.kgolap.repo.RepoFactory;
import at.jku.dke.kgolap.repo.RepoProperties;

public class SesameHTTPRepoFactory extends RepoFactory {
  @Override
  public Repo createRepo(RepoProperties properties) {
      
    String repositoryURL = properties.getRepositoryURL();
    
    Repo repo = new SesameHTTPRepo(repositoryURL);
    
    return repo;
  }

}
