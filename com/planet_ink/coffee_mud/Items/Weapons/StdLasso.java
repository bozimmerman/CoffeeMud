package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;

public class StdLasso extends StdWeapon
{
	public String ID(){	return "StdLasso";}
	public StdLasso()
	{
		super();
		name="a lasso";
		displayText="a lasso has been left here.";
		miscText="";
		description="Its a rope with a big stiff loop on one end!";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(1);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(0);
		baseGoldValue=10;
		recoverEnvStats();
		minRange=1;
		maxRange=1;
		weaponType=Weapon.TYPE_NATURAL;
		material=EnvResource.RESOURCE_HEMP;
		weaponClassification=Weapon.CLASS_THROWN;
		setRawLogicalAnd(true);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		if((affect.tool()==this)
		&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
			return;
			//affect.addTrailerMsg(new FullMsg(affect.source(),this,Affect.MSG_DROP,null));
		else
		if((affect.tool()==this)
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		&&(affect.target() !=null)
		&&(affect.target() instanceof MOB)
		&&(weaponClassification()==Weapon.CLASS_THROWN))
		{
			unWear();
			affect.addTrailerMsg(new FullMsg(affect.source(),this,Affect.MASK_GENERAL|Affect.MSG_DROP,null));
			affect.addTrailerMsg(new FullMsg((MOB)affect.target(),this,Affect.MASK_GENERAL|Affect.MSG_GET,null));
			affect.addTrailerMsg(new FullMsg(affect.source(),(MOB)affect.target(),this,Affect.MASK_GENERAL|Affect.TYP_GENERAL,null));
		}
		else
		if((affect.tool()==this)
		&&(affect.target()!=null)
		&&(affect.target() instanceof MOB)
		&&(affect.targetMinor()==Affect.TYP_GENERAL)
		&&(((MOB)affect.target()).isMine(this))
		&&(affect.sourceMessage()==null))
		{
			Ability A=CMClass.getAbility("Thief_Bind");
			if(A!=null)
			{
				A.setAffectedOne(this);
				A.invoke(affect.source(),affect.target(),true);
			}
		}
		else
			super.affect(myHost,affect);
	}
	
	public Environmental newInstance()
	{
		return new StdLasso();
	}
}
