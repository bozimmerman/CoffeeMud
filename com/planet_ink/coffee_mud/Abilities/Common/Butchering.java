package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Butchering extends CommonSkill
{
	private DeadBody body=null;
	private String foundShortName="";
	private boolean failed=false;
	public Butchering()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Butchering";

		displayText="You are skinning and butchering something...";
		verb="skinning and butchering";
		miscText="";
		triggerStrings.addElement("BUTCHER");
		triggerStrings.addElement("BUTCHERING");
		triggerStrings.addElement("SKIN");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Butchering();
	}
	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((body!=null)&&(!aborted)&&(mob.location().isContent(body)))
			{
				if(failed)
				{
					mob.tell("You messed up your butchering completely.");
					body.destroyThis();
				}
				else
				{
					mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to skin and chop up "+body.name()+".");
					Vector resources=body.charStats().getMyRace().myResources();
					body.destroyThis();
					for(int i=0;i<resources.size();i++)
					{
						Item newFound=(Item)((Item)resources.elementAt(i)).copyOf();
						newFound.setPossessionTime(Calendar.getInstance());
						newFound.recoverEnvStats();
						mob.location().addItem(newFound);
						mob.location().recoverRoomStats();
					}
				}
			}
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		body=null;
		Item I=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(I==null) return false;
		if(!(I instanceof DeadBody))
		{
			mob.tell("You can't butcher "+I.name()+".");
			return false;
		}
		Vector resources=((DeadBody)I).charStats().getMyRace().myResources();
		if((resources==null)||(resources.size()==0))
		{
			mob.tell("There doesn't appear to be any good parts on "+I.name()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		failed=!profficiencyCheck(0,auto);
		FullMsg msg=new FullMsg(mob,I,null,Affect.MSG_NOISYMOVEMENT,Affect.MSG_OK_ACTION,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) butchering <T-NAME>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			body=(DeadBody)I;
			verb="skinning and butchering "+I.name();
			int duration=(I.envStats().weight()/10);
			if(duration<5) duration=5;
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
