# Federated catalog connector

Supported configuration:

| property                                   | description                                                                                                      | required  | default   |
|--------------------------------------------|------------------------------------------------------------------------------------------------------------------|-----------|-----------|
| edc.catalog.cache.execution.delay.seconds  | The initial delay for the cache crawler engine.                                                                  | yes       | N/A       |
| edc.catalog.cache.execution.period.seconds | The time to elapse between two crawl runs.                                                                       | no        | 60        |
| edc.catalog.cache.partition.num.crawlers   | The number of crawlers (execution threads) that should be used. The engine will re-use crawlers when necessary.  | no        | 2         |
