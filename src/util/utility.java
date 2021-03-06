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
# File:  utility.java
# Description: some utility codes like zipping a dir programatically, 
# deleting a file/directory and escape ":" to avoid confusion to Solr query parser
# -----------------------------------------------------------------
# 
*/
package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class utility {
	/**
	 * escape ":" in volume ID to avoid violation of query parser syntax
	 * 
	 * @param uncleanID unclean volume ID
	 * @return escaped unclean volume ID
	 */
	public static String escape(String uncleanID) { // escape ":" because the
													// query
		// follows pattern
		// "field: term". Extra ":" in
		// term part can cause parsing
		// problem. So escape it by
		// adding "\"

		if (uncleanID.contains(":")) {
			return uncleanID.replace(":", "\\:");
		}

		return uncleanID;
	}

	
	
	/**
	 * delete a file or directory
	 * 
	 * @param file a file or a directory
	 * @throws IOException
	 */
	public static void delete(File file) throws IOException {

		if (file.isDirectory()) {

			// directory is empty, then delete it
			if (file.list().length == 0) {

				file.delete();

			} else {

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
					
				} else
					delete(file);
			}

		} else {
			// if file, then delete it
			file.delete();
		}
	}

	/**
	 * compress a directory recursively into a zip file
	 * 
	 * @param dir directory to compress
	 * @param zipName_no_ext name of result zip file without ".zip" extension
	 * @return file name of the result zip file with ".zip" extension
	 * @throws FileNotFoundException
	 */
	public static String zipDir(File dir, String zipName_no_ext)
			throws FileNotFoundException {
		int BUFFER = 2048;
		String outputname = zipName_no_ext + ".zip";
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(outputname);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			// out.setMethod(ZipOutputStream.DEFLATED);
			byte data[] = new byte[BUFFER];
			// get a list of files from current directory

			File files[] = dir.listFiles();

			for (int i = 0; i < files.length; i++) {
				FileInputStream fi = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(files[i].getName());
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				fi.close();
				origin.close();
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputname;
	}

	/**
	 * uncompress a zip file
	 * 
	 * @param zipFile the file name of the zip file to uncompress
	 * @return relative path of the uncompressed result
	 * @throws ZipException
	 * @throws IOException
	 */
	static public String unzip(String zipFile) throws ZipException, IOException {
		// System.out.println(zipFile);
		int BUFFER = 2048;
		File file = new File(zipFile);

		ZipFile zip = new ZipFile(file);
		String newPath = zipFile.substring(0, zipFile.length() - 4);

		new File(newPath).mkdir();
		Enumeration zipFileEntries = zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
			// grab a zip file entry
			ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
			String currentEntry = entry.getName();
			File destFile = new File(newPath, currentEntry);
			// destFile = new File(newPath, destFile.getName());
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			if (!entry.isDirectory()) {
				BufferedInputStream is = new BufferedInputStream(
						zip.getInputStream(entry));
				int currentByte;
				// establish buffer for writing file
				byte data[] = new byte[BUFFER];

				// write the current file to disk
				FileOutputStream fos = new FileOutputStream(destFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos,
						BUFFER);

				// read and write until last byte is encountered
				while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();
			}

			if (currentEntry.endsWith(".zip")) {
				// found a zip file, try to open
				unzip(destFile.getAbsolutePath());
			}
		}
		return newPath;
	}
}
