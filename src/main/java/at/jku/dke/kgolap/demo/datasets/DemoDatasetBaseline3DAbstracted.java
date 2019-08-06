package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDatasetBaseline3DAbstracted extends DemoDatasetBaseline {
  public DemoDatasetBaseline3DAbstracted() {
    super();
  }
  
  @Override
  public DemoDataset getBaseDataset() {
    DemoDataset dataset = null;
    
    switch(this.getDimensionalSize()) {
      case MEDIUM:
      default:
        switch(this.getFactSize()) {
          case HUGE:
          case LARGE:
            dataset = new DemoDataset3DAbstractedLargeContextLargeFact();
            break;
          case MEDIUM:
            dataset = new DemoDataset3DAbstractedLargeContextMediumFact();
            break;
          case SMALL:
            dataset = new DemoDataset3DAbstractedLargeContextSmallFact();
            break;
          case TINY:
            dataset = new DemoDataset3DAbstractedTinyContextTinyFact();
            break;
        }
        break;
    }
    
    return dataset;
  }
}
