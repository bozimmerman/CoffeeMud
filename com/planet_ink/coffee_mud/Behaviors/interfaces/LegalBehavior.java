package com.planet_ink.coffee_mud.Behaviors.interfaces;
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
   Copyright 2000-2007 Bo Zimmerman

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
public interface LegalBehavior extends Behavior
{
    public static final long ONE_REAL_DAY=(long)1000*60*60*24;
    public static final long EXPIRATION_MILLIS=ONE_REAL_DAY*7; // 7 real days
    public static final long CONTROLTIME=ONE_REAL_DAY*3;

    public boolean isStillACrime(LegalWarrant W, boolean debugging);
    public boolean fillOutWarrant(MOB mob,
                                  Law laws,
                                  Area myArea,
                                  Environmental target,
                                  String crimeLocs,
                                  String crimeFlags,
                                  String crime,
                                  String sentence,
                                  String warnMsg);
    
    public boolean frame(Area myArea, MOB accused, MOB framed);
    public boolean arrest(Area myArea, MOB officer, MOB mob);
    public Vector warrantInfo(Area myArea);
    public Law legalInfo(Area myArea);
    public boolean isElligibleOfficer(Area myArea, MOB mob);
    public boolean hasWarrant(Area myArea, MOB accused);
    public boolean isAnyOfficer(Area myArea, MOB mob);
    public boolean isJudge(Area myArea, MOB mob);
    public void modifyAssessedFines(double d, MOB mob);
    public double finesOwed(MOB mob);
    public boolean updateLaw(Area myArea);
    public String rulingClan();
    public String conquestInfo(Area myArea);
    public boolean isFullyControlledByClan();
    public int controlPoints();
    public int revoltChance();
    public void setControlPoints(String clanID, int newControlPoints);
    public Vector getWarrantsOf(Area myArea, String name);
    public Vector getWarrantsOf(Area myArea, MOB accused);
    public boolean addWarrant(Area myArea, LegalWarrant W);
    public boolean addWarrant(Area myArea, MOB accused, MOB victim, String crimeLocs, String crimeFlags, String crime, String sentence, String warnMsg);
    public boolean deleteWarrant(Area myArea, LegalWarrant W);
    public boolean aquit(Area myArea, MOB accused, Vector lawStrings);
    public boolean isJailRoom(Area myArea, Vector jails);
    public boolean accuse(Area myArea, MOB accused, MOB victim, Vector lawStrings);
}
