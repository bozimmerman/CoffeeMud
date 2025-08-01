package com.planet_ink.coffee_mud.Items.interfaces;
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

/*
   Copyright 2004-2025 Bo Zimmerman

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
 * Clan Items are variations on normal items that behave uniquely for members of clans.
 * They exploit the rivalry between clans, as well as providing benefits to clans.
 * @author Bo Zimmerman
 */
public interface ClanItem extends Item
{
	/**
	 * The type of clan item this is.  Used mostly for cross-identification between
	 * items without resorting to class name analysis.
	 *
	 * @see ClanItem#getClanItemType()
	 * @see ClanItem#setClanItemType(ClanItemType)
	 * @author Bo Zimmerman
	 */
	public enum ClanItemType // ORDER IS SIGNIFICANT UNTIL clancraft.txt format changes
	{
		FLAG("Flag"),
		BANNER("Banner"),
		GAVEL("Gavel"),
		PROPAGANDA("Propaganda"),
		GATHERITEM("Gathering tool"),
		CRAFTITEM("Crafting tool"),
		SPECIALSCALES("Justice tool"),
		SPECIALSCAVENGER("Scavenging tool"),
		SPECIALOTHER("Clan item"),
		SPECIALTAXER("Taxing tool"),
		DONATIONJOURNAL("Journal"),
		ANTI_PROPAGANDA("Anti-propaganda"),
		SPECIALAPRON("Merchant tool"),
		LEGALBADGE("Officer emblem"),
		TABBARD("Tabbard"),
		SAILORSCAP("Sailors cap")
		;
		private final String ID;
		private String displayName = null;

		public final static String[] ALL=new String[ClanItemType.values().length];
		static
		{
			int x=0;
			for(final ClanItemType type : ClanItemType.values())
			{
				ALL[x++] = type.toString();
			}
		}

		private ClanItemType(final String displayName)
		{
			ID = this.name().replace('_', '-');
			this.displayName=displayName;
		}

		@Override
		public String toString()
		{
			return ID;
		}

		public String getDisplayName()
		{
			if(displayName == null)
			{
				switch(this)
				{
				case FLAG: displayName = CMLib.lang().L("Flag"); break;
				case BANNER: displayName = CMLib.lang().L("Banner"); break;
				case GAVEL: displayName = CMLib.lang().L("Gavel"); break;
				case PROPAGANDA: displayName = CMLib.lang().L("Propaganda"); break;
				case GATHERITEM: displayName = CMLib.lang().L("Gathering tool"); break;
				case CRAFTITEM: displayName = CMLib.lang().L("Crafting tool"); break;
				case SPECIALSCALES: displayName = CMLib.lang().L("Justice tool"); break;
				case SPECIALSCAVENGER: displayName = CMLib.lang().L("Scavenging tool"); break;
				case SPECIALOTHER: displayName = CMLib.lang().L("Clan item"); break;
				case SPECIALTAXER: displayName = CMLib.lang().L("Taxing tool"); break;
				case DONATIONJOURNAL: displayName = CMLib.lang().L("Journal"); break;
				case ANTI_PROPAGANDA: displayName = CMLib.lang().L("Anti-propaganda"); break;
				case SPECIALAPRON: displayName = CMLib.lang().L("Merchant tool"); break;
				case LEGALBADGE: displayName = CMLib.lang().L("Officer emblem"); break;
				case TABBARD: displayName = CMLib.lang().L("Tabbard"); break;
				case SAILORSCAP: displayName = CMLib.lang().L("Sailors cap"); break;
				}
			}
			return displayName;
		}

		/**
		 * Returns the clanitemtype associated with the given string.  The
		 * string may be a numeric ordinal of a clanitemtype, or a string
		 * name, with "_" characters replaced by "-".
		 * @param name the name or ordinal integer value
		 * @return the clanitemtype found, or null
		 */
		public static ClanItemType getValueOf(final String name)
		{
			try
			{
				final int index = Integer.parseInt(name);
				return ClanItemType.values()[index];
			}
			catch(final Exception e)
			{
				final ClanItemType type = ClanItemType.valueOf(name.toUpperCase().trim().replace('-','_'));
				return type;
			}
		}
	}

	/**
	 * Returns the identifier for the specific Clan that this item serves.  There can be only one.
	 * @see ClanItem#setClanID(String)
	 * @return the identifier for the specific Clan that this item serves.  There can be only one.
	 */
	public String clanID();

	/**
	 * Sets the specific clan that this item serves.
	 * @see ClanItem#clanID()
	 * @param ID the specific clan that this item serves.
	 */
	public void setClanID(String ID);

	/**
	 * Returns the type of clan item this is
	 * @see ClanItem.ClanItemType
	 * @see ClanItem#setClanItemType(ClanItemType)
	 * @return the type of clan item this is
	 */
	public ClanItemType getClanItemType();

	/**
	 * Sets the type of clan item this is
	 * @see ClanItem.ClanItemType
	 * @see ClanItem#getClanItemType()
	 * @param type the type of clan item this is
	 */
	public void setClanItemType(ClanItemType type);

	/**
	 * Clan Items have their mob owners tracked.  This is so that sneaky mechanisms for getting items
	 * away from clan member mobs can be thwarted by having them automatically returned.  Only conquest
	 * can end an items usefulness...
	 *
	 * This method returns the room or mob owner that this item should remain with.
	 *
	 * @return the room or mob owner that this item should remain with.
	 */
	public Environmental rightfulOwner();

	/**
	 * Clan Items have their mob owners tracked.  This is so that sneaky mechanisms for getting items
	 * away from clan member mobs can be thwarted by having them automatically returned.  Only conquest
	 * can end an items usefulness...
	 *
	 * This method sets the room or mob owner that this item should remain with.
	 *
	 * @param E the room or mob owner that this item should remain with.
	 */
	public void setRightfulOwner(Environmental E);
}
