
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DDomainRangeLargeContextMediumFact extends DemoDataset4DDomainRangeLargeContext {
  public DemoDataset4DDomainRangeLargeContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(16);
    this.setNoOfAirports(20);
    this.setNoOfRunways(40);
    this.setNoOfTaxiways(120);
    this.setNoOfVors(20);
  }
}
