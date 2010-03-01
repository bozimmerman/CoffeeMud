package com.planet_ink.coffee_mud.Libraries.interfaces;
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
public interface ListingLibrary extends CMLibrary
{

    public String itemSeenString(MOB viewer, Environmental item, boolean useName, boolean longLook, boolean sysMsgs);
    public int getReps(Environmental item, Vector theRest, MOB mob, boolean useName, boolean longLook);
    public void appendReps(int reps, StringBuilder say, boolean compress);
    public StringBuilder lister(MOB mob, Vector things, boolean useName, String tag, String tagParm, boolean longLook, boolean compress);
    public StringBuilder reallyList(Hashtable these, int ofType);
    public StringBuilder reallyList(Hashtable these);
    public StringBuilder reallyList(Hashtable these, Room likeRoom);
    public StringBuilder reallyList(Vector these, int ofType);
    public StringBuilder reallyList(Enumeration these, int ofType);
    public StringBuilder reallyList(Vector these);
    public StringBuilder reallyList(Enumeration these);
    public StringBuilder reallyList(Vector these, Room likeRoom);
    public StringBuilder reallyList(Hashtable these, int ofType, Room likeRoom);
    public StringBuilder reallyList(Vector these, int ofType, Room likeRoom);
    public StringBuilder reallyList(Enumeration these, Room likeRoom);
    public StringBuilder reallyList(Enumeration these, int ofType, Room likeRoom);
    public StringBuilder reallyList2Cols(Enumeration these, int ofType, Room likeRoom);
    public StringBuilder fourColumns(Vector reverseList);
    public StringBuilder fourColumns(Vector reverseList, String tag);
}
