
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DNoLocalInferenceSmallContextTinyFact extends DemoDataset3DNoLocalInferenceSmallContext {
  public DemoDataset3DNoLocalInferenceSmallContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(6);
    this.setNoOfAirports(7);
    this.setNoOfRunways(14);
    this.setNoOfTaxiways(42);
    this.setNoOfVors(7);
  }
}
