package com.dagrest.tracklocation.http;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

public class HttpUtils {

	private static HttpPost httpPost;
	private static HttpClient httpClient;
	private static HttpEntity entity;
	private static HttpResponse httpResponse;
	
	public static HttpResponse post(String url,List<BasicHeader> headers,HttpEntity httpEntity,HttpContext localContext ) 
		throws ClientProtocolException, IOException
	{
		httpPost = new HttpPost(url);
		httpClient = new DefaultHttpClient();
//		httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, new HttpHost("default-proxy", 8080));
		entity = httpEntity;
		if (headers != null) 
		{
			for (BasicHeader basicHeader : headers) 
			{
				httpPost.setHeader(basicHeader);
			}
		}

		if (entity != null) 
		{
			httpPost.setEntity(httpEntity);
		}
		if (localContext == null) 
		{
			httpResponse = httpClient.execute(httpPost);
		} else 
		{
			httpResponse = httpClient.execute(httpPost,localContext);
		}

		return httpResponse;
	}

	
}
