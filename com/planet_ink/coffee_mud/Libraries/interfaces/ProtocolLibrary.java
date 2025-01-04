package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2013-2025 Bo Zimmerman

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
 * Library for supporting various in-line MUD protocols.
 * These include:
 * MSP - MUD Sound Protocol: generated by the server to send
 * sounds to the client for playing.
 * MXP - MUD eXtension Protocol: generated by the server to send
 * formatting, color, sound, and image information to the client
 * MCP - MUD Client Protocol: protocol for client server
 * interaction not supported by simple telnet.
 * MSDP - MUD Server Data Protocol: system for client to request
 * information from the server not readily visible. (over GMCP)
 * GMCP - Generic MUD Communication Protocol - low level protocol
 *
 * @author Bo Zimmerman
 *
 */
public interface ProtocolLibrary extends CMLibrary
{
	/**
	 * Generates an MSP sound tag
	 *
	 * @param soundName the name of the MSP sound
	 * @param priority a bias number for the sound priority
	 * @return the MSP sound tag
	 */
	public String msp(final String soundName, final int priority);

	/**
	 * Handles an MCP command from the user, modifying the line
	 * of text that contains it.
	 *
	 * @param session the session of the user
	 * @param str the string containing the command
	 * @param mcpKey the MCP key
	 * @param clientSupported map of supported features and their versions
	 * @param keyValuePairs protocol-specific key value pairs
	 * @return true if all went well, false otherwise
	 */
	public boolean mcp(final Session session, final StringBuilder str, final String[] mcpKey, Map<String,float[]> clientSupported, final Map<String,String> keyValuePairs);

	/**
	 * Returns the path and filename to an MXP image, given the
	 * image filename, presumably full path.
	 *
	 * @see ProtocolLibrary#getDefaultMXPImage(CMObject)
	 * @see ProtocolLibrary#mxpImage(Environmental, String, String, String)
	 * @see ProtocolLibrary#mxpImage(Environmental, String)
	 * @see ProtocolLibrary#mxpImagePath(String)
	 *
	 * @param fileName the full image path
	 * @return an array with the path, then the filename
	 */
	public String[] mxpImagePath(String fileName);

	/**
	 * Constructs an official MXP image tag for the given object, with
	 * the given parms.
	 *
	 * @see ProtocolLibrary#getDefaultMXPImage(CMObject)
	 * @see ProtocolLibrary#mxpImage(Environmental, String, String, String)
	 * @see ProtocolLibrary#mxpImage(Environmental, String)
	 * @see ProtocolLibrary#mxpImagePath(String)
	 *
	 * @param E the object to generate an image tag for
	 * @param parms MXP image parms
	 * @return "" or a complete MXP image tag
	 */
	public String mxpImage(final Environmental E, final String parms);

	/**
	 * Constructs an official MXP image tag for the given object, with
	 * the given parms, and given pre-image and post-image data.
	 *
	 * @see ProtocolLibrary#getDefaultMXPImage(CMObject)
	 * @see ProtocolLibrary#mxpImage(Environmental, String, String, String)
	 * @see ProtocolLibrary#mxpImage(Environmental, String)
	 * @see ProtocolLibrary#mxpImagePath(String)
	 *
	 * @param E the object to generate an image tag for
	 * @param parms MXP image parms
	 * @param pre something to put in front of the IMAGE tag itself
	 * @param post something to put after it, usually a space
	 * @return "" or a complete MXP image tag
	 */
	public String mxpImage(final Environmental E, final String parms, final String pre, final String post);

	/**
	 * MXP Images for each object can go from very specific to
	 * general.  This function goes through all the various
	 * image layers available to a given game object, and returns
	 * the most specific one that is defined.
	 *
	 * @see ProtocolLibrary#getDefaultMXPImage(CMObject)
	 * @see ProtocolLibrary#mxpImage(Environmental, String, String, String)
	 * @see ProtocolLibrary#mxpImage(Environmental, String)
	 * @see ProtocolLibrary#mxpImagePath(String)
	 *
	 * @param O the object to get an image for
	 * @return "", or the http image sub-path
	 */
	public String getDefaultMXPImage(final CMObject O);

	/**
	 * Main entry point to handle MSDP commands from the user
	 *
	 * @see ProtocolLibrary#pingMsdp(Session, Map)
	 *
	 * @param session the session of the mob to report to
	 * @param data the users MSDP sending
	 * @param dataSize the size of the sending?
	 * @param reportables the 'subscriptions' of the given session
	 * @return null, or bytes to send to the user
	 */
	public byte[] processMsdp(final Session session, final char[] data, final int dataSize, final Map<Object,Object> reportables);

	/**
	 * Called every second from each player session to deal with periodic MSDP
	 * reports.
	 *
	 * @see ProtocolLibrary#processMsdp(Session, char[], int, Map)
	 *
	 * @param session the session of the mob to report to
	 * @param reportables the 'subscriptions' of the given session
	 * @return null, or bytes to send to the user
	 */
	public byte[] pingMsdp(final Session session, final Map<Object,Object> reportables);

	/**
	 * Main entry point to handle GMCP commands from the user
	 *
	 * @see ProtocolLibrary#pingGmcp(Session, Map, Map)
	 * @see ProtocolLibrary#invokeRoomChangeGmcp(Session, Map, Map)
	 * @see ProtocolLibrary.GMCPCommand
	 *
	 * @param session the session of the mob to report to
	 * @param data the command sent by the user
	 * @param supportables map of supported GMCP features
	 * @param reportables the msdp 'subscriptions' of the given session
	 * @return null, or bytes to send to the user
	 */
	public byte[] processGmcp(final Session session, final String data, final Map<String,Double> supportables, final Map<Object,Object> reportables);

	/**
	 * Called every second from each player session to deal with periodic GMCP
	 * reports.
	 *
	 * @see ProtocolLibrary#processGmcp(Session, String, Map)
	 * @see ProtocolLibrary#invokeRoomChangeGmcp(Session, Map, Map)
	 * @see ProtocolLibrary.GMCPCommand
	 *
	 * @param session the session of the mob to report to
	 * @param reporteds the 'subscriptions' of the given session
	 * @param supportables map of supported GMCP features
	 * @param reportables the msdp 'subscriptions' of the given session
	 * @return null, or bytes to send to the user
	 */
	public byte[] pingGmcp(final Session session, final Map<String,Long> reporteds, final Map<String,Double> supportables, final Map<Object,Object> reportables);

	/**
	 * GMCP appears to support getting a report from the protocol when entering
	 * a new room.  This method is called to handle those reports.
	 *
	 * @see ProtocolLibrary#processGmcp(Session, String, Map)
	 * @see ProtocolLibrary#pingGmcp(Session, Map, Map)
	 * @see ProtocolLibrary.GMCPCommand
	 *
	 * @param session the session of the mob to report to
	 * @param reporteds the 'subscriptions' of the given session
	 * @param supportables map of supported GMCP features
	 * @param reportables the msdp 'subscriptions' of the given session
	 * @return null, or bytes to send to the user
	 */
	public byte[] invokeRoomChangeGmcp(final Session session, final Map<String,Long> reporteds, final Map<String,Double> supportables, final Map<Object,Object> reportables);

	/**
	 * Returns all the MSSP variables for crawlers.
	 * Values can be Strings, or String Arrays
	 * @return all the MSSP variables for crawlers.
	 */
	public Map<String,Object> getMSSPPackage();

	/**
	 * Enumeration of all support GMCP commands
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum GMCPCommand
	{
		core_hello,
		core_supports_set,
		core_supports_add,
		core_supports_remove,
		core_keepalive,
		core_ping,
		core_goodbye,
		char_login,
		char_vitals,
		char_statusvars,
		char_status,
		char_base,
		char_maxstats,
		char_worth,
		char_items_inv, // means they want updates, dude
		char_items_contents,
		char_skills_get,
		char_effects_get,
		group,
		room_info, // means they want room.wrongdir and room.enter and room.leave
		room_items_inv,
		room_items_contents,
		room_mobiles,
		room_players,
		comm_channel,
		comm_channel_players,
		ire_composer_setbuffer,
		request_room,
		request_area,
		request_char,
		request_sectors,
		request_group,
		request_quest,
		rawcolor,
		request,
		maplevel,
		client,
		client_version,
		external_discord_hello,
		msdp
	}

	/**
	 * Interface allowing java or javascript plugins to the MCP protocol
	 * @author Bo Zimmerman
	 *
	 */
	public interface MCPPackage
	{
		/**
		 * Return the name of the overall package
		 * @return the name of the overall package
		 */
		public String packageName();

		/**
		 * Returns the minimum version supported here.
		 * @return the minimum version supported here.
		 */
		public float minVersion();

		/**
		 * Returns the maximum version supported here.
		 * @return the maximum version supported here.
		 */
		public float maxVersion();

		/**
		 * Execute the package
		 * @param session the session for which the package is being executed
		 * @param command the actual command being executed by this package
		 * @param clientSupported map of client packages supported followed by min-max versions
		 * @param variables key/value pairs for this package
		 */
		public void executePackage(Session session, String command, Map<String,float[]> clientSupported, Map<String,String> variables);
	}
}
