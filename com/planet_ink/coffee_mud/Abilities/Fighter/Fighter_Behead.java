package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Fighter_Behead extends StdAbility
{
	public String ID() { return "Fighter_Behead"; }
	public String name(){ return "Behead";}
	private static final String[] triggerStrings = {"BEHEAD"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int maxRange(){return 0;}
	public int classificationCode(){ return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
	    MOB target=super.getTarget(mob,commands,givenTarget);
	    if(target==null) return false;
		Race R=target.charStats().getMyRace();
		if(R.bodyMask()[Race.BODY_HEAD]<=0)
		{
		    mob.tell(target.name()+" has no head!");
		    return false;
		}
	    
		Behavior B=null;
		if(mob.location()!=null) B=CoffeeUtensils.getLegalBehavior(mob.location());
		Vector warrants=new Vector();
		if(B!=null)
		{
			warrants.addElement(new Integer(Law.MOD_GETWARRANTSOF));
			warrants.addElement(target.Name());
			if(!B.modifyBehavior(CoffeeUtensils.getLegalObject(mob.location()),target,warrants))
				warrants.clear();
		}
		if(warrants.size()==0)
		{
		    mob.tell("You are not allowed to behead "+target.Name()+" at this time.");
		    return false;
		}
		
		Item w=mob.fetchWieldedItem();
		Weapon ww=null;
		if(!auto)
		{
			if((w==null)||(!(w instanceof Weapon)))
			{
				mob.tell("You cannot behead without a weapon!");
				return false;
			}
			ww=(Weapon)w;
			if(ww.weaponType()!=Weapon.TYPE_SLASHING)
			{
				mob.tell("You cannot behead with a "+ww.name()+"!");
				return false;
			}
			if(mob.isInCombat()&&(mob.rangeToTarget()>0))
			{
				mob.tell("You are too far away to try that!");
				return false;
			}
			if(!Sense.isBoundOrHeld(target))
			{
				mob.tell(target.charStats().HeShe()+" is not bound and would resist.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-adjustedLevel(mob,asLevel);
		if(levelDiff>0)
			levelDiff=levelDiff*3;
		else
			levelDiff=0;
		boolean hit=(auto)||(Dice.normalizeAndRollLess(mob.adjustedAttackBonus(target)+target.adjustedArmor()));
		boolean success=profficiencyCheck(mob,0,auto)&&(hit);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MASK_MOVE|CMMsg.MASK_SOUND|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_GENERAL:0),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.curState().setHitPoints(1);
				int dmg=target.maxState().getHitPoints();
				Ability A2=target.fetchEffect("Injury");
				if(A2!=null) A2.setMiscText(mob.Name()+"/head");
				MUDFight.postDamage(mob,target,ww,dmg,CMMsg.MSG_WEAPONATTACK,ww.weaponClassification(),auto?"":"^F^<FIGHT^><S-NAME> rear(s) back and behead(s) <T-NAME>!^</FIGHT^>^?"+CommonStrings.msp("decap.wav",30));
				mob.location().recoverRoomStats();
				Item limb=CMClass.getItem("GenLimb");
				limb.setName(target.Name()+"`s head");
				limb.baseEnvStats().setAbility(1);
				limb.setDisplayText("the bloody head of "+target.Name()+" is sitting here.");
				limb.setSecretIdentity(target.name()+"`s bloody head.");
				int material=EnvResource.RESOURCE_MEAT;
				if((R!=null)&&(R.myResources()!=null)&&(R.myResources().size()>0))
					for(int r=0;r<R.myResources().size();r++)
					{
						Item I=(Item)R.myResources().elementAt(r);
						int mat=I.material()&EnvResource.MATERIAL_MASK;
						if(((mat==EnvResource.MATERIAL_FLESH))
						||(r==R.myResources().size()-1))
						{
							material=I.material();
							break;
						}
					}
				limb.setMaterial(material);
				limb.baseEnvStats().setLevel(1);
				limb.baseEnvStats().setWeight(5);
				limb.recoverEnvStats();
				mob.location().addItemRefuse(limb,Item.REFUSE_PLAYER_DROP);
				for(int i=0;i<warrants.size();i++)
				{
					LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
					W.setCrime("pardoned");
					W.setOffenses(0);
				}
			}
		}
		else
		    maliciousFizzle(mob,target,"<S-NAME> attempt(s) a beheading and fail(s)!");
		return success;
	}
}
