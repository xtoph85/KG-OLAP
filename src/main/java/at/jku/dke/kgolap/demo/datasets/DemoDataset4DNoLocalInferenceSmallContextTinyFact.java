
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DNoLocalInferenceSmallContextTinyFact extends DemoDataset4DNoLocalInferenceSmallContext {
  public DemoDataset4DNoLocalInferenceSmallContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(6);
    this.setNoOfAirports(7);
    this.setNoOfRunways(14);
    this.setNoOfTaxiways(42);
    this.setNoOfVors(7);
  }
}
