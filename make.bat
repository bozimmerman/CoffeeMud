SET Java_Home=C:\java\j2sdk1.4.2_04
set CLASSPATH=.;%Java_Home%\lib\dt.jar;%Java_Home%\lib\tools.jar
SET JAVACPATH=%Java_Home%\bin\javac

IF "%1" == "docs" GOTO :DOCS

%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Archon/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Common/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Diseases/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Druid/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Fighter/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Languages/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Misc/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Paladin/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Poisons/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Prayers/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Properties/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Ranger/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Skills/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Songs/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Specializations/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Spells/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Thief/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Abilities/Traps/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/application/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Areas/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Behaviors/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/CharClasses/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/commands/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/common/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/exceptions/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Exits/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/i3/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/i3/net/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/i3/packets/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/i3/persist/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/i3/server/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/interfaces/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Items/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Items/Weapons/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Items/MiscMagic/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Items/ClanItems/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Items/Armor/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Locales/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/MOBS/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/Races/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/system/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/utils/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/web/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/web/macros/*.java
%JAVACPATH% -g -deprecation com/planet_ink/coffee_mud/web/macros/grinder/*.java
%JAVACPATH% -g -deprecation com/planet_ink/fakedb/*.java


GOTO :FINISH

:DOCS

%Java_Home%\bin\javadoc -d .\docs -J-Xmx128m com/planet_ink/coffee_mud/Abilities/*.java com/planet_ink/coffee_mud/Abilities/Archon/*.java com/planet_ink/coffee_mud/Abilities/Common/*.java com/planet_ink/coffee_mud/Abilities/Diseases/*.java com/planet_ink/coffee_mud/Abilities/Druid/*.java com/planet_ink/coffee_mud/Abilities/Fighter/*.java com/planet_ink/coffee_mud/Abilities/Languages/*.java com/planet_ink/coffee_mud/Abilities/Misc/*.java com/planet_ink/coffee_mud/Abilities/Paladin/*.java com/planet_ink/coffee_mud/Abilities/Poisons/*.java com/planet_ink/coffee_mud/Abilities/Prayers/*.java com/planet_ink/coffee_mud/Abilities/Properties/*.java com/planet_ink/coffee_mud/Abilities/Ranger/*.java com/planet_ink/coffee_mud/Abilities/Skills/*.java com/planet_ink/coffee_mud/Abilities/Songs/*.java com/planet_ink/coffee_mud/Abilities/Specializations/*.java com/planet_ink/coffee_mud/Abilities/Spells/*.java com/planet_ink/coffee_mud/Abilities/Thief/*.java com/planet_ink/coffee_mud/Abilities/Traps/*.java com/planet_ink/coffee_mud/application/*.java com/planet_ink/coffee_mud/Areas/*.java com/planet_ink/coffee_mud/Behaviors/*.java com/planet_ink/coffee_mud/CharClasses/*.java com/planet_ink/coffee_mud/Clans/*.java com/planet_ink/coffee_mud/commands/*.java com/planet_ink/coffee_mud/commands/base/*.java com/planet_ink/coffee_mud/commands/sysop/*.java com/planet_ink/coffee_mud/common/*.java com/planet_ink/coffee_mud/exceptions/*.java com/planet_ink/coffee_mud/Exits/*.java com/planet_ink/coffee_mud/interfaces/*.java com/planet_ink/coffee_mud/Items/*.java com/planet_ink/coffee_mud/Items/Weapons/*.java com/planet_ink/coffee_mud/Items/MiscMagic/*.java com/planet_ink/coffee_mud/Items/ClanItems/*.java com/planet_ink/coffee_mud/Items/Armor/*.java com/planet_ink/coffee_mud/Locales/*.java com/planet_ink/coffee_mud/MOBS/*.java com/planet_ink/coffee_mud/Races/*.java com/planet_ink/coffee_mud/system/*.java com/planet_ink/coffee_mud/utils/*.java com/planet_ink/coffee_mud/web/*.java com/planet_ink/coffee_mud/web/macros/*.java com/planet_ink/coffee_mud/web/macros/grinder/*.java 

:FINISH
