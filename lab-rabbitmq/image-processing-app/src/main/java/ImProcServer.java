import java.text.*;
import java.util.*;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import cs.edu.uv.http.dynamicresponse.MultipartUtils;

import cs.edu.uv.http.dynamicresponse.ResponseClass;
import cs.edu.uv.http.dynamicresponse.ThingsAboutRequest;
import cs.edu.uv.http.dynamicresponse.ThingsAboutResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;

public class ImProcServer extends ResponseClass{
   // Directorio donde guardaremos las imágenes que nos envían
   private static final String PATH_SRC="/tmp/images/";
   // Directorio donde se deben guardar las imágenes procesadas
   private static final String PATH_DST="/var/web/resources/images";


   public ImProcServer(){}

   public void ifPost(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception{
      Channel c = ConexionRabbitMQ.getChannel();
      
      try {
            if (MultipartUtils.isMultipartFormData(req.getHeaders())) {
                MultipartUtils multipartUtils = new MultipartUtils(req);		
				
                // This is a HashMap to store params from multipart body
                HashMap<String,String> params = new HashMap<String,String>();
                // This is a HashMap to store info about files saved from multipart body
                HashMap<String,String> files = new HashMap<String,String>();
                // This is the path where files will be stored
                String path="/tmp";

                multipartUtils.parseMultipart(params, files, path);

                System.out.println(params.get("ACTION"));
                System.out.println(files.get("IMAGE"));
                
                long startMessage = System.currentTimeMillis();
                
                // TODO Crear una instancia del tipo ImageJob
                ImageJob ij = new ImageJob();
                ij.setAction(params.get("ACTION"));
                ij.setPathSrc(PATH_SRC);
                ij.setImageSrc(files.get("IMAGE"));
                ij.setPathDst(PATH_DST);
                ij.setImageDst(path);
                ij.setTsCreationMessage(startMessage);
                
                // Serialización de la instancia del tipo ImageJob a JSON (String)
                Gson gson = new Gson();
                String json = gson.toJson(ij);  
                // Usamos el canal para definir: el exchange, la cola y la asociación exchange-cola
                c.exchangeDeclare(RabbitMQStuff.EXCHANGE, "direct", true);
                c.queueDeclare(RabbitMQStuff.COLA_TRABAJOS, true, false, false, null);
                c.queueBind(RabbitMQStuff.COLA_TRABAJOS, RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TRABAJOS);

                // Obtención del cuerpo del mensaje
                byte[] messageBody = json.getBytes();
                // Publicar el mensaje con el trabajo a realizar
                c.basicPublish(RabbitMQStuff.EXCHANGE, RabbitMQStuff.RK_TRABAJOS, null, messageBody);

                resp.flushResponseHeaders();
                PrintWriter pw = resp.getWriter();
                pw.println("{\"status\":\"ok\"}");
                pw.flush();
                pw.close();
            }
        } catch (Exception ex) {
                resp.setStatus(500);
                resp.flushResponseHeaders();
                PrintWriter pw = resp.getWriter();
                pw.println("<html><body>");
                StringWriter sw = new StringWriter();
                PrintWriter pwt = new PrintWriter(sw);
                ex.printStackTrace(pwt);
                pw.println(sw.toString());
                pw.println("</body></html>");
                pw.flush();
                pw.close();
        }
    }
   }