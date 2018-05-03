package com.planet_ink.coffee_mud.Items.Basic;

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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
 Copyright 2002-2018 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class GenWallpaper implements Item
{
	@Override
	public String ID()
	{
		return "GenWallpaper";
	}

	protected String		name			= "some wallpaper";
	protected Object		description		= null;
	protected String		readableText	= "";
	protected PhyStats		phyStats		= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected boolean		destroyed		= false;
	protected ItemPossessor	owner			= null;

	// protected String databaseID="";

	public GenWallpaper()
	{
		super();
		// CMClass.bumpCounter(this,CMClass.CMObjectType.ITEM);//removed for mem
		// & perf
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public Rideable riding()
	{
		return null;
	}

	@Override
	public void setRiding(Rideable one)
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
	public void setImage(String newImage)
	{
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public void setDatabaseID(String id)
	{
		// databaseID=id;
	}

	@Override
	public String databaseID()
	{
		return ""; // databaseID;
	}

	@Override
	public boolean canSaveDatabaseID()
	{
		return false;
	}

	@Override
	public String Name()
	{
		return name;
	}

	@Override
	public String name()
	{
		if (phyStats().newName() != null)
			return phyStats().newName();
		return Name();
	}

	@Override
	public void setName(String newName)
	{
		name = newName;
	}

	@Override
	public PhyStats phyStats()
	{
		return phyStats;
	}

	@Override
	public PhyStats basePhyStats()
	{
		return phyStats;
	}

	@Override
	public void recoverPhyStats()
	{
		phyStats().setSensesMask(phyStats().sensesMask() | PhyStats.SENSE_ITEMNOTGET);
	}

	@Override
	public void setBasePhyStats(PhyStats newStats)
	{
	}

	public boolean isAContainer()
	{
		return false;
	}

	@Override
	public int numberOfItems()
	{
		return 1;
	}

	// protected void
	// finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.ITEM);}//removed
	// for mem & perf
	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch (final Exception e)
		{
			Log.errOut(ID(), e);
		}
		return new GenWallpaper();
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return false;
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final GenWallpaper E = (GenWallpaper) this.clone();
			// CMClass.bumpCounter(E,CMClass.CMObjectType.ITEM);//removed for
			// mem & perf
			E.destroyed = false;
			return E;
		}
		catch (final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public int recursiveWeight()
	{
		return phyStats().weight();
	}

	@Override
	public ItemPossessor owner()
	{
		return owner;
	}

	@Override
	public void setOwner(ItemPossessor E)
	{
		owner = E;
	}

	@Override
	public long expirationDate()
	{
		return 0;
	}

	@Override
	public void setExpirationDate(long time)
	{
	}

	@Override
	public boolean amDestroyed()
	{
		return destroyed;
	}

	@Override
	public boolean amWearingAt(long wornCode)
	{
		if (wornCode == Wearable.IN_INVENTORY)
			return true;
		return false;
	}

	@Override
	public boolean fitsOn(long wornCode)
	{
		return false;
	}

	@Override
	public boolean wearIfPossible(MOB mob)
	{
		return false;
	}

	@Override
	public boolean wearIfPossible(MOB mob, long wearCode)
	{
		return false;
	}

	@Override
	public void wearAt(long wornCode)
	{
	}

	@Override
	public long rawProperLocationBitmap()
	{
		return 0;
	}

	@Override
	public boolean rawLogicalAnd()
	{
		return false;
	}

	@Override
	public void setRawProperLocationBitmap(long newValue)
	{
	}

	@Override
	public void setRawLogicalAnd(boolean newAnd)
	{
	}

	@Override
	public boolean compareProperLocations(Item toThis)
	{
		return true;
	}

	@Override
	public long whereCantWear(MOB mob)
	{
		return 0;
	}

	@Override
	public boolean canWear(MOB mob, long wornCode)
	{
		return false;
	}

	@Override
	public long rawWornCode()
	{
		return 0;
	}

	@Override
	public void setRawWornCode(long newValue)
	{
	}

	@Override
	public void unWear()
	{
	}

	public int capacity()
	{
		return 0;
	}

	public void setCapacity(int newValue)
	{
	}

	@Override
	public int material()
	{
		return RawMaterial.RESOURCE_PAPER;
	}

	@Override
	public void setMaterial(int newValue)
	{
	}

	@Override
	public int baseGoldValue()
	{
		return 0;
	}

	@Override
	public int value()
	{
		return 0;
	}

	@Override
	public void setBaseValue(int newValue)
	{
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(String text)
	{
		readableText = text;
	}

	@Override
	public boolean isReadable()
	{
		return CMLib.flags().isReadable(this);
	}

	@Override
	public void setReadable(boolean truefalse)
	{
		CMLib.flags().setReadable(this, truefalse);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void setMiscText(String newText)
	{
		final List<XMLLibrary.XMLTag> V = CMLib.xml().parseAllXML(newText);
		if (V != null)
		{
			setName(CMLib.xml().getValFromPieces(V, "NAME"));
			setDescription(CMLib.xml().getValFromPieces(V, "DESC"));
			CMLib.coffeeMaker().setEnvFlags(this, CMath.s_int(CMLib.xml().getValFromPieces(V, "FLAG")));
			setReadableText(CMLib.xml().getValFromPieces(V, "READ"));
		}
	}

	@Override
	public String text()
	{
		final StringBuffer text = new StringBuffer("");
		text.append(CMLib.xml().convertXMLtoTag("NAME", Name()));
		text.append(CMLib.xml().convertXMLtoTag("DESC", description()));
		text.append(CMLib.xml().convertXMLtoTag("FLAG", CMLib.coffeeMaker().envFlags(this)));
		text.append(CMLib.xml().convertXMLtoTag("READ", readableText()));
		return text.toString();
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if (destroyed)
			return false;
		return true;
	}

	@Override
	public Container container()
	{
		return null;
	}

	@Override
	public Item ultimateContainer(Physical stopAtC)
	{
		return this;
	}

	@Override
	public void wearEvenIfImpossible(MOB mob)
	{
	}

	@Override
	public String rawSecretIdentity()
	{
		return "";
	}

	@Override
	public String secretIdentity()
	{
		return "";
	}

	@Override
	public void setSecretIdentity(String newIdentity)
	{
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public String displayText(MOB viewerMob)
	{
		return displayText();
	}

	@Override
	public String name(MOB viewerMob)
	{
		return name();
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
	}

	@Override
	public String description(MOB viewerMob)
	{
		return description();
	}

	@Override
	public String description()
	{
		if (description == null)
			return "You see nothing special about " + name() + ".";
		else 
		if (description instanceof byte[])
		{
			final byte[] descriptionBytes = (byte[]) description;
			if (descriptionBytes.length == 0)
				return "You see nothing special about " + name() + ".";
			if (CMProps.getBoolVar(CMProps.Bool.ITEMDCOMPRESS))
				return CMLib.encoder().decompressString(descriptionBytes);
			else
				return CMStrings.bytesToStr(descriptionBytes);
		}
		else
			return (String) description;
	}

	@Override
	public void setDescription(String newDescription)
	{
		if (newDescription.length() == 0)
			description = null;
		else 
		if (CMProps.getBoolVar(CMProps.Bool.ITEMDCOMPRESS))
			description = CMLib.encoder().compressString(newDescription);
		else
			description = newDescription;
	}

	@Override
	public void setContainer(Container newContainer)
	{
	}

	@Override
	public int usesRemaining()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void setUsesRemaining(int newUses)
	{
	}

	@Override
	public boolean isSavable()
	{
		return CMLib.flags().isSavable(this);
	}

	@Override
	public void setSavable(boolean truefalse)
	{
		CMLib.flags().setSavable(this, truefalse);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob = msg.source();
		if (!msg.amITarget(this))
			return true;
		else 
		if (msg.targetMinor() == CMMsg.NO_EFFECT)
			return true;
		else 
		if (CMath.bset(msg.targetMajor(), CMMsg.MASK_MAGIC))
		{
			mob.tell(L("Please don't do that."));
			return false;
		}
		else
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
			case CMMsg.TYP_READ:
			case CMMsg.TYP_WASREAD:
			case CMMsg.TYP_SPEAK:
			case CMMsg.TYP_OK_ACTION:
			case CMMsg.TYP_OK_VISUAL:
			case CMMsg.TYP_NOISE:
				return true;
			case CMMsg.TYP_GET:
				if ((msg.tool() == null) || (msg.tool() instanceof MOB))
				{
					mob.tell(L("You can't get @x1.", name()));
					return false;
				}
				break;
			case CMMsg.TYP_DROP:
				return true;
			default:
				break;
			}
		}
		mob.tell(mob, this, null, L("You can't do that to <T-NAMESELF>."));
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if (msg.amITarget(this))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				CMLib.commands().handleBeingLookedAt(msg);
				break;
			case CMMsg.TYP_READ:
				CMLib.commands().handleBeingRead(msg);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void stopTicking()
	{
		destroyed = true; // WHY?!?!?
		CMLib.threads().deleteTick(this, -1);
	}

	@Override
	public void destroy()
	{
		if (owner == null)
			return;
		CMLib.map().registerWorldObjectDestroyed(null, null, this);
		destroyed = true;
		removeFromOwnerContainer();
		owner = null;
	}

	@Override
	public void removeFromOwnerContainer()
	{
		if (owner == null)
			return;
		if (owner instanceof Room)
		{
			final Room thisRoom = (Room) owner;
			thisRoom.delItem(this);
		}
		else 
		if (owner instanceof MOB)
		{
			final MOB mob = (MOB) owner;
			mob.delItem(this);
		}
	}

	@Override
	public void addNonUninvokableEffect(Ability to)
	{
	}

	@Override
	public void addEffect(Ability to)
	{
	}

	@Override
	public void delEffect(Ability to)
	{
	}

	@Override
	public void delAllEffects(boolean unInvoke)
	{
	}

	@Override
	public int numEffects()
	{
		return 0;
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
	}

	@Override
	public Enumeration<Ability> effects()
	{
		return EmptyEnumeration.INSTANCE;
	}

	@Override
	public Ability fetchEffect(int index)
	{
		return null;
	}

	@Override
	public Ability fetchEffect(String ID)
	{
		return null;
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
	public void addBehavior(Behavior to)
	{
	}

	@Override
	public void delBehavior(Behavior to)
	{
	}

	@Override
	public void delAllBehaviors()
	{
	}

	@Override
	public int numBehaviors()
	{
		return 0;
	}

	@Override
	public Enumeration<Behavior> behaviors()
	{
		return EmptyEnumeration.INSTANCE;
	}

	@Override
	public Behavior fetchBehavior(int index)
	{
		return null;
	}

	@Override
	public Behavior fetchBehavior(String ID)
	{
		return null;
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
	}

	@Override
	public void addScript(ScriptingEngine S)
	{
	}

	@Override
	public void delScript(ScriptingEngine S)
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
	public Enumeration<ScriptingEngine> scripts()
	{
		return EmptyEnumeration.INSTANCE;
	}

	@Override
	public ScriptingEngine fetchScript(int x)
	{
		return null;
	}

	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[]	CODES	= { "CLASS", "NAME", "DESCRIPTION", "ISREADABLE", "READABLETEXT" };

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		for (int i = 0; i < CODES.length; i++)
		{
			if (code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(String code)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return name();
		case 2:
			return description();
		case 3:
			return "" + isReadable();
		case 4:
			return readableText();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setName(val);
			break;
		case 2:
			setDescription(val);
			break;
		case 3:
			CMLib.flags().setReadable(this, CMath.s_bool(val));
			break;
		case 4:
			setReadableText(val);
			break;
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if (!(E instanceof GenWallpaper))
			return false;
		for (int i = 0; i < CODES.length; i++)
		{
			if (!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		}
		return true;
	}
}
