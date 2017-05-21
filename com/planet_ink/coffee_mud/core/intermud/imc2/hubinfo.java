package com.planet_ink.coffee_mud.core.intermud.imc2;
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

import java.util.Date;

/**
 * IMC2 version 0.10 - an inter-mud communications protocol
 * Copyright (C) 1996 - 1997 Oliver Jowett: oliver@randomly.org
 *
 * IMC2 Gold versions 1.00 though 2.00 are developed by MudWorld.
 * Copyright (C) 1999 - 2002 Haslage Net Electronics (Anthony R. Haslage)
 *
 * IMC2 MUD-Net version 3.10 is developed by Alsherok and Crimson Oracles
 * Copyright (C) 2002 Roger Libiez ( Samson )
 * Additional code Copyright (C) 2002 Orion Elder
 * Registered with the United States Copyright Office
 * TX 5-555-584
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program (see the file COPYING); if not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * Ported to Java by Istvan David (u_davis@users.sourceforge.net)
 *
 */
public class hubinfo {

	/* The mud's connection data for the hub */
	public String hubname = ""; /* name of hub */
	public String host = ""; /* hostname of hub */
	public int port; /* remote port of hub */
	public String serverpw = ""; /* server password */
	public String clientpw = ""; /* client password */
	public String network = "";  /* intermud network name */
	Date timer_duration; /* delay after next reconnect failure */
	Date last_connected; /* last connected when? */
	int connect_attempts; /* try for 3 times - shogar */
	public boolean autoconnect; /* Do we autoconnect on bootup or not? - Samson */

	/* Conection parameters - These don't save in the config file */
	//int desc; /* descriptor */
	int state; /* IMC_xxxx state */
	int version; /* version of remote site */
		/* try to write at end of cycle regardless of fd_set state? */
	String inbuf = ""; /* input buffer */
	int insize;
	String outbuf = ""; /* output buffer */
	int outsize;
}
