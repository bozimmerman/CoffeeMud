package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_FlameWeapon extends Prayer
{
	public String ID() { return "Prayer_FlameWeapon"; }
	public String name(){ return "Flame Weapon";}
	public String displayText(){return "(Enflamed)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_HEATING|Ability.FLAG_BURNING;}
	private boolean notAgain=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GLOWING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
		if(affected instanceof Item)
			affectableStats.setAbility(affectableStats.ability()+1);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
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
			notAgain=true;
			FullMsg msg2=new FullMsg(msg.source(),msg.target(),affected,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,CMMsg.MSG_NOISYMOVEMENT,null);
			if(msg.source().location().okMessage(msg.source(),msg2))
			{
				msg.source().location().send(msg.source(), msg2);
				if(msg2.value()<=0)
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= baseEnvStats().level();
					msg.addTrailerMsg(new FullMsg(msg.source(),msg.target(),CMMsg.MSG_OK_ACTION,"The flame around "+affected.name()+" "+CommonStrings.standardHitWord(Weapon.TYPE_BURNING,flameDamage)+" <T-NAME>!"));
					FullMsg msg3=new FullMsg(msg.source(),msg.target(),null,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,CMMsg.MSG_DAMAGE,CMMsg.NO_EFFECT,null);
					msg3.setValue(flameDamage);
					msg.addTrailerMsg(msg3);
				}
			}
			notAgain=false;
		}
	}


	public void unInvoke()
	{
		Item destroyMe=null;
		// undo the affects of this spell
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof Item))
			{
				if(((((Weapon)affected).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
				&&((((Weapon)affected).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION))
				{
					if((((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
						((MOB)((Item)affected).owner()).tell("The flames around "+((Item)affected).name()+" consume it.");
					destroyMe=(Item)affected;
				}
				else
				if((((Item)affected).owner()!=null)&&(((Item)affected).owner() instanceof MOB))
					((MOB)((Item)affected).owner()).tell("The flames around "+((Item)affected).name()+" fade.");
			}
		}
		super.unInvoke();
		if(destroyMe!=null)
			destroyMe.destroy();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((auto||mob.isMonster())&&(commands.size()==0)&&(givenTarget==null))
		{
			Item I=mob.fetchWieldedItem();
			if(I==null)
				for(int i=0;i<mob.location().numItems();i++)
				{
					Item I2=mob.location().fetchItem(i);
					if((I2!=null)&&(I2.container()==null)&&(I2 instanceof Weapon))
					{ I2=I; break;}
				}
			if(I!=null) commands.addElement(I.Name());
		}
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!(target instanceof Weapon))
		{
			mob.tell("You can only enflame weapons.");
			return false;
		}
		if(((Weapon)target).fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already enflamed.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> appear(s) surrounded by flames!":"^S<S-NAME> hold(s) <T-NAMESELF> and "+prayWord(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,("<T-NAME> is engulfed in flames!")+CommonStrings.msp("fireball.wav",10));
				target.recoverEnvStats();
				mob.recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> and "+prayWord(mob)+", but nothing happens.");
		// return whether it worked
		return success;
	}
}
