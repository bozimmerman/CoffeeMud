package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class StdExit implements Exit
{
	public String ID(){	return "StdExit";}

	protected EnvStats envStats=new DefaultEnvStats();
	protected EnvStats baseEnvStats=new DefaultEnvStats();
	protected boolean isOpen=true;
	protected boolean isLocked=false;
	protected String miscText="";
	protected String imageName="";
	protected Vector affects=null;
	protected Vector behaviors=null;

	public StdExit()
	{
		isOpen=!defaultsClosed();
		isLocked=defaultsLocked();
	}

	public String Name(){ return "a walkway";}
	public boolean hasADoor(){return false;}
	public boolean hasALock(){return false;}
	public boolean defaultsLocked(){return false;}
	public boolean defaultsClosed(){return false;}
	public String displayText(){ return "";}
	public String description(){ return "";}
	public String doorName(){return "door";}
	public String closedText(){return "a closed door";}
	public String closeWord(){return "close";}
	public String openWord(){return "open";}
	public long getTickStatus(){return Tickable.STATUS_NOT;}

	public void setName(String newName){}
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
		return Name();
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
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=newBaseEnvStats.cloneStats();
	}

	public String image(){return imageName;}
	public void setImage(String newImage){imageName=newImage;}
	
	public Environmental newInstance()
	{
		try{
			return (Environmental)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdExit();
	}
	public boolean isGeneric(){return false;}
	protected void cloneFix(Exit E)
	{
		baseEnvStats=E.baseEnvStats().cloneStats();
		envStats=E.envStats().cloneStats();

		affects=null;
		behaviors=null;
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				addBehavior(B.copyOf());
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
	public void setMiscText(String newMiscText){miscText=newMiscText;}
	public String text(){return miscText;}

	public void setDisplayText(String newDisplayText){}
	public void setDescription(String newDescription){}
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
		String mountStr=ladder.mountString(CMMsg.TYP_MOUNT,mob);
		FullMsg msg=new FullMsg(mob,ladder,null,CMMsg.MSG_MOUNT,"<S-NAME> "+mountStr+" <T-NAMESELF>.");
		Room room=(Room)((Item)ladder).owner();
		if(mob.location()==room) room=null;
		if((mob.location().okMessage(mob,msg))
		&&((room==null)||(room.okMessage(mob,msg))))
		{
			mob.location().send(mob,msg);
			if(room!=null)
				room.sendOthers(mob,msg);
		}
	}

	protected final String closeWordPastTense()
	{
		if(closeWord().length()==0)
			return "closed";
		else
		if(Util.isVowel(closeWord().charAt(closeWord().length()-1)))
			return closeWord()+"d";
		else
			return closeWord()+"ed";
	}
	
	protected final String openWordPastTense()
	{
		if(openWord().length()==0)
			return "opened";
		else
		if(Util.isVowel(closeWord().charAt(closeWord().length()-1)))
			return openWord()+"d";
		else
			return openWord()+"ed";
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
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
		if((!msg.amITarget(this))&&(msg.tool()!=this))
			return true;
		else
		if(msg.targetCode()==CMMsg.NO_EFFECT)
			return true;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
        case CMMsg.TYP_EXAMINE:
		case CMMsg.TYP_READ:
		case CMMsg.TYP_OK_VISUAL:
		case CMMsg.TYP_KNOCK:
		case CMMsg.TYP_OK_ACTION:
			return true;
		case CMMsg.TYP_ENTER:
			if((hasADoor())&&(!isOpen())&&(mob.envStats().height()>=0))
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
				mob.tell("The "+doorName()+" is "+closeWordPastTense()+".");
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
				if(msg.target() instanceof Room)
					ladder=findALadder(mob,(Room)msg.target());
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
		case CMMsg.TYP_LEAVE:
		case CMMsg.TYP_FLEE:
			return true;
		case CMMsg.TYP_CLOSE:
			if(isOpen)
			{
				if(!hasADoor())
				{
					mob.tell("There is nothing to "+closeWord()+"!");
					return false;
				}
				return true;
			}
			mob.tell("The "+doorName()+" is already "+closeWordPastTense()+".");
			return false;
		case CMMsg.TYP_OPEN:
			if(!hasADoor())
			{
				mob.tell("There is nothing to "+openWord()+" that way!");
				return false;
			}
			if(isOpen())
			{
				mob.tell("The "+doorName()+" is already "+openWord()+"!");
				return false;
			}
			if(isLocked()&&hasALock())
			{
				mob.tell("The "+doorName()+" is locked.");
				return false;
			}
			return true;
		case CMMsg.TYP_PUSH:
			if((isOpen())||(!hasADoor()))
			{
				mob.tell("There is nothing to push over there.");
				return false;
			}
			return true;
		case CMMsg.TYP_DELICATE_HANDS_ACT:
		case CMMsg.TYP_JUSTICE:
		case CMMsg.TYP_CAST_SPELL:
		case CMMsg.TYP_SPEAK:
			return true;
		case CMMsg.TYP_PULL:
			if((isOpen())||(!hasADoor()))
			{
				mob.tell("There is nothing to pull over there.");
				return false;
			}
			return true;
		case CMMsg.TYP_LOCK:
			if(!hasADoor())
			{
				mob.tell("There is nothing to lock that way!");
				return false;
			}
		case CMMsg.TYP_UNLOCK:
			if(!hasADoor())
			{
				mob.tell("There is nothing to unlock that way!");
				return false;
			}
			if(isOpen())
			{
				mob.tell("The "+doorName()+" is already "+openWord()+"!");
				return false;
			}
			else
			if(!hasALock())
			{
				mob.tell("There is no lock!");
				return false;
			}
			else
			{
				if((!isLocked())&&(msg.targetMinor()==CMMsg.TYP_UNLOCK))
				{
					mob.tell("The "+doorName()+" is not locked.");
					return false;
				}
				else
				if((isLocked())&&(msg.targetMinor()==CMMsg.TYP_LOCK))
				{
					mob.tell("The "+doorName()+" is already locked.");
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
		if(msg.amITarget(this))
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
				Say.append("^H("+CMMap.getExtendedRoomID(room)+")^? "+room.roomTitle()+Sense.colorCodes(room,mob)+" ");
			Say.append("via ^H("+ID()+")^? "+(isOpen()?displayText():closedText()));
		}
		else
		if(((Sense.canBeSeenBy(this,mob))||(isOpen()&&hasADoor()))
		&&(Sense.isSeen(this)))
			if(isOpen())
			{
				if((room!=null)&&(!Sense.canBeSeenBy(room,mob)))
					Say.append("darkness");
				else
				if(displayText().length()>0)
					Say.append(displayText()+Sense.colorCodes(this,mob));
				else
				if(room!=null)
					Say.append(room.roomTitle()+Sense.colorCodes(room,mob));
			}
			else
			if((Sense.canBeSeenBy(this,mob))&&(closedText().trim().length()>0))
				Say.append(closedText()+Sense.colorCodes(this,mob));
		return Say;
	}

    private static boolean isAClearView(MOB mob, Room room, Exit exit)
    {
        if((room!=null)
        &&(exit!=null)
        &&(exit.isOpen())
        &&(Sense.canBeSeenBy(room,mob))
        &&(Sense.canBeSeenBy(exit,mob)))
        {
            int domain=room.domainType();
            switch(domain)
            {
            case Room.DOMAIN_INDOORS_AIR:
            case Room.DOMAIN_INDOORS_UNDERWATER:
            case Room.DOMAIN_OUTDOORS_AIR:
            case Room.DOMAIN_OUTDOORS_UNDERWATER:
            {
                int weather=room.getArea().getClimateObj().weatherType(room);
                if((weather!=Climate.WEATHER_BLIZZARD)&&(weather!=Climate.WEATHER_DUSTSTORM))
                    return true;
                break;
            }
            }
        }
        return false;
    }
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
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
		if((!msg.amITarget(this))&&(msg.tool()!=this))
			return;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
        case CMMsg.TYP_EXAMINE:
			if(Sense.canBeSeenBy(this,mob))
			{
				if(description().trim().length()>0)
					mob.tell(description());
				else
				if(mob.location()!=null)
				{
                    Room room=null;
                    int direction=-1;
                    if(msg.tool() instanceof Room)
                        room=(Room)msg.tool();
                    else
					for(int r=0;r<Directions.NUM_DIRECTIONS;r++)
						if(mob.location().getExitInDir(r)==this)
						{
							room=mob.location().getRoomInDir(r);
							break;
						}
                    if(room!=null)
                    for(int r=0;r<Directions.NUM_DIRECTIONS;r++)
                        if((mob.location().getRoomInDir(r)==room)
                        &&((mob.location().getExitInDir(r)==this)))
                            direction=r;
					mob.tell(viewableText(mob,room).toString());
                    if(isAClearView(mob,room,this)&&(direction>=0))
                    {
                        Vector view=null;
                        Vector items=new Vector();
                        if(room.getGridParent()!=null)
                            view=room.getGridParent().getAllRooms();
                        else
                        {
                            view=new Vector();
                            view.addElement(room);
                            for(int i=0;i<5;i++)
                            {
                                room=room.getRoomInDir(direction);
                                Exit E=room.getExitInDir(direction);
                                if((room!=null)&&(isAClearView(mob,room,E)))
                                    view.addElement(room);
                            }
                        }
                        Environmental E=null;
                        for(int r=0;r<view.size();r++)
                        {
                            room=(Room)view.elementAt(r);
                            for(int i=0;i<room.numItems();i++)
                            {
                                E=room.fetchItem(i);
                                if(E!=null) items.addElement(E);
                            }
                            for(int i=0;i<room.numInhabitants();i++)
                            {
                                E=room.fetchInhabitant(i);
                                if(E!=null) items.addElement(E);
                            }
                        }
                        StringBuffer seenThatWay=CMLister.lister(msg.source(),items,true,"","",false,true);
                        if(seenThatWay.length()>0)
                            mob.tell("Yonder, you can also see: "+seenThatWay.toString());
                    }
				}
				else
					mob.tell("You don't see anything special.");
				if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
					mob.tell("Misc   : "+text());
			}
			else
				mob.tell("You can't see that way!");
			return;
		case CMMsg.TYP_READ:
			if(Sense.canBeSeenBy(this,mob))
			{
				if((isReadable())&&(readableText()!=null)&&(readableText().length()>0))
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
			return;
		case CMMsg.TYP_CLOSE:
			if((!hasADoor())||(!isOpen())) return;
			isOpen=false;
			break;
		case CMMsg.TYP_OPEN:
			if((!hasADoor())||(isOpen())) return;
			if(defaultsClosed()||defaultsLocked())
				CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_EXIT_REOPEN,openDelayTicks());
			isLocked=false;
			isOpen=true;
			break;
		case CMMsg.TYP_LOCK:
			if((!hasADoor())||(!hasALock())||(isLocked())) return;
			isOpen=false;
			isLocked=true;
			break;
		case CMMsg.TYP_PULL:
		case CMMsg.TYP_PUSH:
			mob.tell("It doesn't appear to be doing any good.");
			break;
		case CMMsg.TYP_UNLOCK:
			if((!hasADoor())||(!hasALock())||(isOpen())||(!isLocked()))
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
		if(tickID==MudHost.TICK_EXIT_REOPEN)
		{
			if(defaultsClosed())
				isOpen=false;
			if(defaultsLocked())
			{
				isOpen=false;
				isLocked=true;
			}
			return false;
		}
		else
		if(tickID==MudHost.TICK_EXIT_BEHAVIOR)
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
			while(a<numEffects())
			{
				Ability A=fetchEffect(a);
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
	public void setDoorsNLocks(boolean newHasADoor,
								  boolean newIsOpen,
								  boolean newDefaultsClosed,
								  boolean newHasALock,
								  boolean newIsLocked,
								  boolean newDefaultsLocked)
	{
		isOpen=newIsOpen;
		isLocked=newIsLocked;
	}

	public String readableText(){ return (isReadable()?miscText:"");}
	public boolean isReadable(){ return false;}
	public void setReadable(boolean isTrue){}
	public void setReadableText(String text) { miscText=temporaryDoorLink()+text; }
	public void setExitParams(String newDoorName, String newCloseWord, String newOpenWord, String newClosedText){}
	public String keyName()	{ return (hasALock()?miscText:""); }
	public void setKeyName(String newKeyName){miscText=temporaryDoorLink()+newKeyName;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{}//exits will never be asked this, so this method should always do NOTHING
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}//exits will never be asked this, so this method should always do NOTHING
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}//exits will never be asked this, so this method should always do NOTHING

	public String temporaryDoorLink(){
		if(miscText.startsWith("{#"))
		{
			int x=miscText.indexOf("#}");
			if(x>=0)
				return miscText.substring(2,x);
		}
		return "";
	}
	public void setTemporaryDoorLink(String link)
	{
		if(miscText.startsWith("{#"))
		{
			int x=miscText.indexOf("#}");
			if(x>=0) miscText=miscText.substring(x+2);
		}
		if(link.length()>0)
			miscText="{#"+link+"#}"+miscText;
	}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(affects==null) affects=new Vector();
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(affects==null) affects=new Vector();
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
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
		if(behaviors==null)
			behaviors=new Vector();
		if(to==null) return;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		// first one! so start ticking...
		if(behaviors.size()==0)
			CMClass.ThreadEngine().startTickDown(this,MudHost.TICK_EXIT_BEHAVIOR,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		if(behaviors==null) return;
		behaviors.removeElement(to);
		if(behaviors.size()==0)
			CMClass.ThreadEngine().deleteTick(this,MudHost.TICK_EXIT_BEHAVIOR);
	}

	public int numBehaviors()
	{
		if(behaviors==null) return 0;
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		if(behaviors==null)
			return null;
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		if(behaviors==null)
			return null;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}
	public int openDelayTicks()	{ return 45;}
	public void setOpenDelayTicks(int numTicks){}

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
