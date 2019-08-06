
package at.jku.dke.kgolap.demo.datasets;

public class DemoDataset3DAbstractedSmallContextTinyFact extends DemoDataset3DAbstractedSmallContext {
  public DemoDataset3DAbstractedSmallContextTinyFact() {
    super();
    
    this.setNoOfFillerIterations(6);
    this.setNoOfAirports(7);
    this.setNoOfRunways(14);
    this.setNoOfTaxiways(42);
    this.setNoOfVors(7);
  }
}