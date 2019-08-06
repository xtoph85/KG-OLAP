package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DAbstractedTinyContext extends DemoDataset4DAbstracted {
  public DemoDataset4DAbstractedTinyContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(2);
    this.setDescendantsAtRegionYearPackageGranularity(2);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(2);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(2);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(2);
  }
}
