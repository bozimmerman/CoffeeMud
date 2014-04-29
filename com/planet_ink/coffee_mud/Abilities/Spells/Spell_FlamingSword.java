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
import com.planet_ink.coffee_mud.Items.Weapons.FlamingSword;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Spell_FlamingSword extends Spell
{
	public String ID() { return "Spell_FlamingSword"; }
	public String name(){return "Flaming Sword";}
	public String displayText(){return "";}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_LIGHTSOURCE);
	}
	
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()==affected)
		&&((msg.value())>0)
		&&(msg.target() instanceof MOB)
		&&(affected instanceof Item)
		&&(!((MOB)msg.target()).amDead())
		&&(msg.source()==((Item)affected).owner()))
		{
			final Room room=msg.source().location();
			CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),affected,
					CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,CMMsg.MSG_NOISYMOVEMENT,null);
			if((room!=null) && (room.okMessage(msg.source(),msg2)))
			{
				room.send(msg.source(), msg2);
				if(msg2.value()<=0)
				{
					int flameDamage = CMLib.dice().roll(1, (2+affected.basePhyStats().level())/2, 1);
					CMLib.combat().postDamage(msg.source(),(MOB)msg.target(),null,flameDamage,
							CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,name()+" <DAMAGE> <T-NAME>!");
				}
			}
		}
	}    
	
	public void unInvoke()
	{
		// undo the affects of this spell
		final Environmental item=affected;
		if(item==null) return;
		Room room=CMLib.map().roomLocation(item);
		if((canBeUninvoked())&&(room!=null))
			room.showHappens(CMMsg.MSG_OK_VISUAL,item,"<S-YOUPOSS> flaming sword is consumed!");
		super.unInvoke();
		if((canBeUninvoked())&&(room!=null))
		{
			room.recoverRoomStats();
		  item.destroy();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Physical target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
		{
			return false;
		}
		if((!(target instanceof Weapon))
		||(((Weapon)target).weaponClassification()!=Weapon.CLASS_SWORD)
		||(((((Item)target).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
			&&((((Item)target).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL)))
		{
			mob.tell("This magic only affects metal swords.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"^S<T-NAME> erupts into flame!":"^S<S-NAME> invoke(s) a writhing flame around <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				mob.location().recoverRoomStats(); // attempt to handle followers
			}
		}
		else
			beneficialWordsFizzle(mob,mob.location(),"<S-NAME> attempt(s) to invoke a flame, but cause(s) a puff of smoke.");

		return success;
	}
}
