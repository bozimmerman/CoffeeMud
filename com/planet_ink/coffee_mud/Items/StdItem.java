package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class StdItem implements Item
{
	protected String 	myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	protected String 	name="an ordinary item";
	protected String	displayText="a nondescript item sits here doing nothing.";
	protected String 	description="It looks like something.";
	protected Item 		myContainer=null;
	protected int 		myUses=Integer.MAX_VALUE;
	protected long 		myWornCode=Item.INVENTORY;
	protected String 	miscText="";
	protected String	secretIdentity=null;
	protected int 		capacity=0;
	protected boolean	wornLogicalAnd=false;
	protected long 		properWornBitmap=Item.HELD;
	protected boolean 	isAContainer=false;
	protected int		baseGoldValue=0;
	protected boolean	isReadable=false;
	protected boolean	isGettable=true;
	protected boolean	isDroppable=true;
	protected boolean	isRemovable=true;
	protected boolean	isTrapped=false;
	protected int		goldValue=0;
	protected int		material=EnvResource.RESOURCE_COTTON;
	protected Environmental owner=null;
	protected Calendar possessionTime=null;


	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();

	protected boolean destroyed=false;

	public StdItem()
	{
		baseEnvStats().setWeight(1);
		baseEnvStats().setArmor(0);
	}
	public boolean isGeneric(){return false;}

	public String ID()
	{
		return myID;
	}
	public String name(){ return name;}
	public void setName(String newName){name=newName;}
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
		goldValue=baseGoldValue+(10*envStats().ability());
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
		if(envStats().ability()>0)
			envStats().setDisposition(envStats().disposition()|EnvStats.IS_BONUS);
	}

	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}
	public boolean isAContainer()
	{
		return isAContainer;
	}
	public Environmental newInstance()
	{
		return new StdItem();
	}
	public boolean subjectToWearAndTear(){return false;}
	private void cloneFix(Item E)
	{
		destroyed=false;
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();

		affects=new Vector();
		behaviors=new Vector();
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)	addBehavior(B);
		}
			
		for(int a=0;a<E.numAffects();a++)
		{
			Ability A=E.fetchAffect(a);
			if((A!=null)&&(!A.canBeUninvoked()))
				addAffect((Ability)A.copyOf());
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

	public Environmental owner(){return owner;}
	public void setOwner(Environmental E)
	{
		owner=E;
		if((E!=null)&&(!(E instanceof Room)))
			setPossessionTime(null);
		recoverEnvStats();
	}
	public Calendar possessionTime()
	{
		return possessionTime;
	}
	public void setPossessionTime(Calendar time)
	{
		possessionTime=time;
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
	public boolean canBeWornAt(long wornCode)
	{
		if(wornCode==0)
			return true;
		return ((properWornBitmap & wornCode)==wornCode);
	}
	public void wearIfPossible(MOB mob)
	{
		if(canWear(mob))
			for(int i=0;i<20;i++)
			{
				long wornCode=1<<i;
				if((this.canBeWornAt(wornCode))&&(!mob.amWearingSomethingHere(wornCode)))
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
			remove();
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
		
		if(!wornLogicalAnd)
		{
			for(int i=0;i<20;i++)
			{
				long wornCode=1<<i;
				if(canBeWornAt(wornCode))
				{
					couldHaveBeenWornAt=wornCode;
					if(!mob.amWearingSomethingHere(wornCode))
						return 0;
				}
			}
			return couldHaveBeenWornAt;
		}
		else
		{
			for(int i=0;i<20;i++)
			{
				long wornCode=1<<i;
				if((canBeWornAt(wornCode))&&(mob.amWearingSomethingHere(wornCode)))
					return wornCode;
			}
			return 0;
		}
	}
	
	public boolean canWear(MOB mob)
	{
		if(whereCantWear(mob)==0)
			return true;
		return false;
	}

	public long rawWornCode()
	{
		return myWornCode;
	}
	public void setRawWornCode(long newValue)
	{
		myWornCode=newValue;
	}

	public void remove()
	{
		setRawWornCode(Item.INVENTORY);
		recoverEnvStats();
	}


	public int capacity()
	{
		return capacity;
	}
	public void setCapacity(int newValue)
	{
		capacity=newValue;
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
		return goldValue;
	}
	public int baseGoldValue(){return baseGoldValue;}
	public void setBaseValue(int newValue)
	{
		baseGoldValue=newValue;
	}

	public String readableText(){return miscText;}
	public void setReadableText(String text){miscText=text;}
	public boolean isReadable(){return isReadable;}
	public void setReadable(boolean isTrue){isReadable=isTrue;}
	public boolean isGettable(){return isGettable;}
	public void setGettable(boolean isTrue){isGettable=isTrue;}
	public boolean isDroppable(){return isDroppable;}
	public void setDroppable(boolean isTrue){isDroppable=isTrue;}
	public boolean isRemovable(){return isRemovable;}
	public void setRemovable(boolean isTrue){isRemovable=isTrue;}
	public boolean isTrapped(){return isTrapped;}
	public void setTrapped(boolean isTrue){isTrapped=isTrue;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(Sense.isLight(this))
		{
			if((!(affected instanceof Room))&&(rawWornCode()!=Item.INVENTORY))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHT);
			if(Sense.isInDark(affected))
				affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
		}
		if(!this.amWearingAt(Item.FLOATING_NEARBY))
			affectableStats.setWeight(affectableStats.weight()+envStats().weight());
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}
	public void setMiscText(String newText)
	{
		miscText=newText;
	}
	public String text()
	{
		return miscText;
	}

	public boolean tick(int tickID)
	{
		if(destroyed)
			return false;

		if(tickID==Host.ITEM_BEHAVIOR_TICK)
		{
			if(behaviors.size()==0) return false;
			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(this,tickID);
			}
		}
		else
		{
			int a=0;
			while(a<numAffects())
			{
				Ability A=fetchAffect(a);
				if(A!=null)
				{
					int s=affects.size();
					if(!A.tick(tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
		}
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
		return description+"\n\rLevel: "+envStats().level()+tackOns();
	}

	public void setSecretIdentity(String newIdentity)
	{
		if((newIdentity==null)
		||(newIdentity.trim().equalsIgnoreCase(description))
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
		return description;
	}
	public void setDescription(String newDescription)
	{
		description=newDescription;
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
	

	public boolean okAffect(Affect affect)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okAffect(this,affect)))
				return false;
		}

		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(!A.okAffect(affect)))
				return false;
		}

		MOB mob=affect.source();
		if(!affect.amITarget(this))
			return true;
		else
		if(affect.targetCode()==Affect.NO_EFFECT)
			return true;
		else
		if((Util.bset(affect.targetCode(),Affect.MASK_MAGIC))&&(!this.isGettable()))
		{
			mob.tell("Please don't do that.");
			return false;
		}
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_EXAMINESOMETHING:
		case Affect.TYP_READSOMETHING:
		case Affect.TYP_SPEAK:
		case Affect.TYP_OK_ACTION:
		case Affect.TYP_OK_VISUAL:
		case Affect.TYP_DEATH:
		case Affect.TYP_NOISE:
			return true;
		case Affect.TYP_HOLD:
			if(!canBeWornAt(Item.HELD))
			{
				StringBuffer msg=new StringBuffer("You can't hold "+name()+".");
				if(canBeWornAt(Item.WIELD))
					msg.append("Try WIELDing it.");
				else
				if(properWornBitmap>0)
					msg.append("Try WEARing it.");
				mob.tell(msg.toString());
				return false;
			}
			if(!canWear(mob))
			{
				mob.tell("Your hands are full.");
				return false;
			}
			if(!mob.charStats().getMyRace().canWear(this))
			{
				mob.tell("You lack the anatomy to hold "+name()+".");
				return false;
			}
			if(envStats().level()>mob.envStats().level())
			{
				mob.tell("That looks too advanced for you.");
				return false;
			}
			return true;
		case Affect.TYP_WEAR:
			if(properWornBitmap==0)
			{
				mob.tell("You can't wear "+name()+".");
				return false;
			}
			if(!amWearingAt(Item.INVENTORY))
			{
				mob.tell("You are already wearing "+name()+".");
				return false;
			}
			if(!canWear(mob))
			{	
				long cantWearAt=whereCantWear(mob);
				Item alreadyWearing=mob.fetchWornItem(cantWearAt);
				if(alreadyWearing!=null)
					mob.tell("You are already wearing "+alreadyWearing.name()+" on your "+Sense.wornLocation(cantWearAt)+".");
				else
					mob.tell("You are already wearing something on your "+Sense.wornLocation(cantWearAt)+".");
				return false;
			}
			if(!mob.charStats().getMyRace().canWear(this))
			{
				mob.tell("You lack the anatomy to wear "+name()+".");
				return false;
			}
			if(envStats().level()>mob.envStats().level())
			{
				mob.tell("That looks too advanced for you.");
				return false;
			}
			return true;
		case Affect.TYP_WIELD:
			if(!canBeWornAt(Item.WIELD))
			{
				mob.tell("You can't wield "+name()+" as a weapon.");
				return false;
			}
			if(amWearingAt(Item.WIELD))
			{
				mob.tell("That's already being wielded.");
				return false;
			}
			if(mob.amWearingSomethingHere(Item.WIELD))
			{
				Item alreadyWearing=mob.fetchWornItem(Item.WIELD);
				if(alreadyWearing!=null)
					mob.tell("You are already wielding "+alreadyWearing.name()+".");
				else
					mob.tell("You are already wielding something.");
				return false;
			}
			if(!canWear(mob))
			{	
				mob.tell("You can't wield "+name()+", your hands are full.");
				return false;
			}
			if(!mob.charStats().getMyRace().canWear(this))
			{
				mob.tell("You lack the anatomy to wield "+name()+".");
				return false;
			}
			if(envStats().level()>mob.envStats().level())
			{
				mob.tell("That looks too advanced for you.");
				return false;
			}
			return true;
		case Affect.TYP_GET:
			if((affect.tool()==null)||(affect.tool() instanceof MOB))
			{
				if(!Sense.canBeSeenBy(this,mob))
				{
					mob.tell("You can't see that.");
					return false;
				}
				if(mob.envStats().level()<envStats().level()-10)
				{
					mob.tell(name()+" is too powerful to endure possessing it.");
					return false;
				}
				if((envStats().weight()>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
				{
					mob.tell(name()+" is too heavy.");
					return false;
				}
				if((!amWearingAt(Item.INVENTORY))&&(!isRemovable))
				{
					if(amWearingAt(Item.WIELD)||amWearingAt(Item.HELD))
					{
						mob.tell("You can't seem to let go of "+name()+".");
						return false;
					}
					mob.tell("You can't seem to remove "+name()+".");
					return false;
				}
				if(!isGettable)
				{
					mob.tell("You can't get "+name()+".");
					return false;
				}
				if((mob.riding()!=null)&&(mob.riding()==this))
				{
					mob.tell("You are "+mob.riding().stateString()+" "+name()+"!");
					return false;
				}
				return true;
			}
			if(this instanceof Container)
				return true;
			switch(affect.sourceMinor())
			{
			case Affect.TYP_BUY:
			case Affect.TYP_GET:
			case Affect.TYP_GENERAL:
			case Affect.TYP_SELL:
			case Affect.TYP_VALUE:
			case Affect.TYP_GIVE:
				return true;
			}
			break;
		case Affect.TYP_DROP:
			if(!mob.isMine(this))
			{
				mob.tell("You don't have that.");
				return false;
			}
			if(!isDroppable)
			{
				mob.tell("You can't seem to let go of "+name()+".");
				return false;
			}
			return true;
		case Affect.TYP_BUY:
		case Affect.TYP_SELL:
		case Affect.TYP_VALUE:
				return true;
		case Affect.TYP_OPEN:
		case Affect.TYP_CLOSE:
		case Affect.TYP_LOCK:
		case Affect.TYP_PUT:
		case Affect.TYP_UNLOCK:
			if(this instanceof Container)
				return true;
			break;
		case Affect.TYP_DELICATE_HANDS_ACT:
		case Affect.TYP_WAND_USE:
		case Affect.TYP_CAST_SPELL:
			return true;
		case Affect.TYP_FILL:
			if(this instanceof Drink)
				return true;
			if(this instanceof Lantern)
				return true;
			break;
		case Affect.TYP_EAT:
			if(this instanceof Food)
				return true;
			break;
		case Affect.TYP_DRINK:
			if(this instanceof Drink)
				return true;
			break;
		case Affect.TYP_WRITE:
			if((this.isReadable())&&(!(this instanceof Scroll)))
			{
				if(affect.targetMessage().trim().length()==0)
				{
					mob.tell("What what on "+name()+"?");
					return false;
				}
				return true;
			}
			mob.tell("You can't write on "+name()+".");
			return false;
		default:
			break;
		}
		mob.tell("You can't do that to "+name()+".");
		return false;
	}

	public void affect(Affect affect)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)
				B.affect(this,affect);
		}
		
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if(A!=null)
				A.affect(affect);
		}

		MOB mob=affect.source();
		if(!affect.amITarget(this))
			return;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_EXAMINESOMETHING:
			if(!(this instanceof Container))
			{
				if(Sense.canBeSeenBy(this,mob))
				{
					if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
						mob.tell(ID()+"\n\rRejuv :"+baseEnvStats().rejuv()+"\n\rUses  :"+usesRemaining()+"\n\rHeight:"+baseEnvStats().height()+"\n\rAbilty:"+baseEnvStats().ability()+"\n\rLevel :"+baseEnvStats().level()+"\n\rMisc  :'"+text()+"\n\r"+description()+"\n\r");
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
		case Affect.TYP_READSOMETHING:
			if(Sense.canBeSeenBy(this,mob))
			{
				if((isReadable)&&(readableText()!=null)&&(readableText().length()>0))
					mob.tell("It says '"+readableText()+"'.");
				else
					mob.tell("There is nothing written on "+name()+".");
			}
			else
				mob.tell("You can't see that!");
			return;
		case Affect.TYP_HOLD:
			if((this.canWear(mob))&&(this.canBeWornAt(Item.HELD)))
			{
				wearAt(Item.HELD);
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
			break;
		case Affect.TYP_WEAR:
			if(this.canWear(mob))
			{
				wearIfPossible(mob);
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
			break;
		case Affect.TYP_WIELD:
			if((this.canWear(mob))&&(this.canBeWornAt(Item.WIELD)))
			{
				wearAt(Item.WIELD);
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
			}
			break;
		case Affect.TYP_GET:
			if(!(this instanceof Container))
			{
				setContainer(null);
				if(Sense.isHidden(this))
					baseEnvStats().setDisposition(baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));
				if(mob.location().isContent(this))
					mob.location().delItem(this);
				if(!mob.isMine(this))
					mob.addInventory(this);
				remove();
				mob.location().recoverRoomStats();
			}
			break;
		case Affect.TYP_DROP:
			if(mob.isMine(this))
			{
				mob.delInventory(this);
				if(!mob.location().isContent(this))
				{
					mob.location().addItem(this);
					setPossessionTime(Calendar.getInstance());
				}
				mob.location().recoverRoomStats();
			}
			remove();
			setContainer(null);
			break;
		case Affect.TYP_WRITE:
			if(this.isReadable())
				setReadableText(affect.targetMessage());
			break;
		case Affect.TYP_DEATH:
			destroyThis();
			break;
		default:
			break;
		}
	}

	public void destroyThis()
	{
		myContainer=null;
		if(owner==null) return;
		for(int a=this.numAffects()-1;a>=0;a--)
		{
			Ability aff=fetchAffect(a);
			if((aff!=null)&&(!(aff.ID().equals("ItemRejuv"))))
				aff.unInvoke();
		}

		destroyed=true;

		if (owner instanceof Room)
		{
			Room thisRoom=(Room)owner;
			for(int r=thisRoom.numItems()-1;r>=0;r--)
			{
				Item thisItem = thisRoom.fetchItem(r);
				if((thisItem!=null)
				   &&(thisItem.container()!=null)
				   &&(thisItem.container()==this))
					thisItem.destroyThis();
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
				if((thisItem!=null)&&(thisItem.container()!=null)&&(thisItem.container()==this))
					thisItem.destroyThis();
			}
			mob.delInventory(this);
		}
		recoverEnvStats();
	}

	public void removeThis()
	{
		myContainer=null;

		if(owner==null) return;
		for(int a=this.numAffects()-1;a>=0;a--)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(!A.ID().equals("ItemRejuv")))
				A.unInvoke();
		}

		if (owner instanceof Room)
		{
			Room thisRoom=(Room)owner;
			for(int r=thisRoom.numItems()-1;r>=0;r--)
			{
				Item thisItem = thisRoom.fetchItem(r);
				if((thisItem!=null)
				&&(thisItem.container()!=null)
				&&(thisItem.container()==this))
					thisItem.removeThis();
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
					thisItem.removeThis();
			}
			mob.delInventory(this);
		}
		recoverEnvStats();
	}

	public void addNonUninvokableAffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addAffect(Ability to)
	{
		if(to==null) return;
		if(affects.contains(to)) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delAffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public int numAffects()
	{
		return affects.size();
	}
	public Ability fetchAffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchAffect(String ID)
	{
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
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
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B.ID().equals(to.ID()))
				return;
		}

		// first one! so start ticking...
		if(behaviors.size()==0)
			ExternalPlay.startTickDown(this,Host.ITEM_BEHAVIOR_TICK,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
		if(behaviors.size()==0)
			ExternalPlay.deleteTick(this,Host.ITEM_BEHAVIOR_TICK);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	protected String tackOns()
	{
		String identity="";
		if(numAffects()>0)
			identity+="\n\rHas the following magical properties: ";
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(A.accountForYourself().length()>0))
				identity+="\n\r"+A.accountForYourself();
		}
		return identity;
	}

	public int maxRange(){return 0;}
	public int minRange(){return 0;}
}
