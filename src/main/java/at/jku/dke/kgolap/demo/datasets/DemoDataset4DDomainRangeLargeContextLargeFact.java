
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DDomainRangeLargeContextLargeFact extends DemoDataset4DDomainRangeLargeContext {

  public DemoDataset4DDomainRangeLargeContextLargeFact() {
    super();
    
    this.setNoOfFillerIterations(24);
    this.setNoOfAirports(30);
    this.setNoOfRunways(60);
    this.setNoOfTaxiways(180);
    this.setNoOfVors(30);
  }
}
