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

public class Chant_SensePlants extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SensePlants";
	}

	private final static String	localizedName	= CMLib.lang().L("Sense Plants");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Sensing Plants)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTCONTROL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	Room	lastRoom	= null;

	protected String word()
	{
		return "plants";
	}

	private final int[]	myMats	= { RawMaterial.MATERIAL_VEGETATION, RawMaterial.MATERIAL_WOODEN };

	protected int[] okMaterials()
	{
		return myMats;
	}

	private final int[]	myRscs	= { RawMaterial.RESOURCE_COTTON, RawMaterial.RESOURCE_HEMP };

	protected int[] okResources()
	{
		return myRscs;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(L("Your senses are no longer sensitive to @x1.",word()));
	}

	public String itsHere(MOB mob, Room R)
	{
		if(R==null)
			return "";
		if((okMaterials()!=null)&&(okMaterials().length>0))
		{
			for(int m=0;m<okMaterials().length;m++)
			{
				if((R.myResource()&RawMaterial.MATERIAL_MASK)==okMaterials()[m])
					return L("You sense @x1 here.", RawMaterial.CODES.NAME(R.myResource()).toLowerCase());
			}
		}
		if((okResources()!=null)&&(okResources().length>0))
		{
			for(int m=0;m<okResources().length;m++)
			{
				if(R.myResource()==okResources()[m])
					return L("You sense @x1 here.",RawMaterial.CODES.NAME(R.myResource()).toLowerCase());
			}
		}
		return "";
	}

	public void messageTo(MOB mob)
	{
		final String here=itsHere(mob,mob.location());
		if(here.length()>0)
			mob.tell(here);
		else
		{
			String last="";
			String dirs="";
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Room R=mob.location().getRoomInDir(d);
				final Exit E=mob.location().getExitInDir(d);
				if((R!=null)&&(E!=null)&&(itsHere(mob,R).length()>0))
				{
					if(last.length()>0)
						dirs+=", "+last;
					last=CMLib.directions().getFromCompassDirectionName(d);
				}
			}
			if((dirs.length()==0)&&(last.length()>0))
				mob.tell(L("You sense @x1 to @x2.",word(),last));
			else
			if((dirs.length()>2)&&(last.length()>0))
				mob.tell(L("You sense @x1 to @x2, and @x3.",word(),dirs.substring(2),last));
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		   &&(affected instanceof MOB)
		   &&(((MOB)affected).location()!=null)
		   &&((lastRoom==null)||(((MOB)affected).location()!=lastRoom)))
		{
			lastRoom=((MOB)affected).location();
			messageTo((MOB)affected);
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already sensing @x1.",word()));
			return false;
		}
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> gain(s) sensitivity to @x1!",word()):L("^S<S-NAME> chant(s) and gain(s) sensitivity to @x1!^?",word()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s), but nothing happens."));

		return success;
	}
}
