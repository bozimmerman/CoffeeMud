package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class Disease extends StdAbility implements DiseaseAffect
{
	public String ID() { return "Disease"; }
	public String name(){ return "Disease";}
	public String displayText(){ return "(a disease)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	private static final String[] triggerStrings = {"DISEASE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int classificationCode(){return Ability.DISEASE;}

	protected int DISEASE_TICKS(){return 48;}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your disease has run its course.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with a disease.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> ache(s) and groan(s).";}

	public int difficultyLevel(){return 0;}
	public int abilityCode(){return 0;}
	private boolean processing=false;

	protected int diseaseTick=DISEASE_DELAY();

	protected boolean catchIt(MOB mob, Environmental target)
	{
		MOB diseased=invoker;
		if(invoker==target) return true;
		if(diseased==null) diseased=mob;
		if((diseased==null)&&(target instanceof MOB)) diseased=(MOB)target;
		if((target!=null)
		&&(diseased!=null)
		&&(target.fetchEffect(ID())==null))
		{
			if(target instanceof MOB)
			{
				MOB targetMOB=(MOB)target;
				if((Dice.rollPercentage()>targetMOB.charStats().getSave(CharStats.SAVE_DISEASE))
				&&(targetMOB.location()!=null))
				{
					MOB following=targetMOB.amFollowing();
					boolean doMe=invoke(diseased,targetMOB,true,0);
					if(targetMOB.amFollowing()!=following)
						targetMOB.setFollowing(following);
					return doMe;
				}
			}
			else
			{
				maliciousAffect(diseased,target,0,DISEASE_TICKS(),-1);
				return true;
			}
		}
		return false;
	}
	protected boolean catchIt(MOB mob)
	{
		if(mob==null) return false;
		if(mob.location()==null) return false;
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
			{
				if(!mob.amDead())
				{
					Ability A=mob.fetchEffect("TemporaryImmunity");
					if(A==null)
					{
						A=CMClass.getAbility("TemporaryImmunity");
						A.setBorrowed(mob,true);
						A.makeLongLasting();
						mob.addEffect(A);
						A.makeLongLasting();
					}
					A.setMiscText("+"+ID());
				}
				mob.tell(mob,null,this,DISEASE_DONE());
			}
		}
		else
			super.unInvoke();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(affected==null) return;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;

			// when this spell is on a MOBs Affected list,
			// it should consistantly prevent the mob
			// from trying to do ANYTHING except sleep
			if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_DAMAGE))
			&&(msg.amISource(mob))
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon)
			&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)
			&&(msg.source().fetchWieldedItem()==null)
			&&(msg.target()!=null)
			&&(msg.target() instanceof MOB)
			&&(msg.target()!=msg.source())
			&&(Dice.rollPercentage()>(((MOB)msg.target()).charStats().getSave(CharStats.SAVE_DISEASE)+70)))
				catchIt(mob,msg.target());
			else
			if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONTACT))
			&&(msg.amISource(mob)||msg.amITarget(mob))
			&&(msg.target()!=null)
			&&(msg.target() instanceof MOB)
			&&(Util.bset(msg.targetCode(),CMMsg.MASK_MOVE)||Util.bset(msg.targetCode(),CMMsg.MASK_HANDS))
			&&((msg.tool()==null)
				||(msg.tool()!=null)
					&&(msg.tool() instanceof Weapon)
					&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
				catchIt(mob,msg.amITarget(mob)?msg.source():msg.target());
			else
			if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_STD))
			&&((msg.amITarget(mob))||(msg.amISource(mob)))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Social"))
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>")))
				catchIt(mob,msg.amITarget(mob)?msg.source():msg.target());
		}
		else
		if(affected instanceof Item)
		{
			if(!processing)
			{
				Item myItem=(Item)affected;
				if(myItem.owner()==null) return;
				processing=true;
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_DRINK:
					if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONSUMPTION))
					||(Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONTACT)))
					{
						if((myItem instanceof Drink)
						&&(msg.amITarget(myItem)))
							catchIt(msg.source(),msg.source());
					}
					break;
				case CMMsg.TYP_EAT:
					if((Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONSUMPTION))
					||(Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONTACT)))
					{

						if((myItem instanceof Food)
						&&(msg.amITarget(myItem)))
							catchIt(msg.source(),msg.source());
					}
					break;
				case CMMsg.TYP_GET:
					if(Util.bset(abilityCode(),DiseaseAffect.SPREAD_CONTACT))
					{
						if((!(myItem instanceof Drink))
						  &&(!(myItem instanceof Food))
						  &&(msg.amITarget(myItem)))
							catchIt(msg.source(),msg.source());
					}
					break;
				}
			}
			processing=false;
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			MOB mvictim=mob.getVictim();
			MOB tvictim=target.getVictim();
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_HANDS|(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MASK_MALICIOUS|CMMsg.TYP_DISEASE,"");
			Room R=target.location();
			if((R!=null)&&(R.okMessage(target,msg)))
			{
			    R.send(target,msg);
				if(msg.value()<=0)
				{
					R.show(target,null,CMMsg.MSG_OK_VISUAL,DISEASE_START());
				    success=maliciousAffect(mob,target,asLevel,DISEASE_TICKS(),-1);
				}
			}
			if(auto)
			{
				if(mob.getVictim()!=mvictim) mob.setVictim(mvictim);
				if(target.getVictim()!=tvictim) target.setVictim(tvictim);
			}
		}
        return success;
	}
}
