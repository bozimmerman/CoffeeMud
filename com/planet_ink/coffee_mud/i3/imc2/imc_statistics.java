
package com.planet_ink.coffee_mud.i3.imc2;

public final class imc_statistics {
    
    long start; /* when statistics started               */

    long rx_pkts; /* Received packets                      */
    long tx_pkts; /* Transmitted packets                   */
    long rx_bytes; /* Received bytes                        */
    long tx_bytes; /* Transmitted bytes                     */

    int max_pkt; /* Max. size packet processed            */
    int sequence_drops; /* Dropped packets due to age            */  
}
