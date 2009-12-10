package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_GrowItem extends Chant
{
	public String ID() { return "Chant_GrowItem"; }
	public String name(){ return "Grow Item";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 50;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&((mob.location().myResource()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=RawMaterial.RESOURCE_OAK;
		if((mob.location().myResource()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
			material=mob.location().myResource();
		else
		{
			Vector V=mob.location().resourceChoices();
			Vector V2=new Vector();
			if(V!=null)
			for(int v=0;v<V.size();v++)
			{
				if((((Integer)V.elementAt(v)).intValue()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
					V2.addElement(V.elementAt(v));
			}
			if(V2.size()>0)
				material=((Integer)V2.elementAt(CMLib.dice().roll(1,V2.size(),-1))).intValue();
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the trees.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				ItemCraftor A=(ItemCraftor)CMClass.getAbility("Carpentry");
				Vector V=null;
				if(A!=null) V=A.craftAnyItem(material);
				if((V==null)||(V.size()==0))
				{
					mob.tell("The chant failed for some reason...");
					return false;
				}
				Item building=(Item)V.firstElement();
				Item key=null;
				if(V.size()>1) key=(Item)V.lastElement();
				mob.location().addItemRefuse(building,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_RESOURCE));
				if(key!=null) mob.location().addItemRefuse(key,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_RESOURCE));
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,building.name()+" grows out of a tree and drops.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the trees, but nothing happens.");

		// return whether it worked
		return success;
	}
}
