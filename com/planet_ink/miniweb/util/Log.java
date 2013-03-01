package com.planet_ink.miniweb.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/*
   Copyright 2000-2013 Bo Zimmerman

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

/**
 * One of the oldest classes in CoffeeMud -- ye olde logger.  
 * Features include date formatting, the standard set of log channels,
 * plus optional numeric levels within those in case some kinds of info
 * are more important than others.  Also manages multiple log files if
 * necessary, up to a limit defined by the appl.
 * @author Bo Zimmerman
 *
 */
public class Log extends java.util.logging.Logger
{
	/** final date format for headers */
	public static final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd.HHmm.ss");
	
	/** SPACES for headers */
	private static final String SPACES="                                                                                               ";
	
	private PrintWriter fileOutWriter=null; /**	always to "log" */
	private final PrintWriter systemOutWriter=new PrintWriter(System.out,true); /** always to systemout */
	private String 	filePath = ""; /** The fully qualified file path */
	private String 	logName = "application"; /** log name */
	private String 	LOGNAME = "APPLICATION"; /** log name */
	private final Map<LogType,String>		FLAGS=new Hashtable<LogType,String>();
	private final Map<LogType,PrintWriter[]> WRITERS=new Hashtable<LogType,PrintWriter[]>();
	private static final Log[] 				logs=new Log[256];
	
	public static enum LogType { error, help, debug, info, warning, kills, combat }
	
	public Log()
	{
		super("log",null);
		char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(logs[threadCode]==null)
			logs[threadCode]=this;
	}
	private static final Log l(){ return logs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
	public static final Log l(char threadCode){return logs[threadCode];}
	public static final Log instance()
	{
		Log log=l();
		if(log==null) 
			log=new Log();
		return log;
	}
	public static final Log newInstance()
	{
		final char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		logs[threadCode]=new Log();
		return l();
	}
	public static final void shareWith(final char threadCode) 
	{
		final char tc=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(logs[threadCode]!=null)
			logs[tc]=logs[threadCode];
		else
		if(logs[tc]!=null)
			logs[threadCode]=logs[tc];
		else
			logs[tc]=logs[threadCode]=new Log();
	}

	/**
	 * Optional method to determine if message is a masked
	 * out throwable message type.
 	 *
	 * <br><br><b>Usage:</b> if(isMaskedErrMsg(errException.getMessage()))
	 * @param str the message
	 * @return boolean TRUE if masked out.
	 */
	public static final boolean isMaskedErrMsg(final String str)
	{
		if(str==null) return false;
		String upstr=str.toLowerCase();
		for(int i=0;i<maskErrMsgs.length;i++)
			if(upstr.indexOf(maskErrMsgs[i])>=0)
				return true;
		return false;
	}

	/**
	 * Returns the integer value of a string without exception
 	 *
	 * <br><br><b>Usage:</b> int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	private static final int s_int(final String INT)
	{
		try{ return Integer.parseInt(INT); }
		catch(java.lang.NumberFormatException e){ return 0;}
	}


	private final boolean isWriterOn(final LogType type)
	{
		final String flag=prop(type);
		if(flag==null) return true;
		if(flag.length()==0) return false;
		if(flag.startsWith("OFF")) return false;
		return true;
	}
	
	public final String getLogFilename(final LogType type)
	{
		final String flag=prop(type);
		if(flag.startsWith("OWNFILE"))
			return logName+"_"+type.toString().toLowerCase()+".log";
		if((flag.startsWith("FILE"))||(flag.startsWith("BOTH")))
			return logName+".log";
		return null;
	}
	
	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param type log type
	 * @return PrintWriter the writer
	 */
	private final PrintWriter getWriter(final LogType type, int priority)
	{
		PrintWriter[] writers=WRITERS.get(type);
		if(priority<0) priority=0;
		if(priority>9) priority=9;
		if(writers!=null) return writers[priority];
		writers=new PrintWriter[10];
		WRITERS.put(type,writers);
		final String flag=prop(type);
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
				File fileOut=new File(logName+"_"+type.toString().toLowerCase()+".log");
				try
				{
					filePath = fileOut.getAbsolutePath();
					final FileOutputStream fileStream=new FileOutputStream(fileOut,true);
					final PrintWriter pw=new PrintWriter(fileStream,true);
					for(int i=0;i<10;i++)
						writers[i]=pw;
					WRITERS.remove(type);
					pw.close();
				}
				catch(IOException e)
				{
					e.printStackTrace(System.err);
				}
			}
			int x=flag.length();
			while(Character.isDigit(flag.charAt(--x)))
				{}
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
	 * @param type LogType
	 * @return PrintWriter the writer
	 */
	private final String prop(LogType type)
	{
		String s=FLAGS.get(type);
		if(s==null)
		{
			s=System.getProperty("LOG."+LOGNAME+"_"+type.toString().toUpperCase().trim());
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
	public final void setLogOutput(final String newSYSMSGS,
								   final String newERRMSGS,
								   final String newWARNMSGS,
								   final String newDBGMSGS,
								   final String newHLPMSGS,
								   final String newKILMSGS,
								   final String newCBTMSGS)
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
	public final void startLogFiles(final String newLogName, final int numberOfLogs)
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
	public final void startLogFiles(final String newLogName, final String dirPath, final int numberOfLogs)
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
					catch(Exception t)
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
					final File f=new File(name);
					if(f.exists())
						f.delete();
				}catch(Exception e){}
				for(int i=numberOfLogs-1;i>0;i--)
				{
					final String inum=(i>0)?(""+i):"";
					final String inumm1=(i>1)?(""+(i-1)):"";
					try{
						final File f=new File(logName+inumm1+".log");
						if(f.exists())
							f.renameTo(new File(logName+inum+".log"));
					}catch(Exception e){}
				}
			}
			String name=logName+".log";
			if(directoryPath!=null) name=directoryPath.getAbsolutePath()+File.separatorChar+name;
			final File fileOut=new File(name);
			filePath = fileOut.getAbsolutePath();
			final FileOutputStream fileStream=new FileOutputStream(fileOut);
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
	
	/**
	 * Returns number of lines in the log file, if any.
	 * @return a number >=0
	 */
	public final int numLines()
	{
		int num=0;
		try
		{
			final FileReader F=new FileReader(logName+".log");
			final BufferedReader reader = new BufferedReader(F);
			String line="";
			while((line!=null)&&(reader.ready()))
			{ line=reader.readLine(); num++;}
			reader.close();
		}
		catch(Exception e)
		{
			standardOut(LogType.error,"Log",e.getMessage(),Integer.MIN_VALUE);
		}
		return num;
	}
	
	/**
	 * Returns an internally managed log reader class to make
	 * reading lines from the log slightly easier
	 * @return
	 */
	public final LogReader getLogReader()
	{
		return new LogReader() {
			BufferedReader reader = null;
			public String nextLine()
			{
				if(reader==null)
				{
					try
					{
						final FileReader F=new FileReader(logName+".log");
						reader = new BufferedReader(F);
					}
					catch(Exception e)
					{
						standardOut(LogType.error,"Log",e.getMessage(),Integer.MIN_VALUE);
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
	
	/**
	 * Returns the contents of the log file as a StringBuffer, if it exists
	 * @return the contents of the log file, or an empty stringbuffer
	 */
	public final StringBuffer getLog()
	{

		final StringBuffer buf=new StringBuffer("");

		LogReader reader = getLogReader();
		String line;
		while((line = reader.nextLine()) != null)
					buf.append(line+"\n\r");
		return buf;
	}
	
	/**
	* Start all of the log files
	*
	* <br><br><b>Usage:</b>  path = getLogLocation();
	* @return the string representation of the file path
	*/
	public final String getLogLocation()
	{
		return filePath;
	}

	/**
	* Will be used to create a standardized log header for file logs
 	*
	* <br><br><b>Usage:</b> SysOutWriter.println(getLogHeader(S,LogType.info,Module, Message));
	* @param Obj Session object
	* @param Type Type of information
	* @param Module The module name
	* @param Message The message to print
	* @return String The header and message, formatted
	*/
	private static final String getLogHeader(final LogType type, final String Module, final String Message)
	{
		final String date=dateFormat.format(Calendar.getInstance().getTime());
		final StringBuffer Header=new StringBuffer((date+SPACES).substring(0,18));
		Header.append((type.toString()+SPACES).substring(0,6));
		Header.append((Module+SPACES).substring(0,15));
		Header.append(Message);
		return Header.toString();
	}

	public static final void infoOut(final String Out) { infoOut(Thread.currentThread().getName(),Out); }
	public static final void sysOut(final String Out){ infoOut(Out); }
	public static final void debugOut(final String Out){ debugOut(Thread.currentThread().getName(),Out); }
	public static final void errOut(final String Out){ errOut(Thread.currentThread().getName(),Out); }
	public static final void warnOut(final String Out){ warnOut(Thread.currentThread().getName(),Out); }
	public static final void helpOut(final String Out) { helpOut(Thread.currentThread().getName(),Out); }
	public static final void killsOut(final String Out) { killsOut(Thread.currentThread().getName(),Out); }
	public static final void combatOut(final String Out) { combatOut(Thread.currentThread().getName(),Out); }
	public static final void sysOut(final String Module, final String Message){ infoOut(Module,Message);}
	public static final void infoOut(final String Module, final String Message){ l().standardOut(LogType.info,Module,Message,Integer.MIN_VALUE);}
	public static final void errOut(final String Module, final String Message){ l().standardOut(LogType.error,Module,Message,Integer.MIN_VALUE);}
	public static final void warnOut(final String Module, final String Message){ l().standardOut(LogType.warning,Module,Message,Integer.MIN_VALUE);}
	public static final void debugOut(final String Module, final String Message){ l().standardOut(LogType.debug,Module,Message,Integer.MIN_VALUE);}
	public static final void helpOut(final String Module, final String Message){ l().standardOut(LogType.help,Module,Message,Integer.MIN_VALUE);}
	public static final void killsOut(final String Module, final String Message){ l().standardOut(LogType.kills,Module,Message,Integer.MIN_VALUE);}
	public static final void combatOut(final String Module, final String Message){ l().standardOut(LogType.combat,Module,Message,Integer.MIN_VALUE);}
	public static final void debugOut(final String Module, final Throwable e){ l().shortExOut(LogType.debug,Module,Integer.MIN_VALUE,e);}
	public static final void errOut(final String Module, final Throwable e){ l().standardExOut(LogType.error,Module,Integer.MIN_VALUE,e);}
	public static final void warnOut(final String Module, final Throwable e){ l().standardExOut(LogType.error,Module,Integer.MIN_VALUE,e);}
	public static final void rawSysOut(final String Message){l().rawStandardOut(LogType.info,Message,Integer.MIN_VALUE);}
	public static final void infoOut(final String Out, final int priority) { infoOut(Thread.currentThread().getName(),Out,priority); }
	public static final void sysOut(final String Out, final int priority){ infoOut(Out,priority); }
	public static final void debugOut(final String Out, final int priority){ debugOut(Thread.currentThread().getName(),Out,priority); }
	public static final void errOut(final String Out, final int priority){ errOut(Thread.currentThread().getName(),Out,priority); }
	public static final void warnOut(final String Out, final int priority){ warnOut(Thread.currentThread().getName(),Out,priority); }
	public static final void helpOut(final String Out, final int priority) { helpOut(Thread.currentThread().getName(),Out,priority); }
	public static final void killsOut(final String Out, final int priority) { killsOut(Thread.currentThread().getName(),Out,priority); }
	public static final void combatOut(final String Out, final int priority) { combatOut(Thread.currentThread().getName(),Out,priority); }
	public static final void infoOut(final String Module, final String Message, final int priority){ l().standardOut(LogType.info,Module,Message,priority);}
	public static final void sysOut(final String Out, final String Message, final int priority){ infoOut(Out,Message);}
	public static final void errOut(final String Module, final String Message, final int priority){ l().standardOut(LogType.error,Module,Message,priority);}
	public static final void warnOut(final String Module, final String Message, final int priority){ l().standardOut(LogType.warning,Module,Message,priority);}
	public static final void debugOut(final String Module, final String Message, final int priority){ l().standardOut(LogType.debug,Module,Message,priority);}
	public static final void helpOut(final String Module, final String Message, final int priority){ l().standardOut(LogType.help,Module,Message,priority);}
	public static final void killsOut(final String Module, final String Message, final int priority){ l().standardOut(LogType.kills,Module,Message,priority);}
	public static final void combatOut(final String Module, final String Message, final int priority){ l().standardOut(LogType.combat,Module,Message,priority);}
	public static final void debugOut(final String Module, final int priority, final Exception e){ l().shortExOut(LogType.debug,Module,priority,e);}
	public static final void errOut(final String Module, final int priority, final Throwable e){ l().standardExOut(LogType.error,Module,priority,e);}
	public static final void warnOut(final String Module, final int priority, final Throwable e){ l().standardExOut(LogType.error,Module,priority,e);}
	public static final void rawSysOut(final String Message, final int priority){l().rawStandardOut(LogType.info,Message,priority);}

	/**
	* Handles long exception logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> standardExOut(Thread.currentThread().getName(),Out);
	* @param type The channel to print to
	* @param Module The module to print
	* @param e	The exception whose string one wishes to print
	*/
	public final void standardExOut(final LogType type, final String Module, final int priority, final Throwable e)
	{
		final PrintWriter outWriter=getWriter(type,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
    			if(e!=null)
    			{
    				outWriter.println(getLogHeader(type,Module, e.getMessage()));
    				e.printStackTrace(outWriter);
    				outWriter.flush();
    			}
    			else
    				outWriter.println(getLogHeader(type,Module,"Null/Unknown error occurred."));
    			if(prop(type).startsWith("BOTH"))
    			{
    				if(e!=null)
    				{
    					System.out.println(getLogHeader(type,Module, e.getMessage()));
    					e.printStackTrace(System.out);
    					System.out.flush();
    				}
    				else
    					System.out.println(getLogHeader(type,Module,"Null/Unknown error occurred."));
    			}
    			close(outWriter);
			}
		}
	}

	/**
	* Handles error logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> shortExOut(LogType.info,Thread.currentThread().getName(),Out);
	* @param type The type of channel
	* @param Module The message to print
	* @param e	The exception whose string one wishes to print
	*/
	public final void shortExOut(final LogType type, final String Module, final int priority, final Throwable e)
	{
		final PrintWriter outWriter=getWriter(type,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
    			outWriter.println(getLogHeader(type,Module, e.getMessage()));
    			e.printStackTrace(outWriter);
    			outWriter.flush();
    			if(prop(type).startsWith("BOTH"))
    			{
    				System.out.println(getLogHeader(type,Module, e.getMessage()));
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
	* <br><br><b>Usage:</b> rawStandardOut(LogType.info,"REQ-OUT:"+REQ);
	* @param type The type of message
	* @param Message The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	public final void rawStandardOut(final LogType type, final String Message, final int priority)
	{
		final PrintWriter outWriter=getWriter(type,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
    			outWriter.println(Message);
    			outWriter.flush();
    			if(prop(type).startsWith("BOTH"))
    				System.out.println(Message);
    			close(outWriter);
			}
		}
	}

	/**
	* Handles debug logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> standardOut(LogType.info,Thread.currentThread().getName(),Out);
	* @param type The type of writer
	* @param Module The file name
	* @param Message The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	private final void standardOut(final LogType type, final String Module, final String Message, final int priority)
	{
		final PrintWriter outWriter=getWriter(type,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
				outWriter.println(getLogHeader(type,Module, Message));
				outWriter.flush();
				if(prop(type).startsWith("BOTH"))
					System.out.println(getLogHeader(type,Module, Message));
				close(outWriter);
			}
		}
	}

	/**
	* Handles debug timing entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> timeOut(LogType.info,Thread.currentThread().getName(),Out);
	* @param type Channel name
	* @param Module The file name
	* @param Message The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	public final void timeOut(final LogType type, final String Module, String Message, final int priority)
	{
		final PrintWriter outWriter=getWriter(type,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
    			final Calendar C=Calendar.getInstance();
    			Message=C.get(Calendar.MINUTE)+":"+C.get(Calendar.SECOND)+":"+C.get(Calendar.MILLISECOND)+": "+Message;
    			outWriter.println(getLogHeader(type,Module, Message));
    			outWriter.flush();
    			if(prop(type).startsWith("BOTH"))
    				System.out.println(getLogHeader(type,Module, Message));
    			close(outWriter);
			}
		}
	}

	/**
	 * Close the given printwriter, if its an "ownfile".
	 */
	private final PrintWriter close(final PrintWriter pr)
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
	public final void close()
	{
		fileOutWriter.close();
		fileOutWriter=null;
	}

	public static final boolean errorChannelOn() { return l().isWriterOn(LogType.error);}
	public static final boolean helpChannelOn() { return l().isWriterOn(LogType.help);}
	public static final boolean debugChannelOn() { return l().isWriterOn(LogType.debug);}
	public static final boolean infoChannelOn() { return l().isWriterOn(LogType.info);}
	public static final boolean warnChannelOn() { return l().isWriterOn(LogType.warning);}
	public static final boolean killsChannelOn() { return l().isWriterOn(LogType.kills);}
	public static final boolean combatChannelOn() { return l().isWriterOn(LogType.combat);}
	public static final boolean errorChannelAt(int priority) { return l().getWriter(LogType.error,priority)!=null;}
	public static final boolean helpChannelAt(int priority) { return l().getWriter(LogType.help,priority)!=null;}
	public static final boolean debugChannelAt(int priority) { return l().getWriter(LogType.debug,priority)!=null;}
	public static final boolean infoChannelAt(int priority) { return l().getWriter(LogType.info,priority)!=null;}
	public static final boolean warnChannelAt(int priority) { return l().getWriter(LogType.warning,priority)!=null;}
	public static final boolean killsChannelAt(int priority) { return l().getWriter(LogType.kills,priority)!=null;}
	public static final boolean combatChannelAt(int priority) { return l().getWriter(LogType.combat,priority)!=null;}
	
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

	private String toStringList(Object[] os)
	{
		if(os==null) return "";
		if(os.length==0) return "";
		
		StringBuilder str=new StringBuilder((os[0]==null)?"null":os[0].toString());
		for(int i=1;i<os.length;i++)
			str.append(",").append(os[i]==null?"null":os[i].toString());
		return str.toString();
	}
	
	private String toModuleName(String sourceClass)
	{
		if((sourceClass!=null)&&(sourceClass.length()>0)&&(sourceClass.indexOf('.')>=0))
			return sourceClass.substring(sourceClass.lastIndexOf('.')+1);
		else
			return Thread.currentThread().getName();
	}
	
	@Override
	public synchronized void addHandler(Handler handler){}
    //Log a CONFIG message.
	@Override
	public void	config(String msg)
	{
		l().standardOut(LogType.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
    //Log a method entry.
	@Override
	public void	entering(String sourceClass, String sourceMethod) 
	{
		l().standardOut(LogType.debug, toModuleName(sourceClass), sourceMethod, Integer.MIN_VALUE);
	}
    //Log a method entry, with one parameter.
	@Override
	public void	entering(String sourceClass, String sourceMethod, Object param1) 
	{
		l().standardOut(LogType.debug, toModuleName(sourceClass), sourceMethod+": "+param1, Integer.MIN_VALUE);
	}
    //Log a method entry, with an array of parameters.
	@Override
	public void	entering(String sourceClass, String sourceMethod, Object[] params) 
	{
		l().standardOut(LogType.debug, toModuleName(sourceClass), sourceMethod+": "+toStringList(params), Integer.MIN_VALUE);
	}
    //Log a method return.
	@Override
	public void	exiting(String sourceClass, String sourceMethod) 
	{
		l().standardOut(LogType.debug, toModuleName(sourceClass), sourceMethod, Integer.MIN_VALUE);
	}
    //Log a method return, with result object.
	@Override
	public void	exiting(String sourceClass, String sourceMethod, Object result) 
	{
		l().standardOut(LogType.debug, toModuleName(sourceClass), sourceMethod+": "+result, Integer.MIN_VALUE);
	}
    //Log a FINE message.
	@Override
	public void	fine(String msg) 
	{
		l().standardOut(LogType.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
    //Log a FINER message.
	@Override
	public void	finer(String msg) 
	{
		l().standardOut(LogType.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
    //Log a FINEST message.
	@Override
	public void	finest(String msg) 
	{
		l().standardOut(LogType.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
    //Get the current filter for this Logger.
	@Override
	public Filter	getFilter(){ return null;}
    //Get the Handlers associated with this logger.
	@Override
	public synchronized Handler[]	getHandlers() { return new Handler[0];}
    //Get the log Level that has been specified for this Logger.
	@Override
	public Level	getLevel() 
	{
		Log log=l();
		if(log.isWriterOn(LogType.debug)) return Level.FINE;
		if(log.isWriterOn(LogType.info)) return Level.INFO;
		if(log.isWriterOn(LogType.warning)) return Level.WARNING;
		if(log.isWriterOn(LogType.error)) return Level.SEVERE;
		return Level.OFF;
	}
    //Get the name for this logger.
	@Override
	public String	getName() 
	{
		return logName;
	}
    //Return the parent for this Logger.
	@Override
	public Logger	getParent(){ return null; }
    //Retrieve the localization resource bundle for this logger for the current default locale.
	@Override
	public ResourceBundle	getResourceBundle() { return null; }
    //Retrieve the localization resource bundle name for this logger.
	@Override
	public String	getResourceBundleName(){ return ""; }
    //Discover whether or not this logger is sending its output to its parent logger.
	@Override
	public synchronized boolean	getUseParentHandlers(){ return false; }
    //Log an INFO message.
	@Override
	public void	info(String msg) 
	{
		standardOut(LogType.info,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
    //Check if a message of the given level would actually be logged by this logger.
	@Override
	public boolean	isLoggable(Level level) 
	{
		if(level==Level.INFO) return this.isWriterOn(LogType.info);
		else if(level==Level.SEVERE)  return this.isWriterOn(LogType.error);
		else if(level==Level.WARNING)  return this.isWriterOn(LogType.warning);
		else  return this.isWriterOn(LogType.debug);
	}
    //Log a message, with no arguments.
	@Override
	public void	log(Level level, String msg) 
	{
		if(level==Level.INFO) standardOut(LogType.info,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(LogType.error,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(LogType.warning,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
		else standardOut(LogType.debug,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
    //Log a message, with one object parameter.
	@Override
	public void	log(Level level, String msg, Object param1) 
	{
		String oStr=(param1==null)?"null":param1.toString();
		if(level==Level.INFO) standardOut(LogType.info,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(LogType.error,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(LogType.warning,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else standardOut(LogType.debug,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
	}
    //Log a message, with an array of object arguments.
	@Override
	public void	log(Level level, String msg, Object[] params) 
	{
		String oStr=toStringList(params);
		if(level==Level.INFO) standardOut(LogType.info,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(LogType.error,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(LogType.warning,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else standardOut(LogType.debug,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
	}
    //Log a message, with associated Throwable information.
	@Override
	public void	log(Level level, String msg, Throwable thrown) 
	{
		if(thrown==null) log(level,msg);
		else if(level==Level.INFO) standardExOut(LogType.info,toModuleName(msg),Integer.MIN_VALUE,thrown);
		else if(level==Level.SEVERE) standardExOut(LogType.error,toModuleName(msg),Integer.MIN_VALUE,thrown);
		else if(level==Level.WARNING) standardExOut(LogType.warning,toModuleName(msg),Integer.MIN_VALUE,thrown);
		else shortExOut(LogType.debug,toModuleName(msg),Integer.MIN_VALUE,thrown);
	}
    //Log a LogRecord.
	@Override
	public void	log(LogRecord record) 
	{
		log(record.getLevel(), record.getMessage(), record.getThrown());
	}
    //Log a message, specifying source class and method, with no arguments.
	@Override
	public void	logp(Level level, String sourceClass, String sourceMethod, String msg) 
	{
		if(level==Level.INFO) standardOut(LogType.info,toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(LogType.error,toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(LogType.warning,toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
		else standardOut(LogType.debug,toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
	}
    //Log a message, specifying source class and method, with a single object parameter to the log message.
	@Override
	public void	logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) 
	{
		String oStr=(param1==null)?"null":param1.toString();
		if(level==Level.INFO) standardOut(LogType.info,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(LogType.error,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(LogType.warning,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else standardOut(LogType.debug,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
	}
    //Log a message, specifying source class and method, with an array of object arguments.
	@Override
	public void	logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) 
	{
		String oStr=toStringList(params);
		if(level==Level.INFO) standardOut(LogType.info,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(LogType.error,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(LogType.warning,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else standardOut(LogType.debug,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
	}
    //Log a message, specifying source class and method, with associated Throwable information.
	@Override
	public void	logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) 
	{
		if(thrown==null) log(level,msg);
		else if(level==Level.INFO) standardExOut(LogType.info,toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
		else if(level==Level.SEVERE) standardExOut(LogType.error,toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
		else if(level==Level.WARNING) standardExOut(LogType.warning,toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
		else shortExOut(LogType.debug,toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
	}
    //Log a message, specifying source class, method, and resource bundle name with no arguments.
	@Override
	public void	logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg) 
	{
		logp(level,sourceClass,sourceMethod+": "+bundleName, msg);
	}
    //Log a message, specifying source class, method, and resource bundle name, with a single object parameter to the log message.
	@Override
	public void	logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object param1) 
	{
		logp(level,sourceClass,sourceMethod+": "+bundleName, msg, param1);
	}
    //Log a message, specifying source class, method, and resource bundle name, with an array of object arguments.
	@Override
	public void	logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Object[] params) 
	{
		logp(level,sourceClass,sourceMethod+": "+bundleName, msg, params);
	}
    //Log a message, specifying source class, method, and resource bundle name, with associated Throwable information.
	@Override
	public void	logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg, Throwable thrown) 
	{
		logp(level,sourceClass,sourceMethod+": "+bundleName, msg, thrown);
	}
    //Remove a log Handler.
	@Override
	public synchronized void	removeHandler(Handler handler){}
    //Set a filter to control output on this Logger.
	@Override
	public void	setFilter(Filter newFilter){}
    //Set the log level specifying which message levels will be logged by this logger.
	@Override
	public void	setLevel(Level newLevel){}
    //Set the parent for this Logger.
	@Override
	public void	setParent(Logger parent){}
    //Specify whether or not this logger should send its output to it's parent Logger.
	@Override
	public synchronized void	setUseParentHandlers(boolean useParentHandlers){} 
    //Log a SEVERE message.
	@Override
	public void	severe(String msg) 
	{
		standardOut(LogType.error,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
    //Log throwing an exception.
	@Override
	public void	throwing(String sourceClass, String sourceMethod, Throwable thrown) 
	{
		standardExOut(LogType.error, toModuleName(sourceClass), Integer.MIN_VALUE, thrown);
	}
    //Log a WARNING message.
	@Override
	public void	warning(String msg) 
	{
		standardOut(LogType.warning,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
}
