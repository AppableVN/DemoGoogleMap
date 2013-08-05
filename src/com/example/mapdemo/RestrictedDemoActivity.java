package com.example.mapdemo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.example.mapdemo.view.RestrictedMapView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RestrictedDemoActivity extends FragmentActivity implements
		OnMarkerDragListener, OnMarkerClickListener, OnMapClickListener {

	private RestrictedMapView mMapView;
	private GoogleMap mMap;
	private TextView mTopText;
	private ProgressBar progressBar;

	private State previousState = null;
	private List<State> lstStateUS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restricted_demo);

		mMapView = (RestrictedMapView) findViewById(R.id.map);
		mTopText = (TextView) findViewById(R.id.top_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		mMapView.onCreate(savedInstanceState);

		setUpMapIfNeeded();
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((MapView) findViewById(R.id.map)).getMap();
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		initMarkerInMap();

		mMap.setOnMarkerDragListener(this);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnMapClickListener(this);
		mMap.setMyLocationEnabled(true);

		// Setting an info window adapter allows us to change the both the
		// contents and look of the
		// info window.
		mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(
				RestrictedDemoActivity.this));
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				Toast.makeText(RestrictedDemoActivity.this,
						"You chose " + marker.getTitle(), Toast.LENGTH_SHORT)
						.show();
			}
		});

		initMyLocationGUI();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	private InputStream readStateXMLFile() {
		InputStream is = null;
		AssetManager assetManager = getAssets();
		try {
			is = assetManager.open("states.xml");
		} catch (Exception ex) {
			Log.e(getLocalClassName(), ex.toString());
		}
		return is;
	}

	private List<State> parse() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		List<State> lstStates = new ArrayList<State>();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(readStateXMLFile());
			Element root = dom.getDocumentElement();
			NodeList nodeListStates = root.getElementsByTagName("state");
			for (int i = 0; i < nodeListStates.getLength(); i++) {
				State state = new State();
				Element item = (Element) nodeListStates.item(i);
				state.stateName = item.getAttribute("name");
				state.colorState = item.getAttribute("colour");

				List<PointState> lstPoints = new ArrayList<PointState>();
				NodeList nodeListPoints = item.getElementsByTagName("point");
				for (int j = 0; j < nodeListPoints.getLength(); j++) {
					PointState pointState = new PointState();
					Element point = (Element) nodeListPoints.item(j);
					;
					pointState.lat = Double.parseDouble(point
							.getAttribute("lat"));
					pointState.lng = Double.parseDouble(point
							.getAttribute("lng"));
					lstPoints.add(pointState);
				}
				state.lstPoints = lstPoints;
				lstStates.add(state);
			}
		} catch (Exception e) {
			Log.e(getLocalClassName(), e.toString());
		}
		return lstStates;
	}

	private class State {
		public String stateName;
		public String colorState;
		public List<PointState> lstPoints;
		public LatLng centerPoint;
		public PolygonOptions polygonOptions;
		public Polygon polygon;
	}

	private class PointState {
		public double lat;
		public double lng;
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		mTopText.setText("onMarkerDrag.  Current Position: "
				+ marker.getPosition());
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		mTopText.setText("onMarkerDragEnd");
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		mTopText.setText("onMarkerDragStart");
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		Log.d(getLocalClassName(), "onMarkerClick");
		String titleMarker = marker.getTitle();
		if (previousState != null) {
			previousState.polygon.remove();
		}
		for (int i = 0; i < lstStateUS.size(); i++) {
			State state = lstStateUS.get(i);
			PolygonOptions polygonOptions = state.polygonOptions;
			polygonOptions.strokeColor(Color.BLACK);
			polygonOptions.strokeWidth(3);
			if (titleMarker.compareTo(state.stateName) == 0) {
				String stateColor = state.colorState;
				Polygon polygonMap = mMap.addPolygon(polygonOptions);
				int color = Color.parseColor(stateColor);
				color = Color.argb(88, Color.red(color), Color.green(color),
						Color.blue(color));
				polygonMap.setFillColor(color);
				state.polygon = polygonMap;
				previousState = state;
				break;
			}
		}

		animateCameraTo(marker.getPosition().latitude, marker.getPosition().longitude);
		marker.showInfoWindow();
		// We return false to indicate that we have not consumed the event and that we wish
		// for the default behavior to occur (which is for the camera to move such that the
		// marker is centered and for the marker's info window to open, if it has one).
		return true;
	}

	private void initMarkerInMap() {
		lstStateUS = parse();
		for (State state : lstStateUS) {
			PolygonOptions polygonOptions = new PolygonOptions();
			MarkerOptions markerOptions = new MarkerOptions();
			String stateName = state.stateName;
			String colorState = state.colorState;
			List<PointState> lstPointState = state.lstPoints;
			for (int i = 0; i < lstPointState.size(); i++) {
				PointState pointState = lstPointState.get(i);
				double lat = pointState.lat;
				double lng = pointState.lng;
				LatLng latlng = new LatLng(lat, lng);
				polygonOptions.add(latlng);
			}

			polygonOptions.strokeColor(Color.BLACK);
			polygonOptions.strokeWidth(3);

			state.polygonOptions = polygonOptions;
			Polygon polygonMap = mMap.addPolygon(polygonOptions);
			polygonMap.setFillColor(Color.alpha(Color.parseColor(colorState)));
			state.polygon = polygonMap;

			LatLng centerPoint = getPolyLineCentroid(lstPointState);
			state.centerPoint = centerPoint;
			markerOptions.position(state.centerPoint);
			markerOptions.title(stateName);
			markerOptions.draggable(true);
			mMap.addMarker(markerOptions);

		}
	}

	private void initMyLocationGUI() {
		// Get LocationManager object from System Service LOCATION_SERVICE
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// Create a criteria object to retrieve provider
		Criteria criteria = new Criteria();

		// Get the name of the best provider
		String provider = locationManager.getBestProvider(criteria, true);

		// Get Current Location
		Location myLocation = locationManager.getLastKnownLocation(provider);
		double myLat = myLocation.getLatitude();
		double myLng = myLocation.getLongitude();

		Log.d(getLocalClassName(), "myLat: " + myLat);
		Log.d(getLocalClassName(), "myLng: " + myLng);

		// 35.49422,-97.517624 Oklahoma City
		myLat = 35.49422;
		myLng = -97.517624;
		LatLng myLatLng = new LatLng(myLat, myLng);

		CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(myLat,
				myLng));
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(5);
		mMap.moveCamera(center);
		mMap.animateCamera(zoom);

		for (State state : lstStateUS) {
			List<PointState> lstPointState = state.lstPoints;
			boolean checkIsPointInPolygon = isPointInPolygon(lstPointState, myLatLng);
			if (checkIsPointInPolygon) {
				PolygonOptions polygonOptions = state.polygonOptions;
				polygonOptions.strokeColor(Color.BLACK);
				polygonOptions.strokeWidth(3);
				String stateColor = state.colorState;
				Polygon polygonMap = mMap
						.addPolygon(polygonOptions);
				int color = Color.parseColor(stateColor);
				color = Color.argb(88, Color.red(color),
						Color.green(color), Color.blue(color));
				polygonMap.setFillColor(color);
				state.polygon = polygonMap;
				previousState = state;
				break;
			}
		}

		progressBar.setVisibility(View.GONE);
		mMapView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onMapClick(final LatLng latlng) {
		Log.d(getLocalClassName(), "onMapClick");
		
		if (previousState != null) {
			previousState.polygon.remove();
		}
		for (State state : lstStateUS) {
			List<PointState> lstPointState = state.lstPoints;
			boolean checkIsPointInPolygon = isPointInPolygon(lstPointState, latlng);
			if (checkIsPointInPolygon) {
				PolygonOptions polygonOptions = state.polygonOptions;
				polygonOptions.strokeColor(Color.BLACK);
				polygonOptions.strokeWidth(3);
				String stateColor = state.colorState;
				Polygon polygonMap = mMap.addPolygon(polygonOptions);
				int color = Color.parseColor(stateColor);
				color = Color.argb(88, Color.red(color), Color.green(color),
						Color.blue(color));
				polygonMap.setFillColor(color);
				state.polygon = polygonMap;
				previousState = state;

				animateCameraTo(state.centerPoint.latitude, state.centerPoint.longitude);
				break;
			}
		}
	}

	private void animateCameraTo(final double lat, final double lng) {
		CameraPosition camPosition = mMap.getCameraPosition();
		if (!((Math.floor(camPosition.target.latitude * 100) / 100) == (Math
				.floor(lat * 100) / 100) && (Math
				.floor(camPosition.target.longitude * 100) / 100) == (Math
				.floor(lng * 100) / 100))) {
			mMap.getUiSettings().setScrollGesturesEnabled(false);
			mMap.animateCamera(
					CameraUpdateFactory.newLatLng(new LatLng(lat, lng)),
					500,
					new CancelableCallback() {

						@Override
						public void onFinish() {
							mMap.getUiSettings().setScrollGesturesEnabled(true);
							CameraUpdate zoom = CameraUpdateFactory.zoomTo(5);
							mMap.animateCamera(zoom);
						}

						@Override
						public void onCancel() {
							mMap.getUiSettings().setAllGesturesEnabled(true);
						}
					});
		}
	}

	private LatLng getPolyLineCentroid(List<PointState> lstPointState) {

		double centroidX = 0.0;
		double centroidY = 0.0;
		double signedArea = 0.0;
		double x0 = 0.0; // Current vertex X
		double y0 = 0.0; // Current vertex Y
		double x1 = 0.0; // Next vertex X
		double y1 = 0.0; // Next vertex Y
		double a = 0.0; // Partial signed area

		int i = 0;
		for (i = 0; i < lstPointState.size() - 1; ++i) {
			x0 = lstPointState.get(i).lat;
			y0 = lstPointState.get(i).lng;
			x1 = lstPointState.get(i + 1).lat;
			y1 = lstPointState.get(i + 1).lng;
			a = x0 * y1 - x1 * y0;
			signedArea += a;
			centroidX += (x0 + x1) * a;
			centroidY += (y0 + y1) * a;
		}

		x0 = lstPointState.get(i).lat;
		y0 = lstPointState.get(i).lng;
		x1 = lstPointState.get(0).lat;
		y1 = lstPointState.get(0).lng;
		a = x0 * y1 - x1 * y0;
		signedArea += a;
		centroidX += (x0 + x1) * a;
		centroidY += (y0 + y1) * a;

		signedArea *= 0.5;
		centroidX /= (6.0 * signedArea);
		centroidY /= (6.0 * signedArea);

		LatLng latlngResult = new LatLng(centroidX, centroidY);

		return latlngResult;
	}

	private boolean isPointInPolygon(List<PointState> lstPointState, LatLng point2Check) {
		boolean resultCheck = false;

		int size = lstPointState.size();
		int i, j = 0;
		for (i = 0, j = size - 1; i < lstPointState.size(); j = i++) {
			LatLng point_i = new LatLng(lstPointState.get(i).lat,
					lstPointState.get(i).lng);
			LatLng point_j = new LatLng(lstPointState.get(j).lat,
					lstPointState.get(j).lng);
			if (((point_i.longitude > point2Check.longitude) != (point_j.longitude > point2Check.longitude))
					&& (point2Check.latitude < (point_j.latitude - point_i.latitude)
							* (point2Check.longitude - point_i.longitude)
							/ (point_j.longitude - point_i.longitude)
							+ point_i.latitude))
				resultCheck = !resultCheck;

		}
		return resultCheck;
	}
}
