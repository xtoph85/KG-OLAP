
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DNoLocalInferenceLargeContextSmallFact extends DemoDataset3DNoLocalInferenceLargeContext {
  public DemoDataset3DNoLocalInferenceLargeContextSmallFact() {
    super();
    
    this.setNoOfFillerIterations(8);
    this.setNoOfAirports(10);
    this.setNoOfRunways(20);
    this.setNoOfTaxiways(60);
    this.setNoOfVors(10);
  }
}
