package com.planet_ink.coffee_mud.system.I3.packets;

public class Mud {
    public String address;
    public String admin_email;
    public String base_mudlib;
    public String driver;
    public int    modified;
    public String mud_name;
    public String mud_type;
    public String mudlib;
    public int    player_port;
    public int    state;
    public String status;
    public int    tcp_port;
    public int    udp_port;
   
    public Mud() {
        super();
    }
    
    public Mud(Mud other) {
        super();
        address = other.address;
        admin_email = other.admin_email;
        base_mudlib = other.base_mudlib;
        driver = other.driver;
        modified = other.modified;
        mud_name = other.mud_name;
        mud_type = other.mud_type;
        mudlib = other.mudlib;
        player_port = other.player_port;
        state = other.state;
        status = other.status;
        tcp_port = other.tcp_port;
        udp_port = other.udp_port;
    }
}
    