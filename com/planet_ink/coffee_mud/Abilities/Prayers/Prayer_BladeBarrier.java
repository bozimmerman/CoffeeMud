package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_BladeBarrier extends Prayer
{
	public String ID() { return "Prayer_BladeBarrier"; }
	public String name(){ return "Blade Barrier";}
	public String displayText(){ return "(Blade Barrier)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_HOLY;}
	String lastMessage=null;


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> blade barrier disappears.");
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((invoker==null)
		||(affected==null)
		||(!(affected instanceof MOB)))
			return;
		if(msg.target()==invoker)
		{
			if((Dice.rollPercentage()>60+msg.source().charStats().getStat(CharStats.DEXTERITY))
			&&(msg.source().rangeToTarget()==0)
			&&((lastMessage==null)||(!lastMessage.startsWith("The blade barrier around")))
			&&((Util.bset(msg.targetMajor(),CMMsg.MASK_HANDS))
			   ||(Util.bset(msg.targetMajor(),CMMsg.MASK_MOVE))))
			{
				int level=(int)Math.round(Util.div(invoker.envStats().level(),6.0));
				if(level>5) level=5;
				int damage=Dice.roll(2,level,0);
				StringBuffer hitWord=new StringBuffer(CommonStrings.standardHitWord(-1,damage));
				if(hitWord.charAt(hitWord.length()-1)==')')
					hitWord.deleteCharAt(hitWord.length()-1);
				if(hitWord.charAt(hitWord.length()-2)=='(')
					hitWord.deleteCharAt(hitWord.length()-2);
				if(hitWord.charAt(hitWord.length()-3)=='(')
					hitWord.deleteCharAt(hitWord.length()-3);
				MUDFight.postDamage((MOB)msg.target(),msg.source(),this,damage,CMMsg.MSG_OK_ACTION,Weapon.TYPE_SLASHING,"The blade barrier around <S-NAME> slices and <DAMAGE> <T-NAME>.");
				lastMessage="The blade barrier around";
			}
			else
				lastMessage=msg.othersMessage();
		}
		else
			lastMessage=msg.othersMessage();
		return;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;

		affectableStats.setArmor(affectableStats.armor()-mob.envStats().level());
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> the blade barrier.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"":"^S<S-NAME> "+prayWord(mob)+" for divine protection!  ")+"A barrier of blades begin to spin around <T-NAME>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for divine protection, but nothing happens.");


		// return whether it worked
		return success;
	}
}
