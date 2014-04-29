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
   Copyright 2000-2014 Bo Zimmerman

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
	public String ID(){ return "GenWallpaper";}
	protected String		name="some wallpaper";
	protected Object		description=null;
	protected String		readableText="";
	protected PhyStats  	phyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected boolean   	destroyed=false;
	protected ItemPossessor owner=null;
	//protected String databaseID="";

	public GenWallpaper()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.ITEM);//removed for mem & perf
	}
	public String _(final String str, final String ... xs) { return CMLib.lang().fullSessionTranslation(str, xs); }
	public boolean isGeneric(){return true;}
	public Rideable riding(){return null;}
	public void setRiding(Rideable one){}
	public String image(){return "";}
	public String rawImage(){return "";}
	public void setImage(String newImage){}
	public void initializeClass(){}
	
	public void setDatabaseID(String id){}//databaseID=id;}
	public String databaseID(){return "";}//databaseID;}
	public boolean canSaveDatabaseID(){ return false;}

	public String Name(){ return name;}
	public String name()
	{
		if(phyStats().newName()!=null) return phyStats().newName();
		return Name();
	}
	public void setName(String newName){name=newName;}
	public PhyStats phyStats()
	{return phyStats;}
	public PhyStats basePhyStats()
	{ return phyStats; }
	public void recoverPhyStats()
	{ phyStats().setSensesMask(phyStats().sensesMask()|PhyStats.SENSE_ITEMNOTGET);}
	public void setBasePhyStats(PhyStats newStats){}
	public boolean isAContainer(){return false;}
	public int numberOfItems(){return 1;}
	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.ITEM);}//removed for mem & perf
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new GenWallpaper();
	}
	public boolean subjectToWearAndTear(){return false;}
	public CMObject copyOf()
	{
		try
		{
			GenWallpaper E=(GenWallpaper)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.ITEM);//removed for mem & perf
			E.destroyed=false;
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public int recursiveWeight(){return phyStats().weight();}
	public ItemPossessor owner(){return owner;}
	public void setOwner(ItemPossessor E)
	{ owner=E;}
	public long expirationDate(){return 0;}
	public void setExpirationDate(long time){}

	public boolean amDestroyed()
	{return destroyed;}

	public boolean amWearingAt(long wornCode){if(wornCode==Wearable.IN_INVENTORY)return true; return false;}
	public boolean fitsOn(long wornCode){return false;}
	public boolean wearIfPossible(MOB mob){ return false;}
	public boolean wearIfPossible(MOB mob, long wearCode){ return false;}
	public void wearAt(long wornCode){}
	public long rawProperLocationBitmap(){return 0;}
	public boolean rawLogicalAnd(){return false;}
	public void setRawProperLocationBitmap(long newValue){}
	public void setRawLogicalAnd(boolean newAnd){}
	public boolean compareProperLocations(Item toThis){return true;}
	public long whereCantWear(MOB mob){ return 0;}
	public boolean canWear(MOB mob, long wornCode){ return false;}
	public long rawWornCode(){return 0;}
	public void setRawWornCode(long newValue){}
	public void unWear(){}
	public int capacity(){return 0;}
	public void setCapacity(int newValue){}
	public int material(){return RawMaterial.RESOURCE_PAPER;}
	public void setMaterial(int newValue){}
	public int baseGoldValue(){return 0;}
	public int value(){return 0;}
	public void setBaseValue(int newValue){}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public boolean isReadable(){ return CMLib.flags().isReadable(this);}
	public void setReadable(boolean truefalse){ CMLib.flags().setReadable(this, truefalse);}
	public void affectPhyStats(Physical affected, PhyStats affectableStats){}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void setMiscText(String newText)
	{
		List<XMLLibrary.XMLpiece> V=CMLib.xml().parseAllXML(newText);
		if(V!=null)
		{
			setName(CMLib.xml().getValFromPieces(V,"NAME"));
			setDescription(CMLib.xml().getValFromPieces(V,"DESC"));
			CMLib.coffeeMaker().setEnvFlags(this,CMath.s_int(CMLib.xml().getValFromPieces(V,"FLAG")));
			setReadableText(CMLib.xml().getValFromPieces(V,"READ"));
		}
	}
	public String text()
	{    StringBuffer text=new StringBuffer("");
		text.append(CMLib.xml().convertXMLtoTag("NAME",Name()));
		text.append(CMLib.xml().convertXMLtoTag("DESC",description()));
		text.append(CMLib.xml().convertXMLtoTag("FLAG",CMLib.coffeeMaker().envFlags(this)));
		text.append(CMLib.xml().convertXMLtoTag("READ",readableText()));
		return text.toString();
	}
	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	public int getTickStatus(){return Tickable.STATUS_NOT;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(destroyed) return false;
		return true;
	}
	public Container container(){return null;}
	public Item ultimateContainer(Physical stopAtC){return this;}
	public void wearEvenIfImpossible(MOB mob){}
	public String rawSecretIdentity(){return "";}
	public String secretIdentity(){return "";}
	public void setSecretIdentity(String newIdentity){}
	public String displayText(){return "";}
	public String displayText(MOB viewerMob) { return displayText(); }
	public String name(MOB viewerMob) { return name(); }
	public void setDisplayText(String newDisplayText){}
	public String description(MOB viewerMob) { return description(); }
	public String description()
	{
		if(description == null)
			return "You see nothing special about "+name()+".";
		else
		if(description instanceof byte[])
		{
			final byte[] descriptionBytes=(byte[])description;
			if(descriptionBytes.length==0)
				return "You see nothing special about "+name()+".";
			if(CMProps.getBoolVar(CMProps.Bool.ITEMDCOMPRESS))
				return CMLib.encoder().decompressString(descriptionBytes);
			else
				return CMStrings.bytesToStr(descriptionBytes);
		}
		else
			return (String)description;
	}
	
	public void setDescription(String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CMProps.getBoolVar(CMProps.Bool.ITEMDCOMPRESS))
			description=CMLib.encoder().compressString(newDescription);
		else
			description=newDescription;
	}
	
	public void setContainer(Container newContainer){}
	public int usesRemaining(){return Integer.MAX_VALUE;}
	public void setUsesRemaining(int newUses){}
	public boolean isSavable(){return CMLib.flags().isSavable(this);}
	public void setSavable(boolean truefalse){ CMLib.flags().setSavable(this, truefalse);}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return true;
		else
		if(msg.targetMinor()==CMMsg.NO_EFFECT)
			return true;
		else
		if(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
		{
			mob.tell("Please don't do that.");
			return false;
		}
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
		case CMMsg.TYP_EXAMINE:
		case CMMsg.TYP_READ:
		case CMMsg.TYP_SPEAK:
		case CMMsg.TYP_OK_ACTION:
		case CMMsg.TYP_OK_VISUAL:
		case CMMsg.TYP_NOISE:
			return true;
		case CMMsg.TYP_GET:
			if((msg.tool()==null)||(msg.tool() instanceof MOB))
			{
				mob.tell("You can't get "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_DROP:
			return true;
		default:
			break;
		}
		mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
		return false;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
		case CMMsg.TYP_EXAMINE:  CMLib.commands().handleBeingLookedAt(msg); break;
		case CMMsg.TYP_READ: CMLib.commands().handleBeingRead(msg); break;
		default:
			break;
		}
	}

	public void stopTicking()
	{
		destroyed=true; // WHY?!?!?
		CMLib.threads().deleteTick(this,-1);
	}
	public void destroy()
	{
		if(owner==null) return;
		CMLib.map().registerWorldObjectDestroyed(null,null,this);
		destroyed=true;
		removeFromOwnerContainer();
		owner=null;
	}

	public void removeFromOwnerContainer()
	{
		if(owner==null) return;
		if (owner instanceof Room)
		{
			Room thisRoom=(Room)owner;
			thisRoom.delItem(this);
		}
		else
		if (owner instanceof MOB)
		{
			MOB mob=(MOB)owner;
			mob.delItem(this);
		}
	}

	public void addNonUninvokableEffect(Ability to){}
	public void addEffect(Ability to){}
	public void delEffect(Ability to){}
	public void delAllEffects(boolean unInvoke){}
	public int numEffects(){return 0;}
	public void eachEffect(final EachApplicable<Ability> applier){}
	public Enumeration<Ability> effects(){return EmptyEnumeration.INSTANCE;}
	public Ability fetchEffect(int index){return null;}
	public Ability fetchEffect(String ID){return null;}
	public int maxRange(){return 0;}
	public int minRange(){return 0;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public void delAllBehaviors(){}
	public int numBehaviors(){return 0;}
	public Enumeration<Behavior> behaviors() { return EmptyEnumeration.INSTANCE;}
	public Behavior fetchBehavior(int index){return null;}
	public Behavior fetchBehavior(String ID){return null;}
	public void eachBehavior(final EachApplicable<Behavior> applier){}
	public void addScript(ScriptingEngine S){}
	public void delScript(ScriptingEngine S) {}
	public void delAllScripts(){}
	public int numScripts(){return 0;}
	public Enumeration<ScriptingEngine> scripts() { return EmptyEnumeration.INSTANCE;}
	public ScriptingEngine fetchScript(int x){ return null;}
	public void eachScript(final EachApplicable<ScriptingEngine> applier){}

	public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS","NAME","DESCRIPTION","ISREADABLE","READABLETEXT"};
	public String[] getStatCodes(){return CODES;}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code)
	{
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return name();
		case 2: return description();
		case 3: return ""+isReadable();
		case 4: return readableText();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setName(val); break;
		case 2: setDescription(val); break;
		case 3: CMLib.flags().setReadable(this,CMath.s_bool(val));
				break;
		case 4: setReadableText(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenWallpaper)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
