package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Thief_Kidnapping extends ThiefSkill implements PrivateProperty
{
	@Override
	public String ID()
	{
		return "Thief_Kidnapping";
	}

	private final static String localizedName = CMLib.lang().L("Kidnapping");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	protected boolean			reAssist		= false; // false means assist is ON
	protected long				roomTimeout		= System.currentTimeMillis() - 1;
	protected volatile int		aloneTicker		= -1;
	protected Map<MOB, Long>	failures		= new Hashtable<MOB, Long>();
	protected String			roomID			= "";
	protected Room				roomR			= null;
	protected String			followName		= null;
	protected int				price			= -1;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CRIMINAL;
	}

	private static final String[] triggerStrings =I(new String[] {"KIDNAP","KIDNAPPING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_CHARMING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public void unInvoke()
	{
		if(affected instanceof MOB)
			((MOB)affected).setAttribute(Attrib.AUTOASSIST, reAssist);
		super.unInvoke();
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		//super.setMiscText(newMiscText);
		reAssist = CMParms.getParmBool(newMiscText, "NOASSIST", false); // false is ASSIST ON, which is confusing here, i get it
		roomID = CMParms.getParmStr(newMiscText, "ROOM", "");
		followName = CMParms.getParmStr(newMiscText, "FOLLOW", "");
		final String invoker = CMParms.getParmStr(newMiscText, "INVOKER", "");
		if(invoker.length()>0)
		{
			if((invoker()==null)||(!invoker().name().equalsIgnoreCase(invoker)))
			{
				final MOB invokerM = CMLib.players().getLoadPlayer(invoker);
				if(invokerM != null)
					setInvoker(invokerM);
			}
		}
	}

	@Override
	public String text()
	{
		final StringBuilder txt = new StringBuilder("NOASSIST="+reAssist);
		txt.append(" ROOM=\""+CMStrings.escape(roomID)+"\"");
		if(invoker() != null)
			txt.append(" INVOKER=\""+invoker().Name()+"\"");
		if(followName != null)
			txt.append(" FOLLOW=\""+CMStrings.escape(followName)+"\"");
		return txt.toString();
	}

	protected static boolean isKidnappable(final MOB kidnapperM, final MOB M)
	{
		if((M.charStats().ageCategory()>=Race.AGE_YOUNGADULT)
		&&(!CMLib.flags().isAgingChild(M))
		&&(!CMLib.flags().isAnimalIntelligence(M))
		&&(!CMStrings.containsWord(M.name().toLowerCase(), "child"))
		&&(!CMStrings.containsWord(M.name().toLowerCase(), "kid")))
			return false;
		if(M.isPlayer()||(!M.isMonster()))
			return kidnapperM.mayIFight(M);
		if((M.amFollowing()!=null) && (M.amFollowing()!=kidnapperM))
			return kidnapperM.mayIFight(M.getGroupLeader());
		return true;
	}

	protected Room getReturnRoom()
	{
		final Physical P = affected;
		if((roomR == null)
		||(System.currentTimeMillis()>roomTimeout))
		{
			roomTimeout = System.currentTimeMillis() + (TimeManager.MILI_MINUTE*10);
			if(this.roomID.length()>0)
			{
				final Room R = CMLib.map().getRoom(roomID);
				if(R != null)
				{
					roomR = R;
					roomTimeout = Long.MAX_VALUE; // don't timeout, this is perfect
					return roomR;
				}
				roomID="";
			}
			if((followName != null)&&(followName.length()>0))
			{
				if(!CMLib.players().playerExists(followName))
					followName="";
				else
				{
					final MOB M = CMLib.players().getPlayer(followName);
					if((M!=null)&&(CMLib.flags().isInTheGame(M, true)))
					{
						roomR = M.location();
						return roomR;
					}
				}
			}
			if(!(P instanceof Tattooable))
				return null;
			final Tattooable TP = (Tattooable)P;
			for(final Enumeration<Tattoo> t = TP.tattoos();t.hasMoreElements();)
			{
				final Tattoo T = t.nextElement();
				if(T.ID().startsWith("PARENT:"))
				{
					final String parentName = T.ID().substring(7);
					final MOB M = CMLib.players().getPlayer(parentName);
					if((M!=null)&&(CMLib.flags().isInTheGame(M, true)))
					{
						roomR=M.location();
						return roomR;
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(affected instanceof MOB)
		{
			final MOB M = (MOB)affected;
			if((!M.isAttributeSet(Attrib.AUTOASSIST))
			&&(!CMLib.flags().isAgingChild(affected)))
				M.setAttribute(Attrib.AUTOASSIST, true); // true means its assist is turned OFF
			if(invoker() == null)
				return true;
			if(M.isInCombat())
			{
				final MOB vicM = M.getVictim();
				if((vicM != null)
				&& (vicM==invoker().getVictim())
				&&(vicM.getVictim()!=M))
					M.makePeace(false);
			}
			final Room R = M.location();
			if((R!=null)
			&&((M.amFollowing()==null)||(!R.isInhabitant(invoker())))
			&&(R.numInhabitants()==1))
			{
				if(this.aloneTicker < 0)
					this.aloneTicker = 5; // 20 seconds to get it right
				else
				if(--this.aloneTicker<=0)
				{
					final Room returnR = this.getReturnRoom();
					if(returnR == null)
						this.aloneTicker = 40;
					else
					{
						// attempt escape!
						int dir=-1;
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room escapeR = R.getRoomInDir(d);
							if(escapeR != null)
							{
								final Exit E = R.getExitInDir(d);
								if((E!=null)
								&&(E.isOpen()||(!E.isLocked()))
								&&(CMLib.flags().canBeSeenBy(E, M)))
								{
									dir = d;
									break;
								}
							}
						}
						if(dir >= 0)
						{
							final Room escapeR = R.getRoomInDir(dir);
							final Exit E = R.getExitInDir(dir);
							if(!E.isOpen())
								CMLib.commands().postOpen(M, E, false);
							if(E.isOpen())
								CMLib.tracking().walk(M, dir, true, false);
							if(M.location() == escapeR)
							{
								this.unInvoke();
								M.delEffect(this);
								M.setFollowing(null);
								CMLib.tracking().autoTrack(M, returnR);
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_ORDER)
		&&(msg.source().location()!=null)
		&&(msg.target()==affected)
		&&(affected instanceof MOB)
		&&(msg.source() == invoker)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0))
		{
			CMLib.commands().postSay((MOB)affected, "You're not the boss of me!");
			return false;
		}
		return true;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(!isKidnappable(mob, (MOB)target))
				return Ability.QUALITY_INDIFFERENT;
			if(failures.containsKey(target) && (System.currentTimeMillis()<failures.get(target).longValue()))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(failures.containsKey(target) && (System.currentTimeMillis()<failures.get(target).longValue()))
		{
			mob.tell(L("You can't attempt to kidnap @x1 again so soon.",target.name(mob)));
			return false;
		}
		failures.remove(target);

		if(target.amFollowing()==mob)
		{
			mob.tell(L("@x1 is already your follower.",target.name(mob)));
			return false;
		}

		if(!isKidnappable(mob, target))
		{
			mob.tell(L("@x1 doesn't seem like a viable target.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final String str=auto?L("<T-NAME> become(s) kidnapped by <S-NAME>."):L("<S-NAME> kidnap(s) <T-NAME>!");
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT|CMMsg.MASK_SOUND|CMMsg.MASK_MALICIOUS,str);
			if(target.location().okMessage(mob,msg))
			{
				target.location().send(mob,msg);
				if(msg.value()<=0)
				{
					final MOB followP = target.amFollowing();
					target.makePeace(true);
					mob.makePeace(true);
					final boolean wasFollowing;
					String followName = "";
					if(followP != null)
					{
						wasFollowing = true;
						if(followP.isPlayer())
							followName = followP.name();
						CMLib.commands().postFollow(target, null, true);
					}
					else
						wasFollowing = false;
					if(target.amFollowing() != null)
					{
						failures.put(target, Long.valueOf(System.currentTimeMillis()+TimeManager.MILI_HOUR));
						return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to kidnap <T-NAME> and fail(s)."));
					}
					else
					{
						CMLib.commands().postFollow(target, mob, false);
						if(target.amFollowing() != mob)
						{
							failures.put(target, Long.valueOf(System.currentTimeMillis()+TimeManager.MILI_HOUR));
							return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to kidnap <T-NAME> and fail(s)."));
						}
					}
					final boolean autoAssist = target.isAttributeSet(Attrib.AUTOASSIST);
					final Thief_Kidnapping kA = (Thief_Kidnapping)beneficialAffect(mob, target, asLevel, 0);
					if(kA != null)
					{
						kA.reAssist = autoAssist;
						kA.invoker = mob;

						if(wasFollowing)
							kA.followName = followName;
						else
						if((target.getLiegeID().length()>0)
						&&(CMLib.flags().isInTheGame(CMLib.players().getPlayerAllHosts(target.getLiegeID()), true)))
							kA.followName = target.getLiegeID();
						else
							kA.roomID = CMLib.map().getExtendedRoomID(target.location());

						kA.makeNonUninvokable();
					}
				}
			}
		}
		else
		{
			failures.put(target, Long.valueOf(System.currentTimeMillis()+TimeManager.MILI_HOUR));
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to kidnap <T-NAME> and fail(s)."));
		}

		// return whether it worked
		return success;
	}

	@Override
	public int getPrice()
	{
		if(price < 0)
		{
			price = 90;
			final Physical P = affected;
			if(P != null)
				price = price + (P.phyStats().level()*10);
		}
		return price;
	}

	@Override
	public void setPrice(final int price)
	{
		this.price = price;
	}

	@Override
	public String getOwnerName()
	{
		final Physical P = affected;
		if((followName!=null)&&(followName.length()>0))
			return followName;
		if((P instanceof MOB)&&(((MOB)P).getLiegeID().length()>0))
			return ((MOB)P).getLiegeID();
		return "";
	}

	@Override
	public void setOwnerName(final String owner)
	{
		followName = owner;
	}

	@Override
	public boolean isProperlyOwned()
	{
		return getOwnerName().length()>0;
	}

	@Override
	public String getTitleID()
	{
		final Physical P = affected;
		return (P==null)?"":P.Name();
	}
}
