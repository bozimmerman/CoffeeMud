package com.planet_ink.coffee_mud.Libraries.interfaces;
import java.util.List;

import com.planet_ink.coffee_mud.core.interfaces.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.IntermudInterface.InterProto;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
 * Interface library between the mud and the InterMud 3 and
 * Intermud 2 networks.  This includes basic I3 and IMC2
 * functions and management.
 *
 * @author Bo Zimmerman
 *
 */
public interface IntermudInterface extends CMLibrary
{

	/**
	 * The abstract intermud protocol list
	 */
	public static enum InterProto
	{
		CM1,
		IMC2,
		I3,
		Grapevine,
		DISCORD
	}

	/**
	 * The abstract intermud protocol list
	 */
	public static enum InterQuery
	{
		NAME,
		PORT,
		RUNNING
	}

	/**
	 * An inter-mud remote mud definition, for export to the world.
	 */
	public static class RemoteIMud
	{
		public InterProto proto = null;
		public String name = "";
		public String mudLib = "";
		public String hostPort = "";
		public int numOnline = -1;
	}

	/**
	 * Starts the given intermud system.  If it is already
	 * running, it will stop it and restart it if requested.
	 *
	 * @see IntermudInterface#stopIntermud(InterProto)
	 *
	 * @param proto which intermud system to restart
	 * @param restart true to stop first, false to leave it alone
	 * @return true if all went well
	 */
	public boolean startIntermud(final InterProto proto, final boolean restart);

	/**
	 * Stops the given intermud system.
	 *
	 * @see IntermudInterface#startIntermud(InterProto, boolean)
	 *
	 * @param proto which intermud system to restart
	 */
	public void stopIntermud(final InterProto proto);

	/**
	 * Query one of the intermud services for some InterQuery data.
	 * Returns the appropriate value as a string.
	 *
	 * @param proto the service to query
	 * @param query the query type to make
	 * @return the response, as a string
	 */
	public String queryService(final InterProto proto, final InterQuery query);
	/**
	 * Requests a list of characters online on the given inter mud.
	 *
	 * @param mob the player requesting info
	 * @param mudName the mud info is requested about
	 */
	public void imudWho(MOB mob, String mudName);

	/**
	 * Returns whether any of the services, excepting CM1,
	 * are online.
	 *
	 * @return true if at least one of them is.
	 */
	public boolean isAnyNonCM1Online();

	/**
	 * Sends a tell private message from the given player to a character
	 * on an inter mud.
	 *
	 * @param mob the player sending the message
	 * @param tellName the char to send the message to
	 * @param mudName the mud the char is on
	 * @param message the message being sent to the char
	 */
	public void imudTell(MOB mob, String tellName, String mudName, String message);

	/**
	 * Sends an intermud channel message
	 *
	 * @param mob the player sending the message
	 * @param channelName the channel to send the message on
	 * @param message the message to send
	 */
	public void imudChannel(MOB mob, String channelName, String message);

	/**
	 * Sends a locate packet to an intermud router requesting which mud
	 * a character with the given name exists on, and also returning
	 * this fact and the char status to the given player
	 *
	 * @param mob the player requesting info
	 * @param mobName the char info is requested about
	 */
	public void imudLocate(MOB mob, String mobName);

	/**
	 * Sends a finger packet to the target mud requesting
	 * info about the given user on that i3 mud.  That info
	 * is shown to the given player
	 *
	 * @param mob the player who did it
	 * @param mobName the char to request info about
	 * @param mudName the mud that the char is at
	 */
	public void imudFinger(MOB mob, String mobName, String mudName);

	/**
	 * Sends a ping packet to the i3 router, which is only
	 * partially supported.  Effectively the router
	 * just sends your packet right back, because the
	 * target mud is THIS one.
	 *
	 * @param proto which service to ping
	 * @param mob the player who did it
	 */
	public void pingRouter(InterProto proto, MOB mob);

	/**
	 * Returns the simple mud name list of all active intermud
	 * muds.
	 *
	 * @param coffeemudOnly true to only send back coffeemuds, false for all
	 * @return the list of mud names
	 */
	public List<String> getAllMudList(boolean coffeemudOnly);

	/**
	 * Returns the simple mud name list of all active intermud
	 * muds of a particular protocol.
	 *
	 * @param proto the protocol to query
	 * @param coffeemudOnly true to only send back coffeemuds, false for all
	 * @return the list of mud names
	 */
	public List<String> getMudList(InterProto proto, boolean coffeemudOnly);

	/**
	 * Returns basic info about all active intermud muds.
	 *
	 * @param coffeemudOnly true to only send back coffeemuds, false for all
	 * @return the list of muds as a RemoteIMud object
	 */
	public List<RemoteIMud> getAllMudInfo(boolean coffeemudOnly);

	/**
	 * Returns basic info about particular services active intermud muds.
	 *
	 * @param proto the protocol to query
	 * @param coffeemudOnly true to only send back coffeemuds, false for all
	 * @return the list of muds as a RemoteIMud object
	 */
	public List<RemoteIMud> getMudInfo(InterProto proto, boolean coffeemudOnly);

	/**
	 * Returns whether the given string represents
	 * a real mud-mapped intermud channel name
	 *
	 * @param proto the service to query
	 * @param channelName the channel name to check
	 * @return true if its real, false otherwise
	 */
	public boolean isImudChannel(InterProto proto, String channelName);

	/**
	 * Returns whether the given string represents
	 * a real mud-mapped intermud channel name
	 *
	 * @param channelName the channel name to check
	 * @return true if its real, false otherwise
	 */
	public boolean isAnyImudChannel(String channelName);

	/**
	 * Returns whether the mud is presently
	 * connected to the intermud server.
	 *
	 * @param proto the protocol to query
	 * @return whether i3 is online
	 */
	public boolean isOnline(InterProto proto);

	/**
	 * Queries another mud on the I3 network for a list of which
	 * players on that mud are listening on a particular channel.
	 *
	 * @param mob the player who is curious
	 * @param channel the channel in question
	 * @param mudName the mud to query
	 */
	public void chanWho(MOB mob, String channel, String mudName);

	/**
	 * Show the list of available intermud channels to
	 * the given player.
	 *
	 * @param mob the player who wants info
	 */
	public void getChannelsList(MOB mob);

	/**
	 * Show the list of available intermud channels to
	 * the given player for a particular service.
	 *
	 * @param proto the protocol to query
	 * @param mob the player who wants info
	 */
	public void getChannelsList(MOB mob, InterProto proto);

	/**
	 * An administrative command to subscribe to a particular
	 * channel on the I3 network.
	 *
	 * @param proto the protocol to listen to
	 * @param mob the player doing the subscribeing
	 * @param channel the possible channel name to subscribe
	 */
	public void channelListen(InterProto proto, MOB mob, String channel);

	/**
	 * An administrative command to un-subscribe a particular
	 * channel on an intermud network.
	 *
	 * @param proto the protocol to listen to
	 * @param mob the player doing the un-subscribeing
	 * @param channel the possible channel name to un-subscribe
	 */
	public void channelSilence(InterProto proto, MOB mob, String channel);

	/**
	 * An administrative command to register a particular
	 * channel on an intermud network.
	 *
	 * @param proto the protocol to add to
	 * @param mob the player doing the registering
	 * @param channel the possible channel name to register
	 */
	public void channelAdd(InterProto proto, MOB mob, String channel);

	/**
	 * An administrative command to de-register a particular
	 * channel on an intermud network.
	 *
	 * @param proto the protocol to remove from
	 * @param mob the player doing the de-registering
	 * @param channel the possible channel name to de-register
	 */
	public void channelRemove(InterProto proto, MOB mob, String channel);

	/**
	 * Send a request for information about a mud
	 * on an intermud network on behalf of the given
	 * player.
	 *
	 * @param mob the player who wants the info
	 * @param parms the possible mud name to get info on
	 */
	public void mudInfo(MOB mob, String parms);

	/**
	 * Send a request for information about a mud
	 * on an intermud network on behalf of the given
	 * player for the given service.
	 *
	 * @param proto the protocol to limit to
	 * @param mob the player who wants the info
	 * @param parms the possible mud name to get info on
	 */
	public void mudInfo(final InterProto proto, final MOB mob, final String parms);

	/**
	 * Registers a new char as having come online.
	 * @param mob the char online
	 */
	public void registerPlayerOnline(final MOB mob);

	/**
	 * Registers a new char as having gone offline.
	 * @param mob the char offline
	 */
	public void registerPlayerOffline(final MOB mob);
}
