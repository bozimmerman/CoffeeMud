var menuAreaHeight = 20;
var menuArea = null;
var menuBackgroundColor = "#404040";
var menuForegroundColor = "Yellow";
var menuWindow = null;

var menuData = [
	{"Window": [
		{"n":"Connect",
		 "a":"javascript:menuConnect();",
		 "e":""},
		{"n":"Disconnect",
		 "e":"currentSiplet!=null && currentSiplet.wsopened",
		 "a":"javascript:menuDisconnect();"},
		{"n":"Reconnect",
		 "e":"currentSiplet!=null",
		 "a":"javascript:menuReconnect();"}
	]},
	{"Options": [
		{"n":"Global","a":"javascript:menuGlobal();"}
	]},
	{"Help": [
		{"n":"About","a":"javascript:menuAbout()"}
	]}
];

function configureMenu(obj)
{
	menuArea=obj;
	menuArea.style.position='fixed';
	menuArea.style.top=0;
	menuArea.style.width='100%';
	menuArea.style.height=menuAreaHeight+'px';
	menuArea.style.background=menuBackgroundColor;
	menuArea.style.color=menuForegroundColor;
	
	var html = '';
	html += '<TABLE style="border: 1px solid white; border-collapse: collapse; height: 20px; table-layout: fixed; width: 100%;">';
	html +='<TR style="height: 20px;" >';
	for(var to=0;to<menuData.length;to++)
	{
		var topO = menuData[to];
		var topN = Object.keys(topO)[0];
		html += '<TD style="border: 1px solid white; padding: 0;"';
		html += ' ONCLICK="menumenu(this,event,'+to+')" ';
		html += '><FONT COLOR="'+menuForegroundColor+'"><B>&nbsp;&nbsp;';
		html += topN + '</FONT></TD>';
	}
	html += '</TR></TABLE>';
	menuArea.innerHTML = html;
}

function menumenu(obj, e, to) {
	nowhidemenu();
	var topO = menuData[to];
	var topN = Object.keys(topO)[0];
	var subList = topO[topN];
	var href='';
	var hint='';
	for(var h=0;h<subList.length;h++)
	{
		var sub=subList[h];
		hint+=sub['n']+'|';
		if(('e' in sub)&&(sub['e'])&&(!eval(sub['e'])))
			href+='|';
		else
			href+=sub['a']+'|';
	}
	var m = dropdownmenu(obj, e, href, hint, prompt, obj.offsetLeft, obj.offsetTop + 20, 200);
	m.style.background = menuBackgroundColor;
	m.style.color = menuForegroundColor;
	var as = Array.from(m.getElementsByTagName("A"));
	for(var a=0;a<as.length;a++)
	{
		as[a].style.color=menuForegroundColor;
		as[a].style.fontSize=16;
		as[a].style.textDecoration = 'none';
	}
	return m;
}

function hideOptionWindow()
{
	if(menuWindow != null)
	{
		menuWindow.style.visibility = 'hidden';
		menuWindow.onclick = function() {};
		var contentWindow = menuWindow.getElementsByTagName('div')[1];
		contentWindow.innerHTML = '';
	}
}

function getOptionWindow(heading, w, h)
{
	if(menuWindow == null)
	{
		menuWindow = document.createElement('div');
		menuWindow.style.cssText = "position:absolute;top:20%;left:10%;height:60%;width:80%;z-index:99;";
		menuWindow.style.cssText += "border-style:solid;border-width:5px;border-color:white;";
		menuWindow.style.backgroundColor = 'darkgray';
		menuWindow.style.visibility = 'visible';
		menuWindow.style.color = 'black';
		document.body.appendChild(menuWindow);
		var titleBar = document.createElement('div');
		titleBar.style.cssText = "position:absolute;top:0%;left:0%;height:20px;width:100%;";
		titleBar.style.backgroundColor = 'white';
		titleBar.style.color = 'black';
		var contentWindow = document.createElement('div');
		contentWindow.style.cssText = "position:absolute;top:20px;left:0%;height:calc(100% - 20px);width:100%;";
		contentWindow.style.backgroundColor = 'lightgray';
		contentWindow.style.color = 'black';
	    contentWindow.style.overflowY = 'auto';
	    contentWindow.style.overflowX = 'hidden';
		menuWindow.appendChild(titleBar);
		menuWindow.appendChild(contentWindow);
	}
	menuWindow.onclick = function() {};
	var titleBar = menuWindow.getElementsByTagName('div')[0];
	var contentWindow = menuWindow.getElementsByTagName('div')[1];
	contentWindow.innerHTML = '';
	menuWindow.style.top = 'calc(' + (((100-h)/2)+'%') + ' - '+inputAreaHeight+'px);';
	menuWindow.style.left = ((100-w)/2)+'%';
	menuWindow.style.height = h+'%';
	menuWindow.style.width = w+'%';
	menuWindow.style.visibility = 'visible';
	titleBar.innerHTML = '<FONT COLOR=BLACK>'+heading+'</FONT>'+
		'<IMG style="float: right; width: 16px; height: 16px;" '
		+'ONCLICK="hideOptionWindow();" '
		+'SRC="images/close.gif">';
	return contentWindow;
}

function menuAbout()
{
	var content = getOptionWindow("About",60,40);
	populateDivFromUrl(content, 'js/dialogs/about.htm');
	this.menuWindow.onclick = hideOptionWindow;
}

function menuGlobal()
{
	var content = getOptionWindow("Global Options",60,40);
	populateDivFromUrl(content, 'js/dialogs/global.htm');
}

function menuDisconnect()
{
	if(this.currentSiplet != null)
	{
		this.currentSiplet.closeSocket();
	}
}

function menuReconnect()
{
	if(this.currentSiplet != null)
	{
		this.currentSiplet.closeSocket();
		this.currentSiplet.reset();
		this.currentSiplet.connect(this.currentSiplet.url);
	}
}

function menuConnect()
{
	var content = getOptionWindow("Connect",60,40);
	populateDivFromUrl(content, 'js/dialogs/connect.htm');
}
