package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DSmallContext extends DemoDataset3D {
  public DemoDataset3DSmallContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearGranularity(4);
    this.setDescendantsAtSegmentMonthTypeGranularity(4);
    this.setDescendantsAtSegmentDayTypeGranularity(4);
    this.setDescendantsAtSegmentDayModelGranularity(4);
  }
}
