package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDatasetBaseline3D extends DemoDatasetBaseline {
  public DemoDatasetBaseline3D() {
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
            dataset = new DemoDataset3DLargeContextLargeFact();
            break;
          case MEDIUM:
            dataset = new DemoDataset3DLargeContextMediumFact();
            break;
          case SMALL:
            dataset = new DemoDataset3DLargeContextSmallFact();
            break;
          case TINY:
            dataset = new DemoDataset3DTinyContextTinyFact();
            break;
        }
        break;
    }
    
    return dataset;
  }
}
