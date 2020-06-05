package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DNoLocalInferenceMediumContext extends DemoDataset3DNoLocalInference {
  public DemoDataset3DNoLocalInferenceMediumContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearGranularity(4);
    this.setDescendantsAtSegmentMonthTypeGranularity(5);
    this.setDescendantsAtSegmentDayTypeGranularity(5);
    this.setDescendantsAtSegmentDayModelGranularity(5);
  }
}
