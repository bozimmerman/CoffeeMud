package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class InstrumentMaking extends CommonSkill
{
	public String ID() { return "InstrumentMaking"; }
	public String name(){ return "Instrument Making";}
	private static final String[] triggerStrings = {"INSTRUMENTMAKING","INSTRUMENTMAKE"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_MATERIAL=7;
	private static final int RCP_RACES=8;
	private static final int RCP_TYPE=9;

	private Item building=null;
	private boolean messedUp=false;
	private static boolean mapped=false;

	public InstrumentMaking()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("Archon",1,ID(),false);
					CMAble.addCharAbilityMapping("Minstrel",1,ID(),false);}
	}
	public Environmental newInstance(){return new InstrumentMaking();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.TICK_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("INSTRUMENT RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"instruments.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("InstrumentMaking","Recipes not found!");
			Resources.submitResource("INSTRUMENT RECIPES",V);
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
						commonEmote(mob,"<S-NAME> mess(es) up making "+building.name()+".");
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
		if(commands.size()==0)
		{
			commonTell(mob,"Make what Instrument? Enter \"instrumentmake list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" "+Util.padRight("Type",10)+" Material required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					String type=(String)V.elementAt(RCP_MATERIAL);
					String race=((String)V.elementAt(RCP_RACES)).trim();
					String itype=Util.capitalize(((String)V.elementAt(RCP_TYPE)).toLowerCase()).trim();
					if((level<=mob.envStats().level())
					&&((race.length()==0)||((" "+race+" ").toUpperCase().indexOf(" "+mob.charStats().getMyRace().ID().toUpperCase()+" ")>=0)))
						buf.append(Util.padRight(item,20)+" "+Util.padRight(itype,10)+" "+wood+" "+type+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		building=null;
		String recipeName=Util.combine(commands,0);
		Vector foundRecipe=null;
		for(int r=0;r<recipes.size();r++)
		{
			Vector V=(Vector)recipes.elementAt(r);
			if(V.size()>0)
			{
				String item=(String)V.elementAt(RCP_FINALNAME);
				String race=((String)V.elementAt(RCP_RACES)).trim();
				int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
				if((level<=mob.envStats().level())
				&&(replacePercent(item,"").equalsIgnoreCase(recipeName))
				&&((race.length()==0)||((" "+race+" ").toUpperCase().indexOf(" "+mob.charStats().getMyRace().ID().toUpperCase()+" ")>=0)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"instrumentmake list\" for a list.");
			return false;
		}
		int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
		String materialRequired=(String)foundRecipe.elementAt(RCP_MATERIAL);
		Item firstWood=null;
		if(materialRequired.toUpperCase().startsWith("METAL"))
		{
			firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
			if(firstWood==null)
				firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_MITHRIL);
		}
		else
			firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_WOODEN);

		int foundWood=0;
		if(firstWood!=null)
			foundWood=findNumberOfResource(mob.location(),firstWood.material());
		if(foundWood==0)
		{
			commonTell(mob,"There is none of the proper material here to make anything from!  It might need to put it down first.");
			return false;
		}
		if(firstWood.material()==EnvResource.RESOURCE_BALSA)
			woodRequired=woodRequired/2;
		else
		if(firstWood.material()==EnvResource.RESOURCE_IRONWOOD)
			woodRequired=woodRequired*2;
		if(firstWood.material()==EnvResource.RESOURCE_MITHRIL)
			woodRequired=woodRequired/2;
		else
		if(firstWood.material()==EnvResource.RESOURCE_ADAMANTITE)
			woodRequired=woodRequired/3;
		if(woodRequired<1) woodRequired=1;

		if(foundWood<woodRequired)
		{
			commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int lostValue=destroyResources(mob.location(),woodRequired,firstWood.material(),null,null);
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
		building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
		building.setMaterial(firstWood.material());
		building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
		if(building.baseEnvStats().level()<1) building.baseEnvStats().setLevel(1);
		String misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
		String type=(String)foundRecipe.elementAt(RCP_TYPE);
		for(int i=0;i<MusicalInstrument.TYPE_DESC.length;i++)
			if(type.equalsIgnoreCase(MusicalInstrument.TYPE_DESC[i]))
				((MusicalInstrument)building).setInstrumentType(i);
		building.setSecretIdentity("This is the work of "+mob.Name()+".");
		if(building instanceof Rideable)
		{
			((Rideable)building).setRideBasis(Rideable.RIDEABLE_SIT);
			((Rideable)building).setRiderCapacity(Util.s_int(misctype));
			if(((Rideable)building).riderCapacity()<=0)
				((Rideable)building).setRiderCapacity(1);
		}
		else
		{
			((Item)building).setRawProperLocationBitmap(0);
			for(int wo=1;wo<Item.wornLocation.length;wo++)
			{
				String WO=Item.wornLocation[wo].toUpperCase();
				if(misctype.equalsIgnoreCase(WO))
				{
					((Item)building).setRawProperLocationBitmap(Util.pow(2,wo-1));
					((Item)building).setRawLogicalAnd(false);
				}
				else
				if((misctype.toUpperCase().indexOf(WO+"||")>=0)
				||(misctype.toUpperCase().endsWith("||"+WO)))
				{
					((Item)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|Util.pow(2,wo-1));
					((Item)building).setRawLogicalAnd(false);
				}
				else
				if((misctype.toUpperCase().indexOf(WO+"&&")>=0)
				||(misctype.toUpperCase().endsWith("&&"+WO)))
				{
					((Item)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|Util.pow(2,wo-1));
					((Item)building).setRawLogicalAnd(true);
				}
			}
		}
		if(misctype.equalsIgnoreCase("bundle")) building.setBaseValue(lostValue);
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();


		messedUp=!profficiencyCheck(0,auto);
		if(completion<4) completion=4;

		if(misctype.equalsIgnoreCase("bundle"))
		{
			messedUp=false;
			completion=1;
			verb="bundling "+EnvResource.RESOURCE_DESCS[building.material()&EnvResource.RESOURCE_MASK].toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}

		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
