package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DNoLocalInferenceTinyContext extends DemoDataset3DNoLocalInference {
  public DemoDataset3DNoLocalInferenceTinyContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(2);
    this.setDescendantsAtRegionYearGranularity(2);
    this.setDescendantsAtSegmentMonthTypeGranularity(2);
    this.setDescendantsAtSegmentDayTypeGranularity(2);
    this.setDescendantsAtSegmentDayModelGranularity(2);
  }
}
