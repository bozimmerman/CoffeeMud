package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2024-2024 Bo Zimmerman

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
public class Skill_RustingStrike extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_RustingStrike";
	}

	private final static String	localizedName	= CMLib.lang().L("Rusting Strike");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS|CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "RUSTINGSTRIKE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target = super.getTarget(mob, commands, givenTarget);
		if(target == null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final int topDamage=(adjustedLevel(mob,asLevel)/6)+1;
		final int damage=CMLib.dice().roll(1,topDamage,0);
		if(success)
		{
			final Room roomR=CMLib.map().roomLocation(mob);
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.MASK_MALICIOUS|CMMsg.TYP_ACID|(auto?CMMsg.MASK_ALWAYS:0),
					L("<S-NAME> strike(s) at <T-NAME>."));
			if(roomR.okMessage(mob,msg))
			{
				roomR.send(mob,msg);
				if(msg.value() <=0)
				{
					CMLib.combat().postDamage(mob,target,this,damage,
							CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,
							L("^F^<FIGHT^><S-NAME> <DAMAGE> <T-NAME>!^</FIGHT^>^?@x1",CMLib.protocol().msp("bashed1.wav",30)));

					final Map<Long,Item> candidates = new TreeMap<Long,Item>();
					int metalCount = 0;
					for(final Enumeration<Item> i=target.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I!=null)
						&&(I.amBeingWornProperly()))
						{
							if(((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
							&&(RawMaterial.CODES.HARDNESS(I.material())<10)
							&&(I.subjectToWearAndTear()))
								metalCount++;
							for(final long l : Wearable.CODES.ALL())
							{
								if(CMath.bset(I.rawWornCode(), l))
								{
									final Long L = Long.valueOf(l);
									if(I instanceof Armor)
									{
										if(candidates.containsKey(L))
										{
											final Item otherI = candidates.get(L);
											if((otherI instanceof Armor)
											&&(((Armor)otherI).getClothingLayer()<((Armor)I).getClothingLayer()))
												candidates.put(L, I);
										}
									}
									else
										candidates.put(L, I);
								}
							}
						}
					}
					if((candidates.size()>0)
					&&(metalCount>0))
					{
						final List<Item> items = new XVector<Item>(candidates.values());
						final Item rustI =items.get(CMLib.dice().roll(1, items.size(), -1));
						if(((rustI.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
						&&(RawMaterial.CODES.HARDNESS(rustI.material())<10)
						&&(rustI.subjectToWearAndTear()))
						{
							int amt = 10 * (10-RawMaterial.CODES.HARDNESS(rustI.material()));
							final int uses = rustI.usesRemaining() / 10;
							CMLib.combat().postItemDamage(mob, rustI, null, amt, CMMsg.TYP_ACID, "<T-NAME> rusts!");
							final Food F = (Food)CMClass.getBasicItem("GenFoodResource");
							if(F != null)
							{
								if(amt > uses)
									amt = uses;
								if(amt <= 0)
									amt = 1;
								F.setBite(F.nourishment());
								F.setNourishment(F.nourishment() * amt);
								F.setName("some rust");
								F.setDisplayText("some rust lies here");
								F.basePhyStats().setWeight(amt);
								F.phyStats().setWeight(amt);
								F.setMaterial(RawMaterial.RESOURCE_DUST);
								((RawMaterial)F).setSubType("RUST");
								final Ability A = CMClass.getAbility("Disease_Lockjaw");
								if(A!=null)
									F.addNonUninvokableEffect(A);
								final Room R = CMLib.map().roomLocation(mob);
								if(R!=null)
									R.addItem(F, Expire.Monster_EQ);
							}
						}
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to strike <T-NAME>, but fail(s)."));

		return success;
	}
}
