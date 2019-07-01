
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset4DAbstractedSmallContextMediumFact extends DemoDataset4DAbstractedSmallContext {
  public DemoDataset4DAbstractedSmallContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(40);
    this.setNoOfAirports(50);
    this.setNoOfRunways(100);
    this.setNoOfTaxiways(300);
    this.setNoOfVors(50);
  }
}
