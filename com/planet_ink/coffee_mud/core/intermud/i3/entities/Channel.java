package com.planet_ink.coffee_mud.core.intermud.i3.entities;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
/**
 * com.planet_ink.coffee_mud.core.intermud.i3.packets.Channel
 * An I3 channel.
 */

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * This class represents an I3 channel.  The ChannelList
 * class keeps a list of this channels for tracking.
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */

/**
 * Copyright (c) 1996 George Reese
 * This source code may not be modified, copied,
 * redistributed, or used in any fashion without the
 * express written consent of George Reese.
 */
public class Channel implements Serializable
{
	public static final long serialVersionUID=0;

	/**
	 * The name of the channel
	 */
	public String channel;

	/**
	 * The modification status of this channel
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent
	 */
	public int modified;

	/**
	 * The mud which controls the channel
	 */
	public String owner;

	/**
	 * The type of the mud channel
	 * 0  selectively banned
	 * 1  selectively admitted
	 * 2  filtered (selectively admitted)
	 */
	public int    type;

	/**
	 * Either the list of muds allowed,
	 * or the list of muds banned,
	 * according to the Type
	 */
	public List<String> mudlist = new Vector<String>();

	/**
	 * Constructs a new mud channel object
	 */
	public Channel()
	{
		super();
		modified = Persistent.UNMODIFIED;
	}

	/**
	 * Constructs a copy of an existing channel
	 * @param other the other channel
	 */
	public Channel(final Channel other)
	{
		super();
		channel = other.channel;
		modified = other.modified;
		owner = other.owner;
		type = other.type;
		mudlist = new XVector<String>(other.mudlist);
	}
}
