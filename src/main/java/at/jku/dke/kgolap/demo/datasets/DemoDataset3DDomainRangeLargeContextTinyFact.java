
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DDomainRangeLargeContextTinyFact extends DemoDataset3DDomainRangeLargeContext {
  public DemoDataset3DDomainRangeLargeContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(4);
    this.setNoOfAirports(5);
    this.setNoOfRunways(10);
    this.setNoOfTaxiways(30);
    this.setNoOfVors(5);
  }
}
