package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_DemonicConsumption extends Prayer
{
	public String ID() { return "Prayer_DemonicConsumption"; }
	public String name(){return "Demonic Consumption";}
	public int quality(){return MALICIOUS;};
	protected int canTargetCode(){return CAN_ITEMS|CAN_MOBS;}
	public int holyQuality(){ return HOLY_EVIL;}
	public Environmental newInstance(){	return new Prayer_DemonicConsumption();}
	

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=false;
		int affectType=Affect.MSG_CAST_VERBAL_SPELL;
		if(target instanceof Item)
			success=profficiencyCheck(((mob.envStats().level()-target.envStats().level())*25),auto);
		else
		{
			if(!auto)
				affectType=affectType|Affect.MASK_MALICIOUS;
			success=profficiencyCheck(-(target.envStats().level()*3),auto);
		}

		if(auto)affectType=affectType|Affect.MASK_GENERAL;

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"^S<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+" treacherously!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
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
							ExternalPlay.postDamage(mob,(MOB)target,this,(((MOB)target).curState().getHitPoints()*10),Affect.MASK_GENERAL|Affect.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^SThe evil <DAMAGE> <T-NAME>!^?");
						if(((MOB)target).amDead())
							mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> <T-IS-ARE> consumed!");
						else
							return false;
					}
					else
						mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> <T-IS-ARE> consumed!");

					if(target instanceof Item)
						((Item)target).destroyThis();
					else
					{
						int i=0;
						while(i<mob.location().numItems())
						{
							int s=mob.location().numItems();
							Item item=mob.location().fetchItem(i);
							if((item!=null)&&(item instanceof DeadBody)&&(V.get(item)==null))
								item.destroyThis();
							if(s==mob.location().numItems())
								i++;
						}
					}
					mob.location().recoverRoomStats();
				}

			}

		}
		else
			maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF> and "+prayWord(mob)+" treacherously, but fizzles the magic!");


		// return whether it worked
		return success;
	}
}