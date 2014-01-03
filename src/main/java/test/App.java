package test;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
 
/**
 * Hello world!
 */
public class App {
    public static final String CONNECTION_STRING = "multicast://default?group=client";
    //public static final String CONNECTION_STRING = "discovery:(multicast://default?group=client)";
    //public static final String CONNECTION_STRING = "failover:(multicast://default?group=client)";
    //public static final String CONNECTION_STRING = "tcp://amq01:61616";
    //public static final String CONNECTION_STRING = "failover:(tcp://mq01.texo.gorilla.dev.opal.synacor.com:61616)";
    //public static final String CONNECTION_STRING = "discovery:(tcp://mq01.texo.gorilla.dev.opal.synacor.com:61616)";
    public static ActiveMQConnectionFactory connectionFactory;
    
    public static void main(String[] args) throws Exception {
        String url = System.getProperty("activemq.url");
        if(url == null) url = CONNECTION_STRING;
        System.out.println("Creating connection factory: " + url);
        connectionFactory = new ActiveMQConnectionFactory(CONNECTION_STRING);
        
        for(int i = 0; i < 10; i++) thread(new HelloWorldProducer(), false);
        for(int i = 0; i < 100; i++) thread(new HelloWorldConsumer(), false);
    }
 
    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }
 
    public static class HelloWorldProducer implements Runnable {
        public void run() {
            try {
                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();
 
                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST.FOO");
 
                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 
                int countDown = 1000;
                while(countDown-- > 0) {
                  // Create a messages
                  String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
                  TextMessage message = session.createTextMessage(text);
   
                  // Tell the producer to send the message
                  System.out.println("Sending message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
                  producer.send(message);
                  Thread.sleep((int)(Math.random() * 10000));
                }
 
                // Clean up
                session.close();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }
 
    public static class HelloWorldConsumer implements Runnable, ExceptionListener {
        public void run() {
            try {
 
                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();
 
                connection.setExceptionListener(this);
 
                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST.FOO");
 
                // Create a MessageConsumer from the Session to the Topic or Queue
                MessageConsumer consumer = session.createConsumer(destination);
 
                int countDown = 10000;
                while(countDown-- > 0) {
                  // Wait for a message
                  Message message = consumer.receive();
   
                  if (message instanceof TextMessage) {
                      TextMessage textMessage = (TextMessage) message;
                      String text = textMessage.getText();
                      System.out.println("Received: " + text + " in " + Thread.currentThread().getName());
                  } else {
                      System.out.println("Received: " + message);
                  }
                }
 
                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
 
        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }
    }
}
