package com.dagrest.tracklocation;

import java.util.List;

import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.Message;
import com.dagrest.tracklocation.datatype.MessageData;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.datatype.TrackLocationServiceStatusEnum;
import com.google.gson.Gson;

public class Controller {

    public String createJsonMessage(List<String> listRegIDs, 
    		String regIDToReturnMessageTo, 
    		CommandEnum command, 
    		String messageString, 
    		String time,
    		TrackLocationServiceStatusEnum trackLocationServiceStatus,
    		PushNotificationServiceStatusEnum pushNotificationServiceStatus){
    	
    	String jsonMessage = null;
    	
        Gson gson = new Gson();
    	
        MessageData messageData = new MessageData();
        messageData.setMessage(messageString);
        messageData.setTime(time);
        messageData.setCommand(command);
        messageData.setRegIDToReturnMessageTo(regIDToReturnMessageTo);
        messageData.setTrackLocationServiceStatusEnum(trackLocationServiceStatus);
        messageData.setPushNotificationServiceStatusEnum(pushNotificationServiceStatus);
        
        Message message = new Message();
        message.setData(messageData); 
        message.setRegistrationIDs(listRegIDs);

        jsonMessage = gson.toJson(message);
    	
    	return jsonMessage;
    }
    

}
