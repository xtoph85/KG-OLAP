package at.jku.dke.kgolap;

import java.util.HashMap;
import java.util.Map;

import at.jku.dke.kgolap.repo.RepoProperties;

public class KGOLAPCubeProperties {
  private Map<String,String> prefixes = new HashMap<String,String>();
  private String rulesetTtl = null;
  private String baseRepoFactoryClass = null;
  private String tempRepoFactoryClass = null;
  private RepoProperties baseRepoProperties = null;
  private RepoProperties tempRepoProperties = null;
    
  public void addPrefix(String prefix, String uri) {
    this.prefixes.put(prefix, uri);
  }
  
  public Map<String,String> getPrefixes() {
    return this.prefixes;
  }
  
  public String getRulesetTtl() {
    return rulesetTtl;
  }

  public void setRulesetTtl(String rulesetTtl) {
    this.rulesetTtl = rulesetTtl;
  }

  public String getBaseRepoFactoryClass() {
    return baseRepoFactoryClass;
  }
  
  public void setBaseRepoFactoryClass(String baseRepoFactoryClass) {
    this.baseRepoFactoryClass = baseRepoFactoryClass;
  }

  public String getTempRepoFactoryClass() {
    return tempRepoFactoryClass;
  }
  
  public void setTempRepoFactoryClass(String tempRepoFactoryClass) {
    this.tempRepoFactoryClass = tempRepoFactoryClass;
  }

  public RepoProperties getBaseRepoProperties() {
    return baseRepoProperties;
  }

  public void setBaseRepoProperties(RepoProperties baseRepoProperties) {
    this.baseRepoProperties = baseRepoProperties;
  }

  public RepoProperties getTempRepoProperties() {
    return tempRepoProperties;
  }

  public void setTempRepoProperties(RepoProperties tempRepoProperties) {
    this.tempRepoProperties = tempRepoProperties;
  }
}
