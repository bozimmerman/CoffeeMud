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
import com.planet_ink.coffee_mud.core.CMProps.Str;
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
import com.planet_ink.coffee_mud.Libraries.CMChannels;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.CMChannel;
import com.planet_ink.coffee_mud.Libraries.interfaces.ChannelsLibrary.ChannelFlag;
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
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
public class DiscordClient implements Closeable
{
	protected ClassLoader					discordClassLoader	= null;
	protected Object						discordApi			= null;
	protected DoubleMap<CMChannel, Object>	discordChannels		= new DoubleMap<CMChannel, Object>(SHashtable.class);
	protected Map<String, CMChannel>		discordChanNames	= new Hashtable<String, CMChannel>();
	protected LimitedTreeSet<String>		lastDiscordMsgs		= new LimitedTreeSet<String>(10000,100,false,true);
	protected final Map<String,MOB> 		discordTalkers 		= Collections.synchronizedMap(new TreeMap<String,MOB>());
	protected List<CMChannel>				channels;

	protected final Map<Object, List<ChannelsLibrary>> discordLibMap = new Hashtable<Object,List<ChannelsLibrary>>();

	public DiscordClient(final List<CMChannel> channels)
	{
		this.channels = channels;
	}

	protected void initDiscord()
	{
		if(discordApi != null)
			return;
		final String jarPath = CMProps.getVar(Str.DISCORD_JAR_PATH);
		if(jarPath.length()==0)
		{
			Log.errOut("DISCORD_JAR_PATH not set in INI file.");
			return;
		}
		final CMFile F = new CMFile(jarPath, null);
		if(!F.exists())
		{
			Log.errOut("DISCORD jar file not found in "+jarPath);
			return;
		}
		URL jarUrl;
		try
		{
			jarUrl = new URL("vfs:" + jarPath);
		}
		catch (final MalformedURLException e)
		{
			Log.errOut(e);
			return;
		}
		discordClassLoader=new URLClassLoader(new URL[]{jarUrl});
		final PrintStream originalOut = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(final int b)
			{
			}
		}));
		try
		{
			final Class<?> apiBuilderClass = discordClassLoader.loadClass("org.javacord.api.DiscordApiBuilder");
			final Class<?> apiClass = discordClassLoader.loadClass("org.javacord.api.DiscordApi");
			final Class<?> intentClass = discordClassLoader.loadClass("org.javacord.api.entity.intent.Intent");
			final Class<?> listenClass = discordClassLoader.loadClass("org.javacord.api.listener.message.MessageCreateListener");
			final Class<?> eventInterface = discordClassLoader.loadClass("org.javacord.api.event.message.MessageCreateEvent");
			final Object apiBuilder = apiBuilderClass.getDeclaredConstructor().newInstance();
			final String secretToken = CMProps.getVar(CMProps.Str.DISCORD_BOT_KEY);
			if(secretToken.length()==0)
				return;
			Object api = apiBuilder;
			final Method setTokenM = apiBuilderClass.getMethod("setToken", String.class);
			api = setTokenM.invoke(api, secretToken);
			final Method addIntentsM = apiBuilderClass.getMethod("addIntents", intentClass.getEnumConstants().getClass());
			final Object array = Array.newInstance(intentClass, 1);
			for(final Object o : intentClass.getEnumConstants())
			{
				@SuppressWarnings("rawtypes")
				final Enum e = (Enum)o;
				if(e.name().equals("MESSAGE_CONTENT"))
					Array.set(array, 0, e);
			}
			api = addIntentsM.invoke(api, new Object[] {array});
			final Method loginM = apiBuilderClass.getMethod("login");
			final CompletableFuture<?> future = (CompletableFuture<?>)loginM.invoke(api);
			discordApi = future.join();
			final Method createBotInviteM = apiClass.getMethod("createBotInvite");
			final String url=CMStrings.replaceAll(createBotInviteM.invoke(discordApi).toString(),"permissions=0","permissions=2048");
			Log.infoOut("Discord Bot auth url: "+url);
			final Method listenM = apiClass.getMethod("addMessageCreateListener", listenClass);
			final Class<?>[] classArray = new Class<?>[] { listenClass };
			final Object listener = Proxy.newProxyInstance(discordClassLoader, classArray, new DiscordMsgListener(eventInterface));
			listenM.invoke(discordApi, listener);
		}
		catch (final Exception e)
		{
			Log.errOut(e);
		}
		finally
		{
			System.setOut(originalOut);
		}
	}

	public void stopClient()
	{
		if(discordApi != null)
		{
			try
			{
				final Method disconnM = discordApi.getClass().getMethod("disconnect");
				disconnM.invoke(discordApi);
			}
			catch(final Exception e)
			{
				Log.errOut(e);
			}
			finally
			{
				discordApi = null;
			}
		}
	}

	public boolean isActive()
	{
		return (discordApi != null);
	}

	public TriadList<String,String,String> getRemoteChannelsList()
	{
		// can't really know what other channels exist
		final TriadList<String,String,String> list = new TriadVector<String,String,String>();
		for(final String name : getTextChannels().keySet())
			if(!list.containsFirst(name))
				list.add(name,"Discord","");
		return list;
	}

	public List<String> getLocalIMudChannelsList()
	{
		final List<String> list = new Vector<String>();
		for(final CMChannel chan : channels)
			list.add(chan.name());
		return list;
	}

	/**
	 * Requires including special library and special configuration.
	 *
	 * @param chanName the channel to send to
	 * @param msg the message to send
	 */
	public void sendDiscordMsg(final String chanName, final String msg)
	{
		if(discordApi==null)
			return;
		final CMChannel chan = this.discordChanNames.get(chanName.toLowerCase().trim());
		if(chan == null)
			return;
		final Object chanObj = this.discordChannels.get(chan);
		if(chanObj != null)
		{
			try
			{
				final Class<?> schannelClass = discordClassLoader.loadClass("org.javacord.core.entity.channel.ServerTextChannelImpl");
				final Method sendM = schannelClass.getMethod("sendMessage",String.class);
				lastDiscordMsgs.add(msg);
				sendM.invoke(chanObj, msg);
			}
			catch(final Exception e)
			{
				Log.errOut(e);
			}
		}
	}

	protected class DiscordMsgListener implements InvocationHandler
	{
		private final Class<?> eventClass;
		public DiscordMsgListener(final Class<?> eventClass)
		{
			this.eventClass = eventClass;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
		{
			final Object result = null;
			try
			{
				if(method.getName().equals("hashCode"))
					return Integer.valueOf(this.hashCode());
				else
				if(method.getName().equals("equals") && (args!=null) && (args.length>0))
					return Boolean.valueOf(this.equals(args[0]));
				else
				if(method.getName().equals("toString"))
					return "";
				else
				if((method.getName().equals("onMessageCreate"))
				&&(args != null)
				&&(args.length>0)
				&&(CMProps.isState(CMProps.HostState.RUNNING)))
				{
					final Method channelM = eventClass.getMethod("getChannel");
					final Object channelObj = channelM.invoke(args[0]);
					if(discordLibMap.containsKey(channelObj))
					{
						for(final ChannelsLibrary lib : discordLibMap.get(channelObj))
						{
							final CMChannel chan = discordChannels.getValue(channelObj);
							if(chan == null)
								continue;
							if(chan.flags().contains(ChannelsLibrary.ChannelFlag.READONLY)
							|| chan.flags().contains(ChannelsLibrary.ChannelFlag.PLAYERREADONLY)
							|| chan.flags().contains(ChannelsLibrary.ChannelFlag.ARCHONREADONLY))
								continue;
							final Class<?> authClass = discordClassLoader.loadClass("org.javacord.api.entity.message.MessageAuthor");
							final Method contentM = eventClass.getMethod("getMessageContent");
							final String content = (String)contentM.invoke(args[0]);
							if(!lastDiscordMsgs.contains(content))
							{
								final Method authorM = eventClass.getMethod("getMessageAuthor");
								final Object authorO = authorM.invoke(args[0]);
								final Method nameM = authClass.getMethod("getDisplayName");
								final String name = (String)nameM.invoke(authorO);
								final MOB M;
								// preserve because backlogs
								if(discordTalkers.containsKey(name))
									M = discordTalkers.get(name);
								else
								if(CMLib.players().getPlayer(name)!=null)
								{
									M=CMLib.players().getPlayer(name);
									discordTalkers.put(name,M);
								}
								else
								{
									M=CMClass.getMOB("StdMOB");
									M.setName(name);
									M.setLocation(CMLib.map().getRandomRoom());
									discordTalkers.put(name,M);
								}
								final StringBuilder str = new StringBuilder("");
								for(final char c : content.toCharArray())
								{
									if((c>31)&&(c<128))
										str.append(c);
									else
									switch(c)
									{
									case '\n':case '\r':case '\t':
										str.append(c);
										break;
									}
								}
								lib.createAndSendChannelMessage(M, chan.name(), str.toString(), false,true);
							}
						}
					}
				}
			}
			catch (final Exception e)
			{
				Log.errOut(e);
			}
			return result;
		}
	}

	public boolean removeChannel(final String discordChanName)
	{
		for(final CMChannel chan : channels)
		{
			if((chan.discordName()!=null)
			&&(chan.discordName().equalsIgnoreCase(discordChanName)))
			{
				discordChanNames.remove(chan.name().toLowerCase());
				discordChannels.remove(chan);
				synchronized(discordLibMap)
				{
					final Object chanObj = discordLibMap.get(chan);
					discordLibMap.remove(chanObj);
					return true;
				}
			}
		}
		return false;
	}

	public boolean addChannel(final String discordChanName)
	{
		for(final CMChannel chan : channels)
		{
			if((chan.discordName()!=null)
			&&(chan.discordName().equalsIgnoreCase(discordChanName)))
				return addChannel(discordChanName, chan);
		}
		return false;
	}

	private boolean addChannel(final String discordName, final CMChannel chan)
	{
		final Object chanObj = getDiscordChannelObj(chan.discordName());
		if(chanObj != null)
		{
			discordChanNames.put(chan.name().toLowerCase(), chan);
			discordChannels.put(chan, chanObj);
			synchronized(discordLibMap)
			{
				if(!discordLibMap.containsKey(chanObj))
					discordLibMap.put(chanObj, new Vector<ChannelsLibrary>());
				discordLibMap.get(chanObj).add(CMLib.channels());
			}
			return true;
		}
		else
			Log.errOut("Unable to map discord channel '"+chan.discordName()+"'");
		return false;
	}

	public boolean startClient()
	{
		for(final CMChannel chan : channels)
		{
			if(chan.flags().contains(ChannelFlag.DISCORD)
			&& (chan.discordName().length() > 0))
			{
				initDiscord();
				if(discordApi == null)
					break;
				addChannel(chan.discordName(), chan);
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	protected Map<String,Object> getTextChannels()
	{
		final Map<String,Object> names = new TreeMap<String,Object>();
		if(discordApi == null)
			return names;
		try
		{
			final Class<?> apiClass = discordClassLoader.loadClass("org.javacord.api.DiscordApi");
			final Class<?> serverClass = discordClassLoader.loadClass("org.javacord.api.entity.server.Server");
			final Class<?> schannelClass = discordClassLoader.loadClass("org.javacord.core.entity.channel.ServerTextChannelImpl");
			final Method getServersM = apiClass.getMethod("getServers");
			for(final Object svrObj : (Iterable)getServersM.invoke(discordApi))
			{
				final Method getChannels = serverClass.getMethod("getTextChannels");
				for(final Object chanObj : (Iterable)getChannels.invoke(svrObj))
				{
					if(schannelClass.isInstance(chanObj))
					{
						final Method getName = schannelClass.getMethod("getName");
						final String channelName = (String)getName.invoke(chanObj);
						names.put(channelName.toUpperCase(), chanObj);
					}
				}
			}
		}
		catch (final Exception e)
		{
			Log.errOut(e);
		}
		return names;
	}

	protected Object getDiscordChannelObj(final String named)
	{
		if(discordApi == null)
			return null;
		try
		{
			final Map<String,Object> names = getTextChannels();
			final String unamed = named.toUpperCase();
			if(names.containsKey(unamed))
				return names.get(unamed);
			for(final String name : names.keySet())
			{
				if(unamed.startsWith(name))
					return names.get(name);
			}
			for(final String name : names.keySet())
			{
				if(unamed.endsWith(name))
					return names.get(name);
			}
			for(final String name : names.keySet())
			{
				if(name.startsWith(unamed))
					return names.get(name);
			}
			for(final String name : names.keySet())
			{
				if(name.endsWith(unamed))
					return names.get(name);
			}
		}
		catch (final Exception e)
		{
			Log.errOut(e);
		}
		return null;
	}

	@Override
	public void close() throws IOException
	{
		this.stopClient();
	}
}
