FROM toposoid/toposoid-core:0.1.0

WORKDIR /app

ENV DEPLOYMENT=local
ENV _JAVA_OPTIONS="-Xms2g -Xmx4g"

RUN git clone https://github.com/toposoid/scala-data-accessor-neo4j-web.git \
&& cd scala-data-accessor-neo4j-web \
&& sbt playUpdateSecret 1> /dev/null \
&& sbt dist \
&& cd /app/scala-data-accessor-neo4j-web/target/universal \
&& unzip -o scala-data-accessor-neo4j-web-0.1.0.zip


COPY ./docker-entrypoint.sh /app/
ENTRYPOINT ["/app/docker-entrypoint.sh"]

