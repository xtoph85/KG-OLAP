
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DNoLocalInferenceLargeContextTinyFact extends DemoDataset4DNoLocalInferenceLargeContext {
  public DemoDataset4DNoLocalInferenceLargeContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(4);
    this.setNoOfAirports(5);
    this.setNoOfRunways(10);
    this.setNoOfTaxiways(30);
    this.setNoOfVors(5);
  }
}
