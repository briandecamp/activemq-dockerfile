docker-activemq
===================

Build the test app:
```
mvn clean install
```

Build the image:
```
sudo docker build -t bdecamp/activemq:5.8.0 .
```

Get the IP address of lxcbr0 interface that docker uses.
```
HOST_IP=`ifconfig lxcbr0 | grep 'inet addr' | cut -d ":" -f 2 | cut -d " " -f 1`
```

Start two copies of the image, bypassing the CMD, and activate networking.
```
sudo docker run -t -i -dns ${HOST_IP} -h amq01 bdecamp/activemq:5.8.0 /bin/bash
sudo docker run -t -i -dns ${HOST_IP} -h amq02 bdecamp/activemq:5.8.0 /bin/bash
```

The multicast protocol will send out a connection string to clients with the hostname.
We need a hostname and an /etc/hosts entry for each broker.
Update the /etc/hosts file with the docker IP addresses so the brokers can find each other by name.
```
sudo ./update-hosts
```

In each amq image, manually start the brokers.
I can't get this to work with sudo service activemq start for some reason.
```
root@amq01:/# java -Xmx1G -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.password.file=/opt/activemq/conf/jmx.password -Dcom.sun.management.jmxremote.access.file=/opt/activemq/conf/jmx.access -Djava.util.logging.config.file=logging.properties -Dcom.sun.management.jmxremote -Djava.io.tmpdir=/opt/activemq/tmp -Dactivemq.classpath=/opt/activemq/conf -Dactivemq.home=/opt/activemq -Dactivemq.base=/opt/activemq -Dactivemq.conf=/opt/activemq/conf -Dactivemq.data=/opt/activemq/data -jar /opt/activemq/bin/activemq.jar start 

root@amq02:/# java -Xmx1G -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.password.file=/opt/activemq/conf/jmx.password -Dcom.sun.management.jmxremote.access.file=/opt/activemq/conf/jmx.access -Djava.util.logging.config.file=logging.properties -Dcom.sun.management.jmxremote -Djava.io.tmpdir=/opt/activemq/tmp -Dactivemq.classpath=/opt/activemq/conf -Dactivemq.home=/opt/activemq -Dactivemq.base=/opt/activemq
-Dactivemq.conf=/opt/activemq/conf -Dactivemq.data=/opt/activemq/data -jar /opt/activemq/bin/activemq.jar start 
```

Start a third image to run the test, again bypassing the CMD
```
sudo docker run -t -i -dns ${HOST_IP} -h tester bdecamp/activemq:5.8.0 /bin/bash
```

Run the test
```
root@tester:/# java -jar /opt/docker-activemq-0.0.1-SNAPSHOT.jar
```
