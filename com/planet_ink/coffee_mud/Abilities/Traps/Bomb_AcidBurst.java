package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bomb_AcidBurst extends StdBomb
{
	public String ID() { return "Bomb_AcidBurst"; }
	public String name(){ return "acid burst bomb";}
	protected int trapLevel(){return 20;}
	public String requiresToSet(){return "some lemons";}
	public Environmental newInstance(){	return new Bomb_AcidBurst();}
	
	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if((!(E instanceof Item))
		||(((Item)E).material()!=EnvResource.RESOURCE_LEMONS))
		{
			mob.tell("You need some lemons to make this out of.");
			return false;
		}
		return true;
	}
	public void spring(MOB target)
	{
		if(target.location()!=null)
		{
			if((target==invoker())||(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS)))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) the acid burst!");
			else
			if(invoker().mayIFight(target))
				if(target.location().show(invoker(),target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,affected.displayName()+" sprays acid all over <T-NAME>!"))
				{
					super.spring(target);
					ExternalPlay.postDamage(invoker(),target,null,Dice.roll(trapLevel(),24,1),Affect.MASK_GENERAL|Affect.TYP_ACID,Weapon.TYPE_MELTING,"The acid <DAMAGE> <T-NAME>!");
				}
		}
	}
	
}
