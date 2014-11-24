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
# File:  SolrManagerTest.java
# Description: TODO
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.connection;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SolrManagerTest {

	SolrManager manager;
	static QueryResponse response;
	static SolrDocument doc;
	static String id = "mdp.39015037738534";
	
	@Before
	public void prepare(){
		manager = new SolrManager("chinkapin.pti.indiana.edu", "9545","collection1");
		
		String queryStr = "id:"+id;
		
		response = manager.query(queryStr);
		doc = response.getResults().get(0);
	}
	
	@Test
	public void testQuery() {
		
		String queryStr = "id:"+id;
		
		//response = manager.query(queryStr);
		
		//doc = response.getResults().get(0);
		
		assertEquals(id, doc.getFieldValue("id"));
	}
	
	@Test
	public void testGetFieldsMap() {
		
		Map<String, String> id2marcMap = manager.getFieldsMap(response, "id", "fullrecord");
		
		String marc = doc.getFieldValue("fullrecord").toString();
		
		assertEquals(marc, id2marcMap.get(id));
	}

}
