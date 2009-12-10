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
import java.text.SimpleDateFormat;
import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Log
{
    private static Log[] logs=new Log[256];
    public Log(){
        super();
        char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(logs==null) 
            logs=new Log[256];
        if(logs[threadCode]==null)
            logs[threadCode]=this;
    }
    private static Log l(){ return logs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
    public static Log l(char threadCode){return logs[threadCode];}
    public static Log instance(){
        Log log=l();
        if(log==null) 
            log=new Log();
        return log;
    }
    public static Log newInstance(){
        char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(logs==null) 
            logs=new Log[256];
        logs[threadCode]=new Log();
        return l();
    }
    public static void shareWith(char threadCode) {
        char tc=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(logs[threadCode]!=null)
            logs[tc]=logs[threadCode];
        else
        if(logs[tc]!=null)
            logs[threadCode]=logs[tc];
        else
            logs[tc]=logs[threadCode]=new Log();
    }
    
    /** final date format for headers */
    public static SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd.HHmm.ssSS");
    /** SPACES for headers */
    private static final String SPACES="                                                                                               ";

	/**	always to "log" */
	private PrintWriter fileOutWriter=null;
	/** always to systemout */
	private PrintWriter systemOutWriter=new PrintWriter(System.out,true);
	/** The fully qualified file path */
	private String filePath = "";
	/** log name */
	private String logName = "application";
	private String LOGNAME = "APPLICATION";

    private Hashtable FLAGS=new Hashtable();
    private Hashtable WRITERS=new Hashtable();

	/**
	 * Optional method to determine if message is a masked
	 * out throwable message type.
 	 *
	 * <br><br><b>Usage:</b> if(isMaskedErrMsg(errException.getMessage()))
	 * @param str the message
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


    private boolean isWriterOn(String name)
    {
        String flag=prop(name);
        if(flag==null) return true;
        if(flag.length()==0) return false;
        if(flag.startsWith("OFF")) return false;
        return true;
    }
    
    public String getLogFilename(String name)
    {
		String flag=prop(name.toUpperCase().trim());
		if(flag.startsWith("OWNFILE"))
			return logName+"_"+name.toLowerCase()+".log";
		if((flag.startsWith("FILE"))||(flag.startsWith("BOTH")))
			return logName+".log";
		return null;
    }
    
	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param name code string
	 * @return PrintWriter the writer
	 */
	private PrintWriter getWriter(String name, int priority)
	{
		PrintWriter[] writers=(PrintWriter[])WRITERS.get(name);
		if(priority<0) priority=0;
		if(priority>9) priority=9;
		if(writers!=null) return writers[priority];
		writers=new PrintWriter[10];
		WRITERS.put(name,writers);
		String flag=prop(name);
		if(flag==null)
		{
			for(int i=0;i<10;i++)
				writers[i]=systemOutWriter;
		}
		else
		if(flag.length()>0)
		{
            if(flag.startsWith("OFF")) return null;
			if(flag.startsWith("ON"))
			{
				for(int i=0;i<10;i++)
					writers[i]=systemOutWriter;
			}
			else
			if((flag.startsWith("FILE"))||(flag.startsWith("BOTH")))
			{
				for(int i=0;i<10;i++)
					writers[i]=fileOutWriter;
			}
			else
			if(flag.startsWith("OWNFILE"))
			{
				File fileOut=new File(logName+"_"+name.toLowerCase()+".log");
				try
				{
					filePath = fileOut.getAbsolutePath();
					FileOutputStream fileStream=new FileOutputStream(fileOut,true);
					PrintWriter pw=new PrintWriter(fileStream,true);
					for(int i=0;i<10;i++)
						writers[i]=pw;
					WRITERS.remove(name);
				}
				catch(IOException e)
				{
					Log.errOut("Log",e);
				}
			}
            int x=flag.length();
			while(Character.isDigit(flag.charAt(--x)));
			int max=s_int(flag.substring(x+1));
			for(int i=max+1;i<10;i++)
				writers[i]=null;
        	return writers[priority];
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
	private String prop(String type)
	{
        String s=(String)FLAGS.get(type);
        if(s==null)
        {
    		s=System.getProperty("LOG."+LOGNAME+"_"+type.toUpperCase().trim());
    		if(s==null) s="";
            FLAGS.put(type,s);
        }
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
	public void setLogOutput(String newSYSMSGS,
						     String newERRMSGS,
						     String newWARNMSGS,
						     String newDBGMSGS,
						     String newHLPMSGS,
						     String newKILMSGS,
						     String newCBTMSGS)
	{
		System.setProperty("LOG."+LOGNAME+"_INFO",newSYSMSGS);
		System.setProperty("LOG."+LOGNAME+"_ERROR",newERRMSGS);
		System.setProperty("LOG."+LOGNAME+"_WARN",newWARNMSGS);
		System.setProperty("LOG."+LOGNAME+"_DEBUG",newDBGMSGS);
		System.setProperty("LOG."+LOGNAME+"_HELP",newHLPMSGS);
		System.setProperty("LOG."+LOGNAME+"_KILLS",newKILMSGS);
		System.setProperty("LOG."+LOGNAME+"_COMBAT",newCBTMSGS);
		FLAGS.clear();
		WRITERS.clear();
	}

	/**
	* Start all of the log files in the info temp directory
	*
	* <br><br><b>Usage:</b>  startLogFiles(5);
	* @param numberOfLogs maximum number of files
	*/
	public void startLogFiles(String newLogName, int numberOfLogs)
	{
		// ===== pass in a null to force the temp directory
		startLogFiles(newLogName, "", numberOfLogs);
	}

	/**
	* Start all of the log files in the specified directory
	*
	* <br><br><b>Usage:</b>  startLogFiles("mud","",10);
	* @param dirPath the place to create the file
	* @param numberOfLogs maximum number of files
	*/
	public void startLogFiles(String newLogName, String dirPath, int numberOfLogs)
	{
		logName=newLogName;
		LOGNAME=logName.toUpperCase().trim();
        FLAGS.clear();
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
	        WRITERS.clear();
		}
		catch(IOException e)
		{
			System.out.println("NO OPEN LOG: "+e.toString());
		}
	}

	public static interface LogReader
	{
		public String nextLine();
		public void close();
	}
	
	public int numLines()
	{
		int num=0;
		try
		{
			FileReader F=new FileReader(logName+".log");
			BufferedReader reader = new BufferedReader(F);
			String line="";
			while((line!=null)&&(reader.ready()))
			{ line=reader.readLine(); num++;}
		}
		catch(Exception e)
		{
			Log.errOut("Log",e.getMessage());
		}
		return num;
	}
	
	public LogReader getLogReader()
	{
		return new LogReader() {
			BufferedReader reader = null;
			public String nextLine()
			{
				if(reader==null)
				{
					try
					{
						FileReader F=new FileReader(logName+".log");
						reader = new BufferedReader(F);
					}
					catch(Exception e)
					{
						Log.errOut("Log",e.getMessage());
						return null;
					}
				}
				String line=null;
				try {
					if(reader.ready())
						line=reader.readLine();
			    }
			    catch ( final IOException ignore ){}
				if(line==null) close();
				return line;
			}
			public void close() {
				{
				    try
				    {
				        if ( reader != null )
				        {
				            reader.close();
				            reader = null;
				        }
				    }
				    catch ( final IOException ignore ){}
				}
			}
		};
	}
	public StringBuffer getLog()
	{

		StringBuffer buf=new StringBuffer("");

		BufferedReader reader = null;
		try
		{
			FileReader F=new FileReader(logName+".log");
			reader = new BufferedReader(F);

			String line="";
			while((line!=null)&&(reader.ready()))
			{
				line=reader.readLine();
				if(line!=null)
					buf.append(line+"\n\r");
			}
		}
		catch(Exception e)
		{
			Log.errOut("Log",e.getMessage());
		}
		finally
		{
		    try
		    {
		        if ( reader != null )
		        {
		            reader.close();
		            reader = null;
		        }
		    }
		    catch ( final IOException ignore )
		    {

		    }
		}
		return buf;
	}
	/**
	* Start all of the log files
	*
	* <br><br><b>Usage:</b>  path = getLogLocation();
	* @return the string representation of the file path
	*/
	public String getLogLocation()
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
        String date=dateFormat.format(Calendar.getInstance().getTime());
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
	public static void killsOut(String Out) { killsOut("UNKN",Out); }
	public static void combatOut(String Out) { combatOut("UNKN",Out); }
	public static void sysOut(String Module, String Message){ infoOut(Module,Message);}
	public static void infoOut(String Module, String Message){ standardOut("Info",Module,Message,Integer.MIN_VALUE);}
	public static void errOut(String Module, String Message){ standardOut("Error",Module,Message,Integer.MIN_VALUE);}
	public static void warnOut(String Module, String Message){ standardOut("Warn",Module,Message,Integer.MIN_VALUE);}
	public static void debugOut(String Module, String Message){ standardOut("Debug",Module,Message,Integer.MIN_VALUE);}
	public static void helpOut(String Module, String Message){ standardOut("Help",Module,Message,Integer.MIN_VALUE);}
	public static void killsOut(String Module, String Message){ standardOut("Kills",Module,Message,Integer.MIN_VALUE);}
	public static void combatOut(String Module, String Message){ standardOut("Combat",Module,Message,Integer.MIN_VALUE);}
	public static void debugOut(String Module, Exception e){ shortExOut("Debug",Module,Integer.MIN_VALUE,e);}
	public static void errOut(String Module, Throwable e){ standardExOut("Error",Module,Integer.MIN_VALUE,e);}
	public static void warnOut(String Module, Throwable e){ standardExOut("Error",Module,Integer.MIN_VALUE,e);}
	public static void rawSysOut(String Message){rawStandardOut("Info",Message,Integer.MIN_VALUE);}
	public static void infoOut(String Out, int priority) { infoOut("UNKN",Out,priority); }
	public static void sysOut(String Out, int priority){ infoOut(Out,priority); }
	public static void debugOut(String Out, int priority){ debugOut("UNKN",Out,priority); }
	public static void errOut(String Out, int priority){ errOut("UNKN",Out,priority); }
	public static void warnOut(String Out, int priority){ warnOut("UNKN",Out,priority); }
	public static void helpOut(String Out, int priority) { helpOut("UNKN",Out,priority); }
	public static void killsOut(String Out, int priority) { killsOut("UNKN",Out,priority); }
	public static void combatOut(String Out, int priority) { combatOut("UNKN",Out,priority); }
	public static void infoOut(String Module, String Message, int priority){ standardOut("Info",Module,Message,priority);}
	public static void sysOut(String Out, String Message, int priority){ infoOut(Out,Message);}
	public static void errOut(String Module, String Message, int priority){ standardOut("Error",Module,Message,priority);}
	public static void warnOut(String Module, String Message, int priority){ standardOut("Warn",Module,Message,priority);}
	public static void debugOut(String Module, String Message, int priority){ standardOut("Debug",Module,Message,priority);}
	public static void helpOut(String Module, String Message, int priority){ standardOut("Help",Module,Message,priority);}
	public static void killsOut(String Module, String Message, int priority){ standardOut("Kills",Module,Message,priority);}
	public static void combatOut(String Module, String Message, int priority){ standardOut("Combat",Module,Message,priority);}
	public static void debugOut(String Module, int priority, Exception e){ shortExOut("Debug",Module,priority,e);}
	public static void errOut(String Module, int priority, Throwable e){ standardExOut("Error",Module,priority,e);}
	public static void warnOut(String Module, int priority, Throwable e){ standardExOut("Error",Module,priority,e);}
	public static void rawSysOut(String Message, int priority){rawStandardOut("Info",Message,priority);}

	/**
	* Handles long exception logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> standardExOut("UNKN",Out);
	* @param Type The channel to print to
	* @param Module The module to print
	* @param e	The exception whose string one wishes to print
	*/
	public static void standardExOut(String Type, String Module, int priority, Throwable e)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=l().getWriter(Type,priority);
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
				if(l().prop(Type).startsWith("BOTH"))
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
				l().close(outWriter);
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
	public static void shortExOut(String Type, String Module, int priority, Exception e)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=l().getWriter(Type,priority);
			if(outWriter!=null)
			{
				outWriter.println(getLogHeader(Type,Module, e.getMessage()));
				e.printStackTrace(outWriter);
				outWriter.flush();
				if(l().prop(Type).startsWith("BOTH"))
				{
					System.out.println(getLogHeader(Type,Module, e.getMessage()));
					e.printStackTrace(System.out);
					System.out.flush();
				}
				l().close(outWriter);
			}
		}
	}

	/**
	* Handles raw info logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> rawStandardOut("Info","REQ-OUT:"+REQ);
	* @param Type The type of message
	* @param Message The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	public static void rawStandardOut(String Type, String Message, int priority)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=l().getWriter(Type,priority);
			if(outWriter!=null)
			{
				outWriter.println(Message);
				outWriter.flush();
				if(l().prop(Type).startsWith("BOTH"))
					System.out.println(Message);
				l().close(outWriter);
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
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	private static void standardOut(String Type, String Module, String Message, int priority)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=l().getWriter(Type,priority);
			if(outWriter!=null)
			{
				outWriter.println(getLogHeader(Type,Module, Message));
				outWriter.flush();
				if(l().prop(Type).startsWith("BOTH"))
					System.out.println(getLogHeader(Type,Module, Message));
				l().close(outWriter);
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
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	public static void timeOut(String Type, String Module, String Message, int priority)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=l().getWriter(Type,priority);
			if(outWriter!=null)
			{
				Calendar C=Calendar.getInstance();
				Message=C.get(Calendar.MINUTE)+":"+C.get(Calendar.SECOND)+":"+C.get(Calendar.MILLISECOND)+": "+Message;
				outWriter.println(getLogHeader("-time-",Module, Message));
				outWriter.flush();
				if(l().prop(Type).startsWith("BOTH"))
					System.out.println(getLogHeader("-time-",Module, Message));
				l().close(outWriter);
			}
		}
	}

	/**
	 * Close the given printwriter, if its an "ownfile".
	 */
	private PrintWriter close(PrintWriter pr)
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
	public void close()
	{
		fileOutWriter.close();
		fileOutWriter=null;
	}

	public static boolean errorChannelOn() { return l().isWriterOn("error");}
	public static boolean helpChannelOn() { return l().isWriterOn("help");}
	public static boolean debugChannelOn() { return l().isWriterOn("debug");}
	public static boolean infoChannelOn() { return l().isWriterOn("info");}
	public static boolean warnChannelOn() { return l().isWriterOn("warning");}
	public static boolean killsChannelOn() { return l().isWriterOn("kills");}
	public static boolean combatChannelOn() { return l().isWriterOn("combat");}
	public static boolean errorChannelAt(int priority) { return l().getWriter("error",priority)!=null;}
	public static boolean helpChannelAt(int priority) { return l().getWriter("help",priority)!=null;}
	public static boolean debugChannelAt(int priority) { return l().getWriter("debug",priority)!=null;}
	public static boolean infoChannelAt(int priority) { return l().getWriter("info",priority)!=null;}
	public static boolean warnChannelAt(int priority) { return l().getWriter("warning",priority)!=null;}
	public static boolean killsChannelAt(int priority) { return l().getWriter("kills",priority)!=null;}
	public static boolean combatChannelAt(int priority) { return l().getWriter("combat",priority)!=null;}
	
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

}
