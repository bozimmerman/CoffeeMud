del com\planet_ink\grinder\*.class
javac com/planet_ink/grinder/*.java
del *.jar
jar -cmvf manifest MUDGrinder.jar com *.gif
xcopy /e /c /r /y *.* i:\projects\mudgrinder\.
xcopy /e /c /r /y  MUDGrinder.jar i:\windows\desktop\.
xcopy /e /c /r /y  MUDGrinder.jar i:\www.zimmers.net\home\mud\.
xcopy /e /c /r /y  MUDGrinder.jar c:\windows\desktop\.
