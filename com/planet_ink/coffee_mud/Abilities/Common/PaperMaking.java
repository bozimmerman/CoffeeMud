package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class PaperMaking extends CommonSkill
{
	public String ID() { return "PaperMaking"; }
	public String name(){ return "Paper Making";}
	private static final String[] triggerStrings = {"PAPERMAKE","PAPERMAKING"};
	public String[] triggerStrings(){return triggerStrings;}
	public long flags(){return FLAG_CRAFTING;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_WOODTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_SPELL=9;

	private Item building=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public PaperMaking()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",5,ID(),false);}
	}
	public Environmental newInstance(){	return new PaperMaking();}

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
		Vector V=(Vector)Resources.getResource("PAPERMAKING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"papermaking.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("PaperMaking","Recipes not found!");
			Resources.submitResource("PAPERMAKING RECIPES",V);
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
						commonTell(mob,"<S-NAME> mess(es) up making "+building.name()+".");
					else
						mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		randomRecipeFix(mob,loadRecipes(),commands);
		if(commands.size()==0)
		{
			commonTell(mob,"Papermake what? Enter \"Papermake list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",21)+" Material required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					String material=(String)V.elementAt(RCP_WOODTYPE);
					if(level<=mob.envStats().level())
						buf.append(Util.padRight(item,21)+" "+wood+" "+material.toLowerCase()+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		else
		{
			building=null;
			messedUp=false;
			int materialType=0;
			String materialDesc="";
			String recipeName=Util.combine(commands,0);
			Vector foundRecipe=null;
			Vector matches=matchingRecipeNames(recipes,recipeName);
			for(int r=0;r<matches.size();r++)
			{
				Vector V=(Vector)matches.elementAt(r);
				if(V.size()>0)
				{
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					if(level<=mob.envStats().level())
					{
						foundRecipe=V;
						materialDesc=(String)foundRecipe.elementAt(RCP_WOODTYPE);
						if(materialDesc.equalsIgnoreCase("WOOD"))
							materialType=EnvResource.MATERIAL_WOODEN;
						else
						for(int r2=0;r2<EnvResource.RESOURCE_DESCS.length;r2++)
						{
							if(EnvResource.RESOURCE_DESCS[r2].equalsIgnoreCase(materialDesc))
							{
								materialType=EnvResource.RESOURCE_DATA[r2][0];
								break;
							}
						}
						break;
					}
				}
			}
			if((foundRecipe==null)||(materialType<=0))
			{
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"make list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			Item firstWood=null;
			int foundWood=0;
			if(materialType==EnvResource.MATERIAL_WOODEN)
				firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_WOODEN);
			else
				firstWood=findFirstResource(mob.location(),materialType);
			if(firstWood!=null)
				foundWood=findNumberOfResource(mob.location(),firstWood.material());
			if(foundWood==0)
			{
				commonTell(mob,"There is no "+materialDesc.toLowerCase()+" here to make anything from!  It might need to put it down first.");
				return false;
			}
			if(foundWood<woodRequired)
			{
				commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			destroyResources(mob.location(),woodRequired,firstWood.material(),null,null);
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)]).toLowerCase();
			itemName=Util.startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(firstWood.baseGoldValue())));
			building.setMaterial(firstWood.material());
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
			if(spell.length()>0)
			{
				String parm="";
				if(spell.indexOf(";")>0)
				{
					parm=spell.substring(spell.indexOf(";")+1);
					spell=spell.substring(0,spell.indexOf(";"));
				}
				Ability A=CMClass.getAbility(spell);
				A.setMiscText(parm);
				if(A!=null)	building.addNonUninvokableEffect(A);
			}
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			if(materialType==EnvResource.MATERIAL_WOODEN)
				building.setMaterial(EnvResource.RESOURCE_PAPER);
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			building.recoverEnvStats();
			building.text();
			building.recoverEnvStats();
		}


		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<20) completion=20;

		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
