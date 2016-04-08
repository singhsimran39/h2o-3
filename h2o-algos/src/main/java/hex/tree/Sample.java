package hex.tree;

import water.MRTask;
import water.fvec.Chunk;
import water.util.RandomUtils;

import java.util.Random;

// Deterministic sampling
public class Sample extends MRTask<Sample> {
  final DTree _tree;
  final float _rate;
  final float[] _rate_per_class;

  public Sample(DTree tree, float rate, float[] rate_per_class) {
    _tree = tree;
    _rate = rate;
    _rate_per_class = rate_per_class;
  }

  @Override
  public void map(Chunk nids, Chunk ys) {
    Random rand = RandomUtils.getRNG(_tree._seed);
    for (int row = 0; row < nids._len; row++) {
      boolean skip = ys.isNA(row);
      if (!skip) {
        float rate = _rate_per_class==null ? _rate : _rate_per_class[(int)ys.at8(row)];
        rand.setSeed(_tree._seed + row + nids.start()); //seeding is independent of chunking
        skip = rand.nextFloat() >= rate;
      }
      if (skip) nids.set(row, ScoreBuildHistogram.OUT_OF_BAG);     // Flag row as being ignored by sampling
    }
  }
}
