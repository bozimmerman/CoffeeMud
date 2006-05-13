package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.*;
import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Log
{
    private Log(){super();}
    private static Log inst=new Log();
    public static Log instance(){return inst;}
    
	/**	reroutable error channel */
	private static PrintWriter errOutWriter=null;
	/**	reroutable debug channel */
	private static PrintWriter debugOutWriter=null;
	/**	reroutable logging channel */
	private static PrintWriter systemOutWriter=null;
	/**	always to "webiq.log" */
	private static PrintWriter fileOutWriter=null;
	/**	system messages status */
	private static String sysMsgs="ON";
	/**	debug messages status */
	private static String dbgMsgs="OFF";
	/**	error messages status */
	private static String errMsgs="ON";
	/** SPACES for headers */
	private static final String SPACES="                                                                                               ";
	/** The fully qualified file path */
	private static String filePath = "";
	
	private final static String[] maskErrMsgs={
    	"broken pipe",
		"reset by peer",
		"socket closed",
		"connection abort",
		"connection reset",
		"network is unreachable",
		"jvm_recv",
		"timed out",
		"stream closed",
        "no route to host",
		"protocol not available"
	};
	
	public static boolean isMaskedErrMsg(String str)
	{
		if(str==null) return false;
		str=str.toLowerCase();
		for(int i=0;i<maskErrMsgs.length;i++)
			if(str.indexOf(maskErrMsgs[i])>=0)
				return true;
		return false;
	}

	/**
	* Reset all of the log files
	* ON, OFF, FILE, BOTH
	* <br><br><b>Usage:</b>  CMProps.Initialize("ON","OFF");
	* @param newSYSMSGS code string to describe info msgs
	* @param newERRMSGS code string to describe error msgs
	* @param newDBGMSGS code string to describe debug msgs
	*/
	public static void Initialize(String newSYSMSGS, 
								  String newERRMSGS, 
								  String newDBGMSGS)
	{
		sysMsgs=newSYSMSGS;
		errMsgs=newERRMSGS;
		dbgMsgs=newDBGMSGS;
		
		if(sysMsgs.equalsIgnoreCase("ON"))
			systemOutWriter=new PrintWriter(System.out,true);
		else
		if(sysMsgs.equalsIgnoreCase("OFF"))
			systemOutWriter=null;
		else
		if((sysMsgs.equalsIgnoreCase("FILE"))||(sysMsgs.equalsIgnoreCase("BOTH")))
			systemOutWriter=fileOutWriter;

		if(errMsgs.equalsIgnoreCase("ON"))
			errOutWriter=new PrintWriter(System.out,true);
		else
		if(errMsgs.equalsIgnoreCase("OFF"))
			errOutWriter=null;
		else
		if((errMsgs.equalsIgnoreCase("FILE"))||(errMsgs.equalsIgnoreCase("BOTH")))
			errOutWriter=fileOutWriter;
		
		if(dbgMsgs.equalsIgnoreCase("ON"))
			debugOutWriter=new PrintWriter(System.out,true);
		else
		if(dbgMsgs.equalsIgnoreCase("OFF"))
			debugOutWriter=null;
		else
		if((dbgMsgs.equalsIgnoreCase("FILE"))||(dbgMsgs.equalsIgnoreCase("BOTH")))
			debugOutWriter=fileOutWriter;
	}
	
	/**
	* Start all of the log files in the system temp directory
	* 
	* <br><br><b>Usage:</b>  startLogFiles(5);
	* @param numberOfLogs maximum number of files
	*/
	public static void startLogFiles(int numberOfLogs)
	{
		// ===== pass in a null to force the temp directory
		startLogFiles(null,numberOfLogs);
	}
	
	/**
	* Start all of the log files in the specified directory
	* 
	* <br><br><b>Usage:</b>  startLogFiles("c:\\temp");
	* @param dirPath the place to create the file
	* @param numberOfLogs maximum number of files
	*/
	public static void startLogFiles(String dirPath, int numberOfLogs)
	{
		try
		{
			File directoryPath = null;

			if (dirPath!=null)
			{
				if (dirPath.length()!=0)
				{
					try
					{
						directoryPath = new File(dirPath);
						
						if ((!directoryPath.isDirectory())||(!directoryPath.canWrite())||(!directoryPath.canRead()))
						{
							directoryPath = null;	
						}					
					}
					catch(Throwable t)
					{
						directoryPath=null;	
					}
				}
			}
			
			// initializes the logging objects
			systemOutWriter=new PrintWriter(System.out,true);
			if(numberOfLogs>1)
			{
				try{ 
					File f=new File("mud"+(numberOfLogs-1)+".log"); 
					if(f.exists()) 
						f.delete();
				}catch(Exception e){}
				for(int i=numberOfLogs-1;i>0;i--)
				{
					String inum=(i>0)?(""+i):"";
					String inumm1=(i>1)?(""+(i-1)):"";
					try{ 
						File f=new File("mud"+inumm1+".log"); 
						if(f.exists()) 
							f.renameTo(new File("mud"+inum+".log"));
					}catch(Exception e){}
				}
			}
			File fileOut=new File("mud.log");
			filePath = fileOut.getAbsolutePath();
			FileOutputStream fileStream=new FileOutputStream(fileOut);
			fileOutWriter=new PrintWriter(fileStream,true);
			errOutWriter=systemOutWriter;
			System.setErr(new PrintStream(fileStream));
		}
		catch(IOException e)
		{
			System.out.println("NO OPEN LOG: "+e.toString());
		}
	}
	
	public static StringBuffer getLog()
	{
		
		StringBuffer buf=new StringBuffer("");
		try
		{
			FileReader F=new FileReader("mud.log");
			BufferedReader reader=new BufferedReader(F);
			String line="";
			while((line!=null)&&(reader.ready()))
			{
				line=reader.readLine();
				if(line!=null)
					buf.append(line+"\n\r");
			}
			F.close();
		}
		catch(Exception e)
		{
			Log.errOut("Log",e.getMessage());
		}
		return buf;
	}
	/**
	* Start all of the log files
	* 
	* <br><br><b>Usage:</b>  path = getLogLocation();
	* @return the string representation of the file path
	*/
	public static String getLogLocation()
	{
		return filePath;
	}	
	
	/**
	* Just send a string out to regular old System.out
 	* 
	* <br><br><b>Usage:</b> systemOut("output mesage");
	* @param Out String to print
	*/
	private static void systemOut(String Out)
	{
		System.out.println(Out);
		System.out.flush();
	}

	/**
	* Will be used to create a standardized log header for file logs
 	* 
	* <br><br><b>Usage:</b> SysOutWriter.println(getLogHeader(S,"Info",Module, Message));
	* @param Obj Session object
	* @param Type Type of information
	* @param Module The module name
	* @param Message The message to print
	* @return String The header and message, formatted
	*/
    private static String getLogHeader(Object Obj, String Type, String Module, String Message)
    {
        Calendar C=Calendar.getInstance();
        String MINUTE=Integer.toString(C.get(Calendar.MINUTE)).trim();
        if(MINUTE.length()==1) MINUTE="0"+MINUTE;
        String AMPM="AM";
        if(C.get(Calendar.AM_PM)==Calendar.PM) AMPM="PM";
        int Hour=C.get(Calendar.HOUR);
        if(Hour==0) Hour=12;
        String Year=Integer.toString(C.get(Calendar.YEAR));
        if(Year.length()<4)
        {
            if(Year.length()<2)
                Year=("0"+Year);
            if(Year.length()<2)
                Year=("0"+Year);
            int Yr=CMath.s_int(Year);
            if(Yr<50)Year="20"+Year;
            else Year="19"+Year;
        }
        String date=(C.get(Calendar.MONTH)+1)+"/"+C.get(Calendar.DATE)+"/"+Year+" "+Hour+":"+MINUTE+" "+AMPM;
        StringBuffer Header=new StringBuffer((date+SPACES).substring(0,20));
        Header.append((Type+SPACES).substring(0,6));
        Header.append((Module+SPACES).substring(0,13));
        Header.append(Message);
        return Header.toString();
    }


	/**
	* Handles standard logging entries.  Sends them to System.out,
	* the webiq.log file, or nowhere.
 	* 
	* <br><br><b>Usage:</b> sysOut(S,"WebIQ","REQ-OUT:"+REQ);
	* @param Obj Session object
	* @param Module The mofule name
	* @param Message The message to print
	*/
	public static void sysOut(Object Obj, String Module, String Message)
	{
		if(systemOutWriter!=null)
		{
			systemOutWriter.println(getLogHeader(Obj,"Info",Module, Message));
			systemOutWriter.flush();
			if(sysMsgs.equalsIgnoreCase("BOTH"))
				systemOut(getLogHeader(Obj,"Info",Module, Message));
		}
	}

	public static void rawSysOut(String Message)
	{
		if(systemOutWriter!=null)
		{
			systemOutWriter.println(Message);
			systemOutWriter.flush();
			if(sysMsgs.equalsIgnoreCase("BOTH"))
				systemOut(Message);
		}
	}
	
	/**
	* Handles error logging entries.  Sends them to System.out,
	* the webiq.log file, or nowhere.
 	* 
	* <br><br><b>Usage:</b> errOut(null,"UNKN",Out);
	* @param Obj Session object
	* @param Module The file name
	* @param Message The message to print
	*/
	public static void errOut(Object Obj, String Module, String Message)
	{
		if(errOutWriter!=null)
		{
			errOutWriter.println(getLogHeader(Obj,"Error",Module, Message));
			errOutWriter.flush();
			if(errMsgs.equalsIgnoreCase("BOTH"))
				systemOut(getLogHeader(Obj,"Error",Module, Message));
		}
	}
	
	/**
	* Handles error logging entries.  Sends them to System.out,
	* the webiq.log file, or nowhere.
 	* 
	* <br><br><b>Usage:</b> errOut(null,"UNKN",Out);
	* @param Module The module to print
	* @param e	The exception whose string one wishes to print
	*/
	public static void errOut(String Module, Throwable e)
	{
		if(errOutWriter!=null)
		{
		    if(e!=null)
		    {
				errOutWriter.println(getLogHeader(null,"Error",Module, e.getMessage()));
				e.printStackTrace(errOutWriter);
				errOutWriter.flush();
		    }
		    else
				errOutWriter.println(getLogHeader(null,"Error",Module,"Null/Unknown error occurred."));
			if(errMsgs.equalsIgnoreCase("BOTH"))
			{
			    if(e!=null)
			    {
					systemOut(getLogHeader(null,"Error",Module, e.getMessage()));
					e.printStackTrace(System.out);
					System.out.flush();
			    }
			    else
			        systemOut(getLogHeader(null,"Error",Module,"Null/Unknown error occurred."));
			}
		}
	}
	/**
	* Handles error logging entries.  Sends them to System.out,
	* the webiq.log file, or nowhere.
 	* 
	* <br><br><b>Usage:</b> debugOut(null,"UNKN",Out);
	* @param Module The message to print
	* @param e	The exception whose string one wishes to print
	*/
	public static void debugOut(String Module, Exception e)
	{
		if(debugOutWriter!=null)
		{
			debugOutWriter.println(getLogHeader(null,"Debug",Module, e.getMessage()));
			e.printStackTrace(debugOutWriter);			
			debugOutWriter.flush();
			if(dbgMsgs.equalsIgnoreCase("BOTH"))
			{
				systemOut(getLogHeader(null,"Debug",Module, e.getMessage()));
				e.printStackTrace(System.out);
				System.out.flush();
			}
		}
	}
	
	/**
	* Handles debug logging entries.  Sends them to System.out,
	* the webiq.log file, or nowhere.
 	* 
	* <br><br><b>Usage:</b> debugOut(null,"UNKN",Out);
	* @param Obj Session object
	* @param Module The file name
	* @param Message The message to print
	*/
	public static void debugOut(Object Obj, String Module, String Message)
	{
		if(debugOutWriter!=null)
		{
			debugOutWriter.println(getLogHeader(Obj,"Debug",Module, Message));
			debugOutWriter.flush();
			if(dbgMsgs.equalsIgnoreCase("BOTH"))
				systemOut(getLogHeader(Obj,"Debug",Module, Message));
		}
	}
	
	
	/**
	* Handles debug timing entries.  Sends them to System.out,
	* the webiq.log file, or nowhere.
 	* 
	* <br><br><b>Usage:</b> timeOut("UNKN",Out);
	* @param Obj Session object
	* @param Module The file name
	* @param Message The message to print
	*/
	public static void timeOut(Object Obj, String Module, String Message)
	{
		if(debugOutWriter!=null)
		{
			Calendar C=Calendar.getInstance();
			Message=C.get(Calendar.MINUTE)+":"+C.get(Calendar.SECOND)+":"+C.get(Calendar.MILLISECOND)+": "+Message;
			debugOutWriter.println(getLogHeader(Obj,"-time-",Module, Message));
			debugOutWriter.flush();
			if(dbgMsgs.equalsIgnoreCase("BOTH"))
				systemOut(getLogHeader(Obj,"-time-",Module, Message));
		}
	}
	
	/**
	* Pass through sysOut
 	* 
	* <br><br><b>Usage:</b> sysOut(S,Out);
	* @param Obj Session object
	* @param Out Output message
	*/
	public static void sysOut(Object Obj, String Out)
	{
		sysOut(Obj,"UNKN",Out);
	}

	/**
	* Pass through errOut
 	* 
	* <br><br><b>Usage:</b> errOut(S,Out);
	* @param Obj Session object
	* @param Out Output message
	*/
	public static void errOut(Object Obj, String Out)
	{
		errOut(Obj,"UNKN",Out);
	}

	/**
	* Pass through errOut
 	* 
	* <br><br><b>Usage:</b> errOut(Out);
	* @param Out Output message
	*/
	public static void errOut(String Out)
	{
		errOut(null,"UNKN",Out);
	}

	/**
	* Pass through sysOut
 	* 
	* <br><br><b>Usage:</b> sysOut(Module,Out);
	* @param Module The module name
	* @param Out Output message
	*/
	public static void sysOut(String Module, String Out)
	{
		sysOut(null,Module,Out);
	}

	/**
	* Pass through sysOut
 	* 
	* <br><br><b>Usage:</b> sysOut(Out);
	* @param Out Output message
	*/
	public static void sysOut(String Out)
	{
		sysOut(null,"UNKN",Out);
	}

	/**
	* Pass through errOut
 	* 
	* <br><br><b>Usage:</b> errOut(Module,Out);
	* @param Module The module name
	* @param Out Output message
	*/
	public static void errOut(String Module, String Out)
	{
		errOut(null,Module,Out);
	}
	
	/**
	* Pass through debugOut
 	* 
	* <br><br><b>Usage:</b> debugOut(S,Out);
	* @param Obj Session object
	* @param Out Output message
	*/
	public static void debugOut(Object Obj, String Out)
	{
		debugOut(Obj,"UNKN",Out);
	}

	/**
	* Pass through debugOut
 	* 
	* <br><br><b>Usage:</b> debugOut(Out);
	* @param Out Output message
	*/
	public static void debugOut(String Out)
	{
		debugOut(null,"UNKN",Out);
	}

	/**
	* Pass through debugOut
 	* 
	* <br><br><b>Usage:</b> debugOut(Module,Out);
	* @param Module The module name
	* @param Out Output message
	*/
	public static void debugOut(String Module, String Out)
	{
		debugOut(null,Module,Out);
	}
	
	/** 
	 * Shut down this class forever
	 */
	public static void close()
	{
		fileOutWriter.close();
		errOutWriter=null;
		systemOutWriter=null;
		systemOutWriter=null;
		debugOutWriter=null;
		fileOutWriter=null;
	}

	
	public static boolean errorChannelOn()
	{
		return errOutWriter!=null;
	}
	public static boolean debugChannelOn()
	{
		return debugOutWriter!=null;
	}
	public static boolean sysChannelOn()
	{
		return systemOutWriter!=null;
	}
}
