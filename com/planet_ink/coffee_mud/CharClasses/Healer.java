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
   Copyright 2000-2008 Bo Zimmerman

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
public class Healer extends Cleric
{
	public String ID(){return "Healer";}
	public String name(){return "Healer";}
	public String baseClass(){return "Cleric";}
	public int getAttackAttribute(){return CharStats.STAT_WISDOM;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_GOODCLERIC;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	protected int alwaysFlunksThisQuality(){return 0;}

	private DVector downs=new DVector(4);
	public Healer()
	{
        super();
		maxStatAdj[CharStats.STAT_WISDOM]=4;
		maxStatAdj[CharStats.STAT_CHARISMA]=4;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",100,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Revoke",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Convert",50,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_TurnUndead",0,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Marry",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_Annul",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_CureLight",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Prayer_RestoreSmell",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_SenseEvil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),2,"Prayer_InfuseHoliness",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_Sacrifice",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),3,"Prayer_RemoveDeathMark",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_ProtEvil",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),4,"Prayer_CureFatigue",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_CureDeafness",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Prayer_Fidelity",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_CureSerious",true,CMParms.parseSemicolons("Prayer_CureLight",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),6,"Prayer_SenseDisease",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Prayer_Bless",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Freedom",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Prayer_Forgive",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_DispelEvil",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),9,"Prayer_GodLight",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_RestoreVoice",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Prayer_CureVampirism",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_RemovePoison",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),11,"Prayer_Benediction",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_CureDisease",true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Prayer_CureExhaustion",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Prayer_ProtectHealth",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_CureCritical",true,CMParms.parseSemicolons("Prayer_CureSerious",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),14,"Prayer_AuraHeal",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_HolyAura",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Prayer_HolyShield",true);

		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_Calm",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Prayer_CureCannibalism",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_CureBlindness",true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),17,"Prayer_Invigorate",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_DispelUndead",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Prayer_BlessedHearth",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_Godstrike",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),19,"Prayer_DeathsDoor",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_MassFreedom",true,CMParms.parseSemicolons("Prayer_Freedom",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"Prayer_PeaceRitual",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Heal",true,CMParms.parseSemicolons("Prayer_CureCritical",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Prayer_Atonement",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_BlessItem",false,CMParms.parseSemicolons("Prayer_Bless",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Prayer_ConsecrateLand",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_MassHeal",true,CMParms.parseSemicolons("Prayer_Heal",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Prayer_MassCureDisease",false,CMParms.parseSemicolons("Prayer_CureDisease",true));

		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_HolyWord",false,CMParms.parseSemicolons("Prayer_HolyAura",true));
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Prayer_DivineResistance",false);

		CMLib.ableMapper().addCharAbilityMapping(ID(),25,"Prayer_Resurrect",true);
	}

	public int availabilityCode(){return Area.THEME_FANTASY;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(ticking instanceof MOB)) return super.tick(ticking,tickID);
		MOB myChar=(MOB)ticking;
		if((tickID==Tickable.TICKID_MOB)
		&&(myChar.charStats().getClassLevel(this)>=30)
		&&(CMLib.flags().isGood(myChar)))
		{
		    int x=downs.indexOf(myChar.Name());
			int fiveDown=5;
			int tenDown=10;
			int twentyDown=20;
			if(x>=0)
			{
			    fiveDown=((Integer)downs.elementAt(x,2)).intValue();
			    tenDown=((Integer)downs.elementAt(x,3)).intValue();
			    twentyDown=((Integer)downs.elementAt(x,4)).intValue();
				if(((--fiveDown)<=0)||((--tenDown)<=0)||((--twentyDown)<=0))
				{
					HashSet followers=myChar.getGroupMembers(new HashSet());
					if(myChar.location()!=null)
						for(int i=0;i<myChar.location().numInhabitants();i++)
						{
							MOB M=myChar.location().fetchInhabitant(i);
							if((M!=null)
							&&((M.getVictim()==null)||(!followers.contains(M.getVictim()))))
								followers.add(M);
						}
					if((fiveDown)<=0)
					{
						fiveDown=5;
						Ability A=CMClass.getAbility("Prayer_CureLight");
						if(A!=null)
						for(Iterator e=followers.iterator();e.hasNext();)
						{
						    MOB M=(MOB)e.next();
						    if(M.curState().getHitPoints()<M.maxState().getHitPoints())
								A.invoke(myChar,M,true,0);
						}
					}
					else
					if((tenDown)<=0)
					{
						tenDown=10;
						Ability A=CMClass.getAbility("Prayer_RemovePoison");
						if(A!=null)
						for(Iterator e=followers.iterator();e.hasNext();)
						{
						    MOB M=(MOB)e.next();
							A.invoke(myChar,M,true,0);
						}
					}
					else
					if((twentyDown)<=0)
					{
						twentyDown=10;
						Ability A=CMClass.getAbility("Prayer_CureDisease");
						if(A!=null)
						for(Iterator e=followers.iterator();e.hasNext();)
							A.invoke(myChar,((MOB)e.next()),true,0);
					}
				}
			    downs.setElementAt(x,2,new Integer(fiveDown));
			    downs.setElementAt(x,3,new Integer(tenDown));
			    downs.setElementAt(x,4,new Integer(twentyDown));
			}
			else
				downs.addElement(myChar.Name(),new Integer(fiveDown),new Integer(tenDown),new Integer(twentyDown));
		}
		return super.tick(myChar,tickID);
	}

	public String statQualifications(){return "Wisdom 9+ Charisma 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob.baseCharStats().getStat(CharStats.STAT_WISDOM)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Wisdom to become a Healer.");
			return false;
		}
		if(mob.baseCharStats().getStat(CharStats.STAT_CHARISMA)<=8)
		{
			if(!quiet)
				mob.tell("You need at least a 9 Charisma to become a Healer.");
			return false;
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public String otherBonuses(){return "All healing prayers give bonus healing.  Attains healing aura after 30th level.";}
	public String otherLimitations(){return "Always fumbles evil prayers.  Qualifies and receives good prayers.  Using non-aligned prayers introduces failure chance.";}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!(myHost instanceof MOB)) return super.okMessage(myHost,msg);
		MOB myChar=(MOB)myHost;
		if(!super.okMessage(myChar, msg))
			return false;

		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.sourceMinor()==CMMsg.TYP_COLD)
			||(msg.sourceMinor()==CMMsg.TYP_WATER)))
		{
			int recovery=myChar.charStats().getClassLevel(this);
			msg.setValue(msg.value()-recovery);
		}
		else
		if((msg.amITarget(myChar))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.sourceMinor()==CMMsg.TYP_FIRE))
		{
			int recovery=msg.value();
			msg.setValue(msg.value()+recovery);
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(myHost instanceof MOB)) return;
		MOB myChar=(MOB)myHost;
		if(msg.amISource(myChar)
		&&(!myChar.isMonster())
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool() instanceof Ability)
		&&(CMLib.ableMapper().getQualifyingLevel(ID(),true,msg.tool().ID())>0)
		&&(myChar.isMine(msg.tool()))
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
		{
			if((msg.target()!=null)
			   &&(msg.target() instanceof MOB))
			{
				MOB tmob=(MOB)msg.target();
				if(msg.tool().ID().equals("Prayer_CureLight"))
					tmob.curState().adjHitPoints(CMLib.dice().roll(2,6,4),tmob.maxState());
				else
				if(msg.tool().ID().equals("Prayer_CureSerious"))
					tmob.curState().adjHitPoints(CMLib.dice().roll(2,16,4),tmob.maxState());
				else
				if(msg.tool().ID().equals("Prayer_CureCritical"))
					tmob.curState().adjHitPoints(CMLib.dice().roll(4,16,4),tmob.maxState());
				else
				if(msg.tool().ID().equals("Prayer_Heal"))
					tmob.curState().adjHitPoints(CMLib.dice().roll(5,20,4),tmob.maxState());
				else
				if(msg.tool().ID().equals("Prayer_MassHeal"))
					tmob.curState().adjHitPoints(CMLib.dice().roll(5,20,4),tmob.maxState());
			}
		}
	}

	public Vector outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("SmallMace");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

}
