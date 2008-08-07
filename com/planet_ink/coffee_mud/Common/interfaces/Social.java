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

import java.util.Vector;

/**
 * The Interface Social.
 */
public interface Social extends Environmental, CMCommon
{
    
    /**
     * Base name.
     * 
     * @return the string
     */
    public String baseName();
    
    /**
     * You_see.
     * 
     * @return the string
     */
    public String You_see();
    
    /**
     * Third_party_sees.
     * 
     * @return the string
     */
    public String Third_party_sees();
    
    /**
     * Target_sees.
     * 
     * @return the string
     */
    public String Target_sees();
    
    /**
     * See_when_no_target.
     * 
     * @return the string
     */
    public String See_when_no_target();
    
    /**
     * Source code.
     * 
     * @return the int
     */
    public int sourceCode();
    
    /**
     * Others code.
     * 
     * @return the int
     */
    public int othersCode();
    
    /**
     * Target code.
     * 
     * @return the int
     */
    public int targetCode();
    
    /**
     * Sets the you_see.
     * 
     * @param str the new you_see
     */
    public void setYou_see(String str);
    
    /**
     * Sets the third_party_sees.
     * 
     * @param str the new third_party_sees
     */
    public void setThird_party_sees(String str);
    
    /**
     * Sets the target_sees.
     * 
     * @param str the new target_sees
     */
    public void setTarget_sees(String str);
    
    /**
     * Sets the see_when_no_target.
     * 
     * @param str the new see_when_no_target
     */
    public void setSee_when_no_target(String str);
    
    /**
     * Sets the source code.
     * 
     * @param code the new source code
     */
    public void setSourceCode(int code);
    
    /**
     * Sets the others code.
     * 
     * @param code the new others code
     */
    public void setOthersCode(int code);
    
    /**
     * Sets the target code.
     * 
     * @param code the new target code
     */
    public void setTargetCode(int code);
    
    /**
     * Targetable.
     * 
     * @return true, if successful
     */
    public boolean targetable();
    
    /* (non-Javadoc)
     * @see com.planet_ink.coffee_mud.core.interfaces.Tickable#getTickStatus()
     */
    public long getTickStatus();
    
    /**
     * MS pfile.
     * 
     * @return the string
     */
    public String MSPfile();
    
    /**
     * Sets the mS pfile.
     * 
     * @param newFile the new mS pfile
     */
    public void setMSPfile(String newFile);

    /**
     * Invoke.
     * 
     * @param mob the mob
     * @param commands the commands
     * @param target the target
     * @param auto the auto
     * 
     * @return true, if successful
     */
    public boolean invoke(MOB mob,
                          Vector commands,
                          Environmental target,
                          boolean auto);
    
    /**
     * Make channel msg.
     * 
     * @param mob the mob
     * @param channelInt the channel int
     * @param channelName the channel name
     * @param commands the commands
     * @param makeTarget the make target
     * 
     * @return the cM msg
     */
    public CMMsg makeChannelMsg(MOB mob,
					            int channelInt,
					            String channelName,
					            Vector commands,
					            boolean makeTarget);
	
	/**
	 * Make message.
	 * 
	 * @param mob the mob
	 * @param str the str
	 * @param end the end
	 * @param srcMask the src mask
	 * @param fullCode the full code
	 * @param commands the commands
	 * @param I3channelName the i3channel name
	 * @param makeTarget the make target
	 * 
	 * @return the cM msg
	 */
	public CMMsg makeMessage(MOB mob,
							 String str,
							 String end,
							 int srcMask,
							 int fullCode,
							 Vector commands,
							 String I3channelName,
							 boolean makeTarget);
}
