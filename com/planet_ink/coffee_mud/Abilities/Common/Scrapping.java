package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Scrapping extends CommonSkill
{
	public String ID() { return "Scrapping"; }
	public String name(){ return "Scrapping";}
	private static final String[] triggerStrings = {"SCRAP","SCRAPPING"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLPRACCOST);}

	private Item found=null;
	private Item fire=null;
	private int amount=0;
	private String oldItemName="";
	private String foundShortName="";
	private boolean messedUp=false;
	public Scrapping()
	{
		super();
		displayText="You are scrapping...";
		verb="scrapping";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((found==null)
			||((fire!=null)&&((!Sense.isOnFire(fire))
							||(!mob.location().isContent(fire))
							||(mob.isMine(fire)))))
			{
				messedUp=true;
				unInvoke();
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
					if(messedUp)
						commonTell(mob,"You've messed up scrapping "+oldItemName+"!");
					else
					{
						amount=amount*(abilityCode());
						String s="s";
						if(amount==1) s="";
						mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to scrap "+amount+" pound"+s+" of "+foundShortName+".");
						for(int i=0;i<amount;i++)
						{
							Item newFound=(Item)found.copyOf();
							mob.location().addItemRefuse(newFound,Item.REFUSE_PLAYER_DROP);
							CommonMsgs.get(mob,null,newFound,true);
						}
					}
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		verb="scrapping";
		String str=Util.combine(commands,0);
		Item I=mob.location().fetchItem(null,str);
		if((I==null)||(!Sense.canBeSeenBy(I,mob)))
		{
			commonTell(mob,"You don't see anything called '"+str+"' here.");
			return false;
		}
		boolean okMaterial=true;
		oldItemName=I.Name();
		switch(I.material()&EnvResource.MATERIAL_MASK)
		{
		case EnvResource.MATERIAL_FLESH:
		case EnvResource.MATERIAL_LIQUID:
		case EnvResource.MATERIAL_PAPER:
		case EnvResource.MATERIAL_ENERGY:
		case EnvResource.MATERIAL_VEGETATION:
			{ okMaterial=false; break;}
		}
		if(!okMaterial)
		{
			commonTell(mob,"You don't know how to scrap "+I.name()+".");
			return false;
		}
		
		if(I instanceof EnvResource)
		{
			commonTell(mob,I.name()+" already looks like scrap.");
			return false;
		}
		
		if(enchanted(I))
		{
			commonTell(mob,I.name()+" is enchanted, and can't be scrapped.");
			return false;
		}
		
		Vector V=new Vector();
		int totalWeight=0;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.sameAs(I)))
			{
				totalWeight+=I2.envStats().weight();
				V.addElement(I2);
			}
		}

		LandTitle t=CoffeeUtensils.getLandTitle(mob.location());
		if((t!=null)&&(!CoffeeUtensils.doesOwnThisProperty(mob,mob.location())))
		{
			mob.tell("You are not allowed to scrap anything here.");
			return false;
		}

		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2.container()!=null)&&(V.contains(I2.container())))
			{
				commonTell(mob,"You need to remove the contents of "+I2.name()+" first.");
				return false;
			}
		}
		amount=totalWeight/5;
		if(amount<1)
		{
			commonTell(mob,"You don't have enough here to get anything from.");
			return false;
		}

		fire=null;
		if(((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_GLASS)
		||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
		||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PLASTIC)
		||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL))
		{
			fire=getRequiredFire(mob,0);
			if(fire==null) return false;
		}

		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int duration=35-mob.envStats().level();
		if(duration<10) duration=10;
		messedUp=!profficiencyCheck(mob,0,auto);
		found=CoffeeUtensils.makeItemResource(I.material());
		foundShortName="nothing";
		if(found!=null)
			foundShortName=EnvResource.RESOURCE_DESCS[found.material()&EnvResource.RESOURCE_MASK].toLowerCase();
		FullMsg msg=new FullMsg(mob,found,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) scrapping "+I.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			for(int v=0;v<V.size();v++)
			{
			    if(((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PRECIOUS)
			    ||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
			    ||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL))
			        duration+=((Item)V.elementAt(v)).envStats().weight();
			    else
			        duration+=((Item)V.elementAt(v)).envStats().weight()/2;
			    ((Item)V.elementAt(v)).destroy();
			}
			mob.location().send(mob,msg);
			found=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
