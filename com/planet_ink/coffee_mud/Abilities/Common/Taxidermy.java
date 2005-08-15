package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

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

public class Taxidermy extends CraftingSkill
{
	public String ID() { return "Taxidermy"; }
	public String name(){ return "Taxidermy";}
	private static final String[] triggerStrings = {"STUFF","TAXIDERMY"};
	public String[] triggerStrings(){return triggerStrings;}
    protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_BODIES;}

	private Item found=null;
	private String foundShortName="";
	private boolean messedUp=false;

	public Taxidermy()
	{
		super();
		displayText="You are stuffing...";
		verb="stuffing";
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((found!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,"You've messed up stuffing "+foundShortName+"!");
					else
						mob.location().addItemRefuse(found,Item.REFUSE_PLAYER_DROP);
				}
			}
		}
		super.unInvoke();
	}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("TAXIDERMY POSES");
		if(V==null)
		{
			V=new Vector();
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"taxidermy.txt");
			Vector strV=Resources.getFileLineVector(str);
			Vector V2=null;
			boolean header=true;
			for(int v=0;v<strV.size();v++)
			{
				String s=(String)strV.elementAt(v);
				if(header)
				{
					if((V2!=null)&&(V2.size()>0))
						V.addElement(V2);
					V2=new Vector();
				}
				if(s.length()==0)
					header=true;
				else
				{
					V2.addElement(s);
					header=false;
				}
			}
			if((V2!=null)&&(V2.size()>0))
				V.addElement(V2);
			if(V.size()==0)
				Log.errOut("Taxidermy","Poses not found!");
			Resources.submitResource("TAXIDERMY POSES",V);
		}
		return V;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Vector POSES=loadRecipes();
		String pose=null;
		if(Util.combine(commands,0).equalsIgnoreCase("list"))
		{
			StringBuffer str=new StringBuffer("^xTaxidermy Poses^?^.\n");
			for(int p=0;p<POSES.size();p++)
			{
				Vector PP=(Vector)POSES.elementAt(p);
				if(PP.size()>1) str.append(((String)PP.firstElement())+"\n");
			}
			mob.tell(str.toString());
			return true;
		}
		else
		if(commands.size()>0)
		{
			for(int p=0;p<POSES.size();p++)
			{
				Vector PP=(Vector)POSES.elementAt(p);
				if((PP.size()>1)&&(((String)PP.firstElement()).equalsIgnoreCase((String)commands.firstElement())))
				{
					commands.removeElementAt(0);
					pose=(String)PP.elementAt(Dice.roll(1,PP.size()-1,0));
					break;
				}
			}
		}

		verb="stuffing";
		String str=Util.combine(commands,0);
		Item I=mob.location().fetchItem(null,str);
		if((I==null)||(!Sense.canBeSeenBy(I,mob)))
		{
			commonTell(mob,"You don't see anything called '"+str+"' here.");
			return false;
		}
		foundShortName=I.Name();
		if((!(I instanceof DeadBody))||(((DeadBody)I).playerCorpse())||(((DeadBody)I).mobName().length()==0))
		{
			commonTell(mob,"You don't know how to stuff "+I.name()+".");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if(I2.container()==I)
			{
				commonTell(mob,"You need to remove the contents of "+I2.name()+" first.");
				return false;
			}
		}
		int woodRequired=I.baseEnvStats().weight()/5;
		int[] pm={EnvResource.MATERIAL_CLOTH};
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,"cloth stuffing",pm,
											0,null,null,
											false,
											0);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];

		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null,0);
		messedUp=!profficiencyCheck(mob,0,auto);
		if(found!=null)	foundShortName=I.Name();
		int duration=15+(woodRequired/3)-mob.envStats().level();
		if(duration>65) duration=65;
		if(duration<10) duration=10;
		found=CMClass.getItem("GenItem");
		found.baseEnvStats().setWeight(woodRequired);
		String name=((DeadBody)I).mobName();
		String desc=((DeadBody)I).mobDescription();
		I.setMaterial(data[0][FOUND_CODE]);
		found.setName("the stuffed body of "+name);
		CharStats C=(I instanceof DeadBody)?((DeadBody)I).charStats():null;
		if((pose==null)||(C==null))
			found.setDisplayText("the stuffed body of "+name+" stands here");
		else
		{
			pose=Util.replaceAll(pose,"<S-NAME>",found.name());
			pose=Util.replaceAll(pose,"<S-HIS-HER>",C.hisher());
			pose=Util.replaceAll(pose,"<S-HIM-HER>",C.himher());
			pose=Util.replaceAll(pose,"<S-HIM-HERSELF>",C.himher()+"self");
			found.setDisplayText(pose);
		}
		found.setDescription(desc);
		found.setSecretIdentity("This is the work of "+mob.Name()+".");
		found.recoverEnvStats();
		displayText="You are stuffing "+I.name();
		verb="stuffing "+I.name();
		I.destroy();
		FullMsg msg=new FullMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) stuffing "+I.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
