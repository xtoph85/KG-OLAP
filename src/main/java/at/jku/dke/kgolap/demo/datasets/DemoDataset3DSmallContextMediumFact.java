
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DSmallContextMediumFact extends DemoDataset3DSmallContext {
  public DemoDataset3DSmallContextMediumFact() {
    super();
    
    this.setNoOfFillerIterations(40);
    this.setNoOfAirports(50);
    this.setNoOfRunways(100);
    this.setNoOfTaxiways(300);
    this.setNoOfVors(50);
  }
}
