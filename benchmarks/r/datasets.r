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

statementsInferred <- benchmarkStatistics[benchmarkStatistics$V4 == 'ModelSize-StatementsAssertedInferred', ]
statementsInferred <- statementsInferred %>% select(2,5)
statementsInferred <- statementsInferred %>% rename(benchmarkId = V2)
statementsInferred <- statementsInferred %>% rename(statementsAssertedInferred = V5)
statementsInferred$statementsAssertedInferred <- unfactor(statementsInferred$statementsAssertedInferred)

contexts <- benchmarkStatistics[benchmarkStatistics$V4 == 'ModelSize-Contexts', ]
contexts <- contexts %>% select(2,5)
contexts <- contexts %>% rename(benchmarkId = V2)
contexts <- contexts %>% rename(contexts = V5)
contexts$contexts <- unfactor(contexts$contexts)

cases <- join(begin, end)
cases <- join(cases, datasets)
cases <- join(cases, statements)
cases <- join(cases, statementsInferred)
cases <- join(cases, contexts)

grouped <- cases %>% group_by(dataset, dimensions, statementsAsserted, statementsAssertedInferred, contexts)

selected <- grouped %>% select(dataset, dimensions, contexts, statementsAsserted, statementsAssertedInferred)

datasetInformation <- summarise(selected,)
