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
	public Butchering()
	{
		super();
		displayText="You are skinning and butchering something...";
		verb="skinning and butchering";
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((body!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&((!((MOB)affected).location().isContent(body)))
		&&((!((MOB)affected).isMine(body))))
			unInvoke();
		return super.tick(ticking,tickID);
	}
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
						body.destroy();
					}
					else
					{
						mob.location().show(mob,null,body,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to skin and chop up <O-NAME>.");
						Vector resources=body.charStats().getMyRace().myResources();
						Vector diseases=new Vector();
						for(int i=0;i<body.numEffects();i++)
						{
							Ability A=body.fetchEffect(i);
							if((A!=null)&&(A instanceof DiseaseAffect))
							{
								if((Util.bset(((DiseaseAffect)A).abilityCode(),DiseaseAffect.SPREAD_CONSUMPTION))
								||(Util.bset(((DiseaseAffect)A).abilityCode(),DiseaseAffect.SPREAD_CONTACT)))
									diseases.addElement(A);
							}
						}
						body.emptyPlease();
						body.destroy();
						for(int y=0;y<abilityCode();y++)
						{
							for(int i=0;i<resources.size();i++)
							{
								Item newFound=(Item)((Item)resources.elementAt(i)).copyOf();
								if((newFound instanceof Food)||(newFound instanceof Drink))
								for(int d=0;d<diseases.size();d++)
									newFound.addNonUninvokableEffect((Ability)((Ability)diseases.elementAt(d)).copyOf());
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
		Item I=null;
		if((mob.isMonster()
		&&(!Sense.isAnimalIntelligence(mob)))
		&&(commands.size()==0))
		{
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I2=mob.location().fetchItem(i);
				if((I2!=null)
				&&(I2 instanceof DeadBody)
				&&(Sense.canBeSeenBy(I2,mob))
				&&(I2.container()==null))
				{
					I=I2;
					break;
				}
			}
		}
		else
			I=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);

		if(I==null) return false;
		if((!(I instanceof DeadBody))
		   ||(((DeadBody)I).charStats()==null)
		   ||(((DeadBody)I).charStats().getMyRace()==null))
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
		failed=!profficiencyCheck(mob,0,auto);
		FullMsg msg=new FullMsg(mob,I,this,CMMsg.MSG_NOISYMOVEMENT,CMMsg.MSG_OK_ACTION,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) butchering <T-NAME>.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			body=(DeadBody)I;
			verb="skinning and butchering "+I.name();
			int duration=(I.envStats().weight()/10);
			if(duration<3) duration=3;
			if(duration>40) duration=40;
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
