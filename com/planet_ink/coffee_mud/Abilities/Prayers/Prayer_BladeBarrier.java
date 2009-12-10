package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Prayer_BladeBarrier extends Prayer
{
	public String ID() { return "Prayer_BladeBarrier"; }
	public String name(){ return "Blade Barrier";}
	public String displayText(){ return "(Blade Barrier)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
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
			if((CMLib.dice().rollPercentage()>60+msg.source().charStats().getStat(CharStats.STAT_DEXTERITY))
			&&(msg.source().rangeToTarget()==0)
			&&((lastMessage==null)||(!lastMessage.startsWith("The blade barrier around")))
			&&((CMath.bset(msg.targetMajor(),CMMsg.MASK_HANDS))
			   ||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MOVE))))
			{
				MOB meM=(MOB)msg.target();
				int damage = CMLib.dice().roll(1,(int)Math.round(adjustedLevel(meM,0)/5.0),1);
				StringBuffer hitWord=new StringBuffer(CMLib.combat().standardHitWord(-1,damage));
				if(hitWord.charAt(hitWord.length()-1)==')')
					hitWord.deleteCharAt(hitWord.length()-1);
				if(hitWord.charAt(hitWord.length()-2)=='(')
					hitWord.deleteCharAt(hitWord.length()-2);
				if(hitWord.charAt(hitWord.length()-3)=='(')
					hitWord.deleteCharAt(hitWord.length()-3);
				CMLib.combat().postDamage((MOB)msg.target(),msg.source(),this,damage,CMMsg.TYP_CAST_SPELL|CMMsg.MASK_ALWAYS,Weapon.TYPE_SLASHING,"The blade barrier around <S-NAME> slices and <DAMAGE> <T-NAME>.");
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
		affectableStats.setArmor(affectableStats.armor()-1 - (adjustedLevel(invoker(),0)/10));
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

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),(auto?"":"^S<S-NAME> "+prayWord(mob)+" for divine protection!  ")+"A barrier of blades begin to spin around <T-NAME>!^?");
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
