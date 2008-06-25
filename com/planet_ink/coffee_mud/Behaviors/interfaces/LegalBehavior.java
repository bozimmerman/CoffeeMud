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
   Copyright 2000-2008 Bo Zimmerman

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
/**
 * A LegalBehavior is a Behavior that provides functionality related to law and
 * order within a given geographic sphere, which is usually an Area with a 
 * LegalBehavior behavior.  A LegalBehavior keeps track of Warrants against
 * players and mobs which persist only in memory.  It also controls the behavior
 * of arresting officers and judges, and dispenses justice by taking mobs and
 * players to jail, putting them on parole, or issuing warnings or other 
 * punishments.
 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
 * @see com.planet_ink.coffee_mud.Common.interfaces.Law
 */
public interface LegalBehavior extends Behavior
{
    /** constant for the number of miliseconds in a real-life day */
    public static final long ONE_REAL_DAY=(long)1000*60*60*24;
    /** constant for the number of miliseconds before a warrant expires */
    public static final long EXPIRATION_MILLIS=ONE_REAL_DAY*7; // 7 real days
    /** constant for the number of miliseconds before an area is under legal control */
    public static final long CONTROLTIME=ONE_REAL_DAY*3;

    /**
     * Returns whether or not the given legal warrant is still a valid, timely
     * warrant that can be acted upon by law enforcement.
     * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
     * @param W the legal warrant to inspect
     * @param debugging whether debug information should be sent to the log
     * @return whether the given warrant is still a valid, timely crime.
     */
    public boolean isStillACrime(LegalWarrant W, boolean debugging);
    
    /**
     * 
     * @param mob
     * @param laws
     * @param myArea
     * @param target
     * @param crimeLocs
     * @param crimeFlags
     * @param crime
     * @param sentence
     * @param warnMsg
     * @return
     */
    public boolean fillOutWarrant(MOB mob,
                                  Law laws,
                                  Area myArea,
                                  Environmental target,
                                  String crimeLocs,
                                  String crimeFlags,
                                  String crime,
                                  String sentence,
                                  String warnMsg);
    /**
     * 
     * @param myArea
     * @param accused
     * @param framed
     * @return
     */
    public boolean frame(Area myArea, MOB accused, MOB framed);
    
    /**
     * 
     * @param myArea
     * @param officer
     * @param mob
     * @return
     */
    public boolean arrest(Area myArea, MOB officer, MOB mob);
    
    /**
     * 
     * @param myArea
     * @return
     */
    public Vector warrantInfo(Area myArea);
    
    /**
     * 
     * @param myArea
     * @return
     */
    public Law legalInfo(Area myArea);
    
    /**
     * 
     * @param myArea
     * @param mob
     * @return
     */
    public boolean isElligibleOfficer(Area myArea, MOB mob);
    
    /**
     * 
     * @param myArea
     * @param accused
     * @return
     */
    public boolean hasWarrant(Area myArea, MOB accused);
    
    /**
     * 
     * @param myArea
     * @param mob
     * @return
     */
    public boolean isAnyOfficer(Area myArea, MOB mob);
    
    /**
     * 
     * @param myArea
     * @param mob
     * @return
     */
    public boolean isJudge(Area myArea, MOB mob);
    
    /**
     * 
     * @param d
     * @param mob
     */
    public void modifyAssessedFines(double d, MOB mob);
    
    /**
     * 
     * @param mob
     * @return
     */
    public double finesOwed(MOB mob);
    
    /**
     * 
     * @param myArea
     * @return
     */
    public boolean updateLaw(Area myArea);
    
    /**
     * 
     * @return
     */
    public String rulingOrganization();
    
    /**
     * 
     * @param myArea
     * @return
     */
    public String conquestInfo(Area myArea);
    
    /**
     * 
     * @return
     */
    public boolean isFullyControlled();
    
    /**
     * 
     * @return
     */
    public int controlPoints();
    
    /**
     * 
     * @return
     */
    public int revoltChance();
    
    /**
     * 
     * @param clanID
     * @param newControlPoints
     */
    public void setControlPoints(String clanID, int newControlPoints);
    
    /**
     * 
     * @param myArea
     * @param name
     * @return
     */
    public Vector getWarrantsOf(Area myArea, String name);
    
    /**
     * 
     * @param myArea
     * @param accused
     * @return
     */
    public Vector getWarrantsOf(Area myArea, MOB accused);
    
    /**
     * 
     * @param myArea
     * @param W
     * @return
     */
    public boolean addWarrant(Area myArea, LegalWarrant W);
    
    /**
     * 
     * @param myArea
     * @param accused
     * @param victim
     * @param crimeLocs
     * @param crimeFlags
     * @param crime
     * @param sentence
     * @param warnMsg
     * @return
     */
    public boolean addWarrant(Area myArea, MOB accused, MOB victim, String crimeLocs, String crimeFlags, String crime, String sentence, String warnMsg);
    
    /**
     * 
     * @param myArea
     * @param W
     * @return
     */
    public boolean deleteWarrant(Area myArea, LegalWarrant W);
    
    /**
     * 
     * @param myArea
     * @param accused
     * @param lawStrings
     * @return
     */
    public boolean aquit(Area myArea, MOB accused, Vector lawStrings);
    
    /**
     * 
     * @param myArea
     * @param jails
     * @return
     */
    public boolean isJailRoom(Area myArea, Vector jails);
    
    /**
     * 
     * @param myArea
     * @param accused
     * @param victim
     * @param lawStrings
     * @return
     */
    public boolean accuse(Area myArea, MOB accused, MOB victim, Vector lawStrings);
}
