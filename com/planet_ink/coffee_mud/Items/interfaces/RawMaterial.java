package com.planet_ink.coffee_mud.Items.interfaces;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.CharStats.CODES;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
 * RawMaterial houses the complete list of raw resource types, which are used to
 * specify Item materials, and the nature of raw resources. In other words, this
 * is what other stuff is made of.
 * 
 * A resource code is divided into a material type in the upper bits, with a
 * specific resource type in the lower bits.
 * 
 * This interface includes a singleton called CODES for accessing information
 * about the various resource codes.
 * 
 * @author Bo Zimmerman
 * 
 */
public interface RawMaterial extends Item
{
	/**
	 * Gets the specific origin of this resource. If a resource has a specific
	 * source that may be of interest to the owner, here it is. For example, if
	 * the resource is meat then this may be Cow meat, or Horse meat, making
	 * "Cow" and "Horse" the domain source.
	 * 
	 * @return the specific origin of this resource
	 */
	public String domainSource();

	/**
	 * Sets the specific origin of this resource. If a resource has a specific
	 * source that may be of interest to the owner, here it is. For example, if
	 * the resource is meat then this may be Cow meat, or Horse meat, making
	 * "Cow" and "Horse" the domain source.
	 * 
	 * @param src the specific origin of this resource
	 */
	public void setDomainSource(String src);

	/**
	 * Scans the owner of this item (room, mob, whatever) for other rawmaterial
	 * resources of the exact same kind and, if found, combines with them. May
	 * result in this item being destroyed.
	 * 
	 * @return true if this item was rebundled, false otherwise.
	 */
	public boolean rebundle();

	/**
	 * A slightly faster version of {@link Item#destroy()} that eliminates the
	 * item from the owner before calling destroy.
	 */
	public void quickDestroy();

	/**
	 * Item basic material type denoting that this material is of unknown
	 * material type
	 */
	public final static int	MATERIAL_UNKNOWN	= 0;
	/**
	 * Item basic material type denoting that this material is of a cloth-like
	 * type
	 */
	public final static int	MATERIAL_CLOTH		= 1 << 8;
	/**
	 * Item basic material type denoting that this material is of a leather-like
	 * type
	 */
	public final static int	MATERIAL_LEATHER	= 2 << 8;
	/**
	 * Item basic material type denoting that this material is of a metal-like
	 * type
	 */
	public final static int	MATERIAL_METAL		= 3 << 8;
	/**
	 * Item basic material type denoting that this material is of a special
	 * metal mithril-like type
	 */
	public final static int	MATERIAL_MITHRIL	= 4 << 8;
	/**
	 * Item basic material type denoting that this material is of a wood-like
	 * type
	 */
	public final static int	MATERIAL_WOODEN		= 5 << 8;
	/**
	 * Item basic material type denoting that this material is of a glass-like
	 * type
	 */
	public final static int	MATERIAL_GLASS		= 6 << 8;
	/**
	 * Item basic material type denoting that this material is of a
	 * vegetation-like type
	 */
	public final static int	MATERIAL_VEGETATION	= 7 << 8;
	/**
	 * Item basic material type denoting that this material is of a fleshy-like
	 * type
	 */
	public final static int	MATERIAL_FLESH		= 8 << 8;
	/**
	 * Item basic material type denoting that this material is of a paper-like
	 * type
	 */
	public final static int	MATERIAL_PAPER		= 9 << 8;
	/**
	 * Item basic material type denoting that this material is of a rocky-like
	 * type
	 */
	public final static int	MATERIAL_ROCK		= 10 << 8;
	/**
	 * Item basic material type denoting that this material is of a liquidy-like
	 * type
	 */
	public final static int	MATERIAL_LIQUID		= 11 << 8;
	/**
	 * Item basic material type denoting that this material is of a jewely-like
	 * type
	 */
	public final static int	MATERIAL_PRECIOUS	= 12 << 8;
	/**
	 * Item basic material type denoting that this material is of a energy-like
	 * type
	 */
	public final static int	MATERIAL_ENERGY		= 13 << 8;
	/**
	 * Item basic material type denoting that this material is of a
	 * synthetic-like type
	 */
	public final static int	MATERIAL_SYNTHETIC	= 14 << 8;
	/**
	 * Item basic material type denoting that this material is of a gassy-like
	 * type
	 */
	public final static int	MATERIAL_GAS		= 15 << 8;
	/**
	 * Item basic material mask that filters out all bits except the basic
	 * material type
	 */
	public final static int	MATERIAL_MASK		= 255 << 8;

	/**
	 * An enum of all the basic material types, allowing lookups and such
	 * 
	 * @author Bo Zimmerman
	 * 
	 */
	public static enum Material
	{
		UNKNOWN("Unknown material"),
		CLOTH("Cloth"),
		LEATHER("Leather"),
		METAL("Metal"),
		MITHRIL("Metal"),
		WOODEN("Wood"),
		GLASS("Glass"),
		VEGETATION("Vegetation"),
		FLESH("Flesh"),
		PAPER("Paper"),
		ROCK("Rock"),
		LIQUID("Liquid"),
		PRECIOUS("Stone"),
		ENERGY("Energy"),
		SYNTHETIC("Plastic"),
		GAS("Gas");

		private final static String[]	descs	= CMParms.toStringArray(values());
		private final int				mask;
		private final String			desc;
		private final String			noun;

		private Material(String noun)
		{
			this.mask = (ordinal() == 0) ? 0 : (ordinal() << 8);
			this.desc = this.toString();
			this.noun = noun;
		}

		/**
		 * The number of basic materials
		 * @return number of basic materials
		 */
		public static int size()
		{
			return values().length;
		}

		/**
		 * The 2nd byte material code mask
		 * @return the material code
		 */
		public int mask()
		{
			return mask;
		}

		/**
		 * Gets the friendly name of the material
		 * @return the friendly name of the material
		 */
		public String noun()
		{
			return noun;
		}

		/**
		 * Gets the coded enum uppercase name of this material.
		 * @return the coded enum uppercase name of this material.
		 */
		public String desc()
		{
			return desc;
		}

		/**
		 * Gets the list of all enum material code uppercase names
		 * @return the list of all enum material code uppercase names
		 */
		public static String[] names()
		{
			return descs;
		}

		/**
		 * Gets the material enum object given the material code
		 * @param mask the material code to look up
		 * @return the material enum object or null
		 */
		public static Material findByMask(int mask)
		{
			final int maskOrdinal = (mask < size()) ? mask : (mask & MATERIAL_MASK) >> 8;
			if (maskOrdinal < Material.values().length)
				return Material.values()[maskOrdinal];
			return null;
		}

		/**
		 * Gets the material enum object given the exact uppercase name.
		 * @param name the exact uppercase material name to look up
		 * @return the material enum object or null
		 */
		public static Material find(String name)
		{
			try
			{
				return Material.valueOf(name);
			}
			catch (final Exception e)
			{
				return null;
			}
		}

		/**
		 * Gets the material enum object given the case-insensitive name.
		 * @param name the case-insensitive name
		 * @return the material enum object or null
		 */
		public static Material findIgnoreCase(String name)
		{
			return find(name.toUpperCase().trim());
		}

		/**
		 * Searches for the first material enum that starts with the
		 * given name
		 * @param name the name to look for as a prefix
		 * @return the first material enum matching, or null
		 */
		public static Material startsWith(String name)
		{
			for (final Material m : values())
			{
				if (m.desc.startsWith(name))
					return m;
			}
			return null;
		}

		/**
		 * Searches for the first material enum that starts with the
		 * given case-insensitive name
		 * @param name the name to look for as a prefix
		 * @return the first material enum matching, or null
		 */
		public static Material startsWithIgnoreCase(String name)
		{
			return startsWith(name.toUpperCase().trim());
		}
	}

	public final static int	RESOURCE_NOTHING		= MATERIAL_UNKNOWN | 0;
	public final static int	RESOURCE_MEAT			= MATERIAL_FLESH | 1;
	public final static int	RESOURCE_BEEF			= MATERIAL_FLESH | 2;
	public final static int	RESOURCE_PORK			= MATERIAL_FLESH | 3;
	public final static int	RESOURCE_POULTRY		= MATERIAL_FLESH | 4;
	public final static int	RESOURCE_MUTTON			= MATERIAL_FLESH | 5;
	public final static int	RESOURCE_FISH			= MATERIAL_FLESH | 6;
	public final static int	RESOURCE_WHEAT			= MATERIAL_VEGETATION | 7;
	public final static int	RESOURCE_CORN			= MATERIAL_VEGETATION | 8;
	public final static int	RESOURCE_RICE			= MATERIAL_VEGETATION | 9;
	public final static int	RESOURCE_CARROTS		= MATERIAL_VEGETATION | 10;
	public final static int	RESOURCE_TOMATOES		= MATERIAL_VEGETATION | 11;
	public final static int	RESOURCE_PEPPERS		= MATERIAL_VEGETATION | 12;
	public final static int	RESOURCE_GREENS			= MATERIAL_VEGETATION | 13;
	public final static int	RESOURCE_FRUIT			= MATERIAL_VEGETATION | 14;
	public final static int	RESOURCE_APPLES			= MATERIAL_VEGETATION | 15;
	public final static int	RESOURCE_BERRIES		= MATERIAL_VEGETATION | 16;
	public final static int	RESOURCE_ORANGES		= MATERIAL_VEGETATION | 17;
	public final static int	RESOURCE_LEMONS			= MATERIAL_VEGETATION | 18;
	public final static int	RESOURCE_GRAPES			= MATERIAL_VEGETATION | 19;
	public final static int	RESOURCE_OLIVES			= MATERIAL_VEGETATION | 20;
	public final static int	RESOURCE_POTATOES		= MATERIAL_VEGETATION | 21;
	public final static int	RESOURCE_CACTUS			= MATERIAL_VEGETATION | 22;
	public final static int	RESOURCE_DATES			= MATERIAL_VEGETATION | 23;
	public final static int	RESOURCE_SEAWEED		= MATERIAL_VEGETATION | 24;
	public final static int	RESOURCE_STONE			= MATERIAL_ROCK | 25;
	public final static int	RESOURCE_LIMESTONE		= MATERIAL_ROCK | 26;
	public final static int	RESOURCE_FLINT			= MATERIAL_ROCK | 27;
	public final static int	RESOURCE_GRANITE		= MATERIAL_ROCK | 28;
	public final static int	RESOURCE_OBSIDIAN		= MATERIAL_ROCK | 29;
	public final static int	RESOURCE_MARBLE			= MATERIAL_ROCK | 30;
	public final static int	RESOURCE_SAND			= MATERIAL_ROCK | 31;
	public final static int	RESOURCE_JADE			= MATERIAL_PRECIOUS | 32;
	public final static int	RESOURCE_IRON			= MATERIAL_METAL | 33;
	public final static int	RESOURCE_LEAD			= MATERIAL_METAL | 34;
	public final static int	RESOURCE_BRONZE			= MATERIAL_METAL | 35;
	public final static int	RESOURCE_SILVER			= MATERIAL_METAL | 36;
	public final static int	RESOURCE_GOLD			= MATERIAL_METAL | 37;
	public final static int	RESOURCE_ZINC			= MATERIAL_METAL | 38;
	public final static int	RESOURCE_COPPER			= MATERIAL_METAL | 39;
	public final static int	RESOURCE_TIN			= MATERIAL_METAL | 40;
	public final static int	RESOURCE_MITHRIL		= MATERIAL_MITHRIL | 41;
	public final static int	RESOURCE_ADAMANTITE		= MATERIAL_MITHRIL | 42;
	public final static int	RESOURCE_STEEL			= MATERIAL_METAL | 43;
	public final static int	RESOURCE_BRASS			= MATERIAL_METAL | 44;
	public final static int	RESOURCE_WOOD			= MATERIAL_WOODEN | 45;
	public final static int	RESOURCE_PINE			= MATERIAL_WOODEN | 46;
	public final static int	RESOURCE_BALSA			= MATERIAL_WOODEN | 47;
	public final static int	RESOURCE_OAK			= MATERIAL_WOODEN | 48;
	public final static int	RESOURCE_MAPLE			= MATERIAL_WOODEN | 49;
	public final static int	RESOURCE_REDWOOD		= MATERIAL_WOODEN | 50;
	public final static int	RESOURCE_HICKORY		= MATERIAL_WOODEN | 51;
	public final static int	RESOURCE_SCALES			= MATERIAL_LEATHER | 52;
	public final static int	RESOURCE_FUR			= MATERIAL_CLOTH | 53;
	public final static int	RESOURCE_LEATHER		= MATERIAL_LEATHER | 54;
	public final static int	RESOURCE_HIDE			= MATERIAL_CLOTH | 55;
	public final static int	RESOURCE_WOOL			= MATERIAL_CLOTH | 56;
	public final static int	RESOURCE_FEATHERS		= MATERIAL_CLOTH | 57;
	public final static int	RESOURCE_COTTON			= MATERIAL_CLOTH | 58;
	public final static int	RESOURCE_HEMP			= MATERIAL_CLOTH | 59;
	public final static int	RESOURCE_FRESHWATER		= MATERIAL_LIQUID | 60;
	public final static int	RESOURCE_SALTWATER		= MATERIAL_LIQUID | 61;
	public final static int	RESOURCE_DRINKABLE		= MATERIAL_LIQUID | 62;
	public final static int	RESOURCE_GLASS			= MATERIAL_GLASS | 63;
	public final static int	RESOURCE_PAPER			= MATERIAL_PAPER | 64;
	public final static int	RESOURCE_CLAY			= MATERIAL_GLASS | 65;
	public final static int	RESOURCE_CHINA			= MATERIAL_GLASS | 66;
	public final static int	RESOURCE_DIAMOND		= MATERIAL_PRECIOUS | 67;
	public final static int	RESOURCE_CRYSTAL		= MATERIAL_GLASS | 68;
	public final static int	RESOURCE_GEM			= MATERIAL_PRECIOUS | 69;
	public final static int	RESOURCE_PEARL			= MATERIAL_PRECIOUS | 70;
	public final static int	RESOURCE_PLATINUM		= MATERIAL_METAL | 71;
	public final static int	RESOURCE_MILK			= MATERIAL_LIQUID | 72;
	public final static int	RESOURCE_EGGS			= MATERIAL_FLESH | 73;
	public final static int	RESOURCE_HOPS			= MATERIAL_VEGETATION | 74;
	public final static int	RESOURCE_COFFEEBEANS	= MATERIAL_VEGETATION | 75;
	public final static int	RESOURCE_COFFEE			= MATERIAL_LIQUID | 76;
	public final static int	RESOURCE_OPAL			= MATERIAL_PRECIOUS | 77;
	public final static int	RESOURCE_TOPAZ			= MATERIAL_PRECIOUS | 78;
	public final static int	RESOURCE_AMETHYST		= MATERIAL_PRECIOUS | 79;
	public final static int	RESOURCE_GARNET			= MATERIAL_PRECIOUS | 80;
	public final static int	RESOURCE_AMBER			= MATERIAL_PRECIOUS | 81;
	public final static int	RESOURCE_AQUAMARINE		= MATERIAL_PRECIOUS | 82;
	public final static int	RESOURCE_CRYSOBERYL		= MATERIAL_PRECIOUS | 83;
	public final static int	RESOURCE_IRONWOOD		= MATERIAL_WOODEN | 84;
	public final static int	RESOURCE_SILK			= MATERIAL_CLOTH | 85;
	public final static int	RESOURCE_COCOA			= MATERIAL_VEGETATION | 86;
	public final static int	RESOURCE_BLOOD			= MATERIAL_LIQUID | 87;
	public final static int	RESOURCE_BONE			= MATERIAL_ROCK | 88;
	public final static int	RESOURCE_COAL			= MATERIAL_ROCK | 89;
	public final static int	RESOURCE_LAMPOIL		= MATERIAL_LIQUID | 90;
	public final static int	RESOURCE_POISON			= MATERIAL_LIQUID | 91;
	public final static int	RESOURCE_LIQUOR			= MATERIAL_LIQUID | 92;
	public final static int	RESOURCE_SUGAR			= MATERIAL_VEGETATION | 93;
	public final static int	RESOURCE_HONEY			= MATERIAL_LIQUID | 94;
	public final static int	RESOURCE_BARLEY			= MATERIAL_VEGETATION | 95;
	public final static int	RESOURCE_MUSHROOMS		= MATERIAL_VEGETATION | 96;
	public final static int	RESOURCE_HERBS			= MATERIAL_VEGETATION | 97;
	public final static int	RESOURCE_VINE			= MATERIAL_VEGETATION | 98;
	public final static int	RESOURCE_FLOWERS		= MATERIAL_VEGETATION | 99;
	public final static int	RESOURCE_PLASTIC		= MATERIAL_SYNTHETIC | 100;
	public final static int	RESOURCE_RUBBER			= MATERIAL_SYNTHETIC | 101;
	public final static int	RESOURCE_EBONY			= MATERIAL_ROCK | 102;
	public final static int	RESOURCE_IVORY			= MATERIAL_ROCK | 103;
	public final static int	RESOURCE_WAX			= MATERIAL_FLESH | 104;
	public final static int	RESOURCE_NUTS			= MATERIAL_VEGETATION | 105;
	public final static int	RESOURCE_BREAD			= MATERIAL_VEGETATION | 106;
	public final static int	RESOURCE_CRACKER		= MATERIAL_VEGETATION | 107;
	public final static int	RESOURCE_YEW			= MATERIAL_WOODEN | 108;
	public final static int	RESOURCE_DUST			= MATERIAL_ROCK | 109;
	public final static int	RESOURCE_PIPEWEED		= MATERIAL_VEGETATION | 110;
	public final static int	RESOURCE_ENERGY			= MATERIAL_ENERGY | 111;
	public final static int	RESOURCE_STRAWBERRIES	= MATERIAL_VEGETATION | 112;
	public final static int	RESOURCE_BLUEBERRIES	= MATERIAL_VEGETATION | 113;
	public final static int	RESOURCE_RASPBERRIES	= MATERIAL_VEGETATION | 114;
	public final static int	RESOURCE_BOYSENBERRIES	= MATERIAL_VEGETATION | 115;
	public final static int	RESOURCE_BLACKBERRIES	= MATERIAL_VEGETATION | 116;
	public final static int	RESOURCE_SMURFBERRIES	= MATERIAL_VEGETATION | 117;
	public final static int	RESOURCE_PEACHES		= MATERIAL_VEGETATION | 118;
	public final static int	RESOURCE_PLUMS			= MATERIAL_VEGETATION | 119;
	public final static int	RESOURCE_ONIONS			= MATERIAL_VEGETATION | 120;
	public final static int	RESOURCE_CHERRIES		= MATERIAL_VEGETATION | 121;
	public final static int	RESOURCE_GARLIC			= MATERIAL_VEGETATION | 122;
	public final static int	RESOURCE_PINEAPPLES		= MATERIAL_VEGETATION | 123;
	public final static int	RESOURCE_COCONUTS		= MATERIAL_VEGETATION | 124;
	public final static int	RESOURCE_BANANAS		= MATERIAL_VEGETATION | 125;
	public final static int	RESOURCE_LIMES			= MATERIAL_VEGETATION | 126;
	public final static int	RESOURCE_SAP			= MATERIAL_LIQUID | 127;
	public final static int	RESOURCE_ONYX			= MATERIAL_PRECIOUS | 128;
	public final static int	RESOURCE_TURQUOISE		= MATERIAL_PRECIOUS | 129;
	public final static int	RESOURCE_PERIDOT		= MATERIAL_PRECIOUS | 130;
	public final static int	RESOURCE_QUARTZ			= MATERIAL_PRECIOUS | 131;
	public final static int	RESOURCE_LAPIS			= MATERIAL_PRECIOUS | 132;
	public final static int	RESOURCE_BLOODSTONE		= MATERIAL_PRECIOUS | 133;
	public final static int	RESOURCE_MOONSTONE		= MATERIAL_PRECIOUS | 134;
	public final static int	RESOURCE_ALEXANDRITE	= MATERIAL_PRECIOUS | 135;
	public final static int	RESOURCE_TEAK			= MATERIAL_WOODEN | 136;
	public final static int	RESOURCE_CEDAR			= MATERIAL_WOODEN | 137;
	public final static int	RESOURCE_ELM			= MATERIAL_WOODEN | 138;
	public final static int	RESOURCE_CHERRYWOOD		= MATERIAL_WOODEN | 139;
	public final static int	RESOURCE_BEECHWOOD		= MATERIAL_WOODEN | 140;
	public final static int	RESOURCE_WILLOW			= MATERIAL_WOODEN | 141;
	public final static int	RESOURCE_SYCAMORE		= MATERIAL_WOODEN | 142;
	public final static int	RESOURCE_SPRUCE			= MATERIAL_WOODEN | 143;
	public final static int	RESOURCE_MESQUITE		= MATERIAL_WOODEN | 144;
	public final static int	RESOURCE_BASALT			= MATERIAL_ROCK | 145;
	public final static int	RESOURCE_SHALE			= MATERIAL_ROCK | 146;
	public final static int	RESOURCE_PUMICE			= MATERIAL_ROCK | 147;
	public final static int	RESOURCE_SANDSTONE		= MATERIAL_ROCK | 148;
	public final static int	RESOURCE_SOAPSTONE		= MATERIAL_ROCK | 149;
	public final static int	RESOURCE_SALMON			= MATERIAL_FLESH | 150;
	public final static int	RESOURCE_CARP			= MATERIAL_FLESH | 151;
	public final static int	RESOURCE_TROUT			= MATERIAL_FLESH | 152;
	public final static int	RESOURCE_SHRIMP			= MATERIAL_FLESH | 153;
	public final static int	RESOURCE_TUNA			= MATERIAL_FLESH | 154;
	public final static int	RESOURCE_CATFISH		= MATERIAL_FLESH | 155;
	public final static int	RESOURCE_BAMBOO			= MATERIAL_WOODEN | 156;
	public final static int	RESOURCE_SOAP			= MATERIAL_VEGETATION | 157;
	public final static int	RESOURCE_SPIDERSTEEL	= MATERIAL_CLOTH | 158;
	public final static int	RESOURCE_ASH			= MATERIAL_VEGETATION | 159;
	public final static int	RESOURCE_PERFUME		= MATERIAL_LIQUID | 160;
	public final static int	RESOURCE_ATLANTITE		= MATERIAL_MITHRIL | 161;
	public final static int	RESOURCE_CHEESE			= MATERIAL_VEGETATION | 162;
	public final static int	RESOURCE_BEANS			= MATERIAL_VEGETATION | 163;
	public final static int	RESOURCE_CRANBERRIES	= MATERIAL_VEGETATION | 164;
	public final static int	RESOURCE_DRAGONBLOOD	= MATERIAL_LIQUID | 165;
	public final static int	RESOURCE_DRAGONMEAT		= MATERIAL_FLESH | 166;
	public final static int	RESOURCE_RUBY			= MATERIAL_PRECIOUS | 167;
	public final static int	RESOURCE_EMERALD		= MATERIAL_PRECIOUS | 168;
	public final static int	RESOURCE_SAPPHIRE		= MATERIAL_PRECIOUS | 169;
	public final static int	RESOURCE_AGATE			= MATERIAL_PRECIOUS | 170;
	public final static int	RESOURCE_CITRINE		= MATERIAL_PRECIOUS | 171;
	public final static int	RESOURCE_REED			= MATERIAL_WOODEN | 172;
	public final static int	RESOURCE_ALABASTER		= MATERIAL_ROCK | 173;
	public final static int	RESOURCE_CHROMIUM		= MATERIAL_METAL | 174;
	public final static int	RESOURCE_WHITE_GOLD		= MATERIAL_METAL | 175;
	public final static int	RESOURCE_DRAGONSCALES	= MATERIAL_METAL | 176;
	public final static int	RESOURCE_PLASMA			= MATERIAL_ENERGY | 177;
	public final static int	RESOURCE_ELECTRICITY	= MATERIAL_ENERGY | 178;
	public final static int	RESOURCE_DEUTERIUM		= MATERIAL_SYNTHETIC | 179;
	public final static int	RESOURCE_SLIME			= MATERIAL_LIQUID | 180;
	public final static int	RESOURCE_AIR			= MATERIAL_GAS | 181;
	public final static int	RESOURCE_OXYGEN			= MATERIAL_GAS | 182;
	public final static int	RESOURCE_HYDROGEN		= MATERIAL_GAS | 183;
	public final static int	RESOURCE_FLOURINE		= MATERIAL_GAS | 184;
	public final static int	RESOURCE_NITROGEN		= MATERIAL_GAS | 185;
	public final static int	RESOURCE_CHLORINE		= MATERIAL_GAS | 186;
	public final static int	RESOURCE_ALUMINUM		= MATERIAL_METAL | 187;
	public final static int	RESOURCE_TITANIUM		= MATERIAL_METAL | 188;
	public final static int	RESOURCE_FIBREGLASS		= MATERIAL_CLOTH | 189;
	public final static int	RESOURCE_ULTRANIUM		= MATERIAL_METAL | 190;
	public final static int	RESOURCE_DURANIUM		= MATERIAL_METAL | 191;
	public final static int	RESOURCE_URANIUM		= MATERIAL_ROCK  | 192;
	public final static int	RESOURCE_ANTIMATTER		= MATERIAL_ENERGY| 193;
	public final static int	RESOURCE_DIAMELS		= MATERIAL_PRECIOUS | 194;
	public final static int	RESOURCE_PETROLEUM		= MATERIAL_LIQUID| 195;
	public final static int	RESOURCE_GASOLINE		= MATERIAL_LIQUID| 196;
	public final static int	RESOURCE_DIESEL			= MATERIAL_LIQUID| 197;
	public final static int	RESOURCE_SILICON		= MATERIAL_ROCK  | 198;
	public final static int	RESOURCE_POLYESTER		= MATERIAL_CLOTH | 199;
	public final static int	RESOURCE_DENIM			= MATERIAL_CLOTH | 200;
	public final static int	RESOURCE_DILITHIUM		= MATERIAL_PRECIOUS | 201;
	public final static int	RESOURCE_TAR			= MATERIAL_LIQUID | 202;
	public final static int	RESOURCE_SALT			= MATERIAL_ROCK | 203;
	public final static int	RESOURCE_SPICE			= MATERIAL_VEGETATION | 204;
	public final static int	RESOURCE_DIRT			= MATERIAL_ROCK | 205;
	public final static int	RESOURCE_FUNGUS			= MATERIAL_VEGETATION | 206;
	
	public final static int	RESOURCE_MASK			= 255;

	/**
	 * Enums that set certain category flags on a resource,
	 * such as whether it is a fish type, or a berry type
	 * @author Bo Zimmerman
	 *
	 */
	public static enum ResourceFlag
	{
		BERRY,
		FISH,
		WOODY
	}

	/**
	 * Enum of all the default resource data for all basic hard coded
	 * resources.
	 * Data about each resource includes:
	 * code, value, frequency, hardness, bouyancy, smell, effects, ResourceFlag flags
	 * @author Bo Zimmerman
	 */
	public static enum DefResource
	{
		NOTHING(RESOURCE_NOTHING, 0, 0, 0, 0, "", "", null),
		MEAT(RESOURCE_MEAT, 4, 20, 1, 3000, "", "", null),
		BEEF(RESOURCE_BEEF, 6, 20, 1, 3000, "", "", null),
		PORK(RESOURCE_PORK, 8, 20, 1, 2500, "", "", null),
		POULTRY(RESOURCE_POULTRY, 3, 20, 1, 2000, "", "", null),
		MUTTON(RESOURCE_MUTTON, 4, 20, 1, 2800, "", "", null),
		FISH(RESOURCE_FISH, 5, 100, 1, 590, "strong fishy", "", ResourceFlag.FISH),
		WHEAT(RESOURCE_WHEAT, 1, 20, 1, 770, "", "", null),
		CORN(RESOURCE_CORN, 1, 20, 1, 720, "", "", null),
		RICE(RESOURCE_RICE, 1, 20, 1, 750, "", "", null),
		CARROTS(RESOURCE_CARROTS, 1, 5, 1, 720, "", "", null),
		TOMATOES(RESOURCE_TOMATOES, 1, 5, 1, 640, "", "", null),
		PEPPERS(RESOURCE_PEPPERS, 1, 5, 1, 640, "spicy", "", null),
		GREENS(RESOURCE_GREENS, 1, 5, 1, 540, "very mild", "", null),
		FRUIT(RESOURCE_FRUIT, 2, 10, 1, 720, "sweet and fruity", "", null),
		APPLES(RESOURCE_APPLES, 2, 10, 1, 640, "sweet apply", "", ResourceFlag.WOODY),
		BERRIES(RESOURCE_BERRIES, 2, 15, 1, 720, "sweet berry", "", ResourceFlag.BERRY),
		ORANGES(RESOURCE_ORANGES, 2, 10, 1, 640, "citrusy", "", ResourceFlag.WOODY),
		LEMONS(RESOURCE_LEMONS, 2, 10, 1, 480, "strong citrusy", "Prop_UseSpellCast2(Spell_ShrinkMouth)", ResourceFlag.WOODY),
		GRAPES(RESOURCE_GRAPES, 3, 5, 1, 680, "mild sweet", "", null),
		OLIVES(RESOURCE_OLIVES, 2, 5, 1, 640, "pickly olive", "", null),
		POTATOES(RESOURCE_POTATOES, 1, 5, 1, 770, "", "", null),
		CACTUS(RESOURCE_CACTUS, 2, 5, 1, 680, "", "", null),
		DATES(RESOURCE_DATES, 2, 2, 1, 720, "sweet plumy", "", null),
		SEAWEED(RESOURCE_SEAWEED, 1, 50, 1, 540, "", "", null),
		STONE(RESOURCE_STONE, 1, 80, 5, 2500, "mild musty", "", null),
		LIMESTONE(RESOURCE_LIMESTONE, 1, 20, 4, 1550, "", "", null),
		FLINT(RESOURCE_FLINT, 1, 10, 4, 2600, "", "", null),
		GRANITE(RESOURCE_GRANITE, 2, 10, 6, 2690, "", "", null),
		OBSIDIAN(RESOURCE_OBSIDIAN, 10, 5, 6, 2650, "", "", null),
		MARBLE(RESOURCE_MARBLE, 20, 5, 5, 2560, "", "", null),
		SAND(RESOURCE_SAND, 1, 50, 1, 1600, "", "", null),
		JADE(RESOURCE_JADE, 50, 2, 5, 3800, "", "", null),
		IRON(RESOURCE_IRON, 20, 10, 6, 7900, "", "", null),
		LEAD(RESOURCE_LEAD, 10, 10, 4, 11300, "", "", null),
		BRONZE(RESOURCE_BRONZE, 10, 10, 5, 8100, "", "", null),
		SILVER(RESOURCE_SILVER, 30, 2, 5, 10500, "", "", null),
		GOLD(RESOURCE_GOLD, 150, 1, 5, 19320, "", "", null),
		WHITE_GOLD(RESOURCE_WHITE_GOLD, 150, 1, 5, 1450, "", "", null),
		PLATINUM(RESOURCE_PLATINUM, 750, 1, 6, 21450, "", "", null),
		ALUMINUM(RESOURCE_ALUMINUM, 15000, 0, 2, 7300, "", "", null),
		ZINC(RESOURCE_ZINC, 10, 5, 5, 7100, "", "", null),
		COPPER(RESOURCE_COPPER, 10, 10, 5, 8900, "", "", null),
		TIN(RESOURCE_TIN, 10, 10, 4, 7300, "", "", null),
		MITHRIL(RESOURCE_MITHRIL, 100, 1, 9, 3990, "", "", null),
		ADAMANTITE(RESOURCE_ADAMANTITE, 175, 1, 10, 4500, "", "", null),
		STEEL(RESOURCE_STEEL, 75, 0, 8, 7840, "", "", null),
		BRASS(RESOURCE_BRASS, 120, 0, 6, 5500, "", "", null),
		WOOD(RESOURCE_WOOD, 2, 10, 3, 920, "", "", null),
		PINE(RESOURCE_PINE, 4, 10, 3, 650, "fresh, clean piney", "", null),
		BALSA(RESOURCE_BALSA, 1, 5, 2, 130, "", "", null),
		OAK(RESOURCE_OAK, 5, 10, 3, 720, "rich oaky", "", null),
		MAPLE(RESOURCE_MAPLE, 10, 5, 3, 689, "mild maply", "", null),
		REDWOOD(RESOURCE_REDWOOD, 20, 2, 3, 450, "", "", null),
		HICKORY(RESOURCE_HICKORY, 5, 5, 3, 830, "", "", null),
		SCALES(RESOURCE_SCALES, 10, 20, 4, 1800, "", "", null),
		FUR(RESOURCE_FUR, 20, 20, 2, 890, "musky", "", null),
		LEATHER(RESOURCE_LEATHER, 10, 20, 2, 945, "strong leathery", "", null),
		HIDE(RESOURCE_HIDE, 4, 20, 2, 920, "mild stinky", "", null),
		WOOL(RESOURCE_WOOL, 10, 20, 1, 1310, "", "", null),
		FEATHERS(RESOURCE_FEATHERS, 10, 20, 1, 20, "", "", null),
		COTTON(RESOURCE_COTTON, 5, 20, 1, 590, "", "", null),
		HEMP(RESOURCE_HEMP, 4, 10, 1, 720, "grassy", "", null),
		WATER(RESOURCE_FRESHWATER, 0, 10, 0, 1000, "", "", null),
		SALT_WATER(RESOURCE_SALTWATER, 0, 100, 0, 1030, "", "", null),
		LIQUID(RESOURCE_DRINKABLE, 0, 1, 0, 1000, "", "", null),
		GLASS(RESOURCE_GLASS, 10, 0, 3, 2800, "", "", null),
		PAPER(RESOURCE_PAPER, 10, 0, 0, 920, "", "", null),
		CLAY(RESOURCE_CLAY, 1, 50, 1, 1750, "mild dusty", "", null),
		CHINA(RESOURCE_CHINA, 30, 0, 3, 2400, "", "", null),
		DIAMOND(RESOURCE_DIAMOND, 500, 1, 9, 3510, "", "", null),
		CRYSTAL(RESOURCE_CRYSTAL, 10, 5, 5, 2200, "", "", null),
		GEM(RESOURCE_GEM, 100, 1, 5, 3500, "", "", null),
		PEARL(RESOURCE_PEARL, 380, 1, 5, 2000, "", "", null),
		MILK(RESOURCE_MILK, 2, 10, 0, 1020, "mild milky", "", null),
		EGGS(RESOURCE_EGGS, 2, 10, 0, 1120, "", "", null),
		HOPS(RESOURCE_HOPS, 2, 20, 1, 340, "mild grainy", "", null),
		COFFEEBEANS(RESOURCE_COFFEEBEANS, 2, 10, 1, 560, "mild coffee", "Poison_Caffeine", null),
		COFFEE(RESOURCE_COFFEE, 0, 10, 0, 430, "rich coffee", "Poison_Caffeine", null),
		OPAL(RESOURCE_OPAL, 80, 2, 5, 2250, "", "", null),
		TOPAZ(RESOURCE_TOPAZ, 200, 2, 5, 3570, "", "", null),
		AMETHYST(RESOURCE_AMETHYST, 300, 2, 5, 2651, "", "", null),
		GARNET(RESOURCE_GARNET, 70, 2, 5, 3870, "", "", null),
		AMBER(RESOURCE_AMBER, 80, 5, 5, 2500, "", "", null),
		AQUAMARINE(RESOURCE_AQUAMARINE, 50, 2, 5, 2800, "", "", null),
		CRYSOBERYL(RESOURCE_CRYSOBERYL, 50, 2, 5, 2800, "", "", null),
		IRONWOOD(RESOURCE_IRONWOOD, 25, 5, 4, 990, "", "", null),
		SILK(RESOURCE_SILK, 200, 5, 1, 1100, "", "", null),
		COCOA(RESOURCE_COCOA, 4, 5, 0, 590, "", "", ResourceFlag.WOODY),
		BLOOD(RESOURCE_BLOOD, 1, 100, 0, 1025, "strong salty", "", null),
		BONE(RESOURCE_BONE, 1, 100, 5, 1600, "", "", null),
		COAL(RESOURCE_COAL, 1, 50, 1, 1800, "chalky", "", null),
		LAMP_OIL(RESOURCE_LAMPOIL, 1, 10, 1, 880, "light oily", "", null),
		POISON(RESOURCE_POISON, 1, 1, 1, 1000, "", "", null),
		LIQUOR(RESOURCE_LIQUOR, 10, 1, 1, 790, "alcohol", "", null),
		SUGAR(RESOURCE_SUGAR, 1, 50, 1, 1600, "", "", null),
		HONEY(RESOURCE_HONEY, 1, 50, 1, 1600, "", "", null),
		BARLEY(RESOURCE_BARLEY, 1, 20, 1, 610, "", "", null),
		MUSHROOMS(RESOURCE_MUSHROOMS, 1, 20, 1, 500, "", "", null),
		HERBS(RESOURCE_HERBS, 1, 10, 1, 770, "fresh herbal", "", null),
		VINE(RESOURCE_VINE, 1, 10, 1, 880, "rich green", "", null),
		FLOWERS(RESOURCE_FLOWERS, 1, 10, 1, 720, "nice floral", "", null),
		PLASTIC(RESOURCE_PLASTIC, 25, 0, 4, 950, "", "", null),
		RUBBER(RESOURCE_RUBBER, 25, 0, 1, 1506, "sour rubbery", "", null),
		EBONY(RESOURCE_EBONY, 5, 5, 5, 2910, "", "", null),
		IVORY(RESOURCE_IVORY, 5, 5, 3, 1840, "", "", null),
		WAX(RESOURCE_WAX, 1, 0, 0, 900, "", "", null),
		NUTS(RESOURCE_NUTS, 0, 20, 0, 640, "mild nutty", "", ResourceFlag.WOODY),
		BREAD(RESOURCE_BREAD, 3, 0, 0, 660, "", "", null),
		CRACKER(RESOURCE_CRACKER, 2, 0, 0, 200, "", "", null),
		YEW(RESOURCE_YEW, 15, 2, 5, 850, "", "", null),
		DUST(RESOURCE_DUST, 0, 20, 0, 1120, "dusty", "", null),
		PIPEWEED(RESOURCE_PIPEWEED, 3, 10, 1, 320, "strong grassy", "", null),
		ENERGY(RESOURCE_ENERGY, 30, 0, 4, 0, "", "", null),
		STRAWBERRIES(RESOURCE_STRAWBERRIES, 10, 1, 1, 750, "sweet berry", "", ResourceFlag.BERRY),
		BLUEBERRIES(RESOURCE_BLUEBERRIES, 10, 1, 1, 750, "sweet berry", "", ResourceFlag.BERRY),
		RASPBERRIES(RESOURCE_RASPBERRIES, 10, 1, 1, 750, "sweet berry", "", ResourceFlag.BERRY),
		BOYSENBERRIES(RESOURCE_BOYSENBERRIES, 10, 1, 1, 750, "sweet berry", "", ResourceFlag.BERRY),
		BLACKBERRIES(RESOURCE_BLACKBERRIES, 10, 1, 1, 750, "sweet berry", "", ResourceFlag.BERRY),
		SMURFBERRIES(RESOURCE_SMURFBERRIES, 10, 1, 1, 750, "sweet berry", "", ResourceFlag.BERRY),
		PEACHES(RESOURCE_PEACHES, 10, 1, 1, 700, "peachy", "", ResourceFlag.WOODY),
		PLUMS(RESOURCE_PLUMS, 10, 1, 1, 710, "sweey plumy", "", ResourceFlag.WOODY),
		ONIONS(RESOURCE_ONIONS, 10, 1, 1, 760, "stinging oniony", "", null),
		CHERRIES(RESOURCE_CHERRIES, 10, 1, 1, 810, "cherry", "", ResourceFlag.WOODY),
		GARLIC(RESOURCE_GARLIC, 10, 1, 1, 815, "", "", null),
		PINEAPPLES(RESOURCE_PINEAPPLES, 10, 1, 1, 500, "fruity", "", ResourceFlag.WOODY),
		COCONUTS(RESOURCE_COCONUTS, 10, 1, 2, 250, "", "", ResourceFlag.WOODY),
		BANANAS(RESOURCE_BANANAS, 10, 1, 1, 790, "pungent banana", "", ResourceFlag.WOODY),
		LIMES(RESOURCE_LIMES, 10, 1, 1, 690, "citrusy", "", ResourceFlag.WOODY),
		SAP(RESOURCE_SAP, 10, 1, 1, 1600, "strong maply", "", ResourceFlag.WOODY),
		ONYX(RESOURCE_ONYX, 70, 1, 8, 3300, "", "", null),
		TURQUOISE(RESOURCE_TURQUOISE, 70, 1, 8, 3300, "", "", null),
		PERIDOT(RESOURCE_PERIDOT, 65, 1, 6, 3300, "", "", null),
		QUARTZ(RESOURCE_QUARTZ, 25, 1, 5, 3300, "", "", null),
		LAPIS(RESOURCE_LAPIS, 70, 1, 6, 3300, "", "", null),
		BLOODSTONE(RESOURCE_BLOODSTONE, 85, 1, 8, 3300, "", "", null),
		MOONSTONE(RESOURCE_MOONSTONE, 90, 1, 8, 3300, "", "", null),
		ALEXANDRITE(RESOURCE_ALEXANDRITE, 95, 1, 9, 3300, "", "", null),
		TEAK(RESOURCE_TEAK, 20, 2, 3, 1000, "", "", null),
		CEDAR(RESOURCE_CEDAR, 15, 2, 3, 900, "strong cedar", "", null),
		ELM(RESOURCE_ELM, 15, 2, 3, 1100, "", "", null),
		CHERRYWOOD(RESOURCE_CHERRYWOOD, 17, 2, 3, 900, "", "", null),
		BEECHWOOD(RESOURCE_BEECHWOOD, 12, 2, 3, 975, "", "", null),
		WILLOW(RESOURCE_WILLOW, 12, 2, 1, 1000, "", "", null),
		SYCAMORE(RESOURCE_SYCAMORE, 11, 2, 2, 1000, "", "", null),
		SPRUCE(RESOURCE_SPRUCE, 12, 2, 3, 990, "", "", null),
		MESQUITE(RESOURCE_MESQUITE, 9, 2, 3, 1150, "rich mesquite", "", null),
		BASALT(RESOURCE_BASALT, 10, 2, 4, 3300, "", "", null),
		SHALE(RESOURCE_SHALE, 5, 2, 2, 1200, "", "", null),
		PUMICE(RESOURCE_PUMICE, 5, 2, 4, 600, "", "", null),
		SANDSTONE(RESOURCE_SANDSTONE, 10, 2, 2, 3500, "", "", null),
		SOAPSTONE(RESOURCE_SOAPSTONE, 60, 2, 5, 3600, "", "", null),
		SALMON(RESOURCE_SALMON, 6, 18, 1, 1000, "strong fishy", "", ResourceFlag.FISH),
		CARP(RESOURCE_CARP, 6, 1, 10, 1000, "strong fishy", "", ResourceFlag.FISH),
		TROUT(RESOURCE_TROUT, 6, 10, 1, 1000, "strong fishy", "", ResourceFlag.FISH),
		SHRIMP(RESOURCE_SHRIMP, 6, 15, 1, 1000, "mild fishy", "", ResourceFlag.FISH),
		TUNA(RESOURCE_TUNA, 6, 10, 1, 1000, "strong fishy", "", ResourceFlag.FISH),
		CATFISH(RESOURCE_CATFISH, 6, 10, 1, 1000, "strong fishy", "", ResourceFlag.FISH),
		BAMBOO(RESOURCE_BAMBOO, 15, 10, 4, 120, "", "", null),
		SOAP(RESOURCE_SOAP, 1, 0, 1, 430, "light fragrant", "", null),
		SPIDERSTEEL(RESOURCE_SPIDERSTEEL, 150, 0, 2, 630, "", "", null),
		ASH(RESOURCE_ASH, 1, 0, 0, 50, "dusty", "", null),
		PERFUME(RESOURCE_PERFUME, 1, 1, 1, 1000, "strong fragrant", "", null),
		ATLANTITE(RESOURCE_ATLANTITE, 200, 1, 6, 850, "", "", null),
		CHEESE(RESOURCE_CHEESE, 25, 0, 1, 640, "mild cheesy", "", null),
		BEANS(RESOURCE_BEANS, 1, 15, 1, 750, "", "", null),
		CRANBERRIES(RESOURCE_CRANBERRIES, 10, 1, 1, 750, "sweet berry", "", ResourceFlag.BERRY),
		DRAGONBLOOD(RESOURCE_DRAGONBLOOD, 10, 100, 0, 1025, "mild salty", "Prop_UseSpellCast2(Prayer_MinorInfusion)", null),
		DRAGONMEAT(RESOURCE_DRAGONMEAT, 40, 20, 1, 3000, "mild salty", "", null),
		RUBY(RESOURCE_RUBY, 300, 1, 3, 3500, "", "", null),
		EMERALD(RESOURCE_EMERALD, 200, 1, 3, 3500, "", "", null),
		SAPPHIRE(RESOURCE_SAPPHIRE, 180, 1, 3, 3500, "", "", null),
		AGATE(RESOURCE_AGATE, 100, 1, 3, 3500, "", "", null),
		CITRINE(RESOURCE_CITRINE, 100, 1, 3, 3500, "", "", null),
		REED(RESOURCE_REED, 1, 50, 3, 830, "", "", null),
		ALABASTER(RESOURCE_ALABASTER, 5, 10, 4, 1550, "", "", null),
		CHROMIUM(RESOURCE_CHROMIUM, 80, 1, 6, 1550, "", "", null),
		DRAGONSCALES(RESOURCE_DRAGONSCALES, 50, 1, 6, 800, "", "", null),
		PLASMA(RESOURCE_PLASMA, 100, 0, 0, 0, "", "", null),
		ELECTRICITY(RESOURCE_ELECTRICITY, 100, 0, 0, 0, "", "", null),
		DEUTERIUM(RESOURCE_DEUTERIUM, 100, 0, 0, 0, "", "", null),
		SLIME(RESOURCE_SLIME, 5, 10, 1, 750, "horridly acidic", "Prop_UseSpellCast2(Spell_AcidSpray)", null),
		AIR(RESOURCE_AIR, 0, 100, 0, 0, "", "", null),
		OXYGEN(RESOURCE_OXYGEN, 10, 100, 0, 0, "", "", null),
		HYDROGEN(RESOURCE_HYDROGEN,       20, 100,  0, 0, "", "", null),
		FLOURINE(RESOURCE_FLOURINE,       60, 40,   0, 0, "strong pungent smell", "", null),
		NITROGEN(RESOURCE_NITROGEN,       10, 100,  0, 0, "", "", null),
		CHLORINE(RESOURCE_CHLORINE,       10, 100,  0, 0, "strong acidic smell", "", null),
		TITANIUM(RESOURCE_TITANIUM		, 75,   0,  8, 784, "", "", null),
		FIBREGLASS(RESOURCE_FIBREGLASS	, 10,   0,  2, 0, "", "", null),
		ULTRANIUM(RESOURCE_ULTRANIUM	, 175,  0, 11, 900, "", "", null),
		DURANIUM(RESOURCE_DURANIUM		, 159,  0, 10, 300, "", "", null),
		URANIUM(RESOURCE_URANIUM		, 100,  1,  0, 2000, "", "", null),
		ANTIMATTER(RESOURCE_ANTIMATTER	, 500,  0,  0, 0, "", "", null),
		DIAMELS(RESOURCE_DIAMELS		, 5,    20, 9, 3510, "", "", null),
		PETROLEUM(RESOURCE_PETROLEUM	, 50,   5,  1, 880, "strong oily", "", null),
		GASOLINE(RESOURCE_GASOLINE		, 80,   0,  0, 880, "strong unique", "", null),
		DIESEL(RESOURCE_DIESEL			, 70,   0,  0, 880, "strong unique", "", null),
		SILICON(RESOURCE_SILICON		, 10,   10, 6, 500, "", "", null),
		POLYESTER(RESOURCE_POLYESTER	, 15,   0,  2, 545, "", "", null),
		DENIM(RESOURCE_DENIM			, 15,   0,  2, 745, "", "", null),
		DILITHIUM(RESOURCE_DILITHIUM	, 5000, 0,  7, 5010, "", "", null),
		TAR(RESOURCE_TAR				, 10,   15, 2, 1280, "strong oily", "", null),
		SALT(RESOURCE_SALT				, 10,   20, 5, 750, "", "", null),
		SPICE(RESOURCE_SPICE			, 100,  5,  1, 750, "spicy smell", "", null),
		DIRT(RESOURCE_DIRT				, 1,   50,  1, 1600, "rich earthy smell", "", null),
		FUNGUS(RESOURCE_FUNGUS			, 1,    3,  1,  750, "", "", null),
		
		;//code, 						  v, freq, h, b, smell, effects, ResourceFlag flags
		public final int			code, value, frequency, hardness, bouancy;
		public final String			smell, effect, desc;
		public final ResourceFlag	flag;

		/**
		 * Constructs a default resource enum object
		 * @param cod the full resource code
		 * @param val the value in base currency
		 * @param freq the frequency 1-10
		 * @param hard the hardness 1-10
		 * @param bou the bouyancy 0-10000
		 * @param sniff the smell you see when sniffing it
		 * @param eff effects list (semicolon delimited, with parenthesis)
		 * @param flg the ResourceFlag flags, such as berry or fish or something
		 */
		private DefResource(int cod, int val, int freq, int hard, int bou, String sniff, String eff, ResourceFlag flg)
		{
			code = cod;
			value = val;
			frequency = freq;
			hardness = hard;
			bouancy = bou;
			smell = sniff;
			effect = eff;
			flag = flg;
			desc = toString().replace('_', ' ');
		}
	}

	/**
	 * Global resource/raw material code data collector
	 * 
	 * @author Bo Zimmermanimmerman
	 */
	public class CODES
	{
		/**
		 * Constructs a CODES object for the current thread.
		 */
		public CODES()
		{
			super();
			final char c = Thread.currentThread().getThreadGroup().getName().charAt(0);
			if (insts == null)
				insts = new CODES[256];
			if (insts[c] == null)
				insts[c] = this;
			synchronized (this)
			{
				final String[][] addExtra = CMProps.instance().getStrsStarting("ADDMATERIAL_");
				final String[][] repExtra = CMProps.instance().getStrsStarting("REPLACEMATERIAL_");

				final DefResource[] defaults = Arrays.copyOf(DefResource.values(), DefResource.values().length);
				Arrays.sort(defaults, new Comparator<DefResource>()
				{
					@Override
					public int compare(DefResource o1, DefResource o2)
					{
						final int o1c = o1.code & 255;
						final int o2c = o2.code & 255;
						return o1c > o2c ? 1 : (o1c < o2c ? -1 : 0);
					}

				});
				for (final DefResource d : defaults)
				{
					final int material = d.code & MATERIAL_MASK;
					add(material, d.desc, d.smell, d.value, d.frequency, d.hardness, d.bouancy, d.flag == ResourceFlag.FISH, d.flag == ResourceFlag.BERRY, d.flag == ResourceFlag.WOODY, d.effect);
				}
				for (int i = 0; i < addExtra.length + repExtra.length; i++)
				{
					final String[] array = (i >= addExtra.length) ? repExtra[i - addExtra.length] : addExtra[i];
					final boolean replace = i >= addExtra.length;
					final String stat = array[0].toUpperCase().trim();
					final String p = array[1];
					final List<String> V = CMParms.parseCommas(p, false);
					if (V.size() != 8)
					{
						Log.errOut("RawMaterial", "Bad coffeemud.ini material row (requires 8 elements, separated by ,): " + p);
						continue;
					}
					String type = "ADD";
					int oldResourceCode = -1;
					if (replace)
					{
						final DefResource r = (DefResource) CMath.s_valueOf(DefResource.class, stat.toUpperCase().trim().replace(' ', '_'));
						if (r != null)
						{
							oldResourceCode = r.code;
							type = "REPLACE";
						}
						else
						{
							Log.errOut("RawMaterial", "Unknown replaceable resource in coffeemud.ini: " + stat);
							continue;
						}
					}
					final String matStr = V.get(0).toUpperCase();
					final String smell = V.get(1).toUpperCase();
					final int value = CMath.s_int(V.get(2));
					final int frequ = CMath.s_int(V.get(3));
					final int hardness = CMath.s_int(V.get(4));
					final int bouancy = CMath.s_int(V.get(5));
					final boolean fish = V.get(6).equalsIgnoreCase("fish");
					final boolean berry = V.get(6).equalsIgnoreCase("berry");
					final boolean woody = V.get(6).equalsIgnoreCase("woody");
					final String abilityID = V.get(7);
					final Material material = Material.findIgnoreCase(matStr);
					if (material == null)
					{
						Log.errOut("RawMaterial", "Unknown material code in coffeemud.ini: " + matStr);
						continue;
					}
					if (type.equalsIgnoreCase("ADD"))
						add(material.mask(), stat, smell, value, frequ, hardness, bouancy, fish, berry, woody, abilityID);
					else if (type.equalsIgnoreCase("REPLACE") && (oldResourceCode >= 0))
						replace(oldResourceCode, material.mask(), stat, smell, value, frequ, hardness, bouancy, fish, berry, woody, abilityID);
				}
				final String[] sortedNames = descs.clone();
				Arrays.sort(sortedNames);
				final Hashtable<String, Integer> previousIndexes = new Hashtable<String, Integer>();
				for (int ndex = 0; ndex < descs.length; ndex++)
					previousIndexes.put(descs[ndex], Integer.valueOf(ndex));
				allCodesSortedByName = new int[allCodes.length];
				for (int ndex = 0; ndex < sortedNames.length; ndex++)
				{
					final int previousIndex = previousIndexes.get(sortedNames[ndex]).intValue();
					allCodesSortedByName[ndex] = allCodes[previousIndex];
				}
			}
		}

		/**
		 * Returns the CODES object for the current thread group, or null
		 * @return the CODES object for the current thread group, or null
		 */
		private static CODES c()
		{
			return insts[Thread.currentThread().getThreadGroup().getName().charAt(0)];
		}

		/**
		 * Returns the CODES object for the given thread group code, or null
		 * @param c the thread group code
		 * @return the CODES object for the given thread group code, or null
		 */
		public static CODES c(byte c)
		{
			return insts[c];
		}

		/**
		 * Returns the CODES object for the current thread group, or creates one.
		 * @return the CODES object for the current thread group, or creates one.
		 */
		public static CODES instance()
		{
			CODES c = insts[Thread.currentThread().getThreadGroup().getName().charAt(0)];
			if (c == null)
				c = new CODES();
			return c;
		}

		/**
		 * Destroys and recreates the CODES object for the current thread group
		 */
		public static void reset()
		{
			insts[Thread.currentThread().getThreadGroup().getName().charAt(0)] = null;
			instance();
		}

		private static CODES[]				insts					= new CODES[256];

		private int[]						allCodes				= new int[0];
		private int[]						allCodesSortedByName	= new int[0];
		private int[]						berries					= new int[0];
		private int[]						woodies					= new int[0];
		private int[]						fishes					= new int[0];
		private int[][]						data					= new int[0][0];
		private String[]					smells					= new String[0];
		private String[]					descs					= new String[0];
		private String[]					effects					= new String[0];
		private Ability[][]					effectAs				= new Ability[0][];
		private PairList<Integer, Double>[]	buckets					= null;

		/**
		 * Returns an array of the numeric codes for the berry resources
		 * 
		 * @return an array of the numeric codes for the berry resources
		 */
		public static int[] BERRIES()
		{
			return c().berries;
		}

		/**
		 * Returns an array of the numeric codes for the berry resources
		 * 
		 * @return an array of the numeric codes for the berry resources
		 */
		public int[] berries()
		{
			return berries;
		}

		/**
		 * Returns an array of the numeric codes for the wood-choppy resources
		 * 
		 * @return an array of the numeric codes for the wood-choppy resources
		 */
		public static int[] WOODIES()
		{
			return c().woodies;
		}

		/**
		 * Returns an array of the numeric codes for the wood-choppy resources
		 * 
		 * @return an array of the numeric codes for the wood-choppy resources
		 */
		public int[] woodies()
		{
			return woodies;
		}

		/**
		 * Returns an array of the numeric codes for the fishy resources
		 * 
		 * @return an array of the numeric codes for the fishy resources
		 */
		public static int[] FISHES()
		{
			return c().fishes;
		}

		/**
		 * Returns an array of the numeric codes for the fishy resources
		 * 
		 * @return an array of the numeric codes for the fishy resources
		 */
		public int[] fishes()
		{
			return fishes;
		}

		/**
		 * Returns total number of codes 0 - this-1
		 * 
		 * @return total number of codes 0 - this-1
		 */
		public static int TOTAL()
		{
			return c().descs.length;
		}

		/**
		 * Returns total number of codes 0 - this-1
		 * 
		 * @return total number of codes 0 - this-1
		 */
		public int total()
		{
			return descs.length;
		}

		/**
		 * Returns an array of the numeric codes for all resources
		 * 
		 * @return an array of the numeric codes for all resources
		 */
		public static int[] ALL_SBN()
		{
			return c().allCodesSortedByName;
		}

		/**
		 * Returns an array of the numeric codes for all resources
		 * 
		 * @return an array of the numeric codes for all resources
		 */
		public static int[] ALL()
		{
			return c().allCodes;
		}

		/**
		 * Returns an array of the numeric codes for all resources
		 * 
		 * @return an array of the numeric codes for all resources
		 */
		public int[] all()
		{
			return allCodes;
		}

		/**
		 * Returns an the numeric codes of the indexes resource code
		 * 
		 * @param x the indexed resource code
		 * @return an the numeric codes of the indexes resource code
		 */
		public static int GET(int x)
		{
			return c().allCodes[x & RESOURCE_MASK];
		}

		/**
		 * Returns an the numeric codes of the indexes resource code
		 * 
		 * @param x the indexed resource code
		 * @return an the numeric codes of the indexes resource code
		 */
		public int get(int x)
		{
			return allCodes[x & RESOURCE_MASK];
		}

		/**
		 * Returns the code of the names resource, or -1
		 * 
		 * @param rsc the names resource
		 * @return the code of the names resource, or -1
		 */
		public static int FIND_CaseSensitive(String rsc)
		{
			if (rsc == null)
				return -1;
			final CODES C = c();
			final int x = CMParms.indexOf(C.descs, rsc);
			if (x >= 0)
				return C.allCodes[x];
			return -1;
		}

		/**
		 * Returns the code of the names resource, or -1
		 * 
		 * @param rsc the names resource
		 * @return the code of the names resource, or -1
		 */
		public static int FIND_IgnoreCase(String rsc)
		{
			if (rsc == null)
				return -1;
			final CODES C = c();
			final int x = CMParms.indexOfIgnoreCase(C.descs, rsc);
			if (x >= 0)
				return C.allCodes[x];
			return -1;
		}

		/**
		 * Returns the code of the names resource, or -1
		 * 
		 * @param rsc the names resource
		 * @return the code of the names resource, or -1
		 */
		public static int FIND_StartsWith(String rsc)
		{
			if (rsc == null)
				return -1;
			final CODES C = c();
			final int x = CMParms.startsWith(C.descs, rsc.toUpperCase().trim());
			if (x >= 0)
				return C.allCodes[x];
			return -1;
		}

		/**
		 * Returns whether the code is valid
		 * 
		 * @param code the resource code
		 * @return whether the code is valid
		 */
		public static boolean IS_VALID(int code)
		{
			return (code >= 0) && ((code & RawMaterial.RESOURCE_MASK) < c().total());
		}

		/**
		 * Returns the names of the various resources
		 * 
		 * @return the names of the various resources
		 */
		public static String[] NAMES()
		{
			return c().descs;
		}

		/**
		 * Returns the names of the various resources
		 * 
		 * @return the names of the various resources
		 */
		public String[] names()
		{
			return descs;
		}

		/**
		 * Returns the name of the code
		 * 
		 * @param code the code
		 * @return the name of the code
		 */
		public static String NAME(int code)
		{
			return c().descs[code & RESOURCE_MASK];
		}

		/**
		 * Returns the name of the code
		 * 
		 * @param code the code
		 * @return the name of the code
		 */
		public String name(int code)
		{
			return descs[code & RESOURCE_MASK];
		}

		/**
		 * Returns the smells of the various resources
		 * 
		 * @return the smells of the various resources
		 */
		public static String[] SMELLS()
		{
			return c().smells;
		}

		/**
		 * Returns the description of the code smell
		 * 
		 * @param code the code smell
		 * @return the description of the code smell
		 */
		public static String SMELL(int code)
		{
			return c().smells[code & RESOURCE_MASK];
		}

		/**
		 * Returns the description of the code smell
		 * 
		 * @param code the code smell
		 * @return the description of the code smell
		 */
		public String smell(int code)
		{
			return smells[code & RESOURCE_MASK];
		}

		/**
		 * Returns the smells of the various resources
		 * 
		 * @return the smells of the various resources
		 */
		public static String[] EFFECTS()
		{
			return c().effects;
		}

		/**
		 * Returns the description of the code smell
		 * 
		 * @param code the code smell
		 * @return the description of the code smell
		 */
		public static String EFFECT(int code)
		{
			return c().effects[code & RESOURCE_MASK];
		}

		/**
		 * Returns the description of the code smell
		 * 
		 * @param code the code smell
		 * @return the description of the code smell
		 */
		public String effect(int code)
		{
			return effects[code & RESOURCE_MASK];
		}

		/**
		 * Returns the value of the resource
		 * 
		 * @param code the resource code
		 * @return the value of the resource
		 */
		public static int VALUE(int code)
		{
			return c().data[code & RESOURCE_MASK][1];
		}

		/**
		 * Returns the value of the resource
		 * 
		 * @param code the resource code
		 * @return the value of the resource
		 */
		public int value(int code)
		{
			return data[code & RESOURCE_MASK][1];
		}

		/**
		 * Returns the frequency of the resource, or how rare it is.
		 * 
		 * @param code the resource code
		 * @return the frequency of the resource
		 */
		public static int FREQUENCY(int code)
		{
			return c().data[code & RESOURCE_MASK][2];
		}

		/**
		 * Returns the frequency of the resource, or how rare it is.
		 * 
		 * @param code the resource code
		 * @return the frequency of the resource
		 */
		public int frequency(int code)
		{
			return data[code & RESOURCE_MASK][2];
		}

		/**
		 * Returns the resource code of the most frequently found
		 * resource of the given material mask
		 * @param material the material mask
		 * @return the most common resource of that material
		 */
		public static int MOST_FREQUENT(int material)
		{
			return c().mostFrequent(material);
		}
		
		/**
		 * Returns the resource code of the most frequently found
		 * resource of the given material mask
		 * @param material the material mask
		 * @return the most common resource of that material
		 */
		public int mostFrequent(int material)
		{
			final List<Integer> all = COMPOSE_RESOURCES(material);
			if((all==null)||(all.size()==0))
				return -1;
			Collections.sort(all,new Comparator<Integer>()
			{
				@Override
				public int compare(Integer o1, Integer o2)
				{
					int freq1 = frequency(o1.intValue());
					int freq2 = frequency(o2.intValue());
					if(freq1 < freq2)
						return 1;
					if(freq1 > freq2)
						return -1;
					return 0;
				}
			});
			return all.get(0).intValue();
		}
		
		/**
		 * Returns the hardness of the resource, from 1-10
		 * 
		 * @param code the resource code
		 * @return the hardness of the resource
		 */
		public static int HARDNESS(int code)
		{
			return c().data[code & RESOURCE_MASK][3];
		}

		/**
		 * Returns the hardness of the resource, from 1-10
		 * 
		 * @param code the resource code
		 * @return the hardness of the resource
		 */
		public int hardness(int code)
		{
			return data[code & RESOURCE_MASK][3];
		}

		/**
		 * Returns the bouancy of the resource, from 0-20000
		 * 
		 * @param code the resource code
		 * @return the bouancy of the resource
		 */
		public static int BOUANCY(int code)
		{
			return c().data[code & RESOURCE_MASK][4];
		}

		/**
		 * Returns the bouancy of the resource, from 0-20000
		 * 
		 * @param code the resource code
		 * @return the bouancy of the resource
		 */
		public int bouancy(int code)
		{
			return data[code & RESOURCE_MASK][4];
		}

		/**
		 * Search and compose a complete list of all resources of the given
		 * material
		 * 
		 * @param mat the resource code
		 * @return a complete list of all resources of the given material
		 */
		public static List<Integer> COMPOSE_RESOURCES(int mat)
		{
			if (mat <= RESOURCE_MASK)
				mat = mat << 8;
			final List<Integer> rscs = new Vector<Integer>();
			for (final int rsc : c().allCodes)
			{
				if ((rsc & MATERIAL_MASK) == mat)
					rscs.add(Integer.valueOf(rsc));
			}
			return rscs;
		}

		/**
		 * Parses, if necessary, EFFECT strings into ability objects, complete
		 * with parms, ready for copying.
		 * 
		 * @param code the material/resource code
		 * @return an ability, if any.
		 */
		public static Ability[] EFFECTA(int code)
		{
			final CODES c = c();
			final int cd = code & RESOURCE_MASK;
			Ability[] As = c.effectAs[cd];
			if (As != null)
				return As;
			synchronized (c.effectAs)
			{
				As = c.effectAs[cd];
				if (As != null)
					return As;
				final List<String> effectsV = CMParms.parseSafeSemicolonList(c.effect(code), true);
				if (effectsV.size() == 0)
					c.effectAs[cd] = new Ability[0];
				else
				{
					String abilityID;
					String parms;
					final Vector<Ability> listA = new Vector<Ability>();
					for (final Iterator<String> e = effectsV.iterator(); e.hasNext();)
					{
						abilityID = e.next();
						parms = "";
						if ((abilityID == null) || (abilityID.length() == 0))
							continue;
						if (abilityID.charAt(abilityID.length() - 1) == ')')
						{
							final int x = abilityID.indexOf('(');
							if (x > 0)
							{
								parms = abilityID.substring(x + 1, abilityID.length() - 1);
								abilityID = abilityID.substring(0, x);
							}
						}
						final Ability A = CMClass.getAbility(abilityID);
						if (A == null)
							Log.errOut("RawMaterial", "Unknown ability " + abilityID + " in " + c.effect(code));
						else
						{
							A.setMiscText(parms);
							listA.add(A);
						}
					}
					c.effectAs[cd] = listA.toArray(new Ability[0]);
				}
			}
			return c.effectAs[cd];
		}

		/**
		 * For the given material, returns a value-sorted list of all resources
		 * that are made of that material and their values. Performs caching.
		 * @param material the material type to get resources of
		 * @return the value-sorted list of resources and values
		 */
		@SuppressWarnings("unchecked")
		public PairList<Integer, Double> getValueSortedBucket(int material)
		{
			material = (material & RawMaterial.MATERIAL_MASK) >> 8;
			final int numMaterials = Material.values().length;
			if ((material < 0) || (material >= numMaterials))
				return null;
			if (buckets == null)
			{
				final PairList<Integer, Double>[] newBuckets = new PairList[numMaterials];
				for (int matIndex = 0; matIndex < numMaterials; matIndex++)
				{
					final int matCode = Material.values()[matIndex].mask();
					final TreeSet<Pair<Integer, Double>> newBucket = new TreeSet<Pair<Integer, Double>>(new Comparator<Pair<Integer, Double>>()
					{
						@Override
						public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2)
						{
							return o1.second.compareTo(o2.second);
						}
					});
					for (int i = 0; i < total(); i++)
					{
						final int resourceCode = get(i);
						if ((resourceCode & RawMaterial.MATERIAL_MASK) == matCode)
						{
							final int resourceValue = value(resourceCode);
							newBucket.add(new Pair<Integer, Double>(Integer.valueOf(resourceCode), Double.valueOf(resourceValue)));
						}
					}
					final PairSVector<Integer, Double> finalBucket = new PairSVector<Integer, Double>();
					final double pieceSize = 1.0 / newBucket.size();
					double currValue = 0.0;
					for (final Pair<Integer, Double> p : newBucket)
					{
						finalBucket.add(p.first, Double.valueOf(currValue));
						currValue += pieceSize;
					}
					newBuckets[matIndex] = finalBucket;
				}
				buckets = newBuckets;
			}
			return buckets[material];
		}

		/**
		 * Adds a new resource to the official CODES object. The resource code
		 * will end up being determined by the order in which resources are added.
		 * So be careful!
		 * @param material the material type of the new resource
		 * @param name the name of the resource
		 * @param smell the smell of the resource
		 * @param value the value of the resource in base currency
		 * @param frequ the frequency of the resource 0-10
		 * @param hardness the hardness of the resource 0-10
		 * @param bouancy the bouancy of the resource 0-10000
		 * @param fish true if its a fishy-type
		 * @param berry true if its a berry type
		 * @param woody true if its a wood-choppy type
		 * @param abilityID effects list
		 */
		public synchronized void add(int material, String name, String smell, int value, int frequ, int hardness, int bouancy, boolean fish, boolean berry, boolean woody, String abilityID)
		{
			final int newResourceCode = allCodes.length | material;
			allCodes = Arrays.copyOf(allCodes, allCodes.length + 1);
			allCodes[allCodes.length - 1] = newResourceCode;
			if (berry)
			{
				berries = Arrays.copyOf(berries, berries.length + 1);
				berries[berries.length - 1] = newResourceCode;
			}
			if (woody || (material == RawMaterial.MATERIAL_WOODEN))
			{
				woodies = Arrays.copyOf(woodies, woodies.length + 1);
				woodies[woodies.length - 1] = newResourceCode;
			}
			if (fish)
			{
				fishes = Arrays.copyOf(fishes, fishes.length + 1);
				fishes[fishes.length - 1] = newResourceCode;
			}
			descs = Arrays.copyOf(descs, descs.length + 1);
			descs[descs.length - 1] = name;

			smells = Arrays.copyOf(smells, smells.length + 1);
			smells[smells.length - 1] = smell;

			effects = Arrays.copyOf(effects, effects.length + 1);
			effects[effects.length - 1] = abilityID;
			effectAs = Arrays.copyOf(effectAs, effectAs.length + 1);
			effectAs[effectAs.length - 1] = null;

			data = Arrays.copyOf(data, data.length + 1);
			// full code, base value, frequency, hardness (1-10), bouancy
			final int[] newRow = { newResourceCode, value, frequ, hardness, bouancy };
			data[data.length - 1] = newRow;
		}

		/**
		 * Replaces a resource of the given code with new data
		 * @param resourceCode the old resource code
		 * @param material the material type of the new resource
		 * @param name the name of the resource
		 * @param smell the smell of the resource
		 * @param value the value of the resource in base currency
		 * @param frequ the frequency of the resource 0-10
		 * @param hardness the hardness of the resource 0-10
		 * @param bouancy the bouancy of the resource 0-10000
		 * @param fish true if its a fishy-type
		 * @param berry true if its a berry type
		 * @param woody true if its a wood-choppy type
		 * @param abilityID effects list
		 */
		public synchronized void replace(int resourceCode, int material, String name, String smell, int value, int frequ, int hardness, int bouancy, boolean fish, boolean berry, boolean woody, String abilityID)
		{
			final int resourceIndex = resourceCode & RESOURCE_MASK;
			if ((berry) && (!CMParms.contains(berries, resourceCode)))
			{
				berries = Arrays.copyOf(berries, berries.length + 1);
				berries[berries.length - 1] = resourceCode;
			}
			else if ((!berry) && (CMParms.contains(berries, resourceCode)))
			{
				final int[] newberries = new int[berries.length - 1];
				int n = 0;
				for (final int berrie : berries)
				{
					if (berrie != resourceCode)
						newberries[n++] = berrie;
				}
				berries = newberries;
			}
			if ((woody || (material==RawMaterial.MATERIAL_WOODEN)) && (!CMParms.contains(woodies, resourceCode)))
			{
				woodies = Arrays.copyOf(woodies, woodies.length + 1);
				woodies[woodies.length - 1] = resourceCode;
			}
			else if ((!(woody || (material==RawMaterial.MATERIAL_WOODEN))) && (CMParms.contains(woodies, resourceCode)))
			{
				final int[] newwoodies = new int[woodies.length - 1];
				int n = 0;
				for (final int woodie : woodies)
				{
					if (woodie != resourceCode)
						newwoodies[n++] = woodie;
				}
				woodies = newwoodies;
			}
			if ((fish) && (!CMParms.contains(fishes, resourceCode)))
			{
				fishes = Arrays.copyOf(fishes, fishes.length + 1);
				fishes[fishes.length - 1] = resourceCode;
			}
			else if ((!fish) && (CMParms.contains(fishes, resourceCode)))
			{
				final int[] newfishes = new int[fishes.length - 1];
				int n = 0;
				for (final int fishe : fishes)
				{
					if (fishe != resourceCode)
						newfishes[n++] = fishe;
				}
				fishes = newfishes;
			}
			smells[resourceIndex] = smell;
			effects[resourceIndex] = abilityID;
			effectAs[resourceIndex] = null;
			descs[resourceIndex] = name;
			final int[] newRow = { resourceCode, value, frequ, hardness, bouancy };
			data[resourceIndex] = newRow;
		}
	}
}
