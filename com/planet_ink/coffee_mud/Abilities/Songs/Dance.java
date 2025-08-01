package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class Dance extends StdAbility
{
	@Override
	public String ID()
	{
		return "Dance";
	}

	private final static String localizedName = CMLib.lang().L("a Dance");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "("+danceOf()+")"; // danceOf is name, and name is localized
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

	private static final String[] triggerStrings =I(new String[] {"DANCE","DA"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SONG|Ability.DOMAIN_DANCING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(2);
	}

	protected int			invokerManaCost	= -1;
	protected long			timeOut			= 0;
	protected List<Room>	commonRoomSet	= null;
	protected Room			originRoom		= null;
	protected volatile int	danceDepth		= 0;

	protected volatile Pair<Double,Integer> bonusCache = null;

	@Override
	public void setAffectedOne(final Physical P)
	{
		bonusCache = null;
		super.setAffectedOne(P);
	}

	@Override
	public void setInvoker(final MOB mob)
	{
		super.setInvoker(mob);
		bonusCache = null;
	}

	protected synchronized Pair<Double,Integer> getBonuses()
	{
		if(bonusCache != null)
			return bonusCache;
		final Double d = Double.valueOf(innerStatBonusPct());
		final Integer i = Integer.valueOf(innerAvgStat());
		bonusCache = new Pair<Double,Integer>(d,i);
		return bonusCache;
	}

	protected double statBonusPct()
	{
		return getBonuses().first.doubleValue();
	}

	protected int avgStat()
	{
		return getBonuses().second.intValue();
	}

	protected double innerStatBonusPct()
	{
		if(invoker()==null)
			return 1.0;
		final double max = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		double pct = CMath.div(invoker().charStats().getStat(CharStats.STAT_CHARISMA)+3, max);
		pct += CMath.div(invoker().charStats().getStat(CharStats.STAT_STRENGTH)+3, max);
		return pct / 2.0;
	}

	protected int innerAvgStat()
	{
		final int max = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		if(invoker()==null)
			return max;
		final double pct = CMath.div((invoker().charStats().getStat(CharStats.STAT_CHARISMA)+3)
									+ (invoker().charStats().getStat(CharStats.STAT_STRENGTH)+3),2.0);
		return (int)Math.round(pct);
	}

	protected boolean skipStandardDanceInvoke()
	{
		return false;
	}

	protected boolean mindAttack()
	{
		return abstractQuality() == Ability.QUALITY_MALICIOUS;
	}

	protected boolean skipStandardDanceTick()
	{
		return false;
	}

	protected boolean maliciousButNotAggressiveFlag()
	{
		return false;
	}

	protected String danceOf()
	{
		return name();
	}

	protected boolean HAS_QUANTITATIVE_ASPECT()
	{
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		if(this.invoker()==affectedEnv)
			affectableStats.addAmbiance("performing the "+danceOf().toLowerCase());
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public int adjustedLevel(final MOB mob, final int asLevel)
	{
		int level=super.adjustedLevel(mob,asLevel);
		if(mob != null)
		{
			level += (mob.charStats().getStat(CharStats.STAT_CHARISMA)-10)/4;
			level += (mob.charStats().getStat(CharStats.STAT_STRENGTH)-10)/5;
		}
		return level;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((!super.tick(ticking,tickID))||(!(affected instanceof MOB)))
			return false;

		final MOB mob=(MOB)affected;
		if((affected==invoker())&&(invoker()!=null)&&(invoker().location()!=originRoom))
		{
			final List<Room> V=getInvokerScopeRoomSet(this.danceDepth);
			commonRoomSet.clear();
			commonRoomSet.addAll(V);
			originRoom=invoker().location();
		}
		else
		if((abstractQuality()==Ability.QUALITY_MALICIOUS)
		&&(!maliciousButNotAggressiveFlag())
		&&(!mob.amDead())
		&&(mob.isMonster())
		&&(!mob.isInCombat())
		&&(CMLib.flags().isAliveAwakeMobile(mob,true))
		&&(mob.amFollowing()==null)
		&&((!(mob instanceof Rideable))||(((Rideable)mob).numRiders()==0))
		&&(!CMLib.flags().isATrackingMonster(mob)))
		{
			if((mob.location()!=originRoom)
			&&(CMLib.flags().isMobile(mob)))
			{
				final int dir=this.getCorrectDirToOriginRoom(mob.location(),commonRoomSet.indexOf(mob.location()));
				if(dir>=0)
					CMLib.tracking().walk(mob,dir,false,false);
			}
			else
			if((mob.location().isInhabitant(invoker()))
			&&(CMLib.flags().canBeSeenBy(invoker(),mob)))
				CMLib.combat().postAttack(mob,invoker(),mob.fetchWieldedItem());
		}

		if((invoker==null)
		||(invoker.fetchEffect(ID())==null)
		||(commonRoomSet==null))
			return unDanceMe(mob,null, false);
		if(!commonRoomSet.contains(mob.location()))
		{
			final List<Room> V=getInvokerScopeRoomSet(this.danceDepth);
			commonRoomSet.clear();
			commonRoomSet.addAll(V);
			if(!commonRoomSet.contains(mob.location()))
				return unDanceMe(mob,null, false);
		}

		if(skipStandardDanceTick())
			return true;

		if((invoker==null)
		||(CMLib.flags().isSitting(invoker()))
		||(!CMLib.flags().isAliveAwakeMobile(mob,true))
		||(!CMLib.flags().isAliveAwakeMobile(invoker(),true))
		||(mob.riding() instanceof MOB)
		||(!CMLib.flags().canBeSeenBy(invoker,mob)))
			return unDanceMe(mob,null, false);

		if(invokerManaCost<0)
			invokerManaCost=usageCost(invoker(),false)[1];
		if(!mob.curState().adjMovement(-(invokerManaCost/15),mob.maxState()))
		{
			mob.tell(L("The dancing exhausts you."));
			unDanceAll(mob,null,false,false);
			return false;
		}
		return true;
	}

	protected void unDanceAll(final MOB mob, final MOB invoker, final boolean exceptThisOne, final boolean noEcho)
	{
		if(mob!=null)
		{
			for(int a=mob.numEffects()-1;a>=0;a--)
			{
				final Ability A=mob.fetchEffect(a);
				if((A instanceof Dance)
				&&((!exceptThisOne)||(!A.ID().equals(ID())))
				&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
					((Dance)A).unDanceMe(mob,invoker, noEcho);
			}
		}
	}

	protected boolean unDanceMe(final MOB mob, final MOB invoker, final boolean noEcho)
	{
		if(mob==null)
			return false;
		final Ability A=mob.fetchEffect(ID());
		if((A instanceof Dance)
		&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
		{
			if(noEcho)
			{
				A.unInvoke();
				return false;
			}
			else
			{
				final Dance D=(Dance)A;
				if(D.timeOut==0)
					D.timeOut = System.currentTimeMillis()
							  + (CMProps.getTickMillis() * (((invoker()!=null)&&(invoker()!=mob))?super.getXTIMELevel(invoker()):0));
				if(System.currentTimeMillis() >= D.timeOut)
				{
					A.unInvoke();
					return false;
				}
			}
		}
		return true;
	}

	protected int calculateNewSongDepth(final MOB invoker)
	{
		if((invoker!=null)
		&&(invoker.fetchEffect(ID())!=null))
		{
			final Dance D=(Dance)invoker.fetchAbility(ID());
			final int maxDepth = getXMAXRANGELevel(invoker()) / 2; // decreased because fireball
			final int songDepth = ((D!=null)?D.danceDepth:this.danceDepth) + 1;
			if(songDepth > maxDepth)
				return maxDepth;
			return songDepth;
		}
		return 0;
	}

	protected List<Room> getInvokerScopeRoomSet(final int depth)
	{
		final MOB invoker = invoker();
		final Room invokerRoom = (invoker != null) ? invoker.location() : null;
		if((invoker==null)
		||(invokerRoom==null))
			return new Vector<Room>();

		if(depth==0)
			return new XVector<Room>(invokerRoom);
		final Vector<Room> rooms=new Vector<Room>();
		// needs to be area-only, because of the aggro-tracking rule
		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.OPENONLY)
				.plus(TrackingLibrary.TrackingFlag.AREAONLY)
				.plus(TrackingLibrary.TrackingFlag.NOAIR);
		CMLib.tracking().getRadiantRooms(invokerRoom, rooms, flags, null, depth, null);
		if(!rooms.contains(invokerRoom))
			rooms.addElement(invokerRoom);
		return rooms;
	}

	protected int getCorrectDirToOriginRoom(final Room R, final int v)
	{
		if(v<0)
			return -1;
		int dir=-1;
		Room R2=null;
		Exit E2=null;
		int lowest=v;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			R2=R.getRoomInDir(d);
			E2=R.getExitInDir(d);
			if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
			{
				final int dx=commonRoomSet.indexOf(R2);
				if((dx>=0)&&(dx<lowest))
				{
					lowest=dx;
					dir=d;
				}
			}
		}
		return dir;
	}

	protected String getCorrectMsgString(final Room R, final String str, final int v)
	{
		String msgStr=null;
		if(R==originRoom)
			msgStr=str;
		else
		{
			final int dir=this.getCorrectDirToOriginRoom(R,v);
			if(dir>=0)
				msgStr=L("^SYou see the @x1 being performed @x2!^?",danceOf(),CMLib.directions().getInDirectionName(dir));
			else
				msgStr=L("^SYou see the @x1 being performed nearby!^?",danceOf());
		}
		return msgStr;
	}

	public Set<MOB> sendMsgAndGetTargets(final MOB mob, final Room R, final CMMsg msg, final Environmental givenTarget, final boolean auto)
	{
		if(originRoom==R)
			R.send(mob,msg);
		else
			R.sendOthers(mob,msg);
		if(R!=originRoom)
			mob.setLocation(R);
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(R!=originRoom)
		{
			R.delInhabitant(mob);
			mob.setLocation(originRoom);
		}
		if(h==null)
			return null;
		if(R==originRoom)
		{
			if(!h.contains(mob))
				h.add(mob);
		}
		else
			h.remove(mob);
		return h;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((invoker()!=null)
		&&(!unInvoked)
		&&(affected==invoker())
		&&(msg.amISource(invoker()))
		&&(msg.target() instanceof Armor)
		&&(msg.targetMinor()==CMMsg.TYP_WEAR))
		{
			if(msg.source().location()!=null)
				msg.source().location().show(msg.source(),null,CMMsg.MSG_NOISE,L("<S-NAME> stop(s) dancing."));
			unInvoke();
		}
		super.executeMsg(host,msg);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof Dance)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		timeOut=0;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fumble(s) the @x1 due to <S-HIS-HER> armor!",name()));
			return false;
		}

		if(skipStandardDanceInvoke())
			return true;

		if((!auto)&&(!CMLib.flags().isAliveAwakeMobile(mob,false)))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final int newDepth = this.calculateNewSongDepth(mob);
		final boolean redance = mob.fetchEffect(ID())!=null;
		unDanceAll(mob,null,false,false); // because ALWAYS removing myself, depth needs pre-calculating.

		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			final int oldDepth = this.danceDepth;
			commonRoomSet=getInvokerScopeRoomSet(newDepth);
			this.danceDepth = newDepth;
			String str=auto?L("^SThe @x1 begins!^?",danceOf()):L("^S<S-NAME> begin(s) to dance the @x1.^?",danceOf());
			if((!auto) && (redance))
			{
				if(newDepth > oldDepth)
					str=L("^S<S-NAME> extend(s) the @x1`s range.^?",danceOf());
				else
					str=L("^S<S-NAME> start(s) the @x1 over again.^?",danceOf());
			}

			for(int v=0;v<commonRoomSet.size();v++)
			{
				final Room R=commonRoomSet.get(v);
				final String msgStr=getCorrectMsgString(R,str,v);
				final CMMsg msg=CMClass.getMsg(mob,null,this,somaticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					final Set<MOB> h=this.sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null)
						continue;
					invoker=mob;
					final Dance newOne=(Dance)this.copyOf();
					newOne.invoker=mob;
					newOne.invokerManaCost=-1;

					for (final Object element : h)
					{
						final MOB follower=(MOB)element;
						final Room R2=follower.location();

						// malicious dances must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_SOMANTIC_SPELL;
						int taffectType=CMMsg.MASK_MAGIC|CMMsg.MASK_EYES|CMMsg.TYP_CAST_SPELL;
						if((castingQuality(mob,follower)==Ability.QUALITY_MALICIOUS)&&(follower!=mob))
						{
							affectType=affectType|CMMsg.MASK_MALICIOUS;
							taffectType=taffectType|CMMsg.MASK_MALICIOUS;
						}
						if(auto)
						{
							affectType=affectType|CMMsg.MASK_ALWAYS;
							taffectType=taffectType|CMMsg.MASK_ALWAYS;
						}

						final Dance effectD = (Dance)follower.fetchEffect(this.ID());
						if(effectD!=null)
							effectD.danceDepth = this.danceDepth;
						else
						if((R2!=null)
						&&(CMLib.flags().canBeSeenBy(invoker,follower)))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null,taffectType,null,affectType,null);
							final CMMsg msg3=msg2;
							if((mindAttack())&&(follower!=mob))
								msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
							if((R.okMessage(mob,msg2))&&(R.okMessage(mob,msg3)))
							{
								R2.send(follower,msg2);
								if(msg2.value()<=0)
								{
									R2.send(follower,msg3);
									if(msg3.value()<=0)
									{
										//possiblyUndance(follower,null,false);
										newOne.setSavable(false);
										if(follower!=mob)
											follower.addEffect((Ability)newOne.copyOf());
										else
											follower.addEffect(newOne);
									}
								}
							}
						}
					}
					R.recoverRoomStats();
				}
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> make(s) a false step."));

		return success;
	}
}
