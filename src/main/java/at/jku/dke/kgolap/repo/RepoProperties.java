package at.jku.dke.kgolap.repo;

public class RepoProperties {
  private String dataDir = null;
  private String repositoryURL = null;

  public String getRepositoryURL() {
    return repositoryURL;
  }

  public void setRepositoryURL(String repositoryURL) {
    this.repositoryURL = repositoryURL;
  }

  public String getDataDir() {
    return dataDir;
  }

  public void setDataDir(String dataDir) {
    this.dataDir = dataDir;
  }
  
}
