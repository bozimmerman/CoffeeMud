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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
		return CharClass.ARMOR_CLOTH;
	}

	@Override
	public int allowedWeaponLevel()
	{
		return CharClass.WEAPONS_FLAILONLY;
	}

	private final Set<Integer> disallowedWeapons = buildDisallowedWeaponClasses();

	@Override
	protected Set<Integer> disallowedWeaponClasses(MOB mob)
	{
		return disallowedWeapons;
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	public Hashtable<String, int[]> mudHourMOBXPMap = new Hashtable<String, int[]>();

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
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"BodyPiercing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Searching",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Blacksmithing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Carpentry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Tattooing",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"LockSmith",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Skill_Warrants",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Thief_Hide",false);
//  	  CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_MakeSomeoneSleeplessAndFatigued",false);
//  	  CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Skill_Waterboard",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Spell_Brainwash",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Skill_ArrestingSap",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_HandCuff",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Thief_TarAndFeather",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_Flay",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Torturesmithing",false,CMParms.parseSemicolons("Carpentry,Blacksmithing(75)",true),"+INT 14");
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_Leeching",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_CollectBounty",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Skill_Arrest",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_Behead",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Stoning",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"SlaveTrading",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Skill_Enslave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Skill_JailKey",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Skill_Chirgury",false,CMParms.parseSemicolons("Butchering",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Amputation",true);

		// to separate from artisam
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Chopping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Digging",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Drilling",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Fishing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Foraging",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Herbology",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Cobbling",0,"",false,true,CMParms.parseSemicolons("LeatherWorking",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Hunting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Mining",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Pottery",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"ScrimShaw",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"LeatherWorking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"GlassBlowing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Sculpting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Tailoring",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Weaving",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"CageBuilding",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"JewelMaking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Dyeing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shearing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Embroidering",0,"",false,true,CMParms.parseSemicolons("Skill_Write",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Engraving",0,"",false,true,CMParms.parseSemicolons("Skill_Write",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Lacquerring",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Smelting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Armorsmithing",0,"",false,true,CMParms.parseSemicolons("Blacksmithing",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Fletching",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Weaponsmithing",0,"",false,true,CMParms.parseSemicolons("Blacksmithing;Specialization_*",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Shipwright",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Wainwrighting",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"PaperMaking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Distilling",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Farming",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Speculate",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Painting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Construction",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Masonry",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Excavation",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Irrigation",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Landscaping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Taxidermy",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Merchant",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Scrapping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),-1,"Costuming",0,"",false,true);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			final MOB mob=(MOB)ticking;
			if(mob.charStats().getCurrentClass().ID().equals(ID()))
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
					CMLib.leveler().postExperience(mob,null,null,exp,true);
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.source()==host)
		&&(msg.target() instanceof MOB)
		&&(msg.target()!=msg.source())
		&&(((MOB)host).charStats().getCurrentClass().ID().equals(ID()))
		&&(msg.tool() instanceof Ability)
		&&((MOB)host).isMine(msg.tool())
		&&(msg.tool().ID().equals("Thief_Flay")
			||msg.tool().ID().equals("Skill_Chirgury")
			||msg.tool().ID().equals("Tattooing")
			||msg.tool().ID().equals("Thief_TarAndFeather")
			||msg.tool().ID().equals("BodyPiercing")
			||msg.tool().ID().equals("Amputation"))
		&&(CMLib.map().getStartArea(host)!=null)
		&&(((MOB)host).charStats().getClassLevel(this)>0))
		{
			final CMMsg msg2=CMClass.getMsg((MOB)msg.target(),null,null,CMMsg.MSG_NOISE,L("<S-NAME> scream(s) in agony, AAAAAAARRRRGGGHHH!!@x1",CMLib.protocol().msp("scream.wav",40)));
			if(((MOB)msg.target()).location().okMessage(msg.target(),msg2))
			{
				final int xp=(int)Math.round(10.0*CMath.div(((MOB)msg.target()).phyStats().level(),((MOB)host).charStats().getClassLevel(this)));
				int[] done=mudHourMOBXPMap.get(host.Name()+"/"+msg.tool().ID());
				if (done == null)
				{
					done = new int[3];
					mudHourMOBXPMap.put(host.Name() + "/" + msg.tool().ID(), done);
				}
				if(Calendar.getInstance().get(Calendar.SECOND)!=done[2])
				{
					final TimeClock clock =CMLib.map().getStartArea(host).getTimeObj();
					if(done[0]!=clock.getHourOfDay())
						done[1]=0;
					done[0]=clock.getHourOfDay();
					done[2]=Calendar.getInstance().get(Calendar.SECOND);

					if(done[1]<(90+(10*((MOB)host).phyStats().level())))
					{
						done[1]+=xp;
						CMLib.leveler().postExperience((MOB)host,null,null,xp,true);
						msg2.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,L("The sweet screams of your victim earns you @x1 experience points.",""+xp),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
					}
					else
						msg2.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,L("The screams of this victim bore you now."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
					msg.addTrailerMsg(msg2);
				}
			}
		}
	}

	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Strength",Integer.valueOf(5)),
		new Pair<String,Integer>("Dexterity",Integer.valueOf(5))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Whip");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}

	@Override
	public String getOtherBonusDesc()
	{
		return L("Gains experience when using certain skills.  Screams of flayed, amputated, tattooed, body pierced, or chirguried victims grants xp/hr.");
	}
}
