package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Speculate extends CommonSkill
{
	public String ID() { return "Speculate"; }
	public String name(){ return "Speculating";}
	private static final String[] triggerStrings = {"SPECULATE","SPECULATING"};
	public String[] triggerStrings(){return triggerStrings;}
	private static boolean mapped=false;
	
	private boolean success=false;
	public Speculate()
	{
		super();
		displayText="You are speculating...";
		verb="speculating";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",10,ID(),false);}
	}
	public Environmental newInstance(){	return new Speculate();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(success==false)
				{
					StringBuffer str=new StringBuffer("Your speculation attempt failed.\n\r");
					commonTell(mob,str.toString());
					unInvoke();
				}

			}
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
				Room room=mob.location();
				if((success)&&(!aborted)&&(room!=null))
				{
					int resource=room.myResource()&EnvResource.RESOURCE_MASK;
					if((resource>0)&&(resource<EnvResource.RESOURCE_DESCS.length))
					{
						StringBuffer str=new StringBuffer("");
						String resourceStr=EnvResource.RESOURCE_DESCS[resource];
						str.append("You think this spot would be good for "+resourceStr.toLowerCase()+".\n\r");
						for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
						{
							Room room2=room.getRoomInDir(d);
							if((room2!=null)
							&&(room.getExitInDir(d)!=null)
							&&(room.getExitInDir(d).isOpen()))
							{
								resource=room2.myResource()&EnvResource.RESOURCE_MASK;
								if((resource>0)&&(resource<EnvResource.RESOURCE_DESCS.length))
								{
									resourceStr=EnvResource.RESOURCE_DESCS[resource];
									str.append("There looks like "+resourceStr.toLowerCase()+" "+Directions.getInDirectionName(d)+".\n\r");
								}
							}
						}
						commonTell(mob,str.toString());
					}
					else
						commonTell(mob,"You don't find any good resources around here.");
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="speculating";
		success=false;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		if(profficiencyCheck(0,auto))
			success=true;
		int duration=45-mob.envStats().level();
		if(duration<5) duration=5;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) speculating on this area.");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}