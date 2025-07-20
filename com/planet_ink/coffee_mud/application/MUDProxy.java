package com.planet_ink.coffee_mud.application;

import javax.crypto.*;
import javax.crypto.spec.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;
import com.jcraft.jzlib.ZStream;
import com.jcraft.jzlib.ZStreamException;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
/*
Copyright 2025-2025 Bo Zimmerman

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
public class MUDProxy
{
	private static final int	BUFFER_SIZE	= 65536;
	private static String		mpcpKey		= "";
	private static Selector		selector	= null;
	private static LBStrategy	strategy	= LBStrategy.ROUNDROBIN;
	private static Random		rand		= new Random(System.nanoTime());

	private static final Map<SelectionKey, SelectionKey>
		channelPairs	= new Hashtable<>();
	private static final Map<Integer, PairList<String, Integer>>
		portMap			= new Hashtable<Integer, PairList<String, Integer>>();
	private static final Map<Pair<String, Integer>, Pair<SelectionKey, Long>>
		distressPingers	= new Hashtable<Pair<String, Integer>, Pair<SelectionKey, Long>>();
	private static final List<Runnable>
		runnables		= new Vector<Runnable>();
	private static final Map<Integer, AtomicInteger>
		strategyMap		= new Hashtable<Integer, AtomicInteger>();

	private static enum LBStrategy
	{
		ROUNDROBIN,
		LEASTCONN,
		RANDOM
	}

	private static enum ParseState
	{
		NORMAL,
		IAC,
		COMMAND,
		SB_202,
		SB_MCCP,
		SB_MCCP2,
		ANSI_ESC,
		ANSI_CSI
	}

	private final Pair<String, Integer>		port;
	private final String					ipAddress;
	private final boolean					isClient;
	private final int						outsidePortNum;
	private 	  ParseState				readState		= ParseState.NORMAL;
	private		  byte						readCommand		= 0;
	private final RefilByteArrayInputStream inputPipe		= new RefilByteArrayInputStream(new byte[0]);
	private  	  InputStream				in				= null;
	private final ByteArrayOutputStream 	outputPipe		= new ByteArrayOutputStream();
	private 	  OutputStream				out				= new FilterOutputStream(outputPipe);
	private final ByteArrayOutputStream		mpcpCommand		= new ByteArrayOutputStream();
	private final LinkedList<ByteBuffer>	input			= new LinkedList<ByteBuffer>();
	private final LinkedList<ByteBuffer>	output			= new LinkedList<ByteBuffer>();
	private final Map<String,Object>		session			= new Hashtable<String,Object>();
	private 	  long						distressTime	= 0;

	private MUDProxy(final boolean client, final int outsidePort, final Pair<String,Integer> port, final String remoteIP) throws IOException
	{
		this.outsidePortNum = outsidePort;
		this.isClient = client;
		this.port=port;
		this.ipAddress=remoteIP;
		this.in = new PassThroughInputStream(this.inputPipe);
	}

	public static class ProxyChannel
	{
		public volatile SocketChannel chan;
		public final MUDProxy context;
		public ProxyChannel(final SocketChannel chan, final MUDProxy context)
		{
			this.chan=chan;
			this.context=context;
		}
	}

	public static void main(final String a[])
	{
		Thread.currentThread().setName("PROXY");
		final Vector<String> iniFiles=new Vector<String>();
		if(a.length>0)
		{
			final Map<String,String[]> hargs=CMParms.parseCommandLineArgs(a);
			final String[] boots = hargs.remove("BOOT");
			if(boots != null)
			{
				for(final String bootIni : boots)
					iniFiles.add(bootIni);
			}
			final String[] strategy = hargs.remove("STRATEGY");
			if(strategy != null)
			{
				try
				{
					MUDProxy.strategy = LBStrategy.valueOf(strategy[0].toUpperCase().trim());
				}
				catch(final Exception e)
				{
					System.err.println("PROXY/ERROR: Illegal strategy: '"+strategy[0]+"' (try RANDOM, ROUNDROBIN, or LEASTCONN).");
					System.exit(-1);
				}
			}
		}
		CMLib.initialize(); // initialize this threads libs
		if(iniFiles.size()==0)
			iniFiles.addElement("coffeemud.ini");
		Log.instance().configureLogFile("proxy",1);
		for(final Log.Type logType : Log.Type.values())
			Log.instance().configureLog(logType, "BOTH");
		for(final String iniFile : iniFiles)
		{
			final CMProps page=CMProps.loadPropPage("//"+iniFile);
			if ((page==null)||(!page.isLoaded()))
			{
				Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read ini file: '"+iniFile+"'.");
				System.err.println("PROXY/ERROR: Unable to read ini file: '"+iniFile+"'.");
				System.exit(-1);
				return;
			}
			final String key = page.getPrivateStr("MPCPKEY");
			if((key != null)&&(key.length()>0))
				mpcpKey = key;
			final String proxy = page.getPrivateStr("PROXY");
			final String portStr  = page.getPrivateStr("PORT");
			if((proxy != null)&&(proxy.trim().length()>0)
			&&(portStr != null)&&(portStr.trim().length()>0))
			{
				final List<String> proxies = CMParms.parseCommas(proxy, true);
				final List<String> ports = CMParms.parseCommas(portStr, true);
				final PairList<String,Integer> portV = new PairArrayList<String,Integer>();
				int portNum = -1;
				for(final String str : ports)
				{
					String host = "localhost";
					String port = str.trim();
					final int x = str.indexOf(':');
					if(x>0)
					{
						host = str.substring(0,x);
						port = str.substring(x+1).trim();
					}
					if(CMath.isInteger(port))
					{
						portNum = CMath.s_int(port);
						portV.add(new Pair<String,Integer>(host,Integer.valueOf(portNum)));
					}
				}
				if(portNum <= 0)
				{
					Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read port from ini file: '"+iniFile+"'.");
					System.err.println("PROXY/ERROR: Unable to read port from ini file: '"+iniFile+"'.");
					System.exit(-1);
					return;
				}
				for(final String proxyPort : proxies)
				{
					final int proxyNum = CMath.s_int(proxyPort);
					if(proxyNum <= 0)
					{
						Log.errOut(Thread.currentThread().getName(),"ERROR: Invalid proxy port '"+proxyPort+" in ini file: '"+iniFile+"'.");
						System.err.println("PROXY/ERROR: Invalid proxy port '"+proxyPort+" in ini file: '"+iniFile+"'.");
						System.exit(-1);
						return;
					}
					final Integer proxyI = Integer.valueOf(proxyNum);
					if(!portMap.containsKey(proxyI))
						portMap.put(proxyI, new PairVector<String,Integer>());
					for(final Pair<String,Integer> mudPort : portV)
						portMap.get(proxyI).add(mudPort);
				}
			}
		}
		if(portMap.size()==0)
		{
			Log.errOut(Thread.currentThread().getName(),"ERROR: No proxy ports defined in ini file.");
			System.err.println("PROXY/ERROR: No proxy ports defined in ini file.");
			System.exit(-1);
		}
		if(mpcpKey.length()==0)
		{
			Log.errOut(Thread.currentThread().getName(),"ERROR: No mpcpkey defined in any ini file.");
			System.err.println("PROXY/ERROR: No mpcpkey defined in any ini file.");
			System.exit(-1);
		}
		Log.shareWith(MudHost.MAIN_HOST);
		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud Proxy v"+MUD.HOST_VERSION);
		Log.sysOut(Thread.currentThread().getName(),"(C) 2025-2025 Bo Zimmerman");
		Log.sysOut(Thread.currentThread().getName(),"http://www.coffeemud.org");
		try(Selector selector = Selector.open())
		{
			MUDProxy.selector = selector;
			for(final Integer proxyPort : portMap.keySet())
			{
				final ServerSocketChannel serverChannel = ServerSocketChannel.open();
				serverChannel.configureBlocking(false);
				serverChannel.bind(new InetSocketAddress(proxyPort.intValue()));
				final PairList<String,Integer> fws = portMap.get(proxyPort);
				serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				for(final Pair<String,Integer> fw : fws)
					Log.sysOut("Listening on port " + proxyPort + " -> " + fw.first + ":" + fw.second);
			}
			distressThread.start();
			while(true)
			{
				try
				{
					if(runnables.size()>0)
					{
						final List<Runnable> runs;
						synchronized(runnables)
						{
							runs = new ArrayList<Runnable>();
							runs.addAll(runnables);
							runnables.clear();
						}
						for(final Runnable R : runs)
						{
							try
							{
								R.run();
							}
							catch(final Exception e)
							{
								Log.errOut(e);
							}
						}
					}
					selector.select();
					final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					while(keys.hasNext())
					{
						final SelectionKey key = keys.next();
						keys.remove();
						try
						{
							if(!key.isValid())
								continue;
							if(key.isAcceptable())
							{
								final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
								final SocketChannel clientChannel = serverChannel.accept();
								if (clientChannel == null)
									return;
								clientChannel.setOption(StandardSocketOptions.SO_RCVBUF, Integer.valueOf(65536));
								//final MUDProxy serverContext = (MUDProxy)key.attachment();
								final InetSocketAddress clientAddress = (InetSocketAddress) clientChannel.getRemoteAddress();
								final String clientIp = clientAddress.getAddress().getHostAddress();
								final InetSocketAddress localAddress = (InetSocketAddress) serverChannel.getLocalAddress();
								final int listenPort = localAddress.getPort();
								clientChannel.configureBlocking(false);
								final SocketChannel targetChannel = SocketChannel.open();
								targetChannel.configureBlocking(false);
								final Integer serverPortI = Integer.valueOf(listenPort);
								final PairList<String,Integer> targetPorts = portMap.get(serverPortI);
								final Pair<String,Integer> targetPort;
								switch(MUDProxy.strategy)
								{
								case LEASTCONN:
								{
									Pair<String,Integer> lowest=null;
									int least = Integer.MAX_VALUE;
									for(final Pair<String,Integer> p : targetPorts)
									{
										final int t=trackPort(false,p.second,0,Integer.MAX_VALUE);
										if(t<least)
										{
											lowest=p;
											least=t;
										}
									}
									targetPort=lowest;
									break;
								}
								case RANDOM:
									targetPort = targetPorts.get(rand.nextInt(targetPorts.size()));
									break;
								case ROUNDROBIN:
									targetPort = targetPorts.get(trackPort(false,serverPortI,1,targetPorts.size()-1));
									break;
								default:
									targetPort = null;
									break;
								}
								targetChannel.connect(new InetSocketAddress(targetPort.first, targetPort.second.intValue()));
								final Pair<String,Integer> clientPort = new Pair<String,Integer>(clientIp,Integer.valueOf(listenPort));
								final SelectionKey clientKey =
										clientChannel.register(selector, SelectionKey.OP_READ, new MUDProxy(true,listenPort,clientPort,clientIp));
								final SelectionKey targetKey =
										targetChannel.register(selector, SelectionKey.OP_CONNECT, new MUDProxy(false,listenPort,targetPort,targetPort.first));
								synchronized(channelPairs)
								{
									channelPairs.put(clientKey, targetKey);
									channelPairs.put(targetKey, clientKey);
								}
								Log.sysOut(listenPort+"","Connection from "+clientIp+"->"+targetPort.first+":"+targetPort.second);
							}
							else
							if(key.isConnectable())
							{
								final SocketChannel serverChannel = (SocketChannel) key.channel();
								final MUDProxy serverContext = (MUDProxy)key.attachment();
								try
								{
									if (serverChannel.finishConnect())
									{
										serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, Integer.valueOf(65536));
										MUDProxy.trackPort(true,serverContext.port.second, 1,Integer.MAX_VALUE);
										serverChannel.write(ByteBuffer.wrap(new byte[] {
											(byte)Session.TELNET_IAC,
											(byte)Session.TELNET_WILL,
											(byte)Session.TELNET_MPCP
										}));
										if(serverContext.in instanceof ZInputStream)
										{
											// reset the server input MCCP
											serverContext.inputPipe.reset();
											serverContext.in.close();
											serverContext.in = new PassThroughInputStream(serverContext.inputPipe);
										}
										final SelectionKey pairedKey = channelPairs.get(key);
										final String clientAddr = (pairedKey!=null)?((MUDProxy)pairedKey.attachment()).ipAddress:
																				serverContext.ipAddress;
										serverChannel.write(ByteBuffer.wrap(makeMPCPPacket("ClientInfo {"
												+ "\"client_address\":\""+clientAddr+"\","
												+ "\"timestamp\":"+System.currentTimeMillis()+"}")));
										if(serverContext.distressTime != 0)
										{
											final JSONObject obj = new MiniJSON.JSONObject();
											obj.putAll(serverContext.session);
											obj.put("timestamp", Long.valueOf(System.currentTimeMillis()));
											serverChannel.write(ByteBuffer.wrap(makeMPCPPacket("SessionInfo "+obj.toString())));
											if(pairedKey!=null)
											{
												final SocketChannel clientChannel = (SocketChannel)pairedKey.channel();
												final MUDProxy clientContext = (MUDProxy)pairedKey.attachment();
												serverContext.outputPipe.reset();
												serverContext.out.write(("\n\r\n\r\u001B[0m\u001B[37m"
														+"-- Connection restored --\n\r").getBytes()); // in case the client is expecting compressed data
												serverContext.out.flush();
												clientChannel.write(ByteBuffer.wrap(serverContext.outputPipe.toByteArray()));
												Log.sysOut(clientContext.outsidePortNum+"","Connection restored "+clientContext.ipAddress
														+"->"+serverContext.port.first+":"+serverContext.port.second);
											}
											serverContext.distressTime=0;
										}
										key.interestOps(SelectionKey.OP_READ);
									}
									else
									{
										closeKey(key);
									}
								}
								catch (final ConnectException e)
								{
									closeKey(key);
								}
								catch (final IOException e)
								{
									closeKey(key);
								}
							}
							else
							if(key.isReadable())
								handleRead(key);
							else
							if(key.isWritable())
							{
								final SelectionKey k = key;
								MUD.serviceEngine.executeRunnable(new Runnable()
								{
									final SelectionKey key = k;
									@Override
									public void run()
									{
										try
										{
											handleWrite(key);
										}
										catch(final IOException e)
										{
											closeKey(key);
											Log.errOut(e);
										}
									}
								});
							}
						}
						catch (final IOException e)
						{
							closeKey(key);
							Log.errOut(e);
						}
					}
				}
				catch (final Exception e)
				{
					Log.errOut(e);
				}
			}
		}
		catch (final IOException e)
		{
			Log.errOut(e);
			e.printStackTrace();
		}
	}

	public static int trackPort(final boolean proxyPort, Integer portNumber, final int addSub, final int max)
	{
		synchronized(MUDProxy.strategyMap)
		{
			if(proxyPort)
				portNumber = Integer.valueOf(-portNumber.intValue());
			AtomicInteger ai = MUDProxy.strategyMap.get(portNumber);
			if(ai == null)
			{
				ai=new AtomicInteger(0);
				MUDProxy.strategyMap.put(portNumber, ai);
			}
			int val = ai.addAndGet(addSub);
			if(val > max)
			{
				val = 0;
				ai.set(0);
			}
			return val;
		}
	}

	private static byte[] makeMPCPPacket(final String command)
	{
		try
		{
			final byte[] keyBytes = mpcpKey.getBytes(StandardCharsets.UTF_8);
			final byte[] payloadBytes = command.getBytes(StandardCharsets.UTF_8);
			final SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA1");
			final Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(keySpec);
			final ByteArrayOutputStream packetOut = new ByteArrayOutputStream();
			packetOut.write(new byte[] { (byte)Session.TELNET_IAC, (byte)Session.TELNET_SB, (byte)Session.TELNET_MPCP});
			for(final byte b : mac.doFinal(payloadBytes))
			{
				if (b == (byte)0xFF)
					packetOut.write(new byte[] {(byte)0xFF,(byte)0xFF});
				else
					packetOut.write(b);
			}
			packetOut.write(payloadBytes);
			packetOut.write(new byte[] { (byte)Session.TELNET_IAC, (byte)Session.TELNET_SE});
			return packetOut.toByteArray();
		}
		catch(final Exception e)
		{
			Log.errOut(e);
		}
		return new byte[0];
	}

	private static void handleWrite(final SelectionKey key) throws IOException
	{
		final SocketChannel destChannel = (SocketChannel) key.channel();
		final MUDProxy destContext = (MUDProxy)key.attachment();
		while(destContext.output.size()>0)
		{
			final ByteBuffer buffer = destContext.output.getFirst();
			while (buffer.hasRemaining())
			{
				final int bytesWritten = destChannel.write(buffer);
				if (bytesWritten == 0)
				{
					key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
					return;
				}
			}
			if(!buffer.hasRemaining())
				destContext.output.removeFirst();
		}
		if(destContext.output.size() == 0)
			key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
	}

	private static void processMPCPPacket(final SelectionKey key, final MUDProxy context, final byte[] suboptionData)
	{
		// context is the target (mud facing) context, anything else is a waste of time.
		if(context.isClient)
			return;
		final byte[] digest = new byte[20];
		final ByteBuffer rdr = ByteBuffer.wrap(suboptionData);
		rdr.get(digest);
		final byte[] strBuf = new byte[rdr.remaining()];
		rdr.get(strBuf);
		final byte[] keyBytes = mpcpKey.getBytes(StandardCharsets.UTF_8);
		final SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA1");
		try
		{
			final Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(keySpec);
			final byte[] digestCheck = mac.doFinal(strBuf);
			if(!Arrays.equals(digest, digestCheck))
				return;
			final String str = new String(strBuf,"UTF-8");
			final int x = str.indexOf(' ');
			if(x>0)
			{
				final String command = str.substring(0,x).trim();
				final String jsonStr = str.substring(x+1).trim();
				final MiniJSON.JSONObject obj = new MiniJSON().parseObject(jsonStr);
				final Long timestamp = obj.getCheckedLong("timestamp");
				if(Math.abs(System.currentTimeMillis()-timestamp.longValue())>1000)
					return;
				obj.remove("timestamp");
				if(command.equalsIgnoreCase("sessioninfo"))
				{
					context.session.clear();
					context.session.putAll(obj);
				}
				else
				if(command.equalsIgnoreCase("disconnect"))
				{
					if(context.isClient)
						closeKey(key);
					else
					{
						final SelectionKey pairedKey;
						synchronized(channelPairs)
						{
							pairedKey = channelPairs.get(key);
						}
						if(pairedKey != null)
							closeKey(pairedKey);
					}
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("MPCP",e);
		}
	}

	public static class PassThroughInputStream extends FilterInputStream
	{
		public PassThroughInputStream(final InputStream in)
		{
			super(in);
		}
	}

	public static class RefilByteArrayInputStream extends ByteArrayInputStream
	{
		public RefilByteArrayInputStream(final byte[] buf)
		{
			super(buf);
		}

		public void refill(final byte[] newBuf)
		{
			this.buf = newBuf;
			this.count = newBuf.length;
			this.pos = 0;
			this.mark = 0;
		}
	}

	private static ByteBuffer readFilter(final SelectionKey key, final MUDProxy sourceContext, ByteBuffer buffer)
	{
		final byte[] checkBuf = new byte[buffer.remaining()];
		buffer.get(checkBuf);
		buffer.clear();
		int streamType = Session.TELNET_COMPRESS2;
		try
		{
			sourceContext.inputPipe.refill(checkBuf);
			if(sourceContext.in instanceof ZInputStream)
				((ZInputStream)sourceContext.in).allowMoreInput();
			sourceContext.outputPipe.reset();
			int iByte;
			while((iByte = sourceContext.in.read())>=0)
			{
				final byte b=(byte)iByte;
				switch(sourceContext.readState)
				{
				case NORMAL:
					if(b == (byte)Session.TELNET_IAC)
						sourceContext.readState = ParseState.IAC;
					else
					if(b == (byte)27)
						sourceContext.readState = ParseState.ANSI_ESC;
					else
						sourceContext.out.write(b);
					break;
				case IAC:
					if(b == (byte)Session.TELNET_IAC)
					{
						sourceContext.readState = ParseState.NORMAL;
						sourceContext.out.write(b);
						sourceContext.out.write(b);
					}
					else
					if((b == (byte)Session.TELNET_DO)
					||(b == (byte)Session.TELNET_DONT)
					||(b == (byte)Session.TELNET_WILL)
					||(b == (byte)Session.TELNET_WONT))
					{
						sourceContext.readState = ParseState.COMMAND;
						sourceContext.readCommand = b;
					}
					else
					if(b == (byte)Session.TELNET_SB)
					{
						sourceContext.readState = ParseState.COMMAND;
						sourceContext.readCommand = b;
					}
					else
					{
						sourceContext.readState = ParseState.NORMAL;
						sourceContext.out.write((byte)Session.TELNET_IAC);
						sourceContext.out.write(b);
					}
					break;
				case COMMAND:
					if(b == (byte)Session.TELNET_MPCP)
					{
						sourceContext.mpcpCommand.reset();
						if(sourceContext.readCommand==(byte)Session.TELNET_SB)
						{
							sourceContext.readState = ParseState.SB_202;
						}
						else
						{
							if(sourceContext.readCommand == (byte)Session.TELNET_DO)
							{
								if(sourceContext.outsidePortNum == -1)
								{
									final Pair<SelectionKey,Long> d = MUDProxy.distressPingers.get(sourceContext.port);
									if(d != null)
										d.second = Long.valueOf(System.currentTimeMillis());
								}
							}
							sourceContext.readState = ParseState.NORMAL;
						}
					}
					else
					if((b == (byte)Session.TELNET_COMPRESS))
					{
						streamType = Session.TELNET_COMPRESS;
						if(sourceContext.readCommand==(byte)Session.TELNET_SB)
						{
							sourceContext.out.write((byte)Session.TELNET_IAC);
							sourceContext.out.write((byte)Session.TELNET_SB);
							sourceContext.out.write((byte)streamType);
							sourceContext.readState = ParseState.SB_MCCP;
						}
						else
						{
							sourceContext.out.write((byte)Session.TELNET_IAC);
							sourceContext.out.write(sourceContext.readCommand);
							sourceContext.out.write(b);
							sourceContext.readState = ParseState.NORMAL;
						}
					}
					else
					if((b == (byte)Session.TELNET_COMPRESS2))
					{
						streamType = Session.TELNET_COMPRESS2;
						if(sourceContext.readCommand==(byte)Session.TELNET_SB)
						{
							sourceContext.out.write((byte)Session.TELNET_IAC);
							sourceContext.out.write((byte)Session.TELNET_SB);
							sourceContext.out.write((byte)streamType);
							sourceContext.readState = ParseState.SB_MCCP2;
						}
						else
						{
							sourceContext.out.write((byte)Session.TELNET_IAC);
							sourceContext.out.write(sourceContext.readCommand);
							sourceContext.out.write(b);
							sourceContext.readState = ParseState.NORMAL;
						}
					}
					else
					{
						sourceContext.out.write((byte)Session.TELNET_IAC);
						sourceContext.out.write(sourceContext.readCommand);
						sourceContext.out.write(b);
						sourceContext.readState = ParseState.NORMAL;
					}
					break;
				case ANSI_ESC:
					sourceContext.out.write((byte)27);
					sourceContext.out.write(b);
					if(b == (byte)91) // csi sequence
						sourceContext.readState = ParseState.ANSI_CSI;
					else
						sourceContext.readState = ParseState.NORMAL;
					break;
				case ANSI_CSI:
					sourceContext.out.write(b);
					if ((b >= (byte)0x40) && (b <= (byte)0x7E))
						sourceContext.readState = ParseState.NORMAL;
					break;
				case SB_202:
					if(b == (byte)Session.TELNET_IAC)
					{
						if(sourceContext.readCommand == b)
						{
							sourceContext.mpcpCommand.write(b);
							sourceContext.readCommand = 0;
						}
						else
							sourceContext.readCommand = b;
					}
					else
					if(b == (byte)Session.TELNET_SE)
					{
						if(sourceContext.readCommand == (byte)Session.TELNET_IAC)
						{
							sourceContext.readState = ParseState.NORMAL;
							processMPCPPacket(key,sourceContext,sourceContext.mpcpCommand.toByteArray());
							sourceContext.mpcpCommand.reset();
						}
						else
							sourceContext.mpcpCommand.write(b);
						sourceContext.readCommand = 0;
					}
					else
					if(sourceContext.mpcpCommand.size()>65536)
					{
						sourceContext.mpcpCommand.reset();
						sourceContext.readState = ParseState.NORMAL;
						sourceContext.readCommand = 0;
					}
					else
					{
						sourceContext.mpcpCommand.write(b);
						sourceContext.readCommand = 0;
					}
					break;
				case SB_MCCP:
					streamType=Session.TELNET_COMPRESS;
					//$FALL-THROUGH$
				case SB_MCCP2:
					if(b == (byte)Session.TELNET_IAC)
					{
						sourceContext.out.write((byte)Session.TELNET_IAC);
						if(sourceContext.readCommand != (byte)Session.TELNET_SB)
							sourceContext.readState = ParseState.NORMAL;
						else
							sourceContext.readCommand = b;
					}
					else
					if(b == (byte)Session.TELNET_SE)
					{
						sourceContext.out.write((byte)Session.TELNET_SE);
						if(sourceContext.readCommand == (byte)Session.TELNET_IAC)
						{
							sourceContext.readState = ParseState.NORMAL;
							@SuppressWarnings("resource")
							final ZInputStream zIn = new ZInputStream(sourceContext.in, false);
							zIn.setFlushMode(JZlib.Z_SYNC_FLUSH);
							sourceContext.in = zIn;
							sourceContext.out.flush();
							@SuppressWarnings("resource")
							final ZOutputStream zOut = new ZOutputStream(sourceContext.outputPipe, JZlib.Z_DEFAULT_COMPRESSION);
							zOut.setFlushMode(JZlib.Z_SYNC_FLUSH);
							sourceContext.out = zOut;
						}
						else
							sourceContext.readState = ParseState.NORMAL;
					}
					else
					{
						sourceContext.out.write(b);
						sourceContext.readState = ParseState.NORMAL;
					}
					break;
				}
			}
		}
		catch (final IOException e)
		{
			Log.errOut(e);
		}
		final byte[] finalBuf = sourceContext.outputPipe.toByteArray();
		if(finalBuf.length > buffer.capacity())
			buffer = ByteBuffer.allocate(finalBuf.length);
		buffer.put(finalBuf);
		sourceContext.outputPipe.reset();
		buffer.flip();
		return buffer;
	}

	private static void handleRead(final SelectionKey key) throws IOException
	{
		final SocketChannel sourceChannel = (SocketChannel) key.channel();
		final MUDProxy sourceContext = (MUDProxy)key.attachment();
		int bytesRead;
		if(sourceContext.outsidePortNum < 0)
		{
			// special proxy ping connection
			final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			try
			{
				while((bytesRead = sourceChannel.read(buffer))>0)
				{
					buffer.flip();
					readFilter(key,sourceContext, buffer);
					buffer.clear();
				}
			}
			catch(final IOException e)
			{
				key.cancel();
			}
			return;
		}
		final SelectionKey destKey = channelPairs.get(key);
		if (destKey == null)
		{
			closeKey(key);
			return;
		}
		final SocketChannel destChannel = (SocketChannel) destKey.channel();
		if (destChannel == null)
		{
			closeKey(key);
			return;
		}
		final MUDProxy destContext = (MUDProxy)destKey.attachment();
		try
		{
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			while((bytesRead = sourceChannel.read(buffer))>0)
			{
				buffer.flip();
				buffer = readFilter(key,sourceContext, buffer);
				if((destContext.output.size()==0)&&(destChannel.isConnected()))
				{
					while (buffer.hasRemaining())
					{
						final int bytesWritten = destChannel.write(buffer);
						if (bytesWritten == 0)
						{
							if(buffer.hasRemaining())
							{
								destContext.output.add(buffer);
								buffer = ByteBuffer.allocate(BUFFER_SIZE);
								destKey.interestOps(destKey.interestOps() | SelectionKey.OP_WRITE);
							}
							break;
						}
					}
					buffer.clear();
				}
				else
				if(buffer.hasRemaining())
				{
					destContext.output.add(buffer);
					buffer = ByteBuffer.allocate(BUFFER_SIZE);
					destKey.interestOps(destKey.interestOps() | SelectionKey.OP_WRITE);
				}
			}
			if (bytesRead == -1)
			{
				closeKey(key);
				return;
			}
		}
		catch (final IOException e)
		{
			key.cancel();
			closeKey(key);
			return;
		}
	}

	private static void putKeyInDistress(final SelectionKey serverKey,
										 final SocketChannel serverChannel,
										 final MUDProxy serverContext,
										 final SelectionKey clientKey,
										 final SocketChannel clientChannel,
										 final MUDProxy clientContext)
	{
		final byte[] bmsg = ("\n\r\n\r\u001B[0m\u001B[37m"
				+"-- Connection to server lost.  Please stand by --\n\r").getBytes();
		try
		{
			serverContext.outputPipe.reset();
			serverContext.out.write(bmsg); // in case the client is expecting compressed data
			serverContext.out.flush();
			clientChannel.write(ByteBuffer.wrap(serverContext.outputPipe.toByteArray()));
			serverContext.distressTime=System.currentTimeMillis();
			try
			{
				serverChannel.close();
			}
			catch (final IOException e) {}
			final SocketChannel newChannel=SocketChannel.open();
			newChannel.configureBlocking(false);
			final SelectionKey newServerKey = newChannel.register(selector, SelectionKey.OP_CONNECT, serverContext);
			newServerKey.interestOps(SelectionKey.OP_CONNECT);
			channelPairs.put(clientKey, newServerKey);
			channelPairs.put(newServerKey, clientKey);
			Log.sysOut(serverContext.outsidePortNum+"","Connection suspended "+clientContext.ipAddress
					+"->"+serverContext.port.first+":"+serverContext.port.second);
		}
		catch (final IOException e)
		{
			Log.errOut(e);
		}
	}

	private static void closeKey(final SelectionKey key)
	{
		if (key == null)
			return;
		final SocketChannel channel = (SocketChannel) key.channel();
		final MUDProxy context = (MUDProxy)key.attachment();
		if(context.outsidePortNum == -1)
			return; // this is a distress socket
		final SelectionKey pairedKey;
		synchronized(channelPairs)
		{
			pairedKey = channelPairs.get(key);
		}
		SocketChannel pairedChannel = null;
		MUDProxy pairedContext = null;
		synchronized(channelPairs)
		{
			channelPairs.remove(key);
			if(!context.isClient)
				MUDProxy.trackPort(true,context.port.second, -1,Integer.MAX_VALUE);
			if(pairedKey != null)
			{
				pairedChannel = (SocketChannel) pairedKey.channel();
				pairedContext = (MUDProxy)pairedKey.attachment();
				channelPairs.remove(pairedKey);
				if((pairedContext != null) && (!pairedContext.isClient))
					MUDProxy.trackPort(true,pairedContext.port.second, -1,Integer.MAX_VALUE);
			}
		}
		if((!context.isClient)
		&&(pairedChannel!=null))
		{
			if(context.distressTime == 0)
				putKeyInDistress(key,channel,context,pairedKey,pairedChannel,pairedContext);
			return;
		}
		try
		{
			if (pairedChannel != null)
				pairedChannel.close();
		}
		catch (final IOException e) {}
		try
		{
			channel.close();
		}
		catch (final IOException e) {}
		key.cancel();
		if(pairedChannel != null)
		{
			final MUDProxy context1 = (MUDProxy)key.attachment();
			final SelectionKey pKey = pairedChannel.keyFor(key.selector());
			if(pKey != null)
			{
				final MUDProxy context2 = (MUDProxy)pKey.attachment();
				final MUDProxy clientContext = context1.isClient?context1:context2;
				final MUDProxy serverContext = context1.isClient?context2:context1;
				final int listenPort = serverContext.outsidePortNum;
				Log.sysOut(listenPort+"","Connection lost "+clientContext.ipAddress
						+"->"+serverContext.port.first+":"+serverContext.port.second);
			}
		}
	}

	public static Thread distressThread = new Thread()
	{
		@Override
		public void run()
		{
			super.setName("PROXY_DISTRESS");
			boolean lastDistress=false;
			try
			{
				while(true)
				{
					if(runnables.size()>0)
						selector.wakeup();
					Thread.sleep(lastDistress?10000:20000);
					try
					{
						final Map<SelectionKey, SelectionKey> pairs = new HashMap<SelectionKey, SelectionKey>();
						synchronized(channelPairs)
						{
							pairs.putAll(channelPairs);
						}
						final Map<Pair<String,Integer>, List<Pair<SelectionKey,MUDProxy>>> distresses =
								new HashMap<Pair<String,Integer>, List<Pair<SelectionKey,MUDProxy>>>();
						for(final SelectionKey key : pairs.keySet())
						{
							final MUDProxy proxy = (MUDProxy)key.attachment();
							if((proxy != null)
							&& (proxy.distressTime != 0)
							&& (!proxy.isClient))
							{
								lastDistress=true;
								if(!distresses.containsKey(proxy.port))
									distresses.put(proxy.port, new ArrayList<Pair<SelectionKey,MUDProxy>>());
								distresses.get(proxy.port).add(new Pair<SelectionKey,MUDProxy>(key,proxy));
							}
						}
						if(distresses.size() > 0)
						{
							final Set<Pair<String,Integer>> toPings = new HashSet<Pair<String,Integer>>();
							for(final Pair<String,Integer> port : distresses.keySet())
							{
								final Pair<SelectionKey,Long> p = MUDProxy.distressPingers.get(port);
								if(p != null)
								{
									for(final Iterator<Pair<SelectionKey,MUDProxy>> i = distresses.get(port).iterator();i.hasNext();)
									{
										final Pair<SelectionKey,MUDProxy> xP = i.next();
										Long dTime;
										synchronized(p)
										{
											dTime = p.second;
										}
										if(xP.second.distressTime < dTime.longValue())
										{
											synchronized(runnables)
											{
												runnables.add(new Runnable()
												{
													final SelectionKey partnerKey = channelPairs.get(xP.first);
													final SelectionKey key = xP.first;
													final MUDProxy context = xP.second;
													@Override
													public void run()
													{
														try
														{
															// need to create a whole new socket channel
															final SocketChannel chan = (SocketChannel)key.channel();
															key.cancel();
															chan.close();
															synchronized(channelPairs)
															{
																channelPairs.remove(key);
																channelPairs.remove(partnerKey);
															}
															final SocketChannel newChannel=SocketChannel.open();
															newChannel.configureBlocking(false);
															final SelectionKey nkey = newChannel.register(selector, SelectionKey.OP_CONNECT, context);
															nkey.interestOps(SelectionKey.OP_CONNECT);
															channelPairs.put(partnerKey, nkey);
															channelPairs.put(nkey, partnerKey);
															newChannel.connect(new InetSocketAddress(context.port.first,context.port.second.intValue()));
														}
														catch(final Exception e)
														{
															Log.errOut(e);
														}
													}
												});
											}
										}
										else
											toPings.add(port);
									}
								}
								else
									toPings.add(port);
							}
							if(toPings.size()>0)
							{
								for(final Pair<String,Integer> port : toPings)
								{
									synchronized(runnables)
									{
										runnables.add(new Runnable()
										{
											final Pair<String,Integer> p = port;
											@Override
											public void run()
											{
												try
												{
													Pair<SelectionKey,Long> pinger = distressPingers.get(p);
													if(pinger != null)
													{
														final SelectionKey key = pinger.first;
														if(key != null)
														{
															final SocketChannel chan = (SocketChannel)key.channel();
															if(chan != null)
																chan.close();
															key.cancel();
														}
													}
													final SocketChannel pingChannel=SocketChannel.open();
													pingChannel.configureBlocking(false);
													pingChannel.connect(new InetSocketAddress(p.first, p.second.intValue()));
													final SelectionKey key = pingChannel.register(selector, SelectionKey.OP_CONNECT, new MUDProxy(false,-1,p,"PROXY"));
													key.interestOps(SelectionKey.OP_CONNECT);
													pinger = new Pair<SelectionKey,Long>(key,Long.valueOf(0));
													distressPingers.put(p, pinger);
												}
												catch(final Exception e)
												{
													Log.errOut(e);
												}
											}
										});
									}
								}
							}
						}
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
				}
			}
			catch(final InterruptedException e)
			{
				Log.errOut(e);
			}
		}
	};
}
