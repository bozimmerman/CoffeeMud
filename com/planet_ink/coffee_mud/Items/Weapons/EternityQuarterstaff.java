package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class EternityQuarterstaff extends Quarterstaff
{
	public String ID(){	return "EternityQuarterstaff";}
	public EternityQuarterstaff()
	{
		super();

		name="a quarterstaff";
		displayText="a wooden quarterstaff lies on the ground.";
		miscText="";
		description="It\\`s long and wooden, just like a quarterstaff ought to be.";
		secretIdentity="A quarterstaff fashioned from a branch of one of the Fox god's Eternity Trees.  A truely wondrous gift.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(20);
		baseEnvStats.setWeight(4);
		this.setUsesRemaining(50);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(12);
		baseGoldValue+=5000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		wornLogicalAnd=true;
		properWornBitmap=Item.HELD|Item.WIELD;
		material=EnvResource.RESOURCE_OAK;
		weaponType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
		recoverEnvStats();

	}

	public Environmental newInstance()
	{
		return new EternityQuarterstaff();
	}

	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		switch(affect.sourceMinor())
		{
		case Affect.TYP_SPEAK:
			if((mob.isMine(this))
			   &&(!amWearingAt(Item.INVENTORY))
			   &&(affect.target() instanceof MOB)
			   &&(mob.location().isInhabitant((MOB)affect.target())))
			{
				MOB target=(MOB)affect.target();
				int x=affect.targetMessage().toUpperCase().indexOf("HEAL");
				if(x>=0)
				{
					if(usesRemaining()>0)
					{
						this.setUsesRemaining(this.usesRemaining()-5);
						FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_CAST_VERBAL_SPELL,"<S-NAME> point(s) <S-HIS-HER> quarterstaff at <T-NAMESELF>, and delivers a healing beam of light.");
						if(mob.location().okAffect(msg))
						{
		   					int healing=1+(int)Math.round(Util.div(envStats().level(),10.0));
							target.curState().adjHitPoints(healing,target.maxState());
							target.tell("You feel a little better!");
							return;
						}

					}
				}
			}
			break;
		default:
			break;
		}
		super.affect(affect);
	}
}
