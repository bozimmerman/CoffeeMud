package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_AchillesArmor extends Spell
{
	public String ID() { return "Spell_AchillesArmor"; }
	public String name(){return "Achilles Armor";}
	public String displayText(){return "(Achilles Armor)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int overrideMana(){return 100;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ABJURATION;}
	private int vulnerability=0;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> Achilles Armor is now gone.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(msg.source()!=msg.target())
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(msg.source()))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(!mob.amDead()))
		{
			int weaponType=-1;
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Weapon))
			   weaponType=((Weapon)msg.tool()).weaponType();
			else
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_FIRE:
				weaponType=Weapon.TYPE_BURNING;
				break;
			case CMMsg.TYP_WATER:
				weaponType=Weapon.TYPE_FROSTING;
				break;
			case CMMsg.TYP_ACID:
				weaponType=Weapon.TYPE_MELTING;
				break;
			case CMMsg.TYP_COLD:
				weaponType=Weapon.TYPE_FROSTING;
				break;
			case CMMsg.TYP_GAS:
				weaponType=Weapon.TYPE_GASSING;
				break;
			case CMMsg.TYP_ELECTRIC:
				weaponType=Weapon.TYPE_STRIKING;
				break;
			case CMMsg.TYP_DISEASE:
			case CMMsg.TYP_POISON:
			case CMMsg.TYP_UNDEAD:
			case CMMsg.TYP_CAST_SPELL:
				weaponType=Weapon.TYPE_BURSTING;
				break;
			}
			if(weaponType<0)
				return super.okMessage(myHost,msg);

			if(weaponType!=vulnerability)
			{
				String name=null;
				if(msg.tool()==null)
					name="the attack";
				else
				if(msg.tool() instanceof Weapon)
					name=msg.tool().name();
				else
					name="the "+msg.tool().name();
				mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"The armor around <S-NAME> blocks "+name+" attack from <T-NAME>!");
				return false;
			}
			else
				MUDFight.postDeath(msg.source(),mob,msg);
		}
		return super.okMessage(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> attain(s) Achilles Armor!":"^S<S-NAME> invoke(s) Achilles Armor around <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				vulnerability=Dice.roll(1,Weapon.typeDescription.length,-1);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke Achilles Armor, but fail(s).");

		return success;
	}
}
