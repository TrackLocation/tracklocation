package com.doat.tracklocation.log;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.doat.tracklocation.utils.CommonConst;

public class LogManager {
    	public static void LogFunctionCall(String className, String methodName)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.FUNCTION_ENTRY, className + "\n{" + methodName + "}\n");          
    	}                    
    	public static void LogFunctionExit(String className, String methodName)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.FUNCTION_EXIT, className + "\n{" + methodName + "}\n\n");          
    	}                    
    	public static void LogException(Exception exception, String className, String methodName)          
    	{               
    		StringWriter stackTrace = new StringWriter();
    		exception.printStackTrace(new PrintWriter(stackTrace));
    		String stackTraceStr = "";
    		if(CommonConst.STACK_TRACE.equals("TRUE")){
    			stackTraceStr = stackTrace.toString();
    		}
    		LogHelper.getLog().toLog(MessageType.EXCEPTION, className + "\n{" + methodName + "}\n" + 
    		exception.getMessage()+ ":\n" + stackTraceStr + "\n");          
    	}                    
    	public static void LogInfoMsg(String className, String methodName, String infoMessage)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.INFO, className + "\n{" + methodName + "}\n" + infoMessage + "\n");          
    	}                    
    	public static void LogErrorMsg(String className, String methodName, String errorMsg)          
    	{                 
    		LogHelper.getLog().toLog(MessageType.ERROR, className + "\n{" + methodName + "}\n" + errorMsg + "\n");         
    	}  
    	public static void LogActivityCreate(String className, String methodName)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.ACTIVITY_CREATE, className + "\n{" + methodName + "}\n");          
    	}                    
    	public static void LogActivityDestroy(String className, String methodName)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.ACTIVITY_DESTROY, className + "\n{" + methodName + "}");          
    	}                    
}
