package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class StdRideable extends StdMOB implements Rideable
{
	@Override
	public String ID()
	{
		return "StdRideable";
	}

	protected Basis			rideBasis		= Rideable.Basis.LAND_BASED;
	protected int			riderCapacity	= 2;
	protected List<Rider>	riders			= new SVector<Rider>();
	protected String		putString		= "";
	protected String		rideString		= "";
	protected String		stateString		= "";
	protected String		stateSubjectStr	= "";
	protected String		mountString		= "";
	protected String		dismountString	= "";

	public StdRideable()
	{
		super();
		_name="a horse";
		setDescription("A brown riding horse looks sturdy and reliable.");
		setDisplayText("a horse stands here.");
		baseCharStats().setMyRace(CMClass.getRace("Horse"));
		basePhyStats().setWeight(700);
		recoverPhyStats();
	}

	@Override
	protected void cloneFix(final MOB E)
	{
		super.cloneFix(E);
		riders=new SVector<Rider>();
	}

	@Override
	public DeadBody killMeDead(final boolean createBody)
	{
		while(riders.size()>0)
		{
			final Rider mob=fetchRider(0);
			if(mob!=null)
			{
				mob.setRiding(null);
				delRider(mob);
			}
		}
		return super.killMeDead(createBody);
	}

	@Override
	public void destroy()
	{
		while(riders.size()>0)
		{
			final Rider mob=fetchRider(0);
			if(mob!=null)
			{
				mob.setRiding(null);
				delRider(mob);
			}
		}
		super.destroy();
	}

	@Override
	public boolean isMobileRideBasis()
	{
		switch(rideBasis())
		{
			case FURNITURE_SIT:
			case FURNITURE_TABLE:
			case FURNITURE_HOOK:
			case ENTER_IN:
			case FURNITURE_SLEEP:
			case LADDER:
				return false;
			default:
				break;
		}
		return true;
	}

	@Override
	public boolean isSavable()
	{
		Rider R=null;
		for(int r=0;r<numRiders();r++)
		{
			R=fetchRider(r);
			if(!R.isSavable())
				return false;
		}
		return super.isSavable();
	}

	// common item/mob stuff
	@Override
	public Basis rideBasis()
	{
		return rideBasis;
	}

	@Override
	public void setRideBasis(final Basis basis)
	{
		rideBasis = basis;
	}

	@Override
	public int riderCapacity()
	{
		return riderCapacity;
	}

	@Override
	public void setRiderCapacity(final int newCapacity)
	{
		riderCapacity = newCapacity;
	}

	@Override
	public int numRiders()
	{
		return riders.size();
	}

	@Override
	public boolean mobileRideBasis()
	{
		return true;
	}

	@Override
	public Rider fetchRider(final int which)
	{
		try
		{
			return riders.get(which);
		}
		catch (final IndexOutOfBoundsException e)
		{
		}
		return null;
	}

	@Override
	public String putString(final Rider R)
	{
		if((R==null)||(putString.length()==0))
			return "on";
		return putString;
	}

	@Override
	public String getPutString()
	{
		return putString;
	}

	@Override
	public void setPutString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			putString="";
		else
		{
			if(str.equalsIgnoreCase(this.putString(null)))
				putString="";
			else
				putString=str.trim();
		}
	}

	@Override
	public void addRider(final Rider mob)
	{
		if((mob!=null)&&(!riders.contains(mob)))
			riders.add(mob);
	}

	@Override
	public void delRider(final Rider mob)
	{
		if(mob!=null)
			while(riders.remove(mob))
				{
				}
	}

	@Override
	public Enumeration<Rider> riders()
	{
		return new IteratorEnumeration<Rider>(riders.iterator());
	}

	@Override
	public String displayText(final MOB mob)
	{
		return super.displayText(mob); // StdMOB handles rideables
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(rideBasis==Rideable.Basis.AIR_FLYING)
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_FLYING);
		else
		if(rideBasis==Rideable.Basis.WATER_BASED)
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_SWIMMING);
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(amRiding(affected))
		{
			for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(A.bubbleAffect()))
					A.affectCharStats(affected,affectableStats);
			}
		}
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		super.affectCharState(affected,affectableStats);
		if(amRiding(affected))
		{
			for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(A.bubbleAffect()))
					A.affectCharState(affected,affectableStats);
			}
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			if(!CMLib.flags().isWithSeenContents(this))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_NOT_SEEN);
			if(amRiding(mob))
			{
				if((mob.isInCombat())&&(mob.rangeToTarget()==0))
				{
					affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-mob.basePhyStats().attackAdjustment());
					affectableStats.setDamage(affectableStats.damage()-mob.basePhyStats().damage());
				}
				for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)&&(A.bubbleAffect()))
						A.affectPhyStats(affected,affectableStats);
				}
			}
		}
	}

	@Override
	public boolean amRiding(final Rider mob)
	{
		return riders.contains(mob);
	}

	@Override
	public String stateString(final Rider R)
	{

		if((R==null)
		||(stateString.length()==0))
		{
			if(R instanceof Item)
				return "being pulled by";
			else
			if(R instanceof Rideable)
				return "mounted with";
			return "riding on";
		}
		return stateString;
	}

	@Override
	public String getStateString()
	{
		return stateString;
	}

	@Override
	public void setStateString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			stateString="";
		else
		{
			if(str.equalsIgnoreCase(this.stateString(null)))
				stateString="";
			else
				stateString=str.trim();
		}
	}

	@Override
	public String mountString(final int commandType, final Rider R)
	{
		if((R==null)||(mountString.length()==0))
			return "mount(s)";
		return mountString;
	}

	@Override
	public String getMountString()
	{
		return mountString;
	}

	@Override
	public void setMountString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			mountString="";
		else
		{
			if(str.equalsIgnoreCase(this.mountString(0,null)))
				mountString="";
			else
				mountString=str.trim();
		}
	}

	@Override
	public String dismountString(final Rider R)
	{
		if((R==null)||(dismountString.length()==0))
			return "dismount(s)";
		return dismountString;
	}

	@Override
	public String getDismountString()
	{
		return dismountString;
	}

	@Override
	public void setDismountString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			dismountString="";
		else
		{
			if(str.equalsIgnoreCase(this.dismountString(null)))
				dismountString="";
			else
				dismountString=str.trim();
		}
	}

	@Override
	public String rideString(final Rider R)
	{
		if((R==null)||(rideString.length()==0))
		{
			if(R instanceof Item)
				return "is pulled";
			else
			if(R instanceof Rideable)
				return "follow(s)";
			return "ride(s)";
		}
		return rideString;
	}

	@Override
	public String getRideString()
	{
		return rideString;
	}

	@Override
	public void setRideString(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			rideString="";
		else
		{
			if(str.equalsIgnoreCase(this.rideString(null)))
				rideString="";
			else
				rideString=str.trim();
		}
	}

	@Override
	public String stateStringSubject(final Rider R)
	{
		if((R==null)||(stateSubjectStr.length()==0))
		{
			if(R instanceof Rideable)
			{
				final Rideable rI=(Rideable)R;
				if(rI.rideBasis()==Rideable.Basis.WAGON)
					return "pulling along";
				if(rI instanceof MOB)
					return "mounted with";
			}
			return "being ridden by";
		}
		return stateSubjectStr;
	}

	@Override
	public String getStateStringSubject()
	{
		return stateSubjectStr;
	}

	@Override
	public void setStateStringSubject(final String str)
	{
		if((str==null)||(str.trim().length()==0))
			this.stateSubjectStr="";
		else
		{
			if(str.equalsIgnoreCase(this.stateStringSubject(null)))
				stateSubjectStr="";
			else
				stateSubjectStr=str.trim();
		}
	}

	@Override
	public Set<MOB> getRideBuddies(final Set<MOB> list)
	{
		if(list==null)
			return list;
		if(!list.contains(this))
			list.add(this);
		for(int r=0;r<numRiders();r++)
		{
			final Rider R=fetchRider(r);
			if((R instanceof MOB)
			&&(!list.contains(R)))
				list.add((MOB)R);
		}
		return list;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_PUSH:
		case CMMsg.TYP_PULL:
			if(amRiding(msg.source()))
			{
				msg.source().tell(L("You cannot do that while @x1 @x2!",stateString(msg.source()),name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_DISMOUNT:
			if(msg.amITarget(this))
			{
				if(msg.tool() instanceof Rider)
				{
					if(!amRiding((Rider)msg.tool()))
					{
						msg.source().tell(L("@x1 is not @x2 @x3!",msg.tool().name(),stateString((Rider)msg.tool()),name(msg.source())));
						if(((Rider)msg.tool()).riding()==this)
							((Rider)msg.tool()).setRiding(null);
						return false;
					}
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().tell(L("You are not @x1 @x2!",stateString(msg.source()),name(msg.source())));
					if(msg.source().riding()==this)
						msg.source().setRiding(null);
					return false;
				}
				// protects from standard mob rejection
				return true;
			}
			break;
		case CMMsg.TYP_SIT:
			if(amRiding(msg.source()))
			{
				msg.source().tell(L("You are @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			else
			if(msg.amITarget(this))
			{
				if(rideBasis()==Basis.FURNITURE_HOOK)
				{
					msg.source().tell(L("You can't do that to @x1.",name(msg.source())));
					return false;
				}
				msg.source().tell(L("You cannot simply sit on @x1, try 'mount'.",name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_SLEEP:
			if(amRiding(msg.source()))
			{
				msg.source().tell(L("You are @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			else
			if(msg.amITarget(this))
			{
				msg.source().tell(L("You cannot lie down on @x1.",name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_MOUNT:
			if(amRiding(msg.source()))
			{
				msg.source().tell(null,msg.source(),null,L("<T-NAME> <T-IS-ARE> @x1 @x2!",stateString(msg.source()),name(msg.source())));
				msg.source().setRiding(this);
				return false;
			}
			if(msg.amITarget(this))
			{
				final Rider whoWantsToRide=(msg.tool() instanceof Rider)?(Rider)msg.tool():msg.source();
				if(amRiding(whoWantsToRide))
				{
					msg.source().tell(L("@x1 is @x2 @x3!",whoWantsToRide.name(msg.source()),stateString(whoWantsToRide),name(msg.source())));
					whoWantsToRide.setRiding(this);
					return false;
				}
				if((msg.tool() instanceof MOB)&&(!CMLib.flags().isBoundOrHeld((MOB)msg.tool())))
				{
					msg.source().tell(L("@x1 won't let you do that.",((MOB)msg.tool()).name(msg.source())));
					return false;
				}
				if(isInCombat())
				{
					msg.source().tell(L("@x1 won't let you do that right now.",name(msg.source())));
					return false;
				}
				if(rideBasis()==Basis.FURNITURE_HOOK)
				{
					msg.source().tell(L("You can't do that to @x1.",name(msg.source())));
					return false;
				}
				if(riding()==whoWantsToRide)
				{
					if(msg.tool() instanceof Physical)
						msg.source().tell(L("@x1 can not be mounted to @x2!",((Physical)msg.tool()).name(msg.source()),name(msg.source())));
					else
						msg.source().tell(L("@x1 can not be mounted to @x2!",msg.tool().name(),name(msg.source())));
					return false;
				}
				if((msg.tool() instanceof Rideable)&&(msg.tool() instanceof MOB))
				{
					msg.source().tell(L("@x1 is not allowed on @x2.",((MOB)msg.tool()).name(msg.source()),name(msg.source())));
					return false;
				}
				if((msg.source() instanceof Rideable)
				&&(!(msg.tool() instanceof Item)))
				{
					msg.source().tell(L("You can not mount @x1.",name(msg.source())));
					return false;
				}
				if((msg.tool() instanceof Rideable)
				&&(msg.tool() instanceof Item)
				&&(((Rideable)msg.tool()).rideBasis()!=Rideable.Basis.WAGON))
				{
					msg.source().tell(L("@x1 can not be mounted on @x2.",((Item)msg.tool()).name(msg.source()),name(msg.source())));
					return false;
				}
				if((msg.tool() instanceof Rideable)
				&&(msg.tool() instanceof NavigableItem)
				&&((((NavigableItem)msg.tool()).navBasis() ==Rideable.Basis.LAND_BASED)))
				{
					//handles its own pullers
				}
				else
				{
					if((basePhyStats().weight()*5<whoWantsToRide.basePhyStats().weight()))
					{
						msg.source().tell(L("@x1 is too small for @x2.",name(msg.source()),whoWantsToRide.name(msg.source())));
						return false;
					}
				}
				if((numRiders()>=riderCapacity())
				&&(!amRiding(whoWantsToRide)))
				{
					// for items
					msg.source().tell(L("No more can fit on @x1.",name(msg.source())));
					// for mobs
					// msg.source().tell(L("No more can fit on @x1.",name(msg.source())));
					return false;
				}
				if((msg.source()==this)
				&&(!(msg.tool() instanceof Item)))
				{
					msg.source().tell(L("You can not ride yourself!"));
					return false;
				}
				if(this.playerStats!=null)
				{
					if((!charStats().getMyRace().useRideClass())
					||(playerStats.isIgnored(msg.source()))
					||(!getGroupMembers(new HashSet<MOB>()).contains(msg.source())))
					{
						msg.source().tell(L("@x1 won't let you do that.",name(msg.source())));
						return false;
					}
				}
				// protects from standard item rejection
				return true;
			}
			break;
		case CMMsg.TYP_ENTER:
			if(amRiding(msg.source())
			   &&(msg.target() instanceof Room))
			{
				final Room sourceRoom=msg.source().location();
				final Room targetRoom=(Room)msg.target();
				if((sourceRoom!=null)&&(!msg.amITarget(sourceRoom)))
				{
					boolean ok=((targetRoom.domainType()&Room.INDOORS)==0)
								||(targetRoom.maxRange()>4)
								||(targetRoom.phyStats().weight()>2);
					switch(rideBasis)
					{
					case LAND_BASED:
						if((targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(CMLib.flags().isDeepWateryRoom(targetRoom))
						||(targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR))
							ok=false;
						else
						if((!ok)
						&&(phyStats().weight() < 200)
						&&(!(amFollowing() instanceof Item)))
						{
							ok=true;
							for(final Rider R : riders)
							{
								if(R instanceof Item)
									ok=false;
							}
						}
						break;
					case AIR_FLYING:
						break;
					case WATER_BASED:
						if((!CMLib.flags().isWaterySurfaceRoom(sourceRoom))
						&&(!CMLib.flags().isWaterySurfaceRoom(targetRoom)))
							ok=false;
						if((targetRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
						||(targetRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)
						||(CMLib.flags().isUnderWateryRoom(targetRoom)))
							ok=false;
						break;
					default:
						break;
					}
					if(!ok)
					{
						msg.source().tell(L("You cannot ride @x1 that way.",name(msg.source())));
						return false;
					}
					if(CMLib.flags().isSitting(msg.source()))
					{
						msg.source().tell(L("You cannot crawl while @x1 @x2.",stateString(msg.source()),name(msg.source())));
						return false;
					}
				}
			}
			if((this.playerStats!=null)&&(!charStats().getMyRace().useRideClass())&&(numRiders()>0))
			{
				msg.source().tell(L("@x1 is far too burdened!",name(msg.source())));
				return false;
			}
			break;
		case CMMsg.TYP_GIVE:
			if((msg.target() instanceof MOB)
			&&(!msg.targetMajor(CMMsg.MASK_ALWAYS)))
			{
				final MOB tmob=(MOB)msg.target();
				if((amRiding(tmob))
				&&(!amRiding(msg.source())))
				{
					if(!(tmob.okMessage(myHost, msg)))
						return false;
					if(!msg.targetMajor(CMMsg.MASK_ALWAYS))
					{
						msg.source().tell(msg.source(),tmob,null,L("<T-NAME> must dismount first."));
						return false;
					}
				}
			}
			break;
		case CMMsg.TYP_BUY:
		case CMMsg.TYP_BID:
		case CMMsg.TYP_SELL:
			if((amRiding(msg.source()))
			&&(!msg.targetMajor(CMMsg.MASK_ALWAYS)))
			{
				msg.source().tell(L("You cannot do that while @x1 @x2.",stateString(msg.source()),name(msg.source())));
				return false;
			}
			break;
		}
		if((msg.sourceMajor(CMMsg.MASK_HANDS))
		&&(amRiding(msg.source()))
		&&((msg.sourceMessage()!=null)||(msg.othersMessage()!=null))
		&&(!msg.targetMajor(CMMsg.MASK_ALWAYS))
		&&((!CMLib.utensils().reachableItem(msg.source(),msg.target()))
			|| (!CMLib.utensils().reachableItem(msg.source(),msg.tool()))
			|| ((msg.sourceMinor()==CMMsg.TYP_GIVE)
				&&(msg.target() instanceof MOB)
				&&(msg.target()!=this)
				&&(!amRiding((MOB)msg.target())))))
		{
			msg.source().tell(L("You cannot do that while @x1 @x2.",stateString(msg.source()),name(msg.source())));
			return false;
		}
		if(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		{
			if((msg.amITarget(this))
			&&((msg.source().riding()==this)
				||(this.amRiding(msg.source()))))
			{
				msg.source().tell(L("You can't attack @x1 right now.",name(msg.source())));
				if(getVictim()==msg.source())
					setVictim(null);
				if(msg.source().getVictim()==this)
					msg.source().setVictim(null);
				return false;
			}
			else
			if((msg.amISource(this))
			&&(msg.target() instanceof MOB)
			&&((amRiding((MOB)msg.target()))
				||(((MOB)msg.target()).riding()==this)))
			{
				final MOB targ=(MOB)msg.target();
				tell(L("You can't attack @x1 right now.",targ.name(this)));
				if(getVictim()==targ)
					setVictim(null);
				if(targ.getVictim()==this)
					targ.setVictim(null);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_LOOK:
		case CMMsg.TYP_EXAMINE:
			if((msg.target()==this)
			&&(numRiders()>0)
			&&(CMLib.flags().canBeSeenBy(this,msg.source())))
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_VISUAL,displayText(msg.source()),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
			break;
		case CMMsg.TYP_DISMOUNT:
			if(msg.tool() instanceof Rider)
			{
				((Rider)msg.tool()).setRiding(null);
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
				{
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
			}
			else
			if(amRiding(msg.source()))
			{
				msg.source().setRiding(null);
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
				{
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
			}
			break;
		case CMMsg.TYP_MOUNT:
			if(msg.amITarget(this))
			{
				if(msg.tool() instanceof Rider)
				{
					((Rider)msg.tool()).setRiding(this);
					if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					{
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
					}
				}
				else
				if(!amRiding(msg.source()))
				{
					msg.source().setRiding(this);
					if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					{
						if(msg.source().location()!=null)
							msg.source().location().recoverRoomStats();
					}
				}
			}
			break;
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_QUIT:
		case CMMsg.TYP_PANIC:
		case CMMsg.TYP_DEATH:
			if(amRiding(msg.source()))
			{
				msg.source().setRiding(null);
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
				{
					if(msg.source().location()!=null)
						msg.source().location().recoverRoomStats();
				}
			}
			break;
		}
	}
}
