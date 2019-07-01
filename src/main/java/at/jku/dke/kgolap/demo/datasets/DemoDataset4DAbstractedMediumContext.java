package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DAbstractedMediumContext extends DemoDataset4DAbstracted {
  public DemoDataset4DAbstractedMediumContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearPackageGranularity(4);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(5);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(5);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(5);
  }
}
