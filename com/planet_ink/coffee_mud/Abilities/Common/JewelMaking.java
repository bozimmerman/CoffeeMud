package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class JewelMaking extends CommonSkill
{
	public String ID() { return "JewelMaking"; }
	public String name(){ return "Jewel Making";}
	private static final String[] triggerStrings = {"JEWEL","JEWELMAKING"};
	public String[] triggerStrings(){return triggerStrings;}
	
	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_ARMORDMG=8;
	private static final int RCP_EXTRAREQ=9;
	
	private Item building=null;
	private Item fire=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public JewelMaking()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new JewelMaking();}
	
	public boolean tick(int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(fire==null)
			||(!Sense.isOnFire(fire))
			||(!mob.location().isContent(fire))
			||(mob.isMine(fire)))
				unInvoke();
		}
		return super.tick(tickID);
	}
	
	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("JEWELMAKING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"jewelmaking.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Jewelmaking","Recipes not found!");
			Resources.submitResource("JEWELMAKING RECIPES",V);
		}
		return V;
	}
	
	public void unInvoke()
	{
		if(canBeUninvoked)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,"You've ruined "+building.name()+"!");
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
			commonTell(mob,"Make what? Enter \"jewel list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Metals required\n\r");
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
		fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.container()==null)&&(Sense.isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			commonTell(mob,"A fire will need to be built first.");
			return false;
		}
		building=null;
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
			commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"jewel list\" for a list.");
			return false;
		}
		int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
		String otherRequired=(String)foundRecipe.elementAt(RCP_EXTRAREQ);
		Item firstWood=null;
		Item firstOther=null;
		int foundWood=0;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof EnvResource)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
			{
				if(((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
				   ||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL))
				{
					if(firstWood==null)firstWood=I;
					if(firstWood.material()==I.material())
						foundWood++;
				}
				else
				if((otherRequired.length()>0)
				&&(firstOther==null)
				&&(((EnvResource.MATERIAL_DESCS[(I.material()&EnvResource.MATERIAL_MASK)>>8].equalsIgnoreCase(otherRequired))
				   ||(EnvResource.RESOURCE_DESCS[(I.material()&EnvResource.RESOURCE_MASK)].equalsIgnoreCase(otherRequired)))))
					firstOther=I;
			}
		}
		if(foundWood==0)
		{
			commonTell(mob,"There is no metal here to make anything from!  It might need to put it down first.");
			return false;
		}
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
		if((otherRequired.length()>0)&&(firstOther==null))
		{
			commonTell(mob,"You need some sort of precious stones to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int woodDestroyed=woodRequired;
		for(int i=mob.location().numItems()-1;i>=0;i--)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.container()==null)
			&&(I.material()==firstWood.material())
			&&((--woodDestroyed)>=0))
				I.destroyThis();
		}
		if(firstOther!=null) firstOther.destroyThis();
		building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
			return false;
		}
		completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
		String itemName=null;
		if(firstOther==null)
			itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)]).toLowerCase();
		else
			itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstOther.material()&EnvResource.RESOURCE_MASK)]).toLowerCase();
			
		if(new String("aeiou").indexOf(Character.toLowerCase(itemName.charAt(0)))>=0)
			itemName="an "+itemName;
		else
			itemName="a "+itemName;
		building.setName(itemName);
		startStr="<S-NAME> start(s) making "+building.name()+".";
		displayText="You are making "+building.name();
		verb="making "+building.name();
		building.setDisplayText(itemName+" is here");
		building.setDescription(itemName+". ");
		building.baseEnvStats().setWeight(woodRequired);
		building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(firstWood.baseGoldValue())));
		if(firstOther==null)
			building.setMaterial(firstWood.material());
		else
		{
			building.setMaterial(firstOther.material());
			building.setBaseValue(building.baseGoldValue()+firstOther.baseGoldValue());
		}
		building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
		String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
		//int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
		int armordmg=Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));

		if(building instanceof Armor)
		{
			((Armor)building).baseEnvStats().setArmor(armordmg);
			((Armor)building).setRawProperLocationBitmap(0);
			for(int wo=1;wo<Item.wornLocation.length;wo++)
			{
				String WO=Item.wornLocation[wo].toUpperCase();
				if(misctype.equalsIgnoreCase(WO))
				{
					((Armor)building).setRawProperLocationBitmap(Util.pow(2,wo-1));
					((Armor)building).setRawLogicalAnd(false);
				}
				else
				if((misctype.toUpperCase().indexOf(WO+"||")>=0)
				||(misctype.toUpperCase().endsWith("||"+WO)))
				{
					((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|Util.pow(2,wo-1));
					((Armor)building).setRawLogicalAnd(false);
				}
				else
				if((misctype.toUpperCase().indexOf(WO+"&&")>=0)
				||(misctype.toUpperCase().endsWith("&&"+WO)))
				{
					((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|Util.pow(2,wo-1));
					((Armor)building).setRawLogicalAnd(true);
				}
			}
		}
		
		
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();
		
		
		messedUp=!profficiencyCheck(0,auto);
		if(completion<8) completion=8;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
