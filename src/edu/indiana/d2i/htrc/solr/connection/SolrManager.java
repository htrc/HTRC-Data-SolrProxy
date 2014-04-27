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
# Project: solr
# File:  SolrManager.java
# Description: TODO
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.connection;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SolrManager {

	private SolrServer solrServer = null;
	
	public SolrServer getSolrServer() {
		return solrServer;
	}

	/**
	 * Constructor
	 * 
	 * @param solrEPR endpoint of Solr head shard
	 * @param coreName	name of Solr core
	 */
	public SolrManager(String solrhost, String port, String coreName){
		
		solrServer = new HttpSolrServer("http://" + solrhost+":" + port + "/solr/" + coreName);
	}
	
	/**
	 * send query String to Solr and get response
	 * 
	 * @param queryStr query String that can be parsed by Solr Query Parser 
	 * @return query response, a org.apache.solr.client.solrj.response.QueryResponse object
	 */
	public QueryResponse query(String queryStr){
		
		SolrQuery query = new SolrQuery();
		
		query.setRows(Integer.MAX_VALUE);
		query.setQuery(queryStr);
		
		QueryResponse query_response = null;
		try {
			query_response = solrServer.query(query);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}

		return query_response;
	}

	/**
	 * get a map from the document list in query response, value of filed fieldAsKey as key and value of filedAsValue as value
	 * 
	 * @param response query response from Solr, a org.apache.solr.client.solrj.response.QueryResponse object 
	 * @param fieldAsKey the value of fieldAsKey is a key in the returned map
	 * @param fieldAsValue the value of fileAsValue is a value in the returned map
	 * @return a Map object in which each key/value pair is extracted from a solr document in QueryResponse response.
	 */
	public static Map<String, String> getFieldsMap(QueryResponse response, String fieldAsKey,
			String fieldAsValue) {
		
		HashMap<String, String> id2marc_map = new HashMap<String, String>();
		
		SolrDocumentList solr_doc_list = response.getResults();
		
		Iterator<SolrDocument> iterator = solr_doc_list.iterator();

		while (iterator.hasNext()) {
			SolrDocument temp_solr_doc = iterator.next();

			String id = temp_solr_doc.getFieldValue(fieldAsKey).toString();

			String marc = temp_solr_doc.getFieldValue(fieldAsValue).toString();

			id2marc_map.put(id, marc);
		}
		
		return id2marc_map;
	}
}
