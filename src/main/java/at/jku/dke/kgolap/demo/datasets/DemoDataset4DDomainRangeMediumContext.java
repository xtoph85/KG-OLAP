package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DDomainRangeMediumContext extends DemoDataset4DDomainRange {
  public DemoDataset4DDomainRangeMediumContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearPackageGranularity(4);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(5);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(5);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(5);
  }
}
