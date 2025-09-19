package com.planet_ink.coffee_mud.application;

import javax.crypto.*;
import javax.crypto.spec.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;
import java.security.*;

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
/**
 * Proxy server for CoffeeMud MUDs, allowing load balancing and
 * MCCP2 support across reboots.
 *
 * @author BZ
 */
public class MUDProxy
{
	private static final int		BUFFER_SIZE		= 65536;
	private static String			mpcpKey			= "";
	private static Selector			selector		= null;
	private static LBStrategy		strategy		= LBStrategy.ROUNDROBIN;
	private static Random			rand			= new Random(System.nanoTime());
	private static String			ctlPassword		= "" + (rand.nextInt(90000) + 10000);
	private static boolean			packetDebug		= false;

	private static final Map<String,Pair<String, Integer>>
		allPorts		= new Hashtable<String,Pair<String, Integer>>();
	private static final Map<SelectionKey, SelectionKey>
		channelPairs	= new Hashtable<SelectionKey, SelectionKey>();
	private static final Map<Integer, PairList<String, Integer>>
		portMap			= new Hashtable<Integer, PairList<String, Integer>>();
	private static final Map<Pair<String, Integer>, Triad<SelectionKey,Long,Boolean>>
		distressPingers	= new Hashtable<Pair<String, Integer>, Triad<SelectionKey,Long,Boolean>>();
	private static final List<Runnable>
		runnables		= new Vector<Runnable>();
	private static final Map<Integer, AtomicInteger>
		strategyMap		= new Hashtable<Integer, AtomicInteger>();

	/**
	 * Strategies for incoming connections.
	 */
	private static enum LBStrategy
	{
		ROUNDROBIN,
		LEASTCONN,
		RANDOM
	}

	/**
	 * Parse states for traffic from the mud, allowing it to react to
	 * messages from the mud, or maintain MCCP across reboots.
	 */
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

	/**
	 * Traffic parsing state handler class.
	 */
	private static class ParseStatus
	{
		private ParseState					state		= ParseState.NORMAL;
		private int							command		= 0;
		private final ByteArrayOutputStream	mpcpCommand	= new ByteArrayOutputStream();
	}

	private final Pair<String, Integer>		port;
	private final String					ipAddress;
	private final boolean					isClient;
	private final int						outsidePortNum;
	private volatile long					distressedTime	= 0;
	private final ParseStatus				readStatus		= new ParseStatus();
	private final ParseStatus				writeStatus		= new ParseStatus();
	private final RefilByteArrayInputStream	inputPipe		= new RefilByteArrayInputStream(new byte[0]);
	private InputStream						in				= null;
	private final ByteArrayOutputStream		outputPipe		= new ByteArrayOutputStream();
	private OutputStream					out				= new FilterOutputStream(outputPipe);
	private final Map<String, Object>		session			= new Hashtable<String, Object>();
	private final AtomicBoolean				isProcessing	= new AtomicBoolean(false);
	private final Queue<ByteBuffer>			pendingInputs	= new ConcurrentLinkedQueue<>();
	private final Queue<ByteBuffer>			input			= new ConcurrentLinkedQueue<ByteBuffer>(); // raw, from source
	private final Queue<ByteBuffer>			inter			= new ConcurrentLinkedQueue<ByteBuffer>(); // ready to convert for output
	private final Queue<ByteBuffer>			output			= new ConcurrentLinkedQueue<ByteBuffer>(); // converted for output

	/**
	 * A MUDProxy class instance represents a connection between either a user or the mud and this proxy server.
	 *
	 * @param client true if this instance represents a user connection, false if it represents a mud connection
	 * @param outsidePort the outside port number the user connected to
	 * @param port the host:port pair of the other end of this connection
	 * @param remoteIP the IP address of the user, or the mud, depending
	 * @throws IOException if an error occurs
	 */
	private MUDProxy(final boolean client, final int outsidePort, final Pair<String,Integer> port, final String remoteIP) throws IOException
	{
		this.outsidePortNum = outsidePort;
		this.isClient = client;
		this.port=port;
		this.ipAddress=remoteIP;
		this.in = new PassThroughInputStream(this.inputPipe);
	}

	/**
	 * Close the given selection key and its associated channel, and its paired channel/key.
	 */
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

	private static class ReadProcessor implements Runnable
	{
		private final MUDProxy		myCtx;
		private final SelectionKey	k;
		private final MUDProxy		destCtx;
		private final SelectionKey	destK;
		private final boolean		eof;

		public ReadProcessor(final MUDProxy myCtx, final SelectionKey k, final MUDProxy destCtx, final SelectionKey destK, final boolean eof)
		{
			this.myCtx = myCtx;
			this.k = k;
			this.destCtx = destCtx;
			this.destK = destK;
			this.eof = eof;
		}

		@Override
		public void run()
		{
			try
			{
				ByteBuffer buffer;
				while (!myCtx.pendingInputs.isEmpty())
				{
					while((buffer = myCtx.pendingInputs.poll()) != null)
					{
						final ByteBuffer b = readFilter(k, myCtx, buffer);
						synchronized(destCtx.output)
						{
							destCtx.inter.add(b);
						}
					}
					handleWrite(destK);
				}
				if(eof)
					closeKey(k);
			}
			catch (final Exception e)
			{
				Log.debugOut("readThread:" + myCtx.toString());
				Log.errOut(e);
				closeKey(k);
			}
			finally
			{
				myCtx.isProcessing.set(false);
			}
		}
	}

	public static void sendControlCommand(final String a[])
	{
		if (a.length < 5)
		{
			System.err.println("PROXY/ERROR: Invalid command line. Format: MUDProxy CMD [port] [keypassword] [<ctlpassword>] [command] ");
			System.exit(-1);
		}
		final int port = CMath.s_int(a[1]);
		if((port <= 0)||(port > 65535))
		{
			System.err.println("PROXY/ERROR: Invalid port: " + a[1]);
			System.exit(-1);
		}
		final String keypassword = a[2];
		if (keypassword.length()==0)
		{
			System.err.println("PROXY/ERROR: Invalid key password: " + keypassword);
			System.exit(-1);
		}
		final String ctlpassword = a[3];
		if (CMath.s_int(ctlpassword) < 10000)
		{
			System.err.println("PROXY/ERROR: Invalid control password: " + ctlpassword);
			System.exit(-1);
		}
		mpcpKey = keypassword;
		final StringBuilder cmd = new StringBuilder("");
		for (int i = 4; i < a.length; i++)
			cmd.append(a[i]).append(" ");
		final String command = cmd.toString().trim();
		try (Socket sock = new Socket("localhost", port))
		{
			final String payload = command.toUpperCase().trim()+" {\"message\":\"" + MiniJSON.toJSONString(command) + "\""
					+ ",\"password\":\"" + MiniJSON.toJSONString(ctlpassword) + "\""
					+ ",\"timestamp\":" + System.currentTimeMillis() + "}";
			final byte[] packet = makeMPCPPacket(payload);
			sock.getOutputStream().write(packet);
			sock.getOutputStream().write(makeMPCPPacket("DISCONNECT {\"password\":\"" + MiniJSON.toJSONString(ctlpassword) + "\""
											+ ",\"timestamp\":" + System.currentTimeMillis() + "}"));
			sock.getOutputStream().flush();
			System.out.println("Control command sent: " + command);
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final InputStream in = sock.getInputStream();
			final long startTime = System.currentTimeMillis();
			sock.setSoTimeout(5000);
			while(System.currentTimeMillis() - startTime < 5000)
			{
				try
				{
					final int read = in.read();
					if(read >=0)
						bout.write(read);
					else
						break;
				}
				catch (final SocketTimeoutException e)
				{
					break;
				}
			}
			final StringBuilder str = new StringBuilder("");
			final byte[] b = bout.toByteArray();
			for (int i = 0; i < b.length-10; i++)
			{
				if(((b[i]&0xff)==Session.TELNET_IAC)
				&&((b[i+1]&0xff)==Session.TELNET_SB)
				&&((b[i+2]&0xff) == Session.TELNET_MPCP))
				{
					i+=23;
					while((i<b.length-1)&&((b[i]&0xff)!=Session.TELNET_IAC))
						str.append((char)b[i++]);
					final String s = str.toString();
					final int x = s.indexOf('{');
					if(x>0)
					{
						final MiniJSON.JSONObject obj = new MiniJSON().parseObject(s.substring(x));
						if (obj.containsKey("message"))
						{
							str.setLength(0);
							str.append(obj.getCheckedString("message"));
						}
					}
					break;
				}
			}
			if (str.length() > 0)
				System.out.println("Response: " + str.toString().trim());
			else
				System.out.println("No response received.");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			System.err.println("PROXY/ERROR: Failed to send command: " + e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Get an existing or new Pair<String,Integer> representing the given host
	 * and port. Used to reduce memory usage.
	 *
	 * @param host the host
	 * @param port the port
	 * @return the shared Pair<String,Integer>
	 */
	public synchronized static Pair<String, Integer> getPort(final String host, final Integer port)
	{
		final String key = ((host==null)?"":host.toLowerCase()) + ":" + port.toString();
		if (allPorts.containsKey(key))
			return allPorts.get(key);
		final Pair<String,Integer> p = new Pair<String,Integer>(host,port);
		allPorts.put(key, p);
		return p;
	}

	/**
	 * Main entry point for the proxy server.
	 *
	 * The arguments are similar to the MUD (MUD.java), in that
	 * you can specify as many BOOT=inifilename.ini files containing
	 * port information.  You can also specify a STRATEGY=strategy
	 *
	 * @param a the arguments
	 */
	public static void main(final String a[])
	{
		if((a.length>0)&&(a[0].length()>0)&&(Character.toUpperCase(a[0].charAt(0))=='C'))
		{
			sendControlCommand(a);
			return;
		}
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
						portV.add(getPort(host,Integer.valueOf(portNum)));
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
		Log.sysOut(Thread.currentThread().getName(),"CoffeeMud Proxy v" + MUD.HOST_VERSION);
		Log.sysOut(Thread.currentThread().getName(),"(C) 2025-2025 Bo Zimmerman");
		Log.sysOut(Thread.currentThread().getName(),"http://www.coffeemud.org");
		Log.sysOut(Thread.currentThread().getName(),"Control password: "+ctlPassword);
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
			while(true) //main proxy server loop
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
					// main selection processing loop
					selector.select();
					final Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					while(keys.hasNext())
					{
						final SelectionKey key = keys.next();
						keys.remove();
						try
						{
							if(!key.isValid())
							{
								closeKey(key);
								continue;
							}

							//*** Acceptable
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
								clientChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
								final SocketChannel targetChannel = SocketChannel.open();
								targetChannel.configureBlocking(false);
								targetChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
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
								if(targetPort != null)
								{
									targetChannel.connect(new InetSocketAddress(targetPort.first, targetPort.second.intValue()));
									final Pair<String,Integer> clientPort = getPort(clientIp,Integer.valueOf(listenPort));
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
							}

							//*** Connectable
							if(key.isConnectable())
							{
								final SocketChannel serverChannel = (SocketChannel) key.channel();
								final MUDProxy serverContext = (MUDProxy)key.attachment();
								try
								{
									if (serverChannel.finishConnect())
									{
										final SelectionKey pairedKey = channelPairs.get(key);
										final MUDProxy clientContext =(pairedKey!=null) ? ((MUDProxy)pairedKey.attachment()) : null;
										serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, Integer.valueOf(65536));
										MUDProxy.trackPort(true,serverContext.port.second, 1,Integer.MAX_VALUE);
										chanWrite(serverChannel,ByteBuffer.wrap(new byte[] {
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
										final String clientAddr = (pairedKey!=null)?((MUDProxy)pairedKey.attachment()).ipAddress:
																				serverContext.ipAddress;
										chanWrite(serverChannel,ByteBuffer.wrap(makeMPCPPacket("ClientInfo {"
												+ "\"client_address\":\""+clientAddr+"\","
												+ "\"timestamp\":"+System.currentTimeMillis()+"}")));
										if((clientContext!=null)&&(clientContext.distressedTime != 0))
										{
											clientContext.distressedTime=0;
											final JSONObject obj = new MiniJSON.JSONObject();
											obj.putAll(serverContext.session);
											obj.put("timestamp", Long.valueOf(System.currentTimeMillis()));
											chanWrite(serverChannel,ByteBuffer.wrap(makeMPCPPacket("SessionInfo "+obj.toString())));
											if((pairedKey!=null)&&(clientContext!=null))
											{
												clientContext.inter.add(ByteBuffer.wrap(("\n\r\n\r\u001B[0m\u001B[37m"
														+"-- Connection restored --\n\r").getBytes()));
												pairedKey.interestOps(pairedKey.interestOps() & ~SelectionKey.OP_WRITE);
												Log.sysOut(clientContext.outsidePortNum+"","Connection restored "+clientContext.ipAddress
														+"->"+serverContext.port.first+":"+serverContext.port.second);
											}
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
									key.cancel();
									closeKey(key);
								}
								catch (final NoConnectionPendingException e)
								{
									key.cancel();
									closeKey(key);
								}
								catch (final IOException e)
								{
									key.cancel();
									closeKey(key);
								}
							}

							//*** Readable
							if(key.isReadable())
								handleRead(key);

							//*** Writeable
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
										catch(final Exception e)
										{
											closeKey(key);
											Log.errOut(e);
										}
									}
								});
							}
						}
						catch (final CancelledKeyException e)
						{
							key.cancel();
							closeKey(key);
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

	/**
	 * Track the number of connections to a given port, positive for mud-side,
	 * @param proxyPort true if mud-side, false if user-side
	 * @param portNumber the port number
	 * @param addSub +1 to add a connection, -1 to remove one, 0 to just get the current count
	 * @param max the maximum number to count up to before rolling over to 0
	 * @return the current count after the add/sub operation
	 */
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

	/**
	 * Creates a valid MCCP packet from the given command string.
	 *
	 * @param command the command string with JSON
	 * @return the byte array representing the MCCP packet
	 */
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

	/**
	 * Writes the given ByteBuffer data to the channel represented
	 * by the given selection key and MUDProxy context, filtering
	 * it through the output filter first.
	 *
	 * @param key the selection key
	 * @param targetContext the MUDProxy context
	 * @param buffer the data to write
	 * @return the filtered data ready to write
	 */
	private static ByteBuffer writeFilter(final SelectionKey key, final MUDProxy targetContext, final ByteBuffer buffer)
	{
		final byte[] checkBuf = new byte[buffer.remaining()];
		buffer.get(checkBuf);
		buffer.clear();
		final ByteArrayInputStream bin = new ByteArrayInputStream(checkBuf);
		targetContext.outputPipe.reset();
		final ParseStatus status;
		synchronized(targetContext)
		{
			status = targetContext.writeStatus;
		}
		final OutputStream out;
		synchronized(targetContext.out)
		{
			out = targetContext.out;
		}
		filter(key,targetContext,status,bin,out);
		try
		{
			targetContext.out.flush();
		}
		catch (final IOException e)
		{
		}
		return ByteBuffer.wrap(targetContext.outputPipe.toByteArray());
	}

	/**
	 * Handles a writeable selection key, writing all data
	 * in the output queue to the channel, filtering it
	 * first.
	 *
	 * @param key the selection key
	 * @throws IOException if an error occurs
	 */
	private static void handleWrite(final SelectionKey key) throws IOException
	{
		final SocketChannel channel = (SocketChannel) key.channel();
		final MUDProxy context = (MUDProxy)key.attachment();
		try
		{
			synchronized(context.output)
			{
				while(context.inter.size()>0)
				{
					final ByteBuffer buffer = context.inter.poll();
					if(buffer != null)
					{
						context.output.add(writeFilter(key, context, buffer));
						key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
					}
				}
				while(context.output.size()>0)
				{
					key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
					final ByteBuffer buffer = context.output.peek();
					while (buffer.hasRemaining())
					{
						final int bytesWritten = chanWrite(channel,buffer);
						if (bytesWritten == 0)
						{
							key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
							return;
						}
					}
					if(!buffer.hasRemaining())
						context.output.poll();
				}
			}
			key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
		}
		catch(final NotYetConnectedException e)
		{
			// do nothing, its noise.
			closeKey(key);
		}
		catch(final IOException e)
		{
			closeKey(key);
			Log.errOut(e.getMessage());
		}
		catch(final CancelledKeyException e)
		{
		}
	}

	/**
	 * Commands that can be sent via MPCP from the mud to the proxy server.
	 */
	private static enum MPCPCommand
	{
		SESSIONINFO,
		DISCONNECT,
		DEBUGON,
		DEBUGOFF,
		LISTSESSIONS,
		CLIENTINFO,
		LIST,
		COMMANDS,
		MESSAGE
	}

	/**
	 * Sends an MPCP MESSAGE packet to the mud side.
	 *
	 * @param key the selection key
	 * @param context the MUDProxy context
	 * @param message the message
	 * @throws IOException if an error occurs
	 */
	private static void sendMPCPMsg(final SelectionKey key, final MUDProxy context, final String message) throws IOException
	{
		final byte[] bytes = MUDProxy.makeMPCPPacket("MESSAGE {\"message\":"
				+ "\""+MiniJSON.toJSONString(message)+"\",\"timestamp\":"+System.currentTimeMillis()+"}");
		synchronized (context.output)
		{
			context.output.add(ByteBuffer.wrap(bytes));
		}
		handleWrite(key);
	}

	/**
	 * Processes an MPCP packet received from the mud side.
	 * Handles what few commands are defined for the Proxy server.
	 *
	 * @param key the selection key
	 * @param context the MUDProxy context
	 * @param suboptionData the suboption data containing the MCCP Packet
	 */
	private static void processMPCPPacket(final SelectionKey key, final MUDProxy context, final byte[] suboptionData)
	{
		// context is the target (mud facing) context, anything else is a waste of time.
		if(context.isClient
		&& !context.ipAddress.equals("127.0.0.1")
		&& !context.ipAddress.equals("0:0:0:0:0:0:0:1")
		&& !context.ipAddress.equals("::1"))
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
				final MPCPCommand cmd;
				try
				{
					cmd = MPCPCommand.valueOf(command.toUpperCase());
				}
				catch(final Exception e)
				{
					sendMPCPMsg(key,context,"Unknown command: '"+command+"'");
					return;
				}
				final String jsonStr = str.substring(x+1).trim();
				final MiniJSON.JSONObject obj = new MiniJSON().parseObject(jsonStr);
				final Long timestamp = obj.getCheckedLong("timestamp");
				final long timeout = context.isClient ? 5000 : 1000;
				if(Math.abs(System.currentTimeMillis()-timestamp.longValue())>timeout)
					return;
				obj.remove("timestamp");
				final boolean authorized = obj.containsKey("password")
										&& (obj.getCheckedString("password").equals(ctlPassword));
				switch(cmd)
				{
				case CLIENTINFO:
					// currently ignored
					break;
				case SESSIONINFO:
					context.session.clear();
					context.session.putAll(obj);
					break;
				case DISCONNECT:
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
					break;
				}
				case DEBUGON:
					if(authorized)
					{
						packetDebug = true;
						sendMPCPMsg(key,context,"Packet debugging turned on.");
					}
					else
						sendMPCPMsg(key,context,"Not authorized.");
					break;
				case DEBUGOFF:
					if(authorized)
					{
						packetDebug = false;
						sendMPCPMsg(key,context,"Packet debugging turned off.");
					}
					else
						sendMPCPMsg(key,context,"Not authorized.");
					break;
				case LISTSESSIONS:
					if(authorized)
					{
						final MiniJSON.JSONObject robj = new MiniJSON.JSONObject();
						final List<MiniJSON.JSONObject> sessions = new ArrayList<MiniJSON.JSONObject>();
						for(final SelectionKey k : channelPairs.keySet())
						{
							final SelectionKey pk = channelPairs.get(k);
							final MUDProxy sessProxy = (MUDProxy)k.attachment();
							if(sessProxy.isClient
							&& (sessProxy.outsidePortNum >0))
							{
								final MiniJSON.JSONObject sObj = new MiniJSON.JSONObject();
								sObj.put("type", "client");
								sObj.put("source",sessProxy.ipAddress+":"+sessProxy.outsidePortNum);
								if((channelPairs.containsKey(pk))
								&& (pk.attachment() instanceof MUDProxy)
								&& (!((MUDProxy)pk.attachment()).isClient))
								{
									final MUDProxy pProxy=(MUDProxy)pk.attachment();
									sObj.put("target",pProxy.port.first+":"+pProxy.port.second.toString());
								}
								else
									sObj.put("target","");
								sObj.put("distress",Boolean.valueOf(sessProxy.distressedTime!=0));
								sessions.add(sObj);
							}
							else
							{
								final MiniJSON.JSONObject sObj = new MiniJSON.JSONObject();
								sObj.put("type", "server");
								sObj.put("source",sessProxy.ipAddress+":"+sessProxy.outsidePortNum);
								if((channelPairs.containsKey(pk))
								&& (pk.attachment() instanceof MUDProxy)
								&& (((MUDProxy)pk.attachment()).isClient))
								{
									final MUDProxy pProxy=(MUDProxy)pk.attachment();
									sObj.put("target",pProxy.port.first+":"+pProxy.port.second.toString());
								}
								sObj.put("distress",Boolean.FALSE);
								sessions.add(sObj);
							}
						}
						robj.put("sessions", sessions.toArray());
						robj.put("timestamp", Long.valueOf(System.currentTimeMillis()));
						final byte[] bytes = MUDProxy.makeMPCPPacket(command+" "+robj.toString());
						synchronized(context.output)
						{
							context.output.add(ByteBuffer.wrap(bytes));
						}
						handleWrite(key);
					}
					else
						sendMPCPMsg(key,context,"Not authorized.");
					break;
				case LIST:
				case COMMANDS:
					final StringBuilder cmds = new StringBuilder("Available commands: ");
					for (final MPCPCommand c : MPCPCommand.values())
						cmds.append(c.name()).append(", ");
					if(cmds.length()>2)
						cmds.setLength(cmds.length()-2);
					sendMPCPMsg(key,context,cmds.toString());
					break;
				case MESSAGE:
					Log.sysOut("MPCP",obj.getCheckedString("message"));
					break;
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("MPCP",e);
		}
	}

	/**
	 * An input stream that simply passes bytes through.
	 */
	public static class PassThroughInputStream extends FilterInputStream
	{
		public PassThroughInputStream(final InputStream in)
		{
			super(in);
		}
	}

	/**
	 * A ByteArrayInputStream that can be refilled with a new buffer.
	 */
	public static class RefilByteArrayInputStream extends ByteArrayInputStream
	{
		/**
		 * Constructor
		 *
		 * @param buf the initial buffer, usually empty
		 */
		public RefilByteArrayInputStream(final byte[] buf)
		{
			super(buf);
		}

		/**
		 * Refill this stream with a new buffer.
		 *
		 * @param newBuf the new buffer
		 */
		public void refill(final byte[] newBuf)
		{
			this.buf = newBuf;
			this.count = newBuf.length;
			this.pos = 0;
			this.mark = 0;
		}
	}

	/**
	 * Filter the given input stream through the telnet and ANSI filters,
	 * writing the results to the given output stream.
	 *
	 * @param key the selection key
	 * @param context the MUDProxy context
	 * @param status the parse status
	 * @param in the input stream
	 * @param output the output stream
	 */
	private static void filter(final SelectionKey key, final MUDProxy context, final ParseStatus status, InputStream in, OutputStream output)
	{
		int streamType = Session.TELNET_COMPRESS2;
		try
		{
			int iByte;
			while((iByte = in.read())>=0)
			{
				final byte b=(byte)iByte;
				switch(status.state)
				{
				case NORMAL:
					if(b == (byte)Session.TELNET_IAC)
						status.state = ParseState.IAC;
					else
					if(b == (byte)27)
						status.state = ParseState.ANSI_ESC;
					else
						output.write(b);
					break;
				case IAC:
					if(b == (byte)Session.TELNET_IAC)
					{
						status.state = ParseState.NORMAL;
						output.write(b);
						output.write(b);
					}
					else
					if((b == (byte)Session.TELNET_DO)
					||(b == (byte)Session.TELNET_DONT)
					||(b == (byte)Session.TELNET_WILL)
					||(b == (byte)Session.TELNET_WONT))
					{
						status.state = ParseState.COMMAND;
						status.command = b;
					}
					else
					if(b == (byte)Session.TELNET_SB)
					{
						status.state = ParseState.COMMAND;
						status.command = b;
					}
					else
					{
						status.state = ParseState.NORMAL;
						output.write((byte)Session.TELNET_IAC);
						output.write(b);
					}
					break;
				case COMMAND:
					if(b == (byte)Session.TELNET_MPCP)
					{
						status.mpcpCommand.reset();
						if(status.command==(byte)Session.TELNET_SB)
							status.state = ParseState.SB_202;
						else
						{
							if(status.command == (byte)Session.TELNET_DO)
							{
								if(MUDProxy.distressPingers.containsKey(context.port))
									MUDProxy.distressPingers.get(context.port).third=Boolean.TRUE;
							}
							status.state = ParseState.NORMAL;
						}
					}
					else
					if((b == (byte)Session.TELNET_COMPRESS))
					{
						streamType = Session.TELNET_COMPRESS;
						if(status.command==(byte)Session.TELNET_SB)
						{
							output.write((byte)Session.TELNET_IAC);
							output.write((byte)Session.TELNET_SB);
							output.write((byte)streamType);
							status.state = ParseState.SB_MCCP;
						}
						else
						{
							output.write((byte)Session.TELNET_IAC);
							output.write(status.command);
							output.write(b);
							status.state = ParseState.NORMAL;
						}
					}
					else
					if((b == (byte)Session.TELNET_COMPRESS2))
					{
						streamType = Session.TELNET_COMPRESS2;
						if(status.command==(byte)Session.TELNET_SB)
						{
							output.write((byte)Session.TELNET_IAC);
							output.write((byte)Session.TELNET_SB);
							output.write((byte)streamType);
							status.state = ParseState.SB_MCCP2;
						}
						else
						{
							output.write((byte)Session.TELNET_IAC);
							output.write(status.command);
							output.write(b);
							status.state = ParseState.NORMAL;
						}
					}
					else
					{
						output.write((byte)Session.TELNET_IAC);
						output.write(status.command);
						output.write(b);
						status.state = ParseState.NORMAL;
					}
					break;
				case ANSI_ESC:
					output.write((byte)27);
					output.write(b);
					if(b == (byte)91) // csi sequence
						status.state = ParseState.ANSI_CSI;
					else
						status.state = ParseState.NORMAL;
					break;
				case ANSI_CSI:
					output.write(b);
					if ((b >= (byte)0x40) && (b <= (byte)0x7E))
						status.state = ParseState.NORMAL;
					break;
				case SB_202:
					if(b == (byte)Session.TELNET_IAC)
					{
						if(status.command == b)
						{
							status.mpcpCommand.write(b);
							status.command = 0;
						}
						else
							status.command = b;
					}
					else
					if(b == (byte)Session.TELNET_SE)
					{
						if(status.command == (byte)Session.TELNET_IAC)
						{
							status.state = ParseState.NORMAL;
							if(in == context.in) // only input counts
								processMPCPPacket(key,context,status.mpcpCommand.toByteArray());
							status.mpcpCommand.reset();
						}
						else
							status.mpcpCommand.write(b);
						status.command = 0;
					}
					else
					if(status.mpcpCommand.size()>65536)
					{
						status.mpcpCommand.reset();
						status.state = ParseState.NORMAL;
						status.command = 0;
					}
					else
					{
						status.mpcpCommand.write(b);
						status.command = 0;
					}
					break;
				case SB_MCCP:
					streamType=Session.TELNET_COMPRESS;
					//$FALL-THROUGH$
				case SB_MCCP2:
					if(b == (byte)Session.TELNET_IAC)
					{
						output.write((byte)Session.TELNET_IAC);
						if(status.command != (byte)Session.TELNET_SB)
							status.state = ParseState.NORMAL;
						else
							status.command = b;
					}
					else
					if(b == (byte)Session.TELNET_SE)
					{
						output.write((byte)Session.TELNET_SE);
						if(status.command == (byte)Session.TELNET_IAC)
						{
							status.state = ParseState.NORMAL;
							if(in == context.in)
							{
								@SuppressWarnings("resource")
								final ZInputStream zIn = new ZInputStream(context.inputPipe, false);
								zIn.setFlushMode(JZlib.Z_SYNC_FLUSH);
								context.in = zIn;
								in = context.in;
							}
							else
							{
								@SuppressWarnings("resource")
								final ZOutputStream zOut = new ZOutputStream(context.outputPipe, JZlib.Z_DEFAULT_COMPRESSION);
								zOut.setFlushMode(JZlib.Z_SYNC_FLUSH);
								context.out = zOut;
								output = zOut;
							}
						}
						else
							status.state = ParseState.NORMAL;
					}
					else
					{
						output.write(b);
						status.state = ParseState.NORMAL;
					}
					break;
				}
			}
			if(iByte<0)
				throw new java.net.SocketException();
		}
		catch (final SocketException e)
		{
			//closeKey(key);
		}
		catch (final IOException e)
		{
			Log.errOut(e);
		}
	}

	/**
	 * Reads the given ByteBuffer data, filtering it through the input filter
	 * first, and returning the filtered data as a new ByteBuffer.
	 *
	 * @param key the selection key
	 * @param sourceContext the MUDProxy context
	 * @param buffer the data to read
	 * @return the filtered data ready to write
	 */
	private static ByteBuffer readFilter(final SelectionKey key, final MUDProxy sourceContext, final ByteBuffer buffer)
	{
		final ParseStatus status;
		synchronized(sourceContext)
		{
			status = sourceContext.readStatus;
		}
		final InputStream in;
		synchronized(sourceContext.in)
		{
			in = sourceContext.in;
		}
		final byte[] checkBuf = new byte[buffer.remaining()];
		buffer.get(checkBuf);
		buffer.clear();
		sourceContext.inputPipe.refill(checkBuf);
		if(in instanceof ZInputStream)
			((ZInputStream)in).allowMoreInput();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		filter(key,sourceContext,status,in,output);
		return ByteBuffer.wrap(output.toByteArray());
	}

	/**
	 * Handles a readable selection key, reading all available data from the
	 * channel, filtering it first, and then queuing it for writing to the
	 * paired channel.
	 *
	 * @param key the selection key
	 * @throws IOException if an error occurs
	 */
	private static void handleRead(final SelectionKey key) throws IOException
	{
		final SocketChannel channel = (SocketChannel) key.channel();
		final MUDProxy context = (MUDProxy)key.attachment();
		int bytesRead = 0;
		try
		{
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			while((bytesRead = chanRead(channel,buffer))>0)
			{
				buffer.flip();
				context.input.add(buffer);
				buffer = ByteBuffer.allocate(BUFFER_SIZE);
			}
		}
		catch (final IOException e)
		{
			key.cancel();
			closeKey(key);
			return;
		}

		// handle the proxy server checks first
		if(context.outsidePortNum < 0)
		{
			if(context.input.size()>0)
			{
				final int br = bytesRead;
				MUD.serviceEngine.executeRunnable(new Runnable()
				{
					final int bytesRead = br;
					final SelectionKey k = key;
					final MUDProxy myCtx = context;
					@Override
					public void run()
					{
						synchronized(myCtx.input)
						{
							try
							{
								while(myCtx.input.size()>0)
								{
									final ByteBuffer buffer = myCtx.input.poll();
									if(buffer != null)
										writeFilter(k, myCtx,readFilter(k, myCtx, buffer));
								}
								if(bytesRead<0)
									closeKey(k);
							}
							catch(final Exception e)
							{
								Log.debugOut("handleRead:"+myCtx.toString());
								Log.errOut(e);
							}
						}
					}
				});
			}
			return;
		}
		// now handle normal server->client or client->server reads
		final SelectionKey destKey = channelPairs.get(key);
		if(destKey == null)
		{
			closeKey(key);
			return;
		}
		final SocketChannel destChannel = (SocketChannel) destKey.channel();
		if(destChannel == null)
		{
			closeKey(key);
			return;
		}
		final MUDProxy destContext = (MUDProxy)destKey.attachment();
		try
		{
			boolean eof = false;
			if(bytesRead<0) // means we were never really reading
				eof=true;
			else
			{
				ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
				final LinkedList<ByteBuffer> inputList = new LinkedList<ByteBuffer>();
				synchronized(context.input)
				{
					inputList.addAll(context.input);
					context.input.clear();
				}
				while((bytesRead = chanRead(channel,buffer)) > 0)
				{
					buffer.flip();
					inputList.add(buffer);
					buffer = ByteBuffer.allocate(BUFFER_SIZE);
				}
				if(bytesRead < 0)
				{
					eof = true;
					if((!context.isClient)
					&&(context.outsidePortNum>0))
						putKeyInDistress(key,channel,context,destKey,destChannel,destContext);
				}
				context.pendingInputs.addAll(inputList);
			}
			if((context.pendingInputs.size() > 0)|| eof)
			{
				if(!context.isProcessing.getAndSet(true))
					MUD.serviceEngine.executeRunnable(new ReadProcessor(context, key, destContext, destKey, eof));
			}
		}
		catch(final IOException e)
		{
			if((!context.isClient)&&(destContext!=null))
			{
				if(!destKey.isValid())
				{
					destKey.cancel();
					closeKey(destKey);
				}
				else
					putKeyInDistress(key,channel,context,destKey,destChannel,destContext);
			}
			else
			{
				key.cancel();
				closeKey(key);
			}
		}
	}

	/**
	 * Puts the given server and client keys into distress mode, closing the
	 * server channel, notifying the client, and opening a new server channel to
	 * replace the old one.
	 *
	 * @param serverKey the server selection key
	 * @param serverChannel the server socket channel
	 * @param serverContext the server MUDProxy context
	 * @param clientKey the client selection key
	 * @param clientChannel the client socket channel
	 * @param clientContext the client MUDProxy context
	 */
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
			clientContext.inter.add(ByteBuffer.wrap(bmsg));
			clientKey.interestOps(clientKey.interestOps() | SelectionKey.OP_WRITE);
			clientContext.distressedTime=System.currentTimeMillis()-1000; // quick retry
			synchronized(channelPairs)
			{
				channelPairs.remove(serverKey);
				MUDProxy.trackPort(true,serverContext.port.second, -1,Integer.MAX_VALUE);
			}
			try
			{
				serverChannel.close();
				serverKey.cancel();
			}
			catch (final IOException e) {}
			if (clientKey.isValid())
				handleWrite(clientKey);
			Log.sysOut(serverContext.outsidePortNum+"","Connection suspended "+clientContext.ipAddress
					+"->"+serverContext.port.first+":"+serverContext.port.second);
		}
		catch (final IOException e)
		{
			Log.errOut(e);
		}
	}

	/**
	 * Closes the given selection key and its paired key, if any. If the key is
	 * a server key, it will be put into distress mode instead of being closed
	 * outright.
	 *
	 * @param key the selection key to close
	 */
	private static void closeKey(final SelectionKey key)
	{
		if (key == null)
			return;
		final SocketChannel channel = (SocketChannel) key.channel();
		final MUDProxy context = (MUDProxy)key.attachment();
		//if(!channelPairs.containsKey(key)) // this made distress pingers no longer close
		//	return; // this is an unknown key, ignore it.
		Log.debugOut("closeKey:"+context.toString()+", valid="+key.isValid());
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
			}
		}
		if((!context.isClient) // if its a server socket,
		&&(pairedChannel!=null)
		&&(context.outsidePortNum>0))
		{
			putKeyInDistress(key,channel,context,pairedKey,pairedChannel,pairedContext);
			pairedChannel = null; // so we don't close it below
		}
		try
		{
			if (pairedChannel != null)
			{
				synchronized(channelPairs)
				{
					channelPairs.remove(pairedKey);
					if((pairedContext != null) && (!pairedContext.isClient))
						MUDProxy.trackPort(true,pairedContext.port.second, -1,Integer.MAX_VALUE);
				}
				pairedChannel.close();
			}
		}
		catch (final IOException e) {}
		try
		{
			channel.close();
		}
		catch (final IOException e) {}
		key.cancel();
		Log.sysOut(context.outsidePortNum+"","Connection lost "+context.ipAddress
				+"->"+context.port.first+":"+context.port.second);
	}

	public static void reconnectClient(final SelectionKey key, final MUDProxy context, final MUDProxy server)
	{
		synchronized(runnables)
		{
			runnables.add(new Runnable()
			{
				final SelectionKey clientKey = key;
				final MUDProxy clientContext = context;
				final MUDProxy serverContext = server;
				@Override
				public void run()
				{
					try
					{
						clientContext.distressedTime = System.currentTimeMillis(); // update so we don't try again too soon
						final SocketChannel newChannel=SocketChannel.open();
						newChannel.configureBlocking(false);
						newChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
						final SelectionKey nkey = newChannel.register(selector, SelectionKey.OP_CONNECT, serverContext);
						nkey.interestOps(SelectionKey.OP_CONNECT);
						channelPairs.put(clientKey, nkey);
						channelPairs.put(nkey, clientKey);
						newChannel.connect(new InetSocketAddress(serverContext.port.first,serverContext.port.second.intValue()));
					}
					catch(final Exception e)
					{
						Log.debugOut("disThread:"+context.toString());
						Log.errOut(e);
					}
				}
			});
			selector.wakeup();
		}
	}

	public static void startPinger(final Pair<String,Integer> port)
	{
		synchronized(runnables)
		{
			runnables.add(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final SocketChannel pingChannel=SocketChannel.open();
						pingChannel.configureBlocking(false);
						pingChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
						pingChannel.connect(new InetSocketAddress(port.first, port.second.intValue()));
						final SelectionKey key = pingChannel.register(selector, SelectionKey.OP_CONNECT, new MUDProxy(false,-1,port,"PROXY"));
						key.interestOps(SelectionKey.OP_CONNECT);
						final Triad<SelectionKey,Long,Boolean> pinger = new Triad<SelectionKey,Long,Boolean>(key,Long.valueOf(System.currentTimeMillis()), Boolean.FALSE);
						distressPingers.put(port, pinger);
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
				}
			});
			selector.wakeup();
		}
	}

	/**
	 * A background thread that periodically checks for connections in distress
	 * mode, and attempts to re-establish them.
	 */
	public static Thread distressThread = new Thread()
	{
		@Override
		public void run()
		{
			super.setName("PROXY_DISTRESS");
			try
			{
				while(true)
				{
					if(runnables.size()>0)
						selector.wakeup();
					Thread.sleep((MUDProxy.distressPingers.size()>0)?2000:10000);
					try
					{
						final Map<SelectionKey, SelectionKey> pairs = new HashMap<SelectionKey, SelectionKey>();
						synchronized(channelPairs)
						{
							pairs.putAll(channelPairs);
						}
						final Map<Pair<String,Integer>, List<Triad<SelectionKey,MUDProxy,MUDProxy>>> distresses =
								new HashMap<Pair<String,Integer>, List<Triad<SelectionKey,MUDProxy,MUDProxy>>>();
						for(final SelectionKey key : pairs.keySet())
						{
							final MUDProxy clientProxy = (MUDProxy)key.attachment();
							if((clientProxy != null)
							&& (clientProxy.isClient)
							&& (clientProxy.distressedTime!=0))
							{
								final SelectionKey serverKey = pairs.get(key);
								final MUDProxy serverProxy = (MUDProxy)serverKey.attachment();
								if(!distresses.containsKey(serverProxy.port))
									distresses.put(serverProxy.port, new ArrayList<Triad<SelectionKey,MUDProxy,MUDProxy>>());
								distresses.get(serverProxy.port).add(new Triad<SelectionKey,MUDProxy,MUDProxy>(key,clientProxy,serverProxy));
							}
						}
						// clean out deprecated distress pingers
						for(final Pair<String,Integer> serverPort : allPorts.values())
							if(!distresses.containsKey(serverPort))
							{
								final Triad<SelectionKey,Long,Boolean> p = MUDProxy.distressPingers.get(serverPort);
								if(p != null)
								{
									p.first.cancel();
									MUDProxy.distressPingers.remove(serverPort);
								}
							}
						if(distresses.size() > 0)
						{
							final Set<Pair<String,Integer>> toPings = new HashSet<Pair<String,Integer>>();
							for(final Pair<String,Integer> port : distresses.keySet())
							{
								final Triad<SelectionKey,Long,Boolean> p = MUDProxy.distressPingers.get(port);
								if((p != null)&&p.third.booleanValue())
								{
									final List<Triad<SelectionKey,MUDProxy,MUDProxy>> clientList = distresses.get(port);
									for(final Iterator<Triad<SelectionKey,MUDProxy,MUDProxy>> i = clientList.iterator();i.hasNext();)
									{
										final Triad<SelectionKey,MUDProxy,MUDProxy> disPair = i.next();
										final MUDProxy clientContext = disPair.second;
										if((clientContext.distressedTime>0)
										&&((System.currentTimeMillis()-clientContext.distressedTime)>10000))
											MUDProxy.reconnectClient(disPair.first, clientContext, disPair.third);
										// else, if client is not yet 10 seconds in distress, but pinger says OK, wait
									}
								}
								else
									toPings.add(port);
							}
							if(toPings.size()>0)
							{
								for(final Pair<String,Integer> port : toPings)
								{
									final Triad<SelectionKey,Long,Boolean> p = MUDProxy.distressPingers.get(port);
									if ((p != null)
									&& (System.currentTimeMillis() - p.second.longValue()) < 10000)
										continue;
									MUDProxy.startPinger(port);
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

	/**
	 * Reads bytes from the given channel into the given ByteBuffer. If packet
	 * debugging is enabled, the bytes will be logged.
	 *
	 * @param chan the channel to read from
	 * @param bytes the buffer to read into
	 * @return the number of bytes read
	 * @throws IOException if an error occurs
	 */
	private static int chanRead(final SocketChannel chan, final ByteBuffer bytes) throws IOException
	{
		final int numRead = chan.read(bytes);
		if (packetDebug && (numRead > 0))
			Log.debugOut("PROXY", "RCVD:\n" + bytesToHexAscii(Arrays.copyOfRange(bytes.array(), 0, numRead)));
		return numRead;
	}

	/**
	 * Writes bytes from the given ByteBuffer to the given channel. If packet
	 * debugging is enabled, the bytes will be logged.
	 *
	 * @param chan the channel to write to
	 * @param bytes the buffer to write from
	 * @return the number of bytes written
	 * @throws IOException if an error occurs
	 */
	private static int chanWrite(final SocketChannel chan, final ByteBuffer bytes) throws IOException
	{
		if(packetDebug)
			Log.debugOut("PROXY","SENT:\n"+bytesToHexAscii(bytes.array()));
		return chan.write(bytes);
	}

	/**
	 * Converts the given byte array to a hex+ascii string, suitable for
	 * debugging.
	 *
	 * @param bytes the bytes to convert
	 * @return the hex+ascii string
	 */
	private static String bytesToHexAscii(final byte[] bytes)
	{
		if((bytes == null)||(bytes.length == 0))
			return "";
		final StringBuilder result = new StringBuilder();
		final int bytesPerLine = 16;
		for (int i = 0; i < bytes.length; i += bytesPerLine)
		{
			final StringBuilder hex = new StringBuilder();
			final StringBuilder ascii = new StringBuilder();
			for (int j = 0; j < bytesPerLine && i + j < bytes.length; j++)
			{
				final byte b = bytes[i + j];
				hex.append(String.format("%02X ", Byte.valueOf(b)));
				ascii.append(((b >= 32)&&(b <= 126))?(char)b:'.');
			}
			String hexStr = hex.toString();
			if (hexStr.length() < bytesPerLine * 3)
				hexStr = String.format("%-" + (bytesPerLine * 3) + "s", hexStr);
			result.append(hexStr).append("  ").append(ascii).append("\n");
		}
		return result.toString();
	}

	@Override
	public String toString()
	{
		final String cs = isClient?"client":"server";
		final String outs=(out instanceof ZOutputStream)?"zout":"out";
		final String ins=(in instanceof ZInputStream)?"zin":"in";
		final String dis=(this.distressedTime!=0)?"distressed":"ok";
		return cs+":"+ins+"/"+outs+":"+dis+":"+outsidePortNum+":"+this.ipAddress+" ("+this.port.first+":"+this.port.second+")";
	}
}
