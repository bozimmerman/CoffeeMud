package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Trap_AcidSpray extends StdTrap
{
	public String ID() { return "Trap_AcidSpray"; }
	public String name(){ return "acid spray";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 15;}
	public String requiresToSet(){return "";}
	public Environmental newInstance(){	return new Trap_AcidSpray();}
	
	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if(Dice.rollPercentage()<=target.charStats().getSave(CharStats.SAVE_TRAPS))
				target.location().show(target,null,null,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> avoid(s) setting off a acid trap!");
			else
			if(target.location().show(target,target,this,Affect.MASK_GENERAL|Affect.MSG_NOISE,"<S-NAME> set(s) off an acid spraying trap!"))
			{
				super.spring(target);
				ExternalPlay.postDamage(invoker(),target,null,Dice.roll(trapLevel(),6,1),Affect.MASK_GENERAL|Affect.TYP_ACID,Weapon.TYPE_MELTING,"The acid <DAMAGE> <T-NAME>!");
				if((canBeUninvoked())&&(affected instanceof Item))
					disable();
			}
		}
	}
}
