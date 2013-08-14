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
# File:  OcrQueryHandler.java
# Description: TODO
#
# -----------------------------------------------------------------
# 
*/
package edu.indiana.d2i.htrc.solr.proxy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.solr.output.CommonQueryStreamingOutput;
import edu.indiana.d2i.htrc.solr.output.ErrorStreamingOutput;


@Path("/ocr/select")
public class OcrQueryHandler {
	@Context ServletConfig servletConfig;
	
	private static final Logger logger = Logger
			.getLogger(OcrQueryHandler.class.getName());

	/**
	 * get query string from client, forward it to real solr instance and stream the result 
	 * returned from solr instance back to client. 
	 * 
	 * @param query_string recieved query string from client side.
	 * @param ui UriInfo object got from Context in jersey framework.
	 * @param hsr HttpServletRequest object got from Context in jersey framework.
	 * @return a StreamingOutput object that streams content returned from solr instance back to client.
	 * @throws IOException
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public StreamingOutput getOcrQueryResult(
			@Context UriInfo ui, @Context final HttpServletRequest hsr)
			{
		
		final URI uri = ui.getRequestUri();
		
		UriBuilder uriBuilder = ui.getRequestUriBuilder();
		
		Set<String> queryParamKeySet = ui.getQueryParameters().keySet();
		
	//	System.out.println(queryParamKeySet);
		ParamDefinition ParamDef = new ParamDefinition(servletConfig);
		
		
		
		URI newURI = null;
		if(queryParamKeySet.contains("qt")){
			newURI = uriBuilder.host(ParamDef.getConfig()
					.getProperty("solr.ocr.headshard.host"))
					.port(Integer.valueOf(ParamDef.getConfig()
							.getProperty("solr.ocr.headshard.port")))
					.replacePath("solr/select").build();
		}else{
			newURI = uriBuilder.host(ParamDef.getConfig()
					.getProperty("solr.ocr.headshard.host"))
					.port(Integer.valueOf(ParamDef.getConfig()
							.getProperty("solr.ocr.headshard.port")))
							.replacePath("solr/select")
							.queryParam("qt", "sharding").build();  // add qt=sharding so that the distributed search will be used
		}
		
		System.out.println("new OCR uri: " + newURI);
		
		URL newURL = null;
		try {
			newURL = newURI.toURL();
		} catch (MalformedURLException e) {
			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "failed";
			
			logger.info(log_content);
			e.printStackTrace();
			
			return new ErrorStreamingOutput("Malformed REST CALL!!");
		}
		
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) newURL.openConnection();
		} catch (IOException e) {
			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "failed";
			e.printStackTrace();
			return new ErrorStreamingOutput("COULD NOT OPEN COLLECTION to " + newURL);
		}

		try {
			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				Date today = new Date();
				String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
						+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
						+ "	" + "failed";
				
				logger.info(log_content);
				
				return new ErrorStreamingOutput("RESPONSE CODE: " + responseCode);
			//	throw new IOException(conn.getResponseMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
			
			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "failed";
			
			logger.info(log_content);
			
			return new ErrorStreamingOutput("ERROR OCCURED CONNECTING TO SOLR  SERVER!!");
		}
		
		StreamingOutput returnStream = new CommonQueryStreamingOutput(conn);
		
		Date today = new Date();

		String log_content = "\n" + hsr.getHeader("x-forwarded-for")
				+ "	" + hsr.getRemoteAddr() + "	" + today.toString()
				+ "	" + uri + "	" + "allowed";
		
		logger.info(log_content);
		
		return returnStream;		
/*
		if (!uri.toString().split("\\?")[0].endsWith("/select/")
				&& !uri.toString().split("\\?")[0].endsWith("/select")) {

			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "blocked";
			
			logger.info(log_content);
			throw new IOException("Modification is not allowed!!!");
		}

		ParamDefinition ParamDef = new ParamDefinition(servletConfig);
		String solr_endpoint = ParamDef.getConfig().getProperty("solr.shards.head.epr")
				+ "/solr/select/?" + uri.getRawQuery();

		if (hsr.getParameter("qt") == null) {
			solr_endpoint = solr_endpoint + "&qt=sharding"; // make qt=sharding as default query type
		}

		URL url = new URL(solr_endpoint);
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			Date today = new Date();
			String log_content = "\n" + hsr.getHeader("x-forwarded-for") + "	"
					+ hsr.getRemoteAddr() + "	" + today.toString() + "	" + uri
					+ "	" + "failed";
			
			logger.info(log_content);
			throw new IOException(conn.getResponseMessage());
		}
		
		StreamingOutput returnStream = new CommonQueryStreamingOutput(conn);
		
		Date today = new Date();

		String log_content = "\n" + hsr.getHeader("x-forwarded-for")
				+ "	" + hsr.getRemoteAddr() + "	" + today.toString()
				+ "	" + uri + "	" + "allowed";
		
		logger.info(log_content);
		return returnStream;*/
	}
}
