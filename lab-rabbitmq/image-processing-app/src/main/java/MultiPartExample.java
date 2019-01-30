
import java.io.PrintWriter;
import java.io.StringWriter;
import cs.edu.uv.http.dynamicresponse.MultipartUtils;
import cs.edu.uv.http.dynamicresponse.ResponseClass;
import cs.edu.uv.http.dynamicresponse.ThingsAboutRequest;
import cs.edu.uv.http.dynamicresponse.ThingsAboutResponse;
import java.util.HashMap;

public class MultiPartExample extends ResponseClass {
	public void ifPost(ThingsAboutRequest req, ThingsAboutResponse resp) throws Exception {
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
