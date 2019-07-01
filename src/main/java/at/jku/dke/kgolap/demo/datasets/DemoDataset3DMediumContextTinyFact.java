
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DMediumContextTinyFact extends DemoDataset3DMediumContext {
  public DemoDataset3DMediumContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(5);
    this.setNoOfAirports(6);
    this.setNoOfRunways(12);
    this.setNoOfTaxiways(36);
    this.setNoOfVors(6);
  }
}
