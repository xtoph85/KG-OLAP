package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DAbstractedLargeContext extends DemoDataset4DAbstracted {
  public DemoDataset4DAbstractedLargeContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(5);
    this.setDescendantsAtRegionYearPackageGranularity(5);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(5);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(5);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(5);
  }
}
