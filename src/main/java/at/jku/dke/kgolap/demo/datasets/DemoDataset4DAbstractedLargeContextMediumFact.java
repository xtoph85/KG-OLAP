
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DAbstractedLargeContextMediumFact extends DemoDataset4DAbstractedLargeContext {
  public DemoDataset4DAbstractedLargeContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(16);
    this.setNoOfAirports(20);
    this.setNoOfRunways(40);
    this.setNoOfTaxiways(120);
    this.setNoOfVors(20);
  }
}
