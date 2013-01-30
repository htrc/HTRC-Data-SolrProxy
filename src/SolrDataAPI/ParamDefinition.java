package SolrDataAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ParamDefinition {

	public static  String BlackLight_IP = "192.168.1.12";
	public static  File logfile = new File("proxy_logs/logfile"); 
	//public static  String Proxy_Server_EPR = "http://chinkapin.pti.indiana.edu:9994"; // "http://localhost:8080";///*"http://coffeetree.cs.indiana.edu:9994";*/

	// public static final String Actual_Solr_EPR =
	// "http://chinkapin.pti.indiana.edu:9998" ;//"http://localhost:8888" ;
	// /*"http://coffeetree.cs.indiana.edu:9998";*/ //
	// "http://chinkapin.pti.indiana.edu:9998" ;
	public static  String Actual_Solr_EPR = ""; // shard1
																							// on
																							// chinkapin
																							// ---
																							// head
	public static  String LocalIndexPath = /*
												 * "/nfs/magnolia/home/user2/hathitrust/SolrWithTermVectorTest/SolrServiceNGDP/SOLRNGDPTest/example/solr/data/index"
												 * ;
												 */"/usr/local/htrc/solr/ToVM_Solr_related/apache-solr-NGDP/example/solr/data/index"; // "D:/apache-solr-3.1.0/example/solr/data/index";

	// public static final String MARC_Solr_EPR =
	// /*"http://smoketree.cs.indiana.edu:9999";*/
	// "http://chinkapin.pti.indiana.edu:9999";
	public static  String MARC_Solr_EPR = ""; // "http://chinkapin.pti.indiana.edu:9998";

	public static  String MARC_core_name = "marc";// "marc" core is for
														// chinkapin to host
														// MARC index;
	
	public static Properties properties = new Properties();

	static{
		
		try {
			if(!logfile.exists()){
				logfile.mkdirs();
				logfile.createNewFile();
			}
			
			properties.load(new FileInputStream(new File("../conf/solr-proxy.properties")));
		
			Actual_Solr_EPR = properties.getProperty("Actual_Solr_EPR");
			
			MARC_Solr_EPR = properties.getProperty("MARC_Solr_EPR");
			
			MARC_core_name = properties.getProperty("MARC_CORE_NAME");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
