package at.jku.dke.kgolap.repo.sesame;

import org.openrdf.repository.http.HTTPRepository;

import at.jku.dke.kgolap.repo.HTTPConnectable;

public class SesameHTTPRepo extends SesameRepo implements HTTPConnectable {
  private String repositoryURL = null;
  
  public SesameHTTPRepo(String repositoryURL) {
    super(new HTTPRepository(repositoryURL));
    this.repositoryURL = repositoryURL;
  }

  @Override
  public String getRepositoryURL() {
    return this.repositoryURL;
  }
}
