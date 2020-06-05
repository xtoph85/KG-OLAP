
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DDomainRangeSmallContextMediumFact extends DemoDataset4DDomainRangeSmallContext {
  public DemoDataset4DDomainRangeSmallContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(40);
    this.setNoOfAirports(50);
    this.setNoOfRunways(100);
    this.setNoOfTaxiways(300);
    this.setNoOfVors(50);
  }
}
