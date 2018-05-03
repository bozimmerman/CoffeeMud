package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Scalp extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Scalp";
	}

	private final static String	localizedName	= CMLib.lang().L("Scalping");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SCALP", "SCALPING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	public static Vector<DeadBody>	lastSoManyScalps	= new Vector<DeadBody>();

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ANATOMY;
	}

	private DeadBody	body	= null;
	protected boolean	failed	= false;

	public Scalp()
	{
		super();
		displayText=L("You are scalping something...");
		verb=L("scalping");
	}

	@Override
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

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((body!=null)&&(!aborted)&&(mob.location()!=null))
				{
					if((failed)||(!mob.location().isContent(body)))
						commonTell(mob,L("You messed up your scalping completely."));
					else
					{
						mob.location().show(mob,body,this,getCompletedActivityMessageType(),L("<S-NAME> manage(s) to scalp <T-NAME>."));
						lastSoManyScalps.addElement(body);
						if(lastSoManyScalps.size()>100)
							lastSoManyScalps.removeElementAt(0);
						final Item scalp=CMClass.getItem("GenItem");
						String race="";
						if((body.charStats()!=null)&&(body.charStats().getMyRace()!=null))
							race=" "+body.charStats().getMyRace().name();
						if(body.name().startsWith("the body"))
							scalp.setName(L("the@x1 scalp@x2",race,body.name().substring(8)));
						else
							scalp.setName(L("a@x1 scalp",race));
						if(body.displayText().startsWith("the body"))
							scalp.setDisplayText(L("the@x1 scalp@x2",race,body.displayText().substring(8)));
						else
							scalp.setDisplayText(L("a@x1 scalp sits here",race));
						scalp.setBaseValue(1);
						scalp.setDescription(L("This is the bloody top of that poor creatures head."));
						scalp.setMaterial(RawMaterial.RESOURCE_MEAT);
						scalp.setSecretIdentity("This scalp was cut by "+mob.name()+".");
						dropAWinner(mob,scalp);
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		body=null;
		Item I=null;
		if((mob.isMonster()
		&&(!CMLib.flags().isAnimalIntelligence(mob)))
		&&(commands.size()==0))
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				final Item I2=mob.location().getItem(i);
				if((I2 instanceof DeadBody)
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

		if(I==null)
			return false;
		if((!(I instanceof DeadBody))
		   ||(((DeadBody)I).charStats()==null)
		   ||(((DeadBody)I).charStats().getMyRace()==null)
		   ||(((DeadBody)I).charStats().getMyRace().bodyMask()[Race.BODY_HEAD]==0))
		{
			commonTell(mob,L("You can't scalp @x1.",I.name(mob)));
			return false;
		}
		if(lastSoManyScalps.contains(I))
		{
			commonTell(mob,L("@x1 has already been scalped.",I.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		failed=!proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,I,this,getActivityMessageType(),getActivityMessageType(),getActivityMessageType(),L("<S-NAME> start(s) scalping <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			I=(Item)msg.target();
			body=(DeadBody)I;
			verb=L("scalping @x1",I.name());
			playSound="ripping.wav";
			int duration=(I.phyStats().weight()/(10+getXLEVELLevel(mob)));
			if(duration<3)
				duration=3;
			if(duration>40)
				duration=40;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
