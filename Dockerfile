FROM toposoid/toposoid-scala-lib:0.6-SNAPSHOT

WORKDIR /app
ARG TARGET_BRANCH
ARG JAVA_OPT_XMX
ENV DEPLOYMENT=local

RUN git clone https://github.com/toposoid/toposoid-test-utils.git \
&& cd toposoid-test-utils \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/scala-data-accessor-neo4j.git \
&& cd scala-data-accessor-neo4j \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt publishLocal \
&& rm -Rf ./target \
&& cd .. \
&& git clone https://github.com/toposoid/data-accessor-neo4j-web.git \
&& cd data-accessor-neo4j-web \
&& git fetch origin ${TARGET_BRANCH} \
&& git checkout ${TARGET_BRANCH} \
&& sbt playUpdateSecret 1> /dev/null \
&& sbt dist \
&& cd /app/data-accessor-neo4j-web/target/universal \
&& unzip -o data-accessor-neo4j-web-0.6-SNAPSHOT.zip


COPY ./docker-entrypoint.sh /app/
ENTRYPOINT ["/app/docker-entrypoint.sh"]

