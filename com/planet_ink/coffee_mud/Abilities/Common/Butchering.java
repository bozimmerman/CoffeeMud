package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Butchering extends CommonSkill
{
	public String ID() { return "Butchering"; }
	public String name(){ return "Butchering";}
	private static final String[] triggerStrings = {"BUTCHER","BUTCHERING","SKIN"};
	public String[] triggerStrings(){return triggerStrings;}

	private DeadBody body=null;
	private String foundShortName="";
	private boolean failed=false;
	private static boolean mapped=false;
	public Butchering()
	{
		super();
		displayText="You are skinning and butchering something...";
		verb="skinning and butchering";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Butchering();}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((body!=null)&&(!aborted))
				{
					if((failed)||(!mob.location().isContent(body)))
					{
						commonTell(mob,"You messed up your butchering completely.");
						body.destroyThis();
					}
					else
					{
						mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to skin and chop up "+body.name()+".");
						Vector resources=body.charStats().getMyRace().myResources();
						Ability useSpellCast=body.fetchAffect("Prop_UseSpellCast2");
						for(int i=0;i<mob.location().numItems();i++)
						{
							Item I=mob.location().fetchItem(i);
							if((I!=null)&&(I.container()==body))
								I.setContainer(null);
						}
						body.destroyThis();
						for(int y=0;y<usesRemaining();y++)
						{
							for(int i=0;i<resources.size();i++)
							{
								Item newFound=(Item)((Item)resources.elementAt(i)).copyOf();
								if((useSpellCast!=null)
								&&((newFound instanceof Food)||(newFound instanceof Drink)))
									newFound.addNonUninvokableAffect(((Ability)useSpellCast.copyOf()));
								newFound.recoverEnvStats();
								mob.location().addItemRefuse(newFound,Item.REFUSE_RESOURCE);
								mob.location().recoverRoomStats();
							}
						}
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
			commonTell(mob,"You can't butcher "+I.name()+".");
			return false;
		}
		Vector resources=((DeadBody)I).charStats().getMyRace().myResources();
		if((resources==null)||(resources.size()==0))
		{
			commonTell(mob,"There doesn't appear to be any good parts on "+I.name()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		failed=!profficiencyCheck(0,auto);
		FullMsg msg=new FullMsg(mob,I,null,Affect.MSG_NOISYMOVEMENT,Affect.MSG_OK_ACTION,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) butchering <T-NAME>.");
		if(mob.location().okAffect(mob,msg))
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
