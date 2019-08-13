. ./set-variables.sh

java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDatasetBaseline3DAbstractedSmallFact   -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $tempRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysisBaselineAbstractionValueGenerating -i 15 
java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDatasetBaseline3DAbstractedMediumFact -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $tempRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysisBaselineAbstractionValueGenerating -i 15 
java -Xms20g -Xmx20g -Djava.io.tmpdir=tmpdir -Dat.jku.dke.kgolap.demo.benchmark.dir=benchmarks -Dat.jku.dke.kgolap.demo.benchmark.log=benchmarks/benchmark.log -Dat.jku.dke.kgolap.demo.benchmark.statistics.log=benchmarks/benchmark-statistics.log -cp bin/*:lib/* at.jku.dke.kgolap.demo.DemoRunner -s at.jku.dke.kgolap.demo.datasets.DemoDatasetBaseline3DAbstractedLargeFact   -fb at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ub $tempRepoURI -ft at.jku.dke.kgolap.repo.sesame.SesameHTTPRepoFactory -ut $tempRepoURI -a at.jku.dke.kgolap.demo.analyses.DemoAnalysisBaselineAbstractionValueGenerating -i 15  
