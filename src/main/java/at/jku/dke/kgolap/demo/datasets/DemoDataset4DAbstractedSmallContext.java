package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DAbstractedSmallContext extends DemoDataset4DAbstracted {
  public DemoDataset4DAbstractedSmallContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearPackageGranularity(4);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(4);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(4);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(4);
  }
}
