/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: HTRC-Data-SolrProxy
# File:  MarcRecordsFetcher.java
# Description: accept user provided volume IDs and fetch corresponding MARC 
# records from Solr marc core and finally return the zip output stream. 
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import main.java.gov.loc.repository.pairtree.Pairtree;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;

import util.utility;
import edu.indiana.d2i.htrc.solr.connection.SolrManager;
import edu.indiana.d2i.htrc.solr.output.ErrorStreamingOutput;
import edu.indiana.d2i.htrc.solr.output.MarcRecordsFetcherStreamingOutput;

@Path("/MARC/")
public class MarcRecordsFetcher {
	@Context ServletConfig servletConfig;
	
	private static final Logger logger = Logger
			.getLogger(MarcRecordsFetcher.class.getName());

	/**
	 * accept user-provided volume IDs and return user a zip file with marc record as each zip entry
	 * 
	 * @param volume_ids user provided "|"-seperated volume IDs
	 * @param ui UriInfo object got from Context in Jersey framework
	 * @param hsr HttpServletRequest object got from Context in Jersey framework
	 * @return Response object that steams zip file back to client via StreamingOutput object
	 */
	@GET
	@Produces("application/x-zip-compressed")
	public Response getMARC(
			@QueryParam(value = "volumeIDs") String volume_ids,
			@Context UriInfo ui, @Context final HttpServletRequest hsr) {

		final URI uri = ui.getRequestUri();
		
		if(volume_ids == null){
			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "wrongParams";
			
			logger.info(log_content);
			return  Response
					.ok(new ErrorStreamingOutput("wrongParams"))
					.header("content-disposition",
							"attachment; filename = wrongParams.zip").build();
		}

		String[] volumeID_array = null;

		if (volume_ids.contains("|")) {
			volumeID_array = volume_ids.split("\\|");
		} else {
			volumeID_array = new String[1];
			volumeID_array[0] = volume_ids;
		}

		final Map<String, String> id2marc_map = queryByVolIDs(volumeID_array);

		final Set<String> id_set = id2marc_map.keySet();

		if (id_set.isEmpty()) {
			
			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "IdNotfound";

			logger.info(log_content);
			
			return  Response
					.ok(new ErrorStreamingOutput("IdNotfound"))
					.header("content-disposition",
							"attachment; filename = IdNotfound.zip").build();
		}

		StreamingOutput output = new MarcRecordsFetcherStreamingOutput(id2marc_map);
				
		Date today = new Date();
		String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
				+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
				+ "	" + "allowed";
		
		logger.info(log_content);
		return Response
				.ok(output)
				.header("content-disposition",
						"attachment; filename = marcs.zip").build();
	}

	/**
	 * based on provided volumeID_array to get a id to MARC string map
	 * 
	 * @param volumeID_array user provided volume ID array
	 * @return a id to MARC string map
	 */
	private Map<String, String> queryByVolIDs(String[] volumeID_array) {

		Map<String, String> id2marc_map = new HashMap<String, String>();
					
		ParamDefinition ParamDef = new ParamDefinition(servletConfig);
		SolrManager solrManager = new SolrManager(ParamDef.getConfig().getProperty("solr.marc.epr"), ParamDef.getConfig().getProperty("solr.marc.core.name"));
		
		String queryString = "";
		for (int i = 0; i < volumeID_array.length; i++) {

			volumeID_array[i] = utility.escape(volumeID_array[i]);

			if (i == 0)
				queryString = "id:" + volumeID_array[i];

			else {
				queryString += " OR " + "id:" + volumeID_array[i];
			}
		}
		
		QueryResponse response = solrManager.query(queryString);
	
		id2marc_map = SolrManager.getFieldsMap(response, "id", "fullrecord");

		return id2marc_map;
	}
}
