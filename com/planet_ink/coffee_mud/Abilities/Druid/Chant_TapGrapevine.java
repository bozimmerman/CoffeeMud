package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Chant_TapGrapevine extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_TapGrapevine";
	}

	private final static String localizedName = CMLib.lang().L("Tap Grapevine");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS|Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected List<Ability> myChants=new Vector<Ability>();

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof Item)
		&&(((Item)affected).owner() instanceof Room)
		&&(((Room)((Item)affected).owner()).isContent((Item)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(invoker!=null)
		&&(invoker.location()!=((Room)((Item)affected).owner()))
		&&(msg.othersMessage()!=null))
			invoker.executeMsg(invoker,msg);
	}

	@Override
	public CMObject copyOf()
	{
		final Chant_TapGrapevine obj=(Chant_TapGrapevine)super.copyOf();
		obj.myChants=new Vector<Ability>();
		obj.myChants.addAll(myChants);
		return obj;
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(myChants!=null)&&(super.canBeUninvoked()))
		{
			final List<Ability> V=myChants;
			myChants=null;
			for(int i=0;i<V.size();i++)
			{
				final Ability A=V.get(i);
				if((A.affecting()!=null)
				   &&(A.ID().equals(ID()))
				   &&(A.affecting() instanceof Item))
				{
					final Item I=(Item)A.affecting();
					I.delEffect(A);
				}
			}
		}
		super.unInvoke();
	}

	public static Ability isPlant(Item I)
	{
		if((I!=null)&&(I.rawSecretIdentity().length()>0))
		{
			for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)
				&&(A.invoker()!=null)
				&&(A instanceof Chant_SummonPlants))
					return A;
			}
		}
		return null;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.fetchEffect(ID())!=null)||(mob.fetchEffect("Chant_Grapevine")!=null))
		{
			mob.tell(L("You are already listening through a grapevine."));
			return false;
		}
		MOB tapped=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I!=null)&&(isPlant(I)!=null))
			{
				final Ability A=isPlant(I);
				if((A!=null)&&(A.invoker()!=mob))
					tapped=A.invoker();
			}
		}

		final List<Room> myRooms=(tapped==null)?null:Druid_MyPlants.myPlantRooms(tapped);
		if((myRooms==null)||(myRooms.size()==0))
		{
			mob.tell(L("There doesn't appear to be any plants around here to listen through."));
			return false;
		}
		Item myPlant=Druid_MyPlants.myPlant(mob.location(),tapped,0);
		if((!auto)&&(myPlant==null))
		{
			mob.tell(L("You must be in the same room as someone elses plants to initiate this chant."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,myPlant,this,verbalCastCode(mob,myPlant,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF> and listen(s) carefully to <T-HIM-HER>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				myChants=new Vector<Ability>();
				beneficialAffect(mob,mob,asLevel,0);
				final Chant_TapGrapevine C=(Chant_TapGrapevine)mob.fetchEffect(ID());
				if(C==null)
					return false;
				for(int i=0;i<myRooms.size();i++)
				{
					final Room R=myRooms.get(i);
					int ii=0;
					myPlant=Druid_MyPlants.myPlant(R,tapped,ii);
					while(myPlant!=null)
					{
						Ability A=myPlant.fetchEffect(ID());
						if(A!=null)
							myPlant.delEffect(A);
						myPlant.addNonUninvokableEffect((Ability)C.copyOf());
						A=myPlant.fetchEffect(ID());
						if(A!=null)
							myChants.add(A);
						ii++;
						myPlant=Druid_MyPlants.myPlant(R,tapped,ii);
					}
				}
				C.myChants=new XVector<Ability>(myChants);
				myChants=new Vector<Ability>();
			}

		}
		else
			beneficialVisualFizzle(mob,myPlant,L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
