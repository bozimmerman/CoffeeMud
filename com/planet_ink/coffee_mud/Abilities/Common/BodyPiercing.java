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
   Copyright 2004-2025 Bo Zimmerman

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
public class BodyPiercing extends CommonSkill implements RecipeDriven
{
	@Override
	public String ID()
	{
		return "BodyPiercing";
	}

	private final static String	localizedName	= CMLib.lang().L("Body Piercing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BODYPIERCE", "BODYPIERCING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_TORTURING;
	}

	protected String	writing	= "";
	protected MOB		target	= null;
	protected int		oldHP	= 1;
	protected String	bodyPart= "";

	public BodyPiercing()
	{
		super();
		displayText=L("You are piercing...");
		verb=L("piercing");
	}

	@Override
	public List<List<String>> fetchRecipes()
	{
		return loadRecipes(getRecipeFilename());
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"WEAR_LOC\tITEM_LEVEL\tBODY_PART_DESC\tBODY_PART_DESC\tBODY_PART_DESC\tBODY_PART_DESC";
	}

	@Override
	public String getRecipeFilename()
	{
		return "bodypierce.txt";
	}

	@Override
	public List<String> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		final List<String> matches = new Vector<String>();
		for(final List<String> list : fetchRecipes())
		{
			String name;
			for(int i=RecipeDriven.RCP_LEVEL+1;i<list.size();i++)
			{
				name=list.get(i);
				if(name.equalsIgnoreCase(recipeName)
				||(beLoose && (name.toUpperCase().indexOf(recipeName.toUpperCase())>=0)))
					matches.add(name);
			}
		}
		return matches;
	}

	@Override
	public Pair<String, Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return new Pair<String,Integer>(recipe.get( RecipeDriven.RCP_LEVEL + 1 ),
				Integer.valueOf(CMath.s_int(recipe.get( RecipeDriven.RCP_LEVEL ))));
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(!aborted)
			&&(!helping)
			&&(target!=null))
			{
				final MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,L("<S-NAME> mess(es) up the piercing on @x1.",target.name(mob)));
				else
				{
					commonEmote(mob,L("<S-NAME> complete(s) the piercing on @x1.",target.name(mob)));
					target.addTattoo(writing);
				}
				if((bodyPart!=null)
				&&(bodyPart.length()>0))
				{
					Ability injuryA=CMClass.getAbility("Injury");
					if(injuryA!=null)
					{
						injuryA.invoke(mob,new XVector<String>(),target,true,0);
						injuryA=target.fetchEffect("Injury");
						if(injuryA!=null)
						{
							((LimbDamage)injuryA).damageLimb(bodyPart, true);
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if((commands.size()<2)&&(!CMParms.combine(commands, 0).equalsIgnoreCase("list")))
		{
			commonTelL(mob,"You must specify remove and/or whom you want to pierce, and what body part to pierce.");
			return false;
		}
		String name=commands.get(0);
		String part=CMParms.combine(commands,1);
		if(name.equalsIgnoreCase("list"))
			part=name;
		String command="";
		if(commands.size()>2)
		{
			if((commands.get(0)).equalsIgnoreCase("REMOVE"))
			{
				command=(commands.get(0)).toUpperCase();
				name=commands.get(1);
				part=CMParms.combine(commands,2);
			}
		}

		MOB target=null;
		if(!name.equalsIgnoreCase("list"))
		{
			target=super.getTarget(mob,new XVector<String>(name),givenTarget);
			if(target==null)
				return false;
			if((!auto)&&(CMLib.flags().isSleeping(target)))
				CMLib.commands().postStand(target, true, true);
			if((target.isMonster())
			&&(CMLib.flags().isAliveAwakeMobile(target,true))
			&&(!mob.getGroupMembers(new HashSet<MOB>()).contains(target)))
			{
				commonFaiL(mob,commands,"@x1 doesn't want any piercings.",target.Name());
				return false;
			}
		}

		int partNum=-1;
		final StringBuffer allParts=new StringBuffer("");
		String fullPartName=null;
		final Wearable.CODES codes = Wearable.CODES.instance();
		final List<List<String>> recipes = CMLib.utensils().addExtRecipes(mob,ID(),fetchRecipes());
		String wearLocName=null;
		for(final List<String> recipe : recipes)
		{
			final int lvl = CMath.s_int(recipe.get(RecipeDriven.RCP_LEVEL));
			if(lvl <= adjustedLevel(mob,asLevel))
			{
				for(int i=RecipeDriven.RCP_LEVEL+1;i<recipe.size();i++)
				{
					final String l = recipe.get(i);
					if(l.trim().length()>0)
					{
						if(l.toLowerCase().startsWith(part.toLowerCase()))
						{
							partNum=codes.findDex_ignoreCase(recipe.get(RecipeDriven.RCP_FINALNAME));
							if(partNum >= 0)
							{
								fullPartName=l.toLowerCase();
								wearLocName=codes.name(partNum).toUpperCase();
							}
						}
						allParts.append(", "+CMStrings.capitalizeAndLower(l));
					}
				}
			}
		}
		if(part.equalsIgnoreCase("list"))
		{
			commonTelL(mob,"You must specify remove and/or whom you want to pierce, and what body part to pierce.");
			commonTelL(mob,"Valid locations include: @x2",part,allParts.toString().substring(2));
			return false;
		}
		else
		if((partNum<0)||(wearLocName==null))
		{
			commonFaiL(mob,commands,"'@x1' is not a valid location.  Valid locations include: @x2",part,allParts.toString().substring(2));
			return false;
		}
		final long wornCode=codes.get(partNum);
		final String wornName=fullPartName;

		if(target == null)
			return false;

		if((target.getWearPositions(wornCode)<=0)
		||(target.freeWearPositions(wornCode,(short)(Short.MIN_VALUE+1),(short)0)<=0))
		{
			commonFaiL(mob,commands,"That location is not available for piercing. Make sure no clothing is being worn there.");
			return false;
		}
		if(target.curState().getHitPoints() < target.maxState().getHitPoints())
		{
			commonFaiL(mob,commands,"You need to wait until @x1 is at full health.",target.name(mob));
			return false;
		}

		int numTattsDone=0;
		for(final Enumeration<Tattoo> e=target.tattoos();e.hasMoreElements();)
		{
			final Tattoo T=e.nextElement();
			if(T.getTattooName().startsWith(wearLocName+":"))
				numTattsDone++;
		}
		if("REMOVE".equals(command))
		{
			if(numTattsDone<=0)
			{
				commonFaiL(mob,commands,"There is no piercing there to heal.");
				return false;
			}
		}
		else
		if(numTattsDone>=target.getWearPositions(codes.get(partNum)))
		{
			commonFaiL(mob,commands,"That location is already decorated.");
			return false;
		}

		if("REMOVE".equals(command) && (wornName != null))
		{
			if(wornName.toLowerCase().endsWith("s"))
			{
				if(target.findTattoo(wearLocName+":Pierced "+wornName.toLowerCase())==null)
				{
					commonFaiL(mob,commands,"There is no piercing there to heal.  Did you use the full body part name?");
					return false;
				}
			}
			else
			if(target.findTattoo(wearLocName+":A pierced "+wornName.toLowerCase())==null)
			{
				commonFaiL(mob,commands,"There is no piercing there to heal.  Did you use the full body part name?");
				return false;
			}
		}

		if((!super.invoke(mob,commands,givenTarget,auto,asLevel))||(wornName==null))
			return false;
		if(wornName.toLowerCase().endsWith("s"))
		{
			writing=wearLocName+":Pierced "+wornName.toLowerCase();
			verb=L("piercing @x1 on @x2",target.name(),wornName);
		}
		else
		{
			writing=wearLocName+":A pierced "+wornName.toLowerCase();
			verb=L("piercing @x1 on the @x2",target.name(),wornName);
		}
		displayText=L("You are @x1",verb);
		if(!proficiencyCheck(mob,0,auto))
			writing="";
		final int duration=getDuration(30,mob,1,6);
		String msgStr=L("<S-NAME> start(s) piercing <T-NAMESELF> on the @x1.",wornName.toLowerCase());
		if("REMOVE".equals(command))
		{
			if(writing.length()==0)
				msgStr=L("<S-NAME> fail(s) to heal the piercing on <T-YOUPOSS> @x1.",wornName.toLowerCase());
			else
				msgStr=L("<S-NAME> heal(s) the piercing on <T-YOUPOSS> @x1.",wornName.toLowerCase());
		}
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),msgStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			final int percentOff=target.maxState().getHitPoints()/8;
			if(target.curState().getHitPoints() > (percentOff*2))
			{
				CMLib.combat().postDamage(mob, target, this, percentOff, CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE, Weapon.TYPE_PIERCING, null);
				CMLib.combat().postDamage(mob, target, this, percentOff, CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE, Weapon.TYPE_PIERCING, null);
			}
			if("REMOVE".equals(command))
			{
				target.delTattoo(target.findTattoo(writing));
			}
			else
			{
				final List<Integer> bodyPartNums = new ArrayList<Integer>();
				for(int i=0;i<Race.BODY_WEARVECTOR.length;i++)
				{
					if((Race.BODY_WEARVECTOR[i] == wornCode)
					&&(target.charStats().getBodyPart(i)>0)
					&&(!bodyPartNums.contains(Integer.valueOf(i))))
						bodyPartNums.add(Integer.valueOf(i));
				}
				String bodyPartName="";
				if(bodyPartNums.size()>0)
				{
					final Integer pNum=bodyPartNums.get(CMLib.dice().roll(1, bodyPartNums.size(), -1));
					bodyPartName=Race.BODYPARTSTR[pNum.intValue()].toLowerCase();
				}
				beneficialAffect(mob,mob,asLevel,duration);
				final BodyPiercing A=(BodyPiercing)mob.fetchEffect(ID());
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

