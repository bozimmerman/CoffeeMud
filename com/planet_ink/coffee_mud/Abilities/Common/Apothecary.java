package com.planet_ink.coffee_mud.Abilities.Common;
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
public class Apothecary extends Cooking
{
	public String ID() { return "Apothecary"; }
	public String name(){ return "Apothecary";}
	private static final String[] triggerStrings = {"APOTHECARY","MIX"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "MISC";}
	public String cookWordShort(){return "mix";}
	public String cookWord(){return "mixing";}
	public boolean honorHerbs(){return false;}
	protected int iniTrainsRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_SKILLTRAINCOST);}
	protected int iniPracticesRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_SKILLPRACCOST);}

    public String parametersFile(){ return "poisons.txt";}
    protected Vector loadRecipes(){return super.loadRecipes(parametersFile());}

    public Apothecary()
    {
        super();

        defaultFoodSound = "hotspring.wav";
        defaultDrinkSound = "hotspring.wav";
    }



    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if((!super.invoke(mob,commands,givenTarget,auto,asLevel))||(building==null))
            return false;
        Ability A2=building.fetchEffect(0);
        if((A2!=null)
        &&(building instanceof Drink))
            ((Drink)building).setLiquidType(RawMaterial.RESOURCE_POISON);
        return true;
    }
}
