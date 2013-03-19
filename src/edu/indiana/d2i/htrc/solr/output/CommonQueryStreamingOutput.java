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
# File:  CommonQueryReturnStream.java
# Description: TODO
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.solr.proxy.MarcRecordsFetcher;

public class CommonQueryStreamingOutput implements StreamingOutput{
	HttpURLConnection conn; // important member. For getting InputStream from http Solr instance connection
	
	/**
	 * Constructor
	 * 
	 * @param conn For getting InputStream from http connection to Solr instance 
	 */
	public CommonQueryStreamingOutput(HttpURLConnection conn){
		
		this.conn = conn;
	}
	
	// stream the input from http connection to solr to outputStream arg0
	@Override
	public void write(OutputStream arg0) throws IOException,
			WebApplicationException {

		Writer writer = new OutputStreamWriter(arg0);
		
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), "UTF8"));

		int byteRead;

		while ((byteRead = buffer.read()) != -1) {
			writer.append((char) byteRead);
			writer.flush();
			arg0.flush();
		}

		buffer.close();
	}

}
