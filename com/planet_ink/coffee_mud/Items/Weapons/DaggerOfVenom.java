package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Abilities.*;


public class DaggerOfVenom extends Dagger
{
	public DaggerOfVenom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a small dagger";
		displayText="a sharp little dagger lies here.";
		miscText="";
		description="It has a wooden handle and a metal blade.";
        secretIdentity="A Dagger of Venom (Periodically injects poison on a successful hit.)";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats.setWeight(1);
		baseGoldValue=1500;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(4);
        baseEnvStats().setDisposition(Sense.IS_BONUS);
		weaponType=Weapon.TYPE_PIERCING;
		weaponClassification=Weapon.CLASS_EDGED;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new DaggerOfVenom();
	}
	public void strike(MOB source, MOB target, boolean success)
	{
		super.strike(source, target, success);
		if(success)
		{
            int chance = ((int) Math.random() * 20);
            if(chance == 10)
            {
                Poison poison = new Poison();
                poison.baseEnvStats().setLevel(baseEnvStats().level());
                poison.invoke(source, target, true);
            }
		}
	}

}
