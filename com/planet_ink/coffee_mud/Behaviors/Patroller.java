package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Patroller extends ActiveTicker
{
	public String ID(){return "Patroller";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public long flags(){return Behavior.FLAG_MOBILITY;}

	private int step=0;

	public Patroller()
	{
		super();
		minTicks=5; maxTicks=10; chance=100;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new Patroller();
	}

	private Vector getSteps()
	{
		Vector V=new Vector();
		String path=getParms().trim();
		int x=path.indexOf(";");
		if(x<0) return V;
		path=path.substring(x+1).trim();
		x=path.indexOf(";");
		String s=null;
		while(x>=0)
		{
			s=path.substring(0,x).trim();
			if(s.length()>0) V.addElement(s);
			path=path.substring(x+1).trim();
			x=path.indexOf(";");
		}
		if(path.length()>0)
			V.addElement(path);
		if(V.size()>1)
			for(int i=V.size()-1;i>=0;i--)
			{
				s=(String)V.elementAt(i);
				if(s.equalsIgnoreCase("RESTART")) break;
				int dir=Directions.getGoodDirectionCode(s);
				if(dir>=0)
					V.addElement(Directions.getDirectionName(Directions.getOpDirectionCode(dir)));
				else
				if(i<(V.size()-1))
					V.addElement(V.elementAt(i));
			}
		return V;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			// ridden things dont wander!
			MOB mob=(MOB)ticking;
			if(((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0))
			||((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location())))
				return true;

			Room thisRoom=mob.location();
			Room thatRoom=null;
			if(thisRoom instanceof GridLocale)
			{
				Vector V=((GridLocale)thisRoom).getAllRooms();
				Room R=(Room)(V.elementAt(Dice.roll(1,V.size(),-1)));
				if(R!=null) R.bringMobHere(mob,true);
				thisRoom=mob.location();
			}
			Vector steps=getSteps();
			if(steps.size()==0) return true;
			if((step<0)||(step>=steps.size())) step=0;
			String nxt=(String)steps.elementAt(step);

			if((nxt.equalsIgnoreCase("RESTART"))&&(step>0))
			{
				step=0;
				nxt=(String)steps.elementAt(step);
			}

			if(nxt.equalsIgnoreCase("."))
			{
				step++;
				return true;
			}

			int direction=Directions.getGoodDirectionCode(nxt);
			if(direction<0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=thisRoom.getRoomInDir(d);
					if((R!=null)&&(R.roomID().toUpperCase().endsWith(nxt.toUpperCase())))
					{
						thatRoom=R;
						direction=d;
						break;
					}
				}
			else
				thatRoom=thisRoom.getRoomInDir(direction);

			if((direction<0)||(thatRoom==null))
				return true;
			Exit E=thisRoom.getExitInDir(direction);
			if(E==null) return true;

			boolean move=true;
			for(int m=0;m<thisRoom.numInhabitants();m++)
			{
				MOB inhab=thisRoom.fetchInhabitant(m);
				if((inhab!=null)&&(inhab.isASysOp(thisRoom)))
					move=false;
			}
			if(move)
			{
				// handle doors!
				if(E.hasADoor()&&(!E.isOpen()))
				{
					if((E.hasALock())&&(E.isLocked()))
					{
						FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
						if(mob.location().okAffect(mob,msg))
						{
							msg=new FullMsg(mob,E,null,Affect.MSG_OK_VISUAL,Affect.MSG_UNLOCK,Affect.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
							ExternalPlay.roomAffectFully(msg,thisRoom,direction);
						}
					}
					FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
					if(mob.location().okAffect(mob,msg))
					{
						msg=new FullMsg(mob,E,null,Affect.MSG_OK_VISUAL,Affect.MSG_OPEN,Affect.MSG_OK_VISUAL,"<S-NAME> "+E.openWord()+"(s) <T-NAMESELF>.");
						ExternalPlay.roomAffectFully(msg,thisRoom,direction);
					}
				}
				if(!E.isOpen()) return true;

				Ability A=mob.fetchAbility("Thief_Sneak");
				if(A!=null)
				{
					Vector V=new Vector();
					V.add(Directions.getDirectionName(direction));
					if(A.profficiency()<50)
					{
						A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
						Ability A2=mob.fetchAbility("Thief_Hide");
						if(A2!=null)
							A2.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
					}
					int oldMana=mob.curState().getMana();
					A.invoke(mob,V,null,false);
					mob.curState().setMana(oldMana);
				}
				else
					ExternalPlay.move(mob,direction,false,false);

				if(mob.location()==thatRoom)
					step++;
				else
				if(mob.location()==thisRoom)
					tickDown=0;
			}
		}
		return true;
	}
}
