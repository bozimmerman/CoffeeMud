package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class FlamingSword extends Longsword
{
	public String ID(){	return "FlamingSword";}
	public FlamingSword()
	{
		super();

		setName("a fancy longsword");
		setDisplayText("a fancy longsword has been dropped on the ground.");
		setDescription("A one-handed sword with a very slight red tinge on the blade.");
		secretIdentity="A Flaming Sword (Additional fire damage when you strike)";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_LIGHTSOURCE | EnvStats.IS_BONUS);
		baseGoldValue=2500;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;
	}

	public Environmental newInstance()
	{
		return new FlamingSword();
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.source().location()!=null)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_HURT))
		&&((msg.targetCode()-CMMsg.MASK_HURT)>0)
		&&(msg.tool()==this)
		&&(msg.target() instanceof MOB)
		&&(!((MOB)msg.target()).amDead()))
		{
			FullMsg msg2=new FullMsg(msg.source(),(MOB)msg.target(),new FlamingSword(),CMMsg.MSG_OK_ACTION,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_FIRE,CMMsg.MSG_NOISYMOVEMENT,null);
			if(msg.source().location().okMessage(msg.source(),msg2))
			{
				msg.source().location().send(msg.source(), msg2);
				if(!msg2.wasModified())
				{
					int flameDamage = (int) Math.round( Math.random() * 6 );
					flameDamage *= baseEnvStats().level();
					ExternalPlay.postDamage(msg.source(),(MOB)msg.target(),null,flameDamage,CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,name()+" <DAMAGE> <T-NAME>!");
				}
			}
		}
	}

}
