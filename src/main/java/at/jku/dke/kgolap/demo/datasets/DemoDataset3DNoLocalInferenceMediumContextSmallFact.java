
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DNoLocalInferenceMediumContextSmallFact extends DemoDataset3DNoLocalInferenceMediumContext {
  public DemoDataset3DNoLocalInferenceMediumContextSmallFact() {
    super();
    
    this.setNoOfFillerIterations(12);
    this.setNoOfAirports(16);
    this.setNoOfRunways(32);
    this.setNoOfTaxiways(96);
    this.setNoOfVors(16);
  }
}
