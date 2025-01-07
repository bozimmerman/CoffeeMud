package com.planet_ink.coffee_mud.CharClasses;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.SecretFlag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class Gaoler extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Gaoler";
	}

	private final static String localizedStaticName = CMLib.lang().L("Gaoler");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return "Commoner";
	}

	@Override
	public int getBonusPracLevel()
	{
		return 2;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return -1;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_STRENGTH;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 5;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/6)+(1*(1?5))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/10)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_LEATHER;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_FLAILONLY;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(final MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	public Gaoler()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=6;
		maxStatAdj[CharStats.STAT_DEXTERITY]=6;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"ClanCrafting",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"SmokeRings",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Cooking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Baking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"FoodPrep",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Butchering",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"GaolFood",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"BodyPiercing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Groin",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Searching",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"DrugCutting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Blacksmithing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Nippletwist",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Skill_Whipsmack",0,false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Carpentry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"BlackMarketeering",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Tattooing",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_PrisonAssignment",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"LockSmith",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_Gutbuster",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Warrants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Thief_Roofie",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_Hide",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_Informant",false);
//  	  CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_MakeSomeoneSleeplessAndFatigued",false);
//  	  CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_Waterboard",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Brainwash",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Fighter_LegHold",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_ArrestingSap",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_ChildLabor",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_HandCuff",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_PrisonerTransfer",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_TarAndFeather",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_ArmHold",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_Flay",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_Whiplash",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Headlock",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Torturesmithing",false,CMParms.parseSemicolons("Carpentry,Blacksmithing(75)",true),"+INT 14");
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Skill_SpreadHate",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Leeching",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_CollectBounty",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_TrackCriminal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_Arrest",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_SpreadApathy",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_Behead",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Stoning",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"SlaveTrading",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Skill_Lobotomizing",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Skill_Enslave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Pimping",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Skill_JailKey",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"SlaveMarketeering",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_Chirgury",false,CMParms.parseSemicolons("Butchering",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Amputation",true);

		// to separate from artisam
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Chopping",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Digging",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Drilling",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Fishing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Foraging",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Herbology",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Cobbling",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("LeatherWorking",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Hunting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Mining",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Pottery",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"ScrimShaw",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"LeatherWorking",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"GlassBlowing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Sculpting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Tailoring",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Weaving",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"CageBuilding",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"JewelMaking",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Dyeing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shearing",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Embroidering",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("Skill_Write",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Engraving",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("Skill_Write",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Lacquerring",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Smelting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Armorsmithing",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("Blacksmithing",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Fletching",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Weaponsmithing",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("Blacksmithing;Specialization_*",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Boatwright",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shipwright",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Wainwrighting",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"PaperMaking",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Distilling",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Farming",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shrooming",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Speculate",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Painting",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Construction",0,"",false,SecretFlag.SECRET,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Masonry",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Excavation",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Irrigation",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Landscaping",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Taxidermy",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Merchant",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Scrapping",0,"",false,SecretFlag.SECRET);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Costuming",0,"",false,SecretFlag.SECRET);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			if(mob.charStats().getCurrentClass() == this)
			{
				int exp=0;
				for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&(!A.isAutoInvoked())
					&&(mob.isMine(A))
					&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
						exp++;
				}
				if(exp>0)
					CMLib.leveler().postExperience(mob,"CLASS:"+ID(),null,null,exp, true);

				if((CMLib.flags().isSleeping(mob))
				&&(mob.playerStats()!=null)
				&&(CMLib.dice().roll(1, 10, 0)==1))
				{
					final Room R=mob.location();
					final LegalBehavior legalBehavior=CMLib.law().getLegalBehavior(R);
					final Area legalArea=CMLib.law().getLegalObject(R);
					if(this.isRightOutsideAnOccupiedCell(R, legalBehavior, legalArea))
						CMLib.leveler().postExperience(mob,"CLASS:"+ID(),null,null,5, false);
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean isRightOutsideACell(final Room R, final LegalBehavior legalBehavior, final Area legalArea)
	{
		if((R==null)||(legalBehavior==null))
			return false;
		final List<Room> rooms=new ArrayList<Room>(3);
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			final Room R2=R.getRoomInDir(d);
			final Exit E2=R.getExitInDir(d);
			if((R2!=null)
			&&(E2!=null))
				rooms.add(R2);
		}
		return legalBehavior.isJailRoom(legalArea, rooms);
	}

	public boolean isRightOutsideAnOccupiedCell(final Room R, final LegalBehavior legalBehavior, final Area legalArea)
	{
		if((R==null)||(legalBehavior==null))
			return false;
		final List<Room> rooms=new ArrayList<Room>(3);
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			final Room R2=R.getRoomInDir(d);
			final Exit E2=R.getExitInDir(d);
			if((R2!=null)
			&&(E2!=null)
			&&(R2.numInhabitants()>0))
				rooms.add(R2);
		}
		if(rooms.size()==0)
			return false;
		return legalBehavior.isJailRoom(legalArea, rooms);
	}

	protected boolean isAnOfficerOfTheCourt(final MOB mob, final LegalBehavior legalBehavior, final Area legalArea)
	{
		if(mob.isMonster() && (legalBehavior!=null))
		{
			if(legalBehavior.isAnyOfficer(legalArea, mob)
			||legalBehavior.isJudge(legalArea, mob))
				return true;
			final Room R=mob.location();
			if(R!=null)
			{
				if((mob.getStartRoom()==R)
				&&(isRightOutsideACell(R,legalBehavior,legalArea)))
					return true;
			}
		}
		return false;
	}

	public boolean isInACell(final MOB M)
	{
		if(M==null)
			return false;
		final Room R=M.location();
		if(R==null)
			return false;
		final LegalBehavior legalBehavior=CMLib.law().getLegalBehavior(R);
		final Area legalArea=CMLib.law().getLegalObject(R);
		if((legalBehavior != null)
		&&(legalArea != null)
		&&(legalBehavior.isJailRoom(legalArea, new XVector<Room>(R)))
		&&(legalBehavior.hasWarrant(legalArea, M)))
			return true;
		return false;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&(msg.amITarget(host))
		&&(msg.source().isMonster())
		&&(host instanceof MOB)
		&&(((MOB)host).charStats().getCurrentClass() == this)
		&&(!((MOB)host).isInCombat())
		&&(msg.source().getVictim()!=host))
		{
			final LegalBehavior legalBehavior=CMLib.law().getLegalBehavior(msg.source().location());
			final Area legalArea=CMLib.law().getLegalObject(msg.source().location());
			if((legalBehavior != null)
			&&(isAnOfficerOfTheCourt(msg.source(),legalBehavior, legalArea))
			&&((!legalBehavior.hasWarrant(legalArea, (MOB)host))))
			{
				msg.source().tell(L("You may not assault this fellow officer of the court."));
				final MOB mob=(MOB)host;
				if(mob.getVictim()==msg.source())
				{
					mob.makePeace(true);
					mob.setVictim(null);
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((msg.source()==host)
		&&(msg.target() instanceof MOB)
		&&(msg.target()!=msg.source())
		&&(((MOB)host).charStats().getCurrentClass().ID().equals(ID())))
		{
			if((msg.tool() instanceof Ability)
			&&(CMath.bset(((Ability)msg.tool()).flags(), Ability.FLAG_TORTURING))
			&&((MOB)host).isMine(msg.tool())
			&&(CMLib.map().getStartArea(host)!=null)
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)
			&&(msg.sourceMinor()!=CMMsg.TYP_NOISE)
			&&(((MOB)host).charStats().getClassLevel(this)>0))
			{
				final MOB mob = (MOB)host;
				final MOB victiM = (MOB)msg.target();
				final CMMsg msg2=CMClass.getMsg(victiM,null,msg.tool(),CMMsg.MSG_NOISE,L("<S-NAME> scream(s) in agony, AAAAAAARRRRGGGHHH!!@x1",CMLib.protocol().msp("scream.wav",40)));
				if(victiM.location().okMessage(victiM,msg2))
				{
					if((victiM.getStartRoom()!=null)
					&&(!victiM.isPlayer())
					&&(victiM.getStartRoom().getArea() == victiM.location().getArea())
					&&(mob.charStats().getClassLevel(this)>19))
					{
						final LegalBehavior B = CMLib.law().getLegalBehavior(victiM.location());
						if((B!=null)
						&&(B.rulingOrganization().length()>0))
						{
							final Clan C = CMLib.clans().getClanExact(B.rulingOrganization());
							if(C!=null)
							{
								if(mob.getClanRole(C.clanID())!=null)
								{
									if(B.addGetLoyaltyBonus(0)<50)
									{
										mob.tell(L("The screams make this area more loyal to @x1.",C.clanID()));
										B.addGetLoyaltyBonus(1);
									}
								}
								else
								{
									if(B.addGetLoyaltyBonus(0)>-50)
									{
										mob.tell(L("The screams make this area less loyal to @x1.",C.clanID()));
										B.addGetLoyaltyBonus(-1);
									}
								}
							}
						}
					}
					final int baseAmt = 20 + CMLib.ableMapper().qualifyingLevel(msg.source(), (Ability)msg.tool());
					int vicLevel = victiM.phyStats().level();
					int xLevel = 0;
					final Ability tortureA = mob.fetchAbility("Torturesmithing");
					if(tortureA != null)
						xLevel =  CMLib.expertises().getExpertiseLevelCached(mob, tortureA.ID(), ExpertiseLibrary.XType.LEVEL);
					vicLevel += xLevel;
					int xp=(int)Math.round(baseAmt*CMath.div(vicLevel,mob.charStats().getClassLevel(this)));
					@SuppressWarnings("unchecked")
					Map<String, int[]> mudHourMOBXPMap = (Map<String, int[]>)((mob.playerStats()==null)?null:mob.playerStats().getClassVariableMap(this).get("MUDHOURMOBXPMAP"));
					if(mudHourMOBXPMap == null)
					{
						mudHourMOBXPMap = new Hashtable<String, int[]>();
						if(mob.playerStats() != null)
							mob.playerStats().getClassVariableMap(this).put("MUDHOURMOBXPMAP",mudHourMOBXPMap);
					}
					int[] done=mudHourMOBXPMap.get(mob.Name()+"/"+msg.tool().ID());
					if (done == null)
					{
						done = new int[3];
						mudHourMOBXPMap.put(mob.Name() + "/" + msg.tool().ID(), done);
					}
					if(Calendar.getInstance().get(Calendar.SECOND)!=done[2])
					{
						final TimeClock clock =CMLib.map().getStartArea(mob).getTimeObj();
						if(done[0]!=clock.getHourOfDay())
							done[1]=0;
						done[0]=clock.getHourOfDay();
						done[2]=Calendar.getInstance().get(Calendar.SECOND);

						if(done[1]<((90+xLevel)+((50+xLevel)*mob.phyStats().level())))
						{
							xp=CMLib.leveler().postExperience(mob,"CLASS:"+ID(),null,null,xp, true);
							msg2.addTrailerMsg(CMClass.getMsg(mob,null,null,CMMsg.MSG_OK_VISUAL,L("The sweet screams of your victim earns you @x1 experience points.",""+xp),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
							done[1]+=xp;
						}
						else
							msg2.addTrailerMsg(CMClass.getMsg(mob,null,null,CMMsg.MSG_OK_VISUAL,L("The screams of this victim bore you now."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
						msg.addTrailerMsg(msg2);
					}
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_GIVE)
			&&(msg.tool() instanceof Food)
			&&(msg.target() instanceof MOB)
			&&(isInACell((MOB)msg.target())))
			{
				boolean qualifyingFood=false;
				if(((Food)msg.tool()).fetchEffect("Poison_Rotten")!=null)
					qualifyingFood=true;
				else
				{
					final ItemCraftor foodChkA=(ItemCraftor)msg.source().fetchAbility("GaolFood");
					if(foodChkA != null)
					{
						final List<List<String>> V = foodChkA.fetchMyRecipes(msg.source());
						for(final List<String> item : V)
						{
							if(item.get(0).toLowerCase().indexOf(foodChkA.name().toLowerCase())>0)
								qualifyingFood=true;
						}
					}
				}
				if(qualifyingFood)
				{
					if(msg.source().playerStats()!=null)
					{
						@SuppressWarnings("unchecked")
						Map<String,Long> map=(Map<String,Long>)msg.source().playerStats().getClassVariableMap(this).get("GAOLER_FEEDS");
						if(map == null)
						{
							map=new TreeMap<String,Long>();
							msg.source().playerStats().getClassVariableMap(this).put("GAOLER_FEEDS", map);
						}
						for(final Iterator<String> i=map.keySet().iterator();i.hasNext();)
						{
							final Long L=map.get(i.next());
							if(System.currentTimeMillis() > L.longValue())
								i.remove();
						}
						if(map.containsKey(""+msg.target()))
							qualifyingFood=false;
						else
						{
							final TimeClock C=CMLib.time().localClock(msg.source());
							map.put(""+msg.target(), Long.valueOf(System.currentTimeMillis() + (CMProps.getMillisPerMudHour() * C.getHoursInDay())));
						}
					}
					if(qualifyingFood)
						CMLib.leveler().postExperience((MOB)host,"ABILITY:"+ID(),null,null,msg.source().phyStats().level(), false);
				}
			}
		}
		super.executeMsg(host, msg);
	}

	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]
	{
		new Pair<String,Integer>("Strength",Integer.valueOf(5)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(5))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public List<Item> outfit(final MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Whip");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
			cleanOutfit(outfitChoices);
		}
		return outfitChoices;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Gains experience when using certain skills, "
				+ "screaming victims from certain skills per hour, "
				+ "sleeping by an occupied cell, and feeding inmates bad food.  "
				+ " After 20 levels, torturing locals also affects conquered area loyalty.");
	}
}
