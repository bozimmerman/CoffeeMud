package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Chant_SummonTornado extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SummonTornado";
	}

	@Override
	public String name()
	{
		return renderedMundane?"tornado":"Summon Tornado";
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Inside a Tornado)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_MOVING|Ability.FLAG_WEATHERAFFECTING;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		 if(mob!=null)
		 {
			 final Room R=mob.location();
			 if(R!=null)
			 {
				 if((R.domainType()&Room.INDOORS)>0)
					 return Ability.QUALITY_INDIFFERENT;
				 final Area A=R.getArea();
				 if((A.getClimateObj().weatherType(mob.location())!=Climate.WEATHER_THUNDERSTORM)
				 &&(A.getClimateObj().weatherType(mob.location())!=Climate.WEATHER_BLIZZARD)
				 &&(A.getClimateObj().weatherType(mob.location())!=Climate.WEATHER_WINDY))
					 return Ability.QUALITY_INDIFFERENT;
				 if(R.fetchEffect(this.ID())!=null)
					 return Ability.QUALITY_INDIFFERENT;
			 }
		 }
		 return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if((mob.location().getArea().getClimateObj().weatherType(mob.location())!=Climate.WEATHER_THUNDERSTORM)
		&&(mob.location().getArea().getClimateObj().weatherType(mob.location())!=Climate.WEATHER_WINDY)
		&&(mob.location().getArea().getClimateObj().weatherType(mob.location())!=Climate.WEATHER_BLIZZARD)
		&&(!auto))
		{
			mob.tell(L("This chant requires wind, a thunderstorm, or a blizzard!"));
			return false;
		}

		final Physical target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(mob,null,null,L("A tornado is already here!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			CMMsg msg = CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto), L((auto?"^JA":"^S<S-NAME> chant(s) to the sky and a")+" tornado touches down!^?")+CMLib.protocol().msp("tornado.wav",40));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Vector<Physical> stuff=new Vector<Physical>();
				for(int i=0;i<mob.location().numItems();i++)
				{
					final Item I=mob.location().getItem(i);
					if((I!=null)&&(I.container()==null)&&(CMLib.flags().isGettable(I)))
						stuff.addElement(I);
				}
				final Set<MOB> H=properTargets(mob,givenTarget,true);
				if(H!=null)
				{
					for (final MOB element : H)
						stuff.addElement(element);
				}
				final Vector<Room> availableRooms=new Vector<Room>();
				availableRooms.addElement(mob.location());
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R=mob.location().getRoomInDir(d);
					final Exit E=mob.location().getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen())
					&&((R.domainType()&Room.INDOORS)==0))
						availableRooms.addElement(R);
				}
				if(stuff.size()==0)
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The tornado dissipates harmlessly."));
				else
				while(stuff.size()>0)
				{
					final Object O=stuff.elementAt(CMLib.dice().roll(1,stuff.size(),-1));
					stuff.removeElement(O);
					final Room R=availableRooms.elementAt(CMLib.dice().roll(1,availableRooms.size(),-1));
					if(O instanceof Item)
					{
						final Item I=(Item)O;
						if(R==mob.location())
							mob.location().show(mob,null,I,CMMsg.MSG_OK_ACTION,L("The tornado picks up <O-NAME> and whisks it around."));
						else
						{
							mob.location().show(mob,null,I,CMMsg.MSG_OK_ACTION,L("The tornado picks up <O-NAME> and whisks it away."));
							R.moveItemTo(I,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
						}
						if(I.subjectToWearAndTear())
						{
							int damage=0;
							switch(I.material()&RawMaterial.MATERIAL_MASK)
							{
							case RawMaterial.MATERIAL_PRECIOUS:
							case RawMaterial.MATERIAL_ROCK:
							case RawMaterial.MATERIAL_MITHRIL:
								damage=1;
								break;
							case RawMaterial.MATERIAL_LIQUID:
							case RawMaterial.MATERIAL_UNKNOWN:
								break;
							case RawMaterial.MATERIAL_GLASS:
								damage=75;
								break;
							case RawMaterial.MATERIAL_CLOTH:
							case RawMaterial.MATERIAL_FLESH:
							case RawMaterial.MATERIAL_LEATHER:
							case RawMaterial.MATERIAL_PAPER:
							case RawMaterial.MATERIAL_VEGETATION:
							case RawMaterial.MATERIAL_WOODEN:
							case RawMaterial.MATERIAL_SYNTHETIC:
								damage=50;
								break;
							case RawMaterial.MATERIAL_METAL:
								damage=20;
								break;
							case RawMaterial.MATERIAL_ENERGY:
								break;
							case RawMaterial.MATERIAL_GAS:
								break;
							}
							if(damage>0)
								CMLib.combat().postItemDamage(mob, I, this, damage, CMMsg.TYP_COLD, null);
						}
					}
					else
					if(O instanceof MOB)
					{
						final MOB M=(MOB)O;
						msg=CMClass.getMsg(M,mob.location(),null,CMMsg.MSG_LEAVE|CMMsg.MASK_ALWAYS,CMMsg.MSG_LEAVE,CMMsg.NO_EFFECT,null);
						final CMMsg msg2=CMClass.getMsg(mob,M,this,verbalCastCode(mob,M,auto),null);
						final CMMsg msg3=CMClass.getMsg(mob,M,this,verbalCastMask(mob,M,auto)|CMMsg.TYP_JUSTICE,null);
						if((mob.location().okMessage(M,msg))
						&&(mob.location().okMessage(mob,msg2))
						&&(mob.location().okMessage(mob,msg3)))
						{
							mob.location().send(mob,msg2);
							mob.location().send(mob,msg3);
							if(R==mob.location())
								mob.location().show(M,null,null,CMMsg.MSG_OK_ACTION,L("The tornado picks <S-NAME> up and whisks <S-HIM-HER> around."));
							else
							{
								mob.location().show(M,null,null,CMMsg.MSG_OK_ACTION,L("The tornado picks <S-NAME> up and whisks <S-HIM-HER> away."));
								R.bringMobHere(M,false);
							}
							final int maxDie=(int)Math.round(CMath.div(adjustedLevel(mob,asLevel),2.0));
							int damage = CMLib.dice().roll(maxDie,7,1);
							if((msg.value()>0)||(msg2.value()>0))
								damage = (int)Math.round(CMath.div(damage,2.0));
							CMLib.combat().postDamage(mob,M,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_GAS,Weapon.TYPE_BASHING,L("The tornado <DAMAGE> <T-NAME>!"));
							//if(R!=mob.location()) M.tell(L("Wait a minute! Where are you?"));
						}
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s) into the sky, but nothing happens."));

		// return whether it worked
		return success;
	}
}
