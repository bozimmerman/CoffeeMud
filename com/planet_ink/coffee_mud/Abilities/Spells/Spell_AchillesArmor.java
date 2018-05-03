package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

	@Override
	public String ID()
	{
		return "Spell_AchillesArmor";
	}

	private final static String localizedName = CMLib.lang().L("Achilles Armor");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Achilles Armor)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int overrideMana()
	{
		return 100;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;
	}

	protected int vulnerability=0;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> Achilles Armor is now gone."));
		}

		super.unInvoke();

	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(msg.source()!=msg.target())
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(msg.source()))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(!mob.amDead()))
		{
			int weaponDamageType=-1;
			if(msg.tool() instanceof Weapon)
				weaponDamageType=((Weapon)msg.tool()).weaponDamageType();
			else
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_FIRE:
				weaponDamageType=Weapon.TYPE_BURNING;
				break;
			case CMMsg.TYP_WATER:
				weaponDamageType=Weapon.TYPE_FROSTING;
				break;
			case CMMsg.TYP_ACID:
				weaponDamageType=Weapon.TYPE_MELTING;
				break;
			case CMMsg.TYP_COLD:
				weaponDamageType=Weapon.TYPE_FROSTING;
				break;
			case CMMsg.TYP_GAS:
				weaponDamageType=Weapon.TYPE_GASSING;
				break;
			case CMMsg.TYP_ELECTRIC:
				weaponDamageType=Weapon.TYPE_STRIKING;
				break;
			case CMMsg.TYP_DISEASE:
			case CMMsg.TYP_POISON:
			case CMMsg.TYP_UNDEAD:
			case CMMsg.TYP_CAST_SPELL:
				weaponDamageType=Weapon.TYPE_BURSTING;
				break;
			case CMMsg.TYP_JUSTICE:
				weaponDamageType=Weapon.TYPE_BASHING;
				break;
			}
			if(weaponDamageType<0)
				return super.okMessage(myHost,msg);

			if(weaponDamageType!=vulnerability)
			{
				String name=null;
				if(msg.tool()==null)
					name="the attack";
				else
				if(msg.tool() instanceof Weapon)
					name=msg.tool().name();
				else
					name="the "+msg.tool().name();
				mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,L("The armor around <S-NAME> blocks @x1 attack from <T-NAME>!",name));
				return false;
			}
			CMLib.combat().postDeath(msg.source(),mob,msg);
		}
		if(msg.tool() instanceof Ability)
		{
			if(msg.tool().ID().equals("Amputation"))
			{
				mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,L("The armor around <S-NAME> protect(s) <T-NAME>!"));
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> attain(s) Achilles Armor!"):L("^S<S-NAME> invoke(s) Achilles Armor around <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				vulnerability=CMLib.dice().roll(1,Weapon.TYPE_DESCS.length,-1);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to invoke Achilles Armor, but fail(s)."));

		return success;
	}
}
