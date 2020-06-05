
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DNoLocalInferenceMediumContextMediumFact extends DemoDataset3DNoLocalInferenceMediumContext {
  public DemoDataset3DNoLocalInferenceMediumContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(24);
    this.setNoOfAirports(30);
    this.setNoOfRunways(60);
    this.setNoOfTaxiways(180);
    this.setNoOfVors(30);
  }
}
