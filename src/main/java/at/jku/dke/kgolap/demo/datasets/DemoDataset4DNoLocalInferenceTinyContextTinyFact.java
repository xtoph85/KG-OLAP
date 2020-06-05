
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DNoLocalInferenceTinyContextTinyFact extends DemoDataset4DNoLocalInferenceTinyContext {
  public DemoDataset4DNoLocalInferenceTinyContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(7);
    this.setNoOfAirports(9);
    this.setNoOfRunways(18);
    this.setNoOfTaxiways(54);
    this.setNoOfVors(9);
  }
}
