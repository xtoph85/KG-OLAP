
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DLargeContextTinyFact extends DemoDataset4DLargeContext {
  public DemoDataset4DLargeContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(4);
    this.setNoOfAirports(5);
    this.setNoOfRunways(10);
    this.setNoOfTaxiways(30);
    this.setNoOfVors(5);
  }
}
