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

	/**	always to "log" */
	private static PrintWriter fileOutWriter=null;
	/** always to systemout */
	private static PrintWriter systemOutWriter=new PrintWriter(System.out,true);
	/** SPACES for headers */
	private static final String SPACES="                                                                                               ";
	/** The fully qualified file path */
	private static String filePath = "";
	/** log name */
	private static String logName = "application";
	private static String LOGNAME = "APPLICATION";
	/** totally optional, this is the list of maskable error message types.  Useful for internet apps */
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

	/**
	 * Optional method to determine if message is a masked
	 * out throwable message type.
 	 *
	 * <br><br><b>Usage:</b> if(isMaskedErrMsg(errException.getMessage()))
	 * @param String the message
	 * @return boolean TRUE if masked out.
	 */
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
	 * Returns the integer value of a string without crashing
 	 *
	 * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	private static int s_int(String INT)
	{
		int sint=0;
		try{ sint=Integer.parseInt(INT); }
		catch(java.lang.NumberFormatException e){ return 0;}
		return sint;
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param name code string
	 * @return PrintWriter the writer
	 */
	private static PrintWriter getWriter(String name)
	{
		String flag=prop(name);
		if(flag==null)
			return systemOutWriter;
		else
		if(flag.equalsIgnoreCase("ON"))
			return systemOutWriter;
		else
		if(flag.equalsIgnoreCase("OFF"))
			return null;
		else
		if((flag.equalsIgnoreCase("FILE"))||(flag.equalsIgnoreCase("BOTH")))
			return fileOutWriter;
		else
		if(flag.equalsIgnoreCase("OWNFILE"))
		{
			File fileOut=new File(logName+"_"+name.toLowerCase()+".log");
			try
			{
				filePath = fileOut.getAbsolutePath();
				FileOutputStream fileStream=new FileOutputStream(fileOut,true);
				return new PrintWriter(fileStream,true);
			}
			catch(IOException e)
			{
				Log.errOut("Log",e);
			}
		}
		return null;
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param name code string
	 * @return PrintWriter the writer
	 */
	private static String prop(String type)
	{
		String s=System.getProperty("LOG."+LOGNAME+"_"+type.toUpperCase().trim());
		if(s==null) return "";
		return s;
	}
	
	/**
	* Reset all of the log files
	* ON, OFF, FILE, BOTH
	* <br><br><b>Usage:</b>  CMProps.Initialize("ON","OFF");
	* @param newSYSMSGS code string to describe info msgs
	* @param newERRMSGS code string to describe error msgs
	* @param newWARNMSGS code string to describe warning msgs
	* @param newDBGMSGS code string to describe debug msgs
	* @param newHLPMSGS code string to describe help msgs
	*/
	public static void setLogOutput(String newSYSMSGS,
								    String newERRMSGS,
								    String newWARNMSGS,
								    String newDBGMSGS,
								    String newHLPMSGS)
	{
		System.setProperty("LOG."+LOGNAME+"_INFO",newSYSMSGS);
		System.setProperty("LOG."+LOGNAME+"_ERROR",newERRMSGS);
		System.setProperty("LOG."+LOGNAME+"_WARN",newWARNMSGS);
		System.setProperty("LOG."+LOGNAME+"_DEBUG",newDBGMSGS);
		System.setProperty("LOG."+LOGNAME+"_HELP",newHLPMSGS);
	}

	/**
	* Start all of the log files in the info temp directory
	*
	* <br><br><b>Usage:</b>  startLogFiles(5);
	* @param numberOfLogs maximum number of files
	*/
	public static void startLogFiles(String newLogName, int numberOfLogs)
	{
		// ===== pass in a null to force the temp directory
		startLogFiles(newLogName, "", numberOfLogs);
	}

	/**
	* Start all of the log files in the specified directory
	*
	* <br><br><b>Usage:</b>  startLogFiles("c:\\temp");
	* @param dirPath the place to create the file
	* @param numberOfLogs maximum number of files
	*/
	public static void startLogFiles(String newLogName, String dirPath, int numberOfLogs)
	{
		logName=newLogName;
		LOGNAME=logName.toUpperCase().trim();
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
			if(numberOfLogs>1)
			{
				try{
					String name=logName+(numberOfLogs-1)+".log";
					if(directoryPath!=null) name=directoryPath.getAbsolutePath()+File.separatorChar+name;
					File f=new File(name);
					if(f.exists())
						f.delete();
				}catch(Exception e){}
				for(int i=numberOfLogs-1;i>0;i--)
				{
					String inum=(i>0)?(""+i):"";
					String inumm1=(i>1)?(""+(i-1)):"";
					try{
						File f=new File(logName+inumm1+".log");
						if(f.exists())
							f.renameTo(new File(logName+inum+".log"));
					}catch(Exception e){}
				}
			}
			String name=logName+".log";
			if(directoryPath!=null) name=directoryPath.getAbsolutePath()+File.separatorChar+name;
			File fileOut=new File(name);
			filePath = fileOut.getAbsolutePath();
			FileOutputStream fileStream=new FileOutputStream(fileOut);
			fileOutWriter=new PrintWriter(fileStream,true);
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
			FileReader F=new FileReader(logName+".log");
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
	* Will be used to create a standardized log header for file logs
 	*
	* <br><br><b>Usage:</b> SysOutWriter.println(getLogHeader(S,"Info",Module, Message));
	* @param Obj Session object
	* @param Type Type of information
	* @param Module The module name
	* @param Message The message to print
	* @return String The header and message, formatted
	*/
    private static String getLogHeader(String Type, String Module, String Message)
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
            int Yr=s_int(Year);
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

	public static void infoOut(String Out) { infoOut("UNKN",Out); }
	public static void sysOut(String Out){ infoOut(Out); }
	public static void debugOut(String Out){ debugOut("UNKN",Out); }
	public static void errOut(String Out){ errOut("UNKN",Out); }
	public static void warnOut(String Out){ warnOut("UNKN",Out); }
	public static void helpOut(String Out) { helpOut("UNKN",Out); }
	public static void infoOut(String Module, String Message){ standardOut("Info",Module,Message);}
	public static void sysOut(String Out, String Message){ infoOut(Out,Message);}
	public static void errOut(String Module, String Message){ standardOut("Error",Module,Message);}
	public static void warnOut(String Module, String Message){ standardOut("Warn",Module,Message);}
	public static void debugOut(String Module, String Message){ standardOut("Debug",Module,Message);}
	public static void helpOut(String Module, String Message){ standardOut("Help",Module,Message);}
	public static void debugOut(String Module, Exception e){ shortExOut("Debug",Module,e);}
	public static void errOut(String Module, Throwable e){ standardExOut("Error",Module,e);}
	public static void warnOut(String Module, Throwable e){ standardExOut("Error",Module,e);}
	public static void rawSysOut(String Message){rawStandardOut("Info",Message);}

	/**
	* Handles long exception logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> standardExOut("UNKN",Out);
	* @param Type The channel to print to
	* @param Module The module to print
	* @param e	The exception whose string one wishes to print
	*/
	public static void standardExOut(String Type, String Module, Throwable e)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=getWriter(Type);
			if(outWriter!=null)
			{
			    if(e!=null)
			    {
			    	outWriter.println(getLogHeader(Type,Module, e.getMessage()));
					e.printStackTrace(outWriter);
					outWriter.flush();
			    }
			    else
			    	outWriter.println(getLogHeader(Type,Module,"Null/Unknown error occurred."));
				if(prop(Type).equalsIgnoreCase("BOTH"))
				{
				    if(e!=null)
				    {
				    	System.out.println(getLogHeader(Type,Module, e.getMessage()));
						e.printStackTrace(System.out);
						System.out.flush();
				    }
				    else
				    	System.out.println(getLogHeader(Type,Module,"Null/Unknown error occurred."));
				}
				close(outWriter);
			}
		}
	}

	/**
	* Handles error logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> shortExOut("Info","UNKN",Out);
	* @param Type The type of channel
	* @param Module The message to print
	* @param e	The exception whose string one wishes to print
	*/
	public static void shortExOut(String Type, String Module, Exception e)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=getWriter(Type);
			if(outWriter!=null)
			{
				outWriter.println(getLogHeader(Type,Module, e.getMessage()));
				e.printStackTrace(outWriter);
				outWriter.flush();
				if(prop(Type).equalsIgnoreCase("BOTH"))
				{
					System.out.println(getLogHeader(Type,Module, e.getMessage()));
					e.printStackTrace(System.out);
					System.out.flush();
				}
				close(outWriter);
			}
		}
	}

	/**
	* Handles raw info logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	* 
	* <br><br><b>Usage:</b> rawStandardOut("Info","REQ-OUT:"+REQ);
	* @param Message The message to print
	*/
	public static void rawStandardOut(String Type, String Message)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=getWriter(Type);
			if(outWriter!=null)
			{
				outWriter.println(Message);
				outWriter.flush();
				if(prop(Type).equalsIgnoreCase("BOTH"))
					System.out.println(Message);
				close(outWriter);
			}
		}
	}
	
	/**
	* Handles debug logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> standardOut("Info","UNKN",Out);
	* @param Type The type of writer
	* @param Module The file name
	* @param Message The message to print
	*/
	private static void standardOut(String Type, String Module, String Message)
	{
		synchronized(Type.intern())
		{
			synchronized(Type.intern())
			{
				PrintWriter outWriter=getWriter(Type);
				if(outWriter!=null)
				{
					outWriter.println(getLogHeader(Type,Module, Message));
					outWriter.flush();
					if(prop(Type).equalsIgnoreCase("BOTH"))
						System.out.println(getLogHeader(Type,Module, Message));
					close(outWriter);
				}
			}
		}
	}
	
	/**
	* Handles debug timing entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> timeOut("Info","UNKN",Out);
	* @param Type Channel name
	* @param Module The file name
	* @param Message The message to print
	*/
	public static void timeOut(String Type, String Module, String Message)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=getWriter(Type);
			if(outWriter!=null)
			{
				Calendar C=Calendar.getInstance();
				Message=C.get(Calendar.MINUTE)+":"+C.get(Calendar.SECOND)+":"+C.get(Calendar.MILLISECOND)+": "+Message;
				outWriter.println(getLogHeader("-time-",Module, Message));
				outWriter.flush();
				if(prop(Type).equalsIgnoreCase("BOTH"))
					System.out.println(getLogHeader("-time-",Module, Message));
				close(outWriter);
			}
		}
	}

	/**
	 * Close the given printwriter, if its an "ownfile".
	 */
	private static PrintWriter close(PrintWriter pr)
	{
		if(pr==null) return null;
		if((pr!=systemOutWriter)
		&&(pr!=fileOutWriter))
			pr.close();
		return null;
	}
	
	/**
	 * Shut down this class forever
	 */
	public static void close()
	{
		fileOutWriter.close();
		fileOutWriter=null;
	}

	public static boolean errorChannelOn() { return getWriter("error")!=null;}
	public static boolean helpChannelOn() { return getWriter("help")!=null;}
	public static boolean debugChannelOn() { return getWriter("debug")!=null;}
	public static boolean infoChannelOn() { return getWriter("info")!=null;}
	public static boolean warnChannelOn() { return getWriter("warning")!=null;}
}
