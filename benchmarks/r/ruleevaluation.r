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

statements <- benchmarkStatistics[benchmarkStatistics$V4 == 'ModelSize-StatementsAsserted', ]
statements <- statements %>% select(2,5)
statements <- statements %>% rename(benchmarkId = V2)
statements <- statements %>% rename(statementsAsserted = V5)
statements$statementsAsserted <- unfactor(statements$statementsAsserted)

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
cases <- join(cases, statements)
cases <- join(cases, contexts)
cases <- join(cases, statementsInserted)
cases <- join(cases, statementsDeleted)

cases <- cases %>% mutate(wallTimeDuration = as.duration(ymd_hms(cases$timestampEnd) - ymd_hms(cases$timestampBegin)))
cases <- cases %>% mutate(cpuTimeDelta = cases$cpuTimeEnd - cases$cpuTimeBegin)
cases <- cases %>% mutate(statementsInsertedDeleted = cases$statementsInserted + cases$statementsDeleted)

cases$timestampBegin <- unfactor(cases$timestampBegin)

filtered <- cases[cases$case == 'RuleEvaluation-Evaluate' & 
                  !(grepl('Abstracted', cases$dataset)) &
                  !(grepl('Baseline', cases$dataset)), ]

grouped <- filtered %>% group_by(case, dataset, dimensions, statementsAsserted, contexts)

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

pdf("performance-ruleevaluation.pdf", width=15, height=5) 

par(mfrow=c(1,3), oma=c(1.5,3.5,2.5,0), mar=c(5,5,2,2), mgp=c(4,2,0))

plot(y = summarized$medianWallTimeDuration, x = summarized$statementsAsserted/1000000, pch = c(10,0,6,20)[summarized$dimensions], xlab="Repository size [million statements]", ylab="", ylim=c(0,2200), yaxs="i", xlim=c(0,37), xaxs="i", cex = 2, cex.axis = 2, cex.lab=2)
legend(2, 2050, legend=c("3D", "4D"), pch=c(6, 20), cex = 2)

plot(y = summarized$medianWallTimeDuration, x = summarized$contexts, pch = c(10,0,6,20)[summarized$dimensions], xlab="Number of contexts", ylab="", ylim=c(0,2200), yaxs="i", xlim=c(0,4200), xaxs="i", cex = 2, cex.axis = 2, cex.lab=2)
legend(200, 2050, legend=c("3D", "4D"), pch=c(6, 20), cex = 2)

mtext(text="Median run time [s]", side=2, line=0, outer=TRUE, cex = 1.2)

mtext("Rule Evaluation", outer = TRUE, cex = 2)

dev.off() 