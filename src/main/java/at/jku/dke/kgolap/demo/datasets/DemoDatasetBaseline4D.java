package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDatasetBaseline4D extends DemoDatasetBaseline {
  public DemoDatasetBaseline4D() {
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
            dataset = new DemoDataset4DLargeContextLargeFact();
            break;
          case MEDIUM:
            dataset = new DemoDataset4DLargeContextMediumFact();
            break;
          case SMALL:
            dataset = new DemoDataset4DLargeContextSmallFact();
            break;
          case TINY:
            dataset = new DemoDataset4DTinyContextTinyFact();
            break;
        }
        break;
    }
    
    return dataset;
  }
}
