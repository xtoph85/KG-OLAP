package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset4DNoLocalInferenceTinyContext extends DemoDataset4DNoLocalInference {
  public DemoDataset4DNoLocalInferenceTinyContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(2);
    this.setDescendantsAtRegionYearPackageGranularity(2);
    this.setDescendantsAtSegmentMonthTypePackageGranularity(2);
    this.setDescendantsAtSegmentDayTypeImportanceGranularity(2);
    this.setDescendantsAtSegmentDayModelImportanceGranularity(2);
  }
}
