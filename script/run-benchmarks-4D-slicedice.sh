. ./set-variables.sh

java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DSmallContextSmallFact  -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 
java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DSmallContextMediumFact -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 
java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DSmallContextLargeFact  -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 

java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DMediumContextSmallFact  -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 
java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DMediumContextMediumFact -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 
java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DMediumContextLargeFact  -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 

java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DLargeContextSmallFact  -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 
java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DLargeContextMediumFact -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 
java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDataset4DLargeContextLargeFact  -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $baseRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DSliceDice -i 15 