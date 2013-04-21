/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

var INIT_LOCATION = new L.LatLng(48.8547, 2.3472); // ile de la cité
var AUTO_CENTER_MAP = false;
var ROUTER_ID = "";
var MSEC_PER_HOUR = 60 * 60 * 1000;
var MSEC_PER_DAY = MSEC_PER_HOUR * 24;
// Note: time zone does not matter since we are turning this back into text before sending it
var BASE_DATE_MSEC = new Date().getTime() - new Date().getTime() % MSEC_PER_DAY; 
// var BASE_DATE_MSEC = Date.parse('2012-11-15');


var map = new L.Map('map', {
	minZoom : 11,
	maxZoom : 16,
	// what we really need is a fade transition between old and new tiles without removing the old ones
});

var mapboxURL = "http://{s}.tiles.mapbox.com/v3/demory.map-hmr94f0d/{z}/{x}/{y}.png";
var OSMURL    = "http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
var aerialURL = "http://{s}.mqcdn.com/naip/{z}/{x}/{y}.png";

var mapboxAttrib = "Tiles from <a href='http://mapbox.com/about/maps' target='_blank'> Streets</a>";
var mapboxLayer = new L.TileLayer(mapboxURL, {maxZoom: 17, attribution: mapboxAttrib});

var osmAttrib = 'Map data &copy; 2011 OpenStreetMap contributors';
var osmLayer = new L.TileLayer(OSMURL, 
		{subdomains: ["otile1","otile2","otile3","otile4"], maxZoom: 18, attribution: osmAttrib});

var aerialLayer = new L.TileLayer(aerialURL, 
		{subdomains: ["oatile1","oatile2","oatile3","oatile4"], maxZoom: 18, attribution: osmAttrib});

var flags = {
	twoEndpoint: false,
	twoSearch: false
};

// convert a map of query parameters into a query string, 
// expanding Array values into multiple query parameters
var buildQuery = function(params) {
	ret = [];
	for (key in params) {
		vals = params[key];
		// wrap scalars in array
		if ( ! (vals instanceof Array)) vals = new Array(vals);
		for (i in vals) { 
			val = vals[i]; // js iterates over indices not values!
			// skip params that are empty or stated to be the same as previous
			// if (val == '' || val == 'same')
			if (val == 'same') // empty string needed for non-banning
				continue;
			param = [encodeURIComponent(key), encodeURIComponent(val)].join('=');
			ret.push(param);
		}
	}
	return "?" + ret.join('&');
};

var analystUrl = "/opentripplanner-api-webapp/ws/tile/{z}/{x}/{y}.png"; 
var analystLayer = new L.TileLayer(analystUrl, {attribution: osmAttrib});

// create geoJSON layers for DC Purple Line

var purpleLineCoords = 
	[[2.2304,48.8273],
	 [2.25962,48.82112],
	 [2.27319,48.81412],
	 [2.30239,48.81125],
	 [2.31865,48.8028],
	 [2.32805,48.79794],
	 [2.34942,48.79319],
	 [2.36747,48.78847],
	 [2.38792,48.79001],
	 [2.4085,48.78239],
	 [2.43242,48.78981],
	 [2.44929,48.79514],
	 [2.47236,48.80682],
	 [2.50242,48.81614],
	 [2.4942,48.839],
	 [2.489,48.8543],
	 [2.4811,48.8826],
	 [2.4755,48.8947],
	 [2.4683,48.9057],
	 [2.4424,48.9067],
	 [2.429,48.9145],
	 [2.4035,48.915],
	 [2.3751,48.9192],
	 [2.35119,48.91763],
	 [2.34304,48.91955],
	 [2.31406,48.92045],
	 [2.2862,48.92311],
	 [2.27171,48.91412],
	 [2.26904,48.90548],
	 [2.23738,48.89248],
	 [2.22792,48.89736],
	 [2.20035,48.8874],
	 [2.22126,48.87163],
	 [2.22178,48.8425],
	 [2.2304,48.8273]]; // reconnect to first stop

var purpleLineStopsFeature = { 
	"type": "Feature",
	"properties": {
	    "name": "Purple Line stops"
        },
	"geometry": {
	    "type": "MultiPoint",
	    "coordinates": purpleLineCoords,
	}
};

var geojsonMarkerOptions = {
		radius: 4,
		fillColor: "#000",
		color: "#000",
		weight: 0,
		opacity: 0,
		fillOpacity: 0.8
};

var purpleLineStopsLayer = new L.GeoJSON(purpleLineStopsFeature, {
	pointToLayer: function (feature, latlng) { 
		return new L.CircleMarker(latlng, geojsonMarkerOptions);
	}
});
map.addLayer(purpleLineStopsLayer);

var alignmentStyle ={
	"color": "#700000",
	"weight": 20,
	"opacity": 0.8
};

var purpleLineAlignmentFeature = { 
	"type": "Feature", 
	"properties": { 
		"name": "ligne 15",
		"style" : alignmentStyle
	},
	"geometry": { 
		"type": "MultiLineString",  
		"coordinates": [ [ [ 2.346654, 48.918975 ], [ 2.343028, 48.919542 ], [ 2.338255, 48.919474 ], [ 2.334097, 48.919481 ], [ 2.331099, 48.919639 ], [ 2.328036, 48.919806 ], [ 2.324046, 48.920115 ], [ 2.321172, 48.920428 ], [ 2.317284, 48.920522 ], [ 2.314064, 48.920441 ], [ 2.309992, 48.920205 ], [ 2.307031, 48.920281 ], [ 2.304824, 48.920705 ], [ 2.302455, 48.921758 ], [ 2.299079, 48.923052 ], [ 2.295515, 48.923785 ], [ 2.291451, 48.923868 ], [ 2.289353, 48.923585 ], [ 2.287336, 48.923287 ], [ 2.286199, 48.923116 ], [ 2.285027, 48.922613 ], [ 2.283133, 48.921767 ], [ 2.279940, 48.919909 ], [ 2.277113, 48.917893 ], [ 2.271723, 48.914122 ], [ 2.270771, 48.912050 ], [ 2.270071, 48.908602 ], [ 2.269063, 48.905468 ], [ 2.267681, 48.902997 ], [ 2.265687, 48.900793 ], [ 2.262935, 48.899007 ], [ 2.258702, 48.897172 ], [ 2.251933, 48.894544 ], [ 2.245712, 48.892885 ], [ 2.242330, 48.892393 ], [ 2.239937, 48.892153 ], [ 2.236839, 48.892215 ], [ 2.234448, 48.892403 ], [ 2.232205, 48.892991 ], [ 2.230743, 48.894045 ], [ 2.227974, 48.897333 ], [ 2.225963, 48.898634 ], [ 2.224719, 48.899054 ], [ 2.223769, 48.899167 ], [ 2.222326, 48.899128 ], [ 2.220664, 48.898683 ], [ 2.218472, 48.897437 ], [ 2.213148, 48.894528 ], [ 2.202627, 48.888700 ], [ 2.200325, 48.887359 ], [ 2.217878, 48.876158 ], [ 2.220418, 48.874060 ], [ 2.221261, 48.871626 ], [ 2.221006, 48.845208 ], [ 2.221793, 48.842537 ], [ 2.222967, 48.839203 ], [ 2.224424, 48.836329 ], [ 2.225594, 48.833985 ], [ 2.227163, 48.831037 ], [ 2.228824, 48.829019 ], [ 2.230511, 48.827273 ], [ 2.234441, 48.825736 ], [ 2.241315, 48.824266 ], [ 2.252802, 48.821883 ], [ 2.259629, 48.821090 ], [ 2.273202, 48.814122 ], [ 2.275128, 48.813305 ], [ 2.277581, 48.812575 ], [ 2.279996, 48.812137 ], [ 2.283206, 48.811803 ], [ 2.287080, 48.811738 ], [ 2.291621, 48.811765 ], [ 2.294962, 48.811608 ], [ 2.297501, 48.811535 ], [ 2.299580, 48.811506 ], [ 2.301392, 48.811470 ], [ 2.302386, 48.811244 ], [ 2.303305, 48.810776 ], [ 2.313855, 48.805402 ], [ 2.318641, 48.802798 ], [ 2.328050, 48.797940 ], [ 2.330241, 48.797357 ], [ 2.336483, 48.796152 ], [ 2.344252, 48.794583 ], [ 2.350633, 48.792805 ], [ 2.351862, 48.792346 ], [ 2.353259, 48.791747 ], [ 2.356548, 48.790561 ], [ 2.359095, 48.789750 ], [ 2.363302, 48.788846 ], [ 2.367485, 48.788471 ], [ 2.370186, 48.788378 ], [ 2.373913, 48.788298 ], [ 2.378340, 48.788709 ], [ 2.383021, 48.789473 ], [ 2.387952, 48.789961 ], [ 2.391353, 48.789777 ], [ 2.393385, 48.789330 ], [ 2.394742, 48.788688 ], [ 2.395729, 48.787768 ], [ 2.396497, 48.786952 ], [ 2.397598, 48.785838 ], [ 2.398559, 48.785027 ], [ 2.400010, 48.784031 ], [ 2.401677, 48.783278 ], [ 2.404720, 48.782624 ], [ 2.408508, 48.782389 ], [ 2.410940, 48.782321 ], [ 2.414156, 48.782425 ], [ 2.417932, 48.782836 ], [ 2.421058, 48.783529 ], [ 2.423900, 48.784547 ], [ 2.426016, 48.785623 ], [ 2.428037, 48.787005 ], [ 2.430087, 48.788414 ], [ 2.432427, 48.789815 ], [ 2.439525, 48.792808 ], [ 2.442772, 48.794165 ], [ 2.444618, 48.794665 ], [ 2.447031, 48.795000 ], [ 2.449271, 48.795140 ], [ 2.452846, 48.795467 ], [ 2.456469, 48.796186 ], [ 2.459283, 48.797056 ], [ 2.461118, 48.798006 ], [ 2.463111, 48.798986 ], [ 2.465368, 48.800231 ], [ 2.466828, 48.801119 ], [ 2.468814, 48.802804 ], [ 2.470534, 48.804311 ], [ 2.472545, 48.806254 ], [ 2.473279, 48.806956 ], [ 2.474552, 48.807877 ], [ 2.476677, 48.808973 ], [ 2.478842, 48.809708 ], [ 2.480437, 48.810183 ], [ 2.482162, 48.810606 ], [ 2.484121, 48.811134 ], [ 2.486003, 48.811609 ], [ 2.490473, 48.812510 ], [ 2.493689, 48.813199 ], [ 2.497111, 48.814149 ], [ 2.499581, 48.814932 ], [ 2.501108, 48.815444 ], [ 2.502420, 48.816140 ] ], [ [ 2.481203, 48.882567 ], [ 2.477833, 48.886061 ], [ 2.475701, 48.890863 ], [ 2.475495, 48.894722 ], [ 2.475600, 48.897400 ], [ 2.475613, 48.900266 ], [ 2.474919, 48.901894 ], [ 2.473862, 48.903335 ], [ 2.472582, 48.904154 ], [ 2.470953, 48.904870 ], [ 2.468340, 48.905635 ], [ 2.465634, 48.905961 ], [ 2.462608, 48.906079 ], [ 2.458050, 48.905977 ], [ 2.452067, 48.906039 ], [ 2.446023, 48.906297 ], [ 2.442433, 48.906713 ], [ 2.440353, 48.907280 ], [ 2.437164, 48.909253 ], [ 2.434376, 48.911480 ], [ 2.432276, 48.912821 ], [ 2.429063, 48.914485 ], [ 2.425985, 48.915634 ], [ 2.423455, 48.916155 ], [ 2.419171, 48.916228 ], [ 2.414028, 48.916144 ], [ 2.408169, 48.915498 ], [ 2.403502, 48.914975 ], [ 2.398626, 48.914959 ], [ 2.388956, 48.916194 ], [ 2.377695, 48.918912 ], [ 2.375160, 48.919199 ], [ 2.371410, 48.919147 ], [ 2.363324, 48.918493 ], [ 2.354592, 48.917500 ], [ 2.351172, 48.917609 ], [ 2.349073, 48.918152 ], [ 2.346664, 48.918978 ] ], [ [ 2.502420, 48.816140 ], [ 2.493630, 48.841068 ], [ 2.489173, 48.854317 ], [ 2.487714, 48.858161 ], [ 2.485858, 48.863769 ], [ 2.485222, 48.865506 ], [ 2.485215, 48.867020 ], [ 2.485514, 48.868727 ], [ 2.486126, 48.871544 ], [ 2.486182, 48.874818 ], [ 2.485655, 48.876716 ], [ 2.484976, 48.878383 ], [ 2.483354, 48.880567 ], [ 2.481179, 48.882615 ] ], [ [ 2.502764, 48.816091 ], [ 2.502798, 48.816071 ] ], [ [ 2.502798, 48.816071 ], [ 2.502799, 48.816071 ], [ 2.502815, 48.816077 ], [ 2.502815, 48.816077 ] ], [ [ 2.502801, 48.816092 ], [ 2.502800, 48.816095 ] ], [ [ 2.502815, 48.816077 ], [ 2.502811, 48.816086 ] ], [ [ 2.502815, 48.816077 ], [ 2.502820, 48.816078 ] ] ],
	},
};
	
var purpleLineAlignmentLayer = new L.GeoJSON(purpleLineAlignmentFeature);
map.addLayer(purpleLineAlignmentLayer);

var baseMaps = {
	"MapBox": mapboxLayer,
    "OSM": osmLayer,
    "Aerial Photo": aerialLayer
};
	        
var overlayMaps = {
    "Analyst Tiles": analystLayer,
    "Stops": purpleLineStopsLayer,
	"Alignment": purpleLineAlignmentLayer
};

var initLocation = INIT_LOCATION;
if (AUTO_CENTER_MAP) {
	// attempt to get map metadata (bounds) from server
	var request = new XMLHttpRequest();
	request.open("GET", "/opentripplanner-api-webapp/ws/metadata", false); // synchronous request
	request.setRequestHeader("Accept", "application/xml");
	request.send(null);
	if (request.status == 200 && request.responseXML != null) {
		var x = request.responseXML;
		var minLat = parseFloat(x.getElementsByTagName('minLatitude')[0].textContent);
		var maxLat = parseFloat(x.getElementsByTagName('maxLatitude')[0].textContent);
		var minLon = parseFloat(x.getElementsByTagName('minLongitude')[0].textContent);
		var maxLon = parseFloat(x.getElementsByTagName('maxLongitude')[0].textContent);
		var lon = (minLon + maxLon) / 2;
		var lat = (minLat + maxLat) / 2;
		initLocation = new L.LatLng(lat, lon);
	}
}
map.setView(initLocation, 12);
var initLocation2 = new L.LatLng(initLocation.lat + 0.05, initLocation.lng + 0.05);

//Marker icons

var greenMarkerIcon = new L.Icon({ 
	iconUrl: 'js/lib/leaflet/images/marker-green.png', 
	iconSize:     [25, 41], // size of the icon
    iconAnchor:   [12, 39]  // point of the icon which will correspond to marker's location
});
var redMarkerIcon = new L.Icon({ 
	iconUrl: 'js/lib/leaflet/images/marker-red.png',
	iconSize:     [25, 41], // size of the icon
    iconAnchor:   [12, 39]  // point of the icon which will correspond to marker's location
});
var origMarker = new L.Marker(initLocation,  {draggable: true, icon: greenMarkerIcon });
var destMarker = new L.Marker(initLocation2, {draggable: true, icon: redMarkerIcon });
origMarker.on('dragend', mapSetupTool);
destMarker.on('dragend', mapSetupTool);

// add layers to map 
// do not add analyst layer yet -- it will be added in refresh() once params are pulled in

map.addLayer(mapboxLayer);
map.addLayer(origMarker);
map.addControl(new L.Control.Layers(baseMaps, overlayMaps));

var nclicks = 0;
function mapClick(e) {
	++nclicks;
	if (nclicks == 1) {
		setTimeout(function(){
	          if(nclicks == 1) {
	        	  // after n ms there was no other click: this is a single click
        	    origMarker.setLatLng(e.latlng);
        	    mapSetupTool();	            
	          } else {
	            // double clicked
	          }
	          nclicks = 0;
		}, 500);
	}
}
map.on('click', mapClick);

var params;

// use function statement rather than expression to allow hoisting -- is there a better way?
function mapSetupTool() {

	params = { 
		batch: true,
	};

	// pull search parameters from form
	switch($('#searchTypeSelect').val()) {
	case 'single':
		params.layers = 'traveltime';
		params.styles = 'color30';
		break;
	case 'ppa':
		params.layers = 'hagerstrand';
		params.styles = 'transparent';
		break;
	case 'diff2':
		params.layers = 'difference';
		params.styles = 'difference';
		break;
	case 'diff1':
		params.layers = 'difference';
		params.styles = 'difference';
		params.bannedRoutes = ["Test_Purple", ""];
		break;
	}
	// store one-element arrays so we can append as needed for the second search
	params.time = [$('#setupTime').val()];
	params.mode = [$('#setupMode').val()];
	params.maxWalkDistance = [$('#setupMaxDistance').val()];
	params.arriveBy = [$('#arriveByA').val()];
	switch($('#compressWaits').val()) {
		case 'optimize':
			params.reverseOptimizeOnTheFly = ['true'];
			break;
		case 'initial':
		default:
			params.clampInitialWait = [$('#timeLenience').val() * 60];
	}
	if (flags.twoSearch) {
		var pushIfDifferent = function (elementId, paramName) {
			console.log(elementId);
			var elemval = document.getElementById(elementId).value;
			if (elemval != 'same') {
				params[paramName].push(elemval);
			}
		};
		var args = [['setupTime2', 'time'],
		            ['setupMode2', 'mode'],
		            ['setupMaxDistance2', 'maxWalkDistance'],
		            ['arriveByB', 'arriveBy']];
		for (i in args) {
			pushIfDifferent.apply(this, args[i]);
		}
	}
    
    // get origin and destination coordinate from map markers
	var o = origMarker.getLatLng();
	params.fromPlace = [o.lat + ',' + o.lng];
    if (flags.twoEndpoint) {
    	var d = destMarker.getLatLng();
    	params.fromPlace.push(d.lat + ',' + d.lng);
    }
	// set from and to places to the same string(s) so they work for both arriveBy and departAfter
	params.toPlace = params.fromPlace;
    	
    var URL = analystUrl + buildQuery(params);
    console.log(params);
    console.log(URL);
    
    // is there a better way to trigger a refresh than removing and re-adding?
	if (analystLayer != null)
		map.removeLayer(analystLayer);
	analystLayer._url = URL;
    map.addLayer(analystLayer);
	legend.src = "/opentripplanner-api-webapp/ws/legend.png?width=300&height=40&styles=" 
		+ params.styles;

	return false;
};     

var downloadTool = function () { 
    var dlParams = {
        format: document.getElementById('downloadFormat').value,
        srs: document.getElementById('downloadProj').value,
        resolution: document.getElementById('downloadResolution').value
    };

    // TODO: this bounding box needs to be reprojected!
    var bounds = map.getBounds();
    var bbox;

    // reproject
    var src = new Proj4js.Proj('EPSG:4326');
    // TODO: undefined srs?
    var dest = new Proj4js.Proj(dlParams.srs);

    // wait until ready then execute
    var interval;
    interval = setInterval(function () {
        // if not ready, wait for next iteration
        if (!(src.readyToUse && dest.readyToUse))
            return;

        // clear the interval so this function is not called back.
        clearInterval(interval);

        var swll = bounds.getSouthWest();
        var nell = bounds.getNorthEast();
        
        var sw = new Proj4js.Point(swll.lng, swll.lat);
        var ne = new Proj4js.Point(nell.lng, nell.lat);

        Proj4js.transform(src, dest, sw);
        Proj4js.transform(src, dest, ne);

        // left, bot, right, top
        bbox = [sw.x, sw.y, ne.x, ne.y].join(',');

        var url = '/opentripplanner-api-webapp/ws/wms' +
            buildQuery(params) +
            '&format=' + dlParams.format + 
            '&srs=' + dlParams.srs +
            '&resolution=' + dlParams.resolution +
            '&bbox=' + bbox;
            // all of the from, to, time, &c. is taken care of by buildQuery.
        
        window.open(url);
    }, 1000); // this is the end of setInterval, run every 1s

    // prevent form submission
    return false;
};

var displayTimes = function(fractionalHours, fractionalHoursOffset) {
	console.log("fhour", fractionalHours);
	// console.log("offset", fractionalHoursOffset);
	var msec = BASE_DATE_MSEC + fractionalHours * MSEC_PER_HOUR; 
	document.getElementById('setupTime').value = new Date(msec).toISOString().substring(0,19);
	msec += fractionalHoursOffset * MSEC_PER_HOUR; 
	document.getElementById('setupTime2').value = new Date(msec).toISOString().substring(0,19);
};

function setFormDisabled(formName, disabled) {
	var form = document.forms[formName];
    var limit = form.elements.length;
    var i;
    for (i=0;i<limit;i++) {
    	console.log('   ', form.elements[i], disabled);
        form.elements[i].disabled = disabled;
    }
}


/* Bind JS functions to events (handle almost everything at the form level) */

// anytime a form element changes, refresh the map
$('#searchTypeForm').change( mapSetupTool );

// intercept slider change event bubbling to avoid frequent map rendering
(function(slider, offset) {
    slider.bind('change', function() {
    	displayTimes(slider.val(), offset.val()); 
        return false; // block event propagation
    }).change();
    slider.bind('mouseup', function() {
    	slider.parent().trigger('change');
    });
    offset.bind('change', function() {
    	displayTimes(slider.val(), offset.val()); 
    });
}) ($("#timeSlider"), $('#setupRelativeTime2'));

//hide some UI elements when they are irrelevant
$('#searchTypeSelect').change( function() { 
	var type = this.value;
	console.log('search type changed to', type);
	if (type == 'single' || type == 'diff1') {
		// switch to or stay in one-endpoint mode
		map.removeLayer(destMarker);
		flags.twoEndpoint = false;
	} else { 
		if (!(flags.twoEndpoint)) { 
			// switch from one-endpoint to two-endpoint mode
			var llo = origMarker.getLatLng();
			var lld = destMarker.getLatLng();
			lld.lat = llo.lat;
			lld.lng = llo.lng + 0.02;
			map.addLayer(destMarker);
			flags.twoEndpoint = true;
		}
	}
	if (type == 'single') {
		$('.secondaryControl').fadeOut( 500 );
		flags.twoSearch = false;
	} else { 
		$('.secondaryControl').fadeIn( 500 );
		flags.twoSearch = true;
	}
	if (type == 'ppa') {
		// lock arriveBy selectors and rename endpoints
		$('#headerA').text('Origin Setup');
		$('#headerB').text('Destination Setup');
		$('#arriveByA').val('false').prop('disabled', true);
		$('#arriveByB').val('true').prop('disabled', true);
	} else {
		$('#arriveByA').prop('disabled', false);
		$('#arriveByB').prop('disabled', false);
		if (type == 'single') {
			$('#headerA').text('Search Setup');
		} else {
			$('#headerA').text('Search A Setup');
			$('#headerB').text('Search B Setup');
		}
	}
}).change(); // trigger this event (and implicitly a form change event) immediately upon binding

