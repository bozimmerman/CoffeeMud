package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Bomb_Poison extends StdBomb
{
	public String ID() { return "Bomb_Poison"; }
	public String name(){ return "poison gas bomb";}
	protected int trapLevel(){return 5;}
	public String requiresToSet(){return "some poison";}

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.POISON))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		Vector V=returnOffensiveAffects(E);
		if((!(E instanceof Drink))||(V.size()==0))
		{
			if(mob!=null)
				mob.tell("You need some poison to make this out of.");
			return false;
		}
		return true;
	}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{
		if(E==null) return null;
		Vector V=returnOffensiveAffects(E);
		if(V.size()>0)
			setMiscText(((Ability)V.firstElement()).ID());
		return super.setTrap(mob,E,classLevel,qualifyingClassLevel);
	}

	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))||(target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) the poison gas!");
			else
			if(target.location().show(invoker(),target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,affected.name()+" spews poison gas all over <T-NAME>!"))
			{
				super.spring(target);
				Ability A=CMClass.getAbility(text());
				if(A==null) A=CMClass.getAbility("Poison");
				if(A!=null) A.invoke(invoker(),target,true,0);
			}
		}
	}

}
