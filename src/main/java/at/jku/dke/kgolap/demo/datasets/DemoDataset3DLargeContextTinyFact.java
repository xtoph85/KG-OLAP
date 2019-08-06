
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DLargeContextTinyFact extends DemoDataset3DLargeContext {
  public DemoDataset3DLargeContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(4);
    this.setNoOfAirports(5);
    this.setNoOfRunways(10);
    this.setNoOfTaxiways(30);
    this.setNoOfVors(5);
  }
}
