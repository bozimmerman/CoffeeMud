package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bomb_Smoke extends StdBomb
{
	public String ID() { return "Bomb_Smoke"; }
	public String name(){ return "smoke bomb";}
	protected int trapLevel(){return 2;}
	public String requiresToSet(){return "something wooden";}
	public Environmental newInstance(){	return new Bomb_Smoke();}

	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Item))
		||((((Item)E).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN))
		{
			mob.tell("You something wooden to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((!invoker().mayIFight(target))||(target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) the smoke bomb!");
			else
			if(target.location().show(invoker(),target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,affected.name()+" explodes smoke into <T-YOUPOSS> eyes!"))
			{
				super.spring(target);
				Ability A=CMClass.getAbility("Spell_Blindness");
				if(A!=null) A.invoke(target,target,true);
			}
		}
	}

}
