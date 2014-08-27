HTRC-Data-SolrProxy
===================
this solr proxy is a layer above the real solrcloud that HTRC hosts. It only allows "/select" through to real solr and filter out all other requests like update. Audit logs are written by solr proxy to keep track of every single requests it received. It also allows users to download marc records for user-specified volume IDs. 
