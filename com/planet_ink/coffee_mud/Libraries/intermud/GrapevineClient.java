package com.planet_ink.coffee_mud.Libraries.intermud;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.CMThreadFactory;
import com.planet_ink.coffee_mud.core.threads.CMThreadPoolExecutor;
import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.DataBuffers;
import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.util.CWDataBuffers;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMLib.Library;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.Achievements;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.intermud.i3.I3Client;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.SipletInterface.WSState;

import java.util.*;
import java.util.concurrent.*;

import javax.net.ssl.SSLSocketFactory;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.io.*;

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
public class GrapevineClient extends Thread implements Closeable
{
	protected volatile boolean	shutdown		= false;
	protected Socket			clientSocket	= null;
	protected long				lastDelay		= 500L;
	protected final String		webSockKey;
	protected String			serverKey		= null;
	protected volatile WSState	state			= WSState.S0;
	protected volatile int		subState		= 0;
	protected volatile long		dataLen			= 0;
	protected volatile byte		opCode			= 0;
	protected volatile boolean	finished		= false;
	protected volatile long		lastPing		= System.currentTimeMillis();
	protected final String[]	auth;
	protected Room				universalR		= null;

	protected final List<ChannelsLibrary.CMChannel>	channels;
	protected final Map<String,MiniJSON.JSONObject>	knownMuds	= new ConcurrentHashMap<String,MiniJSON.JSONObject>();
	protected final Map<String,MOB>					playerReq	= new ConcurrentHashMap<String,MOB>();
	protected final Map<String,Achievement>			achieves	= new ConcurrentHashMap<String,Achievement>();
	protected final ConcurrentLinkedQueue<byte[]>	outgoing	= new ConcurrentLinkedQueue<byte[]>();
	protected final ByteArrayOutputStream			payload		= new ByteArrayOutputStream();
	protected final ByteArrayOutputStream			msg			= new ByteArrayOutputStream();
	private static final byte[]						pingFrame	= new byte[0];

	protected final Random rand = new Random(System.nanoTime());

	public GrapevineClient(final String clientId, final String clientSecret, final List<ChannelsLibrary.CMChannel> channels)
	{
		this.channels = channels;
		auth = new String[] {clientId, clientSecret};
		final byte[] randy = new byte[16];
		rand.nextBytes(randy);
		webSockKey = Base64.getEncoder().encodeToString(randy);
	}

	protected static enum WSPType
	{
		CONTINUE(0),
		TEXT(1),
		BINARY(2),
		CLOSE(8),
		PING(9),
		PONG(10)
		;
		public final int opCode;
		private WSPType(final int opcode)
		{
			this.opCode = opcode;
		}
	}

	protected boolean needsReconnect()
	{
		return ((clientSocket == null)||(!clientSocket.isConnected())||(clientSocket.isClosed()));
	}

	protected void ping()
	{
		try
		{
			this.sendPacket(pingFrame, WSPType.PING);
			this.lastPing = System.currentTimeMillis();
		}
		catch (final IOException e)
		{
			closeSocket();
		}
	}

	protected Room getUniversalRoom()
	{
		if(universalR==null)
		{
			universalR=CMClass.getLocale("StdRoom");
		}
		return universalR;
	}

	protected void pong()
	{
		try
		{
			this.sendPacket(pingFrame, WSPType.PING);
			this.lastPing = System.currentTimeMillis();
		}
		catch (final IOException e)
		{
			closeSocket();
		}
	}

	public Collection<MiniJSON.JSONObject> getKnownMuds()
	{
		return knownMuds.values();
	}

	public boolean isOnline()
	{
		return this.clientSocket != null && !shutdown && clientSocket.isConnected();
	}

	@Override
	public void run()
	{
		while(!shutdown)
		{
			try
			{
				if(needsReconnect())
				{
					if(!reconnect())
						continue;
				}

				byte[] chunk = new byte[4096];
				int bytesRead;
				try
				{
					bytesRead = clientSocket.getInputStream().read(chunk);
				}
				catch(final SocketTimeoutException ste)
				{
					bytesRead = 0;
				}
				if (bytesRead > 0)
				{
					chunk = Arrays.copyOf(chunk, bytesRead);
					processBuffer(chunk);
				}
				else
				{
					if((System.currentTimeMillis() - lastPing)>60000L)
						ping();
					Thread.sleep(1000);
					dequeOutgoing();
				}
			}
			catch(final SocketException ste)
			{
				try
				{
					if(clientSocket != null)
						clientSocket.close();
				}
				catch(final IOException ioe)
				{}
				clientSocket = null;
			}
			catch(final Exception e)
			{
				try
				{
					Log.errOut(e);
					if(clientSocket != null)
						clientSocket.close();
				}
				catch(final IOException ioe)
				{}
				clientSocket = null;
			}
		}
	}

	protected void dequeOutgoing()
	{
		if(outgoing.size()>0)
		{
			for(int i=0;i<5 && outgoing.size()>0;i++)
			{
				try
				{
					this.sendPacket(outgoing.remove(), WSPType.TEXT);
				}
				catch (final IOException e)
				{
				}
			}
		}
	}

	protected String getMudInfo(final MiniJSON.JSONObject pobj) throws MiniJSON.MJSONException
	{
		final String name = (String)pobj.get("game");
		final String displayName = (String)pobj.get("display_name");
		final String description = (String)pobj.get("description");
		final String homepageUrl = (String)pobj.get("homepage_url");
		final String userAgent = (String)pobj.get("user_agent");
		final String userAgentRepoUrl = (String)pobj.get("user_agent_repo_url");
		final Object[] cconnections = (Object[])pobj.get("connections");
		String[] connections = new String[0];
		if(cconnections != null)
		{
			connections = new String[cconnections.length];
			for(int i=0;i<cconnections.length;i++)
			{
				final MiniJSON.JSONObject cobj = (MiniJSON.JSONObject)cconnections[i];
				String conn;
				if (cobj.getCheckedString("type").equalsIgnoreCase("telnet"))
					conn = "telnet "+cobj.getCheckedString("host")+":"+cobj.getCheckedLong("port");
				else
				if (cobj.getCheckedString("type").equalsIgnoreCase("secure telnet"))
					conn = "ssh "+cobj.getCheckedString("host")+":"+cobj.getCheckedLong("port");
				else
				if (cobj.getCheckedString("type").equalsIgnoreCase("web"))
					conn = cobj.getCheckedString("url");
				else
					conn = "";
				connections[i]=conn;
			}
		}
		final Object[] csupports = (Object[])pobj.get("supports");
		String[] supports = new String[0];
		if(csupports != null)
		{
			supports = new String[csupports.length];
			for(int i=0;i<csupports.length;i++)
				supports[i] = (String)csupports[i];
		}
		final Object[] cchannels = (Object[])pobj.get("channels");
		String[] channels = new String[0];
		if(cchannels != null)
		{
			channels = new String[cchannels.length];
			for(int i=0;i<cchannels.length;i++)
				channels[i] = (String)cchannels[i];
		}
		final String online = ""+pobj.get("players_online_count");
		final StringBuilder str = new StringBuilder("");
		str.append(CMStrings.padRight(CMLib.lang().L("Game name"), 15)).append(name).append("\n\r");
		if((displayName.length()>0)&&(!name.equalsIgnoreCase(displayName)))
			str.append(CMStrings.padRight(CMLib.lang().L("Display name"), 15)).append(displayName).append("\n\r");
		str.append(CMStrings.padRight(CMLib.lang().L("Description"), 15)).append(description).append("\n\r");
		str.append(CMStrings.padRight(CMLib.lang().L("Homepage"), 15)).append(homepageUrl).append("\n\r");
		if(userAgent != null)
			str.append(CMStrings.padRight(CMLib.lang().L("User Agent"), 15)).append(userAgent).append("\n\r");
		if(userAgentRepoUrl != null)
			str.append(CMStrings.padRight(CMLib.lang().L("User Repo"), 15)).append(userAgentRepoUrl).append("\n\r");
		if(online != null)
			str.append(CMStrings.padRight(CMLib.lang().L("Online"), 15)).append(online).append("\n\r");
		for(final String connection : connections)
			str.append(CMStrings.padRight(CMLib.lang().L("Connection"), 15)).append(connection).append("\n\r");
		str.append(CMStrings.padRight(CMLib.lang().L("Supports"), 15)).append(CMParms.toListString(supports)).append("\n\r");
		str.append(CMStrings.padRight(CMLib.lang().L("Channels"), 15)).append(CMParms.toListString(channels)).append("\n\r");
		return str.toString();
	}

	public TriadList<String,String,String> getRemoteChannelsList()
	{
		final TriadList<String,String,String> list = new TriadVector<String,String,String>(0);
		list.add("gossip","grapevine","");
		list.add("testing","grapevine","");
		list.add("moo","grapevine","");
		return list;
	}

	public String isPlayerOnMud(final String mudName, final String name)
	{
		final MiniJSON.JSONObject mudObj = knownMuds.get(mudName.toLowerCase().trim());
		if(mudObj != null)
		{
			try
			{
				if(mudObj.containsKey("players"))
				{
					final Object[] objs = mudObj.getCheckedArray("players");
					for(final Object o : objs)
					{
						if(o.toString().equalsIgnoreCase(name))
							return mudObj.getCheckedString("game");
					}
				}
			}
			catch(final MiniJSON.MJSONException x)
			{
				Log.errOut(x);
			}
		}
		return null;
	}

	public List<String> getPlayerOnMuds(final String name)
	{
		final List<String> list = new Vector<String>();
		for(final String key : knownMuds.keySet())
		{
			final String mName = isPlayerOnMud(key, name);
			if(mName!=null)
				list.add(mName);
		}
		return list;
	}

	protected Pair<byte[], WSPType> processTextInput(final String input) throws IOException
	{
		final MiniJSON jsoner = new MiniJSON();
		try
		{
			MiniJSON.JSONObject obj = (MiniJSON.JSONObject)jsoner.parse(input);
			if(!obj.containsKey("event"))
			{
				Log.errOut("Unknown Grapevine event: "+input);
				return null;
			}
			final String event = obj.getCheckedString("event");
			if(event.equalsIgnoreCase("heartbeat"))
			{
				obj = new MiniJSON.JSONObject();
				obj.put("event", "heartbeat");
				final MiniJSON.JSONObject pobj = new MiniJSON.JSONObject();
				final Set<String> players = new TreeSet<String>();
				for(final Enumeration<CMLibrary> s= CMLib.libraries(Library.SESSIONS);s.hasMoreElements();)
				{
					final SessionsList sLib = (SessionsList)s.nextElement();
					for(final Session S :  sLib.localOnlineIterableAllHosts())
					{
						if(S.mob()!=null)
							players.add(S.mob().Name());
					}
				}
				final Object[] o = new Object[players.size()];
				int i=0;
				for(final String name : players)
					o[i++] = name;
				pobj.put("players", o);
				obj.put("payload",pobj);
				sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
			}
			else
			if(event.equalsIgnoreCase("authenticate"))
			{
				if((!obj.containsKey("status"))
				||(!obj.getCheckedString("status").equalsIgnoreCase("success")))
				{
					Log.errOut("Shutting down grapevine Interface, due to auth failure");
					close();
				}
				else
				{
					gameStatusReq(null,null);
					playerStatusReq(null, null);
					achievementsSync();
				}
			}
			else
			if(event.equalsIgnoreCase("restart"))
			{
				if(obj.containsKey("payload"))
				{
					//final int downtime = obj.getCheckedJSONObject("payload").getCheckedLong("downtime").intValue();
					// that's interesting, but what can I do about it?
				}
			}
			else
			if(event.equalsIgnoreCase("channels/broadcast"))
			{
				if(obj.containsKey("payload"))
				{
					final MiniJSON.JSONObject pobj = obj.getCheckedJSONObject("payload");
					final String channel = pobj.getCheckedString("channel");
					final String game = pobj.getCheckedString("game");
					final String player = pobj.getCheckedString("name");
					final String message = pobj.getCheckedString("message");
					try
					{
						this.relayChannelMessage(player, game, channel, message);
					}
					catch(final Exception e)
					{
						Log.errOut(e);
					}
				}
			}
			else
			if(event.equalsIgnoreCase("players/status"))
			{
				if(obj.containsKey("payload"))
				{
					final MiniJSON.JSONObject pobj = obj.getCheckedJSONObject("payload");
					final String game = pobj.getCheckedString("game");
					final Object[] players = pobj.getCheckedArray("players");
					if(obj.containsKey("ref"))
					{
						final String ref = obj.getCheckedString("ref");
						if(playerReq.containsKey(ref))
						{
							final MOB M = playerReq.remove(ref);
							M.tell(CMLib.lang().L("@x1 has @x2 players online: @x3",game,""+players.length,CMParms.toListString(players)));
						}
					}
					final MiniJSON.JSONObject mud = knownMuds.get(game.toLowerCase());
					mud.put("players", players);
				}
			}
			else
			if(event.equalsIgnoreCase("tells/send"))
			{
				// success or failure, does it matter?
			}
			else
			if(event.equalsIgnoreCase("tells/receive"))
			{
				if(obj.containsKey("payload"))
				{
					final MiniJSON.JSONObject pobj = obj.getCheckedJSONObject("payload");
					final String fromGame = pobj.getCheckedString("from_game");
					final String fromName = pobj.getCheckedString("from_name");
					final String to_name = pobj.getCheckedString("to_name");
					final String message = pobj.getCheckedString("message");
					final MOB smob=CMLib.sessions().findCharacterOnline(to_name, true);
					if(smob!=null)
					{
						MOB mob=null;
						final PlayerStats pstats=smob.playerStats();
						if(pstats!=null)
						{
							if((pstats.getReplyToMOB()!=null)&&(pstats.getReplyToMOB().Name().indexOf('@')>=0))
								mob=pstats.getReplyToMOB();
							else
								mob=CMClass.getFactoryMOB();
							pstats.setReplyTo(mob, PlayerStats.REPLY_TELL);
						}
						else
							mob=CMClass.getFactoryMOB();
						mob.setName(fromName+"@"+fromGame);
						mob.setLocation(getUniversalRoom());
						final String fmessage=CMStrings.removeColors(CMProps.applyINIFilter(message,CMProps.Str.SAYFILTER));
						CMLib.commands().postSay(mob,smob,fmessage,true,true);
					}
				}
			}
			else
			if(event.equalsIgnoreCase("games/connect"))
			{
				if(obj.containsKey("payload"))
				{
					final MiniJSON.JSONObject pobj = obj.getCheckedJSONObject("payload");
					final String game = pobj.getCheckedString("game");
					this.gameStatusReq(null,game);
				}
			}
			else
			if(event.equalsIgnoreCase("games/disconnect"))
			{
				if(obj.containsKey("payload"))
				{
					final MiniJSON.JSONObject pobj = obj.getCheckedJSONObject("payload");
					final String game = pobj.getCheckedString("game");
					this.knownMuds.remove(game.toLowerCase());
				}
			}
			else
			if(event.equalsIgnoreCase("games/status"))
			{
				if(obj.containsKey("payload"))
				{
					final MiniJSON.JSONObject pobj = obj.getCheckedJSONObject("payload");
					final String game = pobj.getCheckedString("game");
					knownMuds.put(game.toLowerCase().trim(), pobj);
					if(obj.containsKey("ref"))
					{
						final String ref = obj.getCheckedString("ref");
						if(this.playerReq.containsKey(ref))
						{
							final MOB M = this.playerReq.remove(ref);
							final String info = this.getMudInfo(pobj);
							final StringBuilder buf=new StringBuilder("\n\r^HGrapevine:^?\n\r");
							buf.append(info);
							if(M.session()!=null)
								M.session().print(buf.toString());
						}
					}
				}
			}
			else
			if(event.equalsIgnoreCase("achievements/update"))
			{
				// assume all is well?
			}
			else
			if(event.equalsIgnoreCase("achievements/sync")||event.equalsIgnoreCase("achievements/create"))
			{
				if(obj.containsKey("payload"))
				{
					final MiniJSON.JSONObject pobj = obj.getCheckedJSONObject("payload");
					final int total;
					final Object[] aobjs;
					if(event.equals("achievements/create"))
					{
						total=-1;
						aobjs = new Object[] {pobj};
					}
					else
					{
						total = pobj.getCheckedLong("total").intValue();
						aobjs = pobj.getCheckedArray("achievements");
					}
					for(final Object o : aobjs)
					{
						final MiniJSON.JSONObject aobj = (MiniJSON.JSONObject)o;
						final String key = aobj.getCheckedString("key");
						final String title = aobj.getCheckedString("title");
						final String aid = CMLib.achievements().findAchievementID(title, true);
						if(aid == null)
							achievementsDelete(key);
						else
						{
							boolean progress=true;
							if(aobj.containsKey("partial_progress"))
								progress=aobj.getCheckedBoolean("partial_progress").booleanValue();
							final Achievement A = CMLib.achievements().getAchievement(aid);
							if(A != null)
							{
								achieves.put(key, A);
								if((!A.getDisplayStr().equals(title))
								||((A.getTargetCount()!=Integer.MIN_VALUE)!=progress))
									this.achievementsUpdate(key, A);
							}
						}
					}
					if((total >=0) && (total <= achieves.size()))
					{
						final Set<Achievement> done = new HashSet<Achievement>();
						for(final Achievement A : achieves.values())
							done.add(A);
						for(final Enumeration<Achievement> a=CMLib.achievements().achievements(AccountStats.Agent.PLAYER);a.hasMoreElements();)
						{
							final Achievement A = a.nextElement();
							if(!done.contains(A))
								this.achievementsCreate(A);
						}
					}
				}
			}
		}
		catch (final MJSONException e)
		{
			Log.errOut(e);
		}
		return null;
	}

	protected void sendPacket(final byte[] buf, final WSPType type) throws IOException
	{
		if(buf.length>0)
		{
			final byte[] outBuf = encodeResponse(buf,type.ordinal());
			clientSocket.getOutputStream().write(outBuf);
			clientSocket.getOutputStream().flush();
		}
	}

	protected String getLocalChannel(final String remoteChannel)
	{
		for(final CMChannel chan : channels)
			if((chan.grapevineName()!=null)&&(chan.grapevineName().equalsIgnoreCase(remoteChannel)))
				return chan.name();
		return null;
	}

	protected void relayChannelMessage(final String from, final String fromMud, String channelName, String message)
	{
		channelName = getLocalChannel(channelName);
		if(channelName == null)
			return;
		CMMsg msg=null;
		final String channelColor="^Q";
		final ChannelsLibrary channels = CMLib.channels();
		final int channelInt=channels.getChannelIndex(channelName);
		int channelCode=channelInt;
		if(channelInt < 0)
		{
			channelCode=47;
		}
		message=CMStrings.removeColors(CMProps.applyINIFilter(message,CMProps.Str.CHANNELFILTER));
		final MOB mob=CMClass.getFactoryMOB();
		mob.setName(from+"@"+fromMud);
		mob.setLocation(getUniversalRoom());
		final String str=channelColor+"^<CHANNEL \""+channelName+"\"^>"+mob.name()+" "+channelName+"(S) '"+message+"'^</CHANNEL^>^N^.";
		msg=CMClass.getMsg(mob,null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+channelCode),str);
		CMLib.commands().monitorGlobalMessage(mob.location(), msg);
		try
		{
			if(channelInt>=0)
				channels.channelQueUp(channelInt, msg, 0);
		}
		catch(final Exception e)
		{
			Log.errOut(e);
		}
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			final MOB M=S.mob();
			final ChannelsLibrary myChanLib=CMLib.get(S)._channels();
			final int chanNum = (myChanLib == channels) ? channelInt : myChanLib.getChannelIndex(channelName);
			if((chanNum >= 0)
			&&(myChanLib.mayReadThisChannel(mob,false,S,chanNum))
			&&(M.location()!=null))
			{
				msg.setOthersCode(CMMsg.MASK_CHANNEL|(CMMsg.TYP_CHANNEL+chanNum));
				if(M.location().okMessage(M,msg))
					M.executeMsg(M,msg);
			}
		}
		mob.destroy();
	}

	protected byte[] encodeResponse(final byte[] resp, final int type)
	{
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		bout.write(0x80 + (byte)type);
		final byte[] mask = new byte[4];
		new SecureRandom().nextBytes(mask);
		if(resp.length < 126)
			bout.write((resp.length&0x7f)|0x80);
		else
		if(resp.length <= 65535)
		{
			bout.write(126 | 0x80);
			bout.write((resp.length>>8)&0xff);
			bout.write(resp.length & 0xff);
		}
		else
		{
			bout.write(127 | 0x80);
			final long len = resp.length;
			for (int i=7;i>=0;i--)
				bout.write((byte)(len>>(i*8))&0xff);
		}
		for(int i=0;i<mask.length;i++)
			bout.write(mask[i]);
		for (int i=0;i<resp.length;i++)
			bout.write(resp[i]^mask[i%4]);
		return bout.toByteArray();
	}


	protected Pair<byte[], WSPType> processBinaryInput(final byte[] input) throws IOException
	{
		// you better never ever get here.
		Log.errOut("Well, I got here. :(");
		return null;
	}

	private void processInput(final int opCode, final byte[] payload) throws HTTPException
	{
		switch(opCode)
		{
		case 0: // continue frame
		case 1: // text frame
		case 2: // binary frame
		{
			this.lastPing=System.currentTimeMillis();
			reset();
			try
			{
				if(payload.length>0)
				{
					Pair<byte[],WSPType> response;
					if (opCode == 2)
						response=processBinaryInput(payload);
					else
						response=processTextInput(new String(payload, StandardCharsets.UTF_8));
					if(response != null)
						sendPacket(response.first, response.second);
				}
			}
			catch (final IOException e)
			{
				closeSocket();
				break;
			}
			break;
		}
		case 8: //connection close?!
		{
			closeSocket();
			break;
		}
		case 9: // ping
			pong();
		//$FALL-THROUGH$
		case 10: // pong -- ignore
		{
			this.lastPing=System.currentTimeMillis();
			break;
		}
		case 11: // close
			break;
		default:
			break;
		}
	}

	public boolean isMappedChannel(final String channelName)
	{
		for(final CMChannel chan : this.channels)
			if(chan.name().equalsIgnoreCase(channelName))
				return true;
		return false;
	}

	public void processBuffer(final byte[] buffer) throws HTTPException
	{
		for(int i=0;i<buffer.length;i++)
		{
			final byte b=buffer[i]; // keep this here .. it ensures the while loop ends
			msg.write(b);
			switch(state)
			{
			case P1:
			{
				this.dataLen=(b&0x7F);
				if ((b&0x80)!=0)
					throw HTTPException.standardException(HTTPStatus.S403_FORBIDDEN);
				if (this.dataLen<126)
				{
					if(dataLen>0)
						state=WSState.PAYLOAD;
					else
					{
						if(finished)
							processInput(opCode, payload.toByteArray());
						state=WSState.S0;
					}
				}
				else
				if(this.dataLen==126)
				{
					this.dataLen = 0;
					this.subState = 2;
					state = WSState.PX;
				}
				else
				{
					this.dataLen = 0;
					this.subState = 8;
					state = WSState.PX;
				}
				break;
			}
			case PAYLOAD:
				payload.write(b);
				if(--dataLen <= 0)
				{
					if(finished)
						processInput(opCode,payload.toByteArray());
					state=WSState.S0;
				}
				break;
			case PX:
				if(subState > 0)
				{
					subState--;
					dataLen = (dataLen << 8) + (b & 0xff);
					if(subState == 0)
					{
						if(dataLen>0)
							state = WSState.PAYLOAD;
						else
						{
							if(finished)
								processInput(opCode, payload.toByteArray());
							state = WSState.S0;
						}
					}
				}
				else
					state = WSState.M1;
				break;
			case S0:
			{
				opCode = (byte)(b & 0x0f);
				finished = (b & 0x80) == 0x80;
				state = WSState.P1;
				break;
			}
			default:
				break;
			}
		}
	}

	public void subscribeChannel(final String channel)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "channels/subscribe");
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("channel", channel);
			obj.put("payload", dobj);
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	public void unsubscribeChannel(final String channel)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "channels/unsubscribe");
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("channel", channel);
			obj.put("payload", dobj);
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	public void playerLoggedIn(final String name)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "players/sign-in");
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("name", name);
			obj.put("payload", dobj);
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	public void playerLoggedOut(final String name)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "players/sign-out");
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("name", name);
			obj.put("payload", dobj);
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	public void playerStatusReq(final MOB mob, final String mudName)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "players/status");
			obj.put("ref", UUID.randomUUID().toString());
			if(mob != null)
				this.playerReq.put((String)obj.get("ref"), mob);
			if(mudName != null)
			{
				final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
				dobj.put("game", mudName);
				obj.put("payload", dobj);
			}
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	public void gameStatusReq(final MOB mob, final String game)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "games/status");
			obj.put("ref", UUID.randomUUID().toString());
			if(mob != null)
				this.playerReq.put((String)obj.get("ref"), mob);
			if((game != null)&&(game.length()>0))
			{
				final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
				dobj.put("game", game);
				obj.put("payload", dobj);
			}
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	private void achievementsSync()
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "achievements/sync");
			obj.put("ref", UUID.randomUUID().toString());
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	private void achievementsCreate(final Achievement A)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "achievements/create");
			obj.put("ref", UUID.randomUUID().toString());
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("title", A.getDisplayStr());
			dobj.put("description", "");
			dobj.put("points", Integer.valueOf(0));
			dobj.put("display", Boolean.TRUE);
			int targetScore = A.getTargetCount();
			if(A.getEvent()==Event.STATVALUE)
				targetScore=A.isTargetFloor()?targetScore+1:targetScore-1;
			if(targetScore == Integer.MIN_VALUE)
			{
				dobj.put("partial_progress",Boolean.FALSE);
				dobj.put("total_progress",MiniJSON.NULL);
			}
			else
			{
				dobj.put("partial_progress",Boolean.TRUE);
				dobj.put("total_progress",Integer.valueOf(targetScore));
			}
			obj.put("payload", dobj);
			outgoing.add(obj.toString().getBytes(StandardCharsets.UTF_8));
		}
		catch(final Exception e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	private void achievementsUpdate(final String key, final Achievement A)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "achievements/update");
			obj.put("ref", UUID.randomUUID().toString());
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("key", key);
			dobj.put("title", A.getDisplayStr());
			dobj.put("description", "");
			dobj.put("points", Integer.valueOf(0));
			dobj.put("display", Boolean.TRUE);
			int targetScore = A.getTargetCount();
			if(A.getEvent()==Event.STATVALUE)
				targetScore=A.isTargetFloor()?targetScore+1:targetScore-1;
			if(targetScore == Integer.MIN_VALUE)
			{
				dobj.put("partial_progress",Boolean.FALSE);
				dobj.put("total_progress",MiniJSON.NULL);
			}
			else
			{
				dobj.put("partial_progress",Boolean.TRUE);
				dobj.put("total_progress",Integer.valueOf(targetScore));
			}
			obj.put("payload", dobj);
			outgoing.add(obj.toString().getBytes(StandardCharsets.UTF_8));
		}
		catch(final Exception e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	private void achievementsDelete(final String key)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "achievements/delete");
			obj.put("ref", UUID.randomUUID().toString());
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("key", key);
			obj.put("payload", dobj);
			outgoing.add(obj.toString().getBytes(StandardCharsets.UTF_8));
		}
		catch(final Exception e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	public List<String> getLocalIMudChannelsList()
	{
		final List<String> list = new Vector<String>();
		for(final CMChannel chan : channels)
			list.add(chan.name());
		return list;
	}

	public void sendMappedChannelMessage(final String player, final String channel, final String message)
	{
		for(final CMChannel chan : channels)
		{
			if((chan.name().equalsIgnoreCase(channel))
			&&(chan.grapevineName()!=null)
			&&(chan.grapevineName().length()>0))
			{
				sendChannel(player,chan.grapevineName(),message);
				return;
			}
		}
	}

	private void sendChannel(final String player, final String channel, final String message)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "channels/send");
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("channel", channel);
			dobj.put("name", player);
			dobj.put("message", message);
			obj.put("payload", dobj);
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	public MiniJSON.JSONObject getMud(final String mudName)
	{
		return knownMuds.get(mudName.toLowerCase().trim());
	}

	public void sendTell(final String from, final String to, final String game, final String message)
	{
		try
		{
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("event", "tells/send");
			obj.put("ref", UUID.randomUUID().toString());
			final MiniJSON.JSONObject dobj = new MiniJSON.JSONObject();
			dobj.put("from_name", from);
			dobj.put("to_name", to);
			dobj.put("to_game", game);
			dobj.put("message", message);
			obj.put("payload", dobj);
			sendPacket(obj.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		}
		catch(final IOException e)
		{
			Log.errOut(e);
			this.closeSocket();
		}
	}

	public boolean isAKnownMud(final String mudName)
	{
		return this.knownMuds.containsKey(mudName.toLowerCase().trim());
	}

	public String getKnownMud(final String mudName)
	{
		if(this.knownMuds.containsKey(mudName.toLowerCase().trim()))
			return (String)this.knownMuds.get(mudName.toLowerCase().trim()).get("game");
		return null;
	}

	public boolean reconnect() throws IOException, InterruptedException
	{
		serverKey = null;
		if(lastDelay < 60000L)
			lastDelay *= 2;
		Thread.sleep(lastDelay);
		clientSocket = SSLSocketFactory.getDefault().createSocket("grapevine.haus", 443);
		if(!clientSocket.isConnected())
			return false;
		final StringBuilder req = new StringBuilder("");
		req.append("GET /socket HTTP/1.1\r\n");
		req.append("Host: grapevine.haus\r\n");
		req.append("Upgrade: websocket\r\n");
		req.append("Origin: http://coffeemud.net\r\n");
		req.append("Connection: Upgrade\r\n");
		req.append("Sec-WebSocket-Key: "+webSockKey+"\r\n");
		req.append("Sec-WebSocket-Version: 13\r\n");
		req.append("\r\n");
		clientSocket.getOutputStream().write(req.toString().getBytes());
		clientSocket.getOutputStream().flush();
		clientSocket.setSoTimeout(10000);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String line = reader.readLine();
		boolean goodResp = false;
		while (line != null && !line.isEmpty())
		{
			line = line.toLowerCase().trim();
			if(line.startsWith("http/1.1 101 "))
				goodResp = true;
			else
			if(line.startsWith("sec-websocket-accept:") && (goodResp))
				serverKey = line.substring(21).trim();
			line = reader.readLine();
		}
		if((!goodResp)||(serverKey == null))
			throw new IOException("Grapevine server gave bad response.");
		final StringBuilder authStr = new StringBuilder("");
		authStr.append("{");
		authStr.append("\"event\": \"authenticate\",");
		authStr.append("\"payload\": {");
		authStr.append("\"client_id\": \""+auth[0]+"\",");
		authStr.append("\"client_secret\": \""+auth[1]+"\",");
		authStr.append("\"supports\": [\"channels\", \"players\", \"tells\", \"games\", \"achievements\"],");
		authStr.append("\"channels\": [");
		for(int i=0;i<channels.size();i++)
		{
			authStr.append("\"").append(channels.get(i).grapevineName()).append("\"");
			if(i<channels.size()-1)
				authStr.append(",");
		}
		authStr.append("],");
		authStr.append("\"version\": \"2.3.0\",");
		authStr.append("\"user_agent\": \"CoffeeMud "+CMProps.getVar(CMProps.Str.MUDVER)+"\"");
		authStr.append("}");
		authStr.append("}");
		sendPacket(authStr.toString().getBytes(StandardCharsets.UTF_8),WSPType.TEXT);
		lastDelay = 500L;
		return true;
	}

	private synchronized void reset()
	{
		msg.reset();
		payload.reset();
		state 		= WSState.S0;
		subState	= 0;
		dataLen		= 0;
		opCode		= 0;
		finished	= false;
	}

	protected void closeSocket()
	{
		try
		{
			if(clientSocket != null)
				clientSocket.close();
		}
		catch(final IOException e)
		{}
		achieves.clear();
		playerReq.clear();
		knownMuds.clear();
	}

	@Override
	public void close() throws IOException
	{
		closeSocket();
		shutdown = true;
	}
}
