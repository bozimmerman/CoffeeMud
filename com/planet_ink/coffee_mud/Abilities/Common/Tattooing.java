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

public class Tattooing extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Tattooing";
	}

	private final static String	localizedName	= CMLib.lang().L("Tattooing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "TATTOO", "TATTOOING" });

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected String	writing	= "";
	protected MOB		target	= null;
	protected int		oldHP	= 1;
	protected String	bodyPart= "";

	public Tattooing()
	{
		super();
		displayText=L("You are tattooing...");
		verb=L("tattooing");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping)&&(target!=null))
			{
				final MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,L("<S-NAME> mess(es) up the tattoo on @x1.",target.name()));
				else
				{
					commonEmote(mob,L("<S-NAME> complete(s) the tattoo on @x1.",target.name()));
					target.addTattoo(writing);
				}
				if((bodyPart!=null)&&(bodyPart.length()>0))
				{
					Ability injuryA=CMClass.getAbility("Injury");
					if(injuryA!=null)
					{
						injuryA.invoke(mob,new XVector<String>(),target,true,0);
						injuryA=target.fetchEffect("Injury");
						if(injuryA!=null)
						{
							((LimbDamage)injuryA).damageLimb(bodyPart);
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((target==null)
			||(mob.location()!=target.location())
			||(!CMLib.flags().canBeSeenBy(target,mob)))
			{
				aborted = true;
				unInvoke();
				return false;
			}
			else
			if(target!=null)
				target.curState().setHitPoints(oldHP);
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<3)
		{
			commonTell(mob,L("You must specify whom you want to tattoo, what body part to tattoo, and what the tattoo looks like. Use 'REMOVE' as the description to remove a tattoo."));
			return false;
		}
		final String whom=commands.get(0);
		commands.remove(0);
		final String part=commands.get(0);
		commands.remove(0);
		final String message=CMParms.combine(commands,0);
		commands.clear();
		commands.add(whom);

		int partNum=-1;
		final StringBuffer allParts=new StringBuffer("");
		final long[] tattoable={Wearable.WORN_ARMS,
								Wearable.WORN_LEGS,
								Wearable.WORN_HANDS,
								Wearable.WORN_HEAD,
								Wearable.WORN_FEET,
								Wearable.WORN_LEFT_WRIST,
								Wearable.WORN_RIGHT_WRIST,
								Wearable.WORN_NECK,
								Wearable.WORN_BACK,
								Wearable.WORN_TORSO};
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int i=0;i<codes.total();i++)
		{
			for (final long element : tattoable)
			{
				if(codes.get(i)==element)
				{
					if(codes.name(i).equalsIgnoreCase(part))
						partNum=i;
					allParts.append(", "+CMStrings.capitalizeAndLower(codes.name(i).toLowerCase()));
					break;
				}
			}
		}
		if(partNum<0)
		{
			commonTell(mob,L("'@x1' is not a valid location.  Valid locations include: @x2",part,allParts.toString().substring(2)));
			return false;
		}
		final long wornCode=codes.get(partNum);
		final String wornName=codes.name(partNum);

		final MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.getWearPositions(wornCode)<=0)
		{
			commonTell(mob,L("That location is not available for tattooing."));
			return false;
		}
		if(target.freeWearPositions(wornCode,(short)(Short.MIN_VALUE+1),(short)0)<=0)
		{
			commonTell(mob,L("That location is currently covered by something."));
			return false;
		}
		if(target.curState().getHitPoints() < target.maxState().getHitPoints())
		{
			commonTell(mob,L("You need to wait until @x1 is at full health.",target.name(mob)));
			return false;
		}

		int numTattsDone=0;
		Tattoo tatToRemove=null;
		for(final Enumeration<Tattoo> e=target.tattoos();e.hasMoreElements();)
		{
			final Tattoo T=e.nextElement();
			if(T.getTattooName().startsWith(wornName.toUpperCase()+":"))
			{
				numTattsDone++;
				if(T.getTattooName().substring(wornName.length()+1).toUpperCase().startsWith("A TATTOO OF"))
					tatToRemove=T;
			}
		}
		if("REMOVE".startsWith(message.toUpperCase()))
		{
			if(tatToRemove==null)
			{
				commonTell(mob,L("There is no tattoo there to remove."));
				return false;
			}
		}
		else
		if(numTattsDone>=target.getWearPositions(codes.get(partNum)))
		{
			commonTell(mob,L("That location is already completely decorated."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		writing=wornName.toUpperCase()+":A tattoo of "+message;
		verb=L("tattooing @x1 on the @x2",target.name(),wornName);
		displayText=L("You are @x1",verb);
		if(!proficiencyCheck(mob,0,auto))
			writing="";
		final int duration=getDuration(35,mob,1,6);
		String str=L("<S-NAME> start(s) tattooing @x1 on <T-YOUPOSS> @x2.",message,wornName.toLowerCase());
		if("REMOVE".startsWith(message.toUpperCase()))
			str=L("<S-NAME> remove(s) the tattoo on <T-YOUPOSS> @x1.",wornName.toLowerCase());

		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),str);
		if(mob.location().okMessage(mob,msg))
		{
			final int percentOff=target.maxState().getHitPoints()/8;
			if(target.curState().getHitPoints() > (percentOff*2))
			{
				CMLib.combat().postDamage(mob, target, this, percentOff, CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE, Weapon.TYPE_PIERCING, null);
				CMLib.combat().postDamage(mob, target, this, percentOff, CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE, Weapon.TYPE_PIERCING, null);
			}
			mob.location().send(mob,msg);
			if("REMOVE".startsWith(message.toUpperCase()))
				target.delTattoo(tatToRemove);
			else
			{
				//target.curState().adjHitPoints(-percentOff, target.maxState());
				List<Integer> bodyPartNums = new ArrayList<Integer>();
				for(int i=0;i<Race.BODY_WEARVECTOR.length;i++)
				{
					if((Race.BODY_WEARVECTOR[i] == wornCode)
					&&(!bodyPartNums.contains(Integer.valueOf(i))))
						bodyPartNums.add(Integer.valueOf(i));
				}
				String bodyPartName="";
				if(bodyPartNums.size()>0)
				{
					Integer pNum=bodyPartNums.get(CMLib.dice().roll(1, bodyPartNums.size(), -1));
					bodyPartName=Race.BODYPARTSTR[pNum.intValue()].toLowerCase();
				}
				beneficialAffect(mob,mob,asLevel,duration);
				final Tattooing A=(Tattooing)mob.fetchEffect(ID());
				if(A!=null)
				{
					A.target=target;
					A.oldHP=target.curState().getHitPoints();
					A.bodyPart=bodyPartName;
				}
			}
		}
		return true;
	}
}
