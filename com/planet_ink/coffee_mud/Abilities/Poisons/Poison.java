package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Poison extends StdAbility
{
	public String ID() { return "Poison"; }
	public String name(){ return "Poison";}
	public String displayText(){ return "(Poisoned)";}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"POISONSTING"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Poison();}
	public int classificationCode(){return Ability.POISON;}
	protected int POISON_TICKS(){return 0;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 3;}
	protected String POISON_DONE(){return "The poison runs its course.";}
	protected String POISON_START(){return "^G<S-NAME> turn(s) green.^?";}
	protected String POISON_AFFECT(){return "<S-NAME> cringe(s) as the poison courses through <S-HIS-HER> blood.";}
	protected String POISON_CAST(){return "^F<S-NAME> attempt(s) to poison <T-NAMESELF>!^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return (invoker!=null)?Dice.roll(invoker().envStats().level(),3,1):0;}
	private boolean processing=false;
	
	protected int poisonTick=3;
	
	protected boolean catchIt(MOB mob, Environmental target)
	{
		MOB poisoner=invoker;
		if(poisoner==null) poisoner=mob;
		if((poisoner==null)&&(target instanceof MOB)) poisoner=(MOB)target;
		if((target!=null)&&(target instanceof MOB)&&(target.fetchAffect(ID())==null))
		{
			MOB targetMOB=(MOB)target;
			if(targetMOB.location().show(targetMOB,null,Affect.MASK_GENERAL|Affect.MASK_MALICIOUS|Affect.TYP_POISON,POISON_START()))
			{
				maliciousAffect(poisoner,target,POISON_TICKS(),-1);
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
		
		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if((--poisonTick)<=0)
		{
			poisonTick=POISON_DELAY();
			if(POISON_AFFECT().length()>0)
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,POISON_AFFECT());
			if(invoker==null) invoker=mob;
			if(POISON_DAMAGE()!=0)
				ExternalPlay.postDamage(invoker,mob,this,POISON_DAMAGE(),Affect.MASK_GENERAL|Affect.TYP_POISON,-1,null);
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if((affected==null)||(!ID().equals("Poison"))) return;
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-5);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-5);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
		if(affectableStats.getStat(CharStats.STRENGTH)<=0)
			affectableStats.setStat(CharStats.STRENGTH,1);
	}

	public void unInvoke()
	{
		if(affected!=null)
		{
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;

				super.unInvoke();
				if((canBeUninvoked())&&(POISON_DONE().length()>0))
					mob.tell(POISON_DONE());
			}
			else
			if(invoker!=null)
				super.unInvoke();
		}
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(affected==null) return;
		if(affected instanceof Item)
		{
			if(!processing)
			{
				Item myItem=(Item)affected;
				if(myItem.owner()==null) return;
				processing=true;
				if(affect.amITarget(myItem))
					switch(affect.sourceMinor())
					{
					case Affect.TYP_DRINK:
						if(myItem instanceof Drink)
							catchIt(affect.source(),affect.source());
						break;
					case Affect.TYP_EAT:
						if(myItem instanceof Food)
							catchIt(affect.source(),affect.source());
						break;
					}
				else
				if(affect.tool()==affected)
					switch(affect.sourceMinor())
					{
					case Affect.TYP_WEAPONATTACK:
						if((affect.source()!=affect.target())
						&&(affect.target()!=null)
						&&(myItem instanceof Weapon)
						&&(affect.target() instanceof MOB))
						{
							tickDown--;
							catchIt((MOB)affect.target(),affect.target());
						}
						break;
					}
			}
			processing=false;
		}
		super.affect(myHost,affect);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			String str=auto?"":POISON_CAST();
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_POISON|(auto?Affect.MASK_GENERAL:0),str);
			if(target.location().okAffect(target,msg))
			{
			    target.location().send(target,msg);
				if(!msg.wasModified())
				{
					target.location().show(target,null,Affect.MSG_OK_VISUAL,POISON_START());
				    success=maliciousAffect(mob,target,POISON_TICKS(),-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,POISON_FAIL());

        return success;

	}

}
