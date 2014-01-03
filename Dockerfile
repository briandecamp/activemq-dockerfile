FROM    ubuntu:12.04

MAINTAINER Brian DeCamp < bdecamp [at] blackstonebay {dot} com>

# make sure the package repository is up to date
RUN echo "deb http://archive.ubuntu.com/ubuntu precise main universe" > /etc/apt/sources.list

RUN apt-get update 
RUN apt-get -y install python-software-properties

RUN apt-get install -y -q openjdk-7-jre-headless wget

# some tools
RUN apt-get -y install curl telnet vim inetutils-ping

# download activemq
RUN cd /opt; curl -L http://www.carfab.com/apachesoftware/activemq/apache-activemq/5.8.0/apache-activemq-5.8.0-bin.tar.gz | tar -xzv

RUN ln -sf /opt/apache-activemq-5.8.0 /opt/activemq
RUN ln -sf /opt/activemq/bin/activemq /etc/init.d/
RUN update-rc.d activemq defaults
RUN /etc/init.d/activemq setup /etc/default/activemq

# Use our own /etc/default/activemq to activate jmx
ADD etc/default /etc/default

# Use our own activemq.xml config
ADD conf /opt/apache-activemq-5.8.0/conf
RUN chmod 600 /opt/activemq/conf/jmx.password

# Add a test jar
ADD target/docker-activemq-0.0.1-SNAPSHOT.jar /opt/

EXPOSE 6155 6156 61616 61617 1099 8161

# CMD service activemq start
# Not sure why 'service' doesn't seem to work
CMD java -Xmx1G -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.password.file=/opt/activemq/conf/jmx.password -Dcom.sun.management.jmxremote.access.file=/opt/activemq/conf/jmx.access -Djava.util.logging.config.file=logging.properties -Dcom.sun.management.jmxremote -Djava.io.tmpdir=/opt/activemq/tmp -Dactivemq.classpath=/opt/activemq/conf -Dactivemq.home=/opt/activemq -Dactivemq.base=/opt/activemq -Dactivemq.conf=/opt/activemq/conf -Dactivemq.data=/opt/activemq/data -jar /opt/activemq/bin/activemq.jar start 

    