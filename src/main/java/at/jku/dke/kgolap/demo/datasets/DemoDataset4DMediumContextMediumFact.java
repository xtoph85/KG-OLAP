
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DMediumContextMediumFact extends DemoDataset4DMediumContext {
  public DemoDataset4DMediumContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(24);
    this.setNoOfAirports(30);
    this.setNoOfRunways(60);
    this.setNoOfTaxiways(180);
    this.setNoOfVors(30);
  }
}
