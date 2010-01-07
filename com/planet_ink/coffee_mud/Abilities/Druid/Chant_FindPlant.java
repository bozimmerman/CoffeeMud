package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

@SuppressWarnings("unchecked")
public class Chant_FindPlant extends Chant
{
	public String ID() { return "Chant_FindPlant"; }
	public String name(){ return "Find Plant";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTCONTROL;}
	public String displayText(){return "(Finding "+lookingFor+")";}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public long flags(){return Ability.FLAG_TRACKING;}
	protected String lookingFor="plants";
	protected Vector theTrail=null;
	protected int nextDirection=-2;
	public int whatImLookingFor=-1;

	private int[] myMats={RawMaterial.MATERIAL_VEGETATION,
						  RawMaterial.MATERIAL_WOODEN};
	protected int[] okMaterials(){	return myMats;}
	private int[] myRscs={RawMaterial.RESOURCE_COTTON,
						  RawMaterial.RESOURCE_HEMP};
	protected int[] okResources(){	return myRscs;}

	protected Vector allResources=null;
	protected Vector allOkResources()
	{
		if(allResources==null)
		{
			allResources=new Vector();
			if(okResources()!=null)
				for(int m=0;m<okResources().length;m++)
					if(!allResources.contains(Integer.valueOf(okResources()[m])))
					   allResources.addElement(Integer.valueOf(okResources()[m]));
			for(int cd : RawMaterial.CODES.ALL())
				if(okMaterials()!=null)
					for(int m=0;m<okMaterials().length;m++)
						if((cd&RawMaterial.MATERIAL_MASK)==okMaterials()[m])
							if(!allResources.contains(Integer.valueOf(cd)))
							   allResources.addElement(Integer.valueOf(cd));
		}
		return allResources;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_MOB)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;

			if(nextDirection==999)
			{
				mob.tell(itsHere(mob,mob.location()));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				if(itsHere(mob,mob.location()).length()==0)
					mob.tell("The trail fizzles out here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("Your sense "+lookingFor+" "+Directions.getInDirectionName(nextDirection)+".");
				nextDirection=-2;
			}

		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(CMLib.flags().canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
			nextDirection=CMLib.tracking().trackNextDirectionFromHere(theTrail,mob.location(),false);
	}

	public String itsHere(MOB mob, Room R)
	{
		if(R==null) return "";
		Room room=R;
		if(room.myResource()==whatImLookingFor)
			return "There seems to be "+lookingFor+" around here.\n\r";
		return "";
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already trying to "+name());
			return false;
		}
		Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
		for(int v=0;v<V.size();v++)	((Ability)V.elementAt(v)).unInvoke();

        if((commands.size()==0)&&(text().length()>0))
            commands.addElement(text());
		if(commands.size()==0)
		{
			mob.tell("Find which "+lookingFor+"?  Use 'CHANT \""+name()+"\" LIST' for a list.");
			return false;
		}
		String s=CMParms.combine(commands,0);
		if(s.equalsIgnoreCase("LIST"))
		{
			StringBuffer msg=new StringBuffer("You may search for any of the following: ");
			for(int i=0;i<allOkResources().size();i++)
				msg.append(RawMaterial.CODES.NAME(((Integer)allOkResources().elementAt(i)).intValue()).toLowerCase()+", ");
			mob.tell(msg.substring(0,msg.length()-2));
			return false;
		}
		whatImLookingFor=-1;
		for(int i=0;i<allOkResources().size();i++)
		{
			int c=((Integer)allOkResources().elementAt(i)).intValue();
			String d=RawMaterial.CODES.NAME(c);
			if(d.equalsIgnoreCase(s))
			{
				lookingFor=d.toLowerCase();
				whatImLookingFor=c;
				break;
			}
		}
		if(whatImLookingFor<0)
		{
			mob.tell("'"+s+"' cannot be found with this chant.    Use 'CHANT \""+name()+"\" LIST' for a list.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		String here=itsHere(target,target.location());
		if(here.length()>0)
		{
			target.tell(here);
			return true;
		}

		boolean success=proficiencyCheck(mob,0,auto);

		Vector rooms=new Vector();
		TrackingLibrary.TrackingFlags flags;
		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.add(TrackingLibrary.TrackingFlag.NOAIR)
				.add(TrackingLibrary.TrackingFlag.NOWATER);
		Vector checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,50);
		for(Enumeration r=checkSet.elements();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(itsHere(target,R).length()>0)
				rooms.addElement(R);
		}

		flags = new TrackingLibrary.TrackingFlags()
				.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
				.add(TrackingLibrary.TrackingFlag.NOAIR)
				.add(TrackingLibrary.TrackingFlag.NOWATER);
		if(rooms.size()>0)
			theTrail=CMLib.tracking().findBastardTheBestWay(mob.location(),rooms,flags,50);

		if((success)&&(theTrail!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> begin(s) to "+name().toLowerCase()+"s!":"^S<S-NAME> chant(s) for "+lookingFor+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Chant_FindPlant newOne=(Chant_FindPlant)this.copyOf();
				if(target.fetchEffect(newOne.ID())==null)
					target.addEffect(newOne);
				target.recoverEnvStats();
				newOne.nextDirection=CMLib.tracking().trackNextDirectionFromHere(newOne.theTrail,target.location(),false);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s), but gain(s) nothing from it.");

		return success;
	}
}
