package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Painting extends CommonSkill
{
	public String ID() { return "Painting"; }
	public String name(){ return "Painting";}
	private static final String[] triggerStrings = {"PAINT","PAINTING"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_CONTAINMASK=8;

	private Item building=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public Painting()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",5,ID(),false);}
	}
	public Environmental newInstance(){	return new Painting();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			if(building==null)
				unInvoke();
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
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,"<S-NAME> completely mess(es) up painting "+building.name()+".");
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
		try{
		if(commands.size()==0)
		{
			commonTell(mob,"Paint on what? Enter \"paint [canvas name]\" or paint \"wall\".");
			return false;
		}
		String str=Util.combine(commands,0);
		building=null;
		messedUp=false;
		Session S=mob.session();
		if((S==null)&&(mob.amFollowing()!=null))
			S=mob.amFollowing().session();
		if(S==null)
		{
			commonTell(mob,"I can't work! I need a player to follow!");
			return false;
		}

		Item I=null;
		if(str.equalsIgnoreCase("wall"))
		{
			if((!ExternalPlay.doesOwnThisProperty(mob,mob.location()))
			&&((mob.amFollowing()==null)||(!ExternalPlay.doesOwnThisProperty(mob.amFollowing(),mob.location()))))
			{
				commonTell(mob,"You need the owners permission to paint the walls here.");
				return false;
			}
		}
		else
		{
			I=mob.location().fetchItem(null,str);
			if((I==null)||(!Sense.canBeSeenBy(I,mob)))
			{
				commonTell(mob,"You don't see any canvases called '"+str+"' sitting here.");
				return false;
			}
			if((I.material()!=EnvResource.RESOURCE_COTTON)
			&&(I.material()!=EnvResource.RESOURCE_SILK)
			&&(!I.Name().toUpperCase().endsWith("CANVAS"))
			&&(!I.Name().toUpperCase().endsWith("SILKSCREEN")))
			{
				commonTell(mob,"You cannot paint on '"+str+"'.");
				return false;
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int completion=25;
		if(str.equalsIgnoreCase("wall"))
		{
			String name=S.prompt("Enter the key words (not the description) for this work.\n\r:","");
			if(name.trim().length()==0) return false;
			Vector V=Util.parse(name.toUpperCase());
			for(int v=0;v<V.size();v++)
			{
				String vstr=" "+((String)V.elementAt(v))+" ";
				for(int i=0;i<mob.location().numItems();i++)
				{
					I=mob.location().fetchItem(i);
					if((I!=null)
					&&(I.displayText().length()==0)
					&&(!I.isGettable())
					&&((" "+I.name().toUpperCase()+" ").indexOf(vstr)>=0))
					{
						if(S.confirm("'"+I.name()+"' already shares one of these key words ('"+vstr.trim().toLowerCase()+"').  Would you like to destroy it (y/N)? ","N"))
						{
							I.destroy();
							return true;
						}
					}
				}
			}
			String desc=S.prompt("Enter a description for this.\n\r:");
			if(desc.trim().length()==0) return false;
			if(!S.confirm("Wall art key words: '"+name+"', description: '"+desc+"'.  Correct (Y/n)?","Y"))
				return false;
			building=CMClass.getItem("GenWallpaper");
			building.setName(name);
			building.setDescription(desc);
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
		}
		else
		{
			String name=S.prompt("In brief, what is this a painting of?\n\r:");
			if(name.trim().length()==0) return false;
			String desc=S.prompt("Please describe this painting.\n\r:");
			if(desc.trim().length()==0) return false;
			building=CMClass.getItem("GenItem");
			building.setName("a painting of "+name);
			building.setDisplayText("a painting of "+name+" is here.");
			building.setDescription(desc);
			building.baseEnvStats().setWeight(I.baseEnvStats().weight());
			building.setBaseValue(I.baseGoldValue()*(Dice.roll(1,5,0)));
			building.setMaterial(I.material());
			building.baseEnvStats().setLevel(I.baseEnvStats().level());
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			I.destroy();
		}
		String startStr="<S-NAME> start(s) painting "+building.name()+".";
		displayText="You are painting "+building.name();
		verb="painting "+building.name();
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();

		messedUp=!profficiencyCheck(0,auto);
		completion=completion-mob.envStats().level()+5;
		if(completion<10) completion=10;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		}catch(java.io.IOException e){return false;}
		return true;
	}
}