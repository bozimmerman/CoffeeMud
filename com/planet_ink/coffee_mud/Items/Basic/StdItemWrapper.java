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
   Copyright 2020-2025 Bo Zimmerman

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
public class StdItemWrapper implements Item, CMObjectWrapper
{
	@Override
	public String ID()
	{
		return "StdItemWrapper";
	}

	protected volatile Item				item 		= null;
	protected volatile Container		myContainer	= null;
	protected volatile ItemPossessor	owner		= null;
	protected volatile boolean			isSavable	= false;
	protected Rideable					riding		= null;
	protected volatile boolean			amDestroyed	= false;
	protected PhyStats					basePhyStats= (PhyStats) CMClass.getCommon("DefaultPhyStats");

	@Override
	public void setWrappedObject(final CMObject obj)
	{
		if(obj instanceof Item)
		{
			this.item=(Item)obj;
			this.item.phyStats().copyInto(basePhyStats);
		}
	}

	@Override
	public CMObject getWrappedObject()
	{
		return this.item;
	}

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
		return (item == null) ? "" : item.displayText(viewerMob);
	}

	@Override
	public String name(final MOB viewerMob)
	{
		return (item == null) ? "" :item.name(viewerMob);
	}

	@Override
	public String description(final MOB viewerMob)
	{
		return (item == null) ? "" :item.description(viewerMob);
	}

	@Override
	public String Name()
	{
		return (item == null) ? "" :item.name();
	}

	@Override
	public void setName(final String newName)
	{
	}

	@Override
	public String displayText()
	{
		return (item == null) ? "" :item.displayText();
	}

	@Override

	public void setDisplayText(final String newDisplayText)
	{
	}

	@Override
	public String description()
	{
		return (item == null) ? "" :item.description();
	}

	@Override
	public void setDescription(final String newDescription)
	{
	}

	@Override
	public String genericName()
	{
		return (item == null) ? L("an item") :item.genericName();
	}

	@Override
	public String image()
	{
		return (item == null) ? "" :item.image();
	}

	@Override
	public String rawImage()
	{
		return (item == null) ? "" : item.rawImage();
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
		return (item == null) ? "" : item.miscTextFormat();
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		return (E instanceof StdItemWrapper)&&(item == ((StdItemWrapper)E).item);
	}

	@Override
	public long expirationDate()
	{
		return (item == null) ? 0 : item.expirationDate();
	}

	@Override
	public void setExpirationDate(final long dateTime)
	{
	}

	@Override
	public int maxRange()
	{
		return (item == null) ? 0 : item.maxRange();
	}

	@Override
	public int minRange()
	{
		return (item == null) ? 0 : item.minRange();
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().L(str, xs);
	}

	@Override
	public String name()
	{
		return (item == null) ? "" : item.name();
	}

	@Override
	public int getTickStatus()
	{
		return (item == null) ? 0 : item.getTickStatus();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return false;
	}

	@Override
	public CMObject newInstance()
	{
		return new StdItemWrapper();
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
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
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
		return (item == null) ? "" : item.databaseID();
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
		return (item == null) ? false : item.amWearingAt(wornCode);
	}

	@Override
	public boolean fitsOn(final long wornCode)
	{
		return (item == null) ? false : item.fitsOn(wornCode);
	}

	@Override
	public long whereCantWear(final MOB mob)
	{
		return (item == null) ? 0 : item.whereCantWear(mob);
	}

	@Override
	public boolean canWear(final MOB mob, final long wornCode)
	{
		return (item == null) ? false : item.canWear(mob, wornCode);
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
		return (item == null) ? 0 : item.rawWornCode();
	}

	@Override
	public void setRawWornCode(final long newValue)
	{
	}

	@Override
	public long rawProperLocationBitmap()
	{
		return (item == null) ? 0 : item.rawProperLocationBitmap();
	}

	@Override
	public void setRawProperLocationBitmap(final long newValue)
	{
	}

	@Override
	public boolean rawLogicalAnd()
	{
		return (item == null) ? false : item.rawLogicalAnd();
	}

	@Override
	public void setRawLogicalAnd(final boolean newAnd)
	{
	}

	@Override
	public boolean compareProperLocations(final Item toThis)
	{
		return (item == null) ? false : item.compareProperLocations(toThis);
	}

	@Override
	public String readableText()
	{
		return (item == null) ? "" : item.readableText();
	}

	@Override
	public boolean isReadable()
	{
		return (item == null) ? false : item.isReadable();
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
	public void addTag(final String tag)
	{
	}

	@Override
	public void delTag(final String tag)
	{
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> tags()
	{
		if (item != null)
			return item.tags();
		return EmptyEnumeration.INSTANCE;
	}

	@Override
	public boolean hasTag(final String tag)
	{
		if(item != null)
			return item.hasTag(tag);
		return false;
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
		return (item == null) ? 0 : item.numberOfItems();
	}

	@Override
	public String secretIdentity()
	{
		return (item == null) ? "" : item.secretIdentity();
	}

	@Override
	public String rawSecretIdentity()
	{
		return (item == null) ? "" : item.rawSecretIdentity();
	}

	@Override
	public void setSecretIdentity(final String newIdentity)
	{
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return (item == null) ? false : item.subjectToWearAndTear();
	}

	@Override
	public int usesRemaining()
	{
		return (item == null) ? 0 : item.usesRemaining();
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
		return (item == null) ? 0 : item.value();
	}

	@Override
	public int baseGoldValue()
	{
		return (item == null) ? 0 : item.baseGoldValue();
	}

	@Override
	public void setBaseValue(final int newValue)
	{
	}

	@Override
	public int material()
	{
		return (item == null) ? 0 : item.material();
	}

	@Override
	public void setMaterial(final int newValue)
	{
	}

	@Override
	public int recursiveWeight()
	{
		return (item == null) ? 0 : item.recursiveWeight();
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
