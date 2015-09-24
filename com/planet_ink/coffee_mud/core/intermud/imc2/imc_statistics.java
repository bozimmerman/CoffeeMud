
package com.planet_ink.coffee_mud.core.intermud.imc2;

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
public final class imc_statistics {

	long start; /* when statistics started  			 */

	long rx_pkts; /* Received packets   				   */
	long tx_pkts; /* Transmitted packets				   */
	long rx_bytes; /* Received bytes						*/
	long tx_bytes; /* Transmitted bytes 					*/

	int max_pkt; /* Max. size packet processed  		  */
	int sequence_drops; /* Dropped packets due to age   		 */
}
