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
   Copyright 2003-2018 Bo Zimmerman

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
	protected Vector<Room>		commonRoomSet	= null;
	protected Room				originRoom		= null;

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
	public int adjustedLevel(MOB mob, int asLevel)
	{
		int level=super.adjustedLevel(mob,asLevel);
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

	protected void inpersistentAffect(MOB mob)
	{
	}

	public static boolean usingInstrument(MusicalInstrument I, MOB mob)
	{
		if((I==null)||(mob==null))
			return false;
		if(I instanceof Rideable)
		{
			return (((Rideable)I).amRiding(mob)
					&&(mob.fetchFirstWornItem(Wearable.WORN_WIELD)==null)
					&&(mob.fetchHeldItem()==null));
		}
		return mob.isMine(I)&&(!I.amWearingAt(Wearable.IN_INVENTORY));
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!super.tick(ticking,tickID))||(!(affected instanceof MOB)))
			return false;

		final MOB mob=(MOB)affected;
		if((affected==invoker())&&(invoker()!=null)&&(invoker().location()!=originRoom))
		{
			final Vector<Room> V=getInvokerScopeRoomSet(null);
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
			return unplayMe(mob,null);
		if(!commonRoomSet.contains(mob.location()))
		{
			final List<Room> V=getInvokerScopeRoomSet(null);
			commonRoomSet.clear();
			commonRoomSet.addAll(V);
			if(!commonRoomSet.contains(mob.location()))
				return unplayMe(mob,null);
		}

		if(skipStandardSongTick())
			return true;

		if((invoker==null)
		||((instrument!=null)&&(!usingInstrument(instrument,invoker)))
		||(!CMLib.flags().isAliveAwakeMobileUnbound(invoker,true))
		||(!CMLib.flags().canBeHeardSpeakingBy(invoker,mob)))
			return unplayMe(mob,null);
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
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
	public void affectPhyStats(Physical affectedEnv, PhyStats affectableStats)
	{
		if((this.invoker()==affectedEnv)&&(instrument!=null))
			affectableStats.addAmbiance("(?)playing "+songOf().toLowerCase()+" on "+instrument.name()+":playing "+instrument.name());
		super.affectPhyStats(affectedEnv, affectableStats);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
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

	protected void unplayAll(MOB mob, MOB invoker)
	{
		if(mob!=null)
		{
			for(int a=mob.numEffects()-1;a>=0;a--)
			{
				final Ability A=mob.fetchEffect(a);
				if((A instanceof Play)
				&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
					((Play)A).unplayMe(mob,invoker);
			}
		}
	}

	protected void unplayAllByThis(MOB mob, MOB invoker)
	{
		if(mob!=null)
		{
			for(int a=mob.numEffects()-1;a>=0;a--)
			{
				final Ability A=mob.fetchEffect(a);
				if((A instanceof Play)
				&&(!A.ID().equals(ID()))
				&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
					((Play)A).unplayMe(mob,invoker);
			}
		}
	}

	protected boolean unplayMe(MOB mob, MOB invoker)
	{
		if(mob==null)
			return false;
		final Ability A=mob.fetchEffect(ID());
		if((A instanceof Play)
		&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
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
		return true;
	}

	public static MusicalInstrument getInstrument(MOB mob, InstrumentType requiredInstrumentType, boolean noisy)
	{
		MusicalInstrument instrument=null;
		if((mob.riding()!=null)&&(mob.riding() instanceof MusicalInstrument))
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

	protected Vector<Room> getInvokerScopeRoomSet(MOB backupMob)
	{
		if((invoker()==null)
		||(invoker().location()==null))
		{
			if((backupMob!=null)&&(backupMob.location()!=null))
				 return new XVector<Room>(backupMob.location());
			return new Vector<Room>();
		}
		final int depth=super.getXMAXRANGELevel(invoker()) / 2; // decreased because fireball
		if(depth==0)
			return new XVector<Room>(invoker().location());
		final Vector<Room> rooms=new Vector<Room>();
		// needs to be area-only, because of the aggro-tracking rule
		TrackingLibrary.TrackingFlags flags;
		flags = CMLib.tracking().newFlags()
				.plus(TrackingLibrary.TrackingFlag.OPENONLY)
				.plus(TrackingLibrary.TrackingFlag.AREAONLY)
				.plus(TrackingLibrary.TrackingFlag.NOAIR);
		CMLib.tracking().getRadiantRooms(invoker().location(), rooms,flags, null, depth, null);
		if(!rooms.contains(invoker().location()))
			rooms.addElement(invoker().location());
		return rooms;
	}

	protected int getCorrectDirToOriginRoom(Room R, int v)
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

	protected String getCorrectMsgString(Room R, String str, int v)
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

	public Set<MOB> sendMsgAndGetTargets(MOB mob, Room R, CMMsg msg, Environmental givenTarget, boolean auto)
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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
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
		&&(mob.isMine(this))
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
		unplayAll(mob,mob);
		if(success)
		{
			invoker=mob;
			originRoom=mob.location();
			commonRoomSet=getInvokerScopeRoomSet(null);
			String songOfStr=L("@x1 on ",songOf());
			if(songOf().equalsIgnoreCase(instrumentName()))
				songOfStr="";
			String str=auto?L("^S@x1 begins to play!^?",songOf()):L("^S<S-NAME> begin(s) to play @x1@x2.^?",songOfStr,instrumentName());
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str=L("^S<S-NAME> start(s) playing @x1@x2 again.^?",songOfStr,instrumentName());
			for(int v=0;v<commonRoomSet.size();v++)
			{
				final Room R=commonRoomSet.elementAt(v);
				final String msgStr=getCorrectMsgString(R,str,v);
				final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),msgStr);
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

						if(CMLib.flags().canBeHeardSpeakingBy(invoker,follower)
						&&(follower.fetchEffect(this.ID())==null))
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
									if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
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
