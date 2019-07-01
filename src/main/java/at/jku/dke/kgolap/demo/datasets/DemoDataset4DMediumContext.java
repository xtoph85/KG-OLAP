package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DMediumContext extends DemoDataset4D {
  public DemoDataset4DMediumContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearPackageGranularity(4);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(5);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(5);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(5);
  }
}
