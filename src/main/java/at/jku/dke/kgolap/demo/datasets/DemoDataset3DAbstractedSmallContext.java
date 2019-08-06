package at.jku.dke.kgolap.demo.datasets;

public abstract class DemoDataset3DAbstractedSmallContext extends DemoDataset3DAbstracted {
  public DemoDataset3DAbstractedSmallContext() {
    super();
    
    this.setDescendantsAtRegionGranularity(4);
    this.setDescendantsAtRegionYearGranularity(4);
    this.setDescendantsAtSegmentMonthTypeGranularity(4);
    this.setDescendantsAtSegmentDayTypeGranularity(4);
    this.setDescendantsAtSegmentDayModelGranularity(4);
  }
}
