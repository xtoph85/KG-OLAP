
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DAbstractedLargeContextLargeFact extends DemoDataset3DAbstractedLargeContext {

  public DemoDataset3DAbstractedLargeContextLargeFact() {
    super();
    
    this.setNoOfFillerIterations(24);
    this.setNoOfAirports(30);
    this.setNoOfRunways(60);
    this.setNoOfTaxiways(180);
    this.setNoOfVors(30);
  }
}
