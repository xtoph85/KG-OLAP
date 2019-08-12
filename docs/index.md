A knowledge graph (KG) represents real-world entities and their relationships with each other. The thus represented knowledge is often context-dependent, leading to the construction of contextualized KGs. Due to the multidimensional and hierarchical nature of context, the multidimensional OLAP cube model from data analysis is a natural fit for the representation of contextualized KGs. Traditional systems for online analytical processing (OLAP) employ cube models to represent numeric values for further processing using dedicated query operations. 

Knowledge Graph OLAP (KG-OLAP) adapts the OLAP cube model for working with contextualized KGs. In particular, the roll-up operation from traditional OLAP is decomposed into a merge and an abstraction operation. The merge operation corresponds to the selection of knowledge from different contexts whereas abstraction replaces entities with more general entities. The result of such a query is a more abstract, high-level view on the contextualized KG.

The following figure illustrates the difference between traditional OLAP and KG-OLAP. In traditional OLAP, each cell has numeric measures and the cell's dimension attributes characterize a fact of interest. In KG-OLAP, each cell of the OLAP cube comprises an RDF graph and the cell's dimension attributes represent the context which the knowledge is relevant for.

![OLAP vs. KG-OLAP](img/kgolap-overview.png)

## Implementation


## Benchmarks
The KG-OLAP system comes with a benchmarking feature that allows to run performance experiments. When executed in benchmarking mode, the KG-OLAP system produces two log files for each query execution. The first captures the timestamps of both the beginning and end of certain operations ("wall time"), e.g., the execution of the SPARQL query calculating the "delta" table.

The `DemoRunner` class can be used to run performance experiments.

A number of predefined (procedurally generated) datasets and corresponding benchmark queries demonstrate the KG-OLAP system.

Using the predefined datasets and queries, we ran performance experiments on a virtual CentOS 6.8 machine with four cores of an Intel Xeon CPU E5-2640 v4 with 2.4 GHz, hosting a GraphDB 8.9 instance. The Java Virtual Machine(JVM) of the GraphDB instance ran with 100 GB heap space. The JVM of the KG-OLAP cube, which conducts rule evaluation and caches query results, ran with 20 GB heap space.

The GraphDB instance comprised two repositories -- base and temporary -- with the following configuration; please refer to the [GraphDB manual](http://graphdb.ontotext.com/documentation/8.9/free/configuring-a-repository.html "Configuring a repository") for further information. The entity index size was 30 000 000 and the entity identifier size was 32 bits. Context index, predicate list, and literal index were enabled. Reasoning and inconsistency checkswere disabled; the KG-OLAP implementation takes care of reasoning via RDFpro rule evaluation.

    @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.
    @prefix rep: <http://www.openrdf.org/config/repository#>.
    @prefix sr: <http://www.openrdf.org/config/repository/sail#>.
    @prefix sail: <http://www.openrdf.org/config/sail#>.
    @prefix owlim: <http://www.ontotext.com/trree/owlim#>.
    
    [] a rep:Repository ;
        rep:repositoryID "Base" ;
        rdfs:label "" ;
        rep:repositoryImpl [
            rep:repositoryType "graphdb:FreeSailRepository" ;
            sr:sailImpl [
                sail:sailType "graphdb:FreeSail" ;
            
                owlim:base-URL "http://dkm.fbk.eu/ckr/meta#" ;
                owlim:defaultNS "" ;
                owlim:entity-index-size "30000000" ;
                owlim:entity-id-size  "32" ;
                owlim:imports "" ;
                owlim:repository-type "file-repository" ;
                owlim:ruleset "empty" ;
                owlim:storage-folder "storage" ;
 
                owlim:enable-context-index "true" ;
  
                owlim:enablePredicateList "true" ;

                owlim:in-memory-literal-properties "true" ;
                owlim:enable-literal-index "true" ;
    
                owlim:check-for-inconsistencies "false" ;
                owlim:disable-sameAs  "true" ;
                owlim:query-timeout  "0" ;
                owlim:query-limit-results  "0" ;
                owlim:throw-QueryEvaluationException-on-timeout "false" ;
                owlim:read-only "false" ;
            ]
        ].
