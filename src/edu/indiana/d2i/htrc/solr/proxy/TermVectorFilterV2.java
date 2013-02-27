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
# File:  TermVectorFilterV2.java
# Description: Get partial termvector for words with user specified prefix 
# and volume ID. Not enabled now because we temporarily removed termvector.
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
//

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

//@Path("/hello")

@Path("/TermVector/")
public class TermVectorFilterV2 {
	private static final Logger logger = Logger
			.getLogger(TermVectorFilterV2.class.getName());

	@GET
	@Produces(MediaType.TEXT_XML)
	public String getTermVector(@QueryParam(value = "prefix") String prefix,
			@QueryParam(value = "offset") boolean offset,
			@QueryParam(value = "ngram") boolean ngram,
			@QueryParam(value = "stopwords") boolean stopwords,
			@QueryParam(value = "volumeID") String volume_id,
			@Context UriInfo ui, @Context final HttpServletRequest hsr)
			throws IOException {

		final URI uri = ui.getRequestUri();
			
		List<String> stopping_list = new LinkedList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File("../conf/stopwords.txt"))));
		String line = null;
		while ((line = br.readLine()) != null) {
			stopping_list.add(line);
		}

		br.close();

		System.out.println("volume_id: " + volume_id);
		System.out.println("prefix: " + prefix);
		System.out.println("offset: " + offset);
		System.out.println("ngram: " + ngram);

		if (prefix == null) {
			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "failed";
			
			logger.debug(log_content);
			throw new IOException("the prefix must be specified!!!");
		}

		if (prefix.equals("*")) {
			prefix = "";
		}

		String result = getFreqOffset(volume_id, prefix, offset, ngram,
				stopwords, stopping_list);
		Date today = new Date();
		String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
				+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
				+ "	" + "allowed";
		
		logger.debug(log_content);
		return result;
	}

	public String getFreqOffset(String volID, String starting_letter,
			boolean offset, boolean ngram, boolean stopwords,
			List<String> stopping_list) throws IOException {
		
		String return_string = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("../conf/shard_locations.txt")));
		String index_path = null;
		while ((index_path = br.readLine()) != null) {
			long t0 = System.currentTimeMillis();
			System.out.println(index_path);// /////////
			File index_file = new File(index_path);

			FSDirectory index_dir = FSDirectory.open(index_file);

			IndexReader index_reader = IndexReader.open(index_dir);

			TermQuery termquery = new TermQuery(new Term("id", volID));
			IndexSearcher index_searcher = new IndexSearcher(index_reader);

			
			TopDocs hits = index_searcher.search(termquery, 1);

			System.out.println(hits.totalHits);
			if (hits.totalHits < 1) {
				System.out.println("continue, " + index_path);
				index_searcher.close();
				index_reader.close();
				long t1 = System.currentTimeMillis();
				System.out.println("not here and takes " + (t1 - t0)
						+ " milliseconds!!");
				continue;
			}

			ScoreDoc[] docs = hits.scoreDocs;

			System.out.println(docs.length);

			Document document = DocumentHelper.createDocument();

			Element rootelement = document.addElement("lst");

			rootelement.addAttribute("name", "termVectors");

			for (int i = 0; i < docs.length; i++) {
				int doc_id = docs[i].doc;

				TermPositionVector vector = (TermPositionVector) index_reader
						.getTermFreqVector(doc_id, "ocr");

				System.out.println("-------------" + "" + doc_id);
				Element docid = rootelement.addElement("lst");
				docid.addAttribute("name", "doc-" + doc_id);

				Element volumeid = docid.addElement("str");
				volumeid.addAttribute("name", "uniqueKey");
				volumeid.setText(volID);

				Element ocr = docid.addElement("lst");

				ocr.addAttribute("name", "ocr");

				String[] terms = vector.getTerms();
				int[] frequencies = vector.getTermFrequencies();

				if (terms.length == frequencies.length) {
					System.out.println(terms.length + " words!!!");
				}

				for (int j = 0; j < terms.length; j++) {
					if (terms[j].startsWith(starting_letter)) {

						if (ngram == false && terms[j].contains("_")) {
							continue;
						}
						if (stopwords == false
								&& stopping_list.contains(terms[j])) {
							continue;
						}

						Element element_term = ocr.addElement("lst");
						element_term.addAttribute("name", terms[j]);
						Element element_freq = element_term.addElement("int");
						element_freq.addAttribute("name", "tf");
						element_freq.setText(frequencies[j] + "");

						return_string = terms[j] + "#" + frequencies[j] + "#"
								+ vector.getOffsets(j);

						if (offset == true) {
							TermVectorOffsetInfo[] TermVectorOffsetInfo_array = vector
									.getOffsets(j);

							System.out.println(return_string);
							Element offsets_element = element_term
									.addElement("lst");
							offsets_element.addAttribute("name", "offsets");
							for (int k = 0; k < TermVectorOffsetInfo_array.length; k++) {
								int startoffset = TermVectorOffsetInfo_array[k]
										.getStartOffset();
								int endoffset = TermVectorOffsetInfo_array[k]
										.getEndOffset();
								Element offset_element_start = offsets_element
										.addElement("int");
								offset_element_start.addAttribute("name",
										"start");
								offset_element_start.setText(startoffset + "");

								Element offset_element_end = offsets_element
										.addElement("int");
								offset_element_end.addAttribute("name", "end");
								offset_element_end.setText(endoffset + "");

							}

						}
					}
				}
			}

			return_string = document.asXML();
			index_searcher.close();
			index_reader.close();
			br.close();

			long t1 = System.currentTimeMillis();
			System.out.println("found here and takes " + (t1 - t0)
					+ " milliseconds!!");

			return return_string;
		}

		return "No volumes found!";

	}

}
