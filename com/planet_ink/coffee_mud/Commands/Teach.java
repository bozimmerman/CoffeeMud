package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Teach extends StdCommand
{
	public Teach(){}

	private String[] access={"TEACH"};
	public String[] getAccessWords(){return access;}
    
    
    public boolean tryTeach(MOB teacher, MOB student, String teachWhat)
    throws java.io.IOException
    {
        if((student.session()!=null)
        &&(!student.session().confirm(teacher.Name()+" wants to teach you "+teachWhat+".  Is this Ok (y/N)?","N")))
        {
            teacher.tell(student.charStats().HeShe()+" does not want you to.");
            return false;
        }
        CMMsg msg=CMClass.getMsg(teacher,student,null,CMMsg.MSG_SPEAK,null);
        if(!teacher.location().okMessage(teacher,msg))
            return false;
        msg=CMClass.getMsg(teacher,student,null,CMMsg.MSG_TEACH,"<S-NAME> teach(es) <T-NAMESELF> '"+teachWhat+"'.");
        if(!teacher.location().okMessage(teacher,msg))
            return false;
        teacher.location().send(teacher,msg);
        return true;
    }
    
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Teach who what?");
			return false;
		}
		commands.removeElementAt(0);


		MOB student=mob.location().fetchInhabitant((String)commands.elementAt(0));
		if((student==null)||(!CMLib.flags().canBeSeenBy(student,mob)))
		{
			mob.tell("That person doesn't seem to be here.");
			return false;
		}
		commands.removeElementAt(0);


		String abilityName=CMParms.combine(commands,0);
		Ability realAbility=CMClass.findAbility(abilityName,student.charStats());
		Ability myAbility=null;
		if(realAbility!=null)
			myAbility=mob.fetchAbility(realAbility.ID());
		else
			myAbility=mob.findAbility(abilityName);
		if(myAbility==null)
		{
            ExpertiseLibrary.ExpertiseDefinition theExpertise=null;
            Vector V=CMLib.expertises().myListableExpertises(mob);
            for(int v=0;v<V.size();v++)
            {
                ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)V.elementAt(v);
                if((def.name.equalsIgnoreCase(abilityName))
                &&(theExpertise==null))
                    theExpertise=def;
            }
            if(theExpertise==null)
            for(int v=0;v<V.size();v++)
            {
                ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)V.elementAt(v);
                if((CMLib.english().containsString(def.name,abilityName)
                &&(theExpertise==null)))
                    theExpertise=def;
            }
            if(theExpertise!=null)
            {
                if(CMath.bset(mob.getBitmap(),MOB.ATT_NOTEACH))
                {
                    mob.tell("You are refusing to teach right now.");
                    return false;
                }
                if((CMath.bset(student.getBitmap(),MOB.ATT_NOTEACH))
                &&((!student.isMonster())||(!student.willFollowOrdersOf(mob))))
                {
                    mob.tell(student.name()+" is refusing training at this time.");
                    return false;
                }
                if(!CMLib.expertises().myQualifiedExpertises(student).contains(theExpertise))
                {
                    mob.tell(student.name()+" does not yet fully qualify for the expertise '"+theExpertise.name+"'.\n\rQualifications:"+CMLib.masking().maskDesc(theExpertise.finalRequirements()));
                    return false;
                }
                if(((theExpertise.trainCost>0)&&(student.getTrains()<theExpertise.trainCost))
                ||((theExpertise.practiceCost>0)&&(student.getPractices()<theExpertise.practiceCost))
                ||((theExpertise.expCost>0)&&(student.getExperience()<theExpertise.expCost))
                ||((theExpertise.qpCost>0)&&(student.getQuestPoint()<theExpertise.qpCost)))
                {
                    mob.tell("Training for that expertise requires "+theExpertise.costDescription()+".");
                    return false;
                }
                if(!tryTeach(mob,student,theExpertise.name))
                    return false;
                student.setPractices(student.getPractices()-theExpertise.practiceCost);
                student.setTrains(student.getTrains()-theExpertise.trainCost);
                student.setExperience(student.getExperience()-theExpertise.expCost);
                student.setQuestPoint(student.getQuestPoint()-theExpertise.qpCost);
                student.addExpertise(theExpertise.ID);
                return true;
            }
            mob.tell("You don't seem to know "+abilityName+".");
            return false;
		}
		if(!myAbility.canBeTaughtBy(mob,student))
			return false;
		if(!myAbility.canBeLearnedBy(mob,student))
			return false;
		if(student.fetchAbility(myAbility.ID())!=null)
		{
			mob.tell(student.name()+" already knows how to do that.");
			return false;
		}
        if(!tryTeach(mob,student,myAbility.name()))
            return false;
		myAbility.teach(mob,student);
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
