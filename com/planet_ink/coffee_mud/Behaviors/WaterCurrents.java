package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WaterCurrents extends ActiveTicker
{
	public String ID(){return "WaterCurrents";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
	protected String dirs="";
	
	public WaterCurrents()
	{
		minTicks=3;maxTicks=5;chance=75;
		tickReset();
	}
	
	public Behavior newInstance()
	{
		return new WaterCurrents();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		Vector V=Util.parse(newParms);
		dirs="";
		for(int v=0;v<V.size();v++)
		{
			int dir=Directions.getGoodDirectionCode((String)V.elementAt(v));
			if(dir>=0) dirs=dirs+Directions.getDirectionChar(dir);
		}
		if(dirs.length()==0)
			dirs="NE";
	}
	public void applyCurrents(Room R, Vector mobsDone)
	{
		if(R.numInhabitants()==0) return;
		for(int m=0;m<R.numInhabitants();m++)
		{
			MOB M=R.fetchInhabitant(m);
			if((M!=null)
			&&(!M.isMonster())
			&&(M.riding()==null)
			&&((!(M instanceof Rideable))||(((Rideable)M).numRiders()==0))
			&&(!M.isInCombat())
			&&(!mobsDone.contains(M)))
				mobsDone.addElement(M);
		}
		if(mobsDone.size()>0)
		{
			int dir=-1;
			Room R2=null;
			for(int dl=0;dl<dirs.length();dl++)
			{
				dir=Directions.getDirectionCode(dirs.charAt(dl));
				if(dir>=0)
				{
					R2=R.getRoomInDir(dir);
					if(R2!=null)
					{
						if((R.getExitInDir(dir)!=null)
						&&(R.getExitInDir(dir).isOpen())
						&&((R2.domainType()==R.domainType())
							||(R2.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
							||(R2.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
							||(R2.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
							||(R2.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)))
								break;
						R2=null;
					}
				}
			}
			if(R2!=null)
				for(int m=0;m<mobsDone.size();m++)
				{
					MOB M=(MOB)mobsDone.elementAt(m);
					R.show(M,null,Affect.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> swept "+Directions.getDirectionName(dir).toLowerCase()+" by the current.");
					R2.bringMobHere(M,false);
					R2.showOthers(M,null,Affect.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> swept in from "+Directions.getFromDirectionName(Directions.getOpDirectionCode(dir)).toLowerCase()+" by the current.");
					ExternalPlay.look(M,null,true);
				}
		}
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(canAct(ticking,tickID))
		{
			Vector sweeps=new Vector();
			if(ticking instanceof Room)
				applyCurrents((Room)ticking,sweeps);
			else
			if(ticking instanceof Area)
			{
				for(Iterator r=((Area)ticking).getMap();r.hasNext();)
				{
					Room R=(Room)r.next();
					if((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
					||(R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
					||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
					||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
						applyCurrents(R,sweeps);
				}
			}
		}
	}
}