package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class Skill_Puppeteer extends StdAbility
{
	public String ID() { return "Skill_Puppeteer"; }
	public String name(){ return "Puppeteer";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"PUPPETEER","PUPPET"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Puppeteer();}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		Item puppet=(Item)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amISource(invoker()))
		&&(!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
		&&((Util.bset(affect.sourceMajor(),Affect.MASK_HANDS))
		||(Util.bset(affect.sourceMajor(),Affect.MASK_MOVE)))
		&&(affect.targetMinor()!=Affect.TYP_LEAVE)
		&&(affect.targetMinor()!=Affect.TYP_ENTER)
		&&(affect.targetMinor()!=Affect.TYP_SPEAK)
		&&(affect.targetMinor()!=Affect.TYP_PANIC)
		&&(!((affect.tool()!=null)&&(affect.tool() instanceof Song)))
		&&(!((affect.tool()!=null)&&(affect.tool() instanceof Dance)))
		&&(!affect.amITarget(puppet)))
		{
			invoker().location().show(invoker(),puppet,Affect.MSG_OK_ACTION,"<S-NAME> animate(s) <T-NAMESELF>.");
			return false;
		}
		else
		if(affect.amITarget(puppet))
			switch(affect.targetMinor())
			{
			case Affect.TYP_GET:
				unInvoke();
				break;
			}
		return super.okAffect(myHost,affect);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room)
		&&(invoker()!=null)
		&&(invoker().location().isContent((Item)affected)))
		{
			if(invoker().isInCombat())
			{
				boolean isHit=(CoffeeUtensils.normalizeAndRollLess(invoker().adjustedAttackBonus()
																   +((Item)affected).envStats().attackAdjustment()
																   +invoker().getVictim().adjustedArmor()));
				if(!isHit)
					invoker().location().show(invoker(),invoker().getVictim(),affected,Affect.MSG_OK_ACTION,"<O-NAME> attacks <T-NAME> and misses!");
				else
					ExternalPlay.postDamage(invoker(),invoker().getVictim(),(Item)affected,
											Dice.roll(1,affected.envStats().level(),1),
											Affect.MASK_GENERAL|Affect.TYP_WEAPONATTACK,
											Weapon.TYPE_BASHING,affected.name()+" attacks and <DAMAGE> <T-NAME>!");
			}
			else
			if(Dice.rollPercentage()>75)
			switch(Dice.roll(1,5,0))
			{
			case 1:
				invoker().location().showHappens(Affect.MSG_OK_VISUAL,affected.name()+" walks around.");
				break;
			case 2:
				invoker().location().showHappens(Affect.MSG_OK_VISUAL,affected.name()+" waves its little arms.");
				break;
			case 3:
				invoker().location().showHappens(Affect.MSG_OK_VISUAL,affected.name()+" hugs you.");
				break;
			case 4:
				invoker().location().showHappens(Affect.MSG_OK_VISUAL,affected.name()+" makes a few fake attacks.");
				break;
			case 5:
				invoker().location().showHappens(Affect.MSG_OK_VISUAL,affected.name()+" dances around.");
				break;
			}
		}
		else
			unInvoke();
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if((affected!=null)
		&&(affected instanceof Item)
		&&(((Item)affected).owner()!=null)
		&&(((Item)affected).owner() instanceof Room))
			((Room)((Item)affected).owner()).showHappens(Affect.MSG_OK_ACTION,affected.name()+" stops moving.");
		super.unInvoke();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;
		if(!target.Name().toUpperCase().endsWith(" puppet"))
		{
			mob.tell("That's not a puppet!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_DELICATE_HANDS_ACT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				target.remove();
				if(mob.isMine(target))
					mob.location().show(mob,target,Affect.MSG_DROP,"<S-NAME> start(s) animating <T-NAME>!");
				else
					mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<S-NAME> start(s) animating <T-NAME>!");
				if(mob.location().isContent(target))
					beneficialAffect(mob,target,0);
			}
		}
		else
			mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> twitch(es) oddly, but does nothing more.");


		// return whether it worked
		return success;
	}
}