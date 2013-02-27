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
# File:  MarcRecordsFetcherStreamingOutputTest.java
# Description: TODO
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.output;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

public class MarcRecordsFetcherStreamingOutputTest {

	@Test
	public void testWrite() {

		Map<String, String> id2marc_map = new HashMap<String, String>();
		String marc = "<record>only for test</record>";
		id2marc_map.put("mdp.39015013745735", marc);
		
		MarcRecordsFetcherStreamingOutput streamingOutput = new MarcRecordsFetcherStreamingOutput(id2marc_map);
		
		try {
			String resultFileName = "testMarc.zip";
			FileOutputStream fos = new FileOutputStream(resultFileName);
			streamingOutput.write(fos);
			
			fos.close();
			
			ZipFile zipFile = new ZipFile(resultFileName);
			
			ZipInputStream zis = new ZipInputStream(new FileInputStream(resultFileName));
			ZipEntry entry = zis.getNextEntry();
			
			if (entry != null) {

				InputStream is = zipFile.getInputStream(entry);
				byte[] buffer = new byte[is.available()];
				is.read(buffer, 0, is.available());

				String actualMarcStr = new String(buffer, "UTF-8");

				assertEquals(marc, actualMarcStr);
				
				zis.closeEntry();
				zis.close();
				is.close();
			}
			
			File result = new File(resultFileName);
			if(result.exists()){
				result.deleteOnExit();
			//	System.out.println(result.delete());
			}
			
		} catch (WebApplicationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
