
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DAbstractedLargeContextMediumFact extends DemoDataset3DAbstractedLargeContext {
  public DemoDataset3DAbstractedLargeContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(16);
    this.setNoOfAirports(20);
    this.setNoOfRunways(40);
    this.setNoOfTaxiways(120);
    this.setNoOfVors(20);
  }
}
