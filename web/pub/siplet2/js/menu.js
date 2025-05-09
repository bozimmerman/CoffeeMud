var menuAreaHeight = 20;
var menuArea = null;
var menuBackgroundColor = "#404040";
var menuForegroundColor = "Yellow";
var menuWindow = null;

function menumenu(obj, e, href, hint) {
	nowhidemenu();
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

function configureMenu(obj)
{
	menuArea=obj;
	menuArea.style.position='fixed';
	menuArea.style.top=0;
	menuArea.style.width='100%';
	menuArea.style.height=menuAreaHeight+'px';
	menuArea.style.background=menuBackgroundColor;
	menuArea.style.color=menuForegroundColor;
	
	var menuData = [
		{"Window": [
			{"n":"New","a":"javascript:menuNew();"},
			{"n":"Disconnect","a":"javascript:menuDisconnect();"},
			{"n":"Reconnect","a":"javascript:menuReconnect();"}
		]},
		{"Options": [
			{"n":"Global"}
		]},
		{"Help": [
			{"n":"About","a":"javascript:menuAbout()"}
		]}
	];
	var html = '';
	html += '<TABLE style="border: 1px solid white; border-collapse: collapse; height: 20px; table-layout: fixed; width: 100%;">';
	html +='<TR style="height: 20px;" >';
	for(var to=0;to<menuData.length;to++)
	{
		var topO = menuData[to];
		var topN = Object.keys(topO)[0];
		var subList = topO[topN];
		html += '<TD style="border: 1px solid white; padding: 0;"';
		var href='';
		var hint='';
		for(var h=0;h<subList.length;h++)
		{
			var sub=subList[h];
			href+=sub['a']+'|';
			hint+=sub['n']+'|';
		}
		html += ' ONCLICK="menumenu(this,event,\''+href+'\',\''+hint+'\')" ';
		html += '><FONT COLOR="'+menuForegroundColor+'"><B>&nbsp;&nbsp;';
		html += topN + '</FONT></TD>';
	}
	html += '</TR></TABLE>';
	menuArea.innerHTML = html;
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
		menuWindow.style.foregroundColor = 'black';
		document.body.appendChild(menuWindow);
		var titleBar = document.createElement('div');
		titleBar.style.cssText = "position:absolute;top:0%;left:0%;height:20px;width:100%;";
		titleBar.style.backgroundColor = 'white';
		titleBar.style.foregroundColor = 'black';
		var contentWindow = document.createElement('div');
		contentWindow.style.cssText = "position:absolute;top:20px;left:0%;height:calc(100% - 20px);width:100%;";
		contentWindow.style.backgroundColor = 'lightgray';
		contentWindow.style.foregroundColor = 'black';
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
	var aboutHtml = '<div style="position:absolute;top:50%;left:50%;white-space:nowrap;';
	aboutHtml += 'transform:translate(-50%,-50%);text-align:center;line-height: 1.5;">';
	aboutHtml += '<div style="display:block;margin:0 auto;"><h1>Siplet v'+Siplet.VERSION_MAJOR;
	if(Siplet.VERSION_MINOR != 0)
		aboutHtml += '.'+Siplet.VERSION_MINOR;
	aboutHtml += '</h1></div><BR><BR><div style="display:block;margin:0 auto;">';
	aboutHtml += '(C)2025-2025 Bo Zimmerman</div>';
	aboutHtml += '</div>';
	content.innerHTML=aboutHtml;
	this.menuWindow.onclick = hideOptionWindow;
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

function menuNew()
{
	if(this.currentSiplet != null)
	{
		AddNewSipletTab(this.currentSiplet.url);
	}
}
