package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.commands.*;

public class FlamingSword extends Longsword
{
	public FlamingSword()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a fancy longsword";
		displayText="a fancy longsword has been dropped on the ground.";
		miscText="";
		description="A one-handed sword with a very slight red tinge on the blade.";
		secretIdentity="A Flaming Sword (Additional fire damage when you strike)";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		baseEnvStats().setDisposition(Sense.IS_LIGHT | Sense.IS_BONUS);
		baseGoldValue=2500;
		recoverEnvStats();
		weaponType=TYPE_SLASHING;
	}

	public Environmental newInstance()
	{
		return new FlamingSword();
	}
	public void strike(MOB source, MOB target, boolean success)
	{
		super.strike(source, target, success);
		if(success)
		{
			FullMsg msg=new FullMsg(source,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_FIRE,Affect.SOUND_MAGIC,"Flames from the sword wielded by <MYAME>'s burn <T-NAME>!");
			if(target.okAffect(msg))
			{
				source.location().send(source, msg);
				if(!msg.wasModified())
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= baseEnvStats().level();
					TheFight.doDamage(target, ++flameDamage);
				}
			}
		}
	}

}
