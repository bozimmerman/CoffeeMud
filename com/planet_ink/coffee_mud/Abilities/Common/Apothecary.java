package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.Ability;
import com.planet_ink.coffee_mud.interfaces.Drink;
import com.planet_ink.coffee_mud.interfaces.EnvResource;
import com.planet_ink.coffee_mud.interfaces.Environmental;
import com.planet_ink.coffee_mud.interfaces.MOB;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Apothecary extends Cooking
{
	public String ID() { return "Apothecary"; }
	public String name(){ return "Apothecary";}
	private static final String[] triggerStrings = {"APOTHECARY","MIX"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "MISC";}
	public String cookWordShort(){return "mix";};
	public String cookWord(){return "mixing";};
	public boolean honorHerbs(){return false;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLPRACCOST);}
    protected String defaultFoodSound="hotspring.wav";
    protected String defaultDrinkSound="hotspring.wav";


	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("APOTHECARY RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"poisons.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Apothecary","Recipes not found!");
			Resources.submitResource("APOTHECARY RECIPES",V);
		}
		return V;
	}
    
    public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
    {
        if((!super.invoke(mob,commands,givenTarget,auto,asLevel))||(finalDish==null))
            return false;
        Ability A2=finalDish.fetchEffect(0);
        if((A2!=null)
        &&(finalDish instanceof Drink))
            ((Drink)finalDish).setLiquidType(EnvResource.RESOURCE_POISON);
        return true;
    }
}
