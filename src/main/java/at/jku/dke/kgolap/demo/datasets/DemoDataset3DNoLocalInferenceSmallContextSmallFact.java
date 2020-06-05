
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DNoLocalInferenceSmallContextSmallFact extends DemoDataset3DNoLocalInferenceSmallContext {
  public DemoDataset3DNoLocalInferenceSmallContextSmallFact() {
    super();
    
    this.setNoOfFillerIterations(22);
    this.setNoOfAirports(27);
    this.setNoOfRunways(54);
    this.setNoOfTaxiways(162);
    this.setNoOfVors(27);
  }
}
