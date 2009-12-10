package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Con extends ThiefSkill
{
	public String ID() { return "Thief_Con"; }
	public String name(){ return "Con";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}
	private static final String[] triggerStrings = {"CON"};
	public String[] triggerStrings(){return triggerStrings;}
	protected boolean disregardsArmorCheck(MOB mob){return true;}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_DECEPTIVE; }
	protected MOB lastChecked=null;
    public double castingTime(MOB mob, Vector cmds){return 5;}
    public boolean preInvoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel, int secondsElapsed, double actionsRemaining)
    {
        if(commands!=null) commands=(Vector)commands.clone();
        if(!conCheck(mob,commands,givenTarget,auto,asLevel))
            return false;
        Vector V=new Vector();
        V.addElement(commands.elementAt(0));
        MOB target=this.getTarget(mob,V,givenTarget);
        if(target==null) return false;
        commands.removeElementAt(0);
        if(secondsElapsed>0)
        {
            if((secondsElapsed%4)==0)
                return mob.location().show(mob,target,CMMsg.MSG_SPEAK,"^T<S-NAME> continue(s) conning <T-NAMESELF> to '"+CMParms.combine(commands,0)+"'.^?");
            return true;
        }
        CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> attempt(s) to con <T-NAMESELF> to '"+CMParms.combine(commands,0)+"'.^?");
        if(mob.location().okMessage(mob,msg))
            mob.location().send(mob,msg);
        else
            return false;
        return true;
    }

    public boolean conCheck(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if(commands!=null) commands=(Vector)commands.clone();
        if(commands.size()<1)
        {
            mob.tell("Con whom into doing what?");
            return false;
        }
        Vector V=new Vector();
        V.addElement(commands.elementAt(0));
        MOB target=this.getTarget(mob,V,givenTarget);
        if(target==null) return false;
        
        commands.removeElementAt(0);

        if((!target.mayIFight(mob))||(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<3))
        {
            mob.tell("You can't con "+target.name()+".");
            return false;
        }

        if(target.isInCombat())
        {
            mob.tell(target.name()+" is too busy fighting right now.");
            return false;
        }

        if(mob.isInCombat())
        {
            mob.tell("You are too busy fighting right now.");
            return false;
        }

        if(commands.size()<1)
        {
            mob.tell("Con "+target.charStats().himher()+" into doing what?");
            return false;
        }


        if(((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
        {
            mob.tell("You can't con someone into following you.");
            return false;
        }
        
        Object O=CMLib.english().findCommand(target,commands);
        if(O instanceof Command)
        {
            if((!((Command)O).canBeOrdered())||(!((Command)O).securityCheck(mob))||(((Command)O).ID().equals("Sleep")))
            {
                mob.tell("You can't con someone into doing that.");
                return false;
            }
        }
        return true;
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        if(commands!=null) commands=(Vector)commands.clone();
        if(!conCheck(mob,commands,givenTarget,auto,asLevel))
            return false;
        Vector V=new Vector();
        V.addElement(commands.elementAt(0));
        MOB target=this.getTarget(mob,V,givenTarget);
        if(target==null) return false;
		commands.removeElementAt(0);

		int oldProficiency=proficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=((mob.envStats().level()+(2*super.getXLEVELLevel(mob)))-target.envStats().level())*10;
		if(levelDiff>0) levelDiff=0;
		boolean success=proficiencyCheck(mob,(mob.charStats().getStat(CharStats.STAT_CHARISMA)*2)+levelDiff,auto);

		if(!success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> tr(ys) to con <T-NAMESELF> to '"+CMParms.combine(commands,0)+"', but <S-IS-ARE> unsuccessful.^?");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,"^T<S-NAME> con(s) <T-NAMESELF> to '"+CMParms.combine(commands,0)+"'.^?");
			mob.recoverEnvStats();
			if((mob.location().okMessage(mob,msg))
            &&(mob.location().show(mob,target,CMMsg.MSG_ORDER,null)))
			{
				mob.location().send(mob,msg);
				target.enqueCommand(commands,Command.METAFLAG_FORCED|Command.METAFLAG_ORDER,0);
			}
			target.recoverEnvStats();
		}
		if(target==lastChecked)
			setProficiency(oldProficiency);
		lastChecked=target;
		return success;
	}

}
