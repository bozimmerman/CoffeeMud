package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class EternityQuarterstaff extends Quarterstaff
{
	public String ID(){	return "EternityQuarterstaff";}
	public EternityQuarterstaff()
	{
		super();

		setName("a quarterstaff");
		setDisplayText("a wooden quarterstaff lies on the ground.");
		setDescription("It\\`s long and wooden, just like a quarterstaff ought to be.");
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



	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		MOB mob=msg.source();
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_SPEAK:
			if((mob.isMine(this))
			   &&(!amWearingAt(Item.INVENTORY))
			   &&(msg.target() instanceof MOB)
			   &&(mob.location().isInhabitant((MOB)msg.target())))
			{
				MOB target=(MOB)msg.target();
				int x=msg.targetMessage().toUpperCase().indexOf("HEAL");
				if(x>=0)
				{
					if(usesRemaining()>0)
					{
						this.setUsesRemaining(this.usesRemaining()-5);
						FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSG_CAST_VERBAL_SPELL,"<S-NAME> point(s) <S-HIS-HER> quarterstaff at <T-NAMESELF>, and delivers a healing beam of light.");
						if(mob.location().okMessage(mob,msg2))
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
		super.executeMsg(myHost,msg);
	}
}
