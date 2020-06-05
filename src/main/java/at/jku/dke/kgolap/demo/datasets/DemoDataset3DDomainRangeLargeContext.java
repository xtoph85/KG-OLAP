package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DDomainRangeLargeContext extends DemoDataset3DDomainRange {
  public DemoDataset3DDomainRangeLargeContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(5);
    this.setDescendantsAtRegionYearGranularity(5);
    this.setDescendantsAtSegmentMonthTypeGranularity(5);
    this.setDescendantsAtSegmentDayTypeGranularity(5);
    this.setDescendantsAtSegmentDayModelGranularity(5);
  }
}
