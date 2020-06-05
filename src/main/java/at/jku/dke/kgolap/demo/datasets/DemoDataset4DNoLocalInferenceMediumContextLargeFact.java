
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DNoLocalInferenceMediumContextLargeFact extends DemoDataset4DNoLocalInferenceMediumContext {

  public DemoDataset4DNoLocalInferenceMediumContextLargeFact() {
    super();
    
    this.setNoOfFillerIterations(35);
    this.setNoOfAirports(44);
    this.setNoOfRunways(88);
    this.setNoOfTaxiways(264);
    this.setNoOfVors(44);
  }
}
