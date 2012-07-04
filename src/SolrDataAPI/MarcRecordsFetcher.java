package SolrDataAPI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import main.java.gov.loc.repository.pairtree.Pairtree;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

@Path("/MARC/")
public class MarcRecordsFetcher {
	private static final Logger logger = Logger.getLogger(MarcRecordsFetcher.class.getName());
	/**
	 * @param args
	 * @return
	 */
	@GET
	@Produces("application/x-zip-compressed")
	public static Response getMARC(
			@QueryParam(value = "volumeIDs") String volume_ids,
			@Context UriInfo ui, @Context final HttpServletRequest hsr) {

		final URI uri = ui.getRequestUri();
		
		String[] volumeID_array = null;

		if (volume_ids.contains("|")) {
			volumeID_array = volume_ids.split("\\|");
		} else {
			volumeID_array = new String[1];
			volumeID_array[0] = volume_ids;
		}

		final HashMap id2marc_map = queryByVolIDs(volumeID_array);

		final Set id_set = id2marc_map.keySet();

		if (id_set.isEmpty()) {
			Date today = new Date();
			String log_content = "\n"+ hsr.getHeader("x-forwarded-for") +"	"+ hsr.getRemoteAddr() + "	" + today.toString() + "	"+ uri+"	"+"IdNotfound";
			//System.out.println(log_content);		
			//RandomAccess.writeLog(ParamDefinition.logfile.getAbsolutePath(), log_content);		
			logger.debug(log_content);
			new Exception("IDs not found!!");
		}

		if (!ParamDefinition.logfile.exists()) {
			try {
				ParamDefinition.logfile.createNewFile();
				System.out.println("\n" + "original_IP	" + "proxy_IP	"
						+ "time	" + "query_string	" + "status");
				/*RandomAccess.writeLog(
						ParamDefinition.logfile.getAbsolutePath(), "\n"
								+ "original_IP	" + "proxy_IP	" + "time	"
								+ "query_string	" + "status");*/
				logger.debug("\n"
						+ "original_IP	" + "proxy_IP	" + "time	"
						+ "query_string	" + "status");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		StreamingOutput output = new StreamingOutput() {

			@Override
			public void write(OutputStream arg0) throws IOException,
					WebApplicationException {

				Pairtree pt = new Pairtree();
				Iterator iter = id_set.iterator();
				String id = null;
				String marc = null;
				ZipOutputStream zos = new ZipOutputStream(arg0);

				while (iter.hasNext()) {
					id = (String) iter.next();
					
					String prefix_seq[] = id.split("\\.", 2);

					marc = (String) id2marc_map.get(id);

					//zos.putNextEntry(new ZipEntry(pt.cleanId(id) + ".xml"));
					zos.putNextEntry(new ZipEntry(prefix_seq[0]+"."+ pt.cleanId(prefix_seq[1]) + ".xml"));

					zos.write(marc.getBytes());

					zos.flush();

					zos.closeEntry();

				}

				zos.flush();
				zos.close();

			}

		};
		
		Date today = new Date();
		String log_content = "\n"+ hsr.getHeader("x-forwarded-for") +"	"+ hsr.getRemoteAddr() + "	" + today.toString() + "	"+ uri+"	"+"allowed";
		//System.out.println(log_content);		
		//RandomAccess.writeLog(ParamDefinition.logfile.getAbsolutePath(), log_content);
		logger.debug(log_content);
		return Response
				.ok(output)
				.header("content-disposition",
						"attachment; filename = marcs.zip").build();

	}

	public static HashMap<String, String> queryByVolIDs(String[] volumeID_array) {

		CommonsHttpSolrServer server = null;

		HashMap<String, String> id2marc_map = new HashMap<String, String>();

		try {
			server = new CommonsHttpSolrServer(ParamDefinition.MARC_Solr_EPR
					+ "/solr/" + ParamDefinition.MARC_core_name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SolrQuery query = new SolrQuery();
		String queryString = "";
		for (int i = 0; i < volumeID_array.length; i++) {
			System.out.println("===" + volumeID_array[i]);

			if (i == 0)
				queryString = "id:" + volumeID_array[i];// maybe we need to escape ":" and "/" here for unclean IDs.

			else {

				queryString += " OR " + "id:" + volumeID_array[i];// maybe we need to escape ":" and "/" here for unclean IDs.

			}
		}
		query.setRows(Integer.MAX_VALUE);
		query.setQuery(queryString);

		try {
			QueryResponse query_response = server.query(query);

			SolrDocumentList solr_doc_list = query_response.getResults();

			System.out.println(solr_doc_list.getNumFound() + "hits found !!");

			Iterator iterator = solr_doc_list.iterator();

			while (iterator.hasNext()) {
				SolrDocument temp_solr_doc = (SolrDocument) iterator.next();

				String id = temp_solr_doc.getFieldValue("id").toString();

				String marc = temp_solr_doc.getFieldValue("marc").toString();

				id2marc_map.put(id, marc);

				System.out.println("--------------------------------");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return id2marc_map;

	}
private static String escape(String uncleanID) {
		
		if(uncleanID.contains(":"))
		{
			return uncleanID.replace(":", "\\:");
		}
		
		return uncleanID;
	}
}
