package com.planet_ink.coffee_mud.Exits;
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
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
public class StdExit implements Exit
{
	@Override
	public String ID()
	{
		return "StdExit";
	}

	protected PhyStats	phyStats		= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected PhyStats	basePhyStats	= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected boolean	isOpen			= true;
	protected boolean	isLocked		= false;
	protected String	miscText		= "";
	protected String	cachedImageName	= null;
	protected String	rawImageName	= null;
	protected boolean	amDestroyed		= false;
	protected short		usage			= 0;

	protected String				lastRoomID	= "";
	protected CList<Ability>		affects		= null;
	protected CList<Behavior>		behaviors	= null;
	protected CList<ScriptingEngine>scripts		= null;
	protected Exit					me			= this;

	protected ApplyAffectPhyStats<Ability>	affectPhyStats = new ApplyAffectPhyStats<Ability>(this);
	
	public StdExit()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.EXIT);
		isOpen=!defaultsClosed();
		isLocked=defaultsLocked();
	}

	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.EXIT);}//removed for mem & perf
	@Override
	public void initializeClass()
	{
	}

	@Override
	public String Name()
	{
		return "a walkway";
	}

	@Override
	public boolean hasADoor()
	{
		return false;
	}

	@Override
	public boolean hasALock()
	{
		return false;
	}

	@Override
	public boolean defaultsLocked()
	{
		return false;
	}

	@Override
	public boolean defaultsClosed()
	{
		return false;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public String description()
	{
		return "";
	}

	@Override
	public String description(MOB viewerMob)
	{
		return description();
	}

	@Override
	public String doorName()
	{
		return "door";
	}

	@Override
	public String closedText()
	{
		return "a closed door";
	}

	@Override
	public String closeWord()
	{
		return "close";
	}

	@Override
	public String openWord()
	{
		return "open";
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
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public short exitUsage(short change)
	{
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

	@Override
	public void setName(String newName)
	{
	}

	@Override
	public String name()
	{
		if(phyStats().newName()!=null)
			return phyStats().newName();
		return Name();
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
	}

	@Override
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}

	@Override
	public void destroy()
	{
		CMLib.map().registerWorldObjectDestroyed(null,null,this);
		CMLib.threads().deleteTick(this,-1);
		affects=null;
		rawImageName=null;
		cachedImageName=null;
		behaviors=null;
		scripts=null;
		miscText=null;
		amDestroyed=true;
	}

	@Override
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	@Override
	public boolean isSavable()
	{
		return !amDestroyed && CMLib.flags().isSavable(this);
	}

	@Override
	public void setSavable(boolean truefalse)
	{
		CMLib.flags().setSavable(this, truefalse);
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
		if((newImage==null)||(newImage.trim().length()==0))
			rawImageName=null;
		else
			rawImageName=newImage;
		if((cachedImageName!=null)&&(!cachedImageName.equals(newImage)))
			cachedImageName=null;
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
		return new StdExit();
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	protected void cloneFix(Exit X)
	{
		me=this;
		basePhyStats=(PhyStats)X.basePhyStats().copyOf();
		phyStats=(PhyStats)X.phyStats().copyOf();

		affectPhyStats = new ApplyAffectPhyStats<Ability>(this);
		
		affects=null;
		behaviors=null;
		scripts=null;
		for(final Enumeration<Behavior> e=X.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if(B!=null)
				addBehavior((Behavior)B.copyOf());
		}
		for(final Enumeration<ScriptingEngine> e=X.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			if(SE!=null)
				addScript((ScriptingEngine)SE.copyOf());
		}
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdExit E=(StdExit)this.clone();
			//CMClass.bumpCounter(this,CMClass.CMObjectType.EXIT);//removed for mem & perf
			E.cloneFix(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		miscText = newMiscText;
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
	public long expirationDate()
	{
		return 0;
	}

	@Override
	public void setExpirationDate(long time)
	{
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
	}

	@Override
	public void setDescription(String newDescription)
	{
	}

	@Override
	public int maxRange()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int minRange()
	{
		return Integer.MIN_VALUE;
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

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
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
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			N=a.nextElement();
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}

		final MOB mob=msg.source();
		if((!msg.amITarget(this))&&(msg.tool()!=this))
			return true;
		else
		if(msg.targetMinor()==CMMsg.NO_EFFECT)
			return true;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
		case CMMsg.TYP_EXAMINE:
		case CMMsg.TYP_READ:
		case CMMsg.TYP_WASREAD:
		case CMMsg.TYP_OK_VISUAL:
		case CMMsg.TYP_KNOCK:
		case CMMsg.TYP_OK_ACTION:
			return true;
		case CMMsg.TYP_ENTER:
			if(msg.target() instanceof Room)
				lastRoomID=CMLib.map().getExtendedRoomID((Room)msg.target());
			if((hasADoor())&&(!isOpen())&&(mob.phyStats().height()>=0))
			{
				if(!CMLib.flags().canBeSeenBy(this,mob))
					mob.tell(L("You can't go that way."));
				else
					mob.tell(L("The @x1 is @x2.",doorName(),closeWordPastTense()));
				return false;
			}
			if((CMLib.flags().isFlying(this))
			&&(!CMLib.flags().isInFlight(mob))
			&&(!CMLib.flags().isFalling(mob)))
			{
				mob.tell(L("You can't fly."));
				return false;
			}
			if((CMLib.flags().isClimbing(this))
			&&(!CMLib.flags().isFalling(this))
			&&(!CMLib.flags().isClimbing(mob))
			&&(!CMLib.flags().isInFlight(mob)))
			{
				Rideable ladder=null;
				if(msg.target() instanceof Room)
					ladder=CMLib.tracking().findALadder(mob,(Room)msg.target());
				if(ladder!=null)
					CMLib.tracking().postMountLadder(mob,ladder);
				if((!CMLib.flags().isClimbing(mob))
				&&(!CMLib.flags().isFalling(mob)))
				{
					mob.tell(L("You need to climb that way, if you know how."));
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
			if(isOpen())
			{
				if(!hasADoor())
				{
					mob.tell(L("There is nothing to @x1!",closeWord()));
					return false;
				}
				return true;
			}
			mob.tell(L("The @x1 is already @x2.",doorName(),closeWordPastTense()));
			return false;
		}
		case CMMsg.TYP_OPEN:
		{
			if(openWord().length()==0)
				setExitParams(doorName(),"open",closeWord(),closedText());
			if(!hasADoor())
			{
				mob.tell(L("There is nothing to @x1 that way!",openWord()));
				return false;
			}
			if(isOpen())
			{
				mob.tell(L("The @x1 is already @x2!",doorName(),openWordPastTense()));
				return false;
			}
			if(isLocked()&&hasALock())
			{
				mob.tell(L("The @x1 is locked.",doorName()));
				return false;
			}
			return true;
		}
		case CMMsg.TYP_PUSH:
			if((isOpen())||(!hasADoor()))
			{
				mob.tell(L("There is nothing to push over there."));
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
				mob.tell(L("There is nothing to pull over there."));
				return false;
			}
			return true;
		case CMMsg.TYP_LOCK:
			if(!hasADoor())
			{
				mob.tell(L("There is nothing to lock that way!"));
				return false;
			}
		//$FALL-THROUGH$
		case CMMsg.TYP_UNLOCK:
			if(!hasADoor())
			{
				mob.tell(L("There is nothing to unlock that way!"));
				return false;
			}
			if(isOpen())
			{
				mob.tell(L("The @x1 is already @x2!",doorName(),openWord()));
				return false;
			}
			else
			if(!hasALock())
			{
				mob.tell(L("There is no lock!"));
				return false;
			}
			else
			{
				if((!isLocked())&&(msg.targetMinor()==CMMsg.TYP_UNLOCK))
				{
					mob.tell(L("The @x1 is not locked.",doorName()));
					return false;
				}
				else
				if((isLocked())&&(msg.targetMinor()==CMMsg.TYP_LOCK))
				{
					mob.tell(L("The @x1 is already locked.",doorName()));
					return false;
				}
				else
				{
					for(int i=0;i<mob.numItems();i++)
					{
						final Item item=mob.getItem(i);
						if((item!=null)
						&&(item instanceof DoorKey)
						&&((DoorKey)item).getKey().equals(keyName())
						&&((item.container()==null)
						   ||((item.container().container()==null)
							  &&((item.container().containTypes()&Container.CONTAIN_KEYS)>0)))
						&&(CMLib.flags().canBeSeenBy(item,mob)))
							return true;
					}
					mob.tell(L("You don't seem to have the key."));
					return false;
				}
			}
			//break;
		default:
			break;
		}
		if(msg.amITarget(this))
		{
			mob.tell(L("You can't do that."));
			return false;
		}
		return true;
	}

	@Override
	public StringBuilder viewableText(MOB mob, Room room)
	{
		final StringBuilder viewMsg=new StringBuilder("");
		if(mob.isAttributeSet(MOB.Attrib.SYSOPMSGS))
		{
			if(room==null)
				viewMsg.append("^Z(null)^.^? ");
			else
				viewMsg.append("^H("+CMLib.map().getExtendedRoomID(room)+")^? "+room.displayText(mob)+CMLib.flags().getDispositionBlurbs(room,mob)+" ");
			viewMsg.append("via ^H("+ID()+")^? "+(isOpen()?displayText():closedText()));
		}
		else
		if(((CMLib.flags().canBeSeenBy(this,mob))||(isOpen()&&hasADoor()))
		&&(CMLib.flags().isSeeable(this)))
		{
			if(isOpen())
			{
				if((room!=null)&&(!CMLib.flags().canBeSeenBy(room,mob)))
					viewMsg.append("darkness");
				else
				if(displayText().length()>0)
					viewMsg.append(displayText()+CMLib.flags().getDispositionBlurbs(this,mob));
				else
				if(room!=null)
					viewMsg.append(room.displayText(mob)+CMLib.flags().getDispositionBlurbs(room,mob));
			}
			else
			if((CMLib.flags().canBeSeenBy(this,mob))&&(closedText().trim().length()>0))
				viewMsg.append(closedText()+CMLib.flags().getDispositionBlurbs(this,mob));
		}
		return viewMsg;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(numBehaviors()>0)
		{
			eachBehavior(new EachApplicable<Behavior>()
			{ 
				@Override
				public final void apply(final Behavior B)
				{
					B.executeMsg(me, msg);
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
					S.executeMsg(me, msg);
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
					A.executeMsg(me,msg);
				}
			});
		}

		final MOB mob=msg.source();
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
			if((!hasADoor())||(!isOpen()))
				return;
			isOpen=false;
			break;
		case CMMsg.TYP_OPEN:
			if((!hasADoor())||(isOpen()))
				return;
			if(defaultsClosed()||defaultsLocked())
			{
				CMLib.threads().deleteTick(this,Tickable.TICKID_EXIT_REOPEN);
				CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_REOPEN,openDelayTicks());
			}
			isLocked=false;
			isOpen=true;
			break;
		case CMMsg.TYP_LOCK:
			if((!hasADoor())||(!hasALock())||(isLocked()))
				return;
			isOpen=false;
			isLocked=true;
			break;
		case CMMsg.TYP_PULL:
		case CMMsg.TYP_PUSH:
			mob.tell(L("It doesn't appear to be doing any good."));
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

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(amDestroyed())
			return false;

		if(usage<=0)
		{
			destroy();
			return false;
		}

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
			if(numBehaviors()>0)
			{
				eachBehavior(new EachApplicable<Behavior>(){ 
					@Override
					public final void apply(final Behavior B)
					{
						B.tick(ticking, tickID);
					} 
				});
			}
			if(numScripts()>0)
			{
				eachScript(new EachApplicable<ScriptingEngine>(){ 
					@Override
					public final void apply(final ScriptingEngine S)
					{
						S.tick(ticking, tickID);
					} 
				});
			}
			return !amDestroyed();
		}
		else
		{
			if(numEffects()>0)
			{
				eachEffect(new EachApplicable<Ability>(){ 
					@Override
					public final void apply(final Ability A)
					{
						if(!A.tick(ticking,tickID))
							A.unInvoke();
					}
				});
			}
			return true;
		}
	}

	@Override
	public boolean isOpen()
	{
		return isOpen;
	}

	@Override
	public boolean isLocked()
	{
		return isLocked;
	}

	@Override
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

	@Override
	public String readableText()
	{
		return (isReadable() ? miscText : "");
	}

	@Override
	public boolean isReadable()
	{
		return false;
	}

	@Override
	public void setReadable(boolean isTrue)
	{
	}

	@Override
	public void setReadableText(String text)
	{
		miscText = temporaryDoorLink() + text;
	}

	@Override
	public void setExitParams(String newDoorName, String newCloseWord, String newOpenWord, String newClosedText)
	{
	}

	@Override
	public String keyName()
	{
		return (hasALock() ? miscText : "");
	}

	@Override
	public void setKeyName(String newKeyName)
	{
		miscText = temporaryDoorLink() + newKeyName;
	}

	@Override
	public Room lastRoomUsedFrom(Room fromRoom)
	{
		return CMLib.map().getRoom(lastRoomID);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
	}// exits will never be asked this, so this method should always do NOTHING

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{// exits will never be asked this, so this method should always do NOTHING
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{// exits will never be asked this, so this method should always do NOTHING
	}

	@Override
	public String temporaryDoorLink()
	{
		if(miscText.startsWith("{#"))
		{
			final int x=miscText.indexOf("#}");
			if(x>=0)
				return miscText.substring(2,x);
		}
		return "";
	}

	@Override
	public void setTemporaryDoorLink(String link)
	{
		if(link.startsWith("{{#"))
		{
			final int x=link.indexOf("#}}");
			if(x>=0)
				lastRoomID=link.substring(3,x);
			return;
		}
		if(miscText.startsWith("{#"))
		{
			final int x=miscText.indexOf("#}");
			if(x>=0)
				miscText=miscText.substring(x+2);
		}
		if(link.length()>0)
			miscText="{#"+link+"#}"+miscText;
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
		affects.add(to);
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
		affects.add(to);
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
		if(affects==null)
			return;
		try
		{
			for(int a=0;a<affects.size();a++)
			{
				final Ability A=affects.get(a);
				if(A!=null)
					applier.apply(A);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e)
		{
		}
	}

	@Override
	public void delAllEffects(boolean unInvoke)
	{
		final CList<Ability> affects=this.affects;
		if(affects==null)
			return;
		for(int a=numEffects()-1;a>=0;a--)
		{
			final Ability A=fetchEffect(a);
			if(A!=null)
			{
				if(unInvoke)
					A.unInvoke();
				A.setAffectedOne(null);
			}
		}
		affects.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<Ability> effects()
	{
		return (affects == null) ? EmptyEnumeration.INSTANCE : affects.elements();
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
			return affects.get(index);
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
		if(behaviors==null)
			behaviors=new SVector<Behavior>(1);
		if(to==null)
			return;
		for(final Behavior B : behaviors)
		{
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		// first one! so start ticking...
		if(behaviors.size()==0)
			CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_BEHAVIOR,1);
		to.startBehavior(this);
		behaviors.add(to);
	}

	@Override
	public void delBehavior(Behavior to)
	{
		if(behaviors==null)
			return;
		behaviors.remove(to);
		if(((behaviors==null)||(behaviors.size()==0))&&((scripts==null)||(scripts.size()==0)))
			CMLib.threads().deleteTick(this,Tickable.TICKID_EXIT_BEHAVIOR);
	}

	@Override
	public void delAllBehaviors()
	{
		final boolean didSomething=(behaviors!=null)&&(behaviors.size()>0);
		if(didSomething)
			behaviors.clear();
		behaviors=null;
		if(didSomething && ((scripts==null)||(scripts.size()==0)))
			CMLib.threads().deleteTick(this,Tickable.TICKID_EXIT_BEHAVIOR);
	}

	@Override
	public int numBehaviors()
	{
		if(behaviors==null)
			return 0;
		return behaviors.size();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<Behavior> behaviors()
	{
		return (behaviors == null) ? EmptyEnumeration.INSTANCE : behaviors.elements();
	}

	@Override
	public Behavior fetchBehavior(int index)
	{
		if(behaviors==null)
			return null;
		try
		{
			return behaviors.get(index);
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
				S2=scripts.get(s);
				if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
					return;
			}
			if(scripts.size()==0)
				CMLib.threads().startTickDown(this,Tickable.TICKID_EXIT_BEHAVIOR,1);
			scripts.add(S);
		}
	}

	@Override
	public void delScript(ScriptingEngine S)
	{
		if(scripts!=null)
		{
			if(scripts.remove(S))
			{
				if(scripts.size()==0)
					scripts=new SVector<ScriptingEngine>(1);
				if(((behaviors==null)||(behaviors.size()==0))&&((scripts==null)||(scripts.size()==0)))
					CMLib.threads().deleteTick(this,Tickable.TICKID_EXIT_BEHAVIOR);
			}
		}
	}

	@Override
	public void delAllScripts()
	{
		final boolean didSomething=(scripts!=null)&&(scripts.size()>0);
		if(didSomething)
			scripts.clear();
		scripts=null;
		if(didSomething && ((behaviors==null)||(behaviors.size()==0)))
			CMLib.threads().deleteTick(this,Tickable.TICKID_EXIT_BEHAVIOR);
	}

	@Override
	public int numScripts()
	{
		return (scripts == null) ? 0 : scripts.size();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<ScriptingEngine> scripts()
	{
		return (scripts == null) ? EmptyEnumeration.INSTANCE : scripts.elements();
	}

	@Override
	public ScriptingEngine fetchScript(int x)
	{
		try
		{
			return scripts.get(x);
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
		try
		{
			for(int a=0;a<scripts.size();a++)
			{
				final ScriptingEngine S=scripts.get(a);
				if(S!=null)
					applier.apply(S);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e)
		{
		}
	}

	@Override
	public int openDelayTicks()
	{
		return 45;
	}

	@Override
	public void setOpenDelayTicks(int numTicks)
	{
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[]	CODES	= { "CLASS", "TEXT" };

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
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
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
			setMiscText(val);
			break;
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdExit))
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
