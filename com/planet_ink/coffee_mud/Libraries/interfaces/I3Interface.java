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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2003-2023 Bo Zimmerman

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
public interface I3Interface extends CMLibrary
{
	/**
	 * Requests a list of characters online on the given i3 mud.
	 *
	 * @param mob the player requesting info
	 * @param mudName the mud info is requested about
	 */
	public void i3who(MOB mob, String mudName);

	/**
	 * Sends a tell private message from the given player to a character
	 * on an I3 mud.
	 *
	 * @param mob the player sending the message
	 * @param tellName the char to send the message to
	 * @param mudName the mud the char is on
	 * @param message the message being sent to the char
	 */
	public void i3tell(MOB mob, String tellName, String mudName, String message);

	/**
	 * Sends an I3 channel message
	 *
	 * @param mob the player sending the message
	 * @param channelName the channel to send the message on
	 * @param message the message to send
	 */
	public void i3channel(MOB mob, String channelName, String message);

	/**
	 * Sends a locate packet to the i3 router requesting which mud
	 * a character with the given name exists on, and also returning
	 * this fact and the char status to the given player
	 *
	 * @param mob the player requesting info
	 * @param mobName the char info is requested about
	 */
	public void i3locate(MOB mob, String mobName);

	/**
	 * Sends a finger packet to the target mud requesting
	 * info about the given user on that i3 mud.  That info
	 * is shown to the given player
	 *
	 * @param mob the player who did it
	 * @param mobName the char to request info about
	 * @param mudName the mud that the char is at
	 */
	public void i3finger(MOB mob, String mobName, String mudName);

	/**
	 * Sends a ping packet to the i3 router, which is only
	 * partially supported.  Effectively the router
	 * just sends your packet right back, because the
	 * target mud is THIS one.
	 *
	 * @param mob the player who did it
	 */
	public void i3pingRouter(MOB mob);

	/**
	 * Shows the list of active I3 muds to the
	 * given mobs.
	 *
	 * @param mob the player to show the list to
	 */
	public void giveI3MudList(MOB mob);

	/**
	 * Returns the simple mud name list of all active I3
	 * muds.
	 *
	 * @param coffeemudOnly true to only send back coffeemuds, false for all
	 * @return the list of mud names
	 */
	public List<String> getI3MudList(boolean coffeemudOnly);

	/**
	 * Shows the list of active IMC2 muds to the
	 * given mobs.
	 *
	 * @param mob the player to show the list to
	 */
	public void giveIMC2MudList(MOB mob);

	/**
	 * Registers the given driver server as the active
	 * imc2 server for the entire mud.
	 *
	 * @param O the IMC2Driver server
	 */
	public void registerIMC2(Object O);

	/**
	 * Returns whether the given string represents
	 * a real mud-mapped im3 channel name
	 *
	 * @param channelName the channel name to check
	 * @return true if its real, false otherwise
	 */
	public boolean isI3channel(String channelName);

	/**
	 * Returns whether the given string represents
	 * a real mud-mapped imc2 channel name
	 *
	 * @param channelName the channel name to check
	 * @return true if its real, false otherwise
	 */
	public boolean isIMC2channel(String channelName);

	/**
	 * Returns whether the mud is presently
	 * connected to the I3 server.
	 *
	 * @return whether i3 is online
	 */
	public boolean i3online();

	/**
	 * Returns whether the mud is presently
	 * connected to the IMC2 server.
	 *
	 * @return whether imc2 is online
	 */
	public boolean imc2online();

	/**
	 * Queries another mud on the I3 network for a list of which
	 * players on that mud are listening on a particular channel.
	 *
	 * @param mob the player who is curious
	 * @param channel the channel in question
	 * @param mudName the mud to query
	 */
	public void i3chanwho(MOB mob, String channel, String mudName);

	/**
	 * Show the list of available I3 channels to
	 * the given player.
	 *
	 * @param mob the player who wants info
	 */
	public void giveI3ChannelsList(MOB mob);

	/**
	 * Show the list of available IMC2 channels to
	 * the given player.
	 *
	 * @param mob the player who wants info
	 */
	public void giveIMC2ChannelsList(MOB mob);

	/**
	 * An administrative command to subscribe to a particular
	 * channel on the I3 network.
	 *
	 * @param mob the player doing the subscribeing
	 * @param channel the possible channel name to subscribe
	 */
	public void i3channelListen(MOB mob, String channel);

	/**
	 * An administrative command to un-subscribe a particular
	 * channel on the I3 network.
	 *
	 * @param mob the player doing the un-subscribeing
	 * @param channel the possible channel name to un-subscribe
	 */
	public void i3channelSilence(MOB mob, String channel);

	/**
	 * An administrative command to register a particular
	 * channel on the I3 network.
	 *
	 * @param mob the player doing the registering
	 * @param channel the possible channel name to register
	 */
	public void i3channelAdd(MOB mob, String channel);

	/**
	 * An administrative command to de-register a particular
	 * channel on the I3 network.
	 *
	 * @param mob the player doing the de-registering
	 * @param channel the possible channel name to de-register
	 */
	public void i3channelRemove(MOB mob, String channel);

	/**
	 * Send a request for information about a mud
	 * on the I3 network on behalf of the given
	 * player.
	 *
	 * @param mob the player who wants the info
	 * @param parms the possible mud name to get info on
	 */
	public void i3mudInfo(MOB mob, String parms);

	/**
	 * Send a request for information about a mud
	 * on the IMC2 network on behalf of the given
	 * player.
	 *
	 * @param mob the player who wants the info
	 * @param parms the possible mud name to get info on
	 */
	public void imc2mudInfo(MOB mob, String parms);
}
