package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Taxidermy extends CraftingSkill
{
	public String ID() { return "Taxidermy"; }
	public String name(){ return "Taxidermy";}
	private static final String[] triggerStrings = {"STUFF","TAXIDERMY"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String foundShortName="";
	private boolean messedUp=false;
	private static boolean mapped=false;

	public Taxidermy()
	{
		super();
		displayText="You are stuffing...";
		verb="stuffing";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",20,ID(),false);
		}
	}
	public Environmental newInstance(){	return new Taxidermy();}

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


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="stuffing";
		String str=Util.combine(commands,0);
		Item I=mob.location().fetchItem(null,str);
		if((I==null)||(!Sense.canBeSeenBy(I,mob)))
		{
			commonTell(mob,"You don't see anything called '"+str+"' here.");
			return false;
		}
		foundShortName=I.Name();
		if((!(I instanceof DeadBody))||(I.rawSecretIdentity().indexOf("/")<0))
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
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null,0);
		messedUp=!profficiencyCheck(mob,0,auto);
		if(found!=null)	foundShortName=I.Name();
		int duration=15+(woodRequired/3)-mob.envStats().level();
		if(duration>65) duration=65;
		if(duration<10) duration=10;
		found=CMClass.getItem("GenItem");
		found.baseEnvStats().setWeight(woodRequired);
		int x=I.rawSecretIdentity().indexOf("/");
		if(x<0) return false;
		String name=I.rawSecretIdentity().substring(0,x);
		String desc=I.rawSecretIdentity().substring(x+1);
		I.setMaterial(data[0][FOUND_CODE]);
		found.setName("the stuffed body of "+name);
		found.setDisplayText("the stuffed body of "+name+" stands here");
		found.setDescription(desc);
		found.setSecretIdentity("This is the work of "+mob.Name()+".");
		found.recoverEnvStats();
		displayText="You are stuffing "+I.name();
		verb="stuffing "+I.name();
		I.destroy();
		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) stuffing "+I.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}