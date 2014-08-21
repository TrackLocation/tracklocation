package com.dagrest.tracklocation.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;

public class HttpUtils {

	private static HttpPost httpPost;
	private static HttpClient httpClient;
	private static HttpEntity entity;
	private static HttpResponse httpResponse;
	
    public static String postGCM(String url, String serverKey, String messageJson){
    	
        int responseCode;
        String message;
        HttpPost req = new HttpPost(url);
        
        try {
			StringEntity se = new StringEntity(messageJson);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			req.setEntity(se);
			List<BasicHeader> headers = new ArrayList<BasicHeader>();
			headers.add(new BasicHeader("Accept", "application/json"));
			headers.add(new BasicHeader("Authorization", "key=" + serverKey));
			HttpResponse resp = HttpUtils
					.post(url, headers, se, null/*localContext*/);
			message = resp.getStatusLine().getReasonPhrase();
			responseCode = resp.getStatusLine().getStatusCode();
			if (responseCode != 200) {
				//throw new Exception("Cloud Exception" + message);
				return null;
			} else {
				String result = EntityUtils.toString(resp.getEntity(),
						HTTP.UTF_8);
				return result;
			}
		} catch (IOException e) {
			// TODO: handle exception
		}
        return null;
    }
    
    
//    public static String sendMessageToBackendAsync(final String jsonMessage) {
//        //LogManager.LogFunctionCall("HttpUtils", "sendMessageToBackendAsync");
//	    new AsyncTask<Void, Void, String>() {
//	        //@Override
//		    protected String doInBackground(Void... params) {
//		    	Log.i(CommonConst.LOG_TAG, "PostThreadId: " + Thread.currentThread().getId());
//		        String result = HttpUtils.postGCM("https://android.googleapis.com/gcm/send", 
//		            	"AIzaSyC2YburJfQ9h12eLEn7Ar1XPK_2deytF30", jsonMessage);
//		        Log.w(CommonConst.LOG_TAG, "HttpUtils.postGCM [sendMessageToBackendAsync] Result: " + result);
//		        Log.w(CommonConst.LOG_TAG, "HttpUtils.postGCM [sendMessageToBackendAsync] CMD: " + jsonMessage);
////		            Log.w(CommonConst.LOG_TAG, "CMD: " + jsonMessage);
////		            Log.w(CommonConst.LOG_TAG, "Res: " + result);
//		            if(result != null && !result.isEmpty() && result.contains("error")){
//		            	LogManager.LogErrorMsg("HttpUtils", "sendMessageToBackend", result);
//		            	Log.e(CommonConst.LOG_TAG, result);
//		            	// TODO: Broadcast clear error 
//		            }
//
//				return result;
//		    }
//	        @Override
//	        protected void onPostExecute(String result) {
//	        	// TODO: fix return value
//	        }
//	    }.execute(null, null, null);
//		return jsonMessage;
//    }

    public static String sendMessageToBackend(String jsonMessage) {
        LogManager.LogFunctionCall("HttpUtils", "sendMessageToBackend");
//        //PostToGCM.post(apiKey, content);
//        new Date().toString();
//        String messageJSON = "{\"registration_ids\" : "
//        	+ "[\"" + regid + "\"],"+
//        	"\"data\" : {\"message\": \"From David\",\"time\": \"" + new Date().toString() + "\"},}";
        
        String result = HttpUtils.postGCM("https://android.googleapis.com/gcm/send", 
        	"AIzaSyC2YburJfQ9h12eLEn7Ar1XPK_2deytF30", jsonMessage);
//        Log.w(CommonConst.LOG_TAG, "HttpUtils.postGCM [sendMessageToBackend] Result: " + result);
//        Log.w(CommonConst.LOG_TAG, "HttpUtils.postGCM [sendMessageToBackend] CMD: " + jsonMessage);
//        Log.w(CommonConst.LOG_TAG, "Res: " + result);
        if(result != null && !result.isEmpty() && result.contains("error")){
        	LogManager.LogErrorMsg("HttpUtils", "sendMessageToBackend", result);
        	Log.e(CommonConst.LOG_TAG, result);
        	// TODO: Broadcast clear error 
        }
        
        LogManager.LogFunctionExit("HttpUtils", "sendMessageToBackend");
        return result;
    }

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
