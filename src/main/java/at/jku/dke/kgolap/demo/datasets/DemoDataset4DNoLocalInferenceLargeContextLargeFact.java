
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DNoLocalInferenceLargeContextLargeFact extends DemoDataset4DNoLocalInferenceLargeContext {

  public DemoDataset4DNoLocalInferenceLargeContextLargeFact() {
    super();
    
    this.setNoOfFillerIterations(24);
    this.setNoOfAirports(30);
    this.setNoOfRunways(60);
    this.setNoOfTaxiways(180);
    this.setNoOfVors(30);
  }
}
