package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease extends StdAbility
{
	public String ID() { return "Disease"; }
	public String name(){ return "Disease";}
	public String displayText(){ return "(a disease)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease();}
	public int classificationCode(){return Ability.DISEASE;}

	protected int DISEASE_TICKS(){return 48;}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your disease has run its coarse.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with a disease.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> ache(s) and groan(s).";}
	protected boolean DISEASE_STD(){return false;}
	protected boolean DISEASE_TOUCHSPREAD(){return false;}

	protected int diseaseTick=DISEASE_DELAY();

	protected boolean catchIt(MOB mob, MOB target)
	{
		if((target!=null)&&(target!=invoker)&&(target!=mob)&&(target.fetchAffect(ID())==null))
			if(Dice.rollPercentage()>target.charStats().getStat(CharStats.SAVE_DISEASE))
			{
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,DISEASE_START());
				maliciousAffect(invoker,target,DISEASE_TICKS(),-1);
				return true;
			}
		return false;
	}
	protected boolean catchIt(MOB mob)
	{
		MOB target=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
		return catchIt(mob,target);
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(mob,null,DISEASE_DONE());
	}

	public void affect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amISource(mob))
		&&(DISEASE_TOUCHSPREAD())
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_NATURAL)
		&&(affect.source().fetchWieldedItem()==null)
		&&(affect.target()!=null)
		&&(affect.target() instanceof MOB)
		&&(affect.target()!=affect.source())
		&&(Dice.rollPercentage()>(((MOB)affect.target()).charStats().getStat(CharStats.SAVE_DISEASE)+75)))
		{
			Ability A=(Ability)this.copyOf();
			A.invoke(mob,affect.target(),true);
		}
		super.affect(affect);
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MASK_HANDS|Affect.TYP_DISEASE|(auto?Affect.MASK_GENERAL:0),"");
			if(target.location().okAffect(msg))
			{
			    target.location().send(target,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,DISEASE_START());
				    success=maliciousAffect(mob,target,DISEASE_TICKS(),-1);
				}
			}
		}
        return success;
	}
}
