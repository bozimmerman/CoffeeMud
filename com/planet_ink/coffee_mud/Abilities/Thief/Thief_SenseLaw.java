package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_SenseLaw extends ThiefSkill
{
	public String ID() { return "Thief_SenseLaw"; }
	public String name(){ return "Sense Law";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Thief_SenseLaw();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public static final Vector empty=new Vector();
	protected Room oldroom=null;
	protected String lastReport="";
	
	public Vector getLawMen(Room room, Behavior B)
	{
		if(room==null) return empty;
		if(room.numInhabitants()==0) return empty;
		if(B==null) return empty;
		Vector V=new Vector();
		for(int m=0;m<room.numInhabitants();m++)
		{
			MOB M=(MOB)room.fetchInhabitant(m);
			if((M!=null)&&(M.isMonster())&&(B.modifyBehavior(M,null)))
				V.addElement(M);
		}
		return V;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.location()!=null)&&(!mob.isMonster()))
			{
				Behavior B=mob.location().getArea().fetchBehavior("Arrest");
				if(B==null)
					return super.tick(ticking,tickID);
				StringBuffer buf=new StringBuffer("");
				Vector V=getLawMen(mob.location(),B);
				for(int l=0;l<V.size();l++)
				{
					MOB M=(MOB)V.elementAt(l);
					if(Sense.canBeSeenBy(M,mob))
						buf.append(M.displayName()+" is an officer of the law.  ");
					else
						buf.append("There is an officer of the law here.  ");
				}
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=mob.location().getRoomInDir(d);
					Exit E=mob.location().getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen()))
					{
						V=getLawMen(R,B);
						if((V!=null)&&(V.size()>0))
							buf.append("There is an officer of the law "+Directions.getInDirectionName(d)+".  ");
					}
				}
				if((buf.length()>0)
				&&(profficiencyCheck(0,false))
				&&((mob.location()!=oldroom)||(!buf.toString().equals(lastReport))))
				{
					mob.tell("You sense: "+buf.toString());
					oldroom=mob.location();
					helpProfficiency(mob);
					lastReport=buf.toString();
				}
			}
		}
		return super.tick(ticking,tickID);
	}
	
	public boolean autoInvocation(MOB mob)
	{
		if(mob.charStats().getCurrentClass().ID().equals("Archon"))
			return false;
		return super.autoInvocation(mob);
	}
}
