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
public class Bomb_Spores extends StdBomb
{
	public String ID() { return "Bomb_Spores"; }
	public String name(){ return "spore bomb";}
	protected int trapLevel(){return 15;}
	public String requiresToSet(){return "some diseased meat";}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE))
				offenders.addElement(A);
		}
		return offenders;
	}

    public Vector getTrapComponents() {
        Vector V=new Vector();
        Item I=CMLib.materials().makeItemResource(RawMaterial.RESOURCE_MEAT);
        Ability A=CMClass.getAbility(text());
        if(A==null) A=CMClass.getAbility("Disease_Cold");
        I.addNonUninvokableEffect(A);
        V.addElement(I);
        return V;
    }
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Vector V=returnOffensiveAffects(E);
		if((!(E instanceof Food))||(V.size()==0))
		{
			if(mob!=null)
				mob.tell("You need some diseased meat to make this out of.");
			return false;
		}
		return true;
	}
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		Vector V=returnOffensiveAffects(E);
		if(V.size()>0)
			setMiscText(((Ability)V.firstElement()).ID());
		return super.setTrap(mob,E,trapBonus,qualifyingClassLevel,perm);
	}

	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))
			||(isLocalExempt(target))
			||(invoker().getGroupMembers(new HashSet()).contains(target))
			||(target==invoker())
			||(CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) the poison gas!");
			else
			if(target.location().show(invoker(),target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,affected.name()+" spews poison gas all over <T-NAME>!"))
			{
				super.spring(target);
				Ability A=CMClass.getAbility(text());
				if(A==null) A=CMClass.getAbility("Disease_Cold");
				if(A!=null)
					A.invoke(invoker(),target,true,0);
			}
		}
	}

}
