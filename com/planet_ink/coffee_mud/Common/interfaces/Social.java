package com.planet_ink.coffee_mud.Common.interfaces;
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
/* 
Copyright 2000-2006 Bo Zimmerman

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

import java.util.Vector;

public interface Social extends Environmental
{
    public String Name();
    public void setName(String newName);
    public String You_see();
    public String Third_party_sees();
    public String Target_sees();
    public String See_when_no_target();
    public int sourceCode();
    public int othersCode();
    public int targetCode();
    public void setYou_see(String str);
    public void setThird_party_sees(String str);
    public void setTarget_sees(String str);
    public void setSee_when_no_target(String str);
    public void setSourceCode(int code);
    public void setOthersCode(int code);
    public void setTargetCode(int code);
    public boolean targetable();
    public long getTickStatus();
    public String MSPfile();
    public void setMSPfile(String newFile);

    public boolean invoke(MOB mob,
                          Vector commands,
                          Environmental target,
                          boolean auto);
    public CMMsg makeChannelMsg(MOB mob,
            int channelInt,
            String channelName,
            Vector commands,
            boolean makeTarget);
}
