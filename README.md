# scala-data-accessor-neo4j-web
This is a WEB API that works as a microservice within the Toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This microservice get information from Neo4J graph database. outputs the result in JSON.

<img width="1197" alt="2021-09-26 19 59 57" src="https://user-images.githubusercontent.com/82787843/134804960-43050ed6-35d6-4d86-bbf4-97e0c359d63b.png">

## Dependency in toposoid Project

## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x

## Recommended environment
* Required: at least 8GB of RAM (The maximum heap memory size of the JVM is set to 6G (Application: 4G, Neo4J: 2G))
* Required: 10G or higher　of HDD

## Setup
```bssh
docker-compose up -d
```
It takes more than 20 minutes to pull the Docker image for the first time.
## Usage
```bash
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
