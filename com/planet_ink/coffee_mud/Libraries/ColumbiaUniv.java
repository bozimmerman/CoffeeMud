package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.EducationLibrary.EducationDefinition;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class ColumbiaUniv extends StdLibrary implements EducationLibrary
{
    public String ID(){return "ColumbiaUniv";}
								
	public Hashtable completeEduMap=new Hashtable();

    public void addDefinition(String ID, String name, String mask, int practices, int trains, int qpCost, int expCost, int timeCost){
    	if(getDefinition(ID)!=null) return;
    	EducationDefinition def=new EducationDefinition();
    	def.ID=ID.toUpperCase();
    	def.name=name;
    	def.uncompiledMask=mask!=null?mask.trim():"";
    	if(mask.trim().length()>0) def.compiledMask=CMLib.masking().maskCompile(mask);
    	def.practiceCost=practices;
    	def.trainCost=trains;
    	def.qpCost=qpCost;
    	def.expCost=expCost;
    	def.timeCost=timeCost;
    	completeEduMap.put(def.ID,def);
    }
    public void delDefinition(String ID){
    	completeEduMap.remove(ID);
    }
    public Enumeration definitions(){ return ((Hashtable)completeEduMap.clone()).elements();}
    public EducationDefinition getDefinition(String ID){ return (EducationDefinition)completeEduMap.get(ID.trim().toUpperCase());}
    
    public Vector myQualifiedEducations(MOB mob)
    {
    	EducationDefinition D=null;
    	Vector V=new Vector();
    	for(Enumeration e=definitions();e.hasMoreElements();)
    	{
    		D=(EducationDefinition)e.nextElement();
    		if((D.compiledMask==null)||(CMLib.masking().maskCheck(D.compiledMask,mob)))
    			V.addElement(D);
    	}
    	return V;
    }
    
}
