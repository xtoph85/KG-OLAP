
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DLargeContextSmallFact extends DemoDataset3DLargeContext {
  public DemoDataset3DLargeContextSmallFact() {
    super();
    
    this.setNoOfFillerIterations(8);
    this.setNoOfAirports(10);
    this.setNoOfRunways(20);
    this.setNoOfTaxiways(60);
    this.setNoOfVors(10);
  }
}
