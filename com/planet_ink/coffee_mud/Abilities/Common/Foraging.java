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
public class Foraging extends GatheringSkill
{
	public String ID() { return "Foraging"; }
	public String name(){ return "Foraging";}
	private static final String[] triggerStrings = {"FORAGE","FORAGING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_GATHERINGSKILL;}
	protected boolean allowedWhileMounted(){return false;}
	public String supportedResourceString(){return "VEGETATION|HEMP|SILK|COTTON";}

	protected Item found=null;
	protected String foundShortName="";
	public Foraging()
	{
		super();
		displayText="You are foraging...";
		verb="foraging";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(found!=null)
				{
					commonTell(mob,"You have found some "+foundShortName+"!");
					displayText="You are foraging for "+foundShortName;
					verb="foraging for "+foundShortName;
				}
				else
				{
					StringBuffer str=new StringBuffer("You can't seem to find anything worth foraging around here.\n\r");
					int d=lookingFor(RawMaterial.MATERIAL_VEGETATION,mob.location());
					if(d<0)
						str.append("You might try elsewhere.");
					else
						str.append("You might try "+Directions.getInDirectionName(d)+".");
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
		}
		return super.tick(ticking,tickID);
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
					int amount=((found.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_CLOTH)?
							   (CMLib.dice().roll(1,30,0)*(abilityCode())):
							   (CMLib.dice().roll(1,5,0)*(abilityCode()));
					String s="s";
					if(amount==1) s="";
					mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to gather "+amount+" pound"+s+" of "+foundShortName+".");
					for(int i=0;i<amount;i++)
					{
						Item newFound=(Item)found.copyOf();
						mob.location().addItemRefuse(newFound,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
						CMLib.commands().postGet(mob,null,newFound,true);
					}
				}
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        bundling=false;
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
            bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
			    return super.bundle(mob,commands);
		    return false;
		}
		
		verb="foraging";
		found=null;
		if((!confirmPossibleMaterialLocation(RawMaterial.MATERIAL_VEGETATION,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_HEMP,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_SILK,mob.location()))
		&&(!confirmPossibleMaterialLocation(RawMaterial.RESOURCE_COTTON,mob.location())))
		{
			commonTell(mob,"You don't think this is a good place to forage.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int resourceType=mob.location().myResource();
		if((proficiencyCheck(mob,0,auto))
		   &&(((resourceType&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION)
			  ||(resourceType==RawMaterial.RESOURCE_HEMP)
			  ||(resourceType==RawMaterial.RESOURCE_SILK)
			  ||(resourceType==RawMaterial.RESOURCE_COTTON)))
		{
			found=(Item)CMLib.materials().makeResource(resourceType,Integer.toString(mob.location().domainType()),false,null);
			foundShortName="nothing";
			if(found!=null)
				foundShortName=RawMaterial.CODES.NAME(found.material()).toLowerCase();
		}
		int duration=getDuration(45,mob,1,10);
		CMMsg msg=CMClass.getMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) foraging.");
		if(mob.location().okMessage(mob,msg))
		{
			// herb/locale customisation for jeremy
			if((found!=null)
			&&(found.material()==RawMaterial.RESOURCE_HERBS)
			&&((found.Name().toUpperCase().endsWith(" HERBS"))
			   ||(found.Name().equalsIgnoreCase("herbs"))))
			{
				Hashtable H=(Hashtable)Resources.getResource("HERB_LOCALE_MAP");
				if(H==null)
				{
					H=Resources.getMultiLists("skills/herbs.txt");
					if(H!=null)
						Resources.submitResource("HERB_LOCALE_MAP",H);
				}
				if(H!=null)
				{
					Vector V=(Vector)H.get(mob.location().ID());
					if((V!=null)&&(V.size()>0))
					{
						int total=0;
						for(int i=0;i<V.size();i++)
						{
							String s=(String)V.elementAt(i);
							int x=s.indexOf(" ");
							if((x>=0)&&(CMath.isNumber(s.substring(0,x).trim())))
								total+=CMath.s_int(s.substring(0,x).trim());
							else
								total+=10;
						}
						int choice=CMLib.dice().roll(1,total,-1);
						total=0;
						for(int i=0;i<V.size();i++)
						{
							String s=(String)V.elementAt(i);
							int x=s.indexOf(" ");
							if((x>=0)&&(CMath.isNumber(s.substring(0,x).trim())))
							{
								total+=CMath.s_int(s.substring(0,x).trim());
								if(choice<=total)
								{
									found.setSecretIdentity(s.substring(x+1).trim());
									break;
								}
							}
							else
							{
								total+=10;
								if(choice<=total)
								{
									found.setSecretIdentity(s);
									break;
								}
							}
						}
					}
				}
			}
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
