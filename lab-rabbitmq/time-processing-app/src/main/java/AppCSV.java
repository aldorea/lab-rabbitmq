
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author twcam
 */
public class AppCSV {
    public static void main(String[] args) {
//        Logger logger = LoggerFactory.getLogger(ReceivedJobCompleted.class);

        System.out.println("Running JobCompletions");

        try {
            final Channel c = ConexionRabbitMQ.getChannel();
            c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
            c.queueDeclare(RabbitMQStuff.COLA_TIEMPOS, false, false, false, null);
            c.queueBind(RabbitMQStuff.COLA_TIEMPOS, RabbitMQStuff.EXCHANGE, "");
            c.basicQos(1);

            Consumer consumer = new DefaultConsumer(c) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {

                    JobCompletion jc = new JobCompletion();
                    try {
                        String msg = new String(body, "UTF-8");
                        jc = new Gson().fromJson(msg, JobCompletion.class);
                    } finally {
                        System.out.println("Time Received: " + jc.getWorker() + ": "
                                + (jc.getTsFinalizationWorker() - jc.getTsReceptionWorker()) + " ms");
                        c.basicAck(envelope.getDeliveryTag(), false);
                        try {
                            String[] entradas = { jc.getWorker(), String.valueOf(jc.getTsCreationMessage()), String.valueOf(jc.getTsReceptionWorker()), String.valueOf(jc.getTsFinalizationWorker()) };
                            crearCsv(entradas);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
            boolean autoAck = false;
            c.basicConsume(RabbitMQStuff.COLA_TIEMPOS, autoAck, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void crearCsv(String[] entradas) throws IOException {
        try {
            String nombre = "/var/web/resources/worker-stats.csv";
            FileWriter fw = new FileWriter(nombre, true);

            for (String entrada: entradas) {
                fw.append(entrada);
                fw.append(",");
            }
            fw.append("\n");
            fw.flush();
            fw.close();
            System.out.println("CSV created successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
