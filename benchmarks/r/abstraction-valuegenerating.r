library(lubridate)
library(plyr)
library(dplyr)
library(Hmisc)
library(varhandle)

benchmarkLog <- file.choose()
benchmarkStatisticsLog <- file.choose()

benchmark <- read.csv(benchmarkLog, header = FALSE, sep=",")
benchmarkStatistics <- read.csv(benchmarkStatisticsLog, header = FALSE, sep=",")

datasets <- benchmarkStatistics[benchmarkStatistics$V4 == 'Dataset-ClassName', ]
datasets <- datasets %>% select(2,5)
datasets <- datasets %>% rename(benchmarkId = V2)
datasets <- datasets %>% rename(dataset = V5)
datasets <- datasets %>% mutate(dimensions = substring(dataset, 44, 44))
datasets <- datasets %>% mutate(dimensions = gsub("B", "2", dimensions))
datasets$dimensions <- as.numeric(datasets$dimensions)

analyses <- benchmarkStatistics[benchmarkStatistics$V4 == 'Analysis-ClassName', ]
analyses <- analyses %>% select(2,5)
analyses <- analyses %>% rename(benchmarkId = V2)
analyses <- analyses %>% rename(analysis = V5)

statements <- benchmarkStatistics[benchmarkStatistics$V4 == 'ModelSize-StatementsAssertedInferred', ]
statements <- statements %>% select(2,5)
statements <- statements %>% rename(benchmarkId = V2)
statements <- statements %>% rename(statementsAssertedInferred = V5)
statements$statementsAssertedInferred <- unfactor(statements$statementsAssertedInferred)

statementsInserted <- benchmarkStatistics[benchmarkStatistics$V4 == 'DeltaSize-StatementsInserted', ]
statementsInserted <- statementsInserted %>% select(2,3,5)
statementsInserted <- statementsInserted %>% rename(benchmarkId = V2)
statementsInserted <- statementsInserted %>% rename(iteration = V3)
statementsInserted <- statementsInserted %>% rename(statementsInserted = V5)
statementsInserted$statementsInserted <- unfactor(statementsInserted$statementsInserted)

statementsDeleted <- benchmarkStatistics[benchmarkStatistics$V4 == 'DeltaSize-StatementsDeleted', ]
statementsDeleted <- statementsDeleted %>% select(2,3,5)
statementsDeleted <- statementsDeleted %>% rename(benchmarkId = V2)
statementsDeleted <- statementsDeleted %>% rename(iteration = V3)
statementsDeleted <- statementsDeleted %>% rename(statementsDeleted = V5)
statementsDeleted$statementsDeleted <- unfactor(statementsDeleted$statementsDeleted)

contexts <- benchmarkStatistics[benchmarkStatistics$V4 == 'ModelSize-Contexts', ]
contexts <- contexts %>% select(2,5)
contexts <- contexts %>% rename(benchmarkId = V2)
contexts <- contexts %>% rename(contexts = V5)
contexts$contexts <- unfactor(contexts$contexts)

begin <- benchmark[benchmark$V6 == 'begin', ]
end <- benchmark[benchmark$V6 == 'end', ]

begin <- begin %>% rename(timestampBegin = V1)
begin <- begin %>% rename(cpuTimeBegin = V2)
begin <- begin %>% rename(benchmarkId = V3)
begin <- begin %>% rename(iteration = V4)
begin <- begin %>% rename(case = V5)
begin <- begin %>% select(timestampBegin, cpuTimeBegin, benchmarkId, iteration, case)

end <- end %>% rename(timestampEnd = V1)
end <- end %>% rename(cpuTimeEnd = V2)
end <- end %>% rename(benchmarkId = V3)
end <- end %>% rename(iteration = V4)
end <- end %>% rename(case = V5)
end <- end %>% select(timestampEnd, cpuTimeEnd, benchmarkId, iteration, case)

cases <- join(begin, end)
cases <- join(cases, datasets)
cases <- join(cases, analyses)
cases <- join(cases, statements)
cases <- join(cases, contexts)
cases <- join(cases, statementsInserted)
cases <- join(cases, statementsDeleted)

cases <- cases %>% mutate(wallTimeDuration = as.duration(ymd_hms(cases$timestampEnd) - ymd_hms(cases$timestampBegin)))
cases <- cases %>% mutate(cpuTimeDelta = cases$cpuTimeEnd - cases$cpuTimeBegin)
cases <- cases %>% mutate(statementsInsertedDeleted = cases$statementsInserted + cases$statementsDeleted)

cases$timestampBegin <- unfactor(cases$timestampBegin)

filtered <- cases[cases$case == 'SesameHTTPRepo-ExecuteDeltaQuery-Evaluate' & (cases$analysis == 'at.jku.dke.kgolap.demo.analyses.DemoAnalysisBaselineAbstractionValueGenerating' | cases$analysis == 'at.jku.dke.kgolap.demo.analyses.DemoAnalysis3DAbstractionValueGenerating' | cases$analysis == 'at.jku.dke.kgolap.demo.analyses.DemoAnalysis4DAbstractionValueGenerating') & 
    (cases$dataset != 'at.jku.dke.kgolap.demo.datasets.DemoDataset4DAbstractedSmallContextLargeFact' | cases$timestampBegin > as.Date('2019-07-25')) & 
    (cases$dataset != 'at.jku.dke.kgolap.demo.datasets.DemoDataset4DAbstractedMediumContextMediumFact' | cases$timestampBegin > as.Date('2019-07-25')), ]

grouped <- filtered %>% group_by(case, dataset, analysis, dimensions, statementsAssertedInferred, contexts, statementsInsertedDeleted)

summarized <- summarise(grouped,meanWallTimeDuration=mean(wallTimeDuration), medianWallTimeDuration=median(wallTimeDuration), sdWallTimeDuration=sd(wallTimeDuration), meanCpuTimeDelta=mean(cpuTimeDelta), medianCpuTimeDelta=median(cpuTimeDelta), sdCpuTimeDelta=sd(cpuTimeDelta), n=n())

summarized <- summarized %>% mutate(
    seWallTimeDuration = sdWallTimeDuration / sqrt(n), 
    lowerCIWallTimeDuration = meanWallTimeDuration - qt(1 - (0.05 / 2), n - 1) * seWallTimeDuration,
    upperCIWallTimeDuration = meanWallTimeDuration + qt(1 - (0.05 / 2), n - 1) * seWallTimeDuration
)

summarized <- summarized %>% mutate(
    seCpuTimeDelta = sdCpuTimeDelta / sqrt(n), 
    lowerCICpuTimeDelta = meanCpuTimeDelta - qt(1 - (0.05 / 2), n - 1) * seCpuTimeDelta,
    upperCICpuTimeDelta = meanCpuTimeDelta + qt(1 - (0.05 / 2), n - 1) * seCpuTimeDelta
)

pdf("performance-abstraction-valuegenerating.pdf", width=15, height=5) 

par(mfrow=c(1,3), oma=c(1.5,3.5,2.5,0), mar=c(5,5,2,2), mgp=c(4,2,0))

plot(y = summarized$medianWallTimeDuration, x = summarized$statementsAssertedInferred/1000000, pch = c(10,0,6,20)[summarized$dimensions], xlab="Repository size [million statements]", ylab="", ylim=c(0,190), yaxs="i", xlim=c(0,37), xaxs="i", cex = 2, cex.axis = 2, cex.lab=2)
legend(2, 180, legend=c("3D", "4D", "Baseline"), pch=c(6, 20, 0), cex = 2)

plot(y = summarized$medianWallTimeDuration, x = summarized$statementsAssertedInferred/1000000, pch = c(0,1,4,5)[as.factor(summarized$contexts)], xlab="Repository size [million statements]", ylab="", ylim=c(0,190), yaxs="i", xlim=c(0,37), xaxs="i", cex = 2, cex.axis = 2, cex.lab=2)
legend(2, 180, legend=c("1365 Contexts", "2501 Contexts", "3906 Contexts", "Baseline"), pch=c(1, 4, 5, 0), cex = 2)

plot(y = summarized$medianWallTimeDuration, x = summarized$statementsInsertedDeleted/1000000, pch = c(0,1,4,5)[as.factor(summarized$contexts)], xlab="Delta size [million statements]", ylab="", ylim=c(0,190), yaxs="i", xlim=c(0,1.75), xaxs="i", cex = 2, cex.axis = 2, cex.lab=2)
legend(0.1, 180, legend=c("1365 Contexts", "2501 Contexts", "3906 Contexts", "Baseline"), pch=c(1, 4, 5, 0), cex = 2)

#plot(y = summarized$medianWallTimeDuration, x = summarized$contexts, pch = c(10,5,1,4)[summarized$dimensions], xlab="Number of contexts", ylab="Run time [s]", ylim=c(0,2100), yaxs="i", xlim=c(-100,4100), xaxs="i", main="Triple-Generating Abstraction")
#legend(250, 1950, legend=c("3D", "4D", "Baseline"), pch=c(1, 4, 5))

mtext(text="Median run time [s]", side=2, line=0, outer=TRUE, cex = 1.2)

mtext("(c) Value-Generating Abstraction", outer = TRUE, cex = 2)

dev.off() 