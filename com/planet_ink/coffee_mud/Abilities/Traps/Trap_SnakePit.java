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
public class Trap_SnakePit extends Trap_RoomPit
{
	public String ID() { return "Trap_SnakePit"; }
	public String name(){ return "snake pit";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 10;}
	public String requiresToSet(){return "some caged snakes";}

	protected Vector monsters=null;

	protected Item getCagedAnimal(MOB mob)
	{
		if(mob==null) return null;
		if(mob.location()==null) return null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if(I instanceof CagedAnimal)
			{
				MOB M=((CagedAnimal)I).unCageMe();
				if((M!=null)&&(M.baseCharStats().getMyRace().racialCategory().equalsIgnoreCase("Serpent")))
					return I;
			}
		}
		return null;
	}

	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		Item I=getCagedAnimal(mob);
		StringBuffer buf=new StringBuffer("<SNAKES>");
		int num=0;
		while((I!=null)&&((++num)<6))
		{
			buf.append(((CagedAnimal)I).cageText());
            I.destroy();
			I=getCagedAnimal(mob);
		}
		buf.append("</SNAKES>");
		setMiscText(buf.toString());
		return super.setTrap(mob,E,trapBonus,qualifyingClassLevel,perm);
	}

    public Vector getTrapComponents() {
        Vector V=new Vector();
        Item I=CMClass.getItem("GenCaged");
        ((CagedAnimal)I).setCageText(text());
        I.recoverEnvStats();
        I.text();
        V.addElement(I);
        return V;
    }
    
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(getCagedAnimal(mob)==null)
		{
			if(mob!=null)
				mob.tell("You'll need to set down some caged snakes first.");
			return false;
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			// recage the motherfather
			if((tickDown<=1)&&(monsters!=null))
			{
				for(int i=0;i<monsters.size();i++)
				{
					MOB M=(MOB)monsters.elementAt(i);
					if(M.amDead()||(!M.isInCombat()))
						M.destroy();
				}
				monsters=null;
			}
		}
		return super.tick(ticking,tickID);
	}

	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.envStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) gently into the pit!");
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> hit(s) the pit floor with a THUMP!");
			int damage=CMLib.dice().roll(trapLevel()+abilityCode(),6,1);
			CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,-1,null);
		}
		Vector snakes=new Vector();
		String t=text();
		int x=t.indexOf("</MOBITEM><MOBITEM>");
		while(x>=0)
		{
			snakes.addElement(t.substring(0,x+10));
			t=t.substring(x+10);
			x=t.indexOf("</MOBITEM><MOBITEM>");
		}
		if(t.length()>0) snakes.addElement(t);
		if(snakes.size()>0)
			monsters=new Vector();
		for(int i=0;i<snakes.size();i++)
		{
			t=(String)snakes.elementAt(i);
			Item I=CMClass.getItem("GenCaged");
			((CagedAnimal)I).setCageText(t);
			MOB monster=((CagedAnimal)I).unCageMe();
			if(monster!=null)
			{
				monsters.addElement(monster);
				monster.baseEnvStats().setRejuv(0);
				monster.bringToLife(target.location(),true);
				monster.setVictim(target);
				if(target.getVictim()==null)
					target.setVictim(monster);
			}
		}
		CMLib.commands().postLook(target,true);
	}
}
