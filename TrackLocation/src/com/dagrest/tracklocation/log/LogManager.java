package com.dagrest.tracklocation.log;

public class LogManager {
    	public static void LogFunctionCall(String className, String methodName)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.FUNCTION_CALL, className + " -> " + methodName + " called.");          
    	}                    
    	public static void LogFunctionExit(String className, String methodName)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.FUNCTION_EXIT, className + " -> " + methodName + " ended.\n-------------------");          
    	}                    
    	public static void LogException(Exception exception, String className, String methodName)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.EXCEPTION, className + " -> " + methodName + " : " + 
    		exception.getMessage()+ " : " + exception.getStackTrace());          
    	}                    
    	public static void LogInfoMsg(String className, String methodName, String infoMessage)          
    	{                  
    		LogHelper.getLog().toLog(MessageType.INFO, className + " -> " + methodName + " : " + infoMessage);          
    	}                    
    	public static void LogErrorMsg(String className, String methodName, String errorMsg)          
    	{                 
    		LogHelper.getLog().toLog(MessageType.ERROR, className + " -> " + methodName + " : " + errorMsg);         
    	}  
}
