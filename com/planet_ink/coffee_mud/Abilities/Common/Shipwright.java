package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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

public class Shipwright extends CraftingSkill
{
	public String ID() { return "Shipwright"; }
	public String name(){ return "Shipwright";}
	private static final String[] triggerStrings = {"SHIPWRIGHT"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	//private static final int RCP_CAPACITY=7;
	//private static final int RCP_ARMORDMG=8;
	private static final int RCP_SPELL=9;


	private Item building=null;
	private Item key=null;
	private boolean mending=false;
	private boolean messedUp=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("SHIPWRIGHT RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"shipwright.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Shipwright","Recipes not found!");
			Resources.submitResource("SHIPWRIGHT RECIPES",V);
		}
		return V;
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(mending)
							commonEmote(mob,"<S-NAME> mess(es) up mending "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up carving "+building.name()+".");
					}
					else
					{
						if(mending)
							building.setUsesRemaining(100);
						else
						{
							mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
							if(key!=null)
							{
								mob.location().addItemRefuse(key,Item.REFUSE_PLAYER_DROP);
								key.setContainer(building);
							}
						}
					}
				}
				building=null;
				key=null;
				mending=false;
			}
		}
		super.unInvoke();
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if(student.fetchAbility("Carpentry")==null)
		{
			teacher.tell(student.name()+" has not yet learned carpentry.");
			student.tell("You need to learn carpentry before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		Item IE=(Item)E;
		if(!(IE instanceof Rideable))
		{
			if(!quiet)
				commonTell(mob,"You don't know how to mend that.");
			return false;
		}
		if((IE.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		{
			if(!quiet)
				commonTell(mob,"That's not made of wood.  That can't be mended.");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int autoGenerate=0;
		if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
			givenTarget=null;
		}
		randomRecipeFix(mob,loadRecipes(),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Shipwright what? Enter \"shipwright list\" for a list, \"shipwright scan\", or \"shipwright mend <item>\".");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		boolean bundle=false;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",16)+" Lvl Wood required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if(level<=mob.envStats().level())
						buf.append(Util.padRight(item,16)+" "+Util.padRight(""+level,3)+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			key=null;
			messedUp=false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(!canMend(mob,building,false)) return false;
			mending=true;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) mending "+building.name()+".";
			displayText="You are mending "+building.name();
			verb="mending "+building.name();
		}
		else
		{
			building=null;
			mending=false;
			key=null;
			messedUp=false;
			int amount=-1;
			if((commands.size()>1)&&(Util.isNumber((String)commands.lastElement())))
			{
				amount=Util.s_int((String)commands.lastElement());
				commands.removeElementAt(commands.size()-1);
			}
			String recipeName=Util.combine(commands,0);
			Vector foundRecipe=null;
			Vector matches=matchingRecipeNames(recipes,recipeName,true);
			for(int r=0;r<matches.size();r++)
			{
				Vector V=(Vector)matches.elementAt(r);
				if(V.size()>0)
				{
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					if(level<=mob.envStats().level())
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to carve a '"+recipeName+"'.  Try \"shipwright list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			if(amount>woodRequired) woodRequired=amount;
			int[] pm={EnvResource.MATERIAL_WOODEN};
			String misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"wood",pm,
												0,null,null,
												false,
												autoGenerate);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int woodDestroyed=woodRequired;
			int lostValue=destroyResources(mob.location(),woodDestroyed,data[0][FOUND_CODE],0,null,autoGenerate);
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=Util.s_int((String)foundRecipe.elementAt(RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK)]).toLowerCase();
			if(misctype.equalsIgnoreCase("BUNDLE"))
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=Util.startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) carving "+building.name()+".";
			displayText="You are carving "+building.name();
			verb="carving "+building.name();
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
			building.setMaterial(data[0][FOUND_CODE]);
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
			bundle=misctype.equalsIgnoreCase("BUNDLE");
			if(misctype.equalsIgnoreCase("bundle")) building.setBaseValue(lostValue);
			if(spell.length()>0)
			{
				String parm="";
				if(spell.indexOf(";")>0)
				{
					parm=spell.substring(spell.indexOf(";")+1);
					spell=spell.substring(0,spell.indexOf(";"));
				}
				Ability A=CMClass.getAbility(spell);
				if(A!=null)
				{
					A.setMiscText(parm);
					building.addNonUninvokableEffect(A);
				}
			}
			key=null;
			if(building instanceof Rideable)
			{
				if(misctype.equalsIgnoreCase("CHAIR"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SIT);
				else
				if(misctype.equalsIgnoreCase("TABLE"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_TABLE);
				else
				if(misctype.equalsIgnoreCase("BED"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SLEEP);
			}
			building.recoverEnvStats();
			building.text();
			building.recoverEnvStats();
		}


		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<4) completion=4;

		if(bundle)
		{
			messedUp=false;
			completion=1;
			verb="bundling "+EnvResource.RESOURCE_DESCS[building.material()&EnvResource.RESOURCE_MASK].toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}

		if(autoGenerate>0)
		{
			commands.addElement(building);
			return true;
		}

		FullMsg msg=new FullMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,completion);
		}
		else
		if(bundle)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
