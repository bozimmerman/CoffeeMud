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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class Chant_GrowFood extends Chant
{
	@Override public String ID() { return "Chant_GrowFood"; }
	@Override public String name(){ return "Grow Food";}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return 0;}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{

		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell(_("You must be outdoors to try this."));
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell(_("This magic will not work here."));
			return false;
		}

		int material=-1;
		final Vector choices=new Vector();
		final String s=CMParms.combine(commands,0);

		int col=0;
		final StringBuffer buf=new StringBuffer("Food types known:\n\r");
		final List<Integer> codes = RawMaterial.CODES.COMPOSE_RESOURCES(RawMaterial.MATERIAL_VEGETATION);
		for(final Integer code : codes)
			if(!CMParms.contains(Chant_SummonSeed.NON_SEEDS,code))
			{
				choices.addElement(code);
				final String desc=RawMaterial.CODES.NAME(code.intValue());
				if((s.length()>0)&&(CMLib.english().containsString(desc,s)))
					material=code.intValue();
				if(col==4){ buf.append("\n\r"); col=0;}
				col++;
				buf.append(CMStrings.padRight(CMStrings.capitalizeAndLower(desc),15));
			}
		if(s.equalsIgnoreCase("list"))
		{
			mob.tell(buf.toString()+"\n\r\n\r");
			return true;
		}
		if((material<0)&&(s.length()>0))
		{
			mob.tell(_("'@x1' is not a recognized form of food or herbs!    Try LIST as a parameter...",s));
			return false;
		}

		if((material<0)&&(choices.size()>0))
			material=((Integer)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).intValue();

		if(material<0) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Food newItem=(Food)CMClass.getBasicItem("GenFoodResource");
				if(material==RawMaterial.RESOURCE_HERBS)
					newItem.setNourishment(1);
				else
					newItem.setNourishment(150+(10*super.getX1Level(mob)));
				final String name=RawMaterial.CODES.NAME(material).toLowerCase();
				newItem.setMaterial(material);
				newItem.setBaseValue(1);
				newItem.basePhyStats().setWeight(1);
				newItem.setName("a pound of "+name);
				newItem.setDisplayText("some "+name+" sits here.");
				newItem.setDescription("");
				CMLib.materials().addEffectsToResource(newItem);
				newItem.recoverPhyStats();
				newItem.setMiscText(newItem.text());
				mob.location().addItem(newItem,ItemPossessor.Expire.Resource);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,_("Suddenly, @x1 pops out of the ground.",newItem.name()));
				mob.location().recoverPhyStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}
