package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Vampirism extends Prayer
{
	public String ID() { return "Prayer_Vampirism"; }
	public String name(){ return "Inflict Vampirism";}
	public String displayText(){ return "(Vampirism)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public Environmental newInstance(){	return new Prayer_Vampirism();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(Sense.canSee(mob)))
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.tell("Your vampirism fades.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!(affected instanceof MOB)) return;
		if(!((MOB)affected).isMonster())
		{
			if(((MOB)affected).location()==null) return;
			if(Sense.isInDark(((MOB)affected).location()))
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
			else
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(msg.amISource(mob)
			   &&(msg.tool()!=null)
			   &&(msg.tool().ID().equals("Skill_Swim")))
			{
				mob.tell("You can't swim!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+1);
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_EAT)
		&&(msg.target() instanceof Food))
			msg.source().curState().adjHunger(-((Food)msg.target()).nourishment(),msg.source().maxState());
		else
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(msg.target() instanceof Drink))
		{
			Drink D=(Drink)msg.target();
			if(D.containsDrink()
			&&(D.liquidType()!=EnvResource.RESOURCE_BLOOD)
			&&((!(D instanceof Item))||((Item)D).material()!=EnvResource.RESOURCE_BLOOD))
				msg.source().curState().adjThirst(-D.thirstQuenched(),msg.source().maxState());
			else
				msg.source().curState().adjHunger(D.thirstQuenched()*5,msg.source().maxState());
		}
	}
	
	public boolean raceWithBlood(Race R)
	{
		Vector V=R.myResources();
		if(V!=null)
		{
			for(int i2=0;i2<V.size();i2++)
			{
				Item I2=(Item)V.elementAt(i2);
				if((I2.material()==EnvResource.RESOURCE_BLOOD)
				&&(I2 instanceof Drink))
					return true;
			}
		}
		return false;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(!(affected instanceof MOB)))
		   return true;
		MOB M=(MOB)affected;
		if((M.location()!=null)&&(!Sense.isSleeping(M)))
		{
			M.curState().adjThirst(-(M.location().thirstPerRound(M)*2),M.maxState());
			M.curState().adjHunger(-2,M.maxState());
			if((M.isMonster())
			&&((M.curState().getThirst()<0)||(M.curState().getHunger()<0))
			&&(Dice.rollPercentage()<10)
			&&(Sense.aliveAwakeMobile(M,true))
			&&(M.fetchEffect("Butchering")==null))
			{
				DeadBody B=null;
				Drink D=null;
				for(int i=0;i<M.location().numItems();i++)
				{
					Item I=M.location().fetchItem(i);
					if((I!=null)
					&&(I instanceof DeadBody)
					&&(I.container()==null)
					&&(((DeadBody)I).charStats()!=null)
					&&(((DeadBody)I).charStats().getMyRace()!=null)
					&&(raceWithBlood(((DeadBody)I).charStats().getMyRace())))
						B=(DeadBody)I;
					else
					if((I!=null)
					&&(I instanceof Drink)
					&&(I.container()==null)
					&&((I.material()==EnvResource.RESOURCE_BLOOD)||(((Drink)I).liquidType()==EnvResource.RESOURCE_BLOOD)))
						D=(Drink)I;
				}
				if(D!=null)
				{
					CommonMsgs.get(M,null,(Item)D,false);
					if(M.isMine(D))
					{
						M.doCommand(Util.parse("DRINK "+D.Name()));
						if(M.isMine(D))
							((Item)D).destroy();
					}
					else
						((Item)D).destroy();
				}
				else
				if(B!=null)
				{
					Ability A=CMClass.getAbility("Butchering");
					if(A!=null) A.invoke(M,Util.parse(B.Name()),B,true);
				}
				else
				{
					MOB M2=M.location().fetchInhabitant(Dice.roll(1,M.location().numInhabitants(),-1));
					if((M2!=null)&&(M2!=M)&&(raceWithBlood(M2.charStats().getMyRace())))
						M.setVictim(M2);
				}
			}
		}
		return true;
	}

	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,-((target.charStats().getStat(CharStats.WISDOM)*2)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> invoke(s) a vampiric hunger upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> inflicted with vampiric hunger!");
					target.curState().setHunger(0);
					target.curState().setThirst(0);
					maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to inflict vampirism upon <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
