package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class PlantLore extends CommonSkill
{
	public String ID() { return "PlantLore"; }
	public String name(){ return "Plant Lore";}
	private static final String[] triggerStrings = {"PLANTLORE","PSPECULATE"};
	public String[] triggerStrings(){return triggerStrings;}
	private static boolean mapped=false;
	
	private boolean success=false;
	public PlantLore()
	{
		super();
		displayText="You are observing plant growth...";
		verb="observing plant growths";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("Archon",10,ID(),false);}
	}
	public Environmental newInstance(){	return new PlantLore();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if(tickUp==6)
			{
				if(success==false)
				{
					StringBuffer str=new StringBuffer("Your growth observation attempt failed.\n\r");
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
					if((room.domainType()&Room.INDOORS)==0)
					{
						StringBuffer str=new StringBuffer("");
						Vector V=new Vector();
						SaucerSupport.getRadiantRooms(room,V,true,true,false,null,2);
						for(int v=0;v<V.size();v++)
						{
							Room R=(Room)V.elementAt(v);
							int material=R.myResource()&EnvResource.MATERIAL_MASK;
							int resource=R.myResource()&EnvResource.RESOURCE_MASK;
							if((resource<0)||(resource>=EnvResource.RESOURCE_DESCS.length))
								continue;
							if((material!=EnvResource.MATERIAL_VEGETATION)
							&&(material!=EnvResource.MATERIAL_WOODEN))
								continue;
							String resourceStr=EnvResource.RESOURCE_DESCS[resource];
							if(R==room)
								str.append("You think this spot would be good for "+resourceStr.toLowerCase()+".\n\r");
							else
							{
								int isAdjacent=-1;
								for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
								{
									Room room2=room.getRoomInDir(d);
									if(room2==R) isAdjacent=d;
								}
								if(isAdjacent>=0)
									str.append("There looks like "+resourceStr.toLowerCase()+" "+Directions.getInDirectionName(isAdjacent)+".\n\r");
								else
								{
									int d=SaucerSupport.radiatesFromDir(R,V);
									if(d>=0)
									{
										d=Directions.getOpDirectionCode(d);
										str.append("There looks like "+resourceStr.toLowerCase()+" far "+Directions.getInDirectionName(d)+".\n\r");
									}
								}

							}
						}
						commonTell(mob,str.toString());
					}
					else
						commonTell(mob,"You don't find any good plant life around here.");
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="observing plant growth";
		success=false;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		if(profficiencyCheck(0,auto))
			success=true;
		int duration=45-mob.envStats().level();
		if(duration<5) duration=5;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) observing the growth in this area.");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}