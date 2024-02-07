# scala-data-accessor-neo4j-web
This is a WEB API that works as a microservice within the Toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This microservice get information from Neo4J graph database. outputs the result in JSON.

[![Unit Test And Build Image Action](https://github.com/toposoid/scala-data-accessor-neo4j-web/actions/workflows/action.yml/badge.svg?branch=main)](https://github.com/toposoid/scala-data-accessor-neo4j-web/actions/workflows/action.yml)

<img width="929" src="https://github.com/toposoid/scala-data-accessor-neo4j-web/assets/82787843/5413ffb9-7772-48e6-859a-2dadd99eb371">

## Dependency in toposoid Project

## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x

## Recommended Environment For Standalone
* Required: at least 3GB of RAM (The maximum heap memory size of the JVM is set to 1G (Application: 1G, Neo4J: 2G))
* Required: at least 1.59G of HDD(Docker Image Size)

## Setup For Standalone
```bssh
docker-compose up
```
The first startup takes a long time until docker pull finishes.
## Usage
```bash
#For unspecified queries
curl -X POST -H "Content-Type: application/json" -d '{ "query":"MATCH (n:ClaimNode)-[e:ClaimEdge]-(m:ClaimNode) return n,e,m", "target": "" }' http://localhost:9005/getQueryResult

#If you want to convert the result to a specific Toposoid object
curl -X POST -H "Content-Type: application/json" -d '{ "query":"MATCH (n:ClaimNode)-[e:ClaimEdge]-(m:ClaimNode) return n,e,m", "target": "" }' http://localhost:9005/getQueryFormattedResult
```

## Note
* This microservice uses 9005 as the default port.
* If you want to run in a remote environment or a virtual environment, change PRIVATE_IP_ADDRESS in docker-compose.yml according to your environment.

## License
toposoid/scala-data-accessor-neo4j-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
