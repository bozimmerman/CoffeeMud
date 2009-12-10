package com.planet_ink.coffee_mud.Abilities.Traps;
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
public class Trap_CrushingRoom extends StdTrap
{
	public String ID() { return "Trap_CrushingRoom"; }
	public String name(){ return "crushing room";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 24;}
	public String requiresToSet(){return "100 pounds of stone";}
	public int baseRejuvTime(int level){ return 16;}

	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if(I!=null)
				super.destroyResources(mob.location(),I.material(),100);
		}
		return super.setTrap(mob,E,trapBonus,qualifyingClassLevel,perm);
	}

    public Vector getTrapComponents() {
        Vector V=new Vector();
        for(int i=0;i<100;i++)
            V.addElement(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_STONE));
        return V;
    }
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<100))
			{
				mob.tell("You'll need to set down at least 100 pounds of stone first.");
				return false;
			}
		}
		if(E instanceof Room)
		{
			Room R=(Room)E;
			if((R.domainType()&Room.INDOORS)==0)
			{
				if(mob!=null)
					mob.tell("You can only set this trap indoors.");
				return false;
			}
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((sprung)
		&&(affected!=null)
		&&(!disabled())
		&&(tickDown>=0))
		{
			if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
				||(msg.targetMinor()==CMMsg.TYP_FLEE))
			   &&(msg.amITarget(affected)))
			{
				msg.source().tell("The exits are blocked! You can't get out!");
				return false;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			   &&(msg.amITarget(affected)))
			{
				msg.source().tell("The entry to that room is blocked!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			if((sprung)
			&&(affected!=null)
			&&(affected instanceof Room)
			&&(!disabled())
			&&(tickDown>=0))
			{
				Room R=(Room)affected;
				if(tickDown>13)
					R.showHappens(CMMsg.MSG_OK_VISUAL,"The walls start closing in around you!");
				else
				if(tickDown>4)
				{
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M=R.fetchInhabitant(i);
						if((M!=null)&&(M!=invoker()))
							if(invoker().mayIFight(M))
							{
								int damage=CMLib.dice().roll(trapLevel()+abilityCode(),30,1);
								CMLib.combat().postDamage(invoker(),M,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,"The crushing walls <DAMAGE> <T-NAME>!");
							}
					}
				}
				else
				{
					R.showHappens(CMMsg.MSG_OK_VISUAL,"The walls begin retracting...");
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			||(invoker().getGroupMembers(new HashSet()).contains(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off a trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> trigger(s) a trap!"))
			{
				super.spring(target);
				target.location().showHappens(CMMsg.MSG_OK_VISUAL,"The exits are blocked off! The walls start closing in!");
			}
		}
	}
}
