package SolrDataAPI;

import java.io.File;

public class ParamDefinition {

	public static final String BlackLight_IP = "192.168.1.12";

	public static final String Proxy_Server_EPR = /*"http://coffeetree.cs.indiana.edu:9994";*/  "http://chinkapin.pti.indiana.edu:9994";      //"http://localhost:8080";// 

	public static final String Actual_Solr_EPR = /*"http://coffeetree.cs.indiana.edu:9998";*/  "http://chinkapin.pti.indiana.edu:9998" ;

	public static final String LocalIndexPath = /*"/nfs/magnolia/home/user2/hathitrust/SolrWithTermVectorTest/SolrServiceNGDP/SOLRNGDPTest/example/solr/data/index";*/ "/usr/local/htrc/solr/ToVM_Solr_related/apache-solr-NGDP/example/solr/data/index";    //"D:/apache-solr-3.1.0/example/solr/data/index";

	//public static final String MARC_Solr_EPR = /*"http://smoketree.cs.indiana.edu:9999";*/ "http://chinkapin.pti.indiana.edu:9999";
	public static final String MARC_Solr_EPR = "http://chinkapin.pti.indiana.edu:9998";
	
	public static final String MARC_core_name = "marc" ;// "marc" core is for chinkapin to host MARC index;
	
	public static final File logfile = new File("logfile");
}
