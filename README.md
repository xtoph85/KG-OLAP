# Knowledge Graph OLAP (KG-OLAP)

The KG-OLAP proof-of-concept prototype comes as a Maven project. In order to build the project run the *package* goal, which will create zip and tar.gz archives that include all the binaries and third-party libraries.

In the bin folder we also provide pre-compiled binaries, which contain shell scripts for running the performance experiments. In order to run the shell scripts, you will need a GraphDB instance running on localhost on port 7200. You can change hostname and port number in the scripts accordingly.

In the benchmarks folder, we also provide logs from our performance experiments for benchmarking purposes. The benchmark.log file contains measurements of run times, the benchmark-statistics.log contains some numbers characterizing the datasets and delta tables of the individual test runs. Note that the logs are not cleaned. In particular, we re-ran some of the tests due to system failures and in order to check extraordinary performance results.
