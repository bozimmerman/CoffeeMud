package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class DaggerOfVenom extends Dagger
{
	public String ID(){	return "DaggerOfVenom";}
	public DaggerOfVenom()
	{
		super();

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
		material=EnvResource.RESOURCE_STEEL;
        baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		weaponType=Weapon.TYPE_PIERCING;
		weaponClassification=Weapon.CLASS_DAGGER;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new DaggerOfVenom();
	}
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if((affect.source().location()!=null)
		   &&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		   &&((affect.targetCode()-Affect.MASK_HURT)>0)
		   &&(affect.tool()==this)
		   &&(affect.target() instanceof MOB))
		{
            int chance = ((int) Math.random() * 20);
            if(chance == 10)
            {
                Ability poison = CMClass.getAbility("Poison");
	            poison.invoke(affect.source(),(MOB)affect.target(), true);
            }
		}
	}

}
