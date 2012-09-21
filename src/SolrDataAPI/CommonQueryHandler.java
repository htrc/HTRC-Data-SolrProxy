package SolrDataAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

//POJO, no interface no extends

//The class registers its methods for the HTTP GET request using the @GET annotation. 
//Using the @Produces annotation, it defines that it can deliver several MIME types,
//text, XML and HTML. 

//The browser requests per default the HTML MIME type.

//Sets the path to base URL + /hello

@Path("/{query_string}")
public class CommonQueryHandler {

	// String BlackLight_IP = "192.168.1.12";
	private static final Logger logger = Logger
			.getLogger(CommonQueryHandler.class.getName());

	@GET
	@Produces(MediaType.TEXT_XML)
	public StreamingOutput getQueryString(
			@PathParam("query_string") String query_string,
			@Context UriInfo ui, @Context final HttpServletRequest hsr)
			throws IOException {
		System.out.println("==============================");
		System.out.println(query_string);
		final URI uri = ui.getRequestUri();

		System.out.println("------------------------------");
		System.out.println(ui.getRequestUri().toString());
		System.out.println("uri: " + uri.toString());
		// log file

		/*
		 * if (!ParamDefinition.logfile.exists()) {
		 * ParamDefinition.logfile.createNewFile(); System.out.println("\n" +
		 * "original_IP	" + "proxy_IP	" + "time	" + "query_string	" + "status");
		 * RandomAccess.writeLog(ParamDefinition.logfile.getAbsolutePath(), "\n"
		 * + "original_IP	" + "proxy_IP	" + "time	" + "query_string	" +
		 * "status"); }
		 */
		if (!ParamDefinition.logfile.exists()) {
			ParamDefinition.logfile.createNewFile();
			/*
			 * System.out.println("\n" + "original_IP	" + "proxy_IP	" + "time	"
			 * + "query_string	" + "status");
			 */
			/*
			 * RandomAccess.writeLog(ParamDefinition.logfile.getAbsolutePath(),
			 * "\n" + "original_IP	" + "proxy_IP	" + "time	" + "query_string	" +
			 * "status");
			 */
			logger.debug("\n" + "original_IP	" + "proxy_IP	" + "time	"
					+ "query_string	" + "status");
		}

		if (!uri.toString().split("\\?")[0].endsWith("/select/")
				&& !uri.toString().split("\\?")[0].endsWith("/select")) {

			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "blocked";
			// System.out.println(log_content);
			/*
			 * RandomAccess.writeLog(ParamDefinition.logfile.getAbsolutePath(),
			 * log_content);
			 */
			logger.debug(log_content);
			throw new IOException("Modification is not allowed!!!");
		}

		System.out.println("rawquery: " + uri.getRawQuery());

		/*
		 * String solr_endpoint = (uri.toString().replace(
		 * ParamDefinition.Proxy_Server_EPR, ParamDefinition.Actual_Solr_EPR));
		 */

		String solr_endpoint = ParamDefinition.Actual_Solr_EPR
				+ "/solr/select/?" + uri.getRawQuery();

		if (hsr.getParameter("qt") == null) {
			solr_endpoint = solr_endpoint + "&qt=sharding"; // make qt=sharding as default query type
		}

		System.out.println("solr EPR: " + (solr_endpoint));
		System.out.println("remote address" + hsr.getRemoteAddr());

		// long t0 = System.nanoTime();
		URL url = new URL(solr_endpoint);
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		System.out.println("response code: " + conn.getResponseCode());

		if (conn.getResponseCode() != 200) {
			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "failed";
			// System.out.println(log_content);
			/*
			 * RandomAccess.writeLog(ParamDefinition.logfile.getAbsolutePath(),
			 * log_content);
			 */

			logger.debug(log_content);
			throw new IOException(conn.getResponseMessage());
		}

		System.out.println("x-forwarded-for : "
				+ hsr.getHeader("x-forwarded-for"));

		// Logging part
		/*
		 * File logfile = new File("logfile"); if(!logfile.exists()) {
		 * logfile.createNewFile(); System.out.println("\n" + "original_IP	" +
		 * "proxy_IP	" + "time	" + "query_string	");
		 * RandomAccess.writeLog(logfile.getAbsolutePath(), "\n" +
		 * "original_IP	" + "proxy_IP	" + "time	" + "query_string	"); }
		 */

		return new StreamingOutput() {

			@Override
			public void write(OutputStream arg0) throws IOException,
					WebApplicationException {
				// long t0 = System.nanoTime();
				Writer writer = new OutputStreamWriter(arg0);
				// long t1 = System.nanoTime();

				// System.out.println("time consumed to establish a connection is: "
				// + (t1 - t0));

				long t3 = System.nanoTime();
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), "UTF8"));

				// StringBuilder builder = new StringBuilder();

				int byteRead;

				while ((byteRead = buffer.read()) != -1) {
					// builder.append((char) byteRead);
					writer.append((char) byteRead);
					writer.flush();
					arg0.flush();
				}

				buffer.close();

				Date today = new Date();

				String log_content = "\n" + hsr.getHeader("x-forwarded-for")
						+ "	" + hsr.getRemoteAddr() + "	" + today.toString()
						+ "	" + uri + "	" + "allowed";
				// System.out.println(log_content);
				/*
				 * RandomAccess.writeLog(
				 * ParamDefinition.logfile.getAbsolutePath(), log_content);
				 */
				logger.debug(log_content);
				// String text = builder.toString();

				// System.out.println("text: " + text);
				// long t4 = System.nanoTime();
				// System.out.println("time consumed to download is: " + (t4 -
				// t3));

				// long time = System.currentTimeMillis();
				/*
				 * for (int i = 0; i < 11; i++) { writer.append(" " + i + " ");
				 * if (i % 2 == 0) { writer.append('\n'); writer.flush();
				 * arg0.flush(); } if (i % 10000 == 0) { try {
				 * Thread.sleep(500); } catch (InterruptedException e) { // TODO
				 * Auto-generated catch block e.printStackTrace(); } if
				 * (System.currentTimeMillis() - time > 2000L) {
				 * System.out.format("%,d%n", i); time =
				 * System.currentTimeMillis(); } } }
				 */

			}
		};
		// return text;
	}
}
