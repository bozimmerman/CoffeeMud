package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AchievementLoadFlag;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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

public class Age extends StdAbility
{
	@Override
	public String ID()
	{
		return "Age";
	}

	private final static String	localizedName	= CMLib.lang().L("Age");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS | CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public String accountForYourself()
	{
		return displayText();
	}

	@Override
	public String displayText()
	{
		final long start=CMath.s_long(text());
		if(start<Short.MAX_VALUE)
			return "";
		final TimeClock C=CMLib.time().localClock(affected);
		final long days=((System.currentTimeMillis()-start)/CMProps.getTickMillis())/CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY); // down to days;
		final long months=days/C.getDaysInMonth();
		final long years=months/C.getMonthsInYear();
		if(days<1)
			return "(<1 day old)";
		else
		if(months<1)
			return "("+days+" day(s) old)";
		else
		if(years<1)
			return "("+months+" month(s) old)";
		else
			return "("+years+" year(s) old)";
	}
	
	protected boolean			norecurse		= false;
	protected Race				myRace			= null;
	protected double			divisor			= 0.0;
	protected long				lastSoiling		= 0;
	protected long				lastFollowCheck	= 0;

	public final static String happyBabyEmoter="min=1 max=500 chance=100;makes goo goo noises.;loves its mommy.;loves its daddy.;smiles.;makes a spit bubble.;wiggles its toes.;chews on their finger.;holds up a finger.;stretches its little body.";
	public final static String otherBabyEmoter="min=1 max=5 chance=10;wants its mommy.;wants its daddy.;cries.;doesnt like you.;cries for its mommy.;cries for its daddy.";
	public final static String downBabyEmoter="min=1 max=2 chance=50;wants its mommy.;wants its daddy.;cries.;cries!;cries.";

	protected Race getMyRace()
	{
		if((myRace==null)&&(affected != null))
		{
			if(affected instanceof CagedAnimal)
			{
				final MOB M=((CagedAnimal)affected).unCageMe();
				if(M!=null)
				{
					myRace=M.baseCharStats().getMyRace();
					M.delEffect(M.fetchEffect(ID()));
					M.destroy();
				}
				else
				{
					final Room R=CMLib.map().roomLocation(affected);
					if(R!=null)
						R.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 died.",affected.name()));
					((Item)affected).destroy();
				}
			}
			else
			if(affected instanceof MOB)
				myRace=((MOB)affected).charStats().getMyRace();
		}
		return myRace;
	}

	protected MOB getFollowing(Environmental babe)
	{
		MOB following=null;
		if(babe instanceof MOB)
			following=((MOB)babe).amFollowing();
		else
		if((babe instanceof Item)
		&&(((Item)babe).owner() instanceof MOB)
		&&(CMLib.flags().isInTheGame((MOB)((Item)babe).owner(),true)))
			following=(MOB)((Item)babe).owner();
		final Room room=CMLib.map().roomLocation(babe);
		if((room!=null)&&(following==null))
		{
			boolean lastFollowEllapsed=false;
			if(lastFollowCheck>0)
				lastFollowEllapsed=(System.currentTimeMillis()-lastFollowCheck)>(60 * 60 * 1000);
			MOB babeM = null;
			if((babe instanceof MOB)
			&&(CMLib.flags().isAnimalIntelligence((MOB)babe) || lastFollowEllapsed)
			&&(((MOB)babe).isMonster()))
				babeM=(MOB)babe;
			else
			if((!(babe instanceof CagedAnimal))
			||(!lastFollowEllapsed && !CMLib.flags().isAnimalIntelligence(babeM=((CagedAnimal)babe).unCageMe())))
				babeM=null;
			if(babeM!=null)
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if((M!=null)
				&&(M!=babe)
				&&(babeM.description().toUpperCase().indexOf(M.Name().toUpperCase())>=0))
					following=M;
			}
			if((following!=null)&&(babe instanceof MOB))
				CMLib.commands().postFollow((MOB)babe, following, true);
		}
		if((following==null)&&(lastFollowCheck==0))
			lastFollowCheck=System.currentTimeMillis();
		if((following!=null)&&(babe.description().toUpperCase().indexOf(following.Name().toUpperCase())<0)&&(room!=null))
		{
			MOB M=null;
			final Vector<MOB> choices=new Vector<MOB>();
			for(int i=0;i<room.numInhabitants();i++)
			{
				M=room.fetchInhabitant(i);
				if((M!=null)
				&&(M!=babe)
				&&(M!=following)
				&&(babe.description().toUpperCase().indexOf(following.Name().toUpperCase())>=0))
				{
					if(M.isMonster())
						choices.addElement(M);
					else
					if(choices.size()==0)
						choices.addElement(M);
					else
						choices.insertElementAt(M,0);
				}
			}
			if(choices.size()>0)
			{
				if(babe instanceof MOB)
					((MOB)babe).setFollowing(choices.firstElement());
				following=choices.firstElement();
			}
		}
		return following;
	}

	protected void doThang()
	{
		final Physical affected=this.affected;
		if(affected==null)
			return;
		if(text().length()==0)
			return;
		final long l=CMath.s_long(text());
		if(l==0)
			return;
		if(norecurse)
			return;
		if(l<Short.MAX_VALUE)
			return;
		norecurse=true;

		if(divisor==0.0)
		{
			final TimeClock C=CMLib.time().localClock(affected);
			divisor = C.getMonthsInYear() *C.getDaysInMonth() * CMProps.getIntVar( CMProps.Int.TICKSPERMUDDAY );
		}

		final int ellapsed=(int)Math.round(Math.floor(CMath.div(CMath.div(System.currentTimeMillis()-l,CMProps.getTickMillis()),divisor)));
		if((affected instanceof Item)
		&&(affected instanceof CagedAnimal)
		&&(!(affected instanceof DeadBody)))
		{
			((Item)affected).setExpirationDate(0);
			if(getMyRace()==null)
				return;
			if(ellapsed>=myRace.getAgingChart()[1])
			{
				final Room R=CMLib.map().roomLocation(affected);
				if(R!=null)
				{
					final Item I=(Item)affected;
					MOB following=getFollowing(I);
					if(following==null)
					{
						norecurse=false;
						return;
					}

					final CagedAnimal C=(CagedAnimal)affected;
					final MOB babe=C.unCageMe();
					if((babe==null)||(babe.baseCharStats()==null))
					{
						R.showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 JUST DIED OF DEFORMITIES!!",affected.name()));
						((Item)affected).destroy();
					}
					else
					{
						MOB leigeM=following;
						Set<MOB> parents=new HashSet<MOB>();
						for(Enumeration<Tattoo> t= babe.tattoos();t.hasMoreElements();)
						{
							final Tattoo T=t.nextElement();
							if(T.name().startsWith("PARENT:"))
							{
								String parentName=T.name().substring(7).trim();
								if(CMLib.players().playerExists(parentName))
									parents.add(CMLib.players().getLoadPlayer(parentName));
								else
								{
									MOB M=R.fetchInhabitant("$"+parentName+"$");
									if(M!=null)
										parents.add(M);
								}
							}
						}
						if((!parents.contains(following))
						&&(parents.size()>0))
						{
							final Iterator<MOB> i=parents.iterator();
							final MOB M=i.next();
							if((M!=null)&&(!M.isMonster()))
							{
								leigeM=M;
								if(M.location()==R)
									following=M;
							}
						}
						babe.baseCharStats().setStat(CharStats.STAT_CHARISMA,10);
						babe.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,7);
						babe.baseCharStats().setStat(CharStats.STAT_DEXTERITY,3);
						babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,3);
						babe.baseCharStats().setStat(CharStats.STAT_STRENGTH,2);
						babe.baseCharStats().setStat(CharStats.STAT_WISDOM,2);
						babe.basePhyStats().setHeight(babe.basePhyStats().height()*2);
						babe.basePhyStats().setWeight(babe.basePhyStats().weight()*2);
						babe.baseState().setHitPoints(2);
						babe.baseState().setMana(10);
						babe.baseState().setMovement(20);
						babe.setLiegeID(leigeM.Name());
						babe.recoverCharStats();
						babe.recoverPhyStats();
						babe.recoverMaxState();
						final Age A=(Age)babe.fetchEffect(ID());
						if(A!=null)
							A.setMiscText(text());
						final Ability B=I.fetchEffect(ID());
						if(B!=null)
							I.delEffect(B);
						if(!CMLib.flags().isAnimalIntelligence(babe))
						{
							final Ability STAT=babe.fetchEffect("Prop_StatTrainer");
							if(STAT!=null)
								STAT.setMiscText("CHA=10 CON=7 DEX=3 INT=3 STR=2 WIS=2");
						}
						babe.text();
						babe.bringToLife(R,true);
						CMLib.beanCounter().clearZeroMoney(babe,null);
						babe.setMoneyVariation(0);
						babe.setFollowing(following);
						R.show(babe,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> JUST TOOK <S-HIS-HER> FIRST STEPS!!!"));
						I.delEffect(this);
						Runnable run=new Runnable()
						{
							final Item I2=I;

							@Override
							public void run()
							{
								final ItemPossessor poss = I2.owner();
								if(poss != null)
									poss.delItem(I2);
								I2.setOwner(null);
								I2.destroy();
							}
						};
						run.run();
						// this oughta kill it, but good.
						CMLib.threads().scheduleRunnable(run, 100);
						CMLib.threads().scheduleRunnable(run, 1000);
						if(!CMLib.flags().isAnimalIntelligence(babe))
							CMLib.database().DBReCreatePlayerData(leigeM.Name(),"HEAVEN",leigeM.Name()+"/HEAVEN/"+text(),babe.ID()+"/"+babe.basePhyStats().ability()+"/"+babe.text());
					}
				}
			}
		}
		else
		if((affected instanceof MOB)
		&&(((MOB)affected).amFollowing()!=null)
		&&(((MOB)affected).amFollowing().playerStats()!=null)
		&&(!((MOB)affected).amFollowing().isMonster())
		&&(((MOB)affected).location().isInhabitant((MOB)affected))
		&&(((MOB)affected).location().isInhabitant(((MOB)affected).amFollowing())))
		{
			final MOB babe=(MOB)affected;
			final MOB following=getFollowing(babe);
			if(getMyRace()==null)
				return;
			if((babe.getLiegeID().length()==0)&&(!following.getLiegeID().equals(affected.Name())))
				babe.setLiegeID(following.Name());
			babe.setAttribute(MOB.Attrib.AUTOASSIST,false);
			if((ellapsed>=myRace.getAgingChart()[2])
			&&(babe.fetchBehavior("MudChat")==null))
			{
				final Room R=CMLib.map().roomLocation(affected);
				if(R!=null)
				{
					if(babe.Name().indexOf(' ')>0)
					{
						final String name=CMLib.english().startWithAorAn(getMyRace().makeMobName((char)babe.baseCharStats().getStat(CharStats.STAT_GENDER), 3)).toLowerCase();
						babe.setName(name);
						babe.setDisplayText(L("@x1 is here.",name));
					}
					babe.baseCharStats().setStat(CharStats.STAT_CHARISMA,10);
					babe.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,10);
					babe.baseCharStats().setStat(CharStats.STAT_DEXTERITY,5);
					babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,6);
					babe.baseCharStats().setStat(CharStats.STAT_STRENGTH,6);
					babe.baseCharStats().setStat(CharStats.STAT_WISDOM,6);
					if(following!=null)
						babe.copyFactions(following);
					babe.basePhyStats().setHeight(babe.basePhyStats().height()*5);
					babe.basePhyStats().setWeight(babe.basePhyStats().weight()*5);
					babe.baseState().setHitPoints(4);
					babe.baseState().setMana(25);
					babe.baseState().setMovement(50);
					final Behavior B=CMClass.getBehavior("MudChat");
					if(B!=null)
						babe.addBehavior(B);
					else
						babe.delEffect(this);
					babe.recoverCharStats();
					babe.recoverPhyStats();
					babe.recoverMaxState();
					babe.text();
					if(following!=null)
						CMLib.database().DBReCreatePlayerData(following.Name(),"HEAVEN",following.Name()+"/HEAVEN/"+text(),babe.ID()+"/"+babe.basePhyStats().ability()+"/"+babe.text());
				}
			}
			else
			if((ellapsed>=myRace.getAgingChart()[3])
			&&(babe.fetchBehavior("MudChat")!=null)
			&&(babe.charStats().getStat(CharStats.STAT_INTELLIGENCE)>1))
			{
				Ability A=babe.fetchEffect("Prop_SafePet");
				if(A!=null)
					babe.delEffect(A);
				CMLib.database().DBDeletePlayerData(following.Name(),"HEAVEN",following.Name()+"/HEAVEN/"+text());

				final Room R=CMLib.map().roomLocation(affected);
				if((R!=null)
				&&(affected.Name().indexOf(' ')<0)
				&&(!CMLib.players().playerExists(affected.Name())))
				{
					MOB liege=null;
					if(babe.getLiegeID().length()>0)
						liege=CMLib.players().getLoadPlayer(babe.getLiegeID());
					if(liege==null)
						liege=babe.amFollowing();
					final MOB newMan=CMClass.getMOB("StdMOB");
					newMan.setAgeMinutes(babe.getAgeMinutes());
					newMan.setBaseCharStats(babe.baseCharStats());
					newMan.setBasePhyStats(babe.basePhyStats());
					newMan.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
					if(liege!=null)
						newMan.copyFactions(liege);
					newMan.basePhyStats().setLevel(1);
					newMan.setAttributesBitmap(babe.getAttributesBitmap());
					for(final Enumeration<Tattoo> e=babe.tattoos();e.hasMoreElements();)
						newMan.addTattoo(e.nextElement());
					String highestBaseClass="Orphan";
					final int highestBaseLevel=0;
					int highestParentLevel=0;
					for(final Pair<Clan,Integer> p : babe.clans())
						newMan.setClan(p.first.clanID(),p.second.intValue());
					int theme=Area.THEME_FANTASY;
					int highestLegacyLevel=0;
					final List<MOB> parents=new ArrayList<MOB>(2);
					for(final Enumeration<Tattoo> e=newMan.tattoos();e.hasMoreElements();)
					{
						final Tattoo T=e.nextElement();
						if(T.getTattooName().startsWith("PARENT:"))
						{
							final MOB M=CMLib.players().getLoadPlayer(T.getTattooName().substring(7));
							if(M!=null)
							{
								parents.add(M);
								if(M.basePhyStats().level()>highestParentLevel)
									highestParentLevel=M.basePhyStats().level();
								for(int i=0;i<M.baseCharStats().numClasses();i++)
								{
									if(M.baseCharStats().getClassLevel(M.baseCharStats().getMyClass(i))>highestBaseLevel)
										highestBaseClass=M.baseCharStats().getMyClass(i).baseClass();
								}
								if(!newMan.clans().iterator().hasNext())
								{
									for(final Pair<Clan,Integer> p : CMLib.clans().findRivalrousClans(M))
									{
										final Pair<Clan,Integer> clanRole=M.getClanRole(p.first.clanID());
										if((clanRole!=null)
										&&(clanRole.first.getAuthority(clanRole.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
											newMan.setClan(p.first.clanID(),p.first.getAutoPosition());
									}
								}
								if((M.getWorshipCharID().length()>0)&&(newMan.getWorshipCharID().length()==0))
									newMan.setWorshipCharID(M.getWorshipCharID());
								for(final Enumeration<Ability> a=M.abilities();a.hasMoreElements();)
								{
									final Ability L=a.nextElement();
									if((L instanceof Language)&&(newMan.fetchAbility(L.ID())==null))
										newMan.addAbility((Ability)L.copyOf());
								}
								theme=M.playerStats().getTheme();
							}
						}
					}
					for(final Enumeration<Tattoo> e=newMan.tattoos();e.hasMoreElements();)
					{
						final Tattoo T=e.nextElement();
						if(T.getTattooName().startsWith("PARENT:"))
						{
							final MOB M=CMLib.players().getLoadPlayer(T.getTattooName().substring(7));
							if((M!=null)&&(M.playerStats()!=null))
							{
								final int legacyLevel=M.playerStats().getLegacyLevel(highestBaseClass);
								if(legacyLevel>highestLegacyLevel)
									highestLegacyLevel=legacyLevel;
							}
						}
					}
					if((!newMan.clans().iterator().hasNext())&&(liege!=null))
					{
						for(final Pair<Clan,Integer> p : CMLib.clans().findRivalrousClans(liege))
						{
							final Pair<Clan,Integer> clanRole=liege.getClanRole(p.first.clanID());
							if((clanRole!=null)
							&&(clanRole.first.getAuthority(clanRole.second.intValue(), Clan.Function.HOME_PRIVS)!=Clan.Authority.CAN_NOT_DO))
								newMan.setClan(p.first.clanID(),p.first.getAutoPosition());
						}
					}
					if(CMLib.clans().findRivalrousClan(newMan)!=null)
					{
						final Clan C = CMLib.clans().findRivalrousClan(newMan);
						if(C!=null)
							C.addMember(newMan, C.getGovernment().getAcceptPos());
					}
					newMan.setDescription(babe.description());
					newMan.setDisplayText(babe.displayText());
					newMan.setExperience(babe.getExperience());
					newMan.setFollowing(null);
					newMan.setLiegeID(babe.getLiegeID());
					newMan.setLocation(babe.location());
					CMLib.beanCounter().setMoney(newMan,CMLib.beanCounter().getMoney(babe));
					newMan.setName(babe.Name());
					newMan.setPractices(babe.getPractices());
					newMan.setQuestPoint(babe.getQuestPoint());
					newMan.setStartRoom(babe.getStartRoom());
					newMan.setTrains(babe.getTrains());
					newMan.setWimpHitPoint(babe.getWimpHitPoint());
					newMan.setWorshipCharID(babe.getWorshipCharID());
					if(liege!=null)
					{
						newMan.playerStats().setPassword(liege.playerStats().getPasswordStr());
						newMan.playerStats().setEmail(liege.playerStats().getEmail());
						newMan.playerStats().setAccount(liege.playerStats().getAccount());
					}
					else
						newMan.playerStats().setPassword(babe.Name());
					newMan.playerStats().setLastUpdated(System.currentTimeMillis());
					newMan.playerStats().setLastDateTime(System.currentTimeMillis());
					if(newMan.playerStats().getBirthday()==null)
					{
						int newAge=newMan.playerStats().initializeBirthday(CMLib.time().localClock(R),ellapsed*15,newMan.baseCharStats().getMyRace());
						if((newAge<0)||(newAge>newMan.baseCharStats().getMyRace().getAgingChart()[Race.AGE_MIDDLEAGED]))
							newAge=newMan.baseCharStats().getMyRace().getAgingChart()[Race.AGE_MIDDLEAGED];
						newMan.baseCharStats().setStat(CharStats.STAT_AGE,newAge);
					}
					newMan.baseCharStats().setStat(CharStats.STAT_AGE,ellapsed);
					newMan.baseState().setHitPoints(CMProps.getIntVar(CMProps.Int.STARTHP));
					newMan.baseState().setMana(CMProps.getIntVar(CMProps.Int.STARTMANA));
					newMan.baseState().setMovement(CMProps.getIntVar(CMProps.Int.STARTMOVE));
					newMan.baseCharStats().getMyRace().setHeightWeight(newMan.basePhyStats(),(char)newMan.baseCharStats().getStat(CharStats.STAT_GENDER));
					final int baseStat=(CMProps.getIntVar(CMProps.Int.BASEMINSTAT)+CMProps.getIntVar(CMProps.Int.BASEMAXSTAT))/2;
					for(int  i : CharStats.CODES.BASECODES())
						newMan.baseCharStats().setStat(i,baseStat);
					if(highestParentLevel>=CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL))
					{
						for(int i=0;i<highestLegacyLevel+1;i++)
							newMan.playerStats().addLegacyLevel(highestBaseClass);
					}
					//final int bonusPoints=newMan.playerStats().getTotalLegacyLevels()+1;
					final Ability reRollA=CMClass.getAbility("Prop_ReRollStats");
					if(reRollA!=null)
					{
						reRollA.setMiscText("PICKCLASS=TRUE");
						newMan.setSavable(true);
						newMan.addNonUninvokableEffect(reRollA);
					}
					newMan.recoverCharStats();
					newMan.baseCharStats().getMyRace().startRacing(newMan,false);
					newMan.playerStats().setTheme(theme);
					try
					{
						newMan.baseCharStats().setMyClasses(";" + CMLib.login().promptCharClass(theme, newMan, null).name());
					}
					catch (final IOException e)
					{
					}
					newMan.baseCharStats().setMyLevels(";1");
					newMan.baseCharStats().getCurrentClass().startCharacter(newMan,false,false);
					for(int i=0;i<babe.numItems();i++)
						newMan.moveItemTo(babe.getItem(i));
					CMLib.utensils().outfit(newMan,newMan.baseCharStats().getMyRace().outfit(newMan));
					CMLib.utensils().outfit(newMan,newMan.baseCharStats().getCurrentClass().outfit(newMan));
					newMan.playerStats().setLastDateTime(System.currentTimeMillis());
					newMan.playerStats().setLastUpdated(System.currentTimeMillis());
					newMan.recoverCharStats();
					newMan.recoverPhyStats();
					newMan.recoverMaxState();
					newMan.resetToMaxState();
					if(CMLib.flags().isAnimalIntelligence(newMan))
					{
						newMan.baseCharStats().setMyClasses(";StdCharClass");
						newMan.recoverCharStats();
						newMan.recoverPhyStats();
						newMan.recoverMaxState();
						newMan.resetToMaxState();
					}
					CMLib.achievements().loadAccountAchievements(newMan,AchievementLoadFlag.NORMAL);
					CMLib.database().DBCreateCharacter(newMan);
					CMLib.players().addPlayer(newMan);

					newMan.setSession((Session)CMClass.getCommon("DefaultSession"));
					CMLib.achievements().possiblyBumpAchievement(newMan, AchievementLibrary.Event.PLAYERBORN, 1);
					newMan.setSession(null);
					for(final MOB parentM : parents)
					{
						if(parentM.isPlayer())
							CMLib.achievements().possiblyBumpAchievement(parentM, AchievementLibrary.Event.PLAYERBORNPARENT, 1, newMan);
					}

					if((liege != null) && (liege.session() != null))
						newMan.playerStats().setLastIP(liege.session().getAddress());
					Log.sysOut("Age","Created user: "+newMan.Name());
					CMLib.login().notifyFriends(newMan,L("^X@x1 has just been created.^.^?",newMan.Name()));

					final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.NEWPLAYERS);
					for(int i=0;i<channels.size();i++)
						CMLib.commands().postChannel(channels.get(i),newMan.clans(),L("@x1 has just been created.",newMan.Name()),true);

					if(liege != null)
					{
						if(liege!=babe.amFollowing())
							babe.amFollowing().tell(L("@x1 has just grown up! @x2 password is the same as @x3's.",newMan.Name(),CMStrings.capitalizeAndLower(newMan.baseCharStats().hisher()),liege.Name()));
						liege.tell(L("@x1 has just grown up! @x2 password is the same as @x3's.",newMan.Name(),CMStrings.capitalizeAndLower(newMan.baseCharStats().hisher()),liege.Name()));
					}
					CMLib.database().DBUpdatePlayer(newMan);
					newMan.removeFromGame(false,true);
					babe.setFollowing(null);
					babe.destroy();
					final MOB fol=newMan.amFollowing();
					newMan.setFollowing(null);
					CMLib.database().DBUpdateFollowers(liege);
					newMan.setFollowing(fol);
				}
				else
				{
					MOB liege=null;
					if(babe.getLiegeID().length()>0)
						liege=CMLib.players().getLoadPlayer(babe.getLiegeID());
					if(liege==null)
						liege=babe.amFollowing();
					if(babe.Name().indexOf(' ')>0)
					{
						final String name=CMLib.english().startWithAorAn(getMyRace().makeMobName((char)babe.baseCharStats().getStat(CharStats.STAT_GENDER), 4)).toLowerCase();
						babe.setName(name);
						babe.setDisplayText(L("@x1 stands here.",name));
					}
					CMLib.database().DBDeletePlayerData(following.Name(),"HEAVEN",following.Name()+"/HEAVEN/"+text());
					if(liege!=babe.amFollowing())
						babe.amFollowing().tell(L("@x1 has just grown up to be a mob.",babe.Name()));
					liege.tell(L("@x1 has just grown up to be a mob.",babe.Name()));
					A=babe.fetchEffect(ID());
					A.setMiscText(""+ellapsed);
					babe.recoverCharStats();
					babe.recoverPhyStats();
					babe.recoverMaxState();
					babe.text();
				}
			}
		}
		else
		if((affected instanceof MOB)
		&&(((MOB)affected).amFollowing()!=null)
		&&(((MOB)affected).amFollowing().playerStats()==null)
		&&(((MOB)affected).amFollowing().isMonster())
		&&(((MOB)affected).location().isInhabitant((MOB)affected))
		&&(((MOB)affected).location().isInhabitant(((MOB)affected).amFollowing()))
		&&(CMLib.law().getLandOwnerName(((MOB)affected).location()).length()>0))
		{
			final MOB babe=(MOB)affected;
			if(getMyRace()==null)
				return;
			babe.setAttribute(MOB.Attrib.AUTOASSIST,true);
			if(ellapsed>=myRace.getAgingChart()[2])
			{
				final Room R=CMLib.map().roomLocation(affected);
				if(R!=null)
				{
					if(babe.Name().indexOf(' ')>0)
					{
						final String name=CMLib.english().startWithAorAn(getMyRace().makeMobName((char)babe.baseCharStats().getStat(CharStats.STAT_GENDER), 3)).toLowerCase();
						babe.setName(name);
						babe.setDisplayText(L("@x1 is here.",name));
					}
					babe.baseCharStats().setStat(CharStats.STAT_CHARISMA,10);
					babe.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,10);
					babe.baseCharStats().setStat(CharStats.STAT_DEXTERITY,5);
					babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,6);
					babe.baseCharStats().setStat(CharStats.STAT_STRENGTH,6);
					babe.baseCharStats().setStat(CharStats.STAT_WISDOM,6);
					babe.basePhyStats().setHeight(babe.basePhyStats().height()*5);
					babe.basePhyStats().setWeight(babe.basePhyStats().weight()*5);
					babe.baseState().setHitPoints(4);
					babe.baseState().setMana(25);
					babe.baseState().setMovement(50);
					final Behavior B=CMClass.getBehavior("MudChat");
					if(B!=null)
						babe.addBehavior(B);
					else
						babe.delEffect(this);
					babe.recoverCharStats();
					babe.recoverPhyStats();
					babe.recoverMaxState();
					babe.text();
				}
			}
			else
			if(ellapsed>=myRace.getAgingChart()[3])
			{
				final Ability A=babe.fetchEffect("Prop_SafePet");
				if(A!=null)
					babe.delEffect(A);

				if(babe.Name().indexOf(' ')>0)
				{
					final String name=CMLib.english().startWithAorAn(getMyRace().makeMobName((char)babe.baseCharStats().getStat(CharStats.STAT_GENDER), 4)).toLowerCase();
					babe.setName(name);
					babe.setDisplayText(L("@x1 stands here.",name));
				}

				babe.baseCharStats().setStat(CharStats.STAT_CHARISMA,10);
				babe.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,10);
				babe.baseCharStats().setStat(CharStats.STAT_DEXTERITY,10);
				babe.baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,10);
				babe.baseCharStats().setStat(CharStats.STAT_STRENGTH,10);
				babe.baseCharStats().setStat(CharStats.STAT_WISDOM,10);
				getMyRace().setHeightWeight(babe.basePhyStats(), (char)babe.baseCharStats().getStat(CharStats.STAT_GENDER));
				babe.baseState().setHitPoints(15);
				babe.baseState().setMana(25);
				babe.baseState().setMovement(80);
				babe.recoverCharStats();
				babe.recoverPhyStats();
				babe.recoverMaxState();
				babe.text();
			}
			else
			if(ellapsed>=myRace.getAgingChart()[5])
			{
				if(babe.Name().indexOf(' ')>0)
				{
					final String name=CMLib.english().startWithAorAn(getMyRace().makeMobName((char)babe.baseCharStats().getStat(CharStats.STAT_GENDER), 5)).toLowerCase();
					babe.setName(name);
					babe.setDisplayText(L("@x1 stands here.",name));
				}
				babe.baseState().setHitPoints(20);
				babe.baseState().setMana(25);
				babe.baseState().setMovement(100);
				final Ability A=babe.fetchEffect(ID());
				babe.delEffect(A);
				babe.recoverCharStats();
				babe.recoverPhyStats();
				babe.recoverMaxState();
				babe.text();
			}
		}
		norecurse=false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		final Physical affected = this.affected;
		if((affected!=null)
		&&(!affected.amDestroyed()))
		{
			if(getMyRace()==null)
				return;
			if((msg.target()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_EXAMINE))
			{
				if((((affected instanceof Item)&&(affected instanceof CagedAnimal)&&(!(affected instanceof DeadBody)))
					||((affected instanceof MOB)&&(!((MOB)affected).isSavable())))
				&&(affected.description().toUpperCase().indexOf(msg.source().name().toUpperCase())>=0))
				{
					if(divisor==0.0)
					{
						final TimeClock C=CMLib.time().localClock(affected);
						divisor = C.getMonthsInYear() * C.getDaysInMonth() * CMProps.getIntVar( CMProps.Int.TICKSPERMUDDAY );
					}
					final long l=CMath.s_long(text());
					if((l>0)&&(l<Long.MAX_VALUE))
					{
						final int ellapsed=(int)Math.round(Math.floor(CMath.div(CMath.div(System.currentTimeMillis()-l,CMProps.getTickMillis()),divisor)));
						if(ellapsed<=myRace.getAgingChart()[3])
						{
							String s=displayText();
							if(s.startsWith("("))s=s.substring(1);
							if(s.endsWith(")")
								)s=s.substring(0,s.length()-1);
							msg.source().tell(L("@x1 is @x2",Name(),s));
						}
					}
				}
			}
			if((affected instanceof Item)
			&&(!(affected instanceof DeadBody))
			&&((msg.target()==affected)||(msg.tool()==affected))
			&&(CMLib.flags().isInTheGame((Item)affected,true)))
			{
				final Item baby=(Item)affected;
				Behavior B=baby.fetchBehavior("Emoter");
				if(B!=null)
				{
					/*
					B=CMClass.getBehavior("Emoter");
					if(B!=null)
						baby.addBehavior(B);
					*/
				//}
				// no else please
				//if(B!=null)
				//{
					if(baby.owner() instanceof Room)
					{
						if(!B.getParms().equalsIgnoreCase(downBabyEmoter))
							B.setParms(downBabyEmoter);
					}
					else
					if(baby.owner()!=null)
					{
						final Environmental o=baby.owner();
						if(baby.description().toUpperCase().indexOf(o.name().toUpperCase())<0)
						{
							if(!B.getParms().equalsIgnoreCase(otherBabyEmoter))
								B.setParms(otherBabyEmoter);
						}
						else
						{
							if(!B.getParms().equalsIgnoreCase(happyBabyEmoter))
								B.setParms(happyBabyEmoter);
						}
					}
				}
			}
			if(((System.currentTimeMillis()-lastSoiling)>(TimeManager.MILI_MINUTE*30))&&(CMLib.dice().rollPercentage()<10))
			{
				if(lastSoiling==0)
					lastSoiling=System.currentTimeMillis();
				else
				{
					lastSoiling=System.currentTimeMillis();
					boolean soil=((affected instanceof CagedAnimal)&&(!(affected instanceof DeadBody)));
					MOB mob=null;
					if(affected instanceof MOB)
					{
						mob=(MOB)affected;
						if(getMyRace()==null)
							return;
						if(divisor==0.0)
						{
							final TimeClock C=CMLib.time().localClock(mob.getStartRoom());
							divisor = C.getMonthsInYear() * C.getDaysInMonth() * CMProps.getIntVar( CMProps.Int.TICKSPERMUDDAY );
						}
						final long l=CMath.s_long(text());
						if((l>0)&&(l<Long.MAX_VALUE))
						{
							final int ellapsed=(int)Math.round(Math.floor(CMath.div(CMath.div(System.currentTimeMillis()-l,CMProps.getTickMillis()),divisor)));
							if(ellapsed<=myRace.getAgingChart()[2])
								soil=true;
						}
					}
					if(invoker()!=null)
						mob=invoker();
					else
					if((affected instanceof Item)&&(((Item)affected).owner() instanceof MOB))
						mob=(MOB)((Item)affected).owner();
					if((mob==null)&&(((Item)affected).owner() instanceof Room))
						mob=((Room)((Item)affected).owner()).fetchInhabitant(0);

					if((soil)&&(affected.fetchEffect("Soiled")==null)&&(mob!=null)&&(!affected.name().toLowerCase().endsWith(" egg")))
					{
						final Ability A=CMClass.getAbility("Soiled");
						if(A!=null)
							A.invoke(mob,affected,true,0);
					}
				}
			}
			doThang();
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		doThang();
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		final long l=CMath.s_long(text());
		if((l<Short.MAX_VALUE)&&(l>0))
		{
			affected.baseCharStats().setStat(CharStats.STAT_AGE,(int)l);
			affectableStats.setStat(CharStats.STAT_AGE,(int)l);
		}
		else
		{
			if(divisor==0.0)
			{
				final TimeClock C=CMLib.time().localClock(affected);
				divisor = C.getMonthsInYear() * C.getDaysInMonth() * CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY );
			}
			final int age=(int)Math.round(Math.floor(CMath.div(CMath.div(System.currentTimeMillis()-l,CMProps.getTickMillis()),divisor)));
			
			if((age>=Short.MAX_VALUE)||(age<0))
				Log.errOut("Age","Recorded, on "+affected.name()+", age of "+age+", from tick values (("+System.currentTimeMillis()+"-"+l+")/4000)/"+divisor);
			else
			{
				affected.baseCharStats().setStat(CharStats.STAT_AGE,age);
				affectableStats.setStat(CharStats.STAT_AGE,affected.baseCharStats().getStat(CharStats.STAT_AGE));
			}
		}
	}
}
