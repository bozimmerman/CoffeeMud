package com.planet_ink.coffee_mud.core.intermud.i3.packets;
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
import java.io.Serializable;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class I3Mud implements Serializable
{
	public static final long serialVersionUID=0;

	public String address;
	public String admin_email;
	public String base_mudlib;
	public String driver;
	public int    modified;
	public String mud_name;
	public String mud_type;
	public String mudlib;
	public int    player_port;
	public int    state;
	public String status;
	public int    tcp_port;
	public int    udp_port;

	public I3Mud()
	{
		super();
	}

	public I3Mud(I3Mud other)
	{
		super();
		address = other.address;
		admin_email = other.admin_email;
		base_mudlib = other.base_mudlib;
		driver = other.driver;
		modified = other.modified;
		mud_name = other.mud_name;
		mud_type = other.mud_type;
		mudlib = other.mudlib;
		player_port = other.player_port;
		state = other.state;
		status = other.status;
		tcp_port = other.tcp_port;
		udp_port = other.udp_port;
	}
}

