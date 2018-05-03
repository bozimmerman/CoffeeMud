package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2005-2018 Bo Zimmerman

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

public class Allergies extends StdAbility implements HealthCondition
{
	@Override
	public String ID()
	{
		return "Allergies";
	}

	private final static String	localizedName	= CMLib.lang().L("Allergies");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected Set<Integer>	resourceAllergies	= new HashSet<Integer>();
	protected Set<Race>		raceAllergies		= new HashSet<Race>();
	protected int			allergicCheckDown	= 0;

	@Override
	public String getHealthConditionDesc()
	{
		final List<String> list=new ArrayList<String>();
		for(final Integer I : resourceAllergies)
			list.add(RawMaterial.CODES.NAME(I.intValue()).toLowerCase());
		for(final Race R : raceAllergies)
			list.add(R.name()+" dander");
		if(list.size()==0)
			return "";
		return "Suffers from allergies to "+CMLib.english().toEnglishStringList(list)+".";
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		resourceAllergies.clear();
		raceAllergies.clear();
		final Vector<String> V=CMParms.parse(newText.toUpperCase().trim());
		final RawMaterial.CODES codes = RawMaterial.CODES.instance();
		for(int s=0;s<codes.total();s++)
		{
			if(V.contains(codes.names()[s]))
				resourceAllergies.add(Integer.valueOf(codes.get(s)));
		}
		Race R=null;
		for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
		{
			R=r.nextElement();
			if(V.contains(R.ID().toUpperCase()))
				raceAllergies.add(R);
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(((++allergicCheckDown)>10)
		&&(affected instanceof MOB))
		{
			allergicCheckDown=0;
			final MOB mob=(MOB)affected;
			if((CMLib.flags().isAliveAwakeMobile(mob,true))&&(CMLib.flags().isInTheGame(mob,true)))
			{
				final Room R=CMLib.map().roomLocation(mob);
				if(raceAllergies.size()>0)
				{
					MOB M=null;
					for(int i=0;i<R.numInhabitants();i++)
					{
						M=R.fetchInhabitant(i);
						if((M!=null)&&(M!=mob)&&(raceAllergies.contains(M.charStats().getMyRace())))
							R.show(mob,null,this,CMMsg.TYP_NOISYMOVEMENT,L("<S-NAME> sneeze(s)! AAAAACHHHOOOO!"));
					}
				}
				else
				if(resourceAllergies.size()>0)
				{
					Item I=null;
					for(int i=0;i<R.numItems();i++)
					{
						I=R.getItem(i);
						if((I!=null)
						&&(I.container()==null)
						&&(resourceAllergies.contains(Integer.valueOf(I.material()))))
							R.show(mob,null,this,CMMsg.TYP_NOISYMOVEMENT,L("<S-NAME> sneeze(s)! AAAAACHHHOOOO!"));
					}
					if(R.numInhabitants()>0)
					{
						final MOB M=R.fetchRandomInhabitant();
						if(M!=null)
						for(int i=0;i<M.numItems();i++)
						{
							I=M.getItem(i);
							if((I!=null)
							&&(I.container()==null)
							&&(resourceAllergies.contains(Integer.valueOf(I.material()))))
								R.show(mob,null,this,CMMsg.TYP_NOISYMOVEMENT,L("<S-NAME> sneeze(s)! AAAAACHHHOOOO!"));
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB))
		{
			if(msg.source()==affected)
			{
				if((msg.targetMinor()==CMMsg.TYP_EAT)
				&&(((msg.target() instanceof Item)&&(resourceAllergies.contains(Integer.valueOf(((Item)msg.target()).material()))))
					||((msg.target() instanceof MOB)&&(raceAllergies.contains(((MOB)msg.target()).charStats().getMyRace())))))
				{
					final Ability A=CMClass.getAbility("Poison_Heartstopper");
					if(A!=null)
						A.invoke(msg.source(),msg.source(),true,0);
				}
				else
				if(((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL))
				&&((msg.target() instanceof Item)&&(resourceAllergies.contains(Integer.valueOf(((Item)msg.target()).material())))))
				{
					final Ability A=CMClass.getAbility("Poison_Hives");
					if(A!=null)
						A.invoke(msg.source(),msg.source(),true,0);
				}
			}
			else
			if((msg.target()==affected)
			&&(raceAllergies.contains(msg.source().charStats().getMyRace()))
			&&((msg.targetMajor(CMMsg.MASK_HANDS))
			   ||(msg.targetMajor(CMMsg.MASK_MOVE)))
			&&(((MOB)affected).location()!=null)
			&&(((MOB)affected).location().isInhabitant(msg.source()))
			&&((msg.tool()==null)||((!msg.tool().ID().equals("Poison_Hives"))&&(!msg.tool().ID().equals("Poison_Heartstopper")))))
			{
				final Ability A=CMClass.getAbility("Poison_Hives");
				if(A!=null)
					A.invoke(msg.source(),affected,true,0);
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String choice="";
		if(givenTarget!=null)
		{
			if((commands.size()>0)&&((commands.get(0)).equals(givenTarget.name())))
				commands.remove(0);
			choice=CMParms.combine(commands,0);
			commands.clear();
		}
		else
		if(commands.size()>1)
		{
			choice=CMParms.combine(commands,1);
			while(commands.size()>1)
				commands.remove(1);
		}
		final MOB target=getTarget(mob,commands,givenTarget);

		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final ArrayList<String> allChoices=new ArrayList<String>();
			for(final int code : RawMaterial.CODES.ALL())
			{
				if(((code&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LIQUID)
				&&((code&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ENERGY)
				&&((code&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GAS)
				&&(code!=RawMaterial.RESOURCE_COTTON)
				&&(code!=RawMaterial.RESOURCE_IRON)
				&&(code!=RawMaterial.RESOURCE_WOOD))
					allChoices.add(RawMaterial.CODES.NAME(code));
			}
			Race R=null;
			for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
			{
				R=r.nextElement();
				allChoices.add(R.ID().toUpperCase());
			}
			String allergies="";
			if((choice.length()>0)&&(allChoices.contains(choice.toUpperCase())))
				allergies=choice.toUpperCase();
			else
			for(int i=0;i<allChoices.size();i++)
			{
				if((CMLib.dice().roll(1,allChoices.size(),0)==1)
				&&(!(allChoices.get(i).equalsIgnoreCase(mob.charStats().getMyRace().ID().toUpperCase()))))
					allergies+=" "+allChoices.get(i);
			}
			if(allergies.length()==0)
				return false;

			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
			if(target.location()!=null)
			{
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					final Ability A=(Ability)copyOf();
					A.setMiscText(allergies.trim());
					target.addNonUninvokableEffect(A);
				}
			}
			else
			{
				final Ability A=(Ability)copyOf();
				A.setMiscText(allergies.trim());
				target.addNonUninvokableEffect(A);
			}
		}
		return success;
	}
}
