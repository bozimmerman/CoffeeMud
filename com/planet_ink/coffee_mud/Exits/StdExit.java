package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdExit implements Exit
{
	public String ID(){	return "StdExit";}
	protected String name="a walkway";
	protected String description="Looks like an ordinary path from here to there.";
	protected String displayText="";
	protected String closedText="A barrier blocks the way.";
	protected String miscText="";

	protected String doorName="door";
	protected String closeName="close";
	protected String openName="open";

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();
	protected boolean isOpen=true;
	protected boolean isLocked=false;
	protected boolean hasADoor=false;
	protected boolean doorDefaultsClosed=true;
	protected boolean hasALock=false;
	protected boolean doorDefaultsLocked=false;
	protected boolean isReadable=false;
	protected int openDelayTicks=45;

	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();

	public StdExit()
	{
	}
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
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public Environmental newInstance()
	{
		return new StdExit();
	}
	public boolean isGeneric(){return false;}
	private void cloneFix(Exit E)
	{
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();

		affects=new Vector();
		behaviors=new Vector();
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				behaviors.addElement(B);
		}

	}
	public Environmental copyOf()
	{
		try
		{
			StdExit E=(StdExit)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public String displayText()
	{ return displayText;}
	public void setDisplayText(String newDisplayText)
	{ displayText=newDisplayText;}
	public void setMiscText(String newMiscText)
	{
		miscText=newMiscText;
	}
	public String text()
	{ return miscText;}
	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	private Rideable findALadder(MOB mob, Room room)
	{
		if(room==null) return null;
		if(mob.riding()!=null) return null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I!=null)
			   &&(I instanceof Rideable)
			   &&(Sense.canBeSeenBy(I,mob))
			   &&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_LADDER))
				return (Rideable)I;
		}
		return null;
	}

	private void mountLadder(MOB mob, Rideable ladder)
	{
		String mountStr=ladder.mountString(Affect.TYP_MOUNT,mob);
		FullMsg msg=new FullMsg(mob,ladder,null,Affect.MSG_MOUNT,"<S-NAME> "+mountStr+" <T-NAMESELF>.");
		Room room=(Room)((Item)ladder).owner();
		if(mob.location()==room) room=null;
		if((mob.location().okAffect(mob,msg))
		&&((room==null)||(room.okAffect(mob,msg))))
		{
			mob.location().send(mob,msg);
			if(room!=null)
				room.sendOthers(mob,msg);
		}
	}

	public boolean okAffect(Environmental myHost, Affect affect)
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
			if((A!=null)&&(!A.okAffect(this,affect)))
				return false;
		}

		MOB mob=affect.source();
		if((!affect.amITarget(this))&&(affect.tool()!=this))
			return true;
		else
		if(affect.targetCode()==Affect.NO_EFFECT)
			return true;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_EXAMINESOMETHING:
		case Affect.TYP_READSOMETHING:
		case Affect.TYP_OK_VISUAL:
		case Affect.TYP_KNOCK:
		case Affect.TYP_OK_ACTION:
			return true;
		case Affect.TYP_ENTER:
			if((hasADoor)&&(!isOpen)&&(mob.envStats().height()>=0))
			{
				if((!Sense.canBeSeenBy(this,mob))
				&&(Sense.canSee(mob))
				&&(!Sense.isGlowing(this))
				&&((Sense.isHidden(this)&&(!Sense.canSeeHidden(mob)))
					||(Sense.isInvisible(this)&&(!Sense.canSeeInvisible(mob)))))
				{
					mob.tell("You can't go that way.");
					return false;
				}
				mob.tell("The "+doorName+" is "+closeName+"d.");
				return false;
			}
			if((Sense.isFlying(this))
			&&(!Sense.isInFlight(mob))
			&&(!Sense.isFalling(mob)))
			{
				mob.tell("You can't fly.");
				return false;
			}
			if((Sense.isClimbing(this))
			&&(!Sense.isFalling(this))
			&&(!Sense.isClimbing(mob))
			&&(!Sense.isInFlight(mob)))
			{
				Rideable ladder=null;
				if(affect.target() instanceof Room)
					ladder=findALadder(mob,(Room)affect.target());
				if(ladder!=null)
					mountLadder(mob,ladder);
				if((!Sense.isClimbing(mob))
				&&(!Sense.isFalling(mob)))
				{
					mob.tell("You need to climb that way, if you know how.");
					return false;
				}
			}
			return true;
		case Affect.TYP_LEAVE:
		case Affect.TYP_FLEE:
			return true;
		case Affect.TYP_CLOSE:
			if(isOpen)
			{
				if(!hasADoor)
				{
					mob.tell("There is nothing to "+closeName+"!");
					return false;
				}
				else
					return true;
			}
			else
			{
				mob.tell("The "+doorName+" is already "+closeName+"d.");
				return false;
			}
			//break;
		case Affect.TYP_OPEN:
			if(!hasADoor)
			{
				mob.tell("There is nothing to "+openName+" that way!");
				return false;
			}
			if(isOpen)
			{
				mob.tell("The "+doorName+" is already "+openName+"!");
				return false;
			}
			else
			{
				if(isLocked)
				{
					mob.tell("The "+doorName+" is locked.");
					return false;
				}
				else
					return true;
			}
			//break;
		case Affect.TYP_PUSH:
			if((isOpen)||(!hasADoor))
			{
				mob.tell("There is nothing to push over there.");
				return false;
			}
			return true;
		case Affect.TYP_DELICATE_HANDS_ACT:
		case Affect.TYP_JUSTICE:
		case Affect.TYP_CAST_SPELL:
			return true;
		case Affect.TYP_PULL:
			if((isOpen)||(!hasADoor))
			{
				mob.tell("There is nothing to pull over there.");
				return false;
			}
			return true;
		case Affect.TYP_LOCK:
			if(!hasADoor)
			{
				mob.tell("There is nothing to lock that way!");
				return false;
			}
		case Affect.TYP_UNLOCK:
			if(!hasADoor)
			{
				mob.tell("There is nothing to unlock that way!");
				return false;
			}
			if(isOpen)
			{
				mob.tell("The "+doorName+" is already "+openName+"!");
				return false;
			}
			else
			if(!hasALock)
			{
				mob.tell("There is no lock!");
				return false;
			}
			else
			{
				if((!isLocked)&&(affect.targetMinor()==Affect.TYP_UNLOCK))
				{
					mob.tell("The "+doorName+" is not locked.");
					return false;
				}
				else
				if((isLocked)&&(affect.targetMinor()==Affect.TYP_LOCK))
				{
					mob.tell("The "+doorName+" is already locked.");
					return false;
				}
				else
				{
					for(int i=0;i<mob.inventorySize();i++)
					{
						Item item=mob.fetchInventory(i);
						if((item!=null)
						&&(item instanceof Key)
						&&((Key)item).getKey().equals(keyName())
						&&((item.container()==null)
						   ||((item.container().container()==null)
							  &&(item.container() instanceof Container)
							  &&((((Container)item.container()).containTypes()&Container.CONTAIN_KEYS)>0)))
						&&(Sense.canBeSeenBy(item,mob)))
							return true;
					}
					mob.tell("You don't seem to have the key.");
					return false;
				}
			}
			//break;
		default:
			break;
		}
		if(affect.amITarget(this))
		{
			mob.tell("You can't do that.");
			return false;
		}
		return true;
	}

	public StringBuffer viewableText(MOB mob, Room room)
	{
		StringBuffer Say=new StringBuffer("");
		if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
		{
			if(room==null)
				Say.append("^Z(null)^.^? ");
			else
				Say.append("^H("+room.roomID()+")^? "+room.displayText()+Sense.colorCodes(room,mob)+" ");
			Say.append("via ^H("+ID()+")^? "+(isOpen()?displayText():closedText()));
		}
		else
		if(((Sense.canBeSeenBy(this,mob))||isOpen())
		&&(Sense.isSeen(this)))
			if(isOpen())
			{
				if((room!=null)&&(!Sense.canBeSeenBy(room,mob)))
					Say.append("darkness");
				else
				if(displayText().length()>0)
					Say.append(displayText());
				else
				if(room!=null)
					Say.append(room.displayText()+Sense.colorCodes(room,mob));
			}
			else
			if(Sense.canBeSeenBy(this,mob))
				Say.append(closedText()+Sense.colorCodes(this,mob));
		return Say;
	}

	public void affect(Environmental myHost, Affect affect)
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
				A.affect(this,affect);
		}

		MOB mob=affect.source();
		if((!affect.amITarget(this))&&(affect.tool()!=this))
			return;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_EXAMINESOMETHING:
			if(Sense.canBeSeenBy(this,mob))
			{
				if(description().trim().length()>0)
					mob.tell(description());
				else
				if(mob.location()!=null)
				{
					Room room=null;
					for(int r=0;r<Directions.NUM_DIRECTIONS;r++)
						if(mob.location().getExitInDir(r)==this)
						{
							room=mob.location().getRoomInDir(r);
							break;
						}
					mob.tell(this.viewableText(mob,room).toString());
				}
				else
					mob.tell("You don't see anything special.");
				if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
					mob.tell("Misc   : "+text());
			}
			else
				mob.tell("You can't see that way!");
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
		case Affect.TYP_CLOSE:
			if((!hasADoor)||(!isOpen)) return;
			isOpen=false;
			break;
		case Affect.TYP_OPEN:
			if((!hasADoor)||(isOpen)||(isLocked)) return;
			if(doorDefaultsClosed||doorDefaultsLocked)
				ExternalPlay.startTickDown(this,Host.EXIT_REOPEN,openDelayTicks);
			isLocked=false;
			isOpen=true;
			break;
		case Affect.TYP_LOCK:
			if((!hasADoor)||(!hasALock)||(isLocked)) return;
			isOpen=false;
			isLocked=true;
			break;
		case Affect.TYP_PULL:
		case Affect.TYP_PUSH:
			mob.tell("It doesn't appear to be doing any good.");
			break;
		case Affect.TYP_UNLOCK:
			if((!hasADoor)||(!hasALock)||(isOpen)||(!isLocked))
				return;
			isLocked=false;
			break;
		default:
			break;
		}
	}
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.EXIT_REOPEN)
		{
			if(doorDefaultsClosed)
				isOpen=false;
			if(doorDefaultsLocked)
			{
				isOpen=false;
				isLocked=true;
			}
			return false;
		}
		else
		if(tickID==Host.EXIT_BEHAVIOR_TICK)
		{
			for(int b=0;b<numBehaviors();b++)
			{
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}
			return true;
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
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
			return true;
		}
	}
	public boolean isOpen(){return isOpen;}
	public boolean isLocked(){return isLocked;}
	public boolean hasADoor(){return hasADoor;}
	public boolean hasALock(){return hasALock;}
	public boolean defaultsLocked(){return doorDefaultsLocked;}
	public boolean defaultsClosed(){return doorDefaultsClosed;}
	public void setDoorsNLocks(boolean newHasADoor,
								  boolean newIsOpen,
								  boolean newDefaultsClosed,
								  boolean newHasALock,
								  boolean newIsLocked,
								  boolean newDefaultsLocked)
	{
		isOpen=newIsOpen;
		isLocked=newIsLocked;
		hasADoor=newHasADoor;
		hasALock=newHasALock;
		doorDefaultsClosed=newDefaultsClosed;
		doorDefaultsLocked=newDefaultsLocked;
	}

	public String readableText(){ return (isReadable?miscText:"");}
	public boolean isReadable(){ return isReadable;}
	public void setReadable(boolean isTrue){isReadable=isTrue;}
	public void setReadableText(String text) { miscText=text; }

	public String doorName(){return doorName;}
	public String closeWord(){return closeName;}
	public String openWord(){return openName;}
	public String closedText(){return closedText;}
	public void setExitParams(String newDoorName,
							  String newCloseWord,
							  String newOpenWord,
							  String newClosedText)
	{
		doorName=newDoorName;
		closeName=newCloseWord;
		openName=newOpenWord;
		closedText=newClosedText;
	}

	public String keyName()	{ return (hasALock?miscText:""); }
	public void setKeyName(String newKeyName){miscText=newKeyName;}



	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{}//exits will never be asked this, so this method should always do NOTHING
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}//exits will never be asked this, so this method should always do NOTHING
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}//exits will never be asked this, so this method should always do NOTHING

	public void addNonUninvokableAffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addAffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numAffects();a++)
		{
			Ability A=fetchAffect(a);
			if((A!=null)&&(A==to))
				return;
		}
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
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		// first one! so start ticking...
		if(behaviors.size()==0)
			ExternalPlay.startTickDown(this,Host.EXIT_BEHAVIOR_TICK,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
		if(behaviors.size()==0)
			ExternalPlay.deleteTick(this,Host.EXIT_BEHAVIOR_TICK);
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
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}
	public int openDelayTicks()	{ return openDelayTicks;}
	public void setOpenDelayTicks(int numTicks){openDelayTicks=numTicks;}
	private static final String[] CODES={"CLASS","TEXT"};
	public String[] getStatCodes(){return CODES;}
	private int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return text();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setMiscText(val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdExit)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
