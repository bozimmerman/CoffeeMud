package com.planet_ink.coffee_mud.interfaces;

import java.util.HashSet;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public interface Rideable extends Environmental
{
	public final static int RIDEABLE_LAND=0;
	public final static int RIDEABLE_WATER=1;
	public final static int RIDEABLE_AIR=2;
	public final static int RIDEABLE_SIT=3;
	public final static int RIDEABLE_SLEEP=4;
	public final static int RIDEABLE_TABLE=5;
	public final static int RIDEABLE_ENTERIN=6;
	public final static int RIDEABLE_LADDER=7;
	public final static int RIDEABLE_WAGON=8;
	public final static String[] RIDEABLE_DESCS=
	{
		"LAND-BASED","WATER-BASED","AIR-FLYING","FURNITURE-SIT","FURNITURE-SLEEP","FURNITURE-TABLE",
		"ENTER-IN","LADDER","WAGON"
	};
	
	public int rideBasis();
	public void setRideBasis(int basis);
	public int riderCapacity();
	public void setRiderCapacity(int newCapacity);
	public int numRiders();
	public Rider fetchRider(int which);
	public void addRider(Rider mob);
	public void delRider(Rider mob);
	public boolean amRiding(Rider mob);
	public String stateString(Rider R);
	public String putString(Rider R);
	public String stateStringSubject(Rider R);
	public boolean mobileRideBasis();
	public String mountString(int commandType, Rider R);
	public String dismountString(Rider R);
	public HashSet getRideBuddies(HashSet list);
}
