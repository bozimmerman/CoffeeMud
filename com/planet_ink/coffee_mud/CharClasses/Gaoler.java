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
public class Gaoler extends StdCharClass
{
	public String ID(){return "Gaoler";}
	public String name(){return "Gaoler";}
	public String baseClass(){return "Commoner";}
	public int getBonusPracLevel(){return 2;}
	public int getBonusAttackLevel(){return -1;}
	public int getAttackAttribute(){return CharStats.STAT_STRENGTH;}
	public int getLevelsPerBonusDamage(){ return 5;}
	public int getHPDivisor(){return 6;}
	public int getHPDice(){return 1;}
	public int getHPDie(){return 5;}
	public int getManaDivisor(){return 10;}
	public int getManaDice(){return 1;}
	public int getManaDie(){return 2;}
	public int allowedArmorLevel(){return CharClass.ARMOR_CLOTH;}
	public int allowedWeaponLevel(){return CharClass.WEAPONS_FLAILONLY;}
	private HashSet disallowedWeapons=buildDisallowedWeaponClasses();
	protected HashSet disallowedWeaponClasses(MOB mob){return disallowedWeapons;}
	public int availabilityCode(){return Area.THEME_FANTASY;}
    public Hashtable mudHourMOBXPMap=new Hashtable();


	public Gaoler()
	{
		super();
		maxStatAdj[CharStats.STAT_STRENGTH]=6;
		maxStatAdj[CharStats.STAT_DEXTERITY]=6;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Write",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Recall",25,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Swim",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Climb",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"FireBuilding",true);
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
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Chopping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Digging",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Drilling",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fishing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Foraging",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Herbology",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Cobbling",0,"",false,true,CMParms.parseSemicolons("LeatherWorking",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Hunting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Mining",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Pottery",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"ScrimShaw",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"LeatherWorking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"GlassBlowing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Sculpting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Tailoring",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Weaving",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"CageBuilding",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"JewelMaking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Dyeing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Embroidering",0,"",false,true,CMParms.parseSemicolons("Skill_Write",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Engraving",0,"",false,true,CMParms.parseSemicolons("Skill_Write",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Lacquerring",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Smelting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Armorsmithing",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Fletching",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Weaponsmithing",0,"",false,true,CMParms.parseSemicolons("Blacksmithing;Specialization_*",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Shipwright",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Wainwrighting",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"PaperMaking",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Distilling",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Farming",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Speculate",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Painting",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Construction",0,"",false,true,CMParms.parseSemicolons("Carpentry",true),"");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Masonry",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Taxidermy",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Merchant",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Scrapping",0,"",false,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Costuming",0,"",false,true);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			if(mob.charStats().getCurrentClass().ID().equals(ID()))
			{
				int exp=0;
				for(int a=0;a<mob.numAllEffects();a++)
				{
					Ability A=mob.fetchEffect(a);
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
            ||msg.tool().ID().equals("Tattooing")
            ||msg.tool().ID().equals("BodyPiercing")
            ||msg.tool().ID().equals("Amputation"))
        &&(CMLib.map().getStartArea(host)!=null)
        &&(((MOB)host).charStats().getClassLevel(this)>0))
        {
            CMMsg msg2=CMClass.getMsg((MOB)msg.target(),null,null,CMMsg.MSG_NOISE,"<S-NAME> scream(s) in agony, AAAAAAARRRRGGGHHH!!"+CMProps.msp("scream.wav",40));
            if(((MOB)msg.target()).location().okMessage((MOB)msg.target(),msg2))
            {
                int xp=(int)Math.round(10.0*CMath.div(msg.target().envStats().level(),((MOB)host).charStats().getClassLevel(this)));
                int[] done=(int[])mudHourMOBXPMap.get(host.Name()+"/"+msg.tool().ID());
                if(done==null){ done=new int[3]; mudHourMOBXPMap.put(host.Name()+"/"+msg.tool().ID(),done);}
                if(Calendar.getInstance().get(Calendar.SECOND)!=done[2])
                {
                    TimeClock clock =CMLib.map().getStartArea(host).getTimeObj(); 
                    if(done[0]!=clock.getTimeOfDay())
                        done[1]=0;
                    done[0]=clock.getTimeOfDay();
                    done[2]=Calendar.getInstance().get(Calendar.SECOND);
                    
                    if(done[1]<(90+(10*((MOB)host).envStats().level())))
                    {
                        done[1]+=xp;
                        CMLib.leveler().postExperience((MOB)host,null,null,xp,true);
                        msg2.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,"The sweet screams of your victim earns you "+xp+" experience points.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
                    }
                    else
                        msg2.addTrailerMsg(CMClass.getMsg((MOB)host,null,null,CMMsg.MSG_OK_VISUAL,"The screams of your victims bore you now.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
                    msg.addTrailerMsg(msg2);
                }
            }
        }
    }

	public String getStatQualDesc(){return "Strength 9+, Dexterity 9+";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(mob != null)
		{
			if(mob.baseCharStats().getStat(CharStats.STAT_STRENGTH)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Strength to become a Gaoler.");
				return false;
			}
			if(mob.baseCharStats().getStat(CharStats.STAT_DEXTERITY)<=8)
			{
				if(!quiet)
					mob.tell("You need at least a 9 Dexterity to become a Gaoler.");
				return false;
			}
		}
		return super.qualifiesForThisClass(mob,quiet);
	}

	public Vector outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("Whip");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

	public String getOtherBonusDesc(){return "Gains experience when using certain skills.  Screams of flayed, amputated, tattooed, body pierced, or chirguried victims grants xp/hr.";}
}
