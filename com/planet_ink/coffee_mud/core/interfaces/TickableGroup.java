package com.planet_ink.coffee_mud.core.interfaces;
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

/**
 * This class represents a thread, consisting of a group of Tickable objects  receiving periodic calls
 * to their tick(Tickable,int) methods by this thread object
 * @see Tickable
 * @see Tickable#tick(Tickable, int)
 * @author Bo Zimmerman
 *
 */
public interface TickableGroup
{
    /** the maximum number of ticking objects which can be handled by a single thread */
    public final static int MAX_TICK_CLIENTS=128;

    /**
     * Returns the current or last Tickable object which this thread made a tick(Tickable,int) method
     * call to.
     * @see Tickable
     * @see Tickable#tick(Tickable, int)
     * @return the Tickable object last accessed
     */ 
    public Tickable lastTicked();
}
