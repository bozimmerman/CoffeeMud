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
public class Prayer_FlameWeapon extends Prayer
{
	public String ID() { return "Prayer_FlameWeapon"; }
	public String name(){ return "Flame Weapon";}
	public String displayText(){return "(Enflamed)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_HEATING|Ability.FLAG_FIREBASED;}
    protected boolean notAgain=false;

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
            try{
    			notAgain=true;
    			CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),affected,CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,CMMsg.MSG_NOISYMOVEMENT,null);
    			if(msg.source().location().okMessage(msg.source(),msg2))
    			{
    				msg.source().location().send(msg.source(), msg2);
    				if(msg2.value()<=0)
    				{
    					int flameDamage = (int) Math.round( Math.random() * 6 );
    					flameDamage *= (super.getXLEVELLevel(invoker())+(super.getX1Level(invoker())));
    					msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),CMMsg.MSG_OK_ACTION,"^RThe flame around "+affected.name()+" "+CMLib.combat().standardHitWord(Weapon.TYPE_BURNING,flameDamage)+" <T-NAME>!^?"));
    					CMMsg msg3=CMClass.getMsg(msg.source(),msg.target(),null,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,CMMsg.MSG_DAMAGE,CMMsg.NO_EFFECT,null);
    					msg3.setValue(flameDamage);
    					msg.addTrailerMsg(msg3);
    				}
    			}
            }finally{notAgain=false;}
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
				if(((((Weapon)affected).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
				||((((Weapon)affected).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION))
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

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if((mob.fetchWieldedItem() instanceof Weapon)
            &&(mob.fetchWieldedItem().fetchEffect(ID())==null))
                return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
        if(target==null) return false;
        if(!(target instanceof Weapon))
        {
            if(auto||mob.isMonster())
            {
                target=mob.fetchWieldedItem();
                if(target==null)
                    for(int i=0;i<mob.location().numItems();i++)
                    {
                        Item I2=mob.location().fetchItem(i);
                        if((I2!=null)&&(I2.container()==null)&&(I2 instanceof Weapon))
                        { target=I2; break;}
                    }
            }
            if(!(target instanceof Weapon))
            {
                mob.tell("You can only enflame weapons.");
                return false;
            }
        }
        
		if(((Weapon)target).fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already enflamed.");
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> appear(s) surrounded by flames!":"^S<S-NAME> hold(s) <T-NAMESELF> and "+prayWord(mob)+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,("<T-NAME> is engulfed in flames!")+CMProps.msp("fireball.wav",10));
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
