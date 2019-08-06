package at.jku.dke.kgolap.demo.analyses;

import at.jku.dke.kgolap.KGOLAPCube;

public abstract class DemoAnalysis {
  private KGOLAPCube cube = null;
  
  public KGOLAPCube getCube() {
    return cube;
  }

  public void setCube(KGOLAPCube cube) {
    this.cube = cube;
  }

  public abstract void run();
}
