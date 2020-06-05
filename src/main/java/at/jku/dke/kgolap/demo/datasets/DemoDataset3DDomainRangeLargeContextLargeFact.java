
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DDomainRangeLargeContextLargeFact extends DemoDataset3DDomainRangeLargeContext {

  public DemoDataset3DDomainRangeLargeContextLargeFact() {
    super();
    
    this.setNoOfFillerIterations(24);
    this.setNoOfAirports(30);
    this.setNoOfRunways(60);
    this.setNoOfTaxiways(180);
    this.setNoOfVors(30);
  }
}
