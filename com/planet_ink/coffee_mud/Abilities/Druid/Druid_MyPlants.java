package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Druid_MyPlants extends StdAbility
{
	public String ID() { return "Druid_MyPlants"; }
	public String name(){ return "My Plants";}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	private static final String[] triggerStrings = {"MYPLANTS","PLANTS"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_NATURELORE;}

	public static boolean isMyPlant(Item I, MOB mob)
	{
		if((I!=null)
		&&(I.rawSecretIdentity().equals(mob.Name()))
		&&(I.owner()!=null)
		&&(I.owner() instanceof Room))
		{
			for(int a=0;a<I.numEffects();a++)
			{
				Ability A=I.fetchEffect(a);
				if((A!=null)
				&&((A.invoker()==mob)||(A.text().equals(mob.Name())))
				&&(A instanceof Chant_SummonPlants))
					return true;
			}
		}
		return false;
	}

	public static Item myPlant(Room R, MOB mob, int which)
	{
		int plantNum=0;
		if(R!=null)
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if(isMyPlant(I,mob))
			{
				if(plantNum==which)
					return I;
				plantNum++;
			}
		}
		return null;
	}

    public static Vector myAreaPlantRooms(MOB mob, Area A)
    {
        Vector V=new Vector();
        try
        {
            if(A!=null)
            for(Enumeration r=A.getMetroMap();r.hasMoreElements();)
            {
                Room R=(Room)r.nextElement();
                if((myPlant(R,mob,0)!=null)&&(!V.contains(R)))
                    V.addElement(R);
            }
        }catch(NoSuchElementException e){}
        return V;
    }
    
	public static Vector myPlantRooms(MOB mob)
	{
		Vector V=new Vector();
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((myPlant(R,mob,0)!=null)&&(!V.contains(R)))
					V.addElement(R);
			}
	    }catch(NoSuchElementException e){}
		return V;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			mob.tell("Your plant senses fail you.");
		else
		{
			CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_QUIETMOVEMENT|CMMsg.MASK_MAGIC,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer yourPlants=new StringBuffer("");
				int plantNum=0;
				Vector V=myPlantRooms(mob);
				for(int v=0;v<V.size();v++)
				{
					Room R=(Room)V.elementAt(v);
					if(R!=null)
					{
						int i=0;
						Item I=myPlant(R,mob,0);
						while(I!=null)
						{
							yourPlants.append(CMStrings.padRight(""+(++plantNum),3)+" ");
							yourPlants.append(CMStrings.padRight(I.name(),20)+" ");
							yourPlants.append(CMStrings.padRight(R.roomTitle(mob),40));
							yourPlants.append("\n\r");
							I=myPlant(R,mob,++i);
						}
					}
				}
				if(V.size()==0)
					mob.tell("You don't sense that there are ANY plants which are attuned to you.");
				else
					mob.tell("### Plant Name           Location\n\r"+yourPlants.toString());
			}
		}
		return success;
	}
}

