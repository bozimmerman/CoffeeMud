package com.planet_ink.coffee_mud.Libraries.intermud.cm1;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMRunnable;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.lang.reflect.Array;
import java.util.concurrent.atomic.*;

/*
   Copyright 2010-2026 Bo Zimmerman

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
public class RequestHandler implements CMRunnable
{
	private static AtomicInteger		  counter			  = new AtomicInteger();
	public  static final AtomicInteger	  blockSequence		  = new AtomicInteger();
	private final String				  runnableName;
	private final SocketChannel			  chan;
	private boolean						  isRunning			  = false;
	private boolean						  closeMe			  = false;
	private long						  idleTime			  = System.currentTimeMillis();
	private String						  user				  = null;
	private PhysicalAgent				  target			  = null;
	private byte[][]					  markBlocks		  = DEFAULT_MARK_BLOCKS;
	private long						  MAX_IDLE_MILLIS	  = 10 * 60 * 1000;
	private long						  startTime			  = 0;
	private volatile CommandMode		  commandMode			  = CommandMode.STANDARD;

	private final List<String>			  execCommands		  = new SLinkedList<String>();
	private final StringBuffer			  accumBuffer		  = new StringBuffer();
	private final List<?>				  results			  = new SLinkedList<Object>();
	private volatile Runnable			  runner			  = null;
	private final SLinkedList<ByteBuffer> workingBuffers	  = new SLinkedList<ByteBuffer>();
	private final Map<String, Object>	  dependents		  = new STreeMap<String, Object>();

	private static final int			  BUFFER_SIZE		  = 4096;
	private static final long			  MAXIMUM_BYTES		  = 1024 * 1024 * 10;
	private static final byte[][]		  DEFAULT_MARK_BLOCKS = { { '\n', '\r' }, { '\r', '\n' }, { '\n' }, { '\r' } };
	private static final char[]			  DEFAULT_CRLF		  = { '\n', '\r' };
	
	/**
	 * The settings for how commands are sent/received.
	 * 
	 * @author Cygnus
	 *
	 */
	public static enum CommandMode
	{
		/**
		 * Standard command mode uses single command words with space-delimited arguments and CR EOL, unless
		 * the BLOCK command is used to set a new EOL.  Responses in []
		 */
		STANDARD,
		/**
		 * XML commands are sent with xml document where tag is command name and inner value(s) or VALUE tags
		 * are arguments.  Responses are xml doc where tag is response type and inner values response message.
		 */
		XML,
		/**
		 * JSON commands are sent with json document where command name in 'command', and arguments is a 
		 * string or string array.  Response are json doc with 'status' as response status and values in 
		 * 'message' key.
		 */
		JSON
	}
	
	/**
	 * Valid server response statuses
	 * @author Cygnus
	 *
	 */
	public static enum Status
	{
		/**
		 * Command successful, output forthcoming
		 */
		OK,
		/**
		 * Command failed, reasons forthcoming
		 */
		FAIL,
		/**
		 * Unprompted message forthcoming
		 */
		MESSAGE,
		/**
		 * Command successful, block output forthcoming
		 */
		BLOCK
	}

	@Override
	public long activeTimeMillis()
	{
		return (startTime > 0) ? System.currentTimeMillis() - startTime : 0;
	}

	public RequestHandler(final SocketChannel chan, final int maxIdleMillis) throws IOException
	{
		super();
		runnableName = "CM1ReqHndler#" + counter.incrementAndGet();
		if (maxIdleMillis > 0)
			MAX_IDLE_MILLIS = ((long) maxIdleMillis) * 60 * 1000;
		this.chan = chan;
	}


	/**
	 * Converts a pojo field to a standard string response value.
	 * @param type the class type
	 * @param val the value
	 * @param allowCR true to allow CRLF, false to strip them
	 * @return the standard string response value
	 */
	public String fromPOJOFieldToStandardString(final Class<?> type, final Object val, final boolean allowCR)
	{
		final StringBuilder str=new StringBuilder("");
		if(type.isArray())
		{
			final int length = Array.getLength(val);
			for (int i=0; i<length; i++)
			{
				final Object e = Array.get(val, i);
				if(e == null)
					continue;
				if(str.length()>0)
					str.append(" ");
				str.append(fromPOJOFieldToStandardString(e.getClass(),e,allowCR));
			}
		}
		else
		if(type == String.class)
			str.append(allowCR?val.toString().trim():CMStrings.replaceAllofAny(val.toString(), DEFAULT_CRLF, ' ').trim());
		else
		if (Collection.class.isAssignableFrom(type)) 
		{
			@SuppressWarnings("rawtypes")
			final Collection coll = (Collection)val;
			final Object[] asArray = coll.toArray();
			str.append(fromPOJOFieldToStandardString(asArray.getClass(),asArray,allowCR));
		}
		else
		if(type.isPrimitive())
			str.append(val.toString());
		else
			str.append(val.toString());
		return str.toString();
	}
	
	public void sendMsg(Status status, Object arguments) throws IOException
	{
		final StringBuilder str = new StringBuilder("");
		final boolean isBlock = (status == Status.BLOCK);
		final String statusName = isBlock ? "OK" : status.name();
		switch(commandMode)
		{
		case JSON:
		{
			str.append("{\"status\":\"").append(statusName).append("\"");
			String argument = new MiniJSON().fromPOJOFieldtoJSON(arguments.getClass(),arguments);
			if(argument.trim().length()>0)
				str.append(",\"message\":").append(argument);
			str.append("}");
			break;
		}
		case STANDARD:
		{
			str.append("[").append(status.name()); // ignore statusName here, always use actual type
			if(isBlock)
			{
				String argument = fromPOJOFieldToStandardString(arguments.getClass(), arguments, true);
				String eob = "/BLOCK:"+blockSequence.addAndGet(1);
				while(argument.indexOf(eob)>=0)
					eob="/BLOCK:"+blockSequence.addAndGet(1);
				str.append(" ").append(eob);
				str.append("]").append(argument).append(eob);
			}
			else
			{
				String argument = fromPOJOFieldToStandardString(arguments.getClass(), arguments, false);
				if(argument.trim().length()>0)
					str.append(" ").append(argument);
				str.append("]");
			}
			break;
		}
		case XML:
		{
			str.append("<").append(statusName).append(">");
			String argument = CMLib.xml().fromPOJOFieldtoXML(arguments.getClass(), arguments).trim();
			if(argument.trim().length()>0)
				str.append(argument);
			str.append("</").append(statusName).append(">");
			break;
		}
		}
		str.append("\r\n");
		final byte[] bytes = str.toString().getBytes();
		final ByteBuffer buf = ByteBuffer.wrap(bytes);
		while (chan.isConnected() && chan.isOpen() && (chan.write(buf) > 0))
		{
			CMLib.s_sleep(1);
		}
	}

	public void close()
	{
		closeMe = true;
		try
		{
			chan.close();
		}
		catch (final Exception e)
		{
		}
	}

	public void shutdown()
	{
		final long time = System.currentTimeMillis();
		try
		{
			chan.close();
		}
		catch (final Exception e)
		{
		}
		while ((System.currentTimeMillis() - time < 30000) && (isRunning))
		{
			CMLib.s_sleep(1000);
		}
	}

	public void login(final MOB M)
	{
		user = M.Name();
		target = M;
	}

	public void setTarget(final PhysicalAgent A)
	{
		target = A;
	}

	public PhysicalAgent getTarget()
	{
		return target;
	}

	public MOB getUser()
	{
		return CMLib.players().getLoadPlayer(user);
	}

	public void logout()
	{
		target = null;
		user = null;
	}
	
	public void setCommandMode(final CommandMode newMode)
	{
		if((newMode != null) && (newMode != this.commandMode))
		{
			synchronized(this)
			{
				this.commandMode = newMode;
				// the below allows commandmode to be used to reset parser manually by toggling modes
				this.accumBuffer.setLength(0);
				this.runner = null; // will be lazy-rebuilt later
				this.workingBuffers.clear();
				this.results.clear();
				this.execCommands.clear();
				this.markBlocks = DEFAULT_MARK_BLOCKS;
			}
		}
	}

	public void addDependent(final String s, final Object O)
	{
		dependents.put(s, O);
	}

	public void delDependent(final String s)
	{
		dependents.remove(s);
	}

	public boolean isRunning()
	{
		return isRunning;
	}

	public boolean needsClosing()
	{
		if (closeMe)
			return true;
		if ((System.currentTimeMillis() - idleTime) > MAX_IDLE_MILLIS)
			return true;
		if ((!chan.isOpen()) || (!chan.isConnected()) || (!chan.isRegistered()))
			return true;
		return false;
	}

	@Override
	public long getStartTime()
	{
		return startTime;
	}

	@Override
	public int getGroupID()
	{
		return Thread.currentThread().getThreadGroup().getName().charAt(0);
	}

	@Override
	public void run()
	{
		isRunning = true;
		startTime = System.currentTimeMillis();
		try
		{
			synchronized (this)
			{
				ByteBuffer buffer = null;
				if (workingBuffers.size() > 0)
					buffer = workingBuffers.getLast();
				if ((buffer == null) || (buffer.capacity() == buffer.limit()))
					buffer = ByteBuffer.allocate(BUFFER_SIZE);
				else
				{
					buffer.position(buffer.limit());
					buffer.limit(buffer.capacity());
				}
				while (chan.isConnected() && (chan.isOpen()) && (chan.read(buffer) > 0))
				{
					buffer.flip();
					switch(commandMode)
					{
					case STANDARD:
					{
						int containIndex = -1;
						for (int i = 0; i < buffer.limit(); i++)
						{
							if ((containIndex = CMParms.containIndex(buffer, markBlocks, i)) >= 0)
							{
								final int containIndexLength = markBlocks[containIndex].length;
								workingBuffers.remove(buffer);
								if (i > 0)
								{
									final ByteBuffer prevBuf = ByteBuffer.allocate(BUFFER_SIZE);
									prevBuf.put(buffer.array(), 0, i);
									prevBuf.flip();
									workingBuffers.add(prevBuf);
								}
								if (((i + containIndexLength) >= buffer.limit()) || ((i + containIndexLength) >= buffer.capacity()))
									buffer.position(buffer.limit());
								else
									buffer.position(i + containIndexLength);
								if (buffer.remaining() > 0)
								{
									final ByteBuffer newBuffer = ByteBuffer.allocate(BUFFER_SIZE);
									newBuffer.put(buffer);
									buffer = newBuffer;
									i = -1;
								}
								else
									buffer = ByteBuffer.allocate(BUFFER_SIZE);
								buffer.flip();

								int fullSize = 0;
								for (final ByteBuffer buf : workingBuffers)
									fullSize += buf.limit();
								final ByteBuffer finalBuf = ByteBuffer.allocate(fullSize);
								for (final ByteBuffer buf : workingBuffers)
								{
									buf.rewind();
									finalBuf.put(buf);
									workingBuffers.remove(buf);
								}
								finalBuf.flip();
								markBlocks = DEFAULT_MARK_BLOCKS;
								execCommands.add(new String(finalBuf.array(),0,finalBuf.limit(), StandardCharsets.UTF_8));
							}
						}
						if (!workingBuffers.contains(buffer) && (buffer.limit() > 0))
							workingBuffers.add(buffer);
						if (buffer.limit() == buffer.capacity())
							buffer = ByteBuffer.allocate(BUFFER_SIZE);
						else
						{
							buffer.position(buffer.limit());
							buffer.limit(buffer.capacity());
						}
						break;
					}
					case XML:
					{
						CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
						if(runner == null)
						{
							@SuppressWarnings("unchecked")
							final List<XMLTag> tags = (List<XMLTag>)results;
							runner = CMLib.xml().getXMLParser(accumBuffer, tags);
						}
						accumBuffer.append(charBuffer);
						runner.run();
						while(results.size()>0)
						{
							XMLTag tag = (XMLTag)results.remove(0);
							execCommands.add(tag.tag()+" "+tag.value());
						}
						buffer.clear(); // act like nothing was read
						if (workingBuffers.size()==0)
							workingBuffers.add(buffer); // re-use same buffer over and over
						break;
					}
					case JSON:
					{
						CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
						if(runner == null)
						{
							@SuppressWarnings("unchecked")
							final List<Object> objs = (List<Object>)results;
							runner = new MiniJSON().getJSONParser(accumBuffer, objs);
						}
						accumBuffer.append(charBuffer);
						try
						{
							runner.run();
						}
						catch(Error e)
						{
							Throwable t = e;
							if(e.getCause() instanceof MiniJSON.MJSONException)
								t = e.getCause();
							Log.errOut("CM1Hndlr", runnableName + ": " + t.getMessage());
							accumBuffer.setLength(0);
							break; // get out of this loop and give up
						}
						while(results.size()>0)
						{
							Object ob = results.remove(0);
							if(ob instanceof MiniJSON.JSONObject)
							{
								final MiniJSON.JSONObject obj = (MiniJSON.JSONObject)ob;
								if(!obj.containsKey("command"))
									execCommands.add("*Error: no command found in document"); // No legal command may start with *
								else
								{
									String command = obj.get("command").toString().toUpperCase().trim();
									final StringBuilder fullCommand = new StringBuilder(command);
									if(obj.containsKey("arguments"))
									{
										if(obj.get("arguments") instanceof Object[])
										{
											for(Object o : obj.getCheckedArray("arguments"))
												fullCommand.append(" ").append(o.toString());
										}
										else
											fullCommand.append(" ").append(obj.get("arguments").toString());
									}
									execCommands.add(fullCommand.toString());
								}
							}
						}
						buffer.clear(); // act like nothing was read
						if (workingBuffers.size()==0)
							workingBuffers.add(buffer); // re-use same buffer over and over
						break;
					}
					}
					if ((((long) BUFFER_SIZE * (long) workingBuffers.size())+accumBuffer.length()) > MAXIMUM_BYTES)
					{
						accumBuffer.setLength(0);
						workingBuffers.clear();
						shutdown();
						return;
					}
				}
				buffer.flip();
			}
			while(execCommands.size()>0)
			{
				String command = execCommands.remove(0);
				if(command.startsWith("*")) // No legal command begins with *
					sendMsg(Status.FAIL,command);
				else
				{
					try
					{
						execute(command);
					}
					catch(Exception e)
					{
						Log.errOut("CM1Hndlr", runnableName + ": " + e.getMessage());
						Log.errOut("CM1Hndlr", e);
					}
				}
			}
			Thread.sleep(1);
		}
		catch (final IOException ioe)
		{
			Log.errOut("CM1Hndlr", runnableName + ": " + ioe.getMessage());
			try
			{
				chan.close();
			}
			catch (final IOException e)
			{
			}
		}
		catch (final Exception e)
		{
			Log.errOut("CM1Hndlr", runnableName + ": " + e.getMessage());
			Log.errOut("CM1Hndlr", e);
		}
		finally
		{
			idleTime = System.currentTimeMillis();
			isRunning = false;
			startTime = 0;
		}
	}

	public void setEndOfLine(final String... msgs)
	{
		synchronized (this)
		{
			final byte[][] newBlocks = new byte[msgs.length][];
			int i = 0;
			for (final String s : msgs)
				newBlocks[i++] = s.getBytes();
			markBlocks = newBlocks;
		}
	}

	public void execute(final String line)
	{
		new CommandHandler(this, line).run();
	}
}
