package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DDomainRangeSmallContext extends DemoDataset3DDomainRange {
  public DemoDataset3DDomainRangeSmallContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearGranularity(4);
    this.setDescendantsAtSegmentMonthTypeGranularity(4);
    this.setDescendantsAtSegmentDayTypeGranularity(4);
    this.setDescendantsAtSegmentDayModelGranularity(4);
  }
}
