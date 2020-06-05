package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DNoLocalInferenceLargeContext extends DemoDataset3DNoLocalInference {
  public DemoDataset3DNoLocalInferenceLargeContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(5);
    this.setDescendantsAtRegionYearGranularity(5);
    this.setDescendantsAtSegmentMonthTypeGranularity(5);
    this.setDescendantsAtSegmentDayTypeGranularity(5);
    this.setDescendantsAtSegmentDayModelGranularity(5);
  }
}
