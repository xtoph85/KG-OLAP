
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DLargeContextMediumFact extends DemoDataset3DLargeContext {
  public DemoDataset3DLargeContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(16);
    this.setNoOfAirports(20);
    this.setNoOfRunways(40);
    this.setNoOfTaxiways(120);
    this.setNoOfVors(20);
  }
}
