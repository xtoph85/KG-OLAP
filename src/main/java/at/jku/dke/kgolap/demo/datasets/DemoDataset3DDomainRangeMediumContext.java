package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DDomainRangeMediumContext extends DemoDataset3DDomainRange {
  public DemoDataset3DDomainRangeMediumContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearGranularity(4);
    this.setDescendantsAtSegmentMonthTypeGranularity(5);
    this.setDescendantsAtSegmentDayTypeGranularity(5);
    this.setDescendantsAtSegmentDayModelGranularity(5);
  }
}
