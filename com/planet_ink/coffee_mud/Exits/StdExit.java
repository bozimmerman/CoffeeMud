package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class StdExit implements Exit
{
	public String ID(){	return "StdExit";}

	protected EnvStats envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	protected EnvStats baseEnvStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	protected boolean isOpen=true;
	protected boolean isLocked=false;
	protected String miscText="";
	protected String imageName=null;
	protected Vector affects=null;
	protected Vector behaviors=null;
    protected Vector scripts=null;
    protected boolean amDestroyed=false;
    protected short usage=0;
    
	public StdExit()
	{
        super();
        CMClass.bumpCounter(this,CMClass.OBJECT_EXIT);
		isOpen=!defaultsClosed();
		isLocked=defaultsLocked();
	}

    protected void finalize(){CMClass.unbumpCounter(this,CMClass.OBJECT_EXIT);}
    public void initializeClass(){}
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
	public short exitUsage(short change){
	    if(change<0)
	    {
	        if((-change)>usage)
	            usage=0;
	        else
	            usage+=change;
	    }
	    else
	    if(Short.MAX_VALUE-change>usage)
	        usage+=change;
	    return usage;
	}

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
		baseEnvStats.copyInto(envStats);
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=(EnvStats)newBaseEnvStats.copyOf();
	}

    public void destroy()
    {
        CMLib.threads().deleteTick(this,-1);
        affects=null;
        imageName=null;
        behaviors=null;
        scripts=null;
        miscText=null;
        amDestroyed=true;
    }
    public boolean amDestroyed(){return amDestroyed;}
    public boolean savable(){return !amDestroyed;}
    
    public String image()
    {
        if(imageName==null) 
            imageName=CMProps.getDefaultMXPImage(this);
        return imageName;
    }
    public String rawImage()
    {
        if(imageName==null) 
            return "";
        return imageName;
    }
    public void setImage(String newImage)
    {
        if((newImage==null)||(newImage.trim().length()==0))
            imageName=null;
        else
            imageName=newImage;
    }
	
	public CMObject newInstance()
	{
		try
        {
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
		baseEnvStats=(EnvStats)E.baseEnvStats().copyOf();
		envStats=(EnvStats)E.envStats().copyOf();

		affects=null;
		behaviors=null;
        scripts=null;
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				addBehavior((Behavior)B.copyOf());
		}
        for(int s=0;s<E.numScripts();s++)
        {
            ScriptingEngine S=E.fetchScript(s);
            if(S!=null) addScript((ScriptingEngine)S.copyOf());
        }
	}
	public CMObject copyOf()
	{
		try
		{
			StdExit E=(StdExit)this.clone();
            CMClass.bumpCounter(this,CMClass.OBJECT_EXIT);
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
	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	public long expirationDate(){return 0;}
	public void setExpirationDate(long time){}

	public void setDisplayText(String newDisplayText){}
	public void setDescription(String newDescription){}
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

    protected Rideable findALadder(MOB mob, Room room)
	{
		if(room==null) return null;
		if(mob.riding()!=null) return null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I!=null)
			   &&(I instanceof Rideable)
			   &&(CMLib.flags().canBeSeenBy(I,mob))
			   &&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_LADDER))
				return (Rideable)I;
		}
		return null;
	}

    protected void mountLadder(MOB mob, Rideable ladder)
	{
		String mountStr=ladder.mountString(CMMsg.TYP_MOUNT,mob);
		CMMsg msg=CMClass.getMsg(mob,ladder,null,CMMsg.MSG_MOUNT,"<S-NAME> "+mountStr+" <T-NAMESELF>.");
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
		if(CMStrings.isVowel(closeWord().charAt(closeWord().length()-1)))
			return closeWord()+"d";
		else
			return closeWord()+"ed";
	}
	
	protected final String openWordPastTense()
	{
		if(openWord().length()==0)
			return "opened";
		else
		if(CMStrings.isVowel(openWord().charAt(openWord().length()-1)))
			return openWord()+"d";
		else
			return openWord()+"ed";
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
        MsgListener N=null;
        for(int b=0;b<numBehaviors();b++)
        {
            N=fetchBehavior(b);
            if((N!=null)&&(!N.okMessage(this,msg)))
                return false;
        }
        for(int s=0;s<numScripts();s++)
        {
            N=fetchScript(s);
            if((N!=null)&&(!N.okMessage(this,msg)))
                return false;
        }
        for(int i=0;i<numEffects();i++)
        {
            N=fetchEffect(i);
            if((N!=null)&&(!N.okMessage(this,msg)))
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
				if(!CMLib.flags().canBeSeenBy(this,mob))
					mob.tell("You can't go that way.");
                else
    				mob.tell("The "+doorName()+" is "+closeWordPastTense()+".");
				return false;
			}
			if((CMLib.flags().isFlying(this))
			&&(!CMLib.flags().isInFlight(mob))
			&&(!CMLib.flags().isFalling(mob)))
			{
				mob.tell("You can't fly.");
				return false;
			}
			if((CMLib.flags().isClimbing(this))
			&&(!CMLib.flags().isFalling(this))
			&&(!CMLib.flags().isClimbing(mob))
			&&(!CMLib.flags().isInFlight(mob)))
			{
				Rideable ladder=null;
				if(msg.target() instanceof Room)
					ladder=findALadder(mob,(Room)msg.target());
				if(ladder!=null)
					mountLadder(mob,ladder);
				if((!CMLib.flags().isClimbing(mob))
				&&(!CMLib.flags().isFalling(mob)))
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
		{
			if(closeWord().length()==0) 
				setExitParams(doorName(),openWord(),"close",closedText());
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
		}
		case CMMsg.TYP_OPEN:
		{
			if(openWord().length()==0) setExitParams(doorName(),"open",closeWord(),closedText());
			if(!hasADoor())
			{
				mob.tell("There is nothing to "+openWord()+" that way!");
				return false;
			}
			if(isOpen())
			{
				mob.tell("The "+doorName()+" is already "+openWordPastTense()+"!");
				return false;
			}
			if(isLocked()&&hasALock())
			{
				mob.tell("The "+doorName()+" is locked.");
				return false;
			}
			return true;
		}
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
						&&(CMLib.flags().canBeSeenBy(item,mob)))
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

	public StringBuilder viewableText(MOB mob, Room room)
	{
		StringBuilder Say=new StringBuilder("");
		if(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
		{
			if(room==null)
				Say.append("^Z(null)^.^? ");
			else
				Say.append("^H("+CMLib.map().getExtendedRoomID(room)+")^? "+room.roomTitle(mob)+CMLib.flags().colorCodes(room,mob)+" ");
			Say.append("via ^H("+ID()+")^? "+(isOpen()?displayText():closedText()));
		}
		else
		if(((CMLib.flags().canBeSeenBy(this,mob))||(isOpen()&&hasADoor()))
		&&(CMLib.flags().isSeen(this)))
			if(isOpen())
			{
				if((room!=null)&&(!CMLib.flags().canBeSeenBy(room,mob)))
					Say.append("darkness");
				else
				if(displayText().length()>0)
					Say.append(displayText()+CMLib.flags().colorCodes(this,mob));
				else
				if(room!=null)
					Say.append(room.roomTitle(mob)+CMLib.flags().colorCodes(room,mob));
			}
			else
			if((CMLib.flags().canBeSeenBy(this,mob))&&(closedText().trim().length()>0))
				Say.append(closedText()+CMLib.flags().colorCodes(this,mob));
		return Say;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
        MsgListener N=null;
        for(int b=0;b<numBehaviors();b++)
        {
            N=fetchBehavior(b);
            if(N!=null)
                N.executeMsg(this,msg);
        }
        
        for(int s=0;s<numScripts();s++)
        {
            N=fetchScript(s);
            if(N!=null)
                N.executeMsg(this,msg);
        }
        
        for(int a=0;a<numEffects();a++)
        {
            N=fetchEffect(a);
            if(N!=null)
                N.executeMsg(this,msg);
        }

		MOB mob=msg.source();
		if((!msg.amITarget(this))&&(msg.tool()!=this))
			return;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
        case CMMsg.TYP_EXAMINE:
            CMLib.commands().handleBeingLookedAt(msg);
            break;
		case CMMsg.TYP_READ:
            CMLib.commands().handleBeingRead(msg);
            break;
		case CMMsg.TYP_CLOSE:
			if((!hasADoor())||(!isOpen())) return;
			isOpen=false;
			break;
		case CMMsg.TYP_OPEN:
			if((!hasADoor())||(isOpen())) return;
			if(defaultsClosed()||defaultsLocked())
				CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_REOPEN,openDelayTicks());
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
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public boolean tick(Tickable ticking, int tickID)
	{
	    if(amDestroyed()) return false;
	    
	    if(usage<=0){ destroy(); return false;}
	    
		if(tickID==Tickable.TICKID_EXIT_REOPEN)
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
		if(tickID==Tickable.TICKID_EXIT_BEHAVIOR)
		{
            int numB=numBehaviors();
            Tickable T=null;
            for(int b=0;b<numB;b++)
            {
                T=fetchBehavior(b);
                if(T!=null)
                    T.tick(ticking,tickID);
            }
            int numS=numScripts();
            if((numB<=0)&&(numS<=0)) return false;
            for(int s=0;s<numS;s++)
            {
                T=fetchScript(s);
                if(T!=null)
                    T.tick(ticking,tickID);
            }
			return !amDestroyed();
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
		if(fetchEffect(to.ID())!=null) return;
		if(affects==null) affects=new Vector(1);
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		if(affects==null) affects=new Vector(1);
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
			behaviors=new Vector(1);
		if(to==null) return;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		// first one! so start ticking...
		if(behaviors.size()==0)
			CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_BEHAVIOR,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		if(behaviors==null) return;
		behaviors.removeElement(to);
		if(((behaviors==null)||(behaviors.size()==0))&&((scripts==null)||(scripts.size()==0)))
			CMLib.threads().deleteTick(this,Tickable.TICKID_EXIT_BEHAVIOR);
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
    
    /** Manipulation of the scripts list */
    public void addScript(ScriptingEngine S)
    {
        if(scripts==null) scripts=new Vector(1);
        if(S==null) return;
        if(!scripts.contains(S)) {
            ScriptingEngine S2=null;
            for(int s=0;s<scripts.size();s++)
            {
                S2=(ScriptingEngine)scripts.elementAt(s);
                if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
                    return;
            }
            if(scripts.size()==0)
                CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_BEHAVIOR,1);
            scripts.addElement(S);
        }
    }
    public void delScript(ScriptingEngine S)
    {
        if(scripts!=null)
        {
            int size=scripts.size();
            scripts.removeElement(S);
            if(scripts.size()<size)
            {
                if(scripts.size()==0)
                    scripts=new Vector(1);
                if(((behaviors==null)||(behaviors.size()==0))&&((scripts==null)||(scripts.size()==0)))
                    CMLib.threads().deleteTick(this,Tickable.TICKID_EXIT_BEHAVIOR);
            }
        }
    }
    public int numScripts(){return (scripts==null)?0:scripts.size();}
    public ScriptingEngine fetchScript(int x){try{return (ScriptingEngine)scripts.elementAt(x);}catch(Exception e){} return null;}
    
	public int openDelayTicks()	{ return 45;}
	public void setOpenDelayTicks(int numTicks){}

	public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS","TEXT"};
	public String[] getStatCodes(){return CODES;}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
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
        String[] codes=getStatCodes();
        for(int i=0;i<codes.length;i++)
            if(!E.getStat(codes[i]).equals(getStat(codes[i])))
                return false;
        return true;
    }
}
