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
   Copyright 2000-2018 Bo Zimmerman

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
	private static final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd.HHmm.ss");

	/** SPACES for headers */
	private static final String SPACES="                                                                                                ";
	private static final String SPACES15		 = SPACES.substring(0,15);
	private PrintWriter 		fileOutWriter[]	 = null; /**	always to "log" */
	private int					numberOfFWLogs	 = 1;
	private final PrintWriter 	systemOutWriter	 = new PrintWriter(System.out,true); /** always to systemout */
	private File 				logPath 		 = new File("."); /** The fully qualified file path */
	private String 				logName 		 = "application"; /** log name */
	private String 				LOGNAME 		 = "APPLICATION"; /** log name */
	private static final Log[] 	logs			 = new Log[256];
	private final Map<Type,Conf>CONFS			 = new Hashtable<Type,Conf>();
	private final Map<PrintWriter,long[]> WRITTEN= new Hashtable<PrintWriter,long[]>();
	private static final Type[] TYPE_LEVEL_MAP   = new Type[1001];

	/**
	 * The internally used targets for a log
	 * @author Bo Zimmerman
	 */
	private static enum Target { ON, OFF, BOTH, FILE, OWNFILE }

	/**
	 * Internal configuration object.
	 * @author Bo Zimmerman
	 */
	private static class Conf
	{
		public final Target target;
		public int maxLevel, maxLogs, maxLines, maxBytes;
		public final PrintWriter[][] writers = new PrintWriter[10][];

		public Conf(Target target, int maxLevel, int maxLogs, int maxLines, int maxBytes)
		{ 
			this.target=target; 
			this.maxLevel=maxLevel; 
			this.maxLogs=maxLogs; 
			this.maxLines=maxLines; 
			this.maxBytes=maxBytes;
		}
	}

	/**
	 * The various log types supported by this logger.  When using the native interface, think of them
	 * as separate channels that can be independenly directed.  When using the java Logger, they are
	 * arbitrarily assigned.  Typically info, warning, debug, and error are most used, with the others
	 * being for special cases.
	 * @author Bo Zimmerman
	 *
	 */
	public static enum Type 
	{ 
		error, 
		help, 
		debug, 
		info, 
		warning, 
		kills, 
		combat, 
		access;
		final String sixChars;
		
		private Type() 
		{ 
			sixChars=(this.toString()+SPACES).substring(0,5)+" "; 
		}
		
		public final String getSixChars() 
		{ 
			return sixChars; 
		}
	}

	static
	{
		for(int i=0;i<TYPE_LEVEL_MAP.length;i++)
			if(i<=Level.FINEST.intValue())
				TYPE_LEVEL_MAP[i]=Type.debug;
			else if(i<=Level.FINER.intValue()) TYPE_LEVEL_MAP[i]=Type.debug;
			else if(i<=Level.FINE.intValue()) TYPE_LEVEL_MAP[i]=Type.access;
			else if(i<=Level.CONFIG.intValue()) TYPE_LEVEL_MAP[i]=Type.info;
			else if(i<=Level.INFO.intValue()) TYPE_LEVEL_MAP[i]=Type.info;
			else if(i<=Level.WARNING.intValue()) TYPE_LEVEL_MAP[i]=Type.warning;
			else if(i<=Level.SEVERE.intValue()) TYPE_LEVEL_MAP[i]=Type.error;
	}

	/**
	 * Constructor for a log object.  Will assign the constructed logger to the 
	 * current thread group by taking the first character of the thread group
	 * name as its code.  If all threadgroups have same first character, they will
	 * all share a log object.  Either way, this needs to be called to construct
	 * the first logger.
	 */
	public Log()
	{
		super("log",null);
		final char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(logs[threadCode]==null)
			logs[threadCode]=this;
	}
	
	/**
	 * Returns the log object for the current threadgroup, or null if unassigned.
	 * @return the Log object, or null
	 */
	private static final Log l()
	{ 
		return logs[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}
	
	/**
	 * Returns the log object for the given threadgroup code, or null if unassigned.
	 * @param threadCode the threadgroup code to check
	 * @return the Log object, or null
	 */
	public static final Log l(char threadCode)
	{
		return logs[threadCode];
	}

	/**
	 * Returns a Log object for the current thread group.  If one is not assigned,
	 * it will be instantiated, thus guaranteeing that a Log object always returns
	 * from this method.
	 * @return a Log object
	 */
	public static final Log instance()
	{
		Log log=l();
		if(log==null)
			log=new Log();
		return log;
	}
	
	/**
	 * Forces the creation of a new, unconfigured Log object for the current thread group.
	 * @return the new Log object
	 */
	public static final Log newInstance()
	{
		final char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		logs[threadCode]=new Log();
		return l();
	}
	
	/**
	 * Forces the current thread group to share a Log object with the one at the given
	 * threadcode.  The one at the threadcode should already have been created before
	 * calling.
	 * @param threadCode the threadcode with an existing log
	 */
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
	 * Usage: if(isMaskedErrMsg(errException.getMessage()))
	 * @param str the message
	 * @return boolean TRUE if masked out.
	 */
	public static final boolean isMaskedErrMsg(final String str)
	{
		if(str==null)
			return false;
		final String upstr=str.toLowerCase();
		for (final String maskErrMsg : maskErrMsgs)
		{
			if(upstr.indexOf(maskErrMsg)>=0)
				return true;
		}
		return false;
	}

	/**
	 * Returns the integer value of a string without exception
 	 *
	 * Usage: int num=s_int(CMD.substring(14));
	 * @param INT Integer value of string
	 * @return int Integer value of the string
	 */
	private static final int s_int(final String INT)
	{
		try{ return Integer.parseInt(INT); }
		catch(final java.lang.NumberFormatException e){ return 0;}
	}

	/**
	 * Given a local logging type, returns true if the writer for that
	 * type is on.
	 * @param type the local logging type
	 * @return true if on, false otherwise
	 */
	private final boolean isWriterOn(final Type type)
	{
		return getTarget(type) != Target.OFF;
	}

	/**
	 * Given a log type, return the default log file name or null.
	 * @param type the local log type
	 * @return the name of the default filename, or null is NA
	 */
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

	/**
	 * When a log is determined to be full, this method closes out the old one,
	 * opens the new one, and designates the new one as ready to be written to
	 * @param writer the writer to close
	 * @return the new writer
	 * @throws IOException typically some sort of file creation exception
	 */
	private final PrintWriter rollLog(final PrintWriter writer) throws IOException
	{
		if(writer==this.fileOutWriter[0])
		{
			WRITTEN.remove(writer);
			writer.close();
			final FileOutputStream fileOut=this.openLogFile(logName, false, this.numberOfFWLogs);
			final PrintWriter newPW=new PrintWriter(fileOut,true);
			this.fileOutWriter[0]=newPW;
			WRITTEN.put(newPW, new long[]{0,0});
			return newPW;
		}
		else
		{
			for(final Type type : Type.values())
			{
				final Conf config=getConfig(type);
				if((config.target==Target.OWNFILE)&&(config.writers[0][0]==writer))
				{
					WRITTEN.remove(writer);
					writer.close();
					final FileOutputStream fileOut=this.openLogFile(getLogFilename(type), false, config.maxLogs);
					final PrintWriter newPW=new PrintWriter(fileOut,true);
					config.writers[0][0]=newPW;
					WRITTEN.put(newPW, new long[]{0,0});
					return newPW;
				}
			}
		}
		return writer;
	}

	/**
	 * Writes the given full log string to the given writer.  If the number of lines in the
	 * given config warrants a log-role, then it is done.
	 * @param config the configuration being worked with
	 * @param writer the writer to write to
	 * @param str the log message
	 * @return the new writer, if there is one, or the current writer
	 */
	private final PrintWriter writeBytes(final Conf config, final PrintWriter writer, final String str)
	{
		try
		{
			writer.println(str);
			long[] counts=this.WRITTEN.get(writer);
			if(counts == null)
			{
				counts=new long[2];
				this.WRITTEN.put(writer, counts);
			}
			counts[0]++;
			counts[1]+=str.length()+1;
			if(((config.maxLines>0)&&(counts[0]>=config.maxLines))
			||((config.maxBytes>0)&&(counts[1]>=config.maxBytes)))
				return rollLog(writer);
		}
		catch(final IOException e)
		{
		}
		return writer;
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * Usage: PrintWriter W=getWriter("BOTH");
	 * @param type log type
	 * @param config log config
	 * @return PrintWriter the writer
	 */
	private final PrintWriter getWriter(final Type type, final Conf config, final int priority)
	{
		final PrintWriter[][] writers=config.writers;
		if(writers[0]!=null)
		{
			if(priority<0)
				return writers[0][0];
			if(priority>9)
				return writers[9][0];
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
					final FileOutputStream fileStream=this.openLogFile(logName+"_"+type.toString().toLowerCase(), false, config.maxLogs);
					final PrintWriter[] writer=new PrintWriter[]{new PrintWriter(fileStream,true)};
					for(int i=0;i<10;i++)
						writers[i]=writer;
					if(!WRITTEN.containsKey(writer))
						WRITTEN.put(writer[0], new long[]{0,0});
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		final int max=config.maxLevel;
		final PrintWriter[] empty=new PrintWriter[]{null};
		for(int i=max+1;i<10;i++)
			writers[i]=empty;
		if((this.fileOutWriter[0]!=null) && (!WRITTEN.containsKey(fileOutWriter[0])))
			WRITTEN.put(fileOutWriter[0], new long[2]);
		if(priority<0)
			return writers[0][0];
		if(priority>9)
			return writers[9][0];
		return writers[priority][0];
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * Usage: PrintWriter W=makeConfig("BOTH");
	 * @param type LogType
	 * @param code ON, OFF, BOTH, OWNFILE, FILE
	 * @param defaultNumberOfLogs default number of log files
	 * @return Conf the config
	 */
	private final Conf makeConfig(final Type type, String code, int defaultNumberOfLogs)
	{
		if((code==null)||(code.trim().length()==0))
			return new Conf(Target.OFF,0,1,0,0);
		int maxNumberOfLogs=defaultNumberOfLogs;
		int maxNumberOfBytes=0;
		int maxNumberOfEntries=0;
		code=code.trim().toUpperCase();
		int x;
		if(Character.isDigit(code.charAt(0)))
		{
			x=0;
			while((x<code.length())&&(Character.isDigit(code.charAt(x)))) {x++;}
			maxNumberOfLogs=s_int(code.substring(0,x));
			code=code.substring(x).trim();
		}
		int maxLevel=0;
		x=code.indexOf('<');
		if(x>0)
		{
			String numStr=code.substring(x+1).trim();
			code=code.substring(0,x).trim();
			if(Character.isDigit(numStr.charAt(0)))
			{
				x=0;
				while((x<numStr.length())&&(Character.isDigit(numStr.charAt(x)))) 
				{
					x++;
				}
				final int num=s_int(numStr.substring(0,x));
				numStr=numStr.substring(x).trim().toLowerCase();
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
			}
		}
		if(Character.isDigit(code.charAt(code.length()-1)))
		{
			x=code.length();
			while(Character.isDigit(code.charAt(--x))) {}
			maxLevel=s_int(code.substring(x+1));
		}
		Target t;
		try
		{
			t=Target.valueOf(code);
		}
		catch (final Exception e)
		{
			t=Target.OFF;
		}
		if( t==null)
			t=Target.OFF;
		if( t==Target.OWNFILE )
			return new Conf(t,maxLevel,maxNumberOfLogs,maxNumberOfEntries,maxNumberOfBytes);
		else
			return new Conf(t,maxLevel,this.numberOfFWLogs,0,0);
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * Usage: PrintWriter W=getWriter("BOTH");
	 * @param type LogType
	 * @return PrintWriter the writer
	 */
	private final Conf getConfig(final Type type)
	{
		Conf t=CONFS.get(type);
		if(t==null)
		{
			t=makeConfig(type, System.getProperty("LOG."+LOGNAME+"_"+type.toString().toUpperCase().trim()),1);
			CONFS.put(type,t);
		}
		return t;
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
 	 *
	 * Usage: PrintWriter W=getWriter("BOTH");
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
	* Usage:  CMProps.initLog(Log.LogType.info,"ON");
	* @param type the log to set the code for
	* @param code the code
	*/
	public final void configureLog(final Type type, final String code)
	{
		System.setProperty("LOG."+LOGNAME+"_"+type.toString().toUpperCase(),code.toUpperCase().trim());
		CONFS.put(type,makeConfig(type, code, 1));
	}

	/**
	* Reset all of the log files
	* ON, OFF, FILE, BOTH
	* Usage:  CMProps.initLog(Log.LogType.info,"OWNFILE",20);
	* @param type the log to set the code for
	* @param code the code
	* @param numberOfLogs if code = "OWNFILE", then how many back logs to keep
	*/
	public final void configureLog(final Type type, final String code, final int numberOfLogs)
	{
		System.setProperty("LOG."+LOGNAME+"_"+type.toString().toUpperCase(),code.toUpperCase().trim());
		CONFS.put(type,makeConfig(type, code, numberOfLogs));
	}

	private final FileOutputStream openLogFile(String logName, final boolean append, final int numberOfLogs) throws IOException
	{
		if(logName.toLowerCase().endsWith(".log")||logName.toLowerCase().endsWith(".txt"))
			logName=logName.substring(0,logName.length()-4);

		// initializes the logging objects
		if((numberOfLogs>1) && (!append))
		{
			try
			{
				final String name=logName+(numberOfLogs-1)+".log";
				final File f=new File(logPath,name);
				if(f.exists())
					f.delete();
			}
			catch(final Exception e)
			{
			}
			
			for(int i=numberOfLogs-1;i>0;i--)
			{
				final String inum=(i>0)?(""+i):"";
				final String inumm1=(i>1)?(""+(i-1)):"";
				try
				{
					final File f=new File(logPath,logName+inumm1+".log");
					if(f.exists())
						f.renameTo(new File(logPath,logName+inum+".log"));
				}
				catch(final Exception e)
				{
				}
			}
		}
		final String name=logName+".log";
		final File fileOut=new File(logPath,name);
		final FileOutputStream fileStream=new FileOutputStream(fileOut, append);
		if(logName.equalsIgnoreCase(this.logName))
			System.setErr(new PrintStream(fileStream));
		return fileStream;
	}

	/**
	* Start all of the log files in the info temp directory
	*
	* Usage:  configureLogFile("mud",5);
	* @param logFilePath maximum name of files
	* @param numberOfLogs maximum number of files
	*/
	public final void configureLogFile(final String logFilePath, final int numberOfLogs)
	{
		if(logFilePath == null)
		{
			fileOutWriter=new PrintWriter[]{null};
			return;
		}
		final File F=new File(logFilePath);
		final File parentFile=F.getParentFile();
		if(parentFile!=null)
			this.logPath=parentFile;
		this.logName=F.getName();
		this.LOGNAME=logName.toUpperCase().trim();
		this.numberOfFWLogs=numberOfLogs;
		try
		{
			final FileOutputStream fileStream=openLogFile(logName,false,numberOfLogs);
			fileOutWriter=new PrintWriter[]{new PrintWriter(fileStream,true)};
			System.setErr(new PrintStream(fileStream));
		}
		catch(final IOException e)
		{
			System.out.println("NO OPEN LOG: "+e.toString());
		}
	}

	/**
	 * A rolling log reader interface for streaming in a log a line at a time.
	 * @author Bo Zimmerman
	 *
	 */
	public static interface LogReader
	{
		/**
		 * Returns the next full line of the log
		 * @return the next full line of the log
		 */
		public String nextLine();
		/**
		 * Tells the reader you are done.
		 */
		public void close();
	}

	/**
	 * Returns number of lines in the log file, if any.
	 * @return a number &gt;=0
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
			{ 
				line=reader.readLine(); 
				num++;
			}
			reader.close();
		}
		catch(final Exception e)
		{
			standardOut(Type.error,"Log",e.getMessage(),Integer.MIN_VALUE);
		}
		return num;
	}

	/**
	 * Returns an internally managed log reader class to make
	 * reading lines from the log slightly easier
	 * @return reader for the log file
	 */
	public final LogReader getLogReader()
	{
		return new LogReader()
		{
			BufferedReader reader = null;

			@Override
			public String nextLine()
			{
				if(reader==null)
				{
					try
					{
						final FileReader F=new FileReader(logName+".log");
						reader = new BufferedReader(F);
					}
					catch(final Exception e)
					{
						standardOut(Type.error,"Log",e.getMessage(),Integer.MIN_VALUE);
						return null;
					}
				}
				String line=null;
				try
				{
					if(reader.ready())
						line=reader.readLine();
				}
				catch ( final IOException ignore )
				{
				}
				if(line==null)
					close();
				return line;
			}

			@Override
			public void close()
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
		};
	}

	/**
	 * Returns the contents of the log file as a StringBuffer, if it exists
	 * @return the contents of the log file, or an empty stringbuffer
	 */
	public final StringBuffer getLog()
	{

		final StringBuffer buf=new StringBuffer("");

		final LogReader reader = getLogReader();
		String line;
		while((line = reader.nextLine()) != null)
		{
			buf.append(line+"\n\r");
		}
		return buf;
	}

	/**
	* Start all of the log files
	*
	* Usage:  path = getLogLocation();
	* @return the string representation of the file path
	*/
	public final String getLogLocation()
	{
		return logPath.getAbsolutePath();
	}

	/**
	* Will be used to create a standardized log header for file logs
 	*
	* Usage: SysOutWriter.println(getLogHeader(S,LogType.info,module, message));
	 * @param type type of information
	 * @param module the module name
	 * @param message the message to print
	* @return String the header and message, formatted
	*/
	public final static String makeLogEntry(final Type type, final String module, final String message)
	{
		final StringBuilder header=new StringBuilder(dateFormat.format(Long.valueOf(System.currentTimeMillis())));
		header.append(" ");
		header.append(type.getSixChars());
		if(module.length()>=14)
			header.append(module.substring(0,14));
		else
			header.append(module).append(SPACES15.substring(0,14-module.length()));
		header.append(" ").append(message);
		return header.toString();
	}

	/**
	* Sends the given message to the info channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void infoOut(final String message) 
	{ 
		infoOut(Thread.currentThread().getName(),message); 
	}

	/**
	* Sends the given message to the info channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void sysOut(final String message)
	{ 
		infoOut(message); 
	}

	/**
	* Sends the given message to the debug channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void debugOut(final String message)
	{ 
		debugOut(Thread.currentThread().getName(),message); 
	}

	/**
	* Sends the given message to the error channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void errOut(final String message)
	{ 
		errOut(Thread.currentThread().getName(),message); 
	}

	/**
	* Sends the given message to the warning channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void warnOut(final String message)
	{ 
		warnOut(Thread.currentThread().getName(),message); 
	}

	/**
	* Sends the given message to the help channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void helpOut(final String message) 
	{ 
		helpOut(Thread.currentThread().getName(),message); 
	}

	/**
	* Sends the given message to the killlog channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void killsOut(final String message) 
	{ 
		killsOut(Thread.currentThread().getName(),message); 
	}

	/**
	* Sends the given message to the combatlog channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void combatOut(final String message) 
	{ 
		combatOut(Thread.currentThread().getName(),message); 
	}

	/**
	* Sends the given message to the access channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param message The message to print
	*/
	public static final void accessOut(final String message) 
	{ 
		accessOut(Thread.currentThread().getName(),message); 
	}

	/**
	* Sends the given message to the info channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void sysOut(final String module, final String message)
	{ 
		infoOut(module,message);
	}

	/**
	* Sends the given message to the info channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void infoOut(final String module, final String message)
	{ 
		l().standardOut(Type.info,module,message,Integer.MIN_VALUE);
	}

	/**
	* Sends the given message to the error channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void errOut(final String module, final String message)
	{ 
		l().standardOut(Type.error,module,message,Integer.MIN_VALUE);
	}

	/**
	* Sends the given message to the warning channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void warnOut(final String module, final String message)
	{ 
		l().standardOut(Type.warning,module,message,Integer.MIN_VALUE);
	}

	/**
	* Sends the given message to the debug channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void debugOut(final String module, final String message)
	{ 
		l().standardOut(Type.debug,module,message,Integer.MIN_VALUE);
	}

	/**
	* Sends the given message to the help channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void helpOut(final String module, final String message)
	{ 
		l().standardOut(Type.help,module,message,Integer.MIN_VALUE);
	}

	/**
	* Sends the given message to the killlog channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void killsOut(final String module, final String message)
	{ 
		l().standardOut(Type.kills,module,message,Integer.MIN_VALUE);
	}

	/**
	* Sends the given message to the combat channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void combatOut(final String module, final String message)
	{ 
		l().standardOut(Type.combat,module,message,Integer.MIN_VALUE);
	}

	/**
	* Sends the given message to the access channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param message The message to print
	*/
	public static final void accessOut(final String module, final String message)
	{ 
		l().standardOut(Type.access,module,message,Integer.MIN_VALUE);
	}

	/**
	* Sends the given exception to the debug channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param e The exception to send out the stack and message of
	*/
	public static final void debugOut(final String module, final Throwable e)
	{ 
		l().shortExOut(Type.debug,module,Integer.MIN_VALUE,e);
	}

	/**
	* Sends the given exception to the error channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param e The exception to send out the stack and message of
	*/
	public static final void errOut(final String module, final Throwable e)
	{ 
		l().standardExOut(Type.error,module,Integer.MIN_VALUE,e);
	}

	/**
	* Sends the given exception to the warning channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no priority is given, priority is set to lowest possible.
	* @param module The module name to prefix the message with
	* @param e The exception to send out the stack and message of
	*/
	public static final void warnOut(final String module, final Throwable e)
	{ 
		l().standardExOut(Type.error,module,Integer.MIN_VALUE,e);
	}

	/**
	* Sends the given exception to the debug channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param e The exception to send out the stack and message of
	*/
	public static final void debugOut(final Throwable e)
	{ 
		l().shortExOut(Type.debug,Thread.currentThread().getName(),Integer.MIN_VALUE,e);
	}

	/**
	* Sends the given exception to the error channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param e The exception to send out the stack and message of
	*/
	public static final void errOut(final Throwable e)
	{ 
		l().standardExOut(Type.error,Thread.currentThread().getName(),Integer.MIN_VALUE,e);
	}
	
	/**
	* Sends the given exception to the warning channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
 	* Since no priority is given, priority is set to lowest possible.
	* @param e The exception to send out the stack and message of
	*/
	public static final void warnOut(final Throwable e)
	{ 
		l().standardExOut(Type.error,Thread.currentThread().getName(),Integer.MIN_VALUE,e);
	}
	
	/**
	 * Sends the given line to the info channel, if appropriate to do so,
	 * whether its to System.out, a file, both, or neither.
	 * No module, timestamp, nothing .. just raw OUT.
	 * Since no priority is given, the lowest possible is assigned.
	 * @param message the message to send exactly as-is.
	 */
	public static final void rawSysOut(final String message)
	{
		l().rawStandardOut(Type.info,message,Integer.MIN_VALUE);
	}
	
	/**
	* Sends the given message to the info channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void infoOut(final String message, final int priority) 
	{ 
		infoOut(Thread.currentThread().getName(),message,priority); 
	}
	
	/**
	* Sends the given message to the info channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void sysOut(final String message, final int priority)
	{ 
		infoOut(message,priority); 
	}
	
	/**
	* Sends the given message to the debug channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void debugOut(final String message, final int priority)
	{ 
		debugOut(Thread.currentThread().getName(),message,priority); 
	}
	
	/**
	* Sends the given message to the error channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void errOut(final String message, final int priority)
	{ 
		errOut(Thread.currentThread().getName(),message,priority); 
	}
	
	/**
	* Sends the given message to the warning channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void warnOut(final String message, final int priority)
	{ 
		warnOut(Thread.currentThread().getName(),message,priority); 
	}
	
	/**
	* Sends the given message to the help channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void helpOut(final String message, final int priority) 
	{ 
		helpOut(Thread.currentThread().getName(),message,priority); 
	}
	
	/**
	* Sends the given message to the killlog channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void killsOut(final String message, final int priority) 
	{ 
		killsOut(Thread.currentThread().getName(),message,priority); 
	}
	
	/**
	* Sends the given message to the combatlog channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
 	* Since no module name is given, the module name is set to the current thread name.
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void combatOut(final String message, final int priority) 
	{ 
		combatOut(Thread.currentThread().getName(),message,priority); 
	}
	
	/**
	* Sends the given message to the info channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void infoOut(final String module, final String message, final int priority)
	{ 
		l().standardOut(Type.info,module,message,priority);
	}
	
	/**
	* Sends the given message to the info channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void sysOut(final String module, final String message, final int priority)
	{ 
		infoOut(module,message);
	}
	
	/**
	* Sends the given message to the error channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void errOut(final String module, final String message, final int priority)
	{ 
		l().standardOut(Type.error,module,message,priority);
	}
	
	/**
	* Sends the given message to the warning channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void warnOut(final String module, final String message, final int priority)
	{ 
		l().standardOut(Type.warning,module,message,priority);
	}
	
	/**
	* Sends the given message to the debug channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void debugOut(final String module, final String message, final int priority)
	{ 
		l().standardOut(Type.debug,module,message,priority);
	}
	
	/**
	* Sends the given message to the help channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void helpOut(final String module, final String message, final int priority)
	{ 
		l().standardOut(Type.help,module,message,priority);
	}
	
	/**
	* Sends the given message to the killlog channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void killsOut(final String module, final String message, final int priority)
	{ 
		l().standardOut(Type.kills,module,message,priority);
	}
	
	/**
	* Sends the given message to the combatlog channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void combatOut(final String module, final String message, final int priority)
	{ 
		l().standardOut(Type.combat,module,message,priority);
	}
	
	/**
	* Sends the given message to the access channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param message the base message to send to the log
	* @param priority the priority level to give to this message
	*/
	public static final void accessOut(final String module, final String message, final int priority)
	{ 
		l().standardOut(Type.access,module,message,priority);
	}
	
	/**
	* Sends the given exception to the debug channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param e the exception to send the stack trace and message of
	* @param priority the priority level to give to this message
	*/
	public static final void debugOut(final String module, final int priority, final Exception e)
	{ 
		l().shortExOut(Type.debug,module,priority,e);
	}
	
	/**
	* Sends the given exception to the error channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param e the exception to send the stack trace and message of
	* @param priority the priority level to give to this message
	*/
	public static final void errOut(final String module, final int priority, final Throwable e)
	{ 
		l().standardExOut(Type.error,module,priority,e);
	}
	
	/**
	* Sends the given exception to the warning channel log, if appropriate to do so,
	* whether its to System.out, a file, both, or neither.
	* @param module the module name to prefix the main message with
	* @param e the exception to send the stack trace and message of
	* @param priority the priority level to give to this message
	*/
	public static final void warnOut(final String module, final int priority, final Throwable e)
	{ 
		l().standardExOut(Type.error,module,priority,e);
	}
	
	/**
	 * Sends the given line to the info channel, if appropriate to do so,
	 * whether its to System.out, a file, both, or neither.
	 * No module, timestamp, nothing .. just raw OUT.
	 * @param message the message to send exactly as-is.
	 * @param priority the priority to assign to this line of text
	 */
	public static final void rawSysOut(final String message, final int priority)
	{
		l().rawStandardOut(Type.info,message,priority);
	}

	/**
	* Handles long exception logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* Usage: standardExOut(Thread.currentThread().getName(),message);
	* @param type The channel to print to
	* @param module The module to print
	* @param priority the priority level to give to this message
	* @param e	The exception whose string one wishes to print
	*/
	public final void standardExOut(final Type type, final String module, final int priority, final Throwable e)
	{
		final Conf conf=getConfig(type);
		PrintWriter outWriter=getWriter(type,conf,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
				final String msg=(e!=null)?e.getMessage():"Null/Unknown error occurred.";
				final String firstLine=makeLogEntry(type,module,msg);
				outWriter=writeBytes(conf,outWriter, firstLine);
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
	* Usage: shortExOut(LogType.info,Thread.currentThread().getName(),message);
	* @param type The type of channel
	* @param module The message to print
	* @param priority the priority level to give to this message
	* @param e	The exception whose string one wishes to print
	*/
	public final void shortExOut(final Type type, final String module, final int priority, final Throwable e)
	{
		final Conf conf=getConfig(type);
		final PrintWriter outWriter=getWriter(type,conf,priority);
		if(outWriter!=null)
		{
			synchronized(outWriter)
			{
				final String line=makeLogEntry(type,module,e.getMessage());
				writeBytes( conf, outWriter, line);
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
	* Usage: rawStandardOut(LogType.info,"REQ-OUT:"+REQ);
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
				writeBytes(conf, outWriter, line);
				if(conf.target==Target.BOTH)
					System.out.println(line);
			}
		}
	}

	/**
	* Handles debug logging entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* Usage: standardOut(LogType.info,Thread.currentThread().getName(),message);
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
				writeBytes(conf, outWriter, line);
				if(conf.target==Target.BOTH)
					System.out.println(line);
			}
		}
	}

	/**
	* Handles debug timing entries.  Sends them to System.out,
	* the log file, or nowhere.
 	*
	* Usage: timeOut(LogType.info,Thread.currentThread().getName(),message);
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
				writeBytes(conf, outWriter, line);
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

	/**
	 * Returns whether error channel writer is allocated for current thread.
	 * @return whether error channel writer is allocated for current thread.
	 */
	public static final boolean errorChannelOn() 
	{ 
		return l().isWriterOn(Type.error);
	}
	
	/**
	 * Returns whether help channel writer is allocated for current thread.
	 * @return whether help channel writer is allocated for current thread.
	 */
	public static final boolean helpChannelOn() 
	{ 
		return l().isWriterOn(Type.help);
	}
	
	/**
	 * Returns whether debug channel writer is allocated for current thread.
	 * @return whether debug channel writer is allocated for current thread.
	 */
	public static final boolean debugChannelOn() 
	{ 
		return l().isWriterOn(Type.debug);
	}
	
	/**
	 * Returns whether info channel writer is allocated for current thread.
	 * @return whether info channel writer is allocated for current thread.
	 */
	public static final boolean infoChannelOn() 
	{ 
		return l().isWriterOn(Type.info);
	}
	
	/**
	 * Returns whether warning channel writer is allocated for current thread.
	 * @return whether warning channel writer is allocated for current thread.
	 */
	public static final boolean warnChannelOn() 
	{ 
		return l().isWriterOn(Type.warning);
	}
	
	/**
	 * Returns whether kill-log channel writer is allocated for current thread.
	 * @return whether kill-log channel writer is allocated for current thread.
	 */
	public static final boolean killsChannelOn() 
	{ 
		return l().isWriterOn(Type.kills);
	}
	
	/**
	 * Returns whether combat-log channel writer is allocated for current thread.
	 * @return whether combat-log channel writer is allocated for current thread.
	 */
	public static final boolean combatChannelOn() 
	{ 
		return l().isWriterOn(Type.combat);
	}
	
	/**
	 * Returns whether access-log channel writer is allocated for current thread.
	 * @return whether access-log channel writer is allocated for current thread.
	 */
	public static final boolean accessChannelOn() 
	{ 
		return l().isWriterOn(Type.access);
	}
	
	/**
	 * Returns whether error channel writer is allocated for current thread for given priority.
	 * @param priority priority level to check for writer at
	 * @return whether error channel writer is allocated for current thread for given priority.
	 */
	public static final boolean errorChannelAt(int priority) 
	{ 
		final Log l=l(); 
		return l.getWriter(Type.error,l.getConfig(Type.error),priority)!=null;
	}
	
	/**
	 * Returns whether help channel writer is allocated for current thread for given priority.
	 * @param priority priority level to check for writer at
	 * @return whether help channel writer is allocated for current thread for given priority.
	 */
	public static final boolean helpChannelAt(int priority) 
	{ 
		final Log l=l(); 
		return l.getWriter(Type.help,l.getConfig(Type.help),priority)!=null;
	}
	
	/**
	 * Returns whether debug channel writer is allocated for current thread for given priority.
	 * @param priority priority level to check for writer at
	 * @return whether debug channel writer is allocated for current thread for given priority.
	 */
	public static final boolean debugChannelAt(int priority) 
	{ 
		final Log l=l(); 
		return l.getWriter(Type.debug,l.getConfig(Type.debug),priority)!=null;
	}
	
	/**
	 * Returns whether info channel writer is allocated for current thread for given priority.
	 * @param priority priority level to check for writer at
	 * @return whether info channel writer is allocated for current thread for given priority.
	 */
	public static final boolean infoChannelAt(int priority) 
	{
		final Log l=l();
		return l.getWriter(Type.info,l.getConfig(Type.info),priority)!=null;
	}
	
	/**
	 * Returns whether warning channel writer is allocated for current thread for given priority.
	 * @param priority priority level to check for writer at
	 * @return whether warning channel writer is allocated for current thread for given priority.
	 */
	public static final boolean warnChannelAt(int priority) 
	{ 
		final Log l=l(); 
		return l.getWriter(Type.warning,l.getConfig(Type.warning),priority)!=null;
	}
	
	/**
	 * Returns whether kill-log channel writer is allocated for current thread for given priority.
	 * @param priority priority level to check for writer at
	 * @return whether kill-log channel writer is allocated for current thread for given priority.
	 */
	public static final boolean killsChannelAt(int priority) 
	{
		final Log l=l(); 
		return l.getWriter(Type.kills,l.getConfig(Type.kills),priority)!=null;
	}
	
	/**
	 * Returns whether combat-log channel writer is allocated for current thread for given priority.
	 * @param priority priority level to check for writer at
	 * @return whether combat-log channel writer is allocated for current thread for given priority.
	 */
	public static final boolean combatChannelAt(int priority) 
	{
		final Log l=l();
		return l.getWriter(Type.combat,l.getConfig(Type.combat),priority)!=null;
	}
	
	/**
	 * Returns whether access-log channel writer is allocated for current thread for given priority.
	 * @param priority priority level to check for writer at
	 * @return whether access-log channel writer is allocated for current thread for given priority.
	 */
	public static final boolean accessChannelAt(int priority) 
	{
		final Log l=l();
		return l.getWriter(Type.access,l.getConfig(Type.access),priority)!=null;
	}

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
	 * Combine an array of arbitrary objects to a comma-delimited list of strings
	 * @param os the objects
	 * @return the string
	 */
	private String toStringList(Object[] os)
	{
		if(os==null)
			return "";
		if(os.length==0)
			return "";

		final StringBuilder str=new StringBuilder((os[0]==null)?"null":os[0].toString());
		for(int i=1;i<os.length;i++)
			str.append(",").append(os[i]==null?"null":os[i].toString());
		return str.toString();
	}

	/**
	 * Returns either the part of the given string after a final '.', or the current thread name
	 * if no . available.
	 * @param sourceClass the string to look for a . in
	 * @return the module name
	 */
	private String toModuleName(String sourceClass)
	{
		if((sourceClass!=null)&&(sourceClass.length()>0)&&(sourceClass.indexOf('.')>=0))
			return sourceClass.substring(sourceClass.lastIndexOf('.')+1);
		else
			return Thread.currentThread().getName();
	}

	/**
	 * Given the log level from the java Log interface, return
	 * the local cmlog type.
	 * @param level the java Log level to translate
	 * @return the local log type
	 */
	private static final Type getTypeFromLevel(final Level level)
	{
		try
		{
			return TYPE_LEVEL_MAP[level.intValue()];
		}
		catch(final Throwable t)
		{
			if(level.intValue()<0)
				return Type.debug;
			else
				return Type.error;
		}
	}

	@Override
	public synchronized void addHandler(Handler handler)
	{
	}
	//Log a CONFIG message.
	@Override
	public void	config(final String msg)
	{
		l().standardOut(Type.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
	//Log a method entry.
	@Override
	public void	entering(final String sourceClass, final String sourceMethod)
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod, Integer.MIN_VALUE);
	}
	//Log a method entry, with one parameter.
	@Override
	public void	entering(final String sourceClass, final String sourceMethod, final Object param1)
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod+": "+param1, Integer.MIN_VALUE);
	}
	//Log a method entry, with an array of parameters.
	@Override
	public void	entering(final String sourceClass, final String sourceMethod, final Object[] params)
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod+": "+toStringList(params), Integer.MIN_VALUE);
	}
	//Log a method return.
	@Override
	public void	exiting(final String sourceClass, final String sourceMethod)
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod, Integer.MIN_VALUE);
	}
	//Log a method return, with result object.
	@Override
	public void	exiting(final String sourceClass, final String sourceMethod, final Object result)
	{
		l().standardOut(Type.debug, toModuleName(sourceClass), sourceMethod+": "+result, Integer.MIN_VALUE);
	}
	//Log a FINE message.
	@Override
	public void	fine(final String msg)
	{
		l().standardOut(Type.access, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
	//Log a FINER message.
	@Override
	public void	finer(final String msg)
	{
		l().standardOut(Type.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
	//Log a FINEST message.
	@Override
	public void	finest(final String msg)
	{
		l().standardOut(Type.debug, Thread.currentThread().getName(), msg, Integer.MIN_VALUE);
	}
	//Get the current filter for this Logger.
	@Override 
	public Filter	getFilter()
	{ 
		return null;
	}
	//Get the Handlers associated with this logger.
	@Override
	public synchronized Handler[]	getHandlers() 
	{ 
		return new Handler[0];
	}
	//Get the log Level that has been specified for this Logger.
	@Override
	public Level	getLevel()
	{
		final Log log=l();
		if(log.isWriterOn(Type.access))
			return Level.FINEST;
		if(log.isWriterOn(Type.debug))
			return Level.FINE;
		if(log.isWriterOn(Type.info))
			return Level.INFO;
		if(log.isWriterOn(Type.warning))
			return Level.WARNING;
		if(log.isWriterOn(Type.error))
			return Level.SEVERE;
		if(log.isWriterOn(Type.access))
			return Level.FINE;
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
	public Logger	getParent()
	{ 
		return null; 
	}
	//Retrieve the localization resource bundle for this logger for the current default locale.
	@Override
	public ResourceBundle	getResourceBundle() 
	{ 
		return null; 
	}
	//Retrieve the localization resource bundle name for this logger.
	@Override
	public String	getResourceBundleName()
	{ 
		return ""; 
	}
	//Discover whether or not this logger is sending its output to its parent logger.
	@Override
	public synchronized boolean	getUseParentHandlers()
	{ 
		return false; 
	}
	//Log an INFO message.
	@Override
	public void	info(final String msg)
	{
		standardOut(Type.info,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
	//Check if a message of the given level would actually be logged by this logger.
	@Override
	public boolean	isLoggable(final Level level)
	{
		return this.isWriterOn(getTypeFromLevel(level));
	}
	//Log a message, with no arguments.
	@Override
	public void	log(final Level level, final String msg)
	{
		standardOut(getTypeFromLevel(level),Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
	//Log a message, with one object parameter.
	@Override
	public void	log(final Level level, final String msg, final Object param1)
	{
		standardOut(getTypeFromLevel(level),Thread.currentThread().getName(),msg+": "+((param1==null)?"null":param1.toString()),Integer.MIN_VALUE);
	}
	//Log a message, with an array of object arguments.
	@Override
	public void	log(final Level level, final String msg, final Object[] params)
	{
		standardOut(getTypeFromLevel(level),Thread.currentThread().getName(),msg+": "+(toStringList(params)),Integer.MIN_VALUE);
	}
	//Log a message, with associated Throwable information.
	@Override
	public void	log(final Level level, final String msg, final Throwable thrown)
	{
		if(thrown==null)
			log(level,msg);
		else standardExOut(getTypeFromLevel(level),toModuleName(msg),Integer.MIN_VALUE,thrown);
	}
	//Log a LogRecord.
	@Override
	public void	log(final LogRecord record)
	{
		log(record.getLevel(), record.getMessage(), record.getThrown());
	}
	//Log a message, specifying source class and method, with no arguments.
	@Override
	public void	logp(final Level level, final String sourceClass, final String sourceMethod, final String msg)
	{
		standardOut(getTypeFromLevel(level),toModuleName(sourceClass),sourceMethod+": "+msg,Integer.MIN_VALUE);
	}
	//Log a message, specifying source class and method, with a single object parameter to the log message.
	@Override
	public void	logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object param1)
	{
		standardOut(getTypeFromLevel(level),toModuleName(sourceClass),sourceMethod+": "+msg+": "+((param1==null)?"null":param1.toString()),Integer.MIN_VALUE);
	}
	//Log a message, specifying source class and method, with an array of object arguments.
	@Override
	public void	logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Object[] params)
	{
		standardOut(getTypeFromLevel(level),toModuleName(sourceClass),sourceMethod+": "+msg+": "+(toStringList(params)),Integer.MIN_VALUE);
	}
	//Log a message, specifying source class and method, with associated Throwable information.
	@Override
	public void	logp(final Level level, final String sourceClass, final String sourceMethod, final String msg, final Throwable thrown)
	{
		if(thrown==null)
			log(level,msg);
		else standardExOut(getTypeFromLevel(level),toModuleName(sourceClass),Integer.MIN_VALUE,thrown);
	}
	//Log a message, specifying source class, method, and resource bundle name with no arguments.
	@Override
	@Deprecated
	public void	logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg)
	{
		logp(level,sourceClass,sourceMethod+": "+bundleName, msg);
	}
	//Log a message, specifying source class, method, and resource bundle name, with a single object parameter to the log message.
	@Override
	@Deprecated
	public void	logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg, Object param1)
	{
		logp(level,sourceClass,sourceMethod+": "+bundleName, msg, param1);
	}
	//Log a message, specifying source class, method, and resource bundle name, with an array of object arguments.
	@Override
	@Deprecated
	public void	logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg, Object[] params)
	{
		logp(level,sourceClass,sourceMethod+": "+bundleName, msg, params);
	}
	//Log a message, specifying source class, method, and resource bundle name, with associated Throwable information.
	@Override
	@Deprecated
	public void	logrb(final Level level, final String sourceClass, final String sourceMethod, final String bundleName, final String msg, final Throwable thrown)
	{
		logp(level,sourceClass,sourceMethod+": "+bundleName, msg, thrown);
	}
	//Remove a log Handler.
	@Override
	public synchronized void	removeHandler(Handler handler)
	{
	}
	//Set a filter to control output on this Logger.
	@Override
	public void	setFilter(Filter newFilter)
	{
	}
	//Set the log level specifying which message levels will be logged by this logger.
	@Override
	public void	setLevel(Level newLevel)
	{
	}
	//Set the parent for this Logger.
	@Override
	public void	setParent(Logger parent)
	{
	}
	//Specify whether or not this logger should send its output to it's parent Logger.
	@Override
	public synchronized void	setUseParentHandlers(boolean useParentHandlers)
	{
	} 
	//Log a SEVERE message.
	@Override
	public void	severe(final String msg)
	{
		standardOut(Type.error,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
	//Log throwing an exception.
	@Override
	public void	throwing(final String sourceClass, final String sourceMethod, final Throwable thrown)
	{
		standardExOut(Type.error, toModuleName(sourceClass), Integer.MIN_VALUE, thrown);
	}
	//Log a WARNING message.
	@Override
	public void	warning(final String msg)
	{
		standardOut(Type.warning,Thread.currentThread().getName(),msg,Integer.MIN_VALUE);
	}
}
