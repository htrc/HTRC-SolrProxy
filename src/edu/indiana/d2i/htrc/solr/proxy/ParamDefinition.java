package edu.indiana.d2i.htrc.solr.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import com.sun.jersey.spi.container.servlet.WebConfig;

public class ParamDefinition extends Application{
	
	ServletConfig servletConfig = null;
	
	ParamDefinition(ServletConfig servletConfig){
		this.servletConfig = servletConfig;
	}
	
	private static ParamDefinitionContainer config = null; //this is a singleton
		
	public ParamDefinitionContainer getConfig() {
		
		System.out.println("servletConfig == null is " + (servletConfig == null));
		
		System.out.println(servletConfig.getInitParameterNames());
		
		if(config == null){
			config = new ParamDefinitionContainer();
			
			config.properties = new Properties();
			
			config.Actual_Solr_EPR = this.servletConfig.getInitParameter("solr.shards.head.epr");
			config.properties.put("solr.shards.head.epr", config.Actual_Solr_EPR);
			
			config.MARC_Solr_EPR = this.servletConfig.getInitParameter("solr.marc.epr");
			config.properties.put("solr.marc.epr", config.MARC_Solr_EPR);
			
			config.MARC_core_name = this.servletConfig.getInitParameter("solr.marc.core.name");
			config.properties.put("solr.marc.core.name", config.MARC_core_name);
			
			return config;
			
		}else{
			return config;
		}
		
		
	}	
	
	class ParamDefinitionContainer{
		 String BlackLight_IP = "192.168.1.12";
		// File logfile = new File("proxy_logs/logfile"); 
		//public static  String Proxy_Server_EPR = "http://chinkapin.pti.indiana.edu:9994"; // "http://localhost:8080";///*"http://coffeetree.cs.indiana.edu:9994";*/
		// public static final String Actual_Solr_EPR =
		// "http://chinkapin.pti.indiana.edu:9998" ;//"http://localhost:8888" ;
		// /*"http://coffeetree.cs.indiana.edu:9998";*/ //
		// "http://chinkapin.pti.indiana.edu:9998" ;
		 String Actual_Solr_EPR = ""; // shard1
																								// on
																								// chinkapin
																								// ---
																							// head
		 String LocalIndexPath = null;/*
													 * "/nfs/magnolia/home/user2/hathitrust/SolrWithTermVectorTest/SolrServiceNGDP/SOLRNGDPTest/example/solr/data/index"
													 * ;
													 *///"/usr/local/htrc/solr/ToVM_Solr_related/apache-solr-NGDP/example/solr/data/index"; // "D:/apache-solr-3.1.0/example/solr/data/index";

		 String MARC_Solr_EPR = ""; // "http://chinkapin.pti.indiana.edu:9998";

		 String MARC_core_name = "marc";// "marc" core is for
															// chinkapin to host
															// MARC index;
		Properties properties = null; //new Properties();
		
		public String getProperty(String name){
		
			return properties.getProperty(name);
		}
	}
}