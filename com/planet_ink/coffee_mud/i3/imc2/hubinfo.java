
package com.planet_ink.coffee_mud.i3.imc2;

import java.util.Date;

public class hubinfo {
    
    /* The mud's connection data for the hub */
    public String hubname = ""; /* name of hub */
    public String host = ""; /* hostname of hub */
    public int port; /* remote port of hub */
    public String serverpw = ""; /* server password */
    public String clientpw = ""; /* client password */
    public String network = "";  /* intermud network name */
    Date timer_duration; /* delay after next reconnect failure */
    Date last_connected; /* last connected when? */
    int connect_attempts; /* try for 3 times - shogar */
    public boolean autoconnect; /* Do we autoconnect on bootup or not? - Samson */

    /* Conection parameters - These don't save in the config file */
    //int desc; /* descriptor */
    int state; /* IMC_xxxx state */
    int version; /* version of remote site */
        /* try to write at end of cycle regardless of fd_set state? */
    String inbuf = ""; /* input buffer */
    int insize;
    String outbuf = ""; /* output buffer */
    int outsize;
}
