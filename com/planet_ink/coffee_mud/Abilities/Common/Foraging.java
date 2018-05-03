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
   Copyright 2002-2018 Bo Zimmerman

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

public class Foraging extends GatheringSkill
{
	@Override
	public String ID()
	{
		return "Foraging";
	}

	private final static String	localizedName	= CMLib.lang().L("Foraging");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "FORAGE", "FORAGING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_GATHERINGSKILL;
	}

	@Override
	protected boolean allowedWhileMounted()
	{
		return false;
	}

	@Override
	public String supportedResourceString()
	{
		return "VEGETATION|HEMP|SILK|COTTON";
	}

	protected Item		found			= null;
	protected String	foundShortName	= "";

	public Foraging()
	{
		super();
		displayText=L("You are foraging...");
		verb=L("foraging");
	}

	protected int getDuration(MOB mob, int level)
	{
		return getDuration(45,mob,level,10);
	}

	@Override
	protected int baseYield()
	{
		return 1;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
				{
					commonTell(mob,L("You have found some @x1!",foundShortName));
					displayText=L("You are foraging for @x1",foundShortName);
					verb=L("foraging for @x1",foundShortName);
				}
				else
				{
					final StringBuffer str=new StringBuffer(L("You can't seem to find anything worth foraging around here.\n\r"));
					final int d=lookingFor(RawMaterial.MATERIAL_VEGETATION,mob.location());
					if(d<0)
						str.append(L("You might try elsewhere."));
					else
						str.append(L("You might try @x1.",CMLib.directions().getInDirectionName(d)));
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
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
				if((found!=null)&&(!aborted)&&(mob.location()!=null))
				{
					final int amount=((found.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_CLOTH)?
							   (CMLib.dice().roll(1,10,0)*(baseYield()+abilityCode())):
							   (CMLib.dice().roll(1,3,0)*(baseYield()+abilityCode()));
					final CMMsg msg=CMClass.getMsg(mob,found,this,getCompletedActivityMessageType(),null);
					msg.setValue(amount);
					if(mob.location().okMessage(mob, msg))
					{
						String s="s";
						if(msg.value()==1)
							s="";
						msg.modify(L("<S-NAME> manage(s) to gather @x1 pound@x2 of @x3.",""+msg.value(),s,foundShortName));
						mob.location().send(mob, msg);
						for(int i=0;i<msg.value();i++)
						{
							final Item newFound=(Item)found.copyOf();
							if(!dropAWinner(mob,newFound))
								break;
							CMLib.commands().postGet(mob,null,newFound,true);
						}
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
		bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}

		verb=L("foraging");
		found=null;
		if((!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_VEGETATION,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_HEMP,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_SILK,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_SALT,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_COTTON,mob.location())))
		{
			commonTell(mob,L("You don't think this is a good place to forage."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final int resourceType=mob.location().myResource();
		if((proficiencyCheck(mob,0,auto))
		   &&(((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
			  ||(resourceType==RawMaterial.RESOURCE_HEMP)
			  ||(resourceType==RawMaterial.RESOURCE_SALT)
			  ||(resourceType==RawMaterial.RESOURCE_SILK)
			  ||(resourceType==RawMaterial.RESOURCE_COTTON)))
		{
			found=(Item)CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		}
		final int duration=getDuration(mob,1);
		final CMMsg msg=CMClass.getMsg(mob,found,this,getActivityMessageType(),L("<S-NAME> start(s) foraging."));
		if(mob.location().okMessage(mob,msg))
		{
			// herb/locale customisation for jeremy
			if((found!=null)
			&&(found.material()==RawMaterial.RESOURCE_HERBS)
			&&((found.Name().toUpperCase().endsWith(" HERBS"))
			   ||(found.Name().equalsIgnoreCase("herbs"))))
			{
				final Map<String,List<String>> H=Resources.getCachedMultiLists("skills/herbs.txt",false);
				if(H!=null)
				{
					final List<String> V=H.get(mob.location().ID());
					if((V!=null)&&(V.size()>0))
					{
						int total=0;
						for(int i=0;i<V.size();i++)
						{
							final String s=V.get(i);
							final int x=s.indexOf(' ');
							if((x>=0)&&(CMath.isNumber(s.substring(0,x).trim())))
								total+=CMath.s_int(s.substring(0,x).trim());
							else
								total+=10;
						}
						final int choice=CMLib.dice().roll(1,total,-1);
						total=0;
						for(int i=0;i<V.size();i++)
						{
							final String s=V.get(i);
							final int x=s.indexOf(' ');
							if((x>=0)&&(CMath.isNumber(s.substring(0,x).trim())))
							{
								total+=CMath.s_int(s.substring(0,x).trim());
								if(choice<=total)
								{
									found.setSecretIdentity(s.substring(x+1).trim());
									break;
								}
							}
							else
							{
								total+=10;
								if(choice<=total)
								{
									found.setSecretIdentity(s);
									break;
								}
							}
						}
					}
				}
			}
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
