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
import com.planet_ink.coffee_mud.Libraries.Socials;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary.DeadResourceRecord;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2006-2020 Bo Zimmerman

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
public interface MaterialLibrary extends CMLibrary
{
	public Environmental unbundle(Item I, int number, Container C);
	public Environmental splitBundle(Item I, int size, Container C);
	public int getMaterialRelativeInt(String s);
	public int getMaterialCode(String s, boolean exact);
	public int getResourceCode(String s, boolean exact);
	public String getResourceDesc(int MASK);
	public String getMaterialDesc(int MASK);
	public Item makeItemResource(int type);
	public Item makeItemResource(int type, String subType);
	public PhysicalAgent makeResource(int myResource, String localeCode, boolean noAnimals, String fullName, String subType);
	public void addEffectsToResource(Item I);
	public int getRandomResourceOfMaterial(int material);
	public boolean rebundle(Item I);
	public boolean quickDestroy(Item I);
	public DeadResourceRecord destroyResources(final Room R, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int finalOtherHash);
	public int destroyResourcesValue(MOB M, int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash);
	public int destroyResourcesValue(final Room R, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash);
	public int destroyResourcesValue(List<Item> V, int howMuch, int finalMaterial, int otherMaterial, Item never, Container C);
	public int destroyResourcesAmt(MOB E, int howMuch, int finalMaterial, String subType, Container C);
	public int destroyResourcesAmt(Room E, int howMuch, int finalMaterial, String subType, Container C);
	public int destroyResourcesAmt(List<Item> V, int howMuch, int finalMaterial, String subType, Container C);
	public RawMaterial fetchFoundOtherEncoded(Room E, String otherRequired);
	public RawMaterial fetchFoundOtherEncoded(MOB E, String otherRequired);
	public RawMaterial findMostOfMaterial(Room E, int material);
	public RawMaterial findMostOfMaterial(MOB E, int material);
	public int findNumberOfResource(Room E, RawMaterial resource);
	public int findNumberOfResource(MOB E, RawMaterial resource);
	public RawMaterial findMostOfMaterial(Room E, String other);
	public RawMaterial findMostOfMaterial(MOB E, String other);
	public RawMaterial findFirstResource(Room E, int resource);
	public RawMaterial findFirstResource(MOB E, int resource);
	public RawMaterial findFirstResource(Room E, String other);
	public RawMaterial findFirstResource(MOB E, String other);
	public void adjustResourceName(Item I);
	public String makeResourceSimpleName(final int rscCode, String subType);
	public String makeResourceDescriptiveName(final int rscCode, String subType, final boolean plural);
	public String genericType(Item I);
	public boolean isResourceCodeRoomMapped(final int resourceCode);
	public List<Item> getAllFarmables(final int materialType);

	/**
	 * Returns the number of ticks that the given item, whatever
	 * will burn, or 0 if it won't burn.
	 * @param E the item to check
	 * @return the number of ticks to burn, or 0
	 */
	public int getBurnDuration(Environmental E);

	/**
	 * A record detailing information about
	 * destoryed resources, used mostly for common
	 * skills that consume them.
	 * @author Bo Zimmerman
	 */
	public static class DeadResourceRecord
	{
		public int lostValue=0;
		public int lostAmt=0;
		public int resCode=-1;
		public String subType="";
		public List<CMObject> lostProps = null;
	}
}
