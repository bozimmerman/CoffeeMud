package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Resurrect extends Prayer
{
	public String ID() { return "Prayer_Resurrect"; }
	public String name(){ return "Resurrect";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public Environmental newInstance(){	return new Prayer_Resurrect();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item body=this.getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(body==null) return false;
		if((!(body instanceof DeadBody))
		||(!((DeadBody)body).playerCorpse())
		||(((DeadBody)body).mobName().length()==0))
		{
			mob.tell("You can't resurrect that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,body,this,affectType(auto),auto?"<T-NAME> is resurrected!":"^S<S-NAME> resurrect(s) <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				invoker=mob;
				mob.location().send(mob,msg);
				MOB rejuvedMOB=CMMap.getPlayer(((DeadBody)body).mobName());
				if(rejuvedMOB!=null)
				{
					rejuvedMOB.tell("You are being resurrected.");
					if(rejuvedMOB.location()!=mob.location())
					{
						rejuvedMOB.location().showOthers(rejuvedMOB,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> disappears!");
						mob.location().bringMobHere(rejuvedMOB,false);
					}
					Ability A=rejuvedMOB.fetchAbility("Prop_AstralSpirit");
					if(A!=null) rejuvedMOB.delAbility(A);
					A=rejuvedMOB.fetchEffect("Prop_AstralSpirit");
					if(A!=null) rejuvedMOB.delEffect(A);

					int it=0;
					while(it<rejuvedMOB.location().numItems())
					{
						Item item=rejuvedMOB.location().fetchItem(it);
						if((item!=null)&&(item.container()==body))
						{
							FullMsg msg2=new FullMsg(rejuvedMOB,body,item,CMMsg.MSG_GET,null);
							rejuvedMOB.location().send(rejuvedMOB,msg2);
							FullMsg msg3=new FullMsg(rejuvedMOB,item,null,CMMsg.MSG_GET,null);
							rejuvedMOB.location().send(rejuvedMOB,msg3);
							it=0;
						}
						else
							it++;
					}
					body.destroy();
					rejuvedMOB.location().show(rejuvedMOB,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> get(s) up!");
					mob.location().recoverRoomStats();
					Vector whatsToDo=Util.parse(CommonStrings.getVar(CommonStrings.SYSTEM_PLAYERDEATH));
					for(int w=0;w<whatsToDo.size();w++)
					{
						String whatToDo=(String)whatsToDo.elementAt(w);
						if(whatToDo.startsWith("UNL"))
							rejuvedMOB.charStats().getCurrentClass().level(rejuvedMOB);
						else
						if(whatToDo.startsWith("ASTR"))
						{}
						else
						if(whatToDo.startsWith("PUR"))
						{}
						else
						if((whatToDo.trim().equals("0"))||(Util.s_int(whatToDo)>0))
						{
							int expLost=Util.s_int(whatToDo)/2;
							rejuvedMOB.tell("^F^*You regain "+expLost+" experience points.^?^.");
							MUDFight.postExperience(rejuvedMOB,null,null,expLost,false);
						}
						else
						if(whatToDo.length()<3)
							continue;
						else
						{
							int expLost=(100*rejuvedMOB.envStats().level())/2;
							rejuvedMOB.tell("^F^*You regain "+expLost+" experience points.^?^.");
							MUDFight.postExperience(rejuvedMOB,null,null,expLost,false);
						}
					}
				}
				else
					mob.location().show(mob,body,CMMsg.MSG_OK_VISUAL,"<T-NAME> twitch(es) for a moment, but the spirit is too far gone.");
			}
		}
		else
			beneficialWordsFizzle(mob,body,auto?"":"<S-NAME> attempt(s) to resurrect <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
