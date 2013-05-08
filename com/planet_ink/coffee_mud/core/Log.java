package com.planet_ink.coffee_mud.core;

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
 * Rewritten on 05/07/2013
 * @author Bo Zimmerman
 *
 */
public class Log extends java.util.logging.Logger
{
	/** final date format for headers */
	public static final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd.HHmm.ss");
	
	/** SPACES for headers */
	private static final String SPACES="                                                                                                ";
	private static final String SPACES15=SPACES.substring(0,15);
	
	private PrintWriter 		fileOutWriter[]	= null; /**	always to "log" */
	private int					numberOfFWLogs	= 1;
	private final PrintWriter 	systemOutWriter	= new PrintWriter(System.out,true); /** always to systemout */
	private String 				filePath 		= ""; /** The fully qualified file path */
	private String 				logName 		= "application"; /** log name */
	private String 				LOGNAME 		= "APPLICATION"; /** log name */
	private static final Log[] 	logs			= new Log[256];
	private final Map<Type,Conf>CONFS			= new Hashtable<Type,Conf>();
	private final Map<PrintWriter,long[]> WRITTEN	= new Hashtable<PrintWriter,long[]>();
	
	public static enum Target { ON, OFF, BOTH, FILE, OWNFILE };
	
	private static class Conf
	{
		public final Target target;
		public int maxLevel, maxLogs, maxLines, maxBytes;
		public final PrintWriter[][] writers = new PrintWriter[10][];
		public Conf(Target target, int maxLevel, int maxLogs, int maxLines, int maxBytes) 
		{ this.target=target; this.maxLevel=maxLevel; this.maxLogs=maxLogs; this.maxLines=maxLines; this.maxBytes=maxBytes;}
	}
	
	public static enum Type { error, help, debug, info, warning, kills, combat, access;
		final String sixChars;
		private Type() { sixChars=(this.toString()+SPACES).substring(0,5)+" "; }
		public final String getSixChars() { return sixChars; }
	}
	
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


	private final boolean isWriterOn(final Type type)
	{
		return getTarget(type) != Target.OFF;
	}
	
	public final String getLogFilename(final Type type)
	{
		switch(getTarget(type))
		{
		case OWNFILE:
			return logName+"_"+type.toString().toLowerCase()+".log";
		case FILE:
		case BOTH:
			return logName+".log";
		default:
			return null;
		}
	}
	
	private final PrintWriter rollLog(final PrintWriter writer) throws IOException
	{
		if(writer==this.fileOutWriter[0])
		{
			WRITTEN.remove(writer);
			writer.close();
			FileOutputStream fileOut=this.startLogFile(logName, this.numberOfFWLogs);
			PrintWriter newPW=new PrintWriter(fileOut,true);
			this.fileOutWriter[0]=newPW;
			WRITTEN.put(newPW, new long[]{0,0});
			return newPW;
		}
		else
		for(final Type type : Type.values())
		{
			final Conf config=getConfig(type);
			if((config.target==Target.OWNFILE)&&(config.writers[0][0]==writer))
			{
				WRITTEN.remove(writer);
				writer.close();
				FileOutputStream fileOut=this.startLogFile(getLogFilename(type), config.maxLogs);
				PrintWriter newPW=new PrintWriter(fileOut,true);
				config.writers[0][0]=newPW;
				WRITTEN.put(newPW, new long[]{0,0});
				return newPW;
			}
		}
		return writer;
	}
	
	private final PrintWriter writeBytes(final Type type, final Conf config, final PrintWriter writer, final String str)
	{
		try
		{
			writer.println(str);
			final long[] counts=this.WRITTEN.get(writer);
			counts[0]++;
			counts[1]+=str.length()+1;
			if(((config.maxLines>0)&&(counts[0]>=config.maxLines))
			||((config.maxBytes>0)&&(counts[1]>=config.maxBytes)))
				return rollLog(writer);
		}
		catch(IOException e)
		{
		}
		return writer;
	}
	
	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param type log type
	 * @param config log config
	 * @return PrintWriter the writer
	 */
	private final PrintWriter getWriter(final Type type, final Conf config, final int priority)
	{
		final PrintWriter[][] writers=config.writers;
		if(writers[0]!=null)
		{
			if(priority<0) return writers[0][0];
			if(priority>9) return writers[9][0];
			return writers[priority][0];
		}
		switch(config.target)
		{
		case OFF:
			return null;
		case ON:
			for(int i=0;i<10;i++)
				writers[i]=new PrintWriter[]{systemOutWriter};
			break;
		case FILE:
		case BOTH:
			for(int i=0;i<10;i++)
				writers[i]=this.fileOutWriter;
			break;
		case OWNFILE:
			{
				try 
				{
					FileOutputStream fileStream=this.startLogFile(logName+"_"+type.toString().toLowerCase(), config.maxLogs);
					final PrintWriter[] writer=new PrintWriter[]{new PrintWriter(fileStream,true)};
					for(int i=0;i<10;i++)
						writers[i]=writer;
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		int max=config.maxLevel;
		PrintWriter[] empty=new PrintWriter[]{null};
		for(int i=max+1;i<10;i++)
			writers[i]=empty;
		for(PrintWriter[] writer : writers)
			if((writer[0]!=null) && (!WRITTEN.containsKey(writer[0])))
				WRITTEN.put(writer[0], new long[2]);
		if(priority<0) return writers[0][0];
		if(priority>9) return writers[9][0];
		return writers[priority][0];
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * <br><br><b>Usage:</b> PrintWriter W=makeConfig("BOTH");
	 * @param type LogType
	 * @param code ON, OFF, BOTH, OWNFILE, FILE
	 * @return Conf the config
	 */
	private final Conf makeConfig(final Type type, String code)
	{
		if((code==null)||(code.trim().length()==0))
			return new Conf(Target.OFF,0,1,0,0);
		int maxNumberOfLogs=1;
		int maxNumberOfBytes=0;
		int maxNumberOfEntries=0;
		code=code.trim().toUpperCase();
		int maxLevel=0;
		int x=code.indexOf('<');
		if(x>0)
		{
			String numStr=code.substring(x+1).trim();
			code=code.substring(0,x).trim();
			if(Character.isDigit(numStr.charAt(0)))
			{
				x=0;
				while((x<numStr.length())&&(Character.isDigit(numStr.charAt(x)))) {x++;}
				int num=s_int(numStr.substring(0,x));
				numStr=numStr.substring(x).toLowerCase();
				if(numStr.startsWith("b"))
					maxNumberOfBytes=num;
				else
				if(numStr.startsWith("k"))
					maxNumberOfBytes=num*1024;
				else
				if(numStr.startsWith("m"))
					maxNumberOfBytes=num*1024*1024;
				else
				if(numStr.startsWith("g"))
					maxNumberOfBytes=num*1024*1024*1024;
				else
					maxNumberOfEntries=num;
				maxNumberOfLogs=this.numberOfFWLogs;
			}
		}
		if(Character.isDigit(code.charAt(code.length()-1)))
		{
			x=code.length();
			while(Character.isDigit(code.charAt(--x))) {}
			maxLevel=s_int(code.substring(x+1));
		}
		Target t;
		try {
			t=Target.valueOf(code);
		} catch (Exception e) {
			t=Target.OFF;
		}
		if( t==null) t=Target.OFF;
		return new Conf(t,maxLevel,maxNumberOfLogs,maxNumberOfEntries,maxNumberOfBytes);
	}
	
	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param type LogType
	 * @return PrintWriter the writer
	 */
	private final Conf getConfig(final Type type)
	{
		Conf t=CONFS.get(type);
		if(t==null)
		{
			t=makeConfig(type, System.getProperty("LOG."+LOGNAME+"_"+type.toString().toUpperCase().trim()));
			CONFS.put(type,t);
		}
		return t;
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param type LogType
	 * @return PrintWriter the writer
	 */
	private final Target getTarget(final Type type)
	{
		return getConfig(type).target;
	}

	/**
	* Reset all of the log files
	* ON, OFF, FILE, BOTH
	* <br><br><b>Usage:</b>  CMProps.initLog(Log.LogType.info,"ON");
	* @param type the log to set the code for
	* @param code the code
	*/
	public final void configureLog(final Type type, final String code)
	{
		System.setProperty("LOG."+LOGNAME+"_"+type.toString().toUpperCase(),code.toUpperCase().trim());
		CONFS.put(type,makeConfig(type, code));
	}

	/**
	* Start all of the log files in the info temp directory
	*
	* <br><br><b>Usage:</b>  startLogFiles("mud",5);
	* @param newLogName maximum name of files
	* @param numberOfLogs maximum number of files
	*/
	public final void startLogging(final String newLogName, final int numberOfLogs)
	{
		// ===== pass in a null to force the temp directory
		startLogging(newLogName, "", numberOfLogs);
	}

	private final FileOutputStream startLogFile(String logName, final int numberOfLogs) throws IOException
	{
		File directoryPath;

		if ((filePath!=null)&&(filePath.length()!=0))
		{
			directoryPath = new File(filePath);
		}
		else
		{
			directoryPath = new File(".");
		}
		
		try
		{

			if ((!directoryPath.isDirectory())||(!directoryPath.canWrite())||(!directoryPath.canRead()))
			{
				directoryPath = null;
			}
		}
		catch(Exception t)
		{
			directoryPath=null;
		}

		if(logName.toLowerCase().endsWith(".log")||logName.toLowerCase().endsWith(".txt"))
			logName=logName.substring(0,logName.length()-4);
		
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
		FileOutputStream fileStream=new FileOutputStream(fileOut);
		if(logName.equalsIgnoreCase(this.logName))
			System.setErr(new PrintStream(fileStream));
		return fileStream;
	}
	
	/**
	* Start all of the log files in the specified directory
	*
	* <br><br><b>Usage:</b>  startLogFiles("mud","",10);
	* @param newLogName the name to create the file
	* @param dirPath the place to create the file
	* @param numberOfLogs maximum number of files
	*/
	public final void startLogging(final String newLogName, final String dirPath, final int numberOfLogs)
	{
		this.filePath=dirPath;
		this.logName=newLogName;
		this.LOGNAME=logName.toUpperCase().trim();
		this.numberOfFWLogs=numberOfLogs;
		try
		{
			final FileOutputStream fileStream=startLogFile(newLogName,numberOfLogs);
			fileOutWriter=new PrintWriter[]{new PrintWriter(fileStream,true)};
			System.setErr(new PrintStream(fileStream));
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
			standardOut(Type.error,"Log",e.getMessage(),Integer.MIN_VALUE);
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
						standardOut(Type.error,"Log",e.getMessage(),Integer.MIN_VALUE);
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
	 * @param type type of information
	 * @param module the module name
	 * @param message the message to print
	* @return String the header and message, formatted
	*/
	private final String makeLogEntry(final Type type, final String module, final String message)
	{
		final StringBuilder header=new StringBuilder(dateFormat.format(Long.valueOf(System.currentTimeMillis()))).append(" ");
		header.append(type.getSixChars());
		if(module.length()>=14)
			header.append(module.substring(0,14));
		else
			header.append(module).append(SPACES15.substring(0,14-module.length()));
		header.append(" ").append(message);
		return header.toString();
	}

	public static final void infoOut(final String Out) { infoOut(Thread.currentThread().getName(),Out); }
	public static final void sysOut(final String Out){ infoOut(Out); }
	public static final void debugOut(final String Out){ debugOut(Thread.currentThread().getName(),Out); }
	public static final void errOut(final String Out){ errOut(Thread.currentThread().getName(),Out); }
	public static final void warnOut(final String Out){ warnOut(Thread.currentThread().getName(),Out); }
	public static final void helpOut(final String Out) { helpOut(Thread.currentThread().getName(),Out); }
	public static final void killsOut(final String Out) { killsOut(Thread.currentThread().getName(),Out); }
	public static final void combatOut(final String Out) { combatOut(Thread.currentThread().getName(),Out); }
	public static final void accessOut(final String Out) { accessOut(Thread.currentThread().getName(),Out); }
	public static final void sysOut(final String Module, final String Message){ infoOut(Module,Message);}
	public static final void infoOut(final String Module, final String Message){ l().standardOut(Type.info,Module,Message,Integer.MIN_VALUE);}
	public static final void errOut(final String Module, final String Message){ l().standardOut(Type.error,Module,Message,Integer.MIN_VALUE);}
	public static final void warnOut(final String Module, final String Message){ l().standardOut(Type.warning,Module,Message,Integer.MIN_VALUE);}
	public static final void debugOut(final String Module, final String Message){ l().standardOut(Type.debug,Module,Message,Integer.MIN_VALUE);}
	public static final void helpOut(final String Module, final String Message){ l().standardOut(Type.help,Module,Message,Integer.MIN_VALUE);}
	public static final void killsOut(final String Module, final String Message){ l().standardOut(Type.kills,Module,Message,Integer.MIN_VALUE);}
	public static final void combatOut(final String Module, final String Message){ l().standardOut(Type.combat,Module,Message,Integer.MIN_VALUE);}
	public static final void accessOut(final String Module, final String Message){ l().standardOut(Type.access,Module,Message,Integer.MIN_VALUE);}
	public static final void debugOut(final String Module, final Throwable e){ l().shortExOut(Type.debug,Module,Integer.MIN_VALUE,e);}
	public static final void errOut(final String Module, final Throwable e){ l().standardExOut(Type.error,Module,Integer.MIN_VALUE,e);}
	public static final void warnOut(final String Module, final Throwable e){ l().standardExOut(Type.error,Module,Integer.MIN_VALUE,e);}
	public static final void rawSysOut(final String Message){l().rawStandardOut(Type.info,Message,Integer.MIN_VALUE);}
	public static final void infoOut(final String Out, final int priority) { infoOut(Thread.currentThread().getName(),Out,priority); }
	public static final void sysOut(final String Out, final int priority){ infoOut(Out,priority); }
	public static final void debugOut(final String Out, final int priority){ debugOut(Thread.currentThread().getName(),Out,priority); }
	public static final void errOut(final String Out, final int priority){ errOut(Thread.currentThread().getName(),Out,priority); }
	public static final void warnOut(final String Out, final int priority){ warnOut(Thread.currentThread().getName(),Out,priority); }
	public static final void helpOut(final String Out, final int priority) { helpOut(Thread.currentThread().getName(),Out,priority); }
	public static final void killsOut(final String Out, final int priority) { killsOut(Thread.currentThread().getName(),Out,priority); }
	public static final void combatOut(final String Out, final int priority) { combatOut(Thread.currentThread().getName(),Out,priority); }
	public static final void infoOut(final String Module, final String Message, final int priority){ l().standardOut(Type.info,Module,Message,priority);}
	public static final void sysOut(final String Out, final String Message, final int priority){ infoOut(Out,Message);}
	public static final void errOut(final String Module, final String Message, final int priority){ l().standardOut(Type.error,Module,Message,priority);}
	public static final void warnOut(final String Module, final String Message, final int priority){ l().standardOut(Type.warning,Module,Message,priority);}
	public static final void debugOut(final String Module, final String Message, final int priority){ l().standardOut(Type.debug,Module,Message,priority);}
	public static final void helpOut(final String Module, final String Message, final int priority){ l().standardOut(Type.help,Module,Message,priority);}
	public static final void killsOut(final String Module, final String Message, final int priority){ l().standardOut(Type.kills,Module,Message,priority);}
	public static final void combatOut(final String Module, final String Message, final int priority){ l().standardOut(Type.combat,Module,Message,priority);}
	public static final void accessOut(final String Module, final String Message, final int priority){ l().standardOut(Type.access,Module,Message,priority);}
	public static final void debugOut(final String Module, final int priority, final Exception e){ l().shortExOut(Type.debug,Module,priority,e);}
	public static final void errOut(final String Module, final int priority, final Throwable e){ l().standardExOut(Type.error,Module,priority,e);}
	public static final void warnOut(final String Module, final int priority, final Throwable e){ l().standardExOut(Type.error,Module,priority,e);}
	public static final void rawSysOut(final String Message, final int priority){l().rawStandardOut(Type.info,Message,priority);}

	/**
	* Handles long exception logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> standardExOut(Thread.currentThread().getName(),Out);
	* @param type The channel to print to
	* @param Module The module to print
	* @param e	The exception whose string one wishes to print
	*/
	public final void standardExOut(final Type type, final String Module, final int priority, final Throwable e)
	{
		final Conf conf=getConfig(type);
		PrintWriter outWriter=getWriter(type,conf,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
				final String msg=(e!=null)?e.getMessage():"Null/Unknown error occurred.";
				final String firstLine=makeLogEntry(type,Module,msg);
				outWriter=writeBytes(type,conf,outWriter, firstLine);
				if(conf.target==Target.BOTH)
					System.out.println(firstLine);
				if(e!=null)
				{
					e.printStackTrace(outWriter);
					if(conf.target==Target.BOTH)
						e.printStackTrace(System.out);
				}
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
	public final void shortExOut(final Type type, final String Module, final int priority, final Throwable e)
	{
		final Conf conf=getConfig(type);
		final PrintWriter outWriter=getWriter(type,conf,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
				final String line=makeLogEntry(type,Module,e.getMessage());
				writeBytes(type, conf, outWriter, line);
				e.printStackTrace(outWriter);
				if(conf.target==Target.BOTH)
				{
					System.out.println(line);
					e.printStackTrace(System.out);
					System.out.flush();
				}
			}
		}
	}

	/**
	* Handles raw info logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> rawStandardOut(LogType.info,"REQ-OUT:"+REQ);
	* @param type The type of message
	* @param line The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	public final void rawStandardOut(final Type type, final String line, final int priority)
	{
		final Conf conf=getConfig(type);
		final PrintWriter outWriter=getWriter(type,conf,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
				writeBytes(type, conf, outWriter, line);
				if(conf.target==Target.BOTH)
					System.out.println(line);
			}
		}
	}

	/**
	* Handles debug logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> standardOut(LogType.info,Thread.currentThread().getName(),Out);
	* @param type The type of writer
	* @param module The file name
	* @param msg The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	private final void standardOut(final Type type, final String module, final String msg, final int priority)
	{
		final Conf conf=getConfig(type);
		final PrintWriter outWriter=getWriter(type,conf,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
				final String line=makeLogEntry(type,module,msg);
				writeBytes(type, conf, outWriter, line);
				if(conf.target==Target.BOTH)
					System.out.println(line);
			}
		}
	}

	/**
	* Handles debug timing entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* <br><br><b>Usage:</b> timeOut(LogType.info,Thread.currentThread().getName(),Out);
	* @param type Channel name
	* @param module The file name
	* @param msg The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	public final void timeOut(final Type type, final String module, final String msg, final int priority)
	{
		final Conf conf=getConfig(type);
		final PrintWriter outWriter=getWriter(type,conf,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
				final Calendar C=Calendar.getInstance();
				final String line=makeLogEntry(type,module,C.get(Calendar.MINUTE)+":"+C.get(Calendar.SECOND)+":"+C.get(Calendar.MILLISECOND)+": "+msg);
				writeBytes(type, conf, outWriter, line);
				if(getTarget(type)==Target.BOTH)
					System.out.println(line);
			}
		}
	}

	/**
	 * Shut down this class forever
	 */
	public final void close()
	{
		fileOutWriter[0].close();
		fileOutWriter=null;
	}

	public static final boolean errorChannelOn() { return l().isWriterOn(Type.error);}
	public static final boolean helpChannelOn() { return l().isWriterOn(Type.help);}
	public static final boolean debugChannelOn() { return l().isWriterOn(Type.debug);}
	public static final boolean infoChannelOn() { return l().isWriterOn(Type.info);}
	public static final boolean warnChannelOn() { return l().isWriterOn(Type.warning);}
	public static final boolean killsChannelOn() { return l().isWriterOn(Type.kills);}
	public static final boolean combatChannelOn() { return l().isWriterOn(Type.combat);}
	public static final boolean accessChannelOn() { return l().isWriterOn(Type.access);}
	public static final boolean errorChannelAt(int priority) { final Log l=l(); return l.getWriter(Type.error,l.getConfig(Type.error),priority)!=null;}
	public static final boolean helpChannelAt(int priority) { final Log l=l(); return l.getWriter(Type.help,l.getConfig(Type.help),priority)!=null;}
	public static final boolean debugChannelAt(int priority) { final Log l=l(); return l.getWriter(Type.debug,l.getConfig(Type.debug),priority)!=null;}
	public static final boolean infoChannelAt(int priority) { final Log l=l(); return l.getWriter(Type.info,l.getConfig(Type.info),priority)!=null;}
	public static final boolean warnChannelAt(int priority) { final Log l=l(); return l.getWriter(Type.warning,l.getConfig(Type.warning),priority)!=null;}
	public static final boolean killsChannelAt(int priority) { final Log l=l(); return l.getWriter(Type.kills,l.getConfig(Type.kills),priority)!=null;}
	public static final boolean combatChannelAt(int priority) { final Log l=l(); return l.getWriter(Type.combat,l.getConfig(Type.combat),priority)!=null;}
	public static final boolean accessChannelAt(int priority) { final Log l=l(); return l.getWriter(Type.access,l.getConfig(Type.access),priority)!=null;}
	
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
		l().standardOut(Type.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
	//Log a method entry.
	@Override
	public void	entering(String sourceClass, String sourceMethod) 
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod, Integer.MIN_VALUE);
	}
	//Log a method entry, with one parameter.
	@Override
	public void	entering(String sourceClass, String sourceMethod, Object param1) 
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod+": "+param1, Integer.MIN_VALUE);
	}
	//Log a method entry, with an array of parameters.
	@Override
	public void	entering(String sourceClass, String sourceMethod, Object[] params) 
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod+": "+toStringList(params), Integer.MIN_VALUE);
	}
	//Log a method return.
	@Override
	public void	exiting(String sourceClass, String sourceMethod) 
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod, Integer.MIN_VALUE);
	}
	//Log a method return, with result object.
	@Override
	public void	exiting(String sourceClass, String sourceMethod, Object result) 
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod+": "+result, Integer.MIN_VALUE);
	}
	//Log a FINE message.
	@Override
	public void	fine(String msg) 
	{
		l().standardOut(Type.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
	//Log a FINER message.
	@Override
	public void	finer(String msg) 
	{
		l().standardOut(Type.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
	//Log a FINEST message.
	@Override
	public void	finest(String msg) 
	{
		l().standardOut(Type.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
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
		if(log.isWriterOn(Type.debug)) return Level.FINE;
		if(log.isWriterOn(Type.info)) return Level.INFO;
		if(log.isWriterOn(Type.warning)) return Level.WARNING;
		if(log.isWriterOn(Type.error)) return Level.SEVERE;
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
		standardOut(Type.info,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
	//Check if a message of the given level would actually be logged by this logger.
	@Override
	public boolean	isLoggable(Level level) 
	{
		if(level==Level.INFO) return this.isWriterOn(Type.info);
		else if(level==Level.SEVERE)  return this.isWriterOn(Type.error);
		else if(level==Level.WARNING)  return this.isWriterOn(Type.warning);
		else  return this.isWriterOn(Type.debug);
	}
	//Log a message, with no arguments.
	@Override
	public void	log(Level level, String msg) 
	{
		if(level==Level.INFO) standardOut(Type.info,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(Type.error,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(Type.warning,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
		else standardOut(Type.debug,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
	//Log a message, with one object parameter.
	@Override
	public void	log(Level level, String msg, Object param1) 
	{
		String oStr=(param1==null)?"null":param1.toString();
		if(level==Level.INFO) standardOut(Type.info,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(Type.error,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(Type.warning,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else standardOut(Type.debug,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
	}
	//Log a message, with an array of object arguments.
	@Override
	public void	log(Level level, String msg, Object[] params) 
	{
		String oStr=toStringList(params);
		if(level==Level.INFO) standardOut(Type.info,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(Type.error,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(Type.warning,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
		else standardOut(Type.debug,Thread.currentThread().getName(),msg+": "+oStr,Integer.MIN_VALUE);
	}
	//Log a message, with associated Throwable information.
	@Override
	public void	log(Level level, String msg, Throwable thrown) 
	{
		if(thrown==null) log(level,msg);
		else if(level==Level.INFO) standardExOut(Type.info,toModuleName(msg),Integer.MIN_VALUE,thrown);
		else if(level==Level.SEVERE) standardExOut(Type.error,toModuleName(msg),Integer.MIN_VALUE,thrown);
		else if(level==Level.WARNING) standardExOut(Type.warning,toModuleName(msg),Integer.MIN_VALUE,thrown);
		else shortExOut(Type.debug,toModuleName(msg),Integer.MIN_VALUE,thrown);
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
		if(level==Level.INFO) standardOut(Type.info,toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(Type.error,toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(Type.warning,toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
		else standardOut(Type.debug,toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
	}
	//Log a message, specifying source class and method, with a single object parameter to the log message.
	@Override
	public void	logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) 
	{
		String oStr=(param1==null)?"null":param1.toString();
		if(level==Level.INFO) standardOut(Type.info,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(Type.error,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(Type.warning,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else standardOut(Type.debug,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
	}
	//Log a message, specifying source class and method, with an array of object arguments.
	@Override
	public void	logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) 
	{
		String oStr=toStringList(params);
		if(level==Level.INFO) standardOut(Type.info,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.SEVERE) standardOut(Type.error,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else if(level==Level.WARNING) standardOut(Type.warning,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
		else standardOut(Type.debug,toModuleName(sourceClass),sourceMethod+": "+msg+": "+oStr,Integer.MIN_VALUE);
	}
	//Log a message, specifying source class and method, with associated Throwable information.
	@Override
	public void	logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) 
	{
		if(thrown==null) log(level,msg);
		else if(level==Level.INFO) standardExOut(Type.info,toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
		else if(level==Level.SEVERE) standardExOut(Type.error,toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
		else if(level==Level.WARNING) standardExOut(Type.warning,toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
		else shortExOut(Type.debug,toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
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
		standardOut(Type.error,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
	//Log throwing an exception.
	@Override
	public void	throwing(String sourceClass, String sourceMethod, Throwable thrown) 
	{
		standardExOut(Type.error, toModuleName(sourceClass), Integer.MIN_VALUE, thrown);
	}
	//Log a WARNING message.
	@Override
	public void	warning(String msg) 
	{
		standardOut(Type.warning,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
}
