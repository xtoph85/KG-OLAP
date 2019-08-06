
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DMediumContextTinyFact extends DemoDataset4DMediumContext {
  public DemoDataset4DMediumContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(5);
    this.setNoOfAirports(6);
    this.setNoOfRunways(12);
    this.setNoOfTaxiways(36);
    this.setNoOfVors(6);
  }
}
