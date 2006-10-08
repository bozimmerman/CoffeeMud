package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
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
public class ColumbiaUniv extends StdLibrary implements ExpertiseLibrary
{
    public String ID(){return "ColumbiaUniv";}
								
	public Hashtable completeEduMap=new Hashtable();

    public void addDefinition(String ID, String name, String listMask, String finalMask, int practices, int trains, int qpCost, int expCost, int timeCost)
    {
    	if(getDefinition(ID)!=null) return;
    	if(CMSecurity.isDisabled("EXPERTISE_"+ID.toUpperCase())) return;
    	if(CMSecurity.isDisabled("EXPERTISE_*")) return;
    	for(int i=1;i<ID.length();i++)
        	if(CMSecurity.isDisabled("EXPERTISE_"+ID.substring(0,i).toUpperCase()+"*")) 
        		return;
    	ExpertiseDefinition def=new ExpertiseDefinition();
    	def.ID=ID.toUpperCase();
    	def.name=name;
    	def.addListMask(listMask);
    	def.addFinalMask(finalMask);
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
    public ExpertiseDefinition getDefinition(String ID){ return (ExpertiseDefinition)completeEduMap.get(ID.trim().toUpperCase());}
    public ExpertiseDefinition findDefinition(String ID, boolean exactOnly)
    {
        ExpertiseDefinition D=getDefinition(ID);
        if(D!=null) return D;
        for(Enumeration e=definitions();e.hasMoreElements();)
        {
            D=(ExpertiseDefinition)e.nextElement();
            if(D.name.equalsIgnoreCase(ID)) return D;
        }
        if(exactOnly) return null;
        for(Enumeration e=definitions();e.hasMoreElements();)
        {
            D=(ExpertiseDefinition)e.nextElement();
            if(D.ID.startsWith(ID)) return D;
        }
        for(Enumeration e=definitions();e.hasMoreElements();)
        {
            D=(ExpertiseDefinition)e.nextElement();
            if(CMLib.english().containsString(D.name,ID)) return D;
        }
        return null;
    }
    
    
    public Vector myQualifiedExpertises(MOB mob)
    {
    	ExpertiseDefinition D=null;
    	Vector V=new Vector();
    	for(Enumeration e=definitions();e.hasMoreElements();)
    	{
    		D=(ExpertiseDefinition)e.nextElement();
    		if(((D.compiledFinalMask==null)||(CMLib.masking().maskCheck(D.compiledFinalMask,mob)))
    		&&((D.compiledListMask==null)||(CMLib.masking().maskCheck(D.compiledListMask,mob))))
    			V.addElement(D);
    	}
    	return V;
    }
    public Vector myListableExpertises(MOB mob)
    {
    	ExpertiseDefinition D=null;
    	Vector V=new Vector();
    	for(Enumeration e=definitions();e.hasMoreElements();)
    	{
    		D=(ExpertiseDefinition)e.nextElement();
    		if((D.compiledListMask==null)||(CMLib.masking().maskCheck(D.compiledListMask,mob)))
    			V.addElement(D);
    	}
    	return V;
    }
    
}
