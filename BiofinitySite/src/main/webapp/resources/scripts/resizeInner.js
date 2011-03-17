<!-- Note: this code is adapted from http://stackoverflow.com/questions/153152/resizing-an-iframe-based-on-content by ConroyP -->

function resizeParent() {
	var frame = getParam('frame');
	var height = getParam('height');
	parent.parent.resizeFrame(frame, height);
}

// Helper function, parse param from request string
function getParam( name ) {
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec( window.location.href );
	if( results == null )
		return "";
	else
		return results[1];
}
