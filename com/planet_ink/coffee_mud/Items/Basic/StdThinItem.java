package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.EachApplicable.ApplyAffectPhyStats;
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

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class StdThinItem implements Item
{
	@Override
	public String ID()
	{
		return "StdThinItem";
	}

	protected String					name		= "an ordinary item";
	protected String					displayText	= L("a nondescript item sits here doing nothing.");
	protected volatile Container		myContainer	= null;
	protected volatile ItemPossessor	owner		= null;
	protected Rideable					riding		= null;
	protected volatile boolean			isSavable	= false;
	protected volatile boolean			amDestroyed	= false;
	protected PhyStats					basePhyStats= (PhyStats) CMClass.getCommon("DefaultPhyStats");

	@Override
	public void setRiding(final Rideable ride)
	{
		this.riding=ride;
	}

	@Override
	public Rideable riding()
	{
		return riding;
	}

	@Override
	public String displayText(final MOB viewerMob)
	{
		return displayText;
	}

	@Override
	public String name(final MOB viewerMob)
	{
		return name;
	}

	@Override
	public String description(final MOB viewerMob)
	{
		return "";
	}

	@Override
	public String Name()
	{
		return name;
	}

	@Override
	public void setName(final String newName)
	{
		name=newName;
	}

	@Override
	public String displayText()
	{
		return displayText;
	}
	@Override

	public void setDisplayText(final String newDisplayText)
	{
		displayText=newDisplayText;
	}

	@Override
	public String description()
	{
		return "";
	}

	@Override
	public void setDescription(final String newDescription)
	{
	}

	@Override
	public String image()
	{
		return "";
	}

	@Override
	public String rawImage()
	{
		return "";
	}

	@Override
	public void setImage(final String newImage)
	{
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
	}

	@Override
	public String text()
	{
		return "";
	}

	@Override
	public String miscTextFormat()
	{
		return "";
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		return ((E instanceof StdThinItem)&&(E.Name().equals(Name()))&&(E.displayText().equals(displayText())));
	}

	@Override
	public long expirationDate()
	{
		return 0;
	}

	@Override
	public void setExpirationDate(final long dateTime)
	{
	}

	@Override
	public int maxRange()
	{
		return 0;
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().L(str, xs);
	}

	@Override
	public String name()
	{
		return Name();
	}

	@Override
	public int getTickStatus()
	{
		return 0;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return false;
	}

	@Override
	public CMObject newInstance()
	{
		return new StdThinItem();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject)this.clone();
		}
		catch (final CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	@Override
	public void initializeClass()
	{
	}


	@Override
	public int compareTo(final CMObject o)
	{
		return this.hashCode() == o.hashCode() ? 0 : (this.hashCode() > o.hashCode()) ? 1 : -1;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return true;
	}

	@Override
	public void destroy()
	{
		amDestroyed=true;
	}

	@Override
	public boolean isSavable()
	{
		return isSavable;
	}

	@Override
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
		isSavable=truefalse;
	}

	@Override
	public String[] getStatCodes()
	{
		return new String[0];
	}

	@Override
	public int getSaveStatIndex()
	{
		return 0;
	}

	@Override
	public String getStat(final String code)
	{
		return "";
	}

	@Override
	public boolean isStat(final String code)
	{
		return false;
	}

	@Override
	public void setStat(final String code, final String val)
	{
	}

	@Override
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}

	@Override
	public void setBasePhyStats(final PhyStats newStats)
	{
		newStats.copyInto(basePhyStats);
	}

	@Override
	public PhyStats phyStats()
	{
		return basePhyStats;
	}

	@Override
	public void recoverPhyStats()
	{
	}

	@Override
	public void addEffect(final Ability to)
	{
	}

	@Override
	public void addNonUninvokableEffect(final Ability to)
	{
	}

	@Override
	public void delEffect(final Ability to)
	{
	}

	@Override
	public int numEffects()
	{
		return 0;
	}

	@Override
	public Ability fetchEffect(final int index)
	{
		return null;
	}

	@Override
	public Ability fetchEffect(final String ID)
	{
		return null;
	}

	@Override
	public Enumeration<Ability> effects()
	{
		return new EmptyEnumeration<Ability>();
	}

	@Override
	public void delAllEffects(final boolean unInvoke)
	{
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
	}

	@Override
	public void addBehavior(final Behavior to)
	{
	}

	@Override
	public void delBehavior(final Behavior to)
	{
	}

	@Override
	public int numBehaviors()
	{
		return 0;
	}

	@Override
	public Behavior fetchBehavior(final int index)
	{
		return null;
	}

	@Override
	public Behavior fetchBehavior(final String ID)
	{
		return null;
	}

	@Override
	public Enumeration<Behavior> behaviors()
	{
		return new EmptyEnumeration<Behavior>();
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
	}

	@Override
	public void addScript(final ScriptingEngine s)
	{
	}

	@Override
	public void delAllBehaviors()
	{
	}

	@Override
	public void delScript(final ScriptingEngine s)
	{
	}

	@Override
	public void delAllScripts()
	{
	}

	@Override
	public int numScripts()
	{
		return 0;
	}

	@Override
	public ScriptingEngine fetchScript(final int x)
	{
		return null;
	}

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return new EmptyEnumeration<ScriptingEngine>();
	}

	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
	}

	@Override
	public String databaseID()
	{
		return "";
	}

	@Override
	public void setDatabaseID(final String ID)
	{
	}

	@Override
	public boolean canSaveDatabaseID()
	{
		return false;
	}

	@Override
	public boolean amWearingAt(final long wornCode)
	{
		return false;
	}

	@Override
	public boolean fitsOn(final long wornCode)
	{
		return false;
	}

	@Override
	public long whereCantWear(final MOB mob)
	{
		return 0;
	}

	@Override
	public boolean canWear(final MOB mob, final long wornCode)
	{
		return false;
	}

	@Override
	public boolean wearIfPossible(final MOB mob)
	{
		return false;
	}

	@Override
	public boolean wearIfPossible(final MOB mob, final long wearCode)
	{
		return false;
	}

	@Override
	public void wearEvenIfImpossible(final MOB mob)
	{
	}

	@Override
	public void wearAt(final long wornCode)
	{
	}

	@Override
	public boolean amBeingWornProperly()
	{
		return false;
	}

	@Override
	public void unWear()
	{
	}

	@Override
	public long rawWornCode()
	{
		return 0;
	}

	@Override
	public void setRawWornCode(final long newValue)
	{
	}

	@Override
	public long rawProperLocationBitmap()
	{
		return 0;
	}

	@Override
	public void setRawProperLocationBitmap(final long newValue)
	{
	}

	@Override
	public boolean rawLogicalAnd()
	{
		return false;
	}

	@Override
	public void setRawLogicalAnd(final boolean newAnd)
	{
	}

	@Override
	public boolean compareProperLocations(final Item toThis)
	{
		return false;
	}

	@Override
	public String readableText()
	{
		return "";
	}

	@Override
	public boolean isReadable()
	{
		return false;
	}

	@Override
	public void setReadable(final boolean isTrue)
	{
	}

	@Override
	public void setReadableText(final String text)
	{
	}

	@Override
	public Container container()
	{
		return myContainer;
	}

	@Override
	public void setContainer(final Container newLocation)
	{
		myContainer = newLocation;
	}

	@Override
	public Item ultimateContainer(final Physical stopAtC)
	{
		return myContainer;
	}

	@Override
	public void removeFromOwnerContainer()
	{
		myContainer=null;
	}

	@Override
	public int numberOfItems()
	{
		return 1;
	}

	@Override
	public String secretIdentity()
	{
		return "";
	}

	@Override
	public String rawSecretIdentity()
	{
		return "";
	}

	@Override
	public void setSecretIdentity(final String newIdentity)
	{
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return false;
	}

	@Override
	public int usesRemaining()
	{
		return 100;
	}

	@Override
	public void setUsesRemaining(final int newUses)
	{
	}

	@Override
	public void stopTicking()
	{
	}

	@Override
	public int value()
	{
		return 0;
	}

	@Override
	public int baseGoldValue()
	{
		return 0;
	}

	@Override
	public void setBaseValue(final int newValue)
	{
	}

	@Override
	public int material()
	{
		return 0;
	}

	@Override
	public void setMaterial(final int newValue)
	{
	}

	@Override
	public int recursiveWeight()
	{
		return 0;
	}

	@Override
	public ItemPossessor owner()
	{
		return owner;
	}

	@Override
	public void setOwner(final ItemPossessor E)
	{
		owner=E;
	}
}
