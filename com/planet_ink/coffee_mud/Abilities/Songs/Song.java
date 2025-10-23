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
   Copyright 2001-2025 Bo Zimmerman

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
public class Song extends StdAbility
{
	@Override
	public String ID()
	{
		return "Song";
	}

	private final static String	localizedName	= CMLib.lang().L("a Song");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "(" + songOf() + ")";
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

	private static final String[]	triggerStrings	= I(new String[] { "SING", "SI" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SONG | Ability.DOMAIN_SINGING;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(2);
	}

	protected boolean HAS_QUANTITATIVE_ASPECT()
	{
		return true;
	}

	protected boolean skipStandardSongInvoke()
	{
		return false;
	}

	protected boolean mindAttack()
	{
		return abstractQuality() == Ability.QUALITY_MALICIOUS;
	}

	protected boolean skipStandardSongTick()
	{
		return false;
	}

	protected boolean maliciousButNotAggressiveFlag()
	{
		return false;
	}

	protected boolean skipSimpleStandardSongTickToo()
	{
		return false;
	}

	protected String songOf()
	{
		return L("Song of ") + name();
	}

	protected volatile long			timeOut			= 0;
	protected volatile List<Room>	commonRoomSet	= null;
	protected volatile Room			originRoom		= null;
	protected volatile int			songDepth		= 0;
	protected volatile int			lyricCtr		= 0;
	protected volatile int			lyricPauseCtdn	= 0;

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
		return CMath.div(invoker().charStats().getStat(CharStats.STAT_CHARISMA), max);
	}

	protected int innerAvgStat()
	{
		if(invoker()==null)
			return CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		return invoker().charStats().getStat(CharStats.STAT_CHARISMA);
	}

	@Override
	public int adjustedLevel(final MOB mob, final int asLevel)
	{
		final int level=super.adjustedLevel(mob,asLevel);
		if(mob != null)
		{
			final int charisma=(mob.charStats().getStat(CharStats.STAT_CHARISMA)-10);
			if(charisma>10)
				return level+(charisma/3);
		}
		return level;
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		if(this.invoker()==affectedEnv)
			affectableStats.addAmbiance("(?)singing the "+songOf().toLowerCase());
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected==invoker)
		&&(msg.amISource(invoker))
		&&(!unInvoked))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)
				||((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)&&(CMath.bset(msg.sourceCode(), CMMsg.MASK_SOUND))))
			&&(!msg.sourceMajor(CMMsg.MASK_HANDS)) // sign language
			&&(!(msg.tool() instanceof Song))
			&&(!(msg.tool() instanceof Play))
			&&(!(msg.tool() instanceof Dance))
			&&(!msg.sourceMajor(CMMsg.MASK_CHANNEL)))
			{
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_NOISE,L("<S-NAME> stop(s) singing."));
				unInvoke();
			}
			else
			if((msg.target() instanceof Armor)
			&&(msg.targetMinor()==CMMsg.TYP_WEAR)
			&&(CMath.bset(((Armor)msg.target()).rawProperLocationBitmap(),Wearable.WORN_MOUTH)))
			{
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_NOISE,L("<S-NAME> stop(s) singing."));
				unInvoke();
			}
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof Song)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@SuppressWarnings("unchecked")
	protected List<String> getLyrics()
	{
		Map<String,List<String>> lyricMap=(Map<String,List<String>>)Resources.getResource("SYSTEM_SONG_LYRICS");
		if(lyricMap==null)
		{
			lyricMap=new TreeMap<String,List<String>>();
			final List<String> lines=Resources.getFileLineVector(new CMFile(Resources.buildResourcePath("skills/songlyrics.txt"),null).text());
			if(lines.size()>0)
			{
				List<String> current=null;
				for(String line : lines)
				{
					line=line.trim();
					if(line.startsWith("[")&&(line.endsWith("]")))
					{
						final String ID=line.substring(1,line.length()-1);
						Ability A=CMClass.getAbility(ID);
						if(A==null)
							A=CMClass.getAbility("Song_"+ID);
						if(A!=null)
						{
							current=new ArrayList<String>();
							lyricMap.put(A.ID(),current);
						}
						else
						{
							Log.errOut("Song","Unknown song class: "+ID);
							current=null;
						}
					}
					else
					if((current!=null)&&(line.length()>0))
						current.add(line);
				}
			}
		}
		return lyricMap.get(ID());
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((!super.tick(ticking,tickID))||(!(affected instanceof MOB)))
			return false;

		if(skipSimpleStandardSongTickToo())
			return true;

		final MOB mob=(MOB)affected;
		if((affected==invoker())
		&&(invoker()!=null)
		&&(invoker().location()!=originRoom))
		{
			final List<Room> V=getInvokerScopeRoomSet(this.songDepth);
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
		&&(mob.amFollowing()==null)
		&&((!(mob instanceof Rideable))||(((Rideable)mob).numRiders()==0))
		&&(!CMLib.flags().isATrackingMonster(mob))
		&&(CMLib.flags().isAliveAwakeMobile(mob,true)))
		{
			if((mob.location()!=originRoom)
			&&(CMLib.flags().isMobile(mob)))
			{
				final int dir=this.getCorrectDirToOriginRoom(mob.location(),commonRoomSet.indexOf(mob.location()));
				if(dir>=0)
					CMLib.tracking().walk(mob,dir,false,false);
			}
			if((mob.location().isInhabitant(invoker()))
			&&(CMLib.flags().canBeSeenBy(invoker(),mob)))
				CMLib.combat().postAttack(mob,invoker(),mob.fetchWieldedItem());
		}

		if((invoker==null)
		||(invoker.fetchEffect(ID())==null)
		||(commonRoomSet==null))
			return unSingMe(mob,null, false);
		if(!commonRoomSet.contains(mob.location()))
		{
			final List<Room> V=getInvokerScopeRoomSet(this.songDepth);
			commonRoomSet.clear();
			commonRoomSet.addAll(V);
			if(!commonRoomSet.contains(mob.location()))
				return unSingMe(mob,null, false);
		}

		if(invoker==affected)
		{
			final List<String> lyrics=this.getLyrics();
			final MOB invoker=this.invoker;
			if((lyrics!=null)
			&& (invoker==affected))
			{
				if(lyricPauseCtdn>0)
					lyricPauseCtdn--;
				else
				{
					if(lyricCtr >= lyrics.size())
						lyricCtr = 0;
					final String line = lyrics.get(lyricCtr++);
					final Room R=invoker.location();
					if((R!=null)
					&&(line.length()>0))
					{
						if((line.length()>=7)
						&&(line.substring(0,6).toUpperCase().startsWith("PAUSE "))
						&&(CMath.isInteger(line.substring(6))))
							lyricPauseCtdn = CMath.s_int(line.substring(6));
						else
							R.show(invoker, null, this, CMMsg.MSG_SPEAK, L("<S-NAME> sing(s) '@x1'",line));
					}
				}
			}
		}

		if(skipStandardSongTick())
			return true;

		if((invoker==null)
		||(!CMLib.flags().isAliveAwakeMobile(invoker,true))
		||(!CMLib.flags().canBeHeardSpeakingBy(invoker,mob)))
			return unSingMe(mob,null, false);
		return true;
	}

	protected void unSingAll(final MOB mob, final MOB invoker, final boolean exceptThisOne, final boolean noEcho)
	{
		if(mob!=null)
		{
			for(int a=mob.numEffects()-1;a>=0;a--)
			{
				final Ability A=mob.fetchEffect(a);
				if((A instanceof Song)
				&&((!exceptThisOne)||(!A.ID().equals(ID())))
				&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
					((Song)A).unSingMe(mob,invoker, noEcho);
			}
		}
	}

	protected boolean unSingMe(final MOB mob, final MOB invoker, final boolean noEcho)
	{
		if(mob==null)
			return false;
		final Ability A=mob.fetchEffect(ID());
		final MOB invokerM=invoker();
		if((A instanceof Song)
		&&((invokerM==null)||(A.invoker()==null)||(invoker==null)||(A.invoker()==invoker)))
		{
			if(noEcho)
			{
				A.unInvoke();
				return false;
			}
			else
			{
				final Song S=(Song)A;
				if(S.timeOut==0)
				{
					S.timeOut = System.currentTimeMillis()
							  + (CMProps.getTickMillis() * (((invokerM!=null)&&(invokerM!=mob))?super.getXTIMELevel(invokerM):0));
				}
				if(System.currentTimeMillis() >= S.timeOut)
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
			final Song S=(Song)invoker.fetchAbility(ID());
			final int maxDepth = getXMAXRANGELevel(invoker()) / 2; // decreased because fireball
			final int songDepth = ((S!=null)?S.songDepth:this.songDepth) + 1;
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
		CMLib.tracking().getRadiantRooms(invokerRoom, rooms,flags, null, depth, null);
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
				msgStr=L("^SYou hear the @x1 being sung @x2!^?",songOf(),CMLib.directions().getInDirectionName(dir));
			else
				msgStr=L("^SYou hear the @x1 being sung nearby!^?",songOf());
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		timeOut = 0;
		lyricCtr = 0;
		lyricPauseCtdn = 0;
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
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> hit(s) a foul note on @x1 due to <S-HIS-HER> armor!",name()));
			return false;
		}

		if(skipStandardSongInvoke())
			return true;

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell(L("You can't sing!"));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);
		unSingAll(mob,mob,true,false);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			final int oldDepth = this.songDepth;
			final int newDepth = this.calculateNewSongDepth(mob);
			commonRoomSet=getInvokerScopeRoomSet(newDepth);
			this.songDepth = newDepth;
			String str=auto?L("^SThe @x1 begins to play!^?",songOf()):L("^S<S-NAME> begin(s) to sing the @x1.^?",songOf());
			if((!auto)
			&&(mob.fetchEffect(this.ID())!=null))
			{
				if(newDepth > oldDepth)
					str=L("^S<S-NAME> extend(s) the @x1`s range.^?",songOf());
				else
					str=L("^S<S-NAME> start(s) the @x1 over again.^?",songOf());
			}
			for(int v=0;v<commonRoomSet.size();v++)
			{
				final Room R=commonRoomSet.get(v);
				final String msgStr=getCorrectMsgString(R,str,v);
				final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					final Set<MOB> h=this.sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null)
						continue;
					final Song newOne=(Song)this.copyOf();
					for (final Object element : h)
					{
						final MOB follower=(MOB)element;
						final Room R2=follower.location();
						if(R2==null)
							continue;

						// malicious songs must not affect the invoker!
						int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
						if(auto)
							affectType=affectType|CMMsg.MASK_ALWAYS;
						if((castingQuality(mob,follower)==Ability.QUALITY_MALICIOUS)&&(follower!=mob))
							affectType=affectType|CMMsg.MASK_MALICIOUS;

						final Song effectS = (Song)follower.fetchEffect(this.ID());
						if(effectS!=null)
							effectS.songDepth = this.songDepth;
						else
						if(CMLib.flags().canBeHeardSpeakingBy(invoker,follower))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
							final CMMsg msg3=msg2;
							if((mindAttack())&&(follower!=mob))
								msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
							if((R.okMessage(mob,msg2))&&(R2.okMessage(mob,msg3)))
							{
								R.send(follower,msg2);
								if(msg2.value()<=0)
								{
									R2.send(follower,msg3);
									if(msg3.value()<=0)
									{
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
			mob.location().show(mob,null,CMMsg.MSG_NOISE,L("<S-NAME> hit(s) a foul note."));

		return success;
	}
}
