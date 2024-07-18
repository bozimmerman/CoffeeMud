var CMLib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var CMFile=Packages.com.planet_ink.coffee_mud.core.CMFile;
var System=Packages.java.lang.System;
/*
 * This script, suitable for a cron job, extracts player
 * records and generates player xml files in a backups
 * directory, so long as the player has logged in
 * in the last week or so.
 */

var name;
var i;
var F,F2;
var backupPath = '::/resources/backups/';
var fnam = '';
var xml = '';
var unload=false;

var totList = CMLib.players().getPlayerLists();
for(i = 0;i<totList.size();i++)
{
    name = totList.get(i);
    fnam = backupPath + name;
    F = new CMFile(fnam+'_3.xml',mob());
    if(F.exists())
        F.delete();
    F = new CMFile(fnam+'_2.xml',mob());
    if(F.exists())
        F.renameTo(new CMFile(fnam+'_3.xml',mob()));
    F = new CMFile(fnam+'_1.xml',mob());
    if(F.exists())
        F.renameTo(new CMFile(fnam+'_2.xml',mob()));
}
totList.clear();

var weekAgo = System.currentTimeMillis() - (60000 * 60 * 24 * 7);
var nameList = CMLib.database().DBRawQuery('SELECT CMUSERID FROM CMCHAR WHERE CMDATE >= '+weekAgo);
for(i = 1;i<nameList.size();i++)
{
    name = nameList.get(i)[0];
    fnam = backupPath + name;
    unload=false;
    M = CMLib.players().getPlayer(name);
    if(M==null)
    {
        unload=true;
        M = CMLib.players().getLoadPlayer(name);
    }
    if(M != null)
    {
        F = new CMFile(fnam+'_1.xml',mob());
        xml = '<PLAYERS><PLAYER>' + CMLib.coffeeMaker().getPlayerXML(M,null,null) + '</PLAYER></PLAYERS>';
        F.saveText(xml);
        if(unload)
            CMLib.players().unloadOfflinePlayer(M);
    }
}
