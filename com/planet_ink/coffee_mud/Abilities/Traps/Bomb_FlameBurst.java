package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Bomb_FlameBurst extends StdBomb
{
	public String ID() { return "Bomb_FlameBurst"; }
	public String name(){ return "flame burst bomb";}
	protected int trapLevel(){return 17;}
	public String requiresToSet(){return "some lamp oil";}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Item))
		||(!(E instanceof Drink))
		||(!((((Drink)E).containsDrink())||(((Drink)E).liquidType()!=EnvResource.RESOURCE_LAMPOIL)))
		   &&(((Item)E).material()!=EnvResource.RESOURCE_LAMPOIL))
		{
			if(mob!=null)
				mob.tell("You need some lamp oil to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))||(target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) the flame burst!");
			else
			if(target.location().show(invoker(),target,this,CMMsg.MASK_GENERAL|CMMsg.MSG_NOISE,(affected.name()+" flames all over <T-NAME>!")+CommonStrings.msp("fireball.wav",30)))
			{
				super.spring(target);
				MUDFight.postDamage(invoker(),target,null,Dice.roll(trapLevel(),12,1),CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The flames <DAMAGE> <T-NAME>!");
			}
		}
	}

}
