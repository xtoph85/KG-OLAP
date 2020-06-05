
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DNoLocalInferenceTinyContextTinyFact extends DemoDataset3DNoLocalInferenceTinyContext {
  public DemoDataset3DNoLocalInferenceTinyContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(7);
    this.setNoOfAirports(9);
    this.setNoOfRunways(18);
    this.setNoOfTaxiways(54);
    this.setNoOfVors(9);
  }
}
