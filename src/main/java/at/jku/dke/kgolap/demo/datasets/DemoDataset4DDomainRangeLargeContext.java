package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DDomainRangeLargeContext extends DemoDataset4DDomainRange {
  public DemoDataset4DDomainRangeLargeContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(5);
    this.setDescendantsAtRegionYearPackageGranularity(5);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(5);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(5);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(5);
  }
}
