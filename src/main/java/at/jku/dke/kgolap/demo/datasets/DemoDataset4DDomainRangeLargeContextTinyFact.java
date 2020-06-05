
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DDomainRangeLargeContextTinyFact extends DemoDataset4DDomainRangeLargeContext {
  public DemoDataset4DDomainRangeLargeContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(4);
    this.setNoOfAirports(5);
    this.setNoOfRunways(10);
    this.setNoOfTaxiways(30);
    this.setNoOfVors(5);
  }
}
