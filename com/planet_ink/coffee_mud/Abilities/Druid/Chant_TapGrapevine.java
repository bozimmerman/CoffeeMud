package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_TapGrapevine extends Chant
{
	public String ID() { return "Chant_TapGrapevine"; }
	public String name(){ return "Tap Grapevine";}

	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_TapGrapevine();}
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

	public static Ability isPlant(Item I)
	{
		if((I!=null)&&(I.rawSecretIdentity().length()>0))
		{
			for(int a=0;a<I.numEffects();a++)
			{
				Ability A=I.fetchEffect(a);
				if((A!=null)
				&&(A.invoker()!=null)
				&&(A instanceof Chant_SummonPlants))
					return A;
			}
		}
		return null;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.fetchEffect(ID())!=null)||(mob.fetchEffect("Chant_Grapevine")!=null))
		{
			mob.tell("You are already listening through a grapevine.");
			return false;
		}
		MOB tapped=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(isPlant(I)!=null))
			{
				Ability A=isPlant(I);
				if((A!=null)&&(A.invoker()!=mob))
					tapped=A.invoker();
			}
		}

		Vector myRooms=(tapped==null)?null:Druid_MyPlants.myPlantRooms(tapped);
		if((myRooms==null)||(myRooms.size()==0))
		{
			mob.tell("There doesn't appear to be any plants around here to listen through.");
			return false;
		}
		Item myPlant=Druid_MyPlants.myPlant(mob.location(),tapped,0);
		if((!auto)&&(myPlant==null))
		{
			mob.tell("You must be in the same room as someone elses plants to initiate this chant.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,myPlant,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF> and listen(s) carefully to <T-HIM-HER>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				myChants=new Vector();
				beneficialAffect(mob,mob,0);
				Chant_TapGrapevine C=(Chant_TapGrapevine)mob.fetchEffect(ID());
				if(C==null) return false;
				for(int i=0;i<myRooms.size();i++)
				{
					Room R=(Room)myRooms.elementAt(i);
					int ii=0;
					myPlant=Druid_MyPlants.myPlant(R,tapped,ii);
					while(myPlant!=null)
					{
						Ability A=myPlant.fetchEffect(ID());
						if(A!=null) myPlant.delEffect(A);
						myPlant.addNonUninvokableEffect((Ability)C.copyOf());
						A=myPlant.fetchEffect(ID());
						if(A!=null) myChants.addElement(A);
						ii++;
						myPlant=Druid_MyPlants.myPlant(R,tapped,ii);
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
