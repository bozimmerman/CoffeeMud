package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Abilities.interfaces.TriggeredAffect;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Common.interfaces.CharStats;
import com.planet_ink.coffee_mud.Common.interfaces.PhyStats;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial;
import com.planet_ink.coffee_mud.Items.interfaces.Weapon;
import com.planet_ink.coffee_mud.Items.interfaces.Wearable;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Races.interfaces.Race;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.interfaces.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/*
   Copyright 2004-2020 Bo Zimmerman

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
public class Prop_SpawnMobFrom extends Property
{
	@Override
	public String ID(){
		return "Prop_SpawnMobFrom";
	}

	@Override
	public String name(){
		return "Spawn Mob";
	}

	@Override
	protected int canAffectCode(){
		return Ability.CAN_MOBS | Ability.CAN_ITEMS;
	}

	private int waitTime = 0;		//Time to wait to spawn the item
	private String mobSummon;		//Mob to summon
	private String summonText;		//Flavor text upon summoning
	private Room location;			//Location of the affected
	private boolean startedTicking = false;	//Prevent starting multiple tickdowns
	private Item thisItem;			//The affected, if item
	Tickable thePackage;			//The ticker


	@Override
	public String accountForYourself(){
		final String id="SpawnItem";
		return id;
	}


	//Set the parameters for the prop, "/" deliminated
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);

		String[] V = newMiscText.split("/");
		waitTime = Integer.parseInt(V[0]);
		mobSummon = V[1];
		summonText = V[2];

	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID){
		return true;
	}


	//Start the tickdown after player enters the room
	private void lightFuse(){
		CMLib.threads().startTickDown(new Tickable(){
			@Override
			public String ID(){
				return "Spawn Item ticker";
			}


			@Override
			public CMObject newInstance(){
				return null;
			}

			@Override
			public CMObject copyOf(){
				return null;
			}


			@Override
			public void initializeClass(){}


			@Override
			public int compareTo(CMObject o){
				return 0;
			}


			@Override
			public String name(){
				return ID();
			}


			@Override
			public int getTickStatus(){
				return 0;
			}


			@Override
			public boolean tick(Tickable ticking, int tickID){

				if(startedTicking){

					waitTime--; //reduce waitTime by 1 after each tick

					//waitTime has elapsed, spawn the item and destroy the affected
					if (waitTime == 0) {

						thePackage = affected;

						if(thePackage instanceof Item) {
							ItemPossessor itemOwner = ((Item) thePackage).owner();

							if(itemOwner instanceof Room){
								location = ((Room) itemOwner);
							}
							else if(itemOwner instanceof MOB){
								location = ((MOB) itemOwner).location();
							}

							mobs(mobSummon,location,summonText);
							((Item)thePackage).destroy();
						}
						else{
							location = ((MOB)thePackage).location();

							mobs(mobSummon,location,summonText);
							((MOB)thePackage).destroy();
						}
						return false;
					}
				}
				return true;
			}
		}, Tickable.TICKID_MOB, 125, waitTime);
	}


	//Check for proper mob in the prop parameters
	private MOB getNewCatalogMob(String mobID){
		
		MOB newMOB=CMLib.catalog().getCatalogMob(mobID);
		
		if(newMOB!=null)
		{
			newMOB=(MOB)newMOB.copyOf();
			try {
				CMLib.catalog().changeCatalogUsage(newMOB,true);
			} catch(final Exception t){}
			
			newMOB.text();
		}
		return newMOB;
	}


	//Spawns the mob in the room of the affected
	public void mobs(String mobName, Room room, String summonWords){


		final String mobID=mobName;
		MOB newMOB=CMClass.getMOB(mobID);

		boolean doGenerica=true;
		
		if(newMOB==null){
			newMOB=getNewCatalogMob(mobID);
			doGenerica=newMOB==null;
		}

		if(newMOB == null){
			final Race R=CMClass.getRace(mobID);
			if(R!=null){
				newMOB = CMClass.getMOB("GenMob");
				newMOB.setName(CMLib.english().startWithAorAn(R.name()));
				newMOB.setDisplayText(L("@x1 is here.",newMOB.Name()));
			}
		}

		if(newMOB==null){
			return;
		}

		if(newMOB.Name().length()==0){
			newMOB.setName(L("A Standard MOB"));
		}
		
		//Set up the new mob parameters and bring to life in the location.
		newMOB.setStartRoom(room);
		newMOB.setLocation(room);
		long rejuv= CMProps.getTicksPerMinute()+CMProps.getTicksPerMinute()+(CMProps.getTicksPerMinute()/2);
		
		if(rejuv>(CMProps.getTicksPerMinute()*20)){
			rejuv=(CMProps.getTicksPerMinute()*20);
		}
		
		newMOB.phyStats().setRejuv((int)rejuv);
		newMOB.baseCharStats().getMyRace().setHeightWeight(newMOB.basePhyStats(),(char)newMOB.baseCharStats().getStat(CharStats.STAT_GENDER));
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(room,true);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.location().show(newMOB,null,CMMsg.MSG_OK_ACTION,L(summonWords));
	}


	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg){
		super.executeMsg(myHost,msg);

		//Parse messages for an ENTER message from an non-archon player
		if((msg.sourceMinor() == CMMsg.TYP_ENTER) && (msg.source().isPlayer() && !CMSecurity.isASysOp(msg.source()))){
			if(!startedTicking){
				startedTicking = true; 
				lightFuse();	//start the tickdown
			}
		}
	}
}
