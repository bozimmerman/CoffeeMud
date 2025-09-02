#You should really input a name for your MUD below....
#Before using this on a UNIX machine, you must 'chmod 755 mud.sh' to make this file executable by the UNIX machine
#FYI - the nohup command will make a nohup.out file, usually in the CofferMud (directory where you start this from) directory - it will log the server messages...
if [ $# -eq 0 ]; then
	MUDNAME="Your Muds Name Here"
else
	MUDNAME="$*"
fi
nohup java -classpath ".:./lib/js.jar:./lib/jzlib.jar" -Djava.awt.headless=true -Xms65535000 -Xmx115535000 com.planet_ink.coffee_mud.application.MUD "$MUDNAME" &
