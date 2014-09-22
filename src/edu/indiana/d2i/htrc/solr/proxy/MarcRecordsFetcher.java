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

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;

import util.utility;
import edu.indiana.d2i.htrc.solr.connection.SolrManager;
import edu.indiana.d2i.htrc.solr.output.ErrorStreamingOutput;
import edu.indiana.d2i.htrc.solr.output.MarcRecordsFetcherStreamingOutput;

@Path("/MARC/")
public class MarcRecordsFetcher {
	@Context
	ServletConfig servletConfig;

	private static final Logger logger = Logger.getLogger(MarcRecordsFetcher.class.getName());

	/**
	 * accept user-provided volume IDs and return user a zip file with marc
	 * record as each zip entry
	 * 
	 * @param volume_ids
	 *            user provided "|"-seperated volume IDs
	 * @param ui
	 *            UriInfo object got from Context in Jersey framework
	 * @param hsr
	 *            HttpServletRequest object got from Context in Jersey framework
	 * @return Response object that steams zip file back to client via
	 *         StreamingOutput object
	 */
	@GET
	@Produces("application/x-zip-compressed")
	public Response getMARC(@QueryParam(value = "volumeIDs") String volumeIDs, @Context UriInfo ui, @Context final HttpServletRequest hsr) {
		final URI uri = ui.getRequestUri();

		if (volumeIDs == null) {
			Date today = new Date();
			String logContent = "\n" + hsr.getHeader("x-forwarded-for") + "	" + hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri + "	" + "wrongParams";

			logger.info(logContent);
			return Response.ok(new ErrorStreamingOutput("wrongParams")).header("content-disposition", "attachment; filename = wrongParams.zip").build();
		}

		if (volumeIDs.trim().equals("")) {
			Date today = new Date();
			String logContent = "\n" + hsr.getHeader("x-forwarded-for") + "	" + hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri + "	" + "emptyID";

			logger.info(logContent);
			return Response.ok(new ErrorStreamingOutput("emptyID")).header("content-disposition", "attachment; filename = emptyID.zip").build();
		}

		String[] volumeIdArray = null;

		if (volumeIDs.contains("|")) {
			volumeIdArray = volumeIDs.split("\\|");
		} else {
			volumeIdArray = new String[1];
			volumeIdArray[0] = volumeIDs;
		}

		final Map<String, String> id2marcMap = queryByVolIDs(volumeIdArray);

		if (id2marcMap.isEmpty()) {
			Date today = new Date();
			String logContent = "\n" + hsr.getHeader("x-forwarded-for") + "	" + hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri + "	" + "IdNotfound";
			logger.info(logContent);
			// just return a empty zip file
			return Response.ok(new MarcRecordsFetcherStreamingOutput(id2marcMap)).header("content-disposition", "attachment; filename = IdNotfound.zip").build();
		}

		StreamingOutput output = new MarcRecordsFetcherStreamingOutput(id2marcMap);

		Date today = new Date();
		String logContent = "\n" + hsr.getHeader("x-forwarded-for") + "	" + hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri + "	" + "allowed";

		logger.info(logContent);
		return Response.ok(output).header("content-disposition", "attachment; filename = marcs.zip").build();
	}

	/**
	 * based on provided volumeIdArray to get a id to MARC string map
	 * 
	 * @param volumeIdArray
	 *            user provided volume ID array
	 * @return a id to MARC string map
	 */
	private Map<String, String> queryByVolIDs(String[] volumeIdArray) {
		Map<String, String> id2marcMap = new HashMap<String, String>();

		ParamDefinition ParamDef = new ParamDefinition(servletConfig);
		SolrManager solrManager = new SolrManager(ParamDef.getConfig().getProperty("solr.meta.host"), ParamDef.getConfig().getProperty("solr.meta.port"), ParamDef.getConfig().getProperty(
				"solr.meta.core"));

		StringBuilder queryStringBuilder = new StringBuilder();
		for (int i = 0; i < volumeIdArray.length; i++) {
			volumeIdArray[i] = utility.escape(volumeIdArray[i]);
			if (i == 0) {
				queryStringBuilder.append("id:").append(volumeIdArray[i]);
			} else {
				queryStringBuilder.append(" OR ").append("id:").append(volumeIdArray[i]);
			}
		}
		QueryResponse response = solrManager.query(queryStringBuilder.toString());
		id2marcMap = SolrManager.getFieldsMap(response, "id", "fullrecord");

		return id2marcMap;
	}
}
