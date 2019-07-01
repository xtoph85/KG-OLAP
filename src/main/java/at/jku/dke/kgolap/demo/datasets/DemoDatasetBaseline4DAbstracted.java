package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDatasetBaseline4DAbstracted extends DemoDatasetBaseline {
  public DemoDatasetBaseline4DAbstracted() {
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
            dataset = new DemoDataset4DAbstractedLargeContextLargeFact();
            break;
          case MEDIUM:
            dataset = new DemoDataset4DAbstractedLargeContextMediumFact();
            break;
          case SMALL:
            dataset = new DemoDataset4DAbstractedLargeContextSmallFact();
            break;
          case TINY:
            dataset = new DemoDataset4DAbstractedTinyContextTinyFact();
            break;
        }
        break;
    }
    
    return dataset;
  }
}
