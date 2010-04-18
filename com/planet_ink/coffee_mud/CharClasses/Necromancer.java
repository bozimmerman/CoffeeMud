package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import java.util.*;



/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Necromancer extends Cleric
{
	public String ID(){return "Necromancer";}
	public String name(){return "Necromancer";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.STAT_WISDOM;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_EVILCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected int alwaysFlunksThisQuality(){return 1000;}
	protected boolean registeredAsListener=false;

	public Necromancer()
	{
        super();
		maxStatAdj[CharStats.STAT_WISDOM]=4;
		maxStatAdj[CharStats.STAT_CONSTITUTION]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_ControlUndead",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_AnimateSkeleton",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_UndeadInvisibility",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Divorce",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseLife",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_PreserveBody",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Desecrate",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_FeedTheDead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_AnimateZombie",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtUndead",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_Deafness",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_DarkSenses",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_AnimateGhoul",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CallUndead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Curse",true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_CauseFatigue",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Paralyze",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_ProtParalyzation",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_AnimateGhast",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseMagic",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_SenseInvisible",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_Poison",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_ProtPoison",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_Plague",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_ProtDisease",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_BloodMoon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_SenseHidden",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_AnimateSpectre",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_HealUndead",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_GreatCurse",true,CMParms.parseSemicolons("Prayer_Curse",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_FeignLife",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Anger",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_AuraFear",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Blindsight",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Skill_AttackHalf",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_AnimateGhost",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_InfuseUnholiness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Hellfire",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassParalyze",true,CMParms.parseSemicolons("Prayer_Paralyze",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_AnimateMummy",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Vampirism",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_CurseItem",true,CMParms.parseSemicolons("Prayer_Curse",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_Disenchant",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_AnimateDead",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_CauseExhaustion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_UnholyWord",true,CMParms.parseSemicolons("Prayer_GreatCurse",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_Nullification",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_AnimateVampire",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_Regeneration",true);
		
		if(!registeredAsListener)
		{
			synchronized(this)
			{
				if(!registeredAsListener)
				{
					CMLib.map().addGlobalHandler(this,CMMsg.TYP_DEATH);
					registeredAsListener=true;
				}
			}
		}
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public String getStatQualDesc(){return "Wisdom 9+ Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STAT_WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Necromancer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STAT_CONSTITUTION)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Constitution to become a Necromancer.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String getOtherBonusDesc(){return "Can sense deaths at Necromancer level 15, and becomes a Lich upon death at 30.  Undead followers will not drain experience.";}
	public String getOtherLimitsDesc(){return "Always fumbles good prayers.  Qualifies and receives evil prayers.  Using non-aligned prayers introduces failure chance.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(myChar.baseCharStats().getClassLevel(this)>=30)
		&&(!myChar.baseCharStats().getMyRace().ID().equals("Lich")))
		{
			Race newRace=CMClass.getRace("Lich");
			if(newRace!=null)
			{
				myChar.tell("The dark powers are transforming you into a "+newRace.name()+"!!");
				myChar.baseCharStats().setMyRace(newRace);
				myChar.recoverCharStats();
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)){
			super.executeMsg(myHost,msg);
		} 
		else
		if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&(msg.othersMinor()==CMMsg.TYP_DEATH))
		{
			MOB aChar=(MOB)myHost;
			super.executeMsg(myHost,msg);
			MOB M=null;
			Room deathRoom=aChar.location();
			if((deathRoom!=null)&&(deathRoom.getArea()!=null))
			for(Enumeration e=CMLib.sessions().sessions();e.hasMoreElements();)
			{
				M=((Session)e.nextElement()).mob();
				if((M!=null)
				&&(M.baseCharStats().getCurrentClass()==this)
				&&(aChar!=M)
				&&(M.baseCharStats().getClassLevel(this)>14)
				&&(CMLib.flags().isInTheGame(M,true))
				&&(!CMath.bset(M.getBitmap(),MOB.ATT_QUIET)))
				{
					if(!aChar.isMonster())
						M.tell("^RYou just felt the death of "+aChar.Name()+".^N");
					else
					if((M.location()!=deathRoom)&&(deathRoom.getArea().Name().equals(M.location().getArea().Name())))
						M.tell("^RYou just felt the death of "+aChar.Name()+" somewhere nearby.^N");
				}
			}
		}
	}
	
    public boolean isValidClassDivider(MOB killer, MOB killed, MOB mob, HashSet followers)
	{
		if((mob!=null)
        &&(mob!=killed)
		&&(!mob.amDead())
		&&((!mob.isMonster())||(!mob.charStats().getMyRace().racialCategory().equals("Undead")))
		&&((mob.getVictim()==killed)
		 ||(followers.contains(mob))
		 ||(mob==killer)))
			return true;
		return false;
	}

	public Vector outfit(MOB myChar)
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
