package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Shatter extends Spell
{
	public Spell_Shatter()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Shatter";


		baseEnvStats().setLevel(8);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Shatter();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ALTERATION;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB mobTarget=getTarget(mob,commands,givenTarget,true);
		Item target=null;
		if(mobTarget!=null)
		{
			Vector goodPossibilities=new Vector();
			Vector possibilities=new Vector();
			for(int i=0;i<mobTarget.inventorySize();i++)
			{
				Item item=mobTarget.fetchInventory(i);
				if((item!=null)
				   &&(item.subjectToWearAndTear()))
				{
					if(item.amWearingAt(Item.INVENTORY))
						possibilities.addElement(item);
					else
						goodPossibilities.addElement(item);
				}
				if(goodPossibilities.size()>0)
					target=(Item)goodPossibilities.elementAt(Dice.roll(1,goodPossibilities.size(),-1));
				else
				if(possibilities.size()>0)
					target=(Item)possibilities.elementAt(Dice.roll(1,possibilities.size(),-1));
			}
		}
		
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands);
		
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> starts vibrating!":"<S-NAME> utter(s) a shattering spell, causing <T-NAMESELF> to vibrate and resonate.");
			FullMsg msg2=new FullMsg(mob,mobTarget,this,affectType,null);
			if((mob.location().okAffect(msg))&&((mobTarget==null)||(mob.location().okAffect(msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(!msg.wasModified())
				{
					int damage=100+mob.envStats().level()-target.envStats().level();
					if(Sense.isABonusItems(target))
						damage=(int)Math.round(Util.div(damage,2.0));
					switch(target.material()&EnvResource.MATERIAL_MASK)
					{
					case EnvResource.MATERIAL_PAPER:
					case EnvResource.MATERIAL_CLOTH:
					case EnvResource.MATERIAL_VEGETATION:
					case EnvResource.MATERIAL_LEATHER:
					case EnvResource.MATERIAL_FLESH:
						damage=(int)Math.round(Util.div(damage,3.0));
						break;
					case EnvResource.MATERIAL_WOODEN:
						damage=(int)Math.round(Util.div(damage,1.5));
						break;
					case EnvResource.MATERIAL_GLASS:
					case EnvResource.MATERIAL_ROCK:
						damage=(int)Math.round(Util.mul(damage,2.0));
						break;
					}
					target.setUsesRemaining(target.usesRemaining()-damage);
					if(target.usesRemaining()>0)
						target.recoverEnvStats();
					else
					{
						target.setUsesRemaining(100);
						if(mobTarget==null)
							mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> is destroyed!");
						else													  
							mob.location().show(mobTarget,target,Affect.MSG_OK_VISUAL,"<T-NAME>, possessed by <S-NAME>, is destroyed!");
						target.remove();
						target.destroyThis();
						mob.location().recoverRoomStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) a shattering spell, but nothing happens.");


		// return whether it worked
		return success;
	}
}

