
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DAbstractedSmallContextMediumFact extends DemoDataset3DAbstractedSmallContext {
  public DemoDataset3DAbstractedSmallContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(40);
    this.setNoOfAirports(50);
    this.setNoOfRunways(100);
    this.setNoOfTaxiways(300);
    this.setNoOfVors(50);
  }
}
