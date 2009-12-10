package com.planet_ink.coffee_mud.Items.Basic;
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
public class GenFoodResource extends GenFood implements RawMaterial, Food
{
	public String ID(){	return "GenFoodResource";}
	protected static Ability rot=null;
	
	public GenFoodResource()
	{
		super();
		setName("an edible resource");
		setDisplayText("a pile of edible resource sits here.");
		setDescription("");
		material=RawMaterial.RESOURCE_BERRIES;
		setNourishment(200);
		baseEnvStats().setWeight(0);
		recoverEnvStats();
		decayTime=0;
	}
	
	public void setMaterial(int newValue)
	{
	    super.setMaterial(newValue);
	    decayTime=0;
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
        super.executeMsg(host,msg);
        if(rot==null){
        	rot=CMClass.getAbility("Prayer_Rot");
        	if(rot==null) return;
        	rot.setAffectedOne(null);
        }
        rot.executeMsg(this,msg);
	}
	
	public boolean rebundle(){return false;}//CMLib.materials().rebundle(this);}
	public void quickDestroy(){ CMLib.materials().quickDestroy(this);}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
        if(rot==null){
        	rot=CMClass.getAbility("Prayer_Rot");
        	if(rot==null) return true;
        	rot.setAffectedOne(null);
        }
        if(!rot.okMessage(this,msg))
        	return false;
        return super.okMessage(host,msg);
	}
	protected String domainSource=null;
	public String domainSource(){return domainSource;}
	public void setDomainSource(String src){domainSource=src;}
}
