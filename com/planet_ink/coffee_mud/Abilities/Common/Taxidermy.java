package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Taxidermy extends CommonSkill
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
		foundShortName=I.name();
		if((!(I instanceof DeadBody))||(I.rawSecretIdentity().indexOf("/")<0))
		{
			commonTell(mob,"You don't know how to stuff "+I.displayName()+".");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if(I2.container()==I)
			{
				commonTell(mob,"You need to remove the contents of "+I2.displayName()+" first.");
				return false;
			}
		}
		int woodRequired=I.baseEnvStats().weight()/5;
		Item firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_CLOTH);
		int foundWood=0;
		if(firstWood!=null)
			foundWood=findNumberOfResource(mob.location(),firstWood.material());
		if(foundWood==0)
		{
			commonTell(mob,"There is no cloth here to stuff anything with!  It might need to put it down first.");
			return false;
		}
		if(foundWood<woodRequired)
		{
			commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to stuff "+I.displayName()+".  There is not enough here.  Are you sure you set it all on the ground first?");
			return false;
		}
		
		found=null;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int woodDestroyed=woodRequired;
		for(int i=mob.location().numItems()-1;i>=0;i--)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2 instanceof EnvResource)
			&&(I2.container()==null)
			&&(!Sense.isOnFire(I2))
			&&(I2.material()==firstWood.material())
			&&((--woodDestroyed)>=0))
				I2.destroyThis();
		}
		messedUp=!profficiencyCheck(0,auto);
		if(found!=null)	foundShortName=I.name();
		int duration=15+(woodRequired/3)-mob.envStats().level();
		if(duration>65) duration=65;
		if(duration<10) duration=10;
		found=CMClass.getItem("GenItem");
		found.baseEnvStats().setWeight(woodRequired);
		int x=I.rawSecretIdentity().indexOf("/");
		if(x<0) return false;
		String name=I.rawSecretIdentity().substring(0,x);
		String desc=I.rawSecretIdentity().substring(x+1);
		I.setMaterial(firstWood.material());
		found.setName("the stuffed body of "+name);
		found.setDisplayText("the stuffed body of "+name+" stands here");
		found.setDescription(desc);
		found.recoverEnvStats();
		displayText="You are stuffing "+I.displayName();
		verb="stuffing "+I.displayName();
		I.destroyThis();
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) stuffing "+I.displayName()+".");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}