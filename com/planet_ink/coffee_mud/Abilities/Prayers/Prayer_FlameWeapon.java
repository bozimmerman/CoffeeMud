package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_FlameWeapon extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_FlameWeapon";
	}

	private final static String localizedName = CMLib.lang().L("Flame Weapon");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Enflamed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY|Ability.FLAG_HEATING|Ability.FLAG_FIREBASED;
	}

	protected boolean notAgain=false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_BONUS);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GLOWING);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_LIGHTSOURCE);
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()+1);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((msg.source().location()!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(msg.tool()==affected)
		&&(!notAgain)
		&&(msg.target() instanceof MOB)
		&&(!((MOB)msg.target()).amDead()))
		{
			try
			{
				notAgain=true;
				final CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),affected,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,CMMsg.MSG_NOISYMOVEMENT,null);
				if(msg.source().location().okMessage(msg.source(),msg2))
				{
					msg.source().location().send(msg.source(), msg2);
					if(msg2.value()<=0)
					{
						int flameDamage = (int) Math.round( Math.random() * 6 );
						flameDamage *= (super.getXLEVELLevel(invoker())+(super.getX1Level(invoker())));
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),CMMsg.MSG_OK_ACTION,L("^RThe flame around @x1 @x2 <T-NAME>!^?",affected.name(),CMLib.combat().standardHitWord(Weapon.TYPE_BURNING,flameDamage))));
						final CMMsg msg3=CMClass.getMsg(msg.source(),msg.target(),null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,CMMsg.MSG_DAMAGE,CMMsg.NO_EFFECT,null);
						msg3.setValue(flameDamage);
						msg.addTrailerMsg(msg3);
					}
				}
			}finally{notAgain=false;}
		}
	}

	@Override
	public void unInvoke()
	{
		Item destroyMe=null;
		// undo the affects of this spell
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof Item))
			{
				if(((((Weapon)affected).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
				||((((Weapon)affected).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION))
				{
					if((((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
						((MOB)((Item)affected).owner()).tell(L("The flames around @x1 consume it.",((Item)affected).name()));
					destroyMe=(Item)affected;
				}
				else
				if((((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
					((MOB)((Item)affected).owner()).tell(L("The flames around @x1 fade.",((Item)affected).name()));
			}
		}
		super.unInvoke();
		if(destroyMe!=null)
			destroyMe.destroy();
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if((mob.fetchWieldedItem() instanceof Weapon)
			&&(mob.fetchWieldedItem().fetchEffect(ID())==null))
				return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if(!(target instanceof Weapon))
		{
			if(auto||mob.isMonster())
			{
				target=mob.fetchWieldedItem();
				if(target==null)
				{
					for(int i=0;i<mob.location().numItems();i++)
					{
						final Item I2=mob.location().getItem(i);
						if((I2!=null)&&(I2.container()==null)&&(I2 instanceof Weapon))
						{
							target=I2;
							break;
						}
					}
				}
			}
			if(!(target instanceof Weapon))
			{
				mob.tell(L("You can only enflame weapons."));
				return false;
			}
		}

		if(((Weapon)target).fetchEffect(this.ID())!=null)
		{
			mob.tell(L("@x1 is already enflamed.",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> appear(s) surrounded by flames!"):L("^S<S-NAME> hold(s) <T-NAMESELF> and @x1.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,("<T-NAME> is engulfed in flames!")+CMLib.protocol().msp("fireball.wav",10));
				target.recoverPhyStats();
				mob.recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> hold(s) <T-NAMESELF> and @x1, but nothing happens.",prayWord(mob)));
		// return whether it worked
		return success;
	}
}
