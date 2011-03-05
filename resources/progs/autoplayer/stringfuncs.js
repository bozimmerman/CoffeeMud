function trim(s) 
{
	return s.replace(/^\s+|\s+$/g,"");
}

function ltrim() 
{
	return s.replace(/^\s+/,"");
}

function rtrim(s) 
{
	return s.replace(/\s+$/,"");
}

function split(s,by)
{
	var arr=new Array();
	var x=s.indexOf(by);
	var arrdex=0;
	while(x>=0)
	{
		var ss=s.substr(0,x);
		arr[arrdex++]=ss;
		s=s.substr(x+1);
		x=s.indexOf(by);
	}
	if(s.length>0)
		arr[arrdex]=s;
	return arr;
}

function splittrim(s,by)
{
	var arr=split(s,by);
	for(var x=0;x<arr.length;x++)
		arr[x]=trim(arr[x]);
	return arr;
}

function splittrimnoempty(s,by)
{
	var arr=splittrim(s,by);
	for(var x=0;x<arr.length;x++)
		arr[x]=trim(arr[x]);
	var arr2=new Array();
	var arr2dex=0;
	for(var x=0;x<arr.length;x++)
		if(arr[x].length>0)
			arr2[arr2dex++]=arr[x];
	return arr2;
}