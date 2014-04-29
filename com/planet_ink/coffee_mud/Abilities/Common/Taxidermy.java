package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class Taxidermy extends CraftingSkill
{
	@Override public String ID() { return "Taxidermy"; }
	@Override public String name(){ return "Taxidermy";}
	private static final String[] triggerStrings = {"STUFF","TAXIDERMY"};
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "BODIES";}
	public String parametersFormat(){ return "POSE_NAME\nPOSE_DESCRIPTION\n...\n";}

	protected String foundShortName="";

	public Taxidermy()
	{
		super();
		displayText="You are stuffing...";
		verb="stuffing";
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				final MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,"You've messed up stuffing "+foundShortName+"!");
					else
						dropAWinner(mob,buildingI);
				}
			}
		}
		super.unInvoke();
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		final String filename="taxidermy.txt";
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(V==null)
		{
			V=new Vector();
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+filename,null,CMFile.FLAG_LOGERRORS).text();
			final List<String> strV=Resources.getFileLineVector(str);
			List<String> V2=null;
			boolean header=true;
			for(int v=0;v<strV.size();v++)
			{
				final String s=strV.get(v);
				if(header)
				{
					if((V2!=null)&&(V2.size()>0))
						V.add(V2);
					V2=new Vector();
				}
				if(s.length()==0)
					header=true;
				else
				if(V2 != null)
				{
					V2.add(s);
					header=false;
				}
			}
			if((V2!=null)&&(V2.size()>0))
				V.add(V2);
			if(V.size()==0)
				Log.errOut("Taxidermy","Poses not found!");
			Resources.submitResource("PARSED_RECIPE: "+filename,V);
		}
		return V;
	}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		final List<List<String>> POSES=loadRecipes();
		String pose=null;
		if(CMParms.combine(commands,0).equalsIgnoreCase("list"))
		{
			final StringBuffer str=new StringBuffer("^xTaxidermy Poses^?^.\n");
			for(int p=0;p<POSES.size();p++)
			{
				final List<String> PP=POSES.get(p);
				if(PP.size()>1)
					str.append((PP.get(0))+"\n");
			}
			mob.tell(str.toString());
			return true;
		}
		else
		if(commands.size()>0)
		{
			for(int p=0;p<POSES.size();p++)
			{
				final List<String> PP=POSES.get(p);
				if((PP.size()>1)&&(PP.get(0).equalsIgnoreCase((String)commands.firstElement())))
				{
					commands.removeElementAt(0);
					pose=PP.get(CMLib.dice().roll(1,PP.size()-1,0));
					break;
				}
			}
		}

		verb="stuffing";
		final String str=CMParms.combine(commands,0);
		final Item I=mob.location().findItem(null,str);
		if((I==null)||(!CMLib.flags().canBeSeenBy(I,mob)))
		{
			commonTell(mob,"You don't see anything called '"+str+"' here.");
			return false;
		}
		foundShortName=I.Name();
		if((!(I instanceof DeadBody))||(((DeadBody)I).playerCorpse())||(((DeadBody)I).mobName().length()==0))
		{
			commonTell(mob,"You don't know how to stuff "+I.name(mob)+".");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I2=mob.location().getItem(i);
			if(I2.container()==I)
			{
				commonTell(mob,"You need to remove the contents of "+I2.name(mob)+" first.");
				return false;
			}
		}
		int woodRequired=I.basePhyStats().weight()/5;
		final int[] pm={RawMaterial.MATERIAL_CLOTH};
		final int[][] data=fetchFoundResourceData(mob,
											woodRequired,"cloth stuffing",pm,
											0,null,null,
											false,
											0,
											null);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];

		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
		messedUp=!proficiencyCheck(mob,0,auto);
		if(buildingI!=null)    foundShortName=I.Name();
		int duration=15+(woodRequired/3);
		if(duration>65) duration=65;
		duration=getDuration(duration,mob,1,10);
		buildingI=CMClass.getItem("GenItem");
		buildingI.basePhyStats().setWeight(woodRequired);
		final String name=((DeadBody)I).mobName();
		final String desc=((DeadBody)I).mobDescription();
		I.setMaterial(data[0][FOUND_CODE]);
		buildingI.setName("the stuffed body of "+name);
		final CharStats C=(I instanceof DeadBody)?((DeadBody)I).charStats():null;
		if((pose==null)||(C==null))
			buildingI.setDisplayText("the stuffed body of "+name+" stands here");
		else
		{
			pose=CMStrings.replaceAll(pose,"<S-NAME>",buildingI.name());
			pose=CMStrings.replaceAll(pose,"<S-HIS-HER>",C.hisher());
			pose=CMStrings.replaceAll(pose,"<S-HIM-HER>",C.himher());
			pose=CMStrings.replaceAll(pose,"<S-HIM-HERSELF>",C.himher()+"self");
			buildingI.setDisplayText(pose);
		}
		buildingI.setDescription(desc);
		buildingI.setSecretIdentity(getBrand(mob));
		buildingI.recoverPhyStats();
		displayText="You are stuffing "+I.name();
		verb="stuffing "+I.name();
		playSound="scissor.wav";
		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),"<S-NAME> start(s) stuffing "+I.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		I.destroy();
		return true;
	}
}
