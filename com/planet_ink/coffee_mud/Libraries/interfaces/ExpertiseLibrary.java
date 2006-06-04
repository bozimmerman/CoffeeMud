package com.planet_ink.coffee_mud.Libraries.interfaces;
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
public interface ExpertiseLibrary extends CMObject
{
    public static class ExpertiseDefinition
    {
        public String ID="";
        public String name="";
        private String uncompiledListMask="";
        public Vector compiledListMask=null;
        private String uncompiledFinalMask="";
        public Vector compiledFinalMask=null;
        public String allRequirements(){
			String req=uncompiledListMask;
			if(req==null) req=""; else req=req.trim();
			if((uncompiledFinalMask!=null)&&(uncompiledFinalMask.length()>0))
				req=req+" "+uncompiledFinalMask;
			return req.trim();
        }
        public String listRequirements(){return uncompiledListMask;}
        public String finalRequirements(){return uncompiledFinalMask;}
        public void addListMask(String mask){
        	if((mask==null)||(mask.length()==0)) return;
        	if(uncompiledListMask==null)
	        	uncompiledListMask=mask;
        	else
	        	uncompiledListMask+=mask;
        	compiledListMask=CMLib.masking().maskCompile(uncompiledListMask);
        	CMLib.ableMapper().addPreRequisites(ID,new Vector(),mask.trim());
        }
        public void addFinalMask(String mask){ 
        	if((mask==null)||(mask.length()==0)) return;
        	if(uncompiledFinalMask==null)
	        	uncompiledFinalMask=mask;
        	else
	        	uncompiledFinalMask+=mask;
        	compiledFinalMask=CMLib.masking().maskCompile(uncompiledFinalMask);
        	CMLib.ableMapper().addPreRequisites(ID,new Vector(),mask.trim());
        }
        
        public int practiceCost=0;
        public int trainCost=0;
        public int expCost=0;
        public int qpCost=0;
        public int timeCost=0;
        public String costDescription(){
        	StringBuffer cost=new StringBuffer("");
        	if(practiceCost>0) cost.append(practiceCost+" practices, ");
        	if(trainCost>0) cost.append(trainCost+" training sessions, ");
        	if(expCost>0) cost.append(expCost+" experience points, ");
        	if(qpCost>0) cost.append(qpCost+" quest points, ");
        	if(cost.length()==0) return "";
        	return cost.substring(0,cost.length()-2);
        }
    }
    
    public void addDefinition(String ID, String name, String listMask, String finalMask, int practices, int trains, int qpCost, int expCost, int timeCost);
    public void delDefinition(String ID);
    public ExpertiseDefinition getDefinition(String ID);
    public Enumeration definitions();
    public Vector myQualifiedExpertises(MOB mob);
    public Vector myListableExpertises(MOB mob);
}
