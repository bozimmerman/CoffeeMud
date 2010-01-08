package com.planet_ink.coffee_mud.Abilities.Common;
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
public class Scalp extends CommonSkill
{
	public String ID() { return "Scalp"; }
	public String name(){ return "Scalping";}
	private static final String[] triggerStrings = {"SCALP","SCALPING"};
	public String[] triggerStrings(){return triggerStrings;}
	public static Vector lastSoManyScalps=new Vector();
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ANATOMY;}

	private DeadBody body=null;
	protected boolean failed=false;
	public Scalp()
	{
		super();
		displayText="You are scalping something...";
		verb="scalping";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((body!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&((!((MOB)affected).location().isContent(body)))
		&&((!((MOB)affected).isMine(body))))
			unInvoke();
		return super.tick(ticking,tickID);
	}
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((body!=null)&&(!aborted))
				{
					if((failed)||(!mob.location().isContent(body)))
						commonTell(mob,"You messed up your scalping completely.");
					else
					{
						mob.location().show(mob,null,body,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to scalp <O-NAME>.");
						lastSoManyScalps.addElement(body);
						if(lastSoManyScalps.size()>100)
							lastSoManyScalps.removeElementAt(0);
						Item scalp=CMClass.getItem("GenItem");
						String race="";
						if((body.charStats()!=null)&&(body.charStats().getMyRace()!=null))
							race=" "+body.charStats().getMyRace().name();
						if(body.name().startsWith("the body"))
							scalp.setName("the"+race+" scalp"+body.name().substring(8));
						else
							scalp.setName("a"+race+" scalp");
						if(body.displayText().startsWith("the body"))
							scalp.setDisplayText("the"+race+" scalp"+body.displayText().substring(8));
						else
							scalp.setDisplayText("a"+race+" scalp sits here");
						scalp.setBaseValue(1);
						scalp.setDescription("This is the bloody top of that poor creatures head.");
						scalp.setMaterial(RawMaterial.RESOURCE_MEAT);
						scalp.setSecretIdentity("This scalp was cut by "+mob.name()+".");
						mob.location().addItemRefuse(scalp,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		body=null;
		Item I=null;
		if((mob.isMonster()
		&&(!CMLib.flags().isAnimalIntelligence(mob)))
		&&(commands.size()==0))
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I2=mob.location().fetchItem(i);
				if((I2!=null)
				&&(I2 instanceof DeadBody)
				&&(CMLib.flags().canBeSeenBy(I2,mob))
				&&(I2.container()==null))
				{
					I=I2;
					break;
				}
			}
		}
		else
			I=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);

		if(I==null) return false;
		if((!(I instanceof DeadBody))
		   ||(((DeadBody)I).charStats()==null)
		   ||(((DeadBody)I).charStats().getMyRace()==null)
		   ||(((DeadBody)I).charStats().getMyRace().bodyMask()[Race.BODY_HEAD]==0))
		{
			commonTell(mob,"You can't scalp "+I.name()+".");
			return false;
		}
		if(lastSoManyScalps.contains(I))
		{
			commonTell(mob,I.name()+" has already been scalped.");
			return false;

		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		failed=!proficiencyCheck(mob,0,auto);
		CMMsg msg=CMClass.getMsg(mob,I,this,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) scalping <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			I=(Item)msg.target();
			body=(DeadBody)I;
			verb="scalping "+I.name();
            playSound="ripping.wav";
			int duration=(I.envStats().weight()/(10+getXLEVELLevel(mob)));
			if(duration<3) duration=3;
			if(duration>40) duration=40;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
