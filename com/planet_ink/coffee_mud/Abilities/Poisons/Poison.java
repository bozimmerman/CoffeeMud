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
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
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
		if((target!=null)&&(target instanceof MOB)&&(target.fetchEffect(ID())==null))
		{
			MOB targetMOB=(MOB)target;
			if(targetMOB.location().show(targetMOB,null,CMMsg.MASK_GENERAL|CMMsg.MASK_MALICIOUS|CMMsg.TYP_POISON,POISON_START()))
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
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,POISON_AFFECT()+CommonStrings.msp("poisoned.wav",10));
			if(invoker==null) invoker=mob;
			if(POISON_DAMAGE()!=0)
				MUDFight.postDamage(invoker,mob,this,POISON_DAMAGE(),CMMsg.MASK_GENERAL|CMMsg.TYP_POISON,-1,null);
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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(affected==null) return;
		if(affected instanceof Item)
		{
			if(!processing)
			{
				Item myItem=(Item)affected;
				if(myItem.owner()==null) return;
				processing=true;
				if(msg.amITarget(myItem))
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_DRINK:
						if(myItem instanceof Drink)
							catchIt(msg.source(),msg.source());
						break;
					case CMMsg.TYP_EAT:
						if(myItem instanceof Food)
							catchIt(msg.source(),msg.source());
						break;
					}
				else
				if(msg.tool()==affected)
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_WEAPONATTACK:
						if((msg.source()!=msg.target())
						&&(msg.target()!=null)
						&&(myItem instanceof Weapon)
						&&(msg.target() instanceof MOB))
						{
							tickDown--;
							catchIt((MOB)msg.target(),msg.target());
						}
						break;
					}
			}
			processing=false;
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=this.getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			String str=auto?"":POISON_CAST();
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_POISON|(auto?CMMsg.MASK_GENERAL:0),str);
			Room R=mob.location();
			if((target instanceof MOB)&&(((MOB)target).location()!=null))
				R=((MOB)target).location();
			if(R.okMessage(mob,msg))
			{
			    R.send(mob,msg);
				if(msg.value()<=0)
				{
					if(target instanceof MOB)
						R.show((MOB)target,null,CMMsg.MSG_OK_VISUAL,POISON_START());
				    success=maliciousAffect(mob,target,POISON_TICKS(),-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,POISON_FAIL());

        return success;

	}

}
