package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdExit implements Exit
{
	protected String myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
	protected boolean isTrapped=false;
	protected boolean levelRestricted=false;
	protected boolean classRestricted=false;
	protected boolean alignmentRestricted=false;
	protected int openDelayTicks=4;

	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();

	public StdExit()
	{
	}

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
		for(int a=0;a<affects.size();a++)
		{
			Ability affect=(Ability)affects.elementAt(a);
			affect.affectEnvStats(this,envStats);
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
		for(int i=0;i<E.numBehaviors();i++)
			behaviors.addElement(E.fetchBehavior(i));

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

	public boolean okAffect(Affect affect)
	{
		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=(Behavior)behaviors.elementAt(b);
			if(!B.okAffect(this,affect)) return false;
		}

		for(int i=0;i<affects.size();i++)
			if(!((Ability)fetchAffect(i)).okAffect(affect))
				return false;

		MOB mob=affect.source();
		if((!affect.amITarget(this))&&(affect.tool()!=this))
			return true;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_EXAMINESOMETHING:
		case Affect.TYP_READSOMETHING:
		case Affect.TYP_OK_VISUAL:
		case Affect.TYP_OK_ACTION:
			return true;
		case Affect.TYP_ENTER:
			if((hasADoor)&&(!isOpen))
			{
				mob.tell("The "+doorName+" is "+closeName+"d.");
				return false;
			}
			if((levelRestricted)&&(mob.envStats().level()<envStats().level()))
			{
				mob.tell("You can't go that way.");
				return false;
			}
			if((classRestricted)&&(!mob.charStats().getMyClass().ID().equalsIgnoreCase(classRestrictedName())))
			{
				mob.tell("You can't go that way.");
				return false;
			}
			if((alignmentRestricted)&&(alignmentRestrictedMask().toUpperCase().indexOf(ExternalPlay.shortAlignmentStr(mob).toUpperCase())>=0))
			{
				mob.tell("You can't go that way.");
				return false;
			}
			if((Sense.isFlying(this))&&(!Sense.isFlying(mob)))
			{
				mob.tell("You can't fly.");
				return false;
			}
			if((Sense.isClimbing(this))
			&&(mob.fetchAffect("Falling")==null)
			&&(!Sense.isClimbing(mob))
			&&(!Sense.isFlying(mob)))
			{
				mob.tell("You need to climb that way, if you know how.");
				return false;
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
						if((item instanceof Key)
						&&(item.location()==null)
						&&(Sense.canBeSeenBy(item,mob)))
						{
							if(((Key)item).getKey().equals(keyName()))
								return true;
						}
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
		if(mob.readSysopMsgs())
		{
			if(room==null)
				Say.append("^Z(null)^? ");
			else
				Say.append("^H("+room.ID()+")^? "+room.displayText()+Sense.colorCodes(room,mob)+" ");
			Say.append("via ^H("+ID()+")^? "+(isOpen()?displayText():closedText()));
		}
		else
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

	public void affect(Affect affect)
	{
		for(int b=0;b<behaviors.size();b++)
		{
			Behavior B=(Behavior)behaviors.elementAt(b);
			B.affect(this,affect);
		}

		for(int i=0;i<affects.size();i++)
			((Ability)fetchAffect(i)).affect(affect);

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
						if(mob.location().exits()[r]==this)
						{
							room=mob.location().doors()[r];
							break;
						}
					mob.tell(this.viewableText(mob,room).toString());
				}
				else
					mob.tell("You don't see anything special.");
				if(mob.readSysopMsgs())
					mob.tell("Misc   : "+text());
			}
			else
				mob.tell("You can't see that way!");
			return;
		case Affect.TYP_READSOMETHING:
			if(Sense.canBeSeenBy(this,mob))
			{
				if((isReadable||((!hasALock)&&(!classRestricted)&&(!alignmentRestricted)))&&(readableText()!=null)&&(readableText().length()>0))
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

	public boolean tick(int tickID)
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
		{
			for(int b=0;b<behaviors.size();b++)
			{
				Behavior B=(Behavior)behaviors.elementAt(b);
				B.tick(this,tickID);
			}

			int a=0;
			while(a<affects.size())
			{
				Ability A=(Ability)affects.elementAt(a);
				int s=affects.size();
				if(!A.tick(tickID))
					A.unInvoke();
				if(affects.size()==s)
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

	public boolean isTrapped() {return isTrapped;}
	public void setTrapped(boolean isTrue){isTrapped=isTrue;}

	public String readableText(){ return (isReadable?miscText:"");}
	public boolean isReadable(){ return isReadable;}
	public void setReadable(boolean isTrue){isReadable=isTrue;}
	public void setReadableText(String text) { miscText=text; }

	public boolean levelRestricted(){ return levelRestricted;}
	public void setLevelRestricted(boolean isTrue){levelRestricted=isTrue;}

	public String classRestrictedName(){ return (classRestricted?miscText:"");}
	public boolean classRestricted(){ return classRestricted;}
	public void setClassRestricted(boolean isTrue){classRestricted=isTrue;}
	public void setClassRestrictedName(String className) { miscText=className; }

	public String alignmentRestrictedMask(){ return (alignmentRestricted?miscText:"");}
	public boolean alignmentRestricted(){ return alignmentRestricted;}
	public void setAlignmentRestricted(boolean isTrue){alignmentRestricted=isTrue;}
	public void setAlignmentRestrictedMask(String alignments) { miscText=alignments; }

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
		for(int i=0;i<affects.size();i++)
			if(((Ability)affects.elementAt(i))==to)
				return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addAffect(Ability to)
	{
		if(to==null) return;
		for(int i=0;i<affects.size();i++)
			if(affects.elementAt(i)==to)
				return;
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
		if(index <numAffects())
			return (Ability)affects.elementAt(index);
		return null;
	}
	public Ability fetchAffect(String ID)
	{
		for(int a=0;a<affects.size();a++)
			if(((Ability)affects.elementAt(a)).ID().equals(ID))
			   return (Ability)affects.elementAt(a);
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(int i=0;i<behaviors.size();i++)
			if(((Behavior)behaviors.elementAt(i)).ID().equals(to.ID()))
				return;
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		if(index <numBehaviors())
			return (Behavior)behaviors.elementAt(index);
		return null;
	}
	public int openDelayTicks()	{ return openDelayTicks;}
	public void setOpenDelayTicks(int numTicks){openDelayTicks=numTicks;}
}
