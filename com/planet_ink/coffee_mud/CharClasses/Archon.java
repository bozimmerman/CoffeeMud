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
public class Archon extends StdCharClass
{
	public String ID(){return "Archon";}
	public String name(){return "Archon";}
	public String baseClass(){return ID();}
	public boolean leveless(){return true;}

	public Archon()
	{
		super();
		for(int i : CharStats.CODES.BASE())
			maxStatAdj[i]=7;
    }
    public void initializeClass()
    {
        super.initializeClass();
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalTaming",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalTrading",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"AnimalTraining",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Domesticating",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"InstrumentMaking",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),20,"PlantLore",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),10,"Scrapping",false);
		
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Common",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Resistance",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Multiwatch",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Wrath",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Hush",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Freeze",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Record",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Stinkify",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Banish",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Archon_Metacraft",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Amputation",100,"",true,true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_AlterTime",true);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Chant_MoveSky",true);
		
		// temporarily here until we find a place for them
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Enslave",false);
		CMLib.ableMapper().addCharAbilityMapping(ID(),1,"SlaveTrading",false);
		
		// new thieves skills
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_EscapeBonds",false,null,"+DEX 14");
		CMLib.ableMapper().addCharAbilityMapping(ID(),5,"Thief_SilentDrop",false,null,"+DEX 10");
		CMLib.ableMapper().addCharAbilityMapping(ID(),7,"Thief_Footlocks",false,null,"+DEX 7");
		CMLib.ableMapper().addCharAbilityMapping(ID(),8,"Thief_MarkTrapped",false,CMParms.parseSemicolons("Thief_DetectTraps",true),"+WIS 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),12,"Thief_UndergroundConnections",false,CMParms.parseSemicolons("Thief_Sneak",true),"+CHA 14");
        CMLib.ableMapper().addCharAbilityMapping(ID(),13,"Thief_Autocaltrops",false,CMParms.parseSemicolons("Thief_Caltrops",true),"+CON 12 +DEX 14");
        CMLib.ableMapper().addCharAbilityMapping(ID(),15,"Thief_IdentifyTraps",false,CMParms.parseSemicolons("Thief_DetectTraps",true),"+INT 14");
        CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_AutoDetectTraps",false,CMParms.parseSemicolons("Thief_DetectTraps",true),"+INT 12 +WIS 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_ConcealItem",false,CMParms.parseSemicolons("Thief_Hide",true),"+WIS 14");
		CMLib.ableMapper().addCharAbilityMapping(ID(),16,"Thief_DisablingCaltrops",false,CMParms.parseSemicolons("Thief_Caltrops",true),"+WIS 10");
		CMLib.ableMapper().addCharAbilityMapping(ID(),18,"Thief_Spying",false,null,"+WIS 18");
		CMLib.ableMapper().addCharAbilityMapping(ID(),21,"Thief_Evesdrop",false,CMParms.parseSemicolons("Thief_Listen",true),"+DEX 14 +WIS 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_SetDecoys",false,CMParms.parseSemicolons("Specialization_Ranged(75)",true),"+WIS 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),22,"Thief_Safehouse",false,CMParms.parseSemicolons("Thief_Sneak;Thief_SenseLaw",true),"+WIS 18");
        CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_AutoMarkTraps",false,CMParms.parseSemicolons("Thief_DetectTraps;Thief_MarkTraps",true),"+CON 14 +WIS 16");
		CMLib.ableMapper().addCharAbilityMapping(ID(),23,"Thief_HideOther",false,CMParms.parseSemicolons("Thief_ImprovedHiding",true),"+INT 18");
		CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_Espionage",false,null,"+CHA 18 +WIS 18");
        CMLib.ableMapper().addCharAbilityMapping(ID(),24,"Thief_DazzlingCaltrops",false,CMParms.parseSemicolons("Thief_Caltrops",true),"+INT 12");
		CMLib.ableMapper().addCharAbilityMapping(ID(),30,"Thief_Shadowpass",false,CMParms.parseSemicolons("Thief_Pick;Thief_DetectTraps",true),"+WIS 18 +DEX 22");
		CMLib.ableMapper().addCharAbilityMapping(ID(),35,"Thief_MarkerSpying",false,CMParms.parseSemicolons("Thief_Mark;Thief_Spying",true),"+WIS 20 +INT 18");
		CMLib.ableMapper().addCharAbilityMapping(ID(),40,"Thief_SlipperyMind",false,CMParms.parseSemicolons("Thief_Con",true),"+CHA 18");
        CMLib.ableMapper().addCharAbilityMapping(ID(),40,"Thief_ConcealDoor",false,CMParms.parseSemicolons("Thief_ConcealItem",true),"+WIS 18");
		CMLib.ableMapper().addCharAbilityMapping(ID(),45,"Thief_TapRoom",false,CMParms.parseSemicolons("Thief_ConcealItem;Thief_Evesdrop",true),"+INT 14 +DEX 18");
		CMLib.ableMapper().addCharAbilityMapping(ID(),45,"Thief_DampenAuras",false,CMParms.parseSemicolons("Thief_SlipperyMind",true),"+CHA 18");
        CMLib.ableMapper().addCharAbilityMapping(ID(),50,"Thief_HideInPlainSight",false,CMParms.parseSemicolons("Thief_ImprovedHiding;Thief_PlantItem;Thief_Swipe;Thief_Nondetection",true),"+DEX 24");
		CMLib.ableMapper().addCharAbilityMapping(ID(),50,"Ranger_WoodlandCreep",false,null,"+DEX 18");
        CMLib.ableMapper().addCharAbilityMapping(ID(),55,"Thief_ConcealWalkway",false,CMParms.parseSemicolons("Thief_ConcealDoor",true),"+WIS 20");
	}

	public int availabilityCode(){return 0;}

	public String getStatQualDesc(){return "Must be granted by another Archon.";}
	public boolean qualifiesForThisClass(MOB mob, boolean quiet)
	{
		if(!quiet)
			mob.tell("This class cannot be learned.");
		return false;
	}

    public static final String[] ARCHON_IMMUNITIES={"Spell_Scry","Thief_Listen","Spell_Claireaudience","Spell_Clairevoyance"};
    
    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if((msg.tool() != null)
        &&(msg.target()==myHost)
        &&(msg.tool() instanceof Ability)
        &&((CMParms.indexOf(ARCHON_IMMUNITIES,msg.tool().ID())>=0)
            ||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE)
            ||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON)))
        {
            //((MOB)msg.target()).tell("You are immune to "+msg.tool().name()+".");
            if(msg.source()!=msg.target())
                msg.source().tell(msg.source(),msg.target(),msg.tool(),"<T-NAME> is immune to <O-NAME>.");
            return false;
        }
        return super.okMessage(myHost, msg);
    }
    
	public Vector outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			Weapon w=CMClass.getWeapon("ArchonStaff");
			outfitChoices.addElement(w);
		}
		return outfitChoices;
	}

	public void startCharacter(MOB mob, boolean isBorrowedClass, boolean verifyOnly)

	{
		// archons ALWAYS use borrowed abilities
		super.startCharacter(mob, true, verifyOnly);
		if(verifyOnly)
			grantAbilities(mob,true);
	}

	public void grantAbilities(MOB mob, boolean isBorrowedClass)
	{
        boolean allowed=CMSecurity.isAllowedEverywhere(mob,"ALLSKILLS");
        if((!allowed)&&(mob.playerStats()!=null)&&(!mob.playerStats().getSecurityGroups().contains("ALLSKILLS"))) 
            mob.playerStats().getSecurityGroups().addElement("ALLSKILLS");
        super.grantAbilities(mob,isBorrowedClass);
        if((!allowed)&&(mob.playerStats()!=null)&&(mob.playerStats().getSecurityGroups().contains("ALLSKILLS"))) 
            mob.playerStats().getSecurityGroups().removeElement("ALLSKILLS");
	}
}
