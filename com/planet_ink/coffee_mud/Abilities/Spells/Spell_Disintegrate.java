package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Disintegrate extends Spell
{
	public String ID() { return "Spell_Disintegrate"; }
	public String name(){return "Disintegrate";}
	public int quality(){return MALICIOUS;};
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Disintegrate();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;	}
	public int overrideMana(){return 100;}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=false;
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(!(target instanceof Item))
		{
			if(!auto)
				affectType=affectType|CMMsg.MASK_MALICIOUS;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		success=profficiencyCheck(-(levelDiff*25),auto);

		if(auto)affectType=affectType|CMMsg.MASK_GENERAL;

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,(auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and utter(s) a treacherous spell!^?")+CommonStrings.msp("spelldam2.wav",40));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					Hashtable V=new Hashtable();
					for(int i=0;i<mob.location().numItems();i++)
					{
						Item item=mob.location().fetchItem(i);
						if((item!=null)&&(item instanceof DeadBody))
							V.put(item,item);
					}

					if(target instanceof MOB)
					{
						if(((MOB)target).curState().getHitPoints()>0)
							MUDFight.postDamage(mob,(MOB)target,this,(((MOB)target).curState().getHitPoints()*10),CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^SThe spell <DAMAGE> <T-NAME>!^?");
						if(((MOB)target).amDead())
							mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> disintegrate(s)!");
						else
							return false;
					}
					else
						mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> disintegrate(s)!");

					if(target instanceof Item)
						((Item)target).destroy();
					else
					{
						int i=0;
						while(i<mob.location().numItems())
						{
							int s=mob.location().numItems();
							Item item=mob.location().fetchItem(i);
							if((item!=null)&&(item instanceof DeadBody)&&(V.get(item)==null))
								item.destroy();
							if(s==mob.location().numItems())
								i++;
						}
					}
					mob.location().recoverRoomStats();
				}

			}

		}
		else
			maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and utter(s) a treacherous but fizzled spell!");


		// return whether it worked
		return success;
	}
}