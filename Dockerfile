FROM jrudolph/ubuntu-openjdk8-jre

MAINTAINER jrudolph

# Install gist
RUN apt-get update && apt-get install -y --no-install-recommends ruby1.9.1 && gem install gist && rm -rf /var/lib/apt/lists/*

CMD echo -n "$GIST_CREDENTIALS" > ~/.gist && gist -l && /bin/bash /tmp/update.sh

VOLUME ["/tmp/run"]

# Setup files needed for running
COPY main/target/scala-2.10/release-train-main-assembly-0.1-SNAPSHOT.jar /tmp/release-train-main-assembly-0.1-SNAPSHOT.jar

COPY scripts/update.sh /tmp/update.sh

ENV LATEST_VERSION 2.13.0-RC2