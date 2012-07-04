package SolrDataAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
//

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

@Path("/getfreqoffset/{volume_id}/{starting_letter}")
public class TermVectorFilter {
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayPlainTextHello(@PathParam("volume_id") String volume_id, @PathParam("starting_letter") String starting_letter) throws IOException
	{
		
		/*File xx = new File("Z:/zongpeng/final.txt");
		
		String s = null;
		
		String return_string = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(xx)));
		
		while((s=br.readLine()) != null)
		{
			return_string += s;
			System.out.println(s);
		}*/
	
		String result = getFreqOffset(volume_id, starting_letter);
		return result;
	}
	
	
	public String getFreqOffset(String volID, String starting_letter) throws IOException
	{

   	 
    	//String volID =  getFreqOffset.getVolID();
    	//String starting_letter = getFreqOffset.getStarting_letter();
    	
    	
		/////////////////

		//File index_file = new File("D:/apache-solr-3.1.0/example/solr/data/index"); // local test
    	File index_file = new File("/nfs/magnolia/home/user2/hathitrust/SolrWithTermVectorTest/SolrServiceV2/apache-solr-9992/example/solr/data/index");// for coffeetree
    	
		FSDirectory index_dir = FSDirectory.open(index_file);
		
		IndexReader index_reader = IndexReader.open(index_dir);
		
		Collection list = index_reader.getFieldNames(IndexReader.FieldOption.INDEXED_WITH_TERMVECTOR);
		
		Iterator iter = list.iterator();
		
		while(iter.hasNext())
		{
			System.out.println(iter.next());
		}
		
		int doc_numbers = index_reader.maxDoc();
		 
		System.out.println(doc_numbers);
		
		TermQuery termquery = new TermQuery(new Term("id", volID));
		IndexSearcher index_searcher = new IndexSearcher(index_reader);
		
		//TopScoreDocCollector collector =  TopScoreDocCollector.create(1, false);
		TopDocs hits  = index_searcher.search(termquery, index_reader.maxDoc());
		
		System.out.println(hits.totalHits);
		
		ScoreDoc[] docs = hits.scoreDocs;
		
		System.out.println(docs.length);
		String return_string = null; 
		
		Document document = DocumentHelper.createDocument();
		
		Element rootelement = document.addElement("lst");
		
		rootelement.addAttribute("name", "termVectors");
		
		
		
		for(int i=0; i<docs.length; i++)
		{
			int doc_id = docs[i].doc;
			
			TermPositionVector vector = (TermPositionVector)index_reader.getTermFreqVector(doc_id, "ocr");
			
			System.out.println("-------------" + "" + doc_id) ;
			Element docid = rootelement.addElement("lst");
			docid.addAttribute("name", "doc-"+doc_id);
			
			Element volumeid = docid.addElement("str");
			volumeid.addAttribute("name", "uniqueKey");
			volumeid.setText(volID);
			
			Element ocr = docid.addElement("lst");
			
			ocr.addAttribute("name", "ocr");
			
			
			
			String[] terms = vector.getTerms();
			int[] frequencies = vector.getTermFrequencies();
			
			if(terms.length == frequencies.length)
			{
				System.out.println(terms.length + " words!!!");
			}
			
			for(int j = 0; j<terms.length; j++)
			{
				if(terms[j].startsWith(starting_letter))
				{
					
					Element element_term = ocr.addElement("lst");
					element_term.addAttribute("name", terms[j]);
					Element element_freq = element_term.addElement("int");
					element_freq.addAttribute("name", "tf");
					element_freq.setText(frequencies[j]+"");
									
					
					return_string =  terms[j]+"#"+frequencies[j]+"#"+ vector.getOffsets(j);
					
					TermVectorOffsetInfo[] TermVectorOffsetInfo_array = vector.getOffsets(j);
					
					System.out.println(return_string);
					Element offsets_element = element_term.addElement("lst");
					offsets_element.addAttribute("name", "offsets");
					for(int k = 0; k<TermVectorOffsetInfo_array.length; k++)
					{
						int startoffset = TermVectorOffsetInfo_array[k].getStartOffset();
						int endoffset = TermVectorOffsetInfo_array[k].getEndOffset();
						Element offset_element_start = offsets_element.addElement("int");
						offset_element_start.addAttribute("name", "start");
						offset_element_start.setText(startoffset+"");
						
						Element offset_element_end = offsets_element.addElement("int");
						offset_element_end.addAttribute("name", "end");
						offset_element_end.setText(endoffset+"");
						
					}
				}
			}
			
		}
		
		return_string = document.asXML();
		
		//System.out.println(return_string);
		
		//GetFreqOffsetResponse response = new GetFreqOffsetResponse();
		//response.set_return(return_string);
		
		return return_string;
		           	
    	/////////////////
     
	}

}
