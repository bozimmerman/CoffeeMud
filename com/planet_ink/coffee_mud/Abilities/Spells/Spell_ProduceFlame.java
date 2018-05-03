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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_ProduceFlame extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ProduceFlame";
	}

	private final static String localizedName = CMLib.lang().L("Produce Flame");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Produce Flame)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_FIREBASED;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_LIGHTSOURCE|PhyStats.IS_GLOWING);
		affectableStats.setDisposition(affectableStats.disposition() & ~PhyStats.IS_DARK);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		final Room room=CMLib.map().roomLocation(affected);
		if(canBeUninvoked()&&(room!=null)&&(affected instanceof MOB))
			room.show((MOB)affected,null,CMMsg.MSG_OK_VISUAL,L("The flames around <S-YOUPOSS> hands go out."));
		super.unInvoke();
		if(canBeUninvoked()&&(room!=null))
			room.recoverRoomStats();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(mob.location()!=null)
		&&(msg.target() instanceof MOB)
		&&((!(msg.tool() instanceof Weapon))||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			final CMMsg msg2=CMClass.getMsg(mob,msg.target(),this,somanticCastCode(mob,(MOB)msg.target(),true),null);
			if(mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg2);
				if(msg2.value()<=0)
				{
					final int damage = CMLib.dice().roll(1,adjustedLevel(invoker(),0),1);
					CMLib.combat().postDamage(mob,(MOB)msg.target(),this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,L("The flames around <S-YOUPOSS> hands <DAMAGE> <T-NAME>!"));
				}
			}
		}
		return;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> already <S-HAS-HAVE> flaming hands."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final Room room=mob.location();
		if((success)&&(room!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?L("^S<S-NAME> attain(s) flaming hands!"):L("^S<S-NAME> evoke(s) gold and blue flames around <S-HIS-HER> hands!^?"));
			if(room.okMessage(mob,msg))
			{
				room.send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				room.recoverRoomStats();
			}
		}
		else
			beneficialWordsFizzle(mob,mob.location(),L("<S-NAME> attempt(s) to evoke flames, but fail(s)."));

		return success;
	}
}
