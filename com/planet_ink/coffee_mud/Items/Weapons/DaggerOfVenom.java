package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
        baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		weaponType=Weapon.TYPE_PIERCING;
		weaponClassification=Weapon.CLASS_DAGGER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new DaggerOfVenom();
	}
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affect.source().location()!=null)
		   &&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		   &&(affect.tool()==this)
		   &&(affect.target() instanceof MOB))
		{
            int chance = ((int) Math.random() * 20);
            if(chance == 10)
            {
                Ability poison = CMClass.getAbility("Poison");
                poison.baseEnvStats().setLevel(baseEnvStats().level());
                poison.invoke(affect.source(),(MOB)affect.target(), true);
            }
		}
	}

}
