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
   Copyright 2001-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class StdItem implements Item
{
	@Override
	public String ID()
	{
		return "StdItem";
	}

	protected String	name				= "an ordinary item";
	protected String	displayText			= L("a nondescript item sits here doing nothing.");
	protected Object	description			= null;
	protected int		myUses				= Integer.MAX_VALUE;
	protected long		myWornCode			= Wearable.IN_INVENTORY;
	protected String	miscText			= "";
	protected Rideable	riding				= null;
	protected String	rawImageName		= null;
	protected String	cachedImageName		= null;
	protected String	secretIdentity		= null;
	protected boolean	wornLogicalAnd		= false;
	protected long		properWornBitmap	= Wearable.WORN_HELD;
	protected int		baseGoldValue		= 0;
	protected int		material			= RawMaterial.RESOURCE_COTTON;
	protected String[]	xtraValues			= null;
	protected long		dispossessionTime	= 0;
	protected int		tickStatus			= Tickable.STATUS_NOT;
	protected String	databaseID			= "";
	protected boolean	destroyed			= false;
	protected Item		me					= this;

	protected PhyStats					phyStats		= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected PhyStats					basePhyStats	= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected volatile Container		myContainer		= null;
	protected volatile ItemPossessor	owner			= null;
	protected SVector<Ability>			affects			= null;
	protected SVector<Behavior>			behaviors		= null;
	protected SVector<ScriptingEngine>	scripts			= null;

	protected ApplyAffectPhyStats		affectPhyStats	= new ApplyAffectPhyStats(this);

	public StdItem()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.ITEM);//removed for mem & perf
		if(basePhyStats == null)
			basePhyStats = (PhyStats) CMClass.getCommon("DefaultPhyStats");
		if(phyStats == null)
			phyStats = (PhyStats) CMClass.getCommon("DefaultPhyStats");
		basePhyStats().setWeight(1);
		basePhyStats().setArmor(0);
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
	
	protected boolean abilityImbuesMagic()
	{
		return true;
	}

	// protected void finalize() {
	// CMClass.unbumpCounter(this,CMClass.CMObjectType.ITEM); }//removed for mem
	// & perf
	@Override
	public void initializeClass()
	{
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public String Name()
	{
		return name;
	}

	@Override
	public void setName(String newName)
	{
		name = newName;
	}

	@Override
	public String name()
	{
		if(phyStats().newName()!=null) 
			return phyStats().newName();
		return Name();
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
	public void setDatabaseID(String id)
	{
		databaseID = id;
	}

	@Override
	public boolean canSaveDatabaseID()
	{
		return true;
	}

	@Override
	public String databaseID()
	{
		return databaseID;
	}

	@Override
	public String image()
	{
		if(cachedImageName==null)
		{
			if((rawImageName!=null)&&(rawImageName.length()>0))
				cachedImageName=rawImageName;
			else
				cachedImageName=CMLib.protocol().getDefaultMXPImage(this);
		}
		return cachedImageName;
	}
	
	@Override
	public String rawImage()
	{
		if(rawImageName==null)
			return "";
		return rawImageName;
	}
	
	@Override
	public void setImage(String newImage)
	{
		if((newImage==null)||(newImage.trim().isEmpty()))
			rawImageName=null;
		else
			rawImageName=newImage;
		if((cachedImageName!=null)&&(!cachedImageName.equals(newImage)))
			cachedImageName=null;
	}

	@Override
	public PhyStats phyStats()
	{
		return phyStats;
	}

	@Override
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}

	@Override
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		eachEffect(affectPhyStats);
		if(((phyStats().ability()>0)&&abilityImbuesMagic())||(this instanceof MiscMagic))
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_BONUS);
		if((owner()!=null)
		&&(owner() instanceof MOB)
		&&(CMLib.flags().isHidden(this)))
			phyStats().setDisposition((int)(phyStats().disposition()&(PhyStats.ALLMASK-PhyStats.IS_HIDDEN)));
	}

 	@Override
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdItem();
	}
	
	@Override
	public boolean subjectToWearAndTear()
	{
		return false;
	}
	
	protected void cloneFix(Item I)
	{
		destroyed=false;
		me=this;
		basePhyStats=(PhyStats)I.basePhyStats().copyOf();
		phyStats=(PhyStats)I.phyStats().copyOf();
		
		affectPhyStats = new ApplyAffectPhyStats(this);

		affects=null;
		behaviors=null;
		scripts=null;
		for(final Enumeration<Behavior> e=I.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if(B!=null)
				addBehavior((Behavior)B.copyOf());
		}
		for(final Enumeration<ScriptingEngine> e=I.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			if(SE!=null)
				addScript((ScriptingEngine)SE.copyOf());
		}

		Ability A;
		for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
		{
			A=a.nextElement();
			if((A!=null)&&(!A.ID().equals("ItemRejuv")))
			{
				A=(Ability)A.copyOf();
				addEffect(A);
				if(A.canBeUninvoked())
				{
					A.unInvoke();
					delEffect(A);
				}
			}
		}
	}
	
	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdItem E=(StdItem)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.ITEM);//removed for mem & perf
			E.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			E.cloneFix(this);
			CMLib.catalog().newInstance(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override 
	public Rideable riding()
	{
		return riding;
	}
	
	@Override
	public void setRiding(Rideable ride)
	{
		if((ride!=null)&&(riding()!=null)&&(riding()==ride)&&(riding().amRiding(this)))
			return;
		if((riding()!=null)&&(riding().amRiding(this)))
			riding().delRider(this);
		riding=ride;
		if((riding()!=null)&&(!riding().amRiding(this)))
			riding().addRider(this);
	}

	@Override 
	public ItemPossessor owner()
	{
		return owner;
	}
	
	@Override
	public void setOwner(ItemPossessor E)
	{
		owner=E;
		if((E!=null)&&(!(E instanceof Room)))
			setExpirationDate(0);
		recoverPhyStats();
	}

	@Override
	public long expirationDate()
	{
		return dispossessionTime;
	}

	@Override
	public void setExpirationDate(long time)
	{
		dispossessionTime=time;
	}

	@Override
	public boolean amDestroyed()
	{
		return destroyed;
	}

	@Override
	public boolean amWearingAt(final long wornCode)
	{
		if(wornCode == 0)
			return myWornCode == 0;
		return (myWornCode & wornCode)==wornCode;
	}

	@Override
	public boolean fitsOn(long wornCode)
	{
		if(wornCode<=0)
			return true;
		return ((properWornBitmap & wornCode)==wornCode);
	}

	@Override
	public void wearEvenIfImpossible(MOB mob)
	{
		for(final long code : Wearable.CODES.ALL_ORDERED())
		{
			if(fitsOn(code))
			{
				wearAt(code);
				break;
			}
		}
	}

	@Override
	public boolean wearIfPossible(MOB mob, long wearCode)
	{
		if(wearCode<=0)
			return false;

		if((fitsOn(wearCode))
		&&(canWear(mob,wearCode)))
		{
			wearAt(wearCode);
			return true;
		}
		return false;
	}

	@Override
	public boolean wearIfPossible(MOB mob)
	{
		for(final long code : Wearable.CODES.ALL_ORDERED())
		{
			if((code>0) && wearIfPossible(mob,code))
				return true;
		}
		return false;
	}

	@Override
	public void wearAt(long wornCode)
	{
		if(wornCode==Wearable.IN_INVENTORY)
		{
			unWear();
			return;
		}
		if(wornLogicalAnd)
			setRawWornCode(properWornBitmap);
		else
			setRawWornCode(wornCode);
		recoverPhyStats();
	}

	@Override
	public long rawProperLocationBitmap()
	{
		return properWornBitmap;
	}

	@Override
	public boolean rawLogicalAnd()
	{
		return wornLogicalAnd;
	}

	@Override
	public void setRawProperLocationBitmap(long newValue)
	{
		properWornBitmap=newValue;
	}

	@Override
	public void setRawLogicalAnd(boolean newAnd)
	{
		wornLogicalAnd=newAnd;
	}

	@Override
	public boolean compareProperLocations(Item toThis)
	{
		if(toThis.rawLogicalAnd()!=wornLogicalAnd)
			return false;
		if((toThis.rawProperLocationBitmap()|Wearable.WORN_HELD)==(properWornBitmap|Wearable.WORN_HELD))
			return true;
		return false;
	}

	@Override
	public long whereCantWear(MOB mob)
	{
		long couldHaveBeenWornAt=-1;
		if(properWornBitmap==0)
			return couldHaveBeenWornAt;
		short layer=0;
		short layerAtt=0;
		if(this instanceof Armor)
		{
			layer=((Armor)this).getClothingLayer();
			layerAtt=((Armor)this).getLayerAttributes();
		}

		final Wearable.CODES codes = Wearable.CODES.instance();
		if(!wornLogicalAnd)
		{
			for(final long wornCode : codes.all())
			{
				if(wornCode != Wearable.IN_INVENTORY)
				{
					if(fitsOn(wornCode))
					{
						couldHaveBeenWornAt=wornCode;
						if(mob.freeWearPositions(wornCode,layer,layerAtt)>0)
							return 0;
					}
				}
			}
			return couldHaveBeenWornAt;
		}
		for(final long wornCode : codes.all())
		{
			if(wornCode != Wearable.IN_INVENTORY)
			{
				if((fitsOn(wornCode))
				&&(mob.freeWearPositions(wornCode,layer,layerAtt)==0))
					return wornCode;
			}
		}
		return 0;
	}

	@Override
	public boolean canWear(MOB mob, long where)
	{
		if(where==0)
			return (whereCantWear(mob)==0);
		if((rawProperLocationBitmap()&where)!=where)
			return false;
		return mob.freeWearPositions(where,(short)0,(short)0)>0;
	}

	@Override
	public long rawWornCode()
	{
		return myWornCode;
	}

	@Override
	public void setRawWornCode(long newValue)
	{
		myWornCode=newValue;
	}

	@Override
	public void unWear()
	{
		setRawWornCode(Wearable.IN_INVENTORY);
		recoverPhyStats();
	}

	@Override
	public int material()
	{
		return material;
	}

	@Override
	public void setMaterial(int newValue)
	{
		material=newValue;
	}

	@Override
	public int value()
	{
		return baseGoldValue()+(10*phyStats().ability());
	}

	@Override
	public int baseGoldValue()
	{
		return baseGoldValue;
	}

	@Override
	public void setBaseValue(int newValue)
	{
		baseGoldValue=newValue;
	}

	@Override
	public String readableText()
	{
		return miscText;
	}

	@Override
	public void setReadableText(String text)
	{
		miscText = text;
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
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(affected instanceof Room)
		{
			if((CMLib.flags().isLightSource(this))&&(CMLib.flags().isInDark(affected)))
				affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_DARK);
		}
		else
		{
			if(CMLib.flags().isLightSource(this))
			{
				if(rawWornCode()!=Wearable.IN_INVENTORY)
					affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_LIGHTSOURCE);
				if(CMLib.flags().isInDark(affected))
					affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_DARK);
			}
			if((amWearingAt(Wearable.WORN_MOUTH))&&(affected instanceof MOB))
			{
				if(!(this instanceof Light))
					affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_SPEAK);
				if((this instanceof Armor)&&(!CMath.bset(((Armor)this).getClothingLayer(),Armor.LAYERMASK_SEETHROUGH)))
					affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_TASTE);
			}
			final Item ultiCon=ultimateContainer(null);
			if(((ultiCon==null)||(!ultiCon.amWearingAt(Wearable.WORN_FLOATING_NEARBY)))
			&&((!(affected instanceof MOB))||(((MOB)affected).riding()!=this)))
				affectableStats.setWeight(affectableStats.weight()+phyStats().weight());
		}
		final List<Ability> affects=this.affects;
		if((affects!=null)&&(!affects.isEmpty()))
		{
			try
			{
				for(int a=0;a<affects.size();a++)
				{
					final Ability A=affects.get(a);
					if((A!=null)&&(A.bubbleAffect()))
						A.affectPhyStats(affected,affectableStats);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		final List<Ability> affects=this.affects;
		if((affects!=null)&&(!affects.isEmpty()))
		{
			try
			{
				for(int a=0;a<affects.size();a++)
				{
					final Ability A=affects.get(a);
					if((A!=null)&&(A.bubbleAffect()))
						A.affectCharStats(affectedMob,affectableStats);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		final List<Ability> affects=this.affects;
		if((affects!=null)&&(!affects.isEmpty()))
		{
			try
			{
				for(int a=0;a<affects.size();a++)
				{
					final Ability A=affects.get(a);
					if((A!=null)&&(A.bubbleAffect()))
						A.affectCharState(affectedMob,affectableMaxState);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText=newText;
	}

	@Override
	public String text()
	{
		return miscText;
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(destroyed)
			return false;
		tickStatus=Tickable.STATUS_START;
		switch(tickID)
		{
		case Tickable.TICKID_ITEM_BEHAVIOR:
		{
			tickStatus=Tickable.STATUS_BEHAVIOR;
			if(numBehaviors()>0)
			{
				eachBehavior(new EachApplicable<Behavior>()
				{ 
					@Override
					public final void apply(final Behavior B)
					{
						B.tick(ticking,tickID);
					} 
				});
			}
			tickStatus=Tickable.STATUS_SCRIPT;
			if(numScripts()>0)
			{
				eachScript(new EachApplicable<ScriptingEngine>()
				{ 
					@Override
					public final void apply(final ScriptingEngine S)
					{
						S.tick(ticking,tickID);
					} 
				});
			}
			if((numBehaviors()==0)&&(numScripts()==0))
				return false;
			break;
		}
		case Tickable.TICKID_CLANITEM:
		case Tickable.TICKID_ELECTRONICS:
		case Tickable.TICKID_SPECIALCOMBAT:
		case Tickable.TICKID_SPECIALMANEUVER:
			break;
		default:
		{
			tickStatus=Tickable.STATUS_AFFECT;
			if(numEffects()>0)
			{
				eachEffect(new EachApplicable<Ability>()
				{ 
					@Override
					public final void apply(final Ability A)
					{
						if(!A.tick(ticking,tickID))
							A.unInvoke();
					} 
				});
			}
			break;
		}
		}
		tickStatus=Tickable.STATUS_NOT;
		return !amDestroyed();
	}

	@Override
	public Item ultimateContainer(Physical stopAtC)
	{
		final Container C=container();
		if(C==null)
			return this;
		else
		if(C==stopAtC)
			return C;
		else
		if(C==this)
			return null;
		else
			return C.ultimateContainer(stopAtC);
	}

	@Override
	public Container container()
	{
		return myContainer;
	}

	@Override
	public String rawSecretIdentity()
	{
		return ((secretIdentity == null) ? "" : secretIdentity);
	}

	@Override
	public String secretIdentity()
	{
		if((secretIdentity!=null)&&(secretIdentity.length()>0))
			return secretIdentity+"\n\rLevel: "+phyStats().level()+tackOns();
		return description()+"\n\rLevel: "+phyStats().level()+tackOns();
	}

	@Override
	public void setSecretIdentity(String newIdentity)
	{
		if((newIdentity==null)
		||(newIdentity.trim().equalsIgnoreCase(description()))
		||(newIdentity.isEmpty()))
			secretIdentity=null;
		else
			secretIdentity=newIdentity;
	}

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}

	@Override
	public String description()
	{
		if(description == null)
			return "";
		else
		if(description instanceof byte[])
		{
			final byte[] descriptionBytes=(byte[])description;
			if(descriptionBytes.length==0)
				return "";
			if(CMProps.getBoolVar(CMProps.Bool.ITEMDCOMPRESS))
				return CMLib.encoder().decompressString(descriptionBytes);
			else
				return CMStrings.bytesToStr(descriptionBytes);
		}
		else
			return (String)description;
	}
	
	@Override 
	public String description(MOB viewerMob) 
	{ 
		return description(); 
	}

	@Override
	public void setDescription(String newDescription)
	{
		if(newDescription.isEmpty())
			description=null;
		else
		if(CMProps.getBoolVar(CMProps.Bool.ITEMDCOMPRESS))
			description=CMLib.encoder().compressString(newDescription);
		else
			description=newDescription;
	}

	@Override
	public void setContainer(Container newContainer)
	{
		if((newContainer!=this)
		&&((newContainer==null)||(newContainer.container()!=this)))
			myContainer=newContainer;
	}

	@Override
	public int numberOfItems()
	{
		return 1;
	}

	@Override
	public int usesRemaining()
	{
		return myUses;
	}

	@Override
	public void setUsesRemaining(int newUses)
	{
		myUses=newUses;
	}

	@Override
	public boolean isSavable()
	{
		if(!CMLib.flags().isSavable(this))
			return false;
		if(container()!=null)
			return container().isSavable();
		return true;
	}

	@Override 
	public void setSavable(boolean truefalse)
	{ 
		CMLib.flags().setSavable(this, truefalse);
	}

	protected boolean canWearComplete(MOB mob, long wearWhere)
	{
		if(!canWear(mob,wearWhere))
		{
			long cantWearAt=whereCantWear(mob);
			if(wearWhere!=0)
				cantWearAt = cantWearAt & wearWhere;
			Item alreadyWearing=(cantWearAt==0)?null:mob.fetchFirstWornItem(cantWearAt);
			final Wearable.CODES codes = Wearable.CODES.instance();
			if(alreadyWearing!=null)
			{
				if((cantWearAt!=Wearable.WORN_HELD)&&(cantWearAt!=Wearable.WORN_WIELD))
				{
					if(!CMLib.commands().postRemove(mob,alreadyWearing,false))
					{
						mob.tell(L("You are already wearing @x1 on your @x2.",alreadyWearing.name(),codes.name(cantWearAt)));
						return false;
					}
					alreadyWearing=mob.fetchFirstWornItem(cantWearAt);
					if((alreadyWearing!=null)&&(!canWear(mob,0)))
					{
						mob.tell(L("You are already wearing @x1 on your @x2.",alreadyWearing.name(),codes.name(cantWearAt)));
						return false;
					}
				}
				else
				{
					final short layer=(this instanceof Armor)?((Armor)this).getClothingLayer():0;
					final short layer2=(alreadyWearing instanceof Armor)?((Armor)alreadyWearing).getClothingLayer():0;
					if((rawProperLocationBitmap() == alreadyWearing.rawProperLocationBitmap())
					&&(rawLogicalAnd())
					&&(alreadyWearing.rawLogicalAnd())
					&&(layer == layer2)
					&&(CMLib.commands().postRemove(mob,alreadyWearing,false)))
						return true;
					if(cantWearAt==Wearable.WORN_HELD)
						mob.tell(L("You are already holding @x1.",alreadyWearing.name()));
					else
					if(cantWearAt==Wearable.WORN_WIELD)
						mob.tell(L("You are already wielding @x1.",alreadyWearing.name()));
					else
						mob.tell(L("You are already wearing @x1 on your @x2.",alreadyWearing.name(),codes.name(cantWearAt)));
					return false;
				}
			}
			else
			if(wearWhere!=0)
			{
				final StringBuilder locs=new StringBuilder("");
				for(int i=0;i<codes.total();i++)
				{
					if((codes.get(i)&wearWhere)>0)
						locs.append(", " + codes.name(i));
				}
				if(locs.length()==0)
					mob.tell(L("You can't wear that there."));
				else
					mob.tell(L("You can't wear that on your @x1.",locs.toString().substring(1).trim()));
				return false;
			}
			else
			{
				mob.tell(L("You don't have anywhere you can wear that."));
				return false;
			}
		}
		return true;
	}

	protected boolean alreadyWornMsg(MOB mob, Item thisItem)
	{
		if(!thisItem.amWearingAt(Wearable.IN_INVENTORY))
		{
			if(thisItem.amWearingAt(Wearable.WORN_WIELD))
				mob.tell(L("@x1 is already being wielded.",thisItem.name()));
			else
			if(thisItem.amWearingAt(Wearable.WORN_HELD))
				mob.tell(L("@x1 is already being held.",thisItem.name()));
			else
			if(thisItem.amWearingAt(Wearable.WORN_FLOATING_NEARBY))
				mob.tell(L("@x1 is floating nearby.",thisItem.name()));
			else
				mob.tell(L("@x1is already being worn.",thisItem.name()));
			return false;
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		// the order that these things are checked in should
		// be holy, and etched in stone.
		int num=numBehaviors();
		MsgListener N=null;
		for(int b=0;b<num;b++)
		{
			N=fetchBehavior(b);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		num=numScripts();
		for(int s=0;s<num;s++)
		{
			N=fetchScript(s);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		num=numEffects();
		for(int i=0;i<num;i++)
		{
			N=fetchEffect(i);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}

		final MOB mob=msg.source();

		if(msg.tool()==this)
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_THROW:
				if(mob.isMine(this))
				{
					if((phyStats().weight()>(mob.maxCarry()/5))
					&&(phyStats().weight()!=0))
					{
						mob.tell(L("@x1 is too heavy to throw.",name()));
						return false;
					}
					if(!CMLib.flags().isDroppable(this))
					{
						mob.tell(L("You can't seem to let go of @x1.",name()));
						return false;
					}
				}
				break;
			case CMMsg.TYP_POUR:
				if(!(this instanceof Drink))
				{
					mob.tell(mob,this,null,L("You can't do that with <T-NAMESELF>."));
					return false;
				}
				break;
			default:
				break;
			}
			return true;
		}
		else
		if(!msg.amITarget(this))
			return true;
		else
		if(msg.targetMinor()==CMMsg.NO_EFFECT)
			return true;
		else
		if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
		&&(!CMLib.flags().isGettable(this))
		&&((displayText().isEmpty())
		   ||((msg.tool() instanceof Ability)
			&&(((Ability)msg.tool()).abstractQuality()==Ability.QUALITY_MALICIOUS))))
		{
			mob.tell(L("Please don't do that."));
			return false;
		}
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_ENHANCE:
		case CMMsg.TYP_REPAIR:
			if(this instanceof Technical)
				return true;
			break;
		case CMMsg.TYP_DAMAGE:
		case CMMsg.TYP_WEAPONATTACK:
		case CMMsg.TYP_ATTACKMISS:
			if((this instanceof BoardableShip)
			||((this instanceof Rideable)||(((Rideable)this).rideBasis()==Rideable.RIDEABLE_WATER)))
				return true;
			break;
		case CMMsg.TYP_EXPIRE:
		case CMMsg.TYP_LOOK:
		case CMMsg.TYP_EXAMINE:
		case CMMsg.TYP_READ:
		case CMMsg.TYP_WASREAD:
		case CMMsg.TYP_QUIETMOVEMENT:
		case CMMsg.TYP_NOISYMOVEMENT:
		case CMMsg.TYP_HANDS:
		case CMMsg.TYP_SPEAK:
		case CMMsg.TYP_OK_ACTION:
		case CMMsg.TYP_OK_VISUAL:
		case CMMsg.TYP_DEATH:
		case CMMsg.TYP_NOISE:
		case CMMsg.TYP_EMOTE:
		case CMMsg.TYP_SNIFF:
			return true;
		case CMMsg.TYP_SIT:
			if((this instanceof DeadBody)
			&&(mob.phyStats().height()<=0)
			&&(mob.phyStats().weight()<=0))
				return true;
			//$FALL-THROUGH$
		case CMMsg.TYP_SLEEP:
		case CMMsg.TYP_MOUNT:
		case CMMsg.TYP_DISMOUNT:
		case CMMsg.TYP_ENTER:
			if(this instanceof Rideable)
				return true;
			break;
		case CMMsg.TYP_LIST:
			if(CMLib.coffeeShops().getShopKeeper(this)!=null)
				return true;
			break;
		case CMMsg.TYP_RELOAD:
			if((this instanceof AmmunitionWeapon)
			&&(((AmmunitionWeapon)this).requiresAmmunition()))
			{
				if(((AmmunitionWeapon)this).ammunitionRemaining()>=((AmmunitionWeapon)this).ammunitionCapacity())
				{
					mob.tell(L("@x1 is already loaded.",name()));
					return false;
				}
				return true;
			}
			break;
		case CMMsg.TYP_UNLOAD:
			if((this instanceof AmmunitionWeapon)
			&&(((AmmunitionWeapon)this).requiresAmmunition()))
			{
				if(((AmmunitionWeapon)this).ammunitionRemaining()<=0)
				{
					mob.tell(L("@x1 is not loaded.",name()));
					return false;
				}
				return true;
			}
			break;
		case CMMsg.TYP_HOLD:
			if((!fitsOn(Wearable.WORN_HELD))||(properWornBitmap==0))
			{
				final StringBuilder str=new StringBuilder(L("You can't hold @x1.",name()));
				if(fitsOn(Wearable.WORN_WIELD))
					str.append(L("Try WIELDing it."));
				else
				if(properWornBitmap>0)
					str.append(L("Try WEARing it."));
				mob.tell(str.toString());
				return false;
			}
			if(!alreadyWornMsg(msg.source(),this))
				return false;
			if(phyStats().level()>mob.phyStats().level())
			{
				mob.tell(L("That looks too advanced for you."));
				return false;
			}
			if((!rawLogicalAnd())||(properWornBitmap==0))
			{
				if(!canWear(mob,Wearable.WORN_HELD))
				{
					final Item alreadyWearing=mob.fetchHeldItem();
					if(alreadyWearing!=null)
					{
						if((!CMLib.commands().postRemove(mob,alreadyWearing,false))
						||(!canWear(mob,Wearable.WORN_HELD)))
						{
							mob.tell(L("Your hands are full."));
							return false;
						}
					}
					else
					{
						mob.tell(L("You need hands to hold things."));
						return false;
					}
				}
				return true;
			}
			return canWearComplete(mob,0);
		case CMMsg.TYP_WEAR:
			if(properWornBitmap==0)
			{
				mob.tell(L("You can't wear @x1.",name()));
				return false;
			}
			if(!alreadyWornMsg(msg.source(),this))
				return false;
			if(phyStats().level()>mob.phyStats().level())
			{
				mob.tell(L("That looks too advanced for you."));
				return false;
			}
			return canWearComplete(mob,(msg.value()<=0)?0:((long)(1<<msg.value())/2));
		case CMMsg.TYP_WIELD:
			if((!fitsOn(Wearable.WORN_WIELD))||(properWornBitmap==0))
			{
				mob.tell(L("You can't wield @x1 as a weapon.",name()));
				return false;
			}
			if(!alreadyWornMsg(msg.source(),this))
				return false;
			if(phyStats().level()>mob.phyStats().level())
			{
				mob.tell(L("That looks too advanced for you."));
				return false;
			}
			if((!rawLogicalAnd())||(properWornBitmap==0))
			{
				if(!canWear(mob,Wearable.WORN_WIELD))
				{
					final Item alreadyWearing=mob.fetchFirstWornItem(Wearable.WORN_WIELD);
					if(alreadyWearing!=null)
					{
						if(!CMLib.commands().postRemove(mob,alreadyWearing,false))
						{
							mob.tell(L("You are already wielding @x1.",alreadyWearing.name()));
							return false;
						}
					}
					else
					{
						mob.tell(L("You need hands to wield things."));
						return false;
					}
				}
			}
			return canWearComplete(mob,0);
		case CMMsg.TYP_PUSH:
		case CMMsg.TYP_PULL:
			if(msg.source().isMine(this))
			{
				mob.tell(L("You'll need to put that down first."));
				return false;
			}
			if(!CMLib.flags().isGettable(this))
			{
				mob.tell(L("You can't move @x1.",name()));
				return false;
			}
			return true;
		case CMMsg.TYP_GET:
			if((msg.tool()==null)||(msg.tool() instanceof MOB))
			{
				if((!CMLib.flags().canBeSeenBy(this,mob))
				&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				&&(amWearingAt(Wearable.IN_INVENTORY)))
				{
					mob.tell(L("You can't see that."));
					return false;
				}
				if((mob.phyStats().level()<phyStats().level()-(10+(mob.phyStats().level()/5)))
				&&(!(mob instanceof ShopKeeper))
				&&(!mob.charStats().getMyRace().leveless())
				&&(!mob.charStats().getCurrentClass().leveless()))
				{
					mob.tell(L("@x1 is too powerful to endure possessing it.",name()));
					return false;
				}
				if((phyStats().weight()>(mob.maxCarry()-mob.phyStats().weight()))
				&&(!mob.isMine(this))
				&&(phyStats().weight()!=0))
				{
					mob.tell(L("@x1 is too heavy.",name()));
					return false;
				}
				if((numberOfItems()>(mob.maxItems()-mob.numItems()))&&(!mob.isMine(this)))
				{
					mob.tell(L("You can't carry that many items."));
					return false;
				}
				if(!CMLib.flags().isGettable(this))
				{
					mob.tell(L("You can't get @x1.",name()));
					return false;
				}
				if((this instanceof Rideable)&&(((Rideable)this).numRiders()>0))
				{
					if((mob.riding()!=null)&&(mob.riding()==this))
						mob.tell(L("You are @x1 @x2!",((Rideable)this).stateString(mob),name()));
					else
						mob.tell(L("Someone is @x1 @x2!",((Rideable)this).stateString(mob),name()));
					return false;
				}
				return true;
			}
			if(this instanceof Container)
				return true;
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_BID:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_GENERAL:
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_VIEW:
			case CMMsg.TYP_GIVE:
				return true;
			}
			break;
		case CMMsg.TYP_REMOVE:
			if((msg.tool()==null)||(msg.tool() instanceof MOB))
			{
				if((!CMLib.flags().canBeSeenBy(this,mob))
				   &&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
				   &&(amWearingAt(Wearable.IN_INVENTORY)))
				{
					mob.tell(L("You can't see that."));
					return false;
				}
				if((!amWearingAt(Wearable.IN_INVENTORY))&&(!CMLib.flags().isRemovable(this)))
				{
					if(amWearingAt(Wearable.WORN_WIELD)||amWearingAt(Wearable.WORN_HELD))
					{
						mob.tell(L("You can't seem to let go of @x1.",name()));
						return false;
					}
					mob.tell(L("You can't seem to remove @x1.",name()));
					return false;
				}
				Item I=null;
				final short layer=(this instanceof Armor)?((Armor)this).getClothingLayer():0;
				short thislayer=0;
				if(rawWornCode()>0)
				for(int i=0;i<mob.numItems();i++)
				{
					I=mob.getItem(i);
					if((I!=null)&&(I!=this)&&((I.rawWornCode()&rawWornCode())>0))
					{
						thislayer=(I instanceof Armor)?((Armor)I).getClothingLayer():0;
						if(thislayer>layer)
						{
							mob.tell(mob,I,null,L("You must remove <T-NAME> first."));
							return false;
						}
					}
				}
				return true;
			}
			if(this instanceof Container)
				return true;
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_BID:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_GENERAL:
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_VIEW:
			case CMMsg.TYP_GIVE:
				return true;
			}
			break;
		case CMMsg.TYP_DROP:
			if(!mob.isMine(this))
			{
				mob.tell(L("You don't have that."));
				return false;
			}
			if(!CMLib.flags().isDroppable(this))
			{
				mob.tell(L("You can't seem to let go of @x1.",name()));
				return false;
			}
			return true;
		case CMMsg.TYP_BUY:
		case CMMsg.TYP_BID:
		case CMMsg.TYP_SELL:
		case CMMsg.TYP_VALUE:
		case CMMsg.TYP_VIEW:
		case CMMsg.TYP_ITEMGENERATED:
		case CMMsg.TYP_ITEMSGENERATED:
			return true;
		case CMMsg.TYP_OPEN:
		case CMMsg.TYP_CLOSE:
		case CMMsg.TYP_LOCK:
		case CMMsg.TYP_INSTALL:
		case CMMsg.TYP_UNLOCK:
			if(this instanceof Container)
				return true;
			break;
		case CMMsg.TYP_PUT:
			if(this instanceof Container)
				return true;
			if(CMath.bset(phyStats().sensesMask(), PhyStats.SENSE_INSIDEACCESSIBLE))
				return true;
			mob.tell(mob,this,null,L("You can't put things inside <T-NAME>."));
			return false;
		case CMMsg.TYP_DELICATE_HANDS_ACT:
		case CMMsg.TYP_JUSTICE:
		case CMMsg.TYP_WAND_USE:
		case CMMsg.TYP_ACID: // slime
		case CMMsg.TYP_FIRE: // lighting
		case CMMsg.TYP_WATER: // rust
		case CMMsg.TYP_CAST_SPELL:
		case CMMsg.TYP_POISON: // for use poison
			return true;
		case CMMsg.TYP_EXTINGUISH:
			if(CMLib.flags().isOnFire(this))
				return true;
			break;
		case CMMsg.TYP_FILL:
			if(this instanceof Drink)
				return true;
			if(this instanceof Lantern)
				return true;
			break;
		case CMMsg.TYP_POUR:
			if(msg.tool() instanceof Drink)
				return true;
			break;
		case CMMsg.TYP_EAT:
			if(this instanceof Food)
				return true;
			break;
		case CMMsg.TYP_DRINK:
			if(this instanceof Drink)
				return true;
			break;
		case CMMsg.TYP_ADVANCE:
			if((this instanceof BoardableShip)
			||((this instanceof Rideable)||(((Rideable)this).rideBasis()==Rideable.RIDEABLE_WATER)))
				return true;
			break;
		case CMMsg.TYP_ACTIVATE:
		case CMMsg.TYP_POWERCURRENT:
		case CMMsg.TYP_DEACTIVATE:
			if((this instanceof Electronics)||(this instanceof Software))
				return true;
			break;
		case CMMsg.TYP_COLLISION:
			if(this instanceof SpaceObject)
				return true;
			break;
		case CMMsg.TYP_REWRITE:
			if((isReadable())
			&&(!(this instanceof Scroll))
			&&(!(this instanceof Electronics)))
			{
				if(msg.targetMessage().trim().isEmpty())
				{
					if(this instanceof Electronics)
						mob.tell(L("Enter what into @x1?",name()));
					else
						mob.tell(L("Write what on @x1?",name()));
					return false;
				}
				return true;
			}
			return false;
		case CMMsg.TYP_WRITE:
			if(((isReadable())&&(!(this instanceof Scroll)))
			||(this instanceof ElecPanel)
			||(this instanceof Software))
			{
				if(msg.targetMessage().trim().isEmpty())
				{
					if(this instanceof Electronics)
						mob.tell(L("Enter what into @x1?",name()));
					else
						mob.tell(L("Write what on @x1?",name()));
					return false;
				}
				return true;
			}
			if(this instanceof Electronics)
				mob.tell(L("You can't enter commands into @x1.",name()));
			else
				mob.tell(L("You can't write on @x1.",name()));
			return false;
		default:
			break;
		}
		mob.tell(mob,this,null,L("You can't do that to <T-NAMESELF>."));
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		// the order that these things are checked in should
		// be holy, and etched in stone.
		if(numBehaviors()>0)
		{
			eachBehavior(new EachApplicable<Behavior>()
			{ 
				@Override
				public final void apply(final Behavior B)
				{
					B.executeMsg(me,msg);
				} 
			});
		}
		if(numScripts()>0)
		{
			eachScript(new EachApplicable<ScriptingEngine>()
			{ 
				@Override
				public final void apply(final ScriptingEngine S)
				{
					S.executeMsg(me,msg);
				} 
			});
		}
		if(numEffects()>0)
		{
			eachEffect(new EachApplicable<Ability>()
			{ 
				@Override
				public final void apply(final Ability A)
				{
					A.executeMsg(me, msg);
				}
			});
		}

		final MOB mob=msg.source();
		if((msg.tool()==this)
		&&(msg.sourceMinor()==CMMsg.TYP_THROW)
		&&(mob!=null)
		&&(msg.target()!=null))
		{
			final Room R=CMLib.map().roomLocation(msg.target());
			if(mob.isMine(this))
			{
				mob.delItem(this);
				if(!R.isContent(this))
					R.addItem(this,ItemPossessor.Expire.Player_Drop);
				if(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_OPTIMIZE))
				{
					R.recoverRoomStats();
					if(mob.location()!=R)
						mob.location().recoverRoomStats();
				}
			}
			unWear();
			setContainer(null);
		}
		else
		if(!msg.amITarget(this))
			return;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_SNIFF:
			CMLib.commands().handleBeingSniffed(msg);
			break;
		case CMMsg.TYP_LOOK:
		case CMMsg.TYP_EXAMINE:
			CMLib.commands().handleBeingLookedAt(msg);
			break;
		case CMMsg.TYP_READ:
			CMLib.commands().handleBeingRead(msg);
			break;
		case CMMsg.TYP_HOLD:
			CMLib.commands().handleBeingHeld(msg);
			break;
		case CMMsg.TYP_WEAR:
			CMLib.commands().handleBeingWorn(msg);
			break;
		case CMMsg.TYP_WIELD:
			CMLib.commands().handleBeingWielded(msg);
			break;
		case CMMsg.TYP_GET:
			CMLib.commands().handleBeingGetted(msg);
			break;
		case CMMsg.TYP_REMOVE:
			CMLib.commands().handleBeingRemoved(msg);
			break;
		case CMMsg.TYP_DROP:
			CMLib.commands().handleBeingDropped(msg);
			break;
		case CMMsg.TYP_WRITE:
			if(isReadable() && (!(this instanceof Electronics)))
				setReadableText((readableText()+" "+msg.targetMessage()).trim());
			break;
		case CMMsg.TYP_REWRITE:
			if(isReadable() && (!(this instanceof Electronics)))
			{
				if((""+msg.targetMessage()).startsWith("DELETE "))
					setReadableText("");
				else
				if(!CMath.isInteger(""+msg.targetMessage()))
					setReadableText(""+msg.targetMessage().trim());
			}
			break;
		case CMMsg.TYP_EXTINGUISH:
			if(CMLib.flags().isOnFire(this))
			{
				final Ability A=fetchEffect("Burning");
				if(A!=null)
				{
					A.setAbilityCode(0);
					A.unInvoke();
					delEffect(A);
				}
			}
			break;
		case CMMsg.TYP_EXPIRE:
		case CMMsg.TYP_DEATH:
			destroy();
			break;
		default:
			break;
		}
	}

	@Override
	public int recursiveWeight()
	{
		return phyStats().weight();
	}

	@Override
	public void stopTicking()
	{
		destroyed=true; // WHY?!?!?
		CMLib.threads().deleteTick(this,-1);
	}

	@Override
	public void destroy()
	{
		if((phyStats().sensesMask()&PhyStats.SENSE_UNDESTROYABLE)>0)
			return;
		myContainer=null;
		CMLib.map().registerWorldObjectDestroyed(null,null,this);
		try
		{
			CMLib.catalog().changeCatalogUsage(this, false);
		}
		catch (final Exception t)
		{
		}
		delAllEffects(true);
		delAllBehaviors();
		delAllScripts();
		CMLib.threads().deleteTick(this,Tickable.TICKID_ITEM_BEHAVIOR);

		riding=null;
		destroyed=true;

		if(owner!=null)
		{
			if (owner instanceof Room)
			{
				final Room thisRoom=(Room)owner;
				if(this instanceof Container)
				{
					for(int r=thisRoom.numItems()-1;r>=0;r--)
					{
						final Item thisItem = thisRoom.getItem(r);
						if((thisItem!=null)
						&&(!thisItem.amDestroyed())
						&&(thisItem.container()!=null)
						&&(thisItem.container()==this))
							thisItem.destroy();
					}
				}
				thisRoom.delItem(this);
			}
			else
			if (owner instanceof MOB)
			{
				final MOB mob=(MOB)owner;
				if(this instanceof Container)
				{
					for(int r=mob.numItems()-1;r>=0;r--)
					{
						final Item thisItem = mob.getItem(r);
						if((thisItem!=null)
						&&(!thisItem.amDestroyed())
						&&(thisItem.container()==this))
							thisItem.destroy();
					}
				}
				mob.delItem(this);
			}
		}
		myContainer=null;
		rawImageName=null;
		cachedImageName=null;
		secretIdentity=null;
		owner=null;
		affects=null;
		behaviors=null;
		scripts=null;
	}

	@Override
	public void removeFromOwnerContainer()
	{
		myContainer=null;

		if(owner==null)
			return;

		if (owner instanceof Room)
		{
			final Room thisRoom=(Room)owner;
			for(int r=thisRoom.numItems()-1;r>=0;r--)
			{
				final Item thisItem = thisRoom.getItem(r);
				if((thisItem!=null)
				&&(thisItem.container()!=null)
				&&(thisItem.container()==this))
					thisItem.removeFromOwnerContainer();
			}
			thisRoom.delItem(this);
		}
		else
		if (owner instanceof MOB)
		{
			final MOB mob=(MOB)owner;
			for(int r=mob.numItems()-1;r>=0;r--)
			{
				final Item thisItem = mob.getItem(r);
				if((thisItem!=null)
				&&(thisItem.container()!=null)
				&&(thisItem.container()==this))
					thisItem.removeFromOwnerContainer();
			}
			mob.delItem(this);
		}
		recoverPhyStats();
	}

	@Override
	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null)
			return;
		if(fetchEffect(to.ID())!=null)
			return;
		if(affects==null)
			affects=new SVector<Ability>(1);
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void addEffect(Ability to)
	{
		if(to==null)
			return;
		if(fetchEffect(to.ID())!=null)
			return;
		if(affects==null)
			affects=new SVector<Ability>(1);
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void delEffect(Ability to)
	{
		if(affects==null)
			return;
		if(affects.remove(to))
			to.setAffectedOne(null);
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
		final List<Ability> affects=this.affects;
		if((affects!=null)&&(!affects.isEmpty()))
		{
			try
			{
				for(int a=0;a<affects.size();a++)
				{
					final Ability A=affects.get(a);
					if(A!=null)
						applier.apply(A);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	@Override
	public void delAllEffects(boolean unInvoke)
	{
		final SVector<Ability> affects=this.affects;
		if(affects==null)
			return;
		Ability keepThisOne=null;
		for(int a=numEffects()-1;a>=0;a--)
		{
			final Ability A=fetchEffect(a);
			if(A!=null)
			{
				if(unInvoke)
				{
					if(A.ID().equals("ItemRejuv"))
					{
						keepThisOne=A;
						continue;
					}
					A.unInvoke();
				}
				A.setAffectedOne(null);
			}
		}
		affects.clear();
		if(keepThisOne != null)
		{
			affects.add(keepThisOne);
		}
	}

	@Override 
	public Enumeration<Ability> effects()
	{
		return (affects==null)?EmptyEnumeration.INSTANCE:affects.elements();
	}

	@Override
	public int numEffects()
	{
		if(affects==null)
			return 0;
		return affects.size();
	}

	@Override
	public Ability fetchEffect(int index)
	{
		if(affects==null)
			return null;
		try
		{
			return affects.elementAt(index);
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Ability fetchEffect(String ID)
	{
		if(affects==null)
			return null;
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.ID().equals(ID)))
				return A;
		}
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	@Override
	public void addBehavior(Behavior to)
	{
		if(to==null)
			return;
		if(behaviors==null)
			behaviors=new SVector<Behavior>(1);
		for(final Behavior B : behaviors)
		{
			if(B.ID().equals(to.ID()))
				return;
		}

		// first one! so start ticking...
		if(behaviors.isEmpty())
			CMLib.threads().startTickDown(this,Tickable.TICKID_ITEM_BEHAVIOR,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}

	@Override
	public void delAllBehaviors()
	{
		final boolean didSomething=(behaviors!=null)&&(!behaviors.isEmpty());
		if(didSomething)
			behaviors.clear();
		behaviors=null;
		if(didSomething && ((scripts==null)||(scripts.isEmpty())))
			CMLib.threads().deleteTick(this,Tickable.TICKID_ITEM_BEHAVIOR);
	}

	@Override
	public void delBehavior(Behavior to)
	{
		if(behaviors==null)
			return;
		if(behaviors.remove(to))
		{
			if(((behaviors==null)||(behaviors.isEmpty()))&&((scripts==null)||(scripts.isEmpty())))
				CMLib.threads().deleteTick(this,Tickable.TICKID_ITEM_BEHAVIOR);
		}
	}

	@Override
	public int numBehaviors()
	{
		if(behaviors==null)
			return 0;
		return behaviors.size();
	}

	@Override
	public Enumeration<Behavior> behaviors()
	{
		return (behaviors==null)?EmptyEnumeration.INSTANCE:behaviors.elements();
	}

	@Override
	public Behavior fetchBehavior(int index)
	{
		if(behaviors==null)
			return null;
		try
		{
			return behaviors.elementAt(index);
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Behavior fetchBehavior(String ID)
	{
		if(behaviors==null)
			return null;
		for(final Behavior B : behaviors)
		{
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
		final List<Behavior> behaviors=this.behaviors;
		if(behaviors!=null)
		{
			try
			{
				for(int a=0;a<behaviors.size();a++)
				{
					final Behavior B=behaviors.get(a);
					if(B!=null)
						applier.apply(B);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	/** Manipulation of the scripts list */
	@Override
	public void addScript(ScriptingEngine S)
	{
		if(scripts==null)
			scripts=new SVector<ScriptingEngine>(1);
		if(S==null)
			return;
		if(!scripts.contains(S))
		{
			ScriptingEngine S2=null;
			for(int s=0;s<scripts.size();s++)
			{
				S2=scripts.elementAt(s);
				if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
					return;
			}
			if(scripts.isEmpty())
				CMLib.threads().startTickDown(this,Tickable.TICKID_ITEM_BEHAVIOR,1);
			scripts.addElement(S);
		}
	}

	@Override
	public void delScript(ScriptingEngine S)
	{
		if(scripts!=null)
		{
			if(scripts.remove(S))
			{
				if(scripts.isEmpty())
					scripts=new SVector(1);
				if(((behaviors==null)||(behaviors.isEmpty()))&&((scripts==null)||(scripts.isEmpty())))
					CMLib.threads().deleteTick(this,Tickable.TICKID_ITEM_BEHAVIOR);
			}
		}
	}

	@Override
	public void delAllScripts()
	{
		final boolean didSomething=(scripts!=null)&&(!scripts.isEmpty());
		if(didSomething)
			scripts.clear();
		scripts=null;
		if(didSomething && ((behaviors==null)||(behaviors.isEmpty())))
			CMLib.threads().deleteTick(this,Tickable.TICKID_ITEM_BEHAVIOR);
	}

	@Override
	public int numScripts()
	{
		return (scripts == null) ? 0 : scripts.size();
	}

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return (scripts == null) ? EmptyEnumeration.INSTANCE : scripts.elements();
	}

	@Override
	public ScriptingEngine fetchScript(int x)
	{
		try
		{
			return scripts.elementAt(x);
		}
		catch (final Exception e)
		{
		}
		return null;
	}

	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
		final List<ScriptingEngine> scripts=this.scripts;
		if(scripts!=null)
		{
			try
			{
				for(int a=0;a<scripts.size();a++)
				{
					final ScriptingEngine S=scripts.get(a);
					if(S!=null)
						applier.apply(S);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	protected String tackOns()
	{
		final StringBuilder identity=new StringBuilder("");
		if(numEffects()>0)
			identity.append("\n\rHas the following magical properties: ");
		if(numEffects()>0)
		{
			eachEffect(new EachApplicable<Ability>()
			{ 
				@Override
				public final void apply(final Ability A)
				{
					if(A.accountForYourself().length()>0)
						identity.append("\n\r"+A.accountForYourself());
				}
			});
		}
		return identity.toString();
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

	protected static String[] CODES={"CLASS","USES","LEVEL","ABILITY","TEXT"};

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return "" + usesRemaining();
		case 2:
			return "" + basePhyStats().ability();
		case 3:
			return "" + basePhyStats().level();
		case 4:
			return text();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setUsesRemaining(CMath.s_parseIntExpression(val));
			break;
		case 2:
			basePhyStats().setLevel(CMath.s_parseIntExpression(val));
			break;
		case 3:
			basePhyStats().setAbility(CMath.s_parseIntExpression(val));
			break;
		case 4:
			setMiscText(val);
			break;
		}
	}
	
	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	protected String I(final String str)
	{
		return CMLib.lang().commandWordTranslation(str);
	}

	@Override
	public int getSaveStatIndex()
	{
		return (xtraValues == null) ? getStatCodes().length : getStatCodes().length - xtraValues.length;
	}

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
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdItem))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
