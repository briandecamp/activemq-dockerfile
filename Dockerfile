FROM    ubuntu:12.04

MAINTAINER Brian DeCamp < bdecamp [at] blackstonebay {dot} com>

# make sure the package repository is up to date
RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe" > /etc/apt/sources.list

RUN apt-get update 
RUN apt-get -y install python-software-properties

RUN apt-get install -y -q openjdk-7-jre-headless wget

RUN apt-get -y install curl
RUN cd /opt; curl -L http://www.carfab.com/apachesoftware/activemq/apache-activemq/5.8.0/apache-activemq-5.8.0-bin.tar.gz | tar -xzv
RUN ln -sf /opt/apache-activemq-5.8.0 /opt/activemq
RUN ln -sf /opt/activemq/bin/activemq /etc/init.d/
# RUN update-rc.d activemq defaults
# RUN /etc/init.d/activemq setup /etc/default/activemq
ADD etc/default /etc/default
ADD conf /opt/apache/conf

EXPOSE 6255 61616 61617 1099 8080

CMD service activemq start
