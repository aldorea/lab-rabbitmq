
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;

class OpenCVUtils {

    public static Mat readFile(String fileName) {
        Mat img = Highgui.imread(fileName);
        return img;
    }

    public static void writeImage(Mat mat, String dest) {
        Highgui.imwrite(dest, mat);
    }

    public static Mat blur(Mat input) {
        Mat destImage = input.clone();
        Imgproc.blur(input, destImage, new Size(3.0, 3.0));
        return destImage;
    }

    public static Mat gray(Mat input) {
        Mat gray = new Mat(input.rows(), input.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGB2GRAY);
        return gray;
    }

    public static Mat canny(Mat input) {
        Mat gray = gray(input);
        Imgproc.blur(gray, input, new Size(3, 3));
        int threshold = 2;
        Imgproc.Canny(input, input, threshold, threshold * 3, 3, false);
        Mat dest = input.clone();
        Core.add(dest, Scalar.all(0), dest);
        dest.copyTo(dest, input);
        return dest;
    }
}

class AppOpenCV {
	
	//static int counter = 0;

    public static void main(String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Esto es para trabajar con OpenCV
        String worker = "Worker 2";
        final JobCompletion jc = new JobCompletion();
        jc.setWorker(worker);
        final Channel c = ConexionRabbitMQ.getChannel();
        boolean autoAck = false;
        
        
        c.basicConsume(RabbitMQStuff.COLA_TRABAJOS, autoAck, "ID2",
                new DefaultConsumer(c) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            		throws IOException {
            	
            	
             		
	                String routingKey = envelope.getRoutingKey();
	                String contentType = properties.getContentType(); // Es un ID que identifica al mensaje
	
	                long deliveryTag = envelope.getDeliveryTag();
	                
	                String cuerpo = new String(body);
	                // Deserializamos el JSON del cuerpo del mensaje a objeto Java
	                Gson gson = new Gson();
	                ImageJob ij = gson.fromJson(cuerpo, ImageJob.class);
	                
	               /* if(counter == 25) {
	                	System.exit(0);
	                }*/
	                
	                long receptionTs = System.currentTimeMillis();
	                System.out.println("Cuerpo: " + cuerpo);
	                System.out.println("ImageJob: " + ij);
	
	                try {
	                    // Se realiza el trabajo solicitado
	                    String scpOrigen = "twcam@10.50.0.10:" + "/tmp/" + ij.getImageSrc();
	                    String scpDestino = ij.getImageDst();
	                    ProcessBuilder pb = new ProcessBuilder("scp", scpOrigen, scpDestino);
	                    Process process = pb.start();
	                    int errorCode = 0;
	                    
	                    try {
	                        errorCode = process.waitFor();
	                    } catch (InterruptedException ex) {
	                        Logger.getLogger(AppOpenCV.class.getName()).log(Level.SEVERE, null, ex);
	                    }
	                    System.out.println("scp command executed, any errors? " + (errorCode == 0 ? "No" : "Yes"));
	
	                    Mat img = OpenCVUtils.readFile(ij.getImageDst() + "/" + ij.getImageSrc());
	                    String reqAction = ij.getAction();
	
	                    System.out.println(scpOrigen + scpDestino);
	
	                    Mat action = null;
	                    String imageName = "";
	                    if (reqAction.equals("blur")) {
	                        action = OpenCVUtils.blur(img);
	                        imageName = ij.getImageDst() + "/blur-" + ij.getImageSrc();
	                        OpenCVUtils.writeImage(action, imageName);
	                    } else if (reqAction.equals("gray")) {
	                        action = OpenCVUtils.gray(img);
	                        imageName = ij.getImageDst() + "/gray-" + ij.getImageSrc();
	                        OpenCVUtils.writeImage(action, imageName);
	                    } else if (reqAction.equals("edge")) {
	                        action = OpenCVUtils.canny(img);
	                        imageName = ij.getImageDst() + "/edge-" + ij.getImageSrc();
	                        OpenCVUtils.writeImage(action, imageName);
	                    }
	
	                    ProcessBuilder pbOut = new ProcessBuilder("scp", imageName, "twcam@10.50.0.10:" + ij.getPathDst());
	                    Process processOut = pbOut.start();
	                    try {
	                        errorCode = processOut.waitFor();
	                    } catch (InterruptedException ex) {
	                        Logger.getLogger(AppOpenCV.class.getName()).log(Level.SEVERE, null, ex);
	                    }
	                    System.out.println("scp command executed, any errors? " + (errorCode == 0 ? "No" : "Yes"));
	                    
	                   // counter ++;
	                   
	
	                    // TODO Crear una instancia del tipo ImageJobjc
	                    
	                    
	                    jc.setImage(ij.getImageDst());
	                    jc.setTsCreationMessage(receptionTs - ij.getTsCreationMessage());
	                    jc.setTsReceptionWorker(receptionTs);
	                    jc.setTsFinalizationWorker(System.currentTimeMillis());
	                    
	                   
	                    
	
	                } catch (Exception e) {
	                    e.printStackTrace();
	                } finally {
	                    System.out.println("Work done");
	                   // System.out.println("Contador = "+counter);
	
	                    // Indicamos al broker que el mensaje ha sido consumido con éxito
	                    c.basicAck(deliveryTag, false);
	                    completeJob(jc);
	                }
	            }
        	
    	}
    );
}

    public static void completeJob(JobCompletion jc) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        byte[] body = gson.toJson(jc).getBytes();
        try {
            Channel c = ConexionRabbitMQ.getChannel();
            c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
            c.queueDeclare(RabbitMQStuff.COLA_TIEMPOS, false, false, false, null);
            c.queueBind(RabbitMQStuff.COLA_TIEMPOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TIEMPOS);
            c.basicPublish(RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TIEMPOS, null, body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
