package com.planet_ink.coffee_mud.Abilities.Spells;

//**********************************************
// the colosal exception to the unbroken rule!!!
import com.planet_ink.coffee_mud.Commands.base.*;
//**********************************************

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Geas extends Spell
{
	public String ID() { return "Spell_Geas"; }
	public String name(){return "Geas";}
	public String displayText(){return "(Geas to "+text()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	public int maxRange(){return 5;}
	public Environmental newInstance(){	return new Spell_Geas();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}
	public EnglishParser.geasStep STEP=null;
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((STEP!=null)&&(STEP.que!=null)&&(STEP.que.size()==0))
				mob.tell("You have completed your geas.");
			else
				mob.tell("You have been released from your geas.");

			if((mob.isMonster())
			&&(!mob.amDead())
			&&(mob.location()!=null)
			&&(mob.location()!=mob.getStartRoom()))
				CoffeeUtensils.wanderAway(mob,true,true);
		}
	}

	public void affect(Environmental myHost, Affect msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&((msg.targetCode()&Affect.MASK_HURT)>0)
		&&((msg.targetCode()-Affect.MASK_HURT)>0))
			ExternalPlay.postPanic(mob,msg);
		if((msg.sourceMinor()==Affect.TYP_SPEAK)
		&&(STEP!=null)
		&&(msg.sourceMessage()!=null)
		&&((msg.target()==null)||(msg.target() instanceof MOB))
		&&(msg.sourceMessage().length()>0))
		{
			int start=msg.sourceMessage().indexOf("'");
			int end=msg.sourceMessage().lastIndexOf("'");
			if((start>0)&&(end>(start+1)))
				STEP.sayResponse(msg.source(),(MOB)msg.target(),msg.sourceMessage().substring(start+1,end));
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		if((tickID==Host.MOB_TICK)&&(STEP!=null))
		{
			if((STEP.que!=null)&&(STEP.que.size()==0))
			{
				unInvoke();
				return false;
			}
			if(STEP.que!=null)	STEP.step();
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isMonster())
		{
			mob.location().show(mob,null,Affect.MSG_NOISE,"<S-NAME> sigh(s).");
			ExternalPlay.quickSay(mob,null,"You know, if I had any ambitions, I would put the geas on myself!",false,false);
			return false;
		}

		if(commands.size()<2)
		{
			mob.tell("You need to specify a target creature, and a geas to place on them.");
			return false;
		}
		Vector name=Util.parse((String)commands.elementAt(0));
		commands.remove(commands.firstElement());
		MOB target=getTarget(mob,name,givenTarget);
		if(target==null) return false;
		if(target.charStats().getStat(CharStats.INTELLIGENCE)<5)
		{
			mob.tell(target.name()+" is too stupid to understand the instructions!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> place(s) a powerful geas upon <T-NAMESELF>!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				STEP=EnglishParser.processRequest(mob,target,Util.combine(commands,0));
				if((STEP==null)||(STEP.que==null)||(STEP.que.size()==0))
				{
					target.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> look(s) confused.");
					return false;
				}
				else
				{
					setMiscText(Util.combine(commands,0));
					if(maliciousAffect(mob,target,500,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0)))
					{
						target.makePeace();
						if(mob.getVictim()==target)
							mob.makePeace();
						if(target.location()==mob.location())
						{
							for(int m=0;m<target.location().numInhabitants();m++)
							{
								MOB M=target.location().fetchInhabitant(m);
								if((M!=null)&&(M.getVictim()==target))
									M.makePeace();
							}
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to place a geas on <T-NAMESELF>, but fails.");

		// return whether it worked
		return success;
	}
}