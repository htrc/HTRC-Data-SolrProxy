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
# File:  ErrorStreamingOutput.java
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
import java.io.StringReader;
import java.io.Writer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public class ErrorStreamingOutput implements StreamingOutput{

	private final String errorMsg;
	public ErrorStreamingOutput(String _errorMsg){
		
		StringBuilder sb = new StringBuilder("<warn>");
		
		sb.append(_errorMsg).append("</warn>");
		
		this.errorMsg = sb.toString();
	}
	
	@Override
	public void write(OutputStream outputStream) throws IOException,
			WebApplicationException {


		Writer writer = new OutputStreamWriter(outputStream);
		
		BufferedReader buffer = new BufferedReader((new StringReader(errorMsg)));

		int byteRead;

		while ((byteRead = buffer.read()) != -1) {
			writer.append((char) byteRead);
			writer.flush();
			outputStream.flush();
		}

		buffer.close();
	}
		
}
