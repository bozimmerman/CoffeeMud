package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Necromancer extends Cleric
{
	public String ID(){return "Necromancer";}
	public String name(){return "Necromancer";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.WISDOM;}
	private static boolean abilitiesLoaded=false;
	public boolean loaded(){return abilitiesLoaded;}
	public void setLoaded(boolean truefalse){abilitiesLoaded=truefalse;};
	public int allowedWeaponLevel(){return CharClass.WEAPONS_EVILCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected boolean disableClericSpellGrant(){return true;}
	protected int alwaysFlunksThisQuality(){return 1000;}

	public Necromancer()
	{
		maxStatAdj[CharStats.WISDOM]=4;
		maxStatAdj[CharStats.CONSTITUTION]=4;
		if(!loaded())
		{
			setLoaded(true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",false);

			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_ControlUndead",25,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_AnimateSkeleton",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_UndeadInvisibility",true);
			CMAble.addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);

			CMAble.addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);
			CMAble.addCharAbilityMapping(ID(),2,"Prayer_PreserveBody",false);

			CMAble.addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
			CMAble.addCharAbilityMapping(ID(),3,"Prayer_FeedTheDead",false);

			CMAble.addCharAbilityMapping(ID(),4,"Prayer_AnimateZombie",false);
			CMAble.addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",true);

			CMAble.addCharAbilityMapping(ID(),5,"Prayer_Deafness",false);

			CMAble.addCharAbilityMapping(ID(),6,"Prayer_AnimateGhoul",false);

			CMAble.addCharAbilityMapping(ID(),7,"Prayer_Curse",true);

			CMAble.addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
			CMAble.addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);

			CMAble.addCharAbilityMapping(ID(),9,"Prayer_AnimateGhast",false);

			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
			CMAble.addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

			CMAble.addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
			CMAble.addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

			CMAble.addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
			CMAble.addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

			CMAble.addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
			CMAble.addCharAbilityMapping(ID(),13,"Prayer_SenseHidden",false);

			CMAble.addCharAbilityMapping(ID(),14,"Prayer_AnimateSpectre",false);
			CMAble.addCharAbilityMapping(ID(),14,"Prayer_HealUndead",false);

			CMAble.addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true);
			CMAble.addCharAbilityMapping(ID(),15,"Prayer_FeignLife",false);

			CMAble.addCharAbilityMapping(ID(),16,"Prayer_Anger",true);

			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
			CMAble.addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

			CMAble.addCharAbilityMapping(ID(),18,"Prayer_AnimateGhost",false);

			CMAble.addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);

			CMAble.addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true);

			CMAble.addCharAbilityMapping(ID(),21,"Prayer_AnimateMummy",false);
			CMAble.addCharAbilityMapping(ID(),21,"Prayer_Vampirism",false);

			CMAble.addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true);
			CMAble.addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

			CMAble.addCharAbilityMapping(ID(),23,"Prayer_AnimateDead",false);

			CMAble.addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true);
			CMAble.addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);

			CMAble.addCharAbilityMapping(ID(),25,"Prayer_AnimateVampire",false);
			CMAble.addCharAbilityMapping(ID(),25,"Prayer_Regeneration",true);
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String statQualifications(){return "Wisdom 9+ Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Necromancer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Necromancer.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "Becomes Lich upon death after reaching 30th level Necromancer.  Undead followers will not drain experience.";}
	public String otherLimitations(){return "Always fumbles good prayers.  Qualifies and receives evil prayers.  Using non-aligned prayers introduces failure chance.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_DEATH)
		&&(myChar.baseCharStats().getClassLevel(this)>=30)
		&&(!myChar.baseCharStats().getMyRace().ID().equals("Lich")))
		{
			Race newRace=CMClass.getRace("Lich");
			if(newRace!=null)
			{
				myChar.tell("You are being transformed into a "+newRace.name()+"!!");
				myChar.baseCharStats().setMyRace(newRace);
				myChar.recoverCharStats();
			}
		}
		return true;
	}

	protected boolean isValidBeneficiary(MOB killer,
									     MOB killed,
									     MOB mob,
									     HashSet followers)
	{
		if((mob!=null)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!mob.charStats().getMyRace().racialCategory().equals("Undead")))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}

	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Shortsword");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}
	
}
