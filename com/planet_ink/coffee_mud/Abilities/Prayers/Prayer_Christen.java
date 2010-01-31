package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Prayer_Christen extends Prayer
{
	public String ID() { return "Prayer_Christen"; }
	public String name(){ return "Christen";}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(mob.isMonster())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
	    if(mob.isInCombat())
	    {
	        mob.tell("Not while you're fighting!");
	        return false;
	    }
		if(commands.size()<2)
		{
			mob.tell("Christen whom what?");
			return false;
		}
		String name=((String)commands.lastElement()).trim();
		commands.removeElementAt(commands.size()-1);
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if((!(target instanceof CagedAnimal))||(target.envStats().ability()<=0)||(!target.isGeneric()))
		{
			mob.tell("You may only christen a child.");
			return false;
		}
		if(name.length()==0)
		{
			mob.tell("Christen "+target.name()+" what?");
			return false;
		}
		if(name.indexOf(" ")>=0)
		{
			mob.tell("The name may not have a space in it.");
			return false;
		}

		name=CMStrings.capitalizeAndLower(name);

		if(CMLib.players().playerExists(name))
		{
			mob.tell("That name is already taken.  Please choose another.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> becomes "+name+".":"^S<S-NAME> christen(s) <T-NAMESELF> '"+name+"'.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String oldName=target.Name();
				target.setName(name);
				target.setDisplayText(name+" is here.");
				String txt=((CagedAnimal)target).cageText();
				txt=CMStrings.replaceFirst(txt,"<NAME>"+oldName+"</NAME>","<NAME>"+name+"</NAME>");
				txt=CMStrings.replaceFirst(txt,"<DISP>"+oldName,"<DISP>"+name);
				((CagedAnimal)target).setCageText(txt);
                Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CHRISTENINGS);
                for(int i=0;i<channels.size();i++)
                    CMLib.commands().postChannel((String)channels.elementAt(i),mob.getClanID(),target.name()+" was just christened.",true);
                CMLib.leveler().postExperience(mob,null,null,5,false);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" over <T-NAME>, but lose(s) <S-HIS-HER> concentration.");


		// return whether it worked
		return success;
	}
}
