package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Chant_SummonSeed extends Chant
{
	public String ID() { return "Chant_SummonSeed"; }
	public String name(){ return "Summon Seeds";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String s=Util.combine(commands,0);
		StringBuffer buf=new StringBuffer("Seed types known:\n\r");
		int material=0;
		String foundShortName=null;
		int col=0;
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
		{
			String str=EnvResource.RESOURCE_DESCS[i];
			if((EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION)
			{
				if(str.toUpperCase().equalsIgnoreCase(s))
				{
					material=EnvResource.RESOURCE_DATA[i][0];
					foundShortName=Util.capitalize(str);
					break;
				}
				if(col==4){ buf.append("\n\r"); col=0;}
				col++;
				buf.append(Util.padRight(Util.capitalize(str),15));
			}
		}
		if(s.equalsIgnoreCase("list"))
		{
			mob.tell(buf.toString()+"\n\r\n\r");
			return true;
		}
		if(s.length()==0)
		{
			mob.tell("Summon what kind of seed?  Try LIST as a parameter...");
			return false;
		}
		if(foundShortName==null)
		{
			mob.tell("'"+s+"' is an unknown type of vegetation.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <S-HIS-HER> hands.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=2;i<(2+(adjustedLevel(mob,asLevel)/4));i++)
				{
					Item newItem=(Item)CMClass.getStdItem("GenResource");
					String name=foundShortName.toLowerCase();
					if(name.endsWith("ies")) name=name.substring(0,name.length()-3)+"y";
					if(name.endsWith("s")) name=name.substring(0,name.length()-1);
					newItem.setName(Util.startWithAorAn(name+" seed"));
					newItem.setDisplayText(newItem.name()+" is here.");
					newItem.setDescription("");
					newItem.setMaterial(material);
					newItem.baseEnvStats().setWeight(0);
					newItem.recoverEnvStats();
					newItem.setMiscText(newItem.text());
					mob.addInventory(newItem);
				}
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Some seeds appear!");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-HIS-HER> hands, but nothing happens.");

		// return whether it worked
		return success;
	}
}
