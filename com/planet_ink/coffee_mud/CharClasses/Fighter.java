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
public class Fighter extends StdCharClass
{
	@Override
	public String ID()
	{
		return "Fighter";
	}

	private final static String localizedStaticName = CMLib.lang().L("Fighter");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public String baseClass()
	{
		return ID();
	}

	@Override
	public int getBonusPracLevel()
	{
		return -1;
	}

	@Override
	public int getBonusAttackLevel()
	{
		return 0;
	}

	@Override
	public int getAttackAttribute()
	{
		return CharStats.STAT_STRENGTH;
	}

	@Override
	public int getLevelsPerBonusDamage()
	{
		return 30;
	}

	@Override
	public int getPracsFirstLevel()
	{
		return 3;
	}

	@Override
	public int getTrainsFirstLevel()
	{
		return 4;
	}

	@Override
	public String getHitPointsFormula()
	{
		return "((@x6<@x7)/2)+(2*(1?7))";
	}

	@Override
	public String getManaFormula()
	{
		return "((@x4<@x5)/8)+(1*(1?2))";
	}

	@Override
	public int allowedArmorLevel()
	{
		return CharClass.ARMOR_ANY;
	}

	@Override
	public String getMovementFormula()
	{
		return "12*((@x2<@x3)/18)";
	}

	protected static final long MILLIS_BETWEEN_DUEL_WINS = 30 * 60000;
	protected static volatile long lastDuelWinner = 0;
	protected static TreeMap<String,long[]> duelWinners = new TreeMap<String,long[]>();

	public Fighter()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=7;
	}

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Axe",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Hammer",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Polearm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Ranged",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Armor",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Shield",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_Kick",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Fighter_ArmorTweaking",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_Parry",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Skill_TwoWeaponFighting",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Skill_Bash",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Fighter_WeaponSharpening",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_Cleave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Fighter_Rescue",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Disarm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Skill_Subdue",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Skill_Dodge",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Fighter_RapidShot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Skill_Attack2",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Fighter_TrueShot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_CritStrike",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Fighter_ShieldBlock",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_BlindFighting",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Fighter_Toughness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_Dirt",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Skill_MountedCombat",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Fighter_WeaponBreak",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Fighter_DualParry",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Skill_Trip",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Fighter_Toughness2",false,CMParms.parseSemicolons("Fighter_Toughness",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Skill_Climb",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Fighter_Sweep",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_Roll",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Fighter_CriticalShot",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_Whomp",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Fighter_DesperateMoves",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Skill_Attack3",true,CMParms.parseSemicolons("Skill_Attack2",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Fighter_Toughness3",false,CMParms.parseSemicolons("Fighter_Toughness2",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_Endurance",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Fighter_PointBlank",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_Tumble",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Fighter_AutoBash",false,CMParms.parseSemicolons("Skill_Bash",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_SizeOpponent",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Fighter_UrbanTactics",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_Berzerk",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Fighter_ImprovedShieldDefence",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_CoverDefence",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Fighter_WeaponCatch",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_CalledStrike",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Fighter_CounterAttack",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_Heroism",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Fighter_Behead",0,"",false,true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fighter_CoupDeGrace",true);
	}

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY;
	}

	private final String[] raceRequiredList = new String[] { "All" };

	@Override
	public String[] getRequiredRaceList()
	{
		return raceRequiredList;
	}

	@SuppressWarnings("unchecked")
	private final Pair<String,Integer>[] minimumStatRequirements=new Pair[]{
		new Pair<String,Integer>("Strength",Integer.valueOf(9))
	};

	@Override
	public Pair<String, Integer>[] getMinimumStatRequirements()
	{
		return minimumStatRequirements;
	}

	@Override
	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
		super.grantAbilities(mob,isBorrowedClass);
		if(mob.playerStats()==null)
		{
			final List<AbilityMapper.AbilityMapping> V=CMLib.ableMapper().getUpToLevelListings(ID(),
															 mob.charStats().getClassLevel(ID()),
															 false,
															 false);
			for(final AbilityMapper.AbilityMapping able : V)
			{
				final Ability A=CMClass.getAbility(able.abilityID());
				if((A!=null)
				&&(!CMLib.ableMapper().getAllQualified(ID(),true,A.ID()))
				&&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
					giveMobAbility(mob,A,CMLib.ableMapper().getDefaultProficiency(ID(),true,A.ID()),CMLib.ableMapper().getDefaultParm(ID(),true,A.ID()),isBorrowedClass);
			}
		}
	}

	@Override 
	public void executeMsg(Environmental host, CMMsg msg)
	{ 
		super.executeMsg(host,msg); 
		Fighter.conquestExperience(this,host,msg);
		Fighter.duelExperience(this, host, msg);
	}
	
	@Override 
	public String getOtherBonusDesc()
	{
		return L("Receives bonus conquest experience and bonus duel experience.");
	}
	
	public static void duelExperience(CharClass C, Environmental host, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_DUELLOSS)
		&&(msg.target()!=msg.tool())
		&&(msg.target()!=host)
		&&(host instanceof MOB)
		&&(msg.tool()==host)
		&&((msg.source().playerStats().getAccount()==null)
			||(((MOB)host).playerStats().getAccount()==null)
			||(((MOB)host).playerStats().getAccount()!=msg.source().playerStats().getAccount()))
		)
		{
			synchronized(duelWinners)
			{
				final long now=System.currentTimeMillis();
				final long timeout=now - (now - MILLIS_BETWEEN_DUEL_WINS);
				if(lastDuelWinner < timeout)
				{
					for(final Iterator<String> m = duelWinners.keySet().iterator(); m.hasNext(); )
					{
						if(duelWinners.get(m.next())[0] < timeout)
							m.remove();
					}
				}
				final long[] lastTime=duelWinners.get(host.Name());
				if((lastTime != null) && (lastTime[0] > timeout))
					return;
				if(lastTime == null)
					duelWinners.put(host.Name(), new long[]{now});
				else
					lastTime[0] = now;
				lastDuelWinner = now;
			}
			((MOB)host).tell(CMLib.lang().L("^YVictory!!^N"));
			CMLib.leveler().postExperience((MOB)host,null,null,300,false);
		}
	}

	public static void conquestExperience(CharClass C, Environmental host, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_AREAAFFECT)
		&&(msg.target() instanceof Area)
		&&(msg.targetMessage()!=null)
		&&(msg.targetMessage().equalsIgnoreCase("CONQUEST"))
		&&(host instanceof MOB)
		&&(((MOB)host).charStats().getCurrentClass().ID().equals(C.ID()))
		&&(((MOB)host).getClanRole(msg.source().Name())!=null)
		&&(!((MOB)host).isMonster())
		)
		{
			final Area A=(Area)msg.target();
			int xp=(int)Math.round(50.0*CMath.div(A.getAreaIStats()[Area.Stats.AVG_LEVEL.ordinal()],((MOB)host).phyStats().level()));
			if(xp>500)
				xp=500;
			if(xp>0)
			{
				((MOB)host).tell(CMLib.lang().L("^YVictory!!^N"));
				CMLib.leveler().postExperience((MOB)host,null,null,xp,false);
			}
		}
	}

	@Override
	public List<Item> outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			final Weapon w=CMClass.getWeapon("Shortsword");
			if(w == null)
				return new Vector<Item>();
			outfitChoices=new Vector<Item>();
			outfitChoices.add(w);
		}
		return outfitChoices;
	}
}
