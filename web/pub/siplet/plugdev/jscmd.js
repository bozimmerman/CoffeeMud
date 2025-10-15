function showHelp()
{
	win.displayText('<FRAME JSCMDHELP SCROLLING=y TITLE="GMCP Help" ACTION=OPEN FLOATING=close SCROLLING=y TOP=10% LEFT=15% WIDTH=450 HEIGHT=250>');
	var html = '<DEST NAME=JSCMDHELP EOF><FONT COLOR=WHITE>';
	html += 'Enter <B>/js [command]</B> to execute some JavaScript.<BR><BR>';
	html += 'Double &quot;quotes&quot; and \\backslashes must be escaped with \\..<BR><BR>';
	html += 'See help on Scripts for API information.<BR><BR>';
	win.displayAt(html,'JSCMDHELP');
}

window.onevent=function(event)
{
	if(event.data && event.data.startsWith)
	{
		if(event.data === 'Help')
		{
			showHelp();
			return;
		}
		try
		{
			var x = eval(event.data);
			if(!event.data.startsWith('win.'))
			{
				if(x !== undefined)
					win.displayText('<BR><FONT COLOR=WHITE>JavaScript result: ' + x+'</FONT><BR>');
			}
		}
		catch(e)
		{
			win.displayText('<BR><FONT COLOR=WHITE>JavaScript error: ' + e+'</FONT><BR>');
		}
	}
};
