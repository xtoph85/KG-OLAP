
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DMediumContextSmallFact extends DemoDataset4DMediumContext {
  public DemoDataset4DMediumContextSmallFact() {
    super();
    
    this.setNoOfFillerIterations(12);
    this.setNoOfAirports(16);
    this.setNoOfRunways(32);
    this.setNoOfTaxiways(96);
    this.setNoOfVors(16);
  }
}
