# Evaluate Benchmark Logs

This directory contains the R scripts that were used to evaluate the benchmark logs. The R scripts prepare the log files and perform some basic cleaning. In particular, in some cases, the first benchmark runs produced exceptional results which were probably down to a slowdown in the GraphDB instance after multiple runs. Subsequent results showed the expected behavior. We exclude the exceptional results from statistical evaluation but keep them in the log for transparency's sake.

- `datasets.r`

- `slicedice.r`
- `merge.r`
- `abstraction-triplegenerating.r`
- `abstraction-individualgenerating.r`
- `abstraction-valuegenerating.r`
- `reification.r`
- `pivot.r`

- `ruleevaluation.r`
