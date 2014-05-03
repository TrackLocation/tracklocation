package com.dagrest.tracklocation;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.dagrest.tracklocation.log.LogManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PostToGCM {
    public static void post(String apiKey, Content content){

        LogManager.LogInfoMsg("PostToGCM", "post", 
               	"Started");
        try{

        // 1. URL
        URL url = new URL("https://android.googleapis.com/gcm/send");

        // 2. Open connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 3. Specify POST method
        conn.setRequestMethod("POST");

        // 4. Set the headers
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "key="+apiKey);

        conn.setDoOutput(true);

            // 5. Add JSON data into POST request body

            //`5.1 Use Jackson object mapper to convert Contnet object into JSON
            ObjectMapper mapper = new ObjectMapper();
            LogManager.LogInfoMsg("PostToGCM", "post", 
                   	"After 5.1");

            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            
            // 5.2 Get connection output stream
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            LogManager.LogInfoMsg("PostToGCM", "post", 
                   	"After 5.2");

            // 5.3 Copy Content "JSON" into
            //mapper.writeValue(wr, content);
            mapper.writeValue(wr, content);

            // 5.4 Send the request
            wr.flush();

            // 5.5 close
            wr.close();

            // 6. Get the response
            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 7. Print result
            //System.out.println(response.toString());
            LogManager.LogInfoMsg("PostToGCM", "post", 
                   	"Finished: " + response.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                LogManager.LogErrorMsg("PostToGCM", "MalformedURLException", 
                   	"Error: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                LogManager.LogErrorMsg("PostToGCM", "IOException", 
                		"Error: " + e.getMessage());
            }
        }
}
