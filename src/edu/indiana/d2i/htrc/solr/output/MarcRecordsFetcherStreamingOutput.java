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
# File:  MarcRecordsFetcherStreamingOutput.java
# Description: TODO
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import main.java.gov.loc.repository.pairtree.Pairtree;

public class MarcRecordsFetcherStreamingOutput implements StreamingOutput{

	Map<String, String> id2marc_map;
	
	public MarcRecordsFetcherStreamingOutput(Map<String, String> id2marc_map){
		this.id2marc_map = id2marc_map;
	}
	
	@Override
	public void write(OutputStream arg0) throws IOException,
			WebApplicationException {

		Set<String> id_set = id2marc_map.keySet();
		Pairtree pt = new Pairtree();
		Iterator<String> iter = id_set.iterator();
		String id = null;
		String marc = null;
		ZipOutputStream zos = new ZipOutputStream(arg0);

		while (iter.hasNext()) {
			id = (String) iter.next();

			String prefix_seq[] = id.split("\\.", 2);

			marc = (String) id2marc_map.get(id);

			zos.putNextEntry(new ZipEntry(prefix_seq[0] + "."
					+ pt.cleanId(prefix_seq[1]) + ".xml"));

			zos.write(marc.getBytes());

			zos.flush();

			zos.closeEntry();
		}
	
		zos.flush();
		zos.close();
	}
}
