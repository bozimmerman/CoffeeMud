package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Wainwrighting extends CommonSkill
{
	public String ID() { return "Wainwrighting"; }
	public String name(){ return "Wainwrighting";}
	private static final String[] triggerStrings = {"WAINWRIGHTING"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_NUMRIDERS=8;
	private static final int RCP_CONTAINMASK=9;

	private Item building=null;
	private Item key=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public Wainwrighting()
	{
		super();
		if((!mapped)&&(ID().equals("Wainwrighting")))
		{
			mapped=true;
			CMAble.addCharAbilityMapping("All",1,ID(),false);
		}
	}
	public Environmental newInstance(){return new Wainwrighting();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	protected synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("WAINWRIGHT RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"wainwright.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Wainwrighting","Recipes not found!");
			Resources.submitResource("WAINWRIGHT RECIPES",V);
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
						commonEmote(mob,"<S-NAME> completely mess(es) up carving "+building.displayName()+".");
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
				building=null;
				key=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Wainwright what? Enter \"wainwright list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Wood required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if(level<=mob.envStats().level())
						buf.append(Util.padRight(item,20)+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		building=null;
		key=null;
		messedUp=false;
		String recipeName=Util.combine(commands,0);
		Vector foundRecipe=null;
		for(int r=0;r<recipes.size();r++)
		{
			Vector V=(Vector)recipes.elementAt(r);
			if(V.size()>0)
			{
				String item=(String)V.elementAt(RCP_FINALNAME);
				int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
				if((level<=mob.envStats().level())
				&&(replacePercent(item,"").equalsIgnoreCase(recipeName)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to carve a '"+recipeName+"'.  Try \"list\" as your parameter for a list.");
			return false;
		}
		int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
		Item firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_WOODEN);
		int foundWood=0;
		if(firstWood!=null)
			foundWood=findNumberOfResource(mob.location(),firstWood.material());
		if(foundWood==0)
		{
			commonTell(mob,"There is no wood here to make anything from!  It might need to put it down first.");
			return false;
		}
		if(foundWood<woodRequired)
		{
			commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int woodDestroyed=woodRequired;
		for(int i=mob.location().numItems()-1;i>=0;i--)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof EnvResource)
			&&((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
			&&(I.container()==null)
			&&(!Sense.isOnFire(I))
			&&(I.material()==firstWood.material())
			&&((--woodDestroyed)>=0))
				I.destroyThis();
		}
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
		startStr="<S-NAME> start(s) carving "+building.displayName()+".";
		displayText="You are carving "+building.displayName();
		verb="carving "+building.displayName();
		building.setDisplayText(itemName+" is here");
		building.setDescription(itemName+". ");
		building.baseEnvStats().setWeight(woodRequired);
		building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
		building.setMaterial(firstWood.material());
		building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
		String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
		int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
		int canContain=Util.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
		int riders=Util.s_int((String)foundRecipe.elementAt(RCP_NUMRIDERS));
		key=null;
		if(building instanceof Rideable)
		{
			((Rideable)building).setRideBasis(Rideable.RIDEABLE_WAGON);
			((Rideable)building).setRiderCapacity(riders);
		}
		
		if((building instanceof Container)
		&&(!(building instanceof Armor)))
		{
			if(capacity>0)
			{
				((Container)building).setCapacity(capacity+woodRequired);
				((Container)building).setContainTypes(canContain);
			}
			if(misctype.equalsIgnoreCase("LID"))
				((Container)building).setLidsNLocks(true,false,false,false);
			else
			if(misctype.equalsIgnoreCase("LOCK"))
			{
				((Container)building).setLidsNLocks(true,false,true,false);
				((Container)building).setKeyName(new Double(Math.random()).toString());
				key=CMClass.getItem("GenKey");
				((Key)key).setKey(((Container)building).keyName());
				key.setName("a key");
				key.setDisplayText("a small key sits here");
				key.setDescription("looks like a key to "+building.displayName());
				key.recoverEnvStats();
				key.text();
			}
		}
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();


		messedUp=!profficiencyCheck(0,auto);
		if(completion<15) completion=15;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
