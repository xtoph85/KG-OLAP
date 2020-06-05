
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DNoLocalInferenceLargeContextMediumFact extends DemoDataset3DNoLocalInferenceLargeContext {
  public DemoDataset3DNoLocalInferenceLargeContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(16);
    this.setNoOfAirports(20);
    this.setNoOfRunways(40);
    this.setNoOfTaxiways(120);
    this.setNoOfVors(20);
  }
}
