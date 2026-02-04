package com.veriflow.veriflow.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import javax.jms.*;

import static javafx.application.Application.launch;

public class JmsService {

    private BrokerService broker;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private static final String QUEUE_NAME = "veriflow.2fa.queue";

    public void startBroker() {
        try {
            broker = new BrokerService();
            broker.setPersistent(false);
            broker.addConnector("vm://localhost");
            broker.start();
            System.out.println("✅ [JMS] Broker ActiveMQ wystartował.");

            initializeJmsClient();
            startConsumer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeJmsClient() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(QUEUE_NAME);

        producer = session.createProducer(destination);
    }

    public void sendMessage(String text) {
        try {
            TextMessage message = session.createTextMessage(text);
            producer.send(message);
            System.out.println("📤 [JMS Producer] Wysłano do kolejki: " + text);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void startConsumer() throws JMSException {
        Destination destination = session.createQueue(QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(destination);

        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage) {
                try {
                    String text = ((TextMessage) message).getText();

                    System.out.println("📥 [JMS Consumer] Odebrano zlecenie: " + text);
                    Thread.sleep(2000); // Udajemy, że wysyłka trwa 2 sekundy
                    System.out.println("🚀 [SMS SERVICE] Kod wysłany do klienta!");

                } catch (JMSException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopBroker() {
        try {
            if (session != null) session.close();
            if (connection != null) connection.close();
            if (broker != null) broker.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
