package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

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
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_LIGHT | EnvStats.IS_BONUS);
		baseGoldValue=2500;
		recoverEnvStats();
		weaponType=TYPE_SLASHING;
	}

	public Environmental newInstance()
	{
		return new FlamingSword();
	}
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affect.source().location()!=null)
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.tool()==this)
		&&(affect.target() instanceof MOB)
		&&(!((MOB)affect.target()).amDead()))
		{
			FullMsg msg=new FullMsg(affect.source(),(MOB)affect.target(),new FlamingSword(),Affect.MSG_OK_ACTION,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_FIRE,Affect.MSG_NOISYMOVEMENT,null);
			if(affect.source().location().okAffect(msg))
			{
				affect.source().location().send(affect.source(), msg);
				if(!msg.wasModified())
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= baseEnvStats().level();
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affect.target(),Affect.MSG_OK_ACTION,name()+" "+ExternalPlay.standardHitWord(Weapon.TYPE_BURNING,flameDamage)+" <T-NAME>!"));
					affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affect.target(),null,Affect.NO_EFFECT,Affect.MASK_HURT+flameDamage,Affect.NO_EFFECT,null));
				}
			}
		}
	}

}
