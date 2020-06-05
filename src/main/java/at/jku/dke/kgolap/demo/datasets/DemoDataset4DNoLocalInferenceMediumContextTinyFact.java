
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DNoLocalInferenceMediumContextTinyFact extends DemoDataset4DNoLocalInferenceMediumContext {
  public DemoDataset4DNoLocalInferenceMediumContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(5);
    this.setNoOfAirports(6);
    this.setNoOfRunways(12);
    this.setNoOfTaxiways(36);
    this.setNoOfVors(6);
  }
}
