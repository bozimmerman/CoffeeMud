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
public class Chant_WindColor extends Chant
{
	public String ID() { return "Chant_WindColor"; }
	public String name(){ return "Wind Color";}
	public String displayText(){return "(Wind Color)";}
    public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;}
	Room lastRoom=null;

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			lastRoom=null;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your senses are no longer sensitive to the winds.");
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&(affected!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null)
		&&(((MOB)affected).location()!=lastRoom)
		&&((((MOB)affected).location().domainType()&Room.INDOORS)==0)
		&&(((MOB)affected).location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER))
		{
			lastRoom=((MOB)affected).location();
			String prediction=getWindColor((MOB)affected,((MOB)affected).location());
			if(prediction.length()>0) ((MOB)affected).tell("The winds are "+prediction+".");
		}
		return true;
	}

	public static String getColor(int i)
	{
		switch(i)
		{
		case 0: return "black";
		case 1: return "red";
		case 2: return "blue";
		case 3: return "green";
		case 4: return "grey";
		case 5: return "purple";
		case 6: return "yellow";
		case 7: return "brown";
		case 8: return "orange";
		}
		return "";
	}

	public static String getWindColor(MOB mob, Room R)
	{
		if((R==null)||(mob==null)) return "";
		if(R.numInhabitants()==0) return "";
		int sourceCode=-1;
		int levelCode=-1;
		int[] colors=null;
		HashSet group=mob.getGroupMembers(new HashSet());
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if(I!=null)
			{
				int done=0;
				if(colors==null) colors=new int[9];
				if(I.envStats().level()>=(mob.envStats().level()+25))
					levelCode=4;
				else
				if(I.envStats().level()>=(mob.envStats().level()+15))
				{ if (levelCode<3) levelCode=3;}
				else
				if(I.envStats().level()>=(mob.envStats().level()+5))
				{ if (levelCode<2) levelCode=2;}
				else
				if(I.envStats().level()>(mob.envStats().level()-5))
				{ if (levelCode<1) levelCode=1;}
				else
				if(I.envStats().level()>(mob.envStats().level()-15))
				{ if (levelCode<0) levelCode=0;}
				if(CMLib.flags().isHidden(I))
				{ done++; colors[5]++;}
				if(CMLib.flags().isInvisible(I))
				{ done++; colors[6]++;}
				if((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
				{ done++; colors[7]++;}
				if(CMLib.flags().isMetal(I)
				||((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK))
				{ done++; colors[4]++;}
				if(CMLib.utensils().fetchMyTrap(I)!=null)
				{ done++; colors[8]++;}
				if(CMLib.flags().isGood(I)){done++; colors[2]++;}
				else
				if(CMLib.flags().isEvil(I)){done++; colors[1]++;}
				if(done>1)
				{
					if(sourceCode>=0) sourceCode=1;
					else sourceCode=0;
				}
			}
		}
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M!=mob)&&(!group.contains(M)))
			{
				if(colors==null) colors=new int[9];
				if(M.envStats().level()>=(mob.envStats().level()+25))
					levelCode=4;
				else
				if(M.envStats().level()>=(mob.envStats().level()+15))
				{ if (levelCode<3) levelCode=3;}
				else
				if(M.envStats().level()>=(mob.envStats().level()+5))
				{ if (levelCode<2) levelCode=2;}
				else
				if(M.envStats().level()>(mob.envStats().level()-5))
				{ if (levelCode<1) levelCode=1;}
				else
				if(M.envStats().level()>(mob.envStats().level()-15))
				{ if (levelCode<0) levelCode=0;}

				int done=0;
				if(M.charStats().getMyRace().racialCategory().equalsIgnoreCase("Undead"))
				{ done++; colors[0]++;}
				if((M.charStats().getMyRace().ID().equals("StoneGolem"))
				||(M.charStats().getMyRace().ID().equals("MetalGolem")))
				{ done++; colors[4]++;}
				if(CMLib.flags().isHidden(M))
				{ done++; colors[5]++;}
				if(CMLib.flags().isInvisible(M))
				{ done++; colors[6]++;}
				if(M.charStats().getMyRace().ID().equals("WoodGolem"))
				{ done++; colors[7]++;}
				if(done>0)
				{
					if(sourceCode>=0) sourceCode=1;
					else sourceCode=0;
				}
				if(CMLib.flags().isGood(M)) colors[2]++;
				else
				if(CMLib.flags().isEvil(M)) colors[1]++;
				else
				if((!CMLib.flags().isGood(M))&&(!CMLib.flags().isEvil(M))) colors[3]++;
			}
		}
		if(colors==null) return "";
		boolean foundOne=false;
		for(int i=0;i<colors.length;i++)
			if(colors[i]>0){foundOne=true; break;}
		if(!foundOne) return "";

		StringBuffer str=new StringBuffer("");
		switch(sourceCode)
		{
		case 0:
			switch(levelCode)
			{
			case -1:
				str.append("dull stripes of ");
				break;
			case 0:
				str.append("faded stripes of ");
				break;
			case 1:
				str.append("striped ");
				break;
			case 2:
				str.append("brightly striped ");
				break;
			case 3:
				str.append("brilliant stripes of ");
				break;
			case 4:
				str.append("dazzling stripes of ");
				break;
			}
			break;
		case 1:
			switch(levelCode)
			{
			case -1:
				str.append("a swirl of dull ");
				break;
			case 0:
				str.append("a swirl of faded ");
				break;
			case 1:
				str.append("a swirl of ");
				break;
			case 2:
				str.append("a bright swirl of ");
				break;
			case 3:
				str.append("a swirl of brilliant ");
				break;
			case 4:
				str.append("a swirl of dazzling ");
				break;
			}
			break;
		default:
			switch(levelCode)
			{
			case -1:
				str.append("faded ");
				break;
			case 0:
				str.append("faded ");
				break;
			case 1:
				break;
			case 2:
				str.append("bright ");
				break;
			case 3:
				str.append("brilliant ");
				break;
			case 4:
				str.append("dazzling ");
				break;
			}
			break;
		}
		Vector V=new Vector();
		for(int i=0;i<colors.length;i++)
			if(colors[i]>0)
				V.addElement(Integer.valueOf(i));
		if(V.size()==1)
			return str.toString()+getColor(((Integer)V.firstElement()).intValue());
		for(int i=0;i<V.size();i++)
		{
			int x=((Integer)V.elementAt(i)).intValue();
			if(i==V.size()-1) str.append("and "+getColor(x)+" ");
			else
			if(i>0) str.append(", "+getColor(x)+" ");
			else str.append(getColor(x)+" ");
		}
		return str.toString().trim();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already watching the winds.");
			return false;
		}

		if(((target.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			target.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> gain(s) visions of the winds!":"^S<S-NAME> chant(s) for visions on the wind!^?");
			if(mob.location().okMessage(mob,msg))
			{
				lastRoom=null;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the air, but the magic fizzles.");

		return success;
	}
}
