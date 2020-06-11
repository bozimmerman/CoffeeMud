package com.planet_ink.coffee_mud.Commands;

import com.planet_ink.coffee_mud.Areas.interfaces.Area;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Common.interfaces.CharStats;
import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Races.interfaces.Race;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.collections.XVector;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;
import com.planet_ink.coffee_mud.core.interfaces.Rideable;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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


public class Invasion extends StdCommand
{
	public Invasion(){}

	private final String[] access=I(new String[]{"Invasion"});
	@Override public String[] getAccessWords(){return access;}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);

		//MOB is not an Archon
		if(!CMSecurity.isASysOp(mob){
			mob.tell("Huh?");
			return false;
		}

		final Room R=mob.location();
		
		if(R==null){
			return false;
		}

		String commandType = "";

		//Get first part of the Invasion command
		if(commands.size() > 1) {
			commandType=commands.get(1).toUpperCase();
		}

		//Either list or stop invasion in progress
		if(commandType.length()>0) {
			
			switch(commandType){
				case "LIST":
					mob.session().println(listTicks(mob.session(), "Invasion: ").toString());
					break;
				case "STOP":
					stopInvasion(CMParms.combine(commands,2), mob);
					break;
				default:
					mob.tell("Invalid invasion command. Try LIST to list current invasions or STOP 'invasion name'.");
					break;
			}
			return false;
		}

		//Create a new invasion
		String a = mob.session().prompt("Enter mob name to summon:", "Q");

		//Quit the invasion creation
		if(a.equalsIgnoreCase("Q")){
			return false;
		}

		//Check catalog for appropriate mob
		MOB newMob = getMOB(a,mob);

		if(newMob == null){
			mob.tell("Invalid mob. That mob does not exist in the catalog.");
			return false;
		}

		String b =  mob.session().prompt("Enter number of mobs to summon (1-100):", "Q");

		if(b.equalsIgnoreCase("Q")){
			return false;
		}

		try {
			Integer.parseInt(b);
		} catch(NumberFormatException e) {
			mob.tell("Invalid entry. Enter a valid integer.");
			return false;
		} catch(NullPointerException e) {
			mob.tell("Invalid entry. Enter a valid integer.");
			return false;
		}

		//Number of mobs to summon per wave.
		int numMob = Integer.parseInt(b);

		if(numMob <= 0 || numMob >= 100){
			mob.tell(L("Number of mobs must be greater than 0 and less than 100."));
			return false;
		}

		String c = mob.session().prompt("Enter number of waves to summon (1-100):", "Q");

		if(c.equalsIgnoreCase("Q")){
			return false;
		}

		try {
			Integer.parseInt(c);
		} catch(NumberFormatException e) {
			mob.tell("Invalid entry. Enter a valid integer.");
			return false;
		} catch(NullPointerException e) {
			mob.tell("Invalid entry. Enter a valid integer.");
			return false;
		}

		int numWaves = Integer.parseInt(c);

		//Number of waves to spawn
		if(numWaves <= 0 || numWaves >= 100){
			mob.tell(L("Number of waves must be greater than 0 and less than 100."));
			return false;
		}

		String d = mob.session().prompt("Enter time between waves (1-600 seconds):", "Q");

		if(d.equalsIgnoreCase("Q")){
			return false;
		}

		try {
			Integer.parseInt(d);
		} catch(NumberFormatException e) {
			mob.tell("Invalid entry. Enter a valid integer.");
			return false;
		} catch(NullPointerException e) {
			mob.tell("Invalid entry. Enter a valid integer.");
			return false;
		}

		int waveTime= Integer.parseInt(d);

		if(waveTime <= 0 || waveTime >= 601){
			mob.tell(L("waveTime must be greater than 0 and less than 601."));
			return false;
		}

		String f = mob.session().prompt("Enter summon flavor text with '@x1' replacing the mobs name:", "@x1 steps out of a swirling dark portal.");

		//Get the area of the Archon for the invasion and populate a room list to spawn in
		final Room mobRoom = mob.location();
		final Area A=mobRoom.getArea();
		final Vector<Room> candidates=new Vector<Room>();
		
		//Add all rooms in area to the vector
		if(A!=null){
			candidates.addAll(new XVector<Room>(A.getProperMap()));
		}
		
		//Iterate through rooms in the area and leave in vector if accessible
		for(int e=candidates.size()-1;e>=0;e--){
			if(!CMLib.flags().canAccess(mob,candidates.elementAt(e)))
				candidates.removeElementAt(e);
		}

		//Invalid area
		if(candidates.size()==0){
			mob.tell(L("You don't know of an area called '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		//Spawn the first wave of mobs in random rooms in the area.
		int tries1=0;

		while(tries < numMob){
			Room newRoom=null;
			newRoom=candidates.elementAt(CMLib.dice().roll(1,candidates.size(),-1));
			if(((newRoom.roomID().length()==0)){
				newRoom=null;
				continue;
			}
			try {
				mobs(mob, a, newRoom, f); //spawn the mob in the room.
			} catch(Exception e){
			}
			tries++;
		}

		//Create the tickable which will spawn the rest of the waves of mobs
		CMLib.threads().startTickDown(new Tickable()
		{
			@Override
			public String ID(){
				return "Invasion: " + newMob.name();
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
			public void initializeClass(){
			}

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

			int numTicks = numWaves - 1;  //first wave was already spawned

			@Override
			public boolean tick(Tickable ticking, int tickID){

				if(numTicks == 0){
					return false;
				}

				numTicks--;
				
				int tries=0;

				while(tries < numMob){
					Room newRoom=null;
					newRoom=candidates.elementAt(CMLib.dice().roll(1,candidates.size(),-1));
					if(((newRoom.roomID().length()==0)){
						newRoom=null;
						continue;
					}

					try {
						mobs(mob, a, newRoom, f); //spawn the mob in the room.
					} catch(Exception e){

					}
					tries++;
				}
				return true;
			}
		}, Tickable.TICKID_AREA, waveTime * 125, numWaves - 1);

		return true;
	}


	//Spawns the mob in the given room
	public void mobs(MOB mob, String mobName, Room room, String summonWords)
			throws IOException
	{

		final String mobID=mobName;

		MOB newMOB=CMClass.getMOB(mobID);

		boolean doGenerica=true;

		if(newMOB==null)
		{
			newMOB=getNewCatalogMob(mobID);
			doGenerica=newMOB==null;
		}

		if(newMOB == null)
		{
			final Race R=CMClass.getRace(mobID);
			if(R!=null)
			{
				newMOB = CMClass.getMOB("GenMob");
				newMOB.setName(CMLib.english().startWithAorAn(R.name()));
				newMOB.setDisplayText(L("@x1 is here.",newMOB.Name()));
			}
		}

		if(newMOB==null)
		{
			mob.tell(L("There's no such thing as a '@x1'.\n\r",mobID));
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
		newMOB.location().show(newMOB,null,CMMsg.MSG_OK_ACTION,L(summonWords,newMOB.name()));
	}


	//Validate the mob to summon is in the catalog
	public MOB getMOB (String mobToSummon, MOB mob)
	{
		final String mobID=mobToSummon;
		MOB newMOB=CMClass.getMOB(mobID);

		boolean doGenerica=true;
		if(newMOB==null)
		{
			newMOB=getNewCatalogMob(mobID);
			doGenerica=newMOB==null;
		}

		if(newMOB == null)
		{
			final Race R=CMClass.getRace(mobID);
			if(R!=null)
			{
				newMOB = CMClass.getMOB("GenMob");
				newMOB.setName(CMLib.english().startWithAorAn(R.name()));
				newMOB.setDisplayText(L("@x1 is here.",newMOB.Name()));
			}
		}

		if(newMOB==null)
		{
			mob.tell(L("There's no such thing as a '@x1'.\n\r",mobID));
			return null;
		}

		return newMOB;
	}


	// returns a copy of the mob based on ID provided
	private MOB getNewCatalogMob(String mobID)

	{
		MOB newMOB=CMLib.catalog().getCatalogMob(mobID);
		if(newMOB!=null)
		{
			newMOB=(MOB)newMOB.copyOf();
			try { CMLib.catalog().changeCatalogUsage(newMOB,true);} catch(final Exception t){}
			newMOB.text();
		}
		return newMOB;
	}


	//Stops the ticking for the invasion name provided
	private void stopInvasion(String whichInvasion, MOB mob)
	{
		final String which = "Invasion: " + whichInvasion;
		
		List<Tickable> V=null;
		if(which.length()>0)
		{
			V=CMLib.threads().getNamedTickingObjects(which);
			if(V.size()==0)
				V=null;
		}
		if(V==null)
			mob.tell(L("Please enter a valid invasion name to stop.  Use invasion list for a list of current invasions."));
		else
		{
			final StringBuffer list=new StringBuffer("");
			for(int v=0;v<V.size();v++)
				list.append(V.get(v).name()+", ");
			if((mob.session()!=null))
			{
				for(int v=0;v<V.size();v++)
					CMLib.threads().deleteTick(V.get(v),-1); //delete the appropriate tick
			}
		}
	}

	//gets a list of the ticking invasions
	public StringBuilder listTicks(Session viewerS, String whichGroupStr)
	{
		final StringBuilder msg=new StringBuilder("\n\r");
		boolean activeOnly=false;
		String mask=null;
		Set<Pair<Integer,Integer>> whichTicks=null;
		Set<Integer> whichGroups=null;
		final int x=whichGroupStr.lastIndexOf(' ');
		String finalCol="tickercodeword";
		String finalColName="Status";
		
		if(x>0)
		{
			String lastWord=whichGroupStr.substring(x+1).trim().toLowerCase();
			final String[] validCols={"tickername","tickerid","tickerstatus","tickerstatusstr","tickercodeword","tickertickdown","tickerretickdown","tickermillitotal","tickermilliavg","tickerlaststartmillis","tickerlaststopmillis","tickerlaststartdate","tickerlaststopdate","tickerlastduration","tickersuspended"};
			final int y=CMParms.indexOf(validCols,lastWord);
			if(y>=0){
				finalCol=lastWord;
			}
			else{
				for(final String w : validCols){
					if(w.endsWith(lastWord)){
						lastWord=w;
						finalCol=lastWord;
					}
				}
			}
			if(!finalCol.equals(lastWord)){
				return new StringBuilder("Invalid column: '"+lastWord+"'.  Valid cols are: "+CMParms.toListString(validCols));
			}
			else{
				whichGroupStr=whichGroupStr.substring(0,x).trim();
				finalColName=finalCol;
				if(finalColName.startsWith("ticker"))
					finalColName=finalColName.substring(6);
				if(finalColName.startsWith("milli"))
					finalColName="ms"+finalColName.substring(5);
				finalColName=CMStrings.limit(CMStrings.capitalizeAndLower(finalColName),5);
			}
		}

		if("ACTIVE".startsWith(whichGroupStr.toUpperCase())&&(whichGroupStr.length()>0)){
			activeOnly=true;
		}
		else
		if("PROBLEMS".startsWith(whichGroupStr.toUpperCase())&&(whichGroupStr.length()>0)){
			whichTicks=new HashSet<Pair<Integer,Integer>>();
			final String problemSets=CMLib.threads().systemReport("tickerProblems");
			final List<String> sets=CMParms.parseSemicolons(problemSets, true);
			for(final String set : sets){
				final List<String> pair=CMParms.parseCommas(set, true);
				if(pair.size()==2)
					whichTicks.add(new Pair<Integer,Integer>(Integer.valueOf(CMath.s_int(pair.get(0))), Integer.valueOf(CMath.s_int(pair.get(1)))));
			}
		}
		else
		if(CMath.isInteger(whichGroupStr)&&(whichGroupStr.length()>0)){
			whichGroups=new HashSet<Integer>();
			whichGroups.add(Integer.valueOf(CMath.s_int(whichGroupStr)));
		}
		else
		if(whichGroupStr.length()>0){
			mask=whichGroupStr.toUpperCase().trim();
		}
		final int COL_LEN1=CMLib.lister().fixColWidth(4.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(20.0,viewerS);
		final int COL_LEN3=CMLib.lister().fixColWidth(3.0,viewerS);
		final int COL_LEN4=CMLib.lister().fixColWidth(8.0,viewerS);
		msg.append(CMStrings.padRight(L("Current Invasions:"),COL_LEN2)+"\n\n\r");
		int col=0;
		final int numGroups=CMath.s_int(CMLib.threads().tickInfo("tickGroupSize"));
		if((mask!=null)&&(mask.length()==0))
			mask=null;
		String chunk=null;
		for(int group=0;group<numGroups;group++){
			if((whichGroups==null)||(whichGroups.contains(Integer.valueOf(group)))){
				final int tickersSize=CMath.s_int(CMLib.threads().tickInfo("tickersSize"+group));
				for(int tick=0;tick<tickersSize;tick++){
					if((whichTicks==null)||(whichTicks.contains(new Pair<Integer,Integer>(Integer.valueOf(group), Integer.valueOf(tick))))){
						final long tickerlaststartdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststartmillis"+group+"-"+tick));
						final long tickerlaststopdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststopmillis"+group+"-"+tick));
						final boolean isActive=(tickerlaststopdate<tickerlaststartdate);
						if((!activeOnly)||(isActive)){
							final String name=CMLib.threads().tickInfo("tickerName"+group+"-"+tick);
							if((mask==null)||(name.toUpperCase().indexOf(mask)>=0)){
								final String id=CMLib.threads().tickInfo("tickerID"+group+"-"+tick);
								final String status=CMLib.threads().tickInfo(finalCol+group+"-"+tick);
								final boolean suspended=CMath.s_bool(CMLib.threads().tickInfo("tickerSuspended"+group+"-"+tick));
								if(((col++)>=2)||(activeOnly)){
									msg.append("\n\r");
									col=1;
								}
								final String name1=name.substring(10);
								chunk=CMStrings.padRight(name1,COL_LEN2);
								msg.append(chunk);
							}
						}
					}
				}
			}
		}
		return msg;
	}
}
