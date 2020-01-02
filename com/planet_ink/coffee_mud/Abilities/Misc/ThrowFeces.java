package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class ThrowFeces extends StdAbility
{
	@Override
	public String ID()
	{
		return "ThrowFeces";
	}

	private final static String	localizedName	= CMLib.lang().L("Throw Feces");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Covered in feces)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "THROWFECES" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected volatile int maxRange = 10;

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(maxRange);
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		affectableStats.setDamage(2);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.source()==affected)
		&&(msg.tool() instanceof Weapon))
			msg.setValue(CMLib.dice().roll(1, 2, 0));
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			this.maxRange=1+super.getXMAXRANGELevel(mob);
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"":L("^F^<FIGHT^><S-NAME> reach(es) around <S-HIM-HERSELF>.^</FIGHT^>^N"));
			CMLib.color().fixSourceFightColor(msg);
			if(target.location().okMessage(target,msg))
			{
				target.location().send(target,msg);
				if(msg.value()<=0)
				{
					final Weapon fecesI=CMClass.getWeapon("GenWeapon");
					fecesI.setName("a glob of feces");
					fecesI.setDisplayText("a glob of feces sits here.");
					fecesI.setWeaponClassification(Weapon.CLASS_THROWN);
					fecesI.setWeaponDamageType(Weapon.TYPE_NATURAL);
					fecesI.basePhyStats().setDamage(2);
					fecesI.basePhyStats().setWeight(1);
					fecesI.setRanges(0,this.maxRange);
					final Ability casterA=CMClass.getAbility("Prop_FightSpellCast");
					final int chance=5+super.getXLEVELLevel(msg.source());
					final String[] diseases=new String[]{
						"Disease_Tetnus", "Disease_Lyme", "Disease_Fever", "Disease_Infection"
					};
					casterA.setMiscText(chance+"%;"+diseases[CMLib.dice().roll(1, diseases.length, -1)]);
					fecesI.addNonUninvokableEffect(casterA);
					fecesI.recoverPhyStats();
					fecesI.setRawWornCode(Wearable.WORN_WIELD);
					try
					{
						mob.addItem(fecesI);
						mob.addEffect(this);
						mob.recoverPhyStats();
						if(CMLib.combat().postAttack(mob,target,fecesI)
						&& (target.fetchEffect("Soiled")==null))
						{
							final Ability soiledA=CMClass.getAbility("Soiled");
							if(soiledA!=null)
								soiledA.startTickDown(mob,target,Ability.TICKS_ALMOST_FOREVER);
						}
					}
					finally
					{
						mob.delEffect(this);
						fecesI.destroy();
						mob.recoverPhyStats();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to produce feces with which to attack <T-NAMESELF>, but fail(s)!"));

		// return whether it worked
		return success;
	}
}
