
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DNoLocalInferenceSmallContextMediumFact extends DemoDataset4DNoLocalInferenceSmallContext {
  public DemoDataset4DNoLocalInferenceSmallContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(40);
    this.setNoOfAirports(50);
    this.setNoOfRunways(100);
    this.setNoOfTaxiways(300);
    this.setNoOfVors(50);
  }
}
