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
import com.planet_ink.coffee_mud.Items.interfaces.MusicalInstrument.InstrumentType;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2024 Bo Zimmerman

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
public class Play extends StdAbility
{
	@Override
	public String ID()
	{
		return "Play";
	}

	private final static String	localizedName	= CMLib.lang().L("a song played");

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

	private static final String[]	triggerStrings	= I(new String[] { "PLAY", "PL", "PLA" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected MusicalInstrument	instrument		= null;
	protected long				timeOut			= 0;
	protected List<Room>		commonRoomSet	= null;
	protected Room				originRoom		= null;
	protected volatile int		playDepth		= 0;

	protected volatile Pair<Double,Integer> bonusCache		= null;

	// needs to be area-only, because of the aggro-tracking rule
	private static final TrackingLibrary.TrackingFlags scopeFlags = CMLib.tracking().newFlags()
																	.plus(TrackingLibrary.TrackingFlag.OPENONLY)
																	.plus(TrackingLibrary.TrackingFlag.AREAONLY)
																	.plus(TrackingLibrary.TrackingFlag.NOAIR);

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SONG | Ability.DOMAIN_PLAYING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	protected boolean maliciousButNotAggressiveFlag()
	{
		return false;
	}

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
		pct += CMath.div(invoker().charStats().getStat(CharStats.STAT_INTELLIGENCE)+3, max);
		if(instrument!=null)
			pct+=CMath.div(instrument.phyStats().ability() * 2, max);
		return pct / 2.0;
	}

	protected int innerAvgStat()
	{
		final int max = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		if(invoker()==null)
			return max;
		double pct = CMath.div((invoker().charStats().getStat(CharStats.STAT_CHARISMA)+3)
							+ (invoker().charStats().getStat(CharStats.STAT_INTELLIGENCE)+3),2.0);
		if(instrument!=null)
			pct+=instrument.phyStats().ability();
		return (int)Math.round(pct);
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(2);
	}

	protected InstrumentType requiredInstrumentType()
	{
		return InstrumentType.OTHER_INSTRUMENT_TYPE;
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

	protected boolean persistentSong()
	{
		return true;
	}

	protected String songOf()
	{
		return name();
	}

	protected boolean HAS_QUANTITATIVE_ASPECT()
	{
		return true;
	}

	public String instrumentName()
	{
		if(instrument!=null)
			return instrument.name();
		return "something";
	}

	@Override
	public int adjustedLevel(final MOB mob, final int asLevel)
	{
		int level=super.adjustedLevel(mob,asLevel);
		if(mob != null)
		{
			level += (mob.charStats().getStat(CharStats.STAT_CHARISMA)-10)/4;
			level += (mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)-10)/5;
		}
		if(instrument!=null)
			level+=instrument.phyStats().ability();
		return level;
	}

	public int invokerLevel()
	{
		if(invoker()!=null)
		{
			if(instrument!=null)
				return invoker().phyStats().level()+instrument.phyStats().ability()+(getXLEVELLevel(invoker())*2);
			return invoker().phyStats().level()+(getXLEVELLevel(invoker())*2);
		}
		else
		if(affected!=null)
			return affected.phyStats().level();
		else
			return 1;
	}

	protected void inpersistentAffect(final MOB mob)
	{
	}

	public static boolean usingInstrument(final MusicalInstrument I, final MOB mob)
	{
		if((I==null)||(mob==null))
			return false;
		if(I instanceof Rideable)
		{
			return (((Rideable)I).amRiding(mob)
					&&(mob.fetchFirstWornItem(Wearable.WORN_WIELD)==null)
					&&(mob.fetchHeldItem()==null));
		}
		return mob.isMine(I)&&(I.amBeingWornProperly());
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((!super.tick(ticking,tickID))||(!(affected instanceof MOB)))
			return false;

		final MOB mob=(MOB)affected;
		if((affected==invoker())
		&&(invoker()!=null)
		&&(invoker().location()!=originRoom))
		{
			final List<Room> V=getInvokerScopeRoomSet(this.playDepth);
			commonRoomSet.clear();
			commonRoomSet.addAll(V);
			originRoom=invoker().location();
		}
		else
		if((abstractQuality()==Ability.QUALITY_MALICIOUS)
		&&(!maliciousButNotAggressiveFlag())
		&&(!mob.amDead())
		&&(mob.isMonster())
		&&(mob.amFollowing()==null)
		&&((!(mob instanceof Rideable))||(((Rideable)mob).numRiders()==0))
		&&(!mob.isInCombat())
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
			else
			if((mob.location().isInhabitant(invoker()))
			&&(CMLib.flags().canBeSeenBy(invoker(),mob)))
				CMLib.combat().postAttack(mob,invoker(),mob.fetchWieldedItem());
		}

		if((invoker==null)
		||(invoker.fetchEffect(ID())==null)
		||(commonRoomSet==null))
			return unPlayMe(mob,null, false);
		if(!commonRoomSet.contains(mob.location()))
		{
			final List<Room> V=getInvokerScopeRoomSet(this.playDepth);
			commonRoomSet.clear();
			commonRoomSet.addAll(V);
			if(!commonRoomSet.contains(mob.location()))
				return unPlayMe(mob,null, false);
		}

		if(skipStandardSongTick())
			return true;

		if((invoker==null)
		||((instrument!=null)&&(!usingInstrument(instrument,invoker)))
		||(!CMLib.flags().isAliveAwakeMobileUnbound(invoker,true))
		||(!CMLib.flags().canBeHeardSpeakingBy(invoker,mob)))
			return unPlayMe(mob,null, false);
		return true;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A instanceof Play)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public void affectPhyStats(final Physical affectedEnv, final PhyStats affectableStats)
	{
		if((this.invoker()==affectedEnv)&&(instrument!=null))
			affectableStats.addAmbiance("(?)playing "+songOf().toLowerCase()+" on "+instrument.name()+":playing "+instrument.name());
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected==invoker)
		&&(msg.amISource(invoker))
		&&(!unInvoked)
		&&(instrument!=null))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(!CMath.bset(msg.sourceMajor(), CMMsg.MASK_CHANNEL))
			&&(instrument.amWearingAt(Wearable.WORN_MOUTH)))
			{
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_NOISE,L("<S-NAME> stop(s) playing."));
				unInvoke();
			}
			else
			if(((msg.sourceMinor()==CMMsg.TYP_REMOVE)
			   ||(msg.sourceMinor()==CMMsg.TYP_WEAR)
			   ||(msg.sourceMinor()==CMMsg.TYP_WIELD))
			&&(instrument.amWearingAt(Wearable.WORN_HELD)))
			{
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_NOISE,L("<S-NAME> stop(s) playing."));
				unInvoke();
			}
		}
	}

	protected void unPlayAll(final MOB mob, final MOB invoker, final boolean exceptThisOne, final boolean noEcho)
	{
		if(mob!=null)
		{
			for(int a=mob.numEffects()-1;a>=0;a--)
			{
				final Ability A=mob.fetchEffect(a);
				if((A instanceof Play)
				&&((!exceptThisOne)||(!A.ID().equals(ID())))
				&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
					((Play)A).unPlayMe(mob,invoker, noEcho);
			}
		}
	}

	protected boolean unPlayMe(final MOB mob, final MOB invoker, final boolean noEcho)
	{
		if(mob==null)
			return false;
		final Ability A=mob.fetchEffect(ID());
		if((A instanceof Play)
		&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
		{
			if(noEcho)
			{
				A.unInvoke();
				return false;
			}
			else
			{
				final Play P=(Play)A;
				if(P.timeOut==0)
				{
					P.timeOut = System.currentTimeMillis()
							  + (CMProps.getTickMillis() * (((invoker()!=null)&&(invoker()!=mob))?super.getXTIMELevel(invoker()):0));
				}
				if(System.currentTimeMillis() >= P.timeOut)
				{
					A.unInvoke();
					return false;
				}
			}
		}
		return true;
	}

	public static MusicalInstrument getInstrument(final MOB mob, final InstrumentType requiredInstrumentType, final boolean noisy)
	{
		MusicalInstrument instrument=null;
		if(mob.riding() instanceof MusicalInstrument)
		{
			if(!usingInstrument((MusicalInstrument)mob.riding(),mob))
			{
				if(noisy)
					mob.tell(CMLib.lang().L("You need to free your hands to play @x1.",mob.riding().name()));
				return null;
			}
			instrument=(MusicalInstrument)mob.riding();
		}
		if(instrument==null)
		{
			for(int i=0;i<mob.numItems();i++)
			{
				final Item I=mob.getItem(i);
				if((I!=null)
				&&(I instanceof MusicalInstrument)
				&&(I.container()==null)
				&&(usingInstrument((MusicalInstrument)I,mob)))
				{
					instrument = (MusicalInstrument) I;
					break;
				}
			}
		}
		if(instrument==null)
		{
			if(noisy)
				mob.tell(CMLib.lang().L("You need an instrument!"));
			return null;
		}
		if((requiredInstrumentType!=InstrumentType.OTHER_INSTRUMENT_TYPE)
		&&(instrument.getInstrumentType()!=requiredInstrumentType))
		{
			if(noisy)
				mob.tell(CMLib.lang().L("This song can only be played on @x1.",requiredInstrumentType.name().toLowerCase()));
			return null;
		}
		return instrument;
	}

	protected int calculateNewSongDepth(final MOB invoker)
	{
		if((invoker!=null)
		&&(invoker.fetchEffect(ID())!=null))
		{
			final Play P=(Play)invoker.fetchAbility(ID());
			final int maxDepth = getXMAXRANGELevel(invoker()) / 2; // decreased because fireball
			final int songDepth = ((P!=null)?P.playDepth:this.playDepth) + 1;
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
			return new ArrayList<Room>();

		if(depth==0)
		{
			final List<Room> listR=new ArrayList<Room>(1);
			listR.add(invokerRoom);
			return listR;
		}
		final ArrayList<Room> rooms=new ArrayList<Room>();
		CMLib.tracking().getRadiantRooms(invokerRoom, rooms,scopeFlags, null, depth, null);
		if(!rooms.contains(invokerRoom))
			rooms.add(invokerRoom);
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
			String songOf=songOf();
			if(!songOf.equals(this.instrumentName()))
				songOf="the "+songOf;
			if(dir>=0)
				msgStr=L("^SYou hear @x1 being played @x2!^?",songOf,CMLib.directions().getInDirectionName(dir));
			else
				msgStr=L("^SYou hear @x1 being played nearby!^?",songOf);
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

	protected boolean isMine(final MOB mob)
	{
		return mob.isMine(this);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		timeOut=0;
		if(!auto)
		{
			instrument=getInstrument(mob,requiredInstrumentType(),true);
			if(instrument==null)
				return false;
			if((mob.riding()!=null)&&(mob.riding() instanceof MusicalInstrument))
			{
				if(!usingInstrument((MusicalInstrument)mob.riding(),mob))
				{
					mob.tell(L("You need to free your hands to play @x1.",mob.riding().name()));
					return false;
				}
				instrument=(MusicalInstrument)mob.riding();
			}
			if(instrument==null)
			{
				for(int i=0;i<mob.numItems();i++)
				{
					final Item I=mob.getItem(i);
					if((I!=null)
					&&(I instanceof MusicalInstrument)
					&&(I.container()==null)
					&&(usingInstrument((MusicalInstrument)I,mob)))
					{
						instrument = (MusicalInstrument) I;
						break;
					}
				}
			}
			if(instrument==null)
			{
				mob.tell(L("You need an instrument!"));
				return false;
			}
			if((requiredInstrumentType()!=InstrumentType.OTHER_INSTRUMENT_TYPE)
			&&(instrument.getInstrumentType()!=requiredInstrumentType()))
			{
				mob.tell(L("This song can only be played on @x1.",requiredInstrumentType().name().toLowerCase()));
				return false;
			}
		}

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(isMine(mob))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> fumble(s) playing @x1 due to <S-HIS-HER> armor!",name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(skipStandardSongInvoke())
			return true;

		if((!auto)&&(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false)))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final boolean replay = mob.fetchEffect(ID())!=null;
		final int newDepth = this.calculateNewSongDepth(mob);
		unPlayAll(mob,mob,false,false);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			final int oldDepth = this.playDepth;
			commonRoomSet=getInvokerScopeRoomSet(newDepth);
			this.playDepth = newDepth;
			String songOfStr=L("@x1 on ",songOf());
			if(songOf().equalsIgnoreCase(instrumentName()))
				songOfStr="";
			String str=auto?L("^S@x1 begins to play!^?",songOf()):L("^S<S-NAME> begin(s) to play @x1@x2.^?",songOfStr,instrumentName());
			if((!auto) && (replay))
			{
				if(newDepth > oldDepth)
					str=L("^S<S-NAME> extend(s) the @x1`s range.^?",songOf());
				else
					str=L("^S<S-NAME> start(s) playing @x1@x2 again.^?",songOfStr,instrumentName());
			}
			for(int v=0;v<commonRoomSet.size();v++)
			{
				final Room R=commonRoomSet.get(v);
				final String msgStr=getCorrectMsgString(R,str,v);
				final CMMsg msg=CMClass.getMsg(mob,null,this,somaticCastCode(mob,null,auto),msgStr);
				if(R.okMessage(mob,msg))
				{
					final Play newOne=(Play)this.copyOf();

					final Set<MOB> h=this.sendMsgAndGetTargets(mob, R, msg, givenTarget, auto);
					if(h==null)
						continue;

					for (final Object element : h)
					{
						final MOB follower=(MOB)element;
						if(follower==null)
							continue;
						final Room R2=follower.location();
						if(R2==null)
							continue;

						// malicious songs must not affect the invoker!
						int msgType=CMMsg.MASK_MAGIC|CMMsg.MASK_SOUND|CMMsg.TYP_CAST_SPELL;
						int mndMsgType=CMMsg.MASK_MAGIC|CMMsg.MASK_SOUND|CMMsg.MASK_MALICIOUS|CMMsg.TYP_MIND;
						if(auto)
						{
							msgType|=CMMsg.MASK_ALWAYS;
							mndMsgType|=CMMsg.MASK_ALWAYS;
						}
						if((castingQuality(mob,follower)==Ability.QUALITY_MALICIOUS)&&(follower!=mob))
							msgType=msgType|CMMsg.MASK_MALICIOUS;

						final Play effectP = (Play)follower.fetchEffect(this.ID());
						if(effectP!=null)
							effectP.playDepth = this.playDepth;
						else
						if(CMLib.flags().canBeHeardSpeakingBy(invoker,follower))
						{
							CMMsg msg2=CMClass.getMsg(mob,follower,this,msgType|CMMsg.MASK_HANDS,null,msgType,null,msgType,null);
							final CMMsg msg3=msg2;
							if((mindAttack())&&(follower!=mob))
								msg2=CMClass.getMsg(mob,follower,this,mndMsgType|CMMsg.MASK_HANDS,null,mndMsgType,null,mndMsgType,null);
							if((R.okMessage(mob,msg2))&&(R2.okMessage(mob,msg3)))
							{
								R2.send(follower,msg2);
								if(msg2.value()<=0)
								{
									R2.send(follower,msg3);
									if(msg3.value()<=0)
									{
										if(persistentSong())
										{
											newOne.setSavable(false);
											if(follower!=mob)
												follower.addEffect((Ability)newOne.copyOf());
											else
												follower.addEffect(newOne);
										}
										else
											inpersistentAffect(follower);
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
