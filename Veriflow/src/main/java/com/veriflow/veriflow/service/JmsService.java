package com.veriflow.veriflow.service;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JmsService {

    private Connection connection;
    private Session session;
    private MessageProducer producer;

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "veriflow.2fa.queue";

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    public void startBroker() {
        try {
            System.out.println("⏳ [JMS] Łączenie z zewnętrznym brokerem ActiveMQ...");

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);

            connectionFactory.setTrustAllPackages(true);

            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);

            producer = session.createProducer(destination);

            System.out.println("✅ [JMS] Połączono z ActiveMQ na porcie 61616.");

            startConsumer();

        } catch (JMSException e) {
            System.err.println("❌ BŁĄD: Nie można połączyć się z Dockerem ActiveMQ!");
            System.err.println("👉 Upewnij się, że wpisałeś: 'docker run -d -p 61616:61616 ...'");
            e.printStackTrace();
        }
    }

    public void sendMessage(String text) {
        try {
            if (session == null) return;
            TextMessage message = session.createTextMessage(text);
            producer.send(message);
            System.out.println("📤 [JMS Producer] Wysłano: " + text);
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

                    System.out.println("📥 [JMS Consumer] Odebrano: " + text);

                    Thread.sleep(2000);

                    System.out.println("🚀 [SMS SERVICE] Kod wysłany do klienta (z zewnętrznego brokera)!");

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
            System.out.println("🛑 [JMS] Rozłączono z brokerem.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}