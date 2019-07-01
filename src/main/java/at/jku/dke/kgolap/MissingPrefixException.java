package at.jku.dke.kgolap;

public class MissingPrefixException extends Exception {
  private static final long serialVersionUID = 5589593407136725358L;
  
  private String prefix = null;
  
  public MissingPrefixException(String prefix) {
    this.prefix = prefix;
  }
  
  public String toString() {
    return "Expected prefix: " + prefix;
  }
}
