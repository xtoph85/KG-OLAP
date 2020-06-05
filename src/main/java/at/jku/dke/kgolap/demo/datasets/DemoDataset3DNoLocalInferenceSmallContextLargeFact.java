
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DNoLocalInferenceSmallContextLargeFact extends DemoDataset3DNoLocalInferenceSmallContext {

  public DemoDataset3DNoLocalInferenceSmallContextLargeFact() {
    super();
    
    this.setNoOfFillerIterations(56);
    this.setNoOfAirports(70);
    this.setNoOfRunways(140);
    this.setNoOfTaxiways(420);
    this.setNoOfVors(70);
  }
}
