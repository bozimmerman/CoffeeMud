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
	
	// should be set to TRUE when the geas is BEING completed!
	private boolean completed=false;
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(completed)
				mob.tell("You have completed your geas.");
			else
				mob.tell("You have been released from your geas.");
			
			if((mob.isMonster())
			&&(!mob.amDead())
			&&(mob.location()!=null)
			&&(mob.location()!=mob.getStartRoom()))
			{
				CoffeeUtensils.wanderAway(mob,true);
				mob.getStartRoom().bringMobHere(mob,false);
			}
		}
	}

	public void affect(Environmental myHost, Affect msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(!completed)
		&&((msg.targetCode()&Affect.MASK_HURT)>0)
		&&((msg.targetCode()-Affect.MASK_HURT)>0))
			ExternalPlay.postPanic(mob,msg);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Host.MOB_TICK)&&(!completed))
		{
			// undo the affects of this spell
			if((affected==null)||(!(affected instanceof MOB)))
				return super.tick(ticking,tickID);
			MOB mob=(MOB)affected;
			CoffeeUtensils.wanderAway(mob,false);
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
		// kill x, bring x, find x, get x, social x, eat x, drink x, buy x,
		// sell x, do a skill x, open x, close x, go x, go to x, emote x, 
		// look x, follow x, give x x, hold x, lock x, unlock x, channel x, 
		// practice x, read x, say x, sit, sit x, sleep, sleep x, stand, 
		// take x, tell x x, wear x, wield x, hold x, mount x, dismount, 
		// serve x, rebuke x, crawl x, enter x, deposit x, withdraw x,
		// hire x, fire x, 
		
		
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
				if(target.location()==mob.location())
				{
					maliciousAffect(mob,target,0,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0));
					
					mob.tell("Geas isn't even CLOSE to being done yet...");
					
					target.makePeace();
					if(mob.getVictim()==target)
						mob.makePeace();
					for(int m=0;m<target.location().numInhabitants();m++)
					{
						MOB M=target.location().fetchInhabitant(m);
						if((M!=null)&&(M.getVictim()==target))
							M.makePeace();
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