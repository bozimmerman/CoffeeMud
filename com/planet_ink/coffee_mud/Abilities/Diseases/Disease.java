package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease extends StdAbility implements DiseaseAffect
{
	public String ID() { return "Disease"; }
	public String name(){ return "Disease";}
	public String displayText(){ return "(a disease)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease();}
	public int classificationCode(){return Ability.DISEASE;}

	protected int DISEASE_TICKS(){return 48;}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your disease has run its coarse.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with a disease.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> ache(s) and groan(s).";}
	
	public int abilityCode(){return 0;}
	private boolean processing=false;
	

	protected int diseaseTick=DISEASE_DELAY();

	protected boolean catchIt(MOB mob, Environmental target)
	{
		MOB diseased=invoker;
		if(invoker==target) return true;
		if(diseased==null) diseased=mob;
		if((diseased==null)&&(target instanceof MOB)) diseased=(MOB)target;
		if((target!=null)&&(diseased!=null)&&(target.fetchAffect(ID())==null))
		{
			if(target instanceof MOB)
			{
				MOB targetMOB=(MOB)target;
				if(Dice.rollPercentage()>targetMOB.charStats().getStat(CharStats.SAVE_DISEASE))
				{
					targetMOB.location().show(targetMOB,null,Affect.MSG_OK_VISUAL,DISEASE_START());
					maliciousAffect(diseased,target,DISEASE_TICKS(),-1);
					return true;
				}
			}
			else
			{
				maliciousAffect(diseased,target,DISEASE_TICKS(),-1);
				return true;
			}
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
		if(affected==null)
			return;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;

			super.unInvoke();
			if(canBeUninvoked())
				mob.tell(mob,null,this,DISEASE_DONE());
		}
		else
			super.unInvoke();
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if(affected==null) return;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;

			// when this spell is on a MOBs Affected list,
			// it should consistantly prevent the mob
			// from trying to do ANYTHING except sleep
			if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_DAMAGE))
			&&(affect.amISource(mob))
			&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon)
			&&(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_NATURAL)
			&&(affect.source().fetchWieldedItem()==null)
			&&(affect.target()!=null)
			&&(affect.target() instanceof MOB)
			&&(affect.target()!=affect.source())
			&&(Dice.rollPercentage()>(((MOB)affect.target()).charStats().getStat(CharStats.SAVE_DISEASE)+70)))
				catchIt(mob,affect.target());
			else
			if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONTACT))
			&&(affect.amISource(mob)||affect.amITarget(mob))
			&&(affect.target()!=null)
			&&(affect.target() instanceof MOB)
			&&(Util.bset(affect.targetCode(),Affect.MASK_MOVE)||Util.bset(affect.targetCode(),Affect.MASK_HANDS))
			&&((affect.tool()==null)
				||(affect.tool()!=null)
					&&(affect.tool() instanceof Weapon)
					&&(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
				catchIt(mob,affect.amITarget(mob)?affect.source():affect.target());
		}
		else
		if(affected instanceof Item)
		{
			if(!processing)
			{
				Item myItem=(Item)affected;
				if(myItem.owner()==null) return;
				processing=true;
				switch(affect.sourceMinor())
				{
				case Affect.TYP_DRINK:
					if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONSUMPTION))
					||(Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONTACT)))
					{
						if((myItem instanceof Drink)
						&&(affect.amITarget(myItem)))
							catchIt(affect.source(),affect.source());
					}
					break;
				case Affect.TYP_EAT:
					if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONSUMPTION))
					||(Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONTACT)))
					{
						
						if((myItem instanceof Food)
						&&(affect.amITarget(myItem)))
							catchIt(affect.source(),affect.source());
					}
					break;
				case Affect.TYP_GET:
					if(Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONTACT))
					{
						if((!(myItem instanceof Drink))
						  &&(!(myItem instanceof Food))
						  &&(affect.amITarget(myItem)))
							catchIt(affect.source(),affect.source());
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
			FullMsg msg=new FullMsg(mob,target,this,Affect.MASK_HANDS|Affect.TYP_DISEASE|(auto?Affect.MASK_GENERAL:0),"");
			if(target.location().okAffect(target,msg))
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
