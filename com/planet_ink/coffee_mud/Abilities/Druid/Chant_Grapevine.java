package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Grapevine extends Chant
{
	public String ID() { return "Chant_Grapevine"; }
	public String name(){ return "Grapevine";}

	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_Grapevine();}
	Vector myChants=new Vector();

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof Item)
		&&(((Item)affected).owner() instanceof Room)
		&&(((Room)((Item)affected).owner()).isContent((Item)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(invoker!=null)
		&&(((MOB)invoker).location()!=((Room)((Item)affected).owner()))
		&&(msg.othersMessage()!=null))
			((MOB)invoker).executeMsg(invoker,msg);
	}

	public void unInvoke()
	{
		if((affected instanceof MOB)&&(myChants!=null))
		{
			Vector V=myChants;
			myChants=null;
			for(int i=0;i<V.size();i++)
			{
				Ability A=(Ability)V.elementAt(i);
				if((A.affecting()!=null)
				   &&(A.ID().equals(ID()))
				   &&(A.affecting() instanceof Item))
				{
					Item I=(Item)A.affecting();
					I.delEffect(A);
				}
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.fetchEffect(ID())!=null)||(mob.fetchEffect("Chant_TapGrapevine")!=null))
		{
			mob.tell("You are already listening through a grapevine.");
			return false;
		}
		Vector myRooms=Druid_MyPlants.myPlantRooms(mob);
		if((myRooms==null)||(myRooms.size()==0))
		{
			mob.tell("There doesn't appear to be any of your plants around to listen through.");
			return false;
		}
		Item myPlant=Druid_MyPlants.myPlant(mob.location(),mob,0);
		if((!auto)&&(myPlant==null))
		{
			mob.tell("You must be in the same room as one of your plants to initiate this chant.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,myPlant,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF> and listen(s) carefully to <T-HIM-HER>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				myChants=new Vector();
				beneficialAffect(mob,mob,0);
				Chant_Grapevine C=(Chant_Grapevine)mob.fetchEffect(ID());
				if(C==null) return false;
				for(int i=0;i<myRooms.size();i++)
				{
					Room R=(Room)myRooms.elementAt(i);
					int ii=0;
					myPlant=Druid_MyPlants.myPlant(R,mob,ii);
					while(myPlant!=null)
					{
						Ability A=myPlant.fetchEffect(ID());
						if(A!=null) myPlant.delEffect(A);
						myPlant.addNonUninvokableEffect((Ability)C.copyOf());
						A=myPlant.fetchEffect(ID());
						if(A!=null) myChants.addElement(A);
						ii++;
						myPlant=Druid_MyPlants.myPlant(R,mob,ii);
					}
				}
				C.myChants=(Vector)myChants.clone();
				myChants=new Vector();
			}

		}
		else
			beneficialVisualFizzle(mob,myPlant,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
