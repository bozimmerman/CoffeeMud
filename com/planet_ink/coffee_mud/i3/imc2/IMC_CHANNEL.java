
package com.planet_ink.coffee_mud.i3.imc2;

public class IMC_CHANNEL {
    
    public int perm_level;
    public String name = ""; /* name of channel */
    public String owner = ""; /* owner (singular) of channel */
    public String operators = ""; /* current operators of channel */

    public int policy;

    public String invited = "";
    public String excluded = "";
    public String active = "";

    public String local_name = "";
    public int level;
    public String regformat  = "";
    public String emoteformat = "";
    public String socformat = "";
    public String history = "";
    public boolean refreshed;
    
}
