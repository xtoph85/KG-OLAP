package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DDomainRangeSmallContext extends DemoDataset4DDomainRange {
  public DemoDataset4DDomainRangeSmallContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearPackageGranularity(4);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(4);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(4);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(4);
  }
}
