package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Trapper extends Thief
{
	public String ID(){return "Trapper";}
	public String name(){return "Trapper";}
	private static boolean abilitiesLoaded2=false;
	public boolean loaded(){return abilitiesLoaded2;}
	public void setLoaded(boolean truefalse){abilitiesLoaded2=truefalse;};

	public Trapper()
	{
		super();
		maxStatAdj[CharStats.DEXTERITY]=4;
		maxStatAdj[CharStats.CONSTITUTION]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",false);
			CMAble.addCharAbilityMapping(ID(),1,"Apothecary",false);
			CMAble.addCharAbilityMapping(ID(),1,"ThievesCant",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",50,false);
			CMAble.addCharAbilityMapping(ID(),1,"Taxidermy",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Carpentry",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Wainwrighting",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Thief_Caltrops",false);

			CMAble.addCharAbilityMapping(ID(),2,"Thief_Hide",false);
			CMAble.addCharAbilityMapping(ID(),2,"Thief_TrophyCount",false);

			CMAble.addCharAbilityMapping(ID(),3,"Thief_AvoidTraps",true);
			CMAble.addCharAbilityMapping(ID(),3,"Thief_IdentifyBombs",false);

			CMAble.addCharAbilityMapping(ID(),4,"Thief_DetectTraps",false);

			CMAble.addCharAbilityMapping(ID(),5,"Thief_StrategicRetreat",true);
			CMAble.addCharAbilityMapping(ID(),5,"Ranger_FindWater",false);

			CMAble.addCharAbilityMapping(ID(),6,"Thief_Sneak",false);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Dodge",false);

			CMAble.addCharAbilityMapping(ID(),7,"Thief_UsePoison",true);

			CMAble.addCharAbilityMapping(ID(),8,"Thief_RemoveTraps",false);

			CMAble.addCharAbilityMapping(ID(),9,"Thief_SneakAttack",false);
			CMAble.addCharAbilityMapping(ID(),9,"Thief_Listen",true);

			CMAble.addCharAbilityMapping(ID(),10,"Thief_Trap",true);

			CMAble.addCharAbilityMapping(ID(),11,"Fighter_TrueShot",false);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Parry",true);

			CMAble.addCharAbilityMapping(ID(),12,"Ranger_Track",false);

			CMAble.addCharAbilityMapping(ID(),13,"Thief_Sap",false);
			CMAble.addCharAbilityMapping(ID(),13,"Thief_Observation",true);

			CMAble.addCharAbilityMapping(ID(),14,"Thief_Lure",false);
			CMAble.addCharAbilityMapping(ID(),14,"Thief_Plant",false);

			CMAble.addCharAbilityMapping(ID(),15,"Thief_BackStab",true);
			CMAble.addCharAbilityMapping(ID(),15,"Spell_ReadMagic",false);

			CMAble.addCharAbilityMapping(ID(),16,"Fighter_CoverDefence",false);
			CMAble.addCharAbilityMapping(ID(),16,"Thief_Bind",false);

			CMAble.addCharAbilityMapping(ID(),17,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),17,"Thief_MakeBomb",false);

			CMAble.addCharAbilityMapping(ID(),18,"Thief_Detection",false);
			CMAble.addCharAbilityMapping(ID(),18,"AnimalTaming",false);

			CMAble.addCharAbilityMapping(ID(),19,"Thief_RunningFight",false);
			CMAble.addCharAbilityMapping(ID(),20,"CageBuilding",25,true);

			CMAble.addCharAbilityMapping(ID(),20,"Thief_SetAlarm",false);
			CMAble.addCharAbilityMapping(ID(),20,"Fighter_Pin",false);
			CMAble.addCharAbilityMapping(ID(),20,"Scrapping",false);

			CMAble.addCharAbilityMapping(ID(),21,"Skill_Cage",true);
			CMAble.addCharAbilityMapping(ID(),21,"Domesticating",false);

			CMAble.addCharAbilityMapping(ID(),22,"Thief_Snipe",false);

			CMAble.addCharAbilityMapping(ID(),23,"AnimalTrading",false);
			CMAble.addCharAbilityMapping(ID(),22,"Thief_Shadow",true);

			CMAble.addCharAbilityMapping(ID(),24,"AnimalTraining",false);

			CMAble.addCharAbilityMapping(ID(),25,"Thief_TrapImmunity",true);
			CMAble.addCharAbilityMapping(ID(),25,"Thief_Kamikaze",true);

			CMAble.addCharAbilityMapping(ID(),30,"Thief_DeathTrap",true);
		}
	}
	public String otherLimitations(){return "Sneak and Hide attempts will fail outside of the wild.";}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.tool() instanceof Ability)
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
		&&(myChar.location()!=null)
		&&(myChar.isMine(msg.tool())))
		{
			if((((myChar.location().domainType()&Room.INDOORS)>0))
			||(myChar.location().domainType()==Room.DOMAIN_OUTDOORS_CITY))
			{
				if(CMAble.lowestQualifyingClassRace(myChar,(Ability)msg.tool())==this)
				{
					if(msg.tool().ID().equalsIgnoreCase("Thief_Hide"))
					{
						myChar.tell("You don't know how to hide outside the wilderness.");
						return false;
					}
					else
					if(msg.tool().ID().equalsIgnoreCase("Thief_Sneak"))
					{
						myChar.tell("You don't know how to sneak outside the wilderness.");
						return false;
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	public String statQualifications(){return "Dexterity 9+ Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Dexterity to become a Trapper.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Trapper.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}
}
