package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class StdItem implements Item
{
	public String ID(){	return "StdItem";}

	protected String 	name="an ordinary item";
	protected String	displayText="a nondescript item sits here doing nothing.";
	protected byte[] 	description=null;
	protected Item 		myContainer=null;
	protected int 		myUses=Integer.MAX_VALUE;
	protected long 		myWornCode=Item.INVENTORY;
	protected String 	miscText="";
	protected String	secretIdentity=null;
	protected boolean	wornLogicalAnd=false;
	protected long 		properWornBitmap=Item.HELD;
	protected int		baseGoldValue=0;
	protected int		material=EnvResource.RESOURCE_COTTON;
	protected Environmental owner=null;
	protected long dispossessionTime=0;
	protected long tickStatus=Tickable.STATUS_NOT;

	protected Vector affects=null;
	protected Vector behaviors=null;

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	protected boolean destroyed=false;

	public StdItem()
	{
		baseEnvStats().setWeight(1);
		baseEnvStats().setArmor(0);
	}
	public boolean isGeneric(){return false;}
	public String Name(){ return name;}
	public void setName(String newName){name=newName;}
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return name;
	}
	public EnvStats envStats()
	{
		return envStats;
	}

	public EnvStats baseEnvStats()
	{
		return baseEnvStats;
	}

	public void recoverEnvStats()
	{
		envStats=baseEnvStats.cloneStats();
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
		if(envStats().ability()>0)
			envStats().setDisposition(envStats().disposition()|EnvStats.IS_BONUS);
		if((owner()!=null)
		&&(owner() instanceof MOB)
		&&(Sense.isHidden(this)))
		   envStats().setDisposition((int)(envStats().disposition()&(EnvStats.ALLMASK-EnvStats.IS_HIDDEN)));
	}

	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}
	public Environmental newInstance()
	{
		return new StdItem();
	}
	public boolean subjectToWearAndTear(){return false;}
	protected void cloneFix(Item E)
	{
		destroyed=false;
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();

		affects=null;
		behaviors=null;
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)	addBehavior((Behavior)B.copyOf());
		}

		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if((A!=null)&&(!A.canBeUninvoked())&&(!A.ID().equals("ItemRejuv")))
				addEffect((Ability)A.copyOf());
		}

	}
	public Environmental copyOf()
	{
		try
		{
			StdItem E=(StdItem)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	protected Rideable riding=null;
	public Rideable riding(){return riding;}
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

	public Environmental owner(){return owner;}
	public void setOwner(Environmental E)
	{
		owner=E;
		if((E!=null)&&(!(E instanceof Room)))
			setDispossessionTime(0);
		recoverEnvStats();
	}
	public long dispossessionTime()
	{
		return dispossessionTime;
	}
	public void setDispossessionTime(long time)
	{
		dispossessionTime=time;
	}

	public boolean amDestroyed()
	{
		return destroyed;
	}

	public boolean amWearingAt(long wornCode)
	{
		if((myWornCode+wornCode)==0)
			return true;
		else
		if(wornCode==0)
			return false;
		return (myWornCode & wornCode)==wornCode;
	}
	public boolean fitsOn(long wornCode)
	{
		if(wornCode<=0)	return true;
		return ((properWornBitmap & wornCode)==wornCode);
	}
	public void wearIfPossible(MOB mob)
	{
		for(int i=0;i<20;i++)
		{
			long wornCode=1<<i;
			if((fitsOn(wornCode))
			&&(canWear(mob,wornCode)))
			{
				wearAt(wornCode);
				break;
			}
		}
	}
	public void wearAt(long wornCode)
	{
		if(wornCode==Item.INVENTORY)
		{
			unWear();
			return;
		}
		if(wornLogicalAnd)
			setRawWornCode(properWornBitmap);
		else
			setRawWornCode(wornCode);
		recoverEnvStats();
	}

	public long rawProperLocationBitmap()
	{ return properWornBitmap;}
	public boolean rawLogicalAnd()
	{ return wornLogicalAnd;}
	public void setRawProperLocationBitmap(long newValue)
	{
		properWornBitmap=newValue;
	}
	public void setRawLogicalAnd(boolean newAnd)
	{
		wornLogicalAnd=newAnd;
	}
	public boolean compareProperLocations(Item toThis)
	{
		if(toThis.rawLogicalAnd()!=wornLogicalAnd)
			return false;
		if((toThis.rawProperLocationBitmap()|Item.HELD)==(properWornBitmap|Item.HELD))
			return true;
		return false;
	}

	public long whereCantWear(MOB mob)
	{
		long couldHaveBeenWornAt=-1;
		if(properWornBitmap==0)
			return couldHaveBeenWornAt;

		long wornCode=0;
		if(!wornLogicalAnd)
		{
			for(int i=0;i<20;i++)
			{
				wornCode=1<<i;
				if(fitsOn(wornCode))
				{
					couldHaveBeenWornAt=wornCode;
					if(mob.freeWearPositions(wornCode)>0)
						return 0;
				}
			}
			return couldHaveBeenWornAt;
		}
		else
		{
			for(int i=0;i<20;i++)
			{
				wornCode=1<<i;
				if((fitsOn(wornCode))
				&&(mob.freeWearPositions(wornCode)==0))
					return wornCode;
			}
			return 0;
		}
	}

	public boolean canWear(MOB mob, long where)
	{
		if(where==0) return (whereCantWear(mob)==0);
		return mob.freeWearPositions(where)>0;
	}

	public long rawWornCode()
	{
		return myWornCode;
	}
	public void setRawWornCode(long newValue)
	{
		myWornCode=newValue;
	}

	public void unWear()
	{
		setRawWornCode(Item.INVENTORY);
		recoverEnvStats();
	}


	public int material()
	{
		return material;
	}

	public void setMaterial(int newValue)
	{
		material=newValue;
	}

	public int value()
	{
		return baseGoldValue()+(10*envStats().ability());
	}
	public int baseGoldValue(){return baseGoldValue;}
	public void setBaseValue(int newValue)
	{
		baseGoldValue=newValue;
	}

	public String readableText(){return miscText;}
	public void setReadableText(String text){miscText=text;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof Room)
		{
			if((Sense.isLightSource(this))&&(Sense.isInDark(affected)))
				affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
		}
		else
		{
			if(Sense.isLightSource(this))
			{
				if(rawWornCode()!=Item.INVENTORY)
					affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
				if(Sense.isInDark(affected))
					affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
			}
			if((amWearingAt(Item.ON_MOUTH))&&(affected instanceof MOB))
			{
				if(!(this instanceof Light))
					affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_TASTE);
			}
			if((!amWearingAt(Item.FLOATING_NEARBY))
			&&((!(affected instanceof MOB))||(((MOB)affected).riding()!=this)))
				affectableStats.setWeight(affectableStats.weight()+envStats().weight());
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.bubbleAffect()))
			   A.affectEnvStats(affected,affectableStats);
		}
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.bubbleAffect()))
			   A.affectCharStats(affectedMob,affectableStats);
		}
	}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.bubbleAffect()))
			   A.affectCharState(affectedMob,affectableMaxState);
		}
	}
	public void setMiscText(String newText)
	{
		miscText=newText;
	}
	public String text()
	{
		return miscText;
	}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public long getTickStatus(){return tickStatus;}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(destroyed)
			return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==MudHost.TICK_ITEM_BEHAVIOR)
		{
			if(numBehaviors()==0) return false;
			for(int b=0;b<numBehaviors();b++)
			{
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}
		}
		else
		if(tickID!=MudHost.TICK_CLANITEM)
		{
			int a=0;
			while(a<numEffects())
			{
				Ability A=fetchEffect(a);
				if(A!=null)
				{
					int s=numEffects();
					tickStatus=Tickable.STATUS_AFFECT+a;
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					if(numEffects()==s)
						a++;
				}
				else
					a++;
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public Item ultimateContainer()
	{
		if(container()==null) return this;
		return container().ultimateContainer();
	}
	public Item container()
	{
		return myContainer;
	}
	public String rawSecretIdentity(){return ((secretIdentity==null)?"":secretIdentity);}
	public String secretIdentity()
	{
		if((secretIdentity!=null)&&(secretIdentity.length()>0))
			return secretIdentity+"\n\rLevel: "+envStats().level()+tackOns();
		return description()+"\n\rLevel: "+envStats().level()+tackOns();
	}

	public void setSecretIdentity(String newIdentity)
	{
		if((newIdentity==null)
		||(newIdentity.trim().equalsIgnoreCase(description()))
		||(newIdentity.length()==0))
			secretIdentity=null;
		else
			secretIdentity=newIdentity;
	}

	public String displayText()
	{
		return displayText;
	}
	public void setDisplayText(String newDisplayText)
	{
		displayText=newDisplayText;
	}
	public String description()
	{
		if((description==null)||(description.length==0))
			return "";
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ITEMDCOMPRESS))
			return Util.decompressString(description);
		else
			return new String(description);
	}
	public void setDescription(String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ITEMDCOMPRESS))
			description=Util.compressString(newDescription);
		else
			description=newDescription.getBytes();
	}
	public void setContainer(Item newContainer)
	{
		myContainer=newContainer;
	}
	public int usesRemaining()
	{
		return myUses;
	}
	public void setUsesRemaining(int newUses)
	{
		myUses=newUses;
	}

	public boolean savable(){return true;}

	protected String dispossessionTimeLeftString()
	{
		if(dispossessionTime()==0)
			return "N/A";
		return ""+(dispossessionTime()-System.currentTimeMillis());
	}

	private boolean alreadyWornMsg(MOB mob, Item thisItem)
	{
		if(!thisItem.amWearingAt(Item.INVENTORY))
		{
			if(thisItem.amWearingAt(Item.WIELD))
				mob.tell(thisItem.name()+" is already being wielded.");
			else
			if(thisItem.amWearingAt(Item.HELD))
				mob.tell(thisItem.name()+" is already being held.");
			else
			if(thisItem.amWearingAt(Item.FLOATING_NEARBY))
				mob.tell(thisItem.name()+" is floating nearby.");
			else
				mob.tell(thisItem.name()+"is already being worn.");
			return false;
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		// the order that these things are checked in should
		// be holy, and etched in stone.
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okMessage(this,msg)))
				return false;
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(!A.okMessage(this,msg)))
				return false;
		}

		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return true;
		else
		if(msg.targetCode()==CMMsg.NO_EFFECT)
			return true;
		else
		if((Util.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
		&&(!Sense.isGettable(this))
		&&((displayText().length()==0)
		   ||((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(((Ability)msg.tool()).quality()==Ability.MALICIOUS))))
		{
			mob.tell("Please don't do that.");
			return false;
		}
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_EXAMINESOMETHING:
		case CMMsg.TYP_READSOMETHING:
		case CMMsg.TYP_QUIETMOVEMENT:
		case CMMsg.TYP_HANDS:
		case CMMsg.TYP_SPEAK:
		case CMMsg.TYP_OK_ACTION:
		case CMMsg.TYP_OK_VISUAL:
		case CMMsg.TYP_DEATH:
		case CMMsg.TYP_NOISE:
			return true;
		case CMMsg.TYP_SIT:
		case CMMsg.TYP_SLEEP:
		case CMMsg.TYP_MOUNT:
		case CMMsg.TYP_DISMOUNT:
		case CMMsg.TYP_ENTER:
			if(this instanceof Rideable)
				return true;
			break;
		case CMMsg.TYP_HOLD:
			if(!alreadyWornMsg(msg.source(),this))
				return false;
			if(!fitsOn(Item.HELD))
			{
				StringBuffer str=new StringBuffer("You can't hold "+name()+".");
				if(fitsOn(Item.WIELD))
					str.append("Try WIELDing it.");
				else
				if(properWornBitmap>0)
					str.append("Try WEARing it.");
				mob.tell(str.toString());
				return false;
			}
			if(envStats().level()>mob.envStats().level())
			{
				mob.tell("That looks too advanced for you.");
				return false;
			}
			if(!canWear(mob,Item.HELD))
			{
				Item alreadyWearing=mob.fetchFirstWornItem(Item.HELD);
				if(alreadyWearing!=null)
				{
					if((!CommonMsgs.remove(mob,alreadyWearing,false))
					||(!canWear(mob,Item.HELD)))
					{
						mob.tell("Your hands are full.");
						return false;
					}
				}
				else
				{
					mob.tell("You need hands to hold things.");
					return false;
				}
			}
			return true;
		case CMMsg.TYP_WEAR:
			if(properWornBitmap==0)
			{
				mob.tell("You can't wear "+name()+".");
				return false;
			}
			if(!alreadyWornMsg(msg.source(),this))
				return false;
			if(envStats().level()>mob.envStats().level())
			{
				mob.tell("That looks too advanced for you.");
				return false;
			}
			if(!canWear(mob,0))
			{
				long cantWearAt=whereCantWear(mob);
				Item alreadyWearing=mob.fetchFirstWornItem(cantWearAt);
				if(alreadyWearing!=null)
				{
					if((cantWearAt!=Item.HELD)&&(cantWearAt!=Item.WIELD))
					{
						if((!CommonMsgs.remove(mob,alreadyWearing,false))
						||(!canWear(mob,0)))
						{
							mob.tell("You are already wearing "+alreadyWearing.name()+" on your "+Sense.wornLocation(cantWearAt)+".");
							return false;
						}
					}
					else
					{
						if(cantWearAt==Item.HELD)
							mob.tell("You are already holding "+alreadyWearing.name()+".");
						else
						if(cantWearAt==Item.WIELD)
							mob.tell("You are already wielding "+alreadyWearing.name()+".");
						else
							mob.tell("You are already wearing "+alreadyWearing.name()+" on your "+Sense.wornLocation(cantWearAt)+".");
						return false;
					}
				}
				else
				{
					mob.tell("You don't have anywhere you can wear that.");
					return false;
				}
			}
			return true;
		case CMMsg.TYP_WIELD:
			if(!fitsOn(Item.WIELD))
			{
				mob.tell("You can't wield "+name()+" as a weapon.");
				return false;
			}
			if(!alreadyWornMsg(msg.source(),this))
				return false;
			if(envStats().level()>mob.envStats().level())
			{
				mob.tell("That looks too advanced for you.");
				return false;
			}
			if(!canWear(mob,Item.WIELD))
			{
				Item alreadyWearing=mob.fetchFirstWornItem(Item.WIELD);
				if(alreadyWearing!=null)
				{
					if(!CommonMsgs.remove(mob,alreadyWearing,false))
					{
						mob.tell("You are already wielding "+alreadyWearing.name()+".");
						return false;
					}
				}
				else
				{
					mob.tell("You need hands to wield things.");
					return false;
				}
			}
			return true;
		case CMMsg.TYP_GET:
			if((msg.tool()==null)||(msg.tool() instanceof MOB))
			{
				if((!Sense.canBeSeenBy(this,mob))
				   &&((msg.sourceMajor()&CMMsg.MASK_GENERAL)==0)
				   &&(amWearingAt(Item.INVENTORY)))
				{
					mob.tell("You can't see that.");
					return false;
				}
				if((mob.envStats().level()<envStats().level()-(10+(mob.envStats().level()/5)))
				&&(!(mob instanceof ShopKeeper)))
				{
					mob.tell(name()+" is too powerful to endure possessing it.");
					return false;
				}
				if((envStats().weight()>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
				{
					mob.tell(name()+" is too heavy.");
					return false;
				}
				if(!Sense.isGettable(this))
				{
					mob.tell("You can't get "+name()+".");
					return false;
				}
				if((this instanceof Rideable)&&(((Rideable)this).numRiders()>0))
				{
					if((mob.riding()!=null)&&(mob.riding()==this))
						mob.tell("You are "+((Rideable)this).stateString(mob)+" "+name()+"!");
					else
						mob.tell("Someone is "+((Rideable)this).stateString(mob)+" "+name()+"!");
					return false;
				}
				return true;
			}
			if(this instanceof Container)
				return true;
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_BUY:
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
				if((!Sense.canBeSeenBy(this,mob))
				   &&((msg.sourceMajor()&CMMsg.MASK_GENERAL)==0)
				   &&(amWearingAt(Item.INVENTORY)))
				{
					mob.tell("You can't see that.");
					return false;
				}
				if((!amWearingAt(Item.INVENTORY))&&(!Sense.isRemovable(this)))
				{
					if(amWearingAt(Item.WIELD)||amWearingAt(Item.HELD))
					{
						mob.tell("You can't seem to let go of "+name()+".");
						return false;
					}
					mob.tell("You can't seem to remove "+name()+".");
					return false;
				}
				return true;
			}
			if(this instanceof Container)
				return true;
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_BUY:
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
				mob.tell("You don't have that.");
				return false;
			}
			if(!Sense.isUltimatelyDroppable(this))
			{
				mob.tell("You can't seem to let go of "+name()+".");
				return false;
			}
			return true;
		case CMMsg.TYP_THROW:
			if(envStats().weight()>(mob.maxCarry()/5))
			{
				mob.tell(name()+" is too heavy to throw.");
				return false;
			}
			if(!Sense.isUltimatelyDroppable(this))
			{
				mob.tell("You can't seem to let go of "+name()+".");
				return false;
			}
			return true;
		case CMMsg.TYP_BUY:
		case CMMsg.TYP_SELL:
		case CMMsg.TYP_VALUE:
		case CMMsg.TYP_VIEW:
				return true;
		case CMMsg.TYP_OPEN:
		case CMMsg.TYP_CLOSE:
		case CMMsg.TYP_LOCK:
		case CMMsg.TYP_PUT:
		case CMMsg.TYP_UNLOCK:
			if(this instanceof Container)
				return true;
			break;
		case CMMsg.TYP_DELICATE_HANDS_ACT:
		case CMMsg.TYP_JUSTICE:
		case CMMsg.TYP_WAND_USE:
		case CMMsg.TYP_FIRE: // lighting
		case CMMsg.TYP_WATER: // rust
		case CMMsg.TYP_CAST_SPELL:
		case CMMsg.TYP_POISON: // for use poison
			return true;
		case CMMsg.TYP_FILL:
			if(this instanceof Drink)
				return true;
			if(this instanceof Lantern)
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
		case CMMsg.TYP_WRITE:
			if((Sense.isReadable(this))&&(!(this instanceof Scroll)))
			{
				if(msg.targetMessage().trim().length()==0)
				{
					mob.tell("Write what on "+name()+"?");
					return false;
				}
				return true;
			}
			mob.tell("You can't write on "+name()+".");
			return false;
		default:
			break;
		}
		mob.tell(mob,this,null,"You can't do that to <T-NAMESELF>.");
		return false;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		// the order that these things are checked in should
		// be holy, and etched in stone.
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)
				B.executeMsg(this,msg);
		}

		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.executeMsg(this,msg);
		}

		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_EXAMINESOMETHING:
			if(!(this instanceof Container))
			{
				if(Sense.canBeSeenBy(this,mob))
				{
					if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
						mob.tell(ID()+"\n\rRejuv :"+baseEnvStats().rejuv()+"\n\rUses  :"+usesRemaining()+"\n\rHeight:"+baseEnvStats().height()+"\n\rAbilty:"+baseEnvStats().ability()+"\n\rLevel :"+baseEnvStats().level()+"\n\rTime  : "+dispossessionTimeLeftString()+"\n\r"+description()+"\n\r"+"\n\rMisc  :'"+text());
					else
					if(description().length()==0)
						mob.tell("You don't see anything special about "+this.name());
					else
						mob.tell(description());
				}
				else
					mob.tell("You can't see that!");
			}
			return;
		case CMMsg.TYP_READSOMETHING:
			if(!(this instanceof LandTitle))
			{
				if(Sense.canBeSeenBy(this,mob))
				{
					if((Sense.isReadable(this))&&(readableText()!=null)&&(readableText().length()>0))
					{
						if(readableText().startsWith("FILE=")
							||readableText().startsWith("FILE="))
						{
							StringBuffer buf=Resources.getFileResource(readableText().substring(5));
							if((buf!=null)&&(buf.length()>0))
								mob.tell("It says '"+buf.toString()+"'.");
							else
								mob.tell("There is nothing written on "+name()+".");
						}
						else
							mob.tell("It says '"+readableText()+"'.");
					}
					else
						mob.tell("There is nothing written on "+name()+".");
				}
				else
					mob.tell("You can't see that!");
			}
			return;
		case CMMsg.TYP_HOLD:
			if((canWear(mob,Item.HELD))&&(fitsOn(Item.HELD)))
			{
				wearAt(Item.HELD);
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
			break;
		case CMMsg.TYP_WEAR:
			if(canWear(mob,0))
			{
				wearIfPossible(mob);
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
			break;
		case CMMsg.TYP_WIELD:
			if((canWear(mob,Item.WIELD))&&(fitsOn(Item.WIELD)))
			{
				wearAt(Item.WIELD);
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
			break;
		case CMMsg.TYP_GET:
			if(!(this instanceof Container))
			{
				setContainer(null);
				if(Sense.isHidden(this))
					baseEnvStats().setDisposition(baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));
				if(mob.location().isContent(this))
					mob.location().delItem(this);
				if(!mob.isMine(this))
				{
					mob.addInventory(this);
					if(Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.envStats().setWeight(mob.envStats().weight()+envStats().weight());
				}
				unWear();
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_REMOVE:
			if(!(this instanceof Container))
			{
				unWear();
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
			}
			break;
		case CMMsg.TYP_THROW:
			if(mob.isMine(this)
			   &&(msg.tool()!=null)
			   &&(msg.tool() instanceof Room))
			{
				mob.delInventory(this);
				if(!((Room)msg.tool()).isContent(this))
					((Room)msg.tool()).addItemRefuse(this,Item.REFUSE_PLAYER_DROP);
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
				{
					((Room)msg.tool()).recoverRoomStats();
					if(mob.location()!=msg.tool())
						mob.location().recoverRoomStats();
				}
			}
			unWear();
			setContainer(null);
			break;
		case CMMsg.TYP_DROP:
			if(mob.isMine(this))
			{
				mob.delInventory(this);
				if(!mob.location().isContent(this))
					mob.location().addItemRefuse(this,Item.REFUSE_PLAYER_DROP);
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
			}
			unWear();
			setContainer(null);
			break;
		case CMMsg.TYP_WRITE:
			if(Sense.isReadable(this))
				setReadableText((readableText()+" "+msg.targetMessage()).trim());
			break;
		case CMMsg.TYP_DEATH:
			destroy();
			break;
		default:
			break;
		}
	}

	public void stopTicking(){destroyed=true;}
	public void destroy()
	{
		myContainer=null;
		for(int a=numEffects()-1;a>=0;a--)
		{
			Ability aff=fetchEffect(a);
			if((aff!=null)&&(!(aff.ID().equals("ItemRejuv"))))
				aff.unInvoke();
		}
		for(int b=numBehaviors()-1;b>=0;b--)
			delBehavior(fetchBehavior(b));
		
		riding=null;
		destroyed=true;

		if(owner!=null)
		{
			if (owner instanceof Room)
			{
				Room thisRoom=(Room)owner;
				for(int r=thisRoom.numItems()-1;r>=0;r--)
				{
					Item thisItem = thisRoom.fetchItem(r);
					if((thisItem!=null)
					&&(thisItem.container()!=null)
					&&(thisItem.container()==this))
						thisItem.destroy();
				}
				thisRoom.delItem(this);
			}
			else
			if (owner instanceof MOB)
			{
				MOB mob=(MOB)owner;
				for(int r=mob.inventorySize()-1;r>=0;r--)
				{
					Item thisItem = mob.fetchInventory(r);
					if((thisItem!=null)
					&&(thisItem.container()!=null)
					&&(thisItem.container()==this))
						thisItem.destroy();
				}
				mob.delInventory(this);
			}
		}
		recoverEnvStats();
	}

	public void removeFromOwnerContainer()
	{
		myContainer=null;

		if(owner==null) return;

		if (owner instanceof Room)
		{
			Room thisRoom=(Room)owner;
			for(int r=thisRoom.numItems()-1;r>=0;r--)
			{
				Item thisItem = thisRoom.fetchItem(r);
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
			MOB mob=(MOB)owner;
			for(int r=mob.inventorySize()-1;r>=0;r--)
			{
				Item thisItem = mob.fetchInventory(r);
				if((thisItem!=null)
				&&(thisItem.container()!=null)
				&&(thisItem.container()==this))
					thisItem.removeFromOwnerContainer();
			}
			mob.delInventory(this);
		}
		recoverEnvStats();
	}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(affects==null) affects=new Vector();
		if(affects.contains(to)) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(affects==null) affects=new Vector();
		if(affects.contains(to)) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		if(affects==null) return;
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numEffects()
	{
		if(affects==null) return 0;
		return affects.size();
	}
	public Ability fetchEffect(int index)
	{
		if(affects==null) return null;
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		if(affects==null) return null;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
				return A;
		}
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		if(behaviors==null) behaviors=new Vector();
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B.ID().equals(to.ID()))
				return;
		}

		// first one! so start ticking...
		if(behaviors.size()==0)
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_ITEM_BEHAVIOR,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		if(behaviors==null) return;
		behaviors.removeElement(to);
		if(behaviors.size()==0)
			CMClass.ThreadEngine().deleteTick(this,MudHost.TICK_ITEM_BEHAVIOR);
	}
	public int numBehaviors()
	{
		if(behaviors==null) return 0;
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		if(behaviors==null) return null;
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		if(behaviors==null) return null;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}
	protected String tackOns()
	{
		String identity="";
		if(numEffects()>0)
			identity+="\n\rHas the following magical properties: ";
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.accountForYourself().length()>0))
				identity+="\n\r"+A.accountForYourself();
		}
		return identity;
	}

	public int maxRange(){return 0;}
	public int minRange(){return 0;}
	protected static String[] CODES={"CLASS","USES","LEVEL","ABILITY","TEXT"};
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+usesRemaining();
		case 2: return ""+baseEnvStats().ability();
		case 3: return ""+baseEnvStats().level();
		case 4: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setUsesRemaining(Util.s_int(val)); break;
		case 2: baseEnvStats().setLevel(Util.s_int(val)); break;
		case 3: baseEnvStats().setAbility(Util.s_int(val)); break;
		case 4: setMiscText(val); break;
		}
	}
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdItem)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
