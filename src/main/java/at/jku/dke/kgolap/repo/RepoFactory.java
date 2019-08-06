package at.jku.dke.kgolap.repo;

public abstract class RepoFactory {
  public abstract Repo createRepo(RepoProperties properties);
}
