package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DLargeContext extends DemoDataset3D {
  public DemoDataset3DLargeContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(5);
    this.setDescendantsAtRegionYearGranularity(5);
    this.setDescendantsAtSegmentMonthTypeGranularity(5);
    this.setDescendantsAtSegmentDayTypeGranularity(5);
    this.setDescendantsAtSegmentDayModelGranularity(5);
  }
}
