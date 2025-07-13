package com.planet_ink.coffee_mud.application;

import java.util.Hashtable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.net.*;

import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;

public class MUDProxy
{
	private static final int BUFFER_SIZE = 65536;
	private static final Map<SocketChannel, SocketChannel> channelPairs = new HashMap<>();
	private static String mpcpKey = "";

	private static enum ParseState
	{
		NORMAL,
		IAC,
		COMMAND,
		SB_202,
		ANSI_ESC,
		ANSI_CSI
	}

	private final long						connectionTime		= System.currentTimeMillis();
	private final Pair<String, Integer>		port;
	private final String					ipAddress;
	private final boolean					isClient;
	private 	  ParseState				readState			= ParseState.NORMAL;
	private		  byte						readCommand			= 0;
	private final		  ByteArrayOutputStream		mpcpCommand			= new ByteArrayOutputStream();
	private final LinkedList<ByteBuffer>	output				= new LinkedList<ByteBuffer>();

	private MUDProxy(final boolean client, final Pair<String,Integer> port, final String remoteIP)
	{
		this.isClient = client;
		this.port=port;
		this.ipAddress=remoteIP;
	}

	public static void main(final String a[])
	{
		final Map<Integer,Pair<String,Integer>> portMap = new Hashtable<Integer,Pair<String,Integer>>();
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
			final String port  = page.getPrivateStr("PORT");
			if((proxy != null)&&(proxy.trim().length()>0)
			&&(port != null)&&(port.trim().length()>0))
			{
				final List<String> proxies = CMParms.parseCommas(proxy, true);
				final List<String> ports = CMParms.parseCommas(port, true);
				int portNum = 0;
				for(final String portStr : ports)
				{
					final int portN = CMath.s_int(portStr);
					if(portN > 0)
					{
						portNum = portN;
						break;
					}
				}
				if(portNum <= 0)
				{
					Log.errOut(Thread.currentThread().getName(),"ERROR: Unable to read port from ini file: '"+iniFile+"'.");
					System.err.println("PROXY/ERROR: Unable to read port from ini file: '"+iniFile+"'.");
					System.exit(-1);
					return;
				}
				for(String proxyPort : proxies)
				{
					final int paren = proxyPort.indexOf('(');
					String host = "localhost";
					if((paren > 0) && (proxyPort.endsWith(")")))
					{
						host = proxyPort.substring(paren+1,proxyPort.length()-1).trim();
						proxyPort = proxyPort.substring(0,paren);
					}
					final int proxyNum = CMath.s_int(proxyPort);
					if(proxyNum <= 0)
					{
						Log.errOut(Thread.currentThread().getName(),"ERROR: Invalid proxy port '"+proxyPort+" in ini file: '"+iniFile+"'.");
						System.err.println("PROXY/ERROR: Invalid proxy port '"+proxyPort+" in ini file: '"+iniFile+"'.");
						System.exit(-1);
						return;
					}
					if(portMap.containsKey(Integer.valueOf(proxyNum)))
					{
						Log.errOut(Thread.currentThread().getName(),"ERROR: Duplicate proxy port '"+proxyPort+" in ini file: '"+iniFile+"'.");
						System.err.println("PROXY/ERROR: Duplicate proxy port '"+proxyPort+" in ini file: '"+iniFile+"'.");
						System.exit(-1);
						return;
					}
					portMap.put(Integer.valueOf(proxyNum), new Pair<String,Integer>(host,Integer.valueOf(portNum)));
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
			for(final Integer proxyPort : portMap.keySet())
			{
				final ServerSocketChannel serverChannel = ServerSocketChannel.open();
				serverChannel.configureBlocking(false);
				serverChannel.bind(new InetSocketAddress(proxyPort.intValue()));
				final Pair<String,Integer> fw = portMap.get(proxyPort);
				serverChannel.register(selector, SelectionKey.OP_ACCEPT, new MUDProxy(false,fw,"localhost"));
				Log.sysOut("Listening on port " + proxyPort + " -> " + fw.first + ":" + fw.second);
			}
			while(true)
			{
				try
				{
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
								final MUDProxy serverContext = (MUDProxy)key.attachment();
								final InetSocketAddress clientAddress = (InetSocketAddress) clientChannel.getRemoteAddress();
								final String clientIp = clientAddress.getAddress().getHostAddress();
								final InetSocketAddress localAddress = (InetSocketAddress) serverChannel.getLocalAddress();
								final int listenPort = localAddress.getPort();
								clientChannel.configureBlocking(false);
								final SocketChannel targetChannel = SocketChannel.open();
								targetChannel.configureBlocking(false);
								final Pair<String,Integer> targetPort = portMap.get(Integer.valueOf(listenPort));
								targetChannel.connect(new InetSocketAddress(targetPort.first, targetPort.second.intValue()));
								channelPairs.put(clientChannel, targetChannel);
								channelPairs.put(targetChannel, clientChannel);
								clientChannel.register(selector, SelectionKey.OP_READ, new MUDProxy(true,serverContext.port,serverContext.ipAddress));
								targetChannel.register(selector, SelectionKey.OP_CONNECT, new MUDProxy(false,targetPort,clientIp));
								Log.sysOut(listenPort+"","Connection from "+clientIp+"->"+targetPort.first+":"+targetPort.second);
							}
							else
							if(key.isConnectable())
							{
								final SocketChannel targetChannel = (SocketChannel) key.channel();
								try
								{
									if (targetChannel.finishConnect())
									{
										final MUDProxy context = (MUDProxy)key.attachment();
										targetChannel.write(ByteBuffer.wrap(new byte[] {
											(byte)Session.TELNET_IAC,
											(byte)Session.TELNET_WILL,
											(byte)Session.TELNET_MPCP
										}));
										targetChannel.write(makeMPCPPacket("ClientInfo {"
												+ "\"client_address\":\""+context.ipAddress+"\","
												+ "\"timestamp\":"+System.currentTimeMillis()+"}"));
										key.interestOps(SelectionKey.OP_READ);
									}
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
								handleWrite(key);
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

	private static ByteBuffer makeMPCPPacket(final String command)
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
			return ByteBuffer.wrap(packetOut.toByteArray());
		}
		catch(final Exception e)
		{
			Log.errOut(e);
		}
		return ByteBuffer.allocate(0);
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

	private static void processMPCPPacket(final byte[] command)
	{
		//TODO:BZ:
	}

	private static void readFilter(final MUDProxy sourceContext, final ByteBuffer buffer)
	{
		final byte[] checkBuf = new byte[buffer.remaining()];
		buffer.get(checkBuf);
		buffer.clear();
		for(int i=0;i<checkBuf.length;i++)
		{
			final byte b=checkBuf[i];
			switch(sourceContext.readState)
			{
			case NORMAL:
				if(b == (byte)Session.TELNET_IAC)
					sourceContext.readState = ParseState.IAC;
				else
				if(b == (byte)27)
					sourceContext.readState = ParseState.ANSI_ESC;
				else
					buffer.put(b);
				break;
			case IAC:
				if(b == (byte)Session.TELNET_IAC)
				{
					sourceContext.readState = ParseState.NORMAL;
					buffer.put(b);
					buffer.put(b);
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
					buffer.put((byte)255);
					buffer.put(b);
				}
				break;
			case COMMAND:
				if(b == (byte)Session.TELNET_MPCP)
				{
					sourceContext.mpcpCommand.reset();
					if(sourceContext.readCommand==(byte)Session.TELNET_IAC)
						sourceContext.readState = ParseState.SB_202;
					else
						sourceContext.readState = ParseState.NORMAL;
				}
				else
				{
					buffer.put((byte)255);
					buffer.put(sourceContext.readCommand);
					buffer.put(b);
					sourceContext.readState = ParseState.NORMAL;
				}
				break;
			case ANSI_ESC:
				buffer.put((byte)27);
				buffer.put(b);
				if(b == (byte)91) // csi sequence
					sourceContext.readState = ParseState.ANSI_CSI;
				else
					sourceContext.readState = ParseState.NORMAL;
				break;
			case ANSI_CSI:
				buffer.put(b);
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
					if(sourceContext.readCommand == b)
					{
						sourceContext.readState = ParseState.NORMAL;
						processMPCPPacket(sourceContext.mpcpCommand.toByteArray());
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
			}
		}
		buffer.flip();
	}

	private static void handleRead(final SelectionKey key) throws IOException
	{
		final SocketChannel sourceChannel = (SocketChannel) key.channel();
		final MUDProxy sourceContext = (MUDProxy)key.attachment();
		final SocketChannel destChannel = channelPairs.get(sourceChannel);
		if (destChannel == null)
		{
			closeKey(key);
			return;
		}
		final SelectionKey destKey = destChannel.keyFor(key.selector());
		final MUDProxy destContext = (MUDProxy)destKey.attachment();
		int bytesRead;
		try
		{
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			while((bytesRead = sourceChannel.read(buffer))>0)
			{
				buffer.flip();
				readFilter(sourceContext, buffer);
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
			closeKey(key);
			return;
		}
	}

	private static void closeKey(final SelectionKey key)
	{
		if (key == null)
			return;
		final SocketChannel channel = (SocketChannel) key.channel();
		final SocketChannel pairedChannel = channelPairs.remove(channel);
		channelPairs.remove(pairedChannel);
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
	}
}
