package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public interface ExpertiseLibrary extends CMLibrary
{
    public static final int XFLAG_X1=0;
    public static final int XFLAG_X2=1;
    public static final int XFLAG_X3=2;
    public static final int XFLAG_X4=3;
    public static final int XFLAG_X5=4;
    public static final int XFLAG_LEVEL=5;
    public static final int XFLAG_TIME=6;
    public static final int XFLAG_MAXRANGE=7;
    public static final int XFLAG_LOWCOST=8;
    public static final int XFLAG_XPCOST=9;
    public static final int NUM_XFLAGS=10;
    public static final String[] XFLAG_CODES={
        "X1","X2","X3","X4","X5",
        "LEVEL","TIME","MAXRANGE","LOWCOST",
        "XPCOST"
    };
    public static class ExpertiseDefinition
    {
        public String ID="";
        public String name="";
        private String uncompiledListMask="";
        private Vector compiledListMask=null;
        private String uncompiledFinalMask="";
        public ExpertiseDefinition parent=null;
        private Vector compiledFinalMask=null;
        private int minLevel=Integer.MIN_VALUE+1;
        public int getMinimumLevel()
        {
            if(minLevel==Integer.MIN_VALUE+1)
                minLevel=CMLib.masking().minMaskLevel(allRequirements(),0);
            return minLevel;
        }
        
        public Vector compiledListMask()
        {
            if((this.compiledListMask==null)&&(uncompiledListMask.length()>0))
            {
                compiledListMask=CMLib.masking().maskCompile(uncompiledListMask);
                CMLib.ableMapper().addPreRequisites(ID,new Vector(),uncompiledListMask.trim());
            }
            return this.compiledListMask;
        }
        public Vector compiledFinalMask()
        {
            if((this.compiledFinalMask==null)&&(uncompiledFinalMask.length()>0))
            {
                this.compiledFinalMask=CMLib.masking().maskCompile(uncompiledFinalMask);
                CMLib.ableMapper().addPreRequisites(ID,new Vector(),uncompiledFinalMask.trim());
            }
            return this.compiledFinalMask;
        }
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
            compiledListMask=null;
        }
        public void addFinalMask(String mask){ 
        	if((mask==null)||(mask.length()==0)) return;
        	if(uncompiledFinalMask==null)
	        	uncompiledFinalMask=mask;
        	else
	        	uncompiledFinalMask+=mask;
        	compiledFinalMask=CMLib.masking().maskCompile(uncompiledFinalMask);
        	CMLib.ableMapper().addPreRequisites(ID,new Vector(),uncompiledFinalMask.trim());
        }
        
        public int practiceCost=0;
        public int trainCost=0;
        public int expCost=0;
        public int qpCost=0;
        public int timeCost=0;
        public String costDescription(){
        	StringBuffer cost=new StringBuffer("");
        	if(practiceCost>0) cost.append(practiceCost+" practice"+((practiceCost>1)?"s":"")+", ");
        	if(trainCost>0) cost.append(trainCost+" train"+((trainCost>1)?"s":"")+", ");
        	if(expCost>0) cost.append(expCost+" experience point"+((expCost>1)?"s":"")+", ");
        	if(qpCost>0) cost.append(qpCost+" quest point"+((qpCost>1)?"s":"")+", ");
        	if(cost.length()==0) return "";
        	return cost.substring(0,cost.length()-2);
        }
    }
    
    public ExpertiseDefinition addDefinition(String ID, String name, String listMask, String finalMask, int practices, int trains, int qpCost, int expCost, int timeCost);
    public void delDefinition(String ID);
    public ExpertiseDefinition getDefinition(String ID);
    public ExpertiseDefinition findDefinition(String ID, boolean exactOnly);
    public Enumeration definitions();
    public Vector myQualifiedExpertises(MOB mob);
    public Vector myListableExpertises(MOB mob);
    public int numExpertises();
    public void recompileExpertises();
    public String getExpertiseHelp(String ID, boolean exact);
    public String getApplicableExpertise(String ID, int code);
    public int getApplicableExpertiseLevel(String ID, int code, MOB mob);
    public int getExpertiseLevel(MOB mob, String expertise);
    public int getStages(String expertiseCode);
    public Vector getStageCodes(String expertiseCode);
    public String confirmExpertiseLine(String row, String ID, boolean addIfPossible);
}
