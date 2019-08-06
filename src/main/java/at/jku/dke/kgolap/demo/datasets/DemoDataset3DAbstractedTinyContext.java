package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DAbstractedTinyContext extends DemoDataset3DAbstracted {
  public DemoDataset3DAbstractedTinyContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(2);
    this.setDescendantsAtRegionYearGranularity(2);
    this.setDescendantsAtSegmentMonthTypeGranularity(2);
    this.setDescendantsAtSegmentDayTypeGranularity(2);
    this.setDescendantsAtSegmentDayModelGranularity(2);
  }
}
