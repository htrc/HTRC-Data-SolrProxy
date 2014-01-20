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
# File:  CommonQueryStreamingOutputTest.java
# Description: TODO
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.output;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class CommonQueryStreamingOutputTest {

	@Test
	public void testWrite() {
		
		String endpoint = "http://chinkapin.pti.indiana.edu:9994/solr/meta/select/?q=id:mdp.39015013745735&start=0&rows=0";
		URL url = null;
		
		try {
			url = new URL(endpoint );
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			
			CommonQueryStreamingOutput streamingOutput = new CommonQueryStreamingOutput(conn);
			
			streamingOutput.write(byteArrayOutputStream);
			
			String result = byteArrayOutputStream.toString("UTF-8");
			
			String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			assertTrue(result.startsWith(head));
			assertTrue(result.contains("<response>"));
			assertTrue(result.contains("</response>"));
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
