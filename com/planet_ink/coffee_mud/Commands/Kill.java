package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Kill extends StdCommand
{
	public Kill(){}

	private String[] access={"KILL","K","ATTACK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
        if(commands==null)
        {
            if(mob.isInCombat())
            {
                CMLib.combat().postAttack(mob,mob.getVictim(),mob.fetchWieldedItem());
                return true;
            }
            return false;
        }
        
		MOB target=null;
		if(commands.size()<2)
		{
			if(!mob.isInCombat())
			{
				mob.tell("Kill whom?");
				return false;
			}
			else
			if(CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)==CombatLibrary.COMBAT_DEFAULT)
				return false;
			else
				target=mob.getVictim();
		}
		
		boolean reallyKill=false;
		String whomToKill=CMParms.combine(commands,1);
		if(CMSecurity.isAllowed(mob,mob.location(),"KILLDEAD")&&(!mob.isMonster()))
		{
			if(((String)commands.lastElement()).equalsIgnoreCase("DEAD"))
			{
				commands.removeElementAt(commands.size()-1);
				whomToKill=CMParms.combine(commands,1);
				reallyKill=true;
			}
		}

		if(target==null)
		{
			target=mob.location().fetchInhabitant(whomToKill);
			if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
			{
				mob.tell("I don't see '"+whomToKill+"' here.");
				return false;
			}
		}
		
		if(reallyKill)
		{
			CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_OK_ACTION,"^F^<FIGHT^><S-NAME> touch(es) <T-NAMESELF>.^</FIGHT^>^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.curState().setHitPoints(0);
				CMLib.combat().postDeath(mob,target,null);
			}
			return false;
		}
		
		if(mob.isInCombat())
		{
			MOB oldVictim=mob.getVictim();
			if(((oldVictim!=null)&&(oldVictim==target)
			&&(CMProps.getIntVar(CMProps.SYSTEMI_COMBATSYSTEM)==CombatLibrary.COMBAT_DEFAULT)))
			{
				mob.tell("^f^<FIGHT^>You are already fighting "+mob.getVictim().name()+".^</FIGHT^>^?");
				return false;
			}
			
			if((mob.location().okMessage(mob,CMClass.getMsg(mob,target,CMMsg.MSG_WEAPONATTACK,null)))
			&&(oldVictim!=target))
			{
				if((oldVictim!=null)
				&&(target.getVictim()==oldVictim.getVictim())
				&&(target.rangeToTarget()>=0)
				&&(oldVictim.rangeToTarget()>=0))
				{
					int range=target.rangeToTarget()-oldVictim.rangeToTarget();
					if(mob.rangeToTarget()>=0)
						range+=mob.rangeToTarget();
					if(range>=0)
						mob.setAtRange(range);
				}
				mob.tell("^f^<FIGHT^>You are now targeting "+target.name()+".^</FIGHT^>^?");
				mob.setVictim(target);
				return false;
			}
		}
		
		if((!mob.mayPhysicallyAttack(target)))
			mob.tell("You are not allowed to attack "+target.name()+".");
		else
        {
            Item weapon=mob.fetchWieldedItem();
            if(weapon==null)
            {
                Item possibleOtherWeapon=mob.fetchFirstWornItem(Wearable.WORN_HELD);
                if((possibleOtherWeapon!=null)
                &&(possibleOtherWeapon instanceof Weapon)
                &&possibleOtherWeapon.fitsOn(Wearable.WORN_WIELD)
                &&(CMLib.flags().canBeSeenBy(possibleOtherWeapon,mob))
                &&(CMLib.flags().isRemovable(possibleOtherWeapon)))
                {
                    CMLib.commands().postRemove(mob,possibleOtherWeapon,false);
                    if(possibleOtherWeapon.amWearingAt(Wearable.IN_INVENTORY))
                    {
                        Command C=CMClass.getCommand("Wield");
                        if(C!=null) C.execute(mob,(Vector)CMParms.makeVector("WIELD",possibleOtherWeapon),metaFlags);
                    }
                }
            }
			CMLib.combat().postAttack(mob,target,mob.fetchWieldedItem());
        }
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
