package com.parkpulse.app.presentation.map.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.parkpulse.app.presentation.map.MapPark
import com.parkpulse.app.presentation.theme.ParksColors
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.sources.GeoJsonSource

private const val PUNE_LAT = 18.5204
private const val PUNE_LNG = 73.8567
private const val INITIAL_ZOOM = 11.0

@Stable
class MapController {
    internal var map: MapLibreMap? = null

    fun moveTo(location: Location, zoom: Double = 15.0) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), zoom),
            600,
        )
    }
}

@Composable
fun rememberMapController(): MapController = remember { MapController() }

@Composable
internal fun ParkMapView(
    controller: MapController,
    parks: List<MapPark>,
    selectedParkId: String?,
    userLocation: Location?,
    hasLocationPermission: Boolean,
    onParkTapped: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnTap by rememberUpdatedState(onParkTapped)

    val backgroundHex = ParksColors.BackgroundMint.toHex()
    val selectedHex = ParksColors.DarkGreen.toHex()
    val statusHex = remember {
        StatusHex(
            open = ParksColors.StatusOpen.toHex(),
            repair = ParksColors.StatusRepair.toHex(),
            closed = ParksColors.StatusClosed.toHex(),
        )
    }

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }
    var loadedStyle by remember { mutableStateOf<Style?>(null) }

    // Forward lifecycle to MapView.
    DisposableEffect(lifecycleOwner, mapView) {
        var destroyed = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> { destroyed = true; mapView.onDestroy() }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (!destroyed) {
                val s = lifecycleOwner.lifecycle.currentState
                if (s.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
                if (s.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
                mapView.onDestroy()
            }
            controller.map = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    controller.map = map
                    map.uiSettings.apply {
                        isCompassEnabled = true
                        isZoomGesturesEnabled = true
                        isScrollGesturesEnabled = true
                        isRotateGesturesEnabled = true
                        isTiltGesturesEnabled = true
                        isDoubleTapGesturesEnabled = true
                    }
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(PUNE_LAT, PUNE_LNG))
                        .zoom(INITIAL_ZOOM)
                        .build()

                    map.setStyle(Style.Builder().fromJson(rasterStyleJson(backgroundHex))) { style ->
                        installParkLayers(style, selectedHex)
                        map.addOnMapClickListener { latLng ->
                            val screenPoint = map.projection.toScreenLocation(latLng)
                            val hit = map.queryRenderedFeatures(screenPoint, MapIds.PARKS_FILL).firstOrNull()
                            val id = hit?.getStringProperty("id")
                            currentOnTap(id)
                            id != null
                        }
                        loadedStyle = style
                    }
                }
            }
        },
    )

    // Parks from Room -> GeoJSON source. No network involved.
    LaunchedEffect(loadedStyle, parks) {
        val style = loadedStyle ?: return@LaunchedEffect
        style.getSourceAs<GeoJsonSource>(MapIds.PARKS_SOURCE)
            ?.setGeoJson(parks.toFeatureCollection(statusHex))
    }

    // Highlight selected polygon.
    LaunchedEffect(loadedStyle, selectedParkId) {
        val style = loadedStyle ?: return@LaunchedEffect
        style.getLayerAs<LineLayer>(MapIds.PARKS_SELECTED)?.setFilter(selectedFilter(selectedParkId))
    }

    // User location dot, fed from the Fused provider (MapLibre's own engine is disabled).
    LaunchedEffect(loadedStyle, hasLocationPermission, userLocation) {
        val style = loadedStyle ?: return@LaunchedEffect
        val map = controller.map ?: return@LaunchedEffect
        if (!hasLocationPermission) return@LaunchedEffect
        activateLocationComponent(context, map, style)
        userLocation?.let { map.locationComponent.forceLocationUpdate(it) }
    }
}

@SuppressLint("MissingPermission")
private fun activateLocationComponent(context: Context, map: MapLibreMap, style: Style) {
    val lc = map.locationComponent
    if (!lc.isLocationComponentActivated) {
        lc.activateLocationComponent(
            LocationComponentActivationOptions.builder(context, style)
                .useDefaultLocationEngine(false)
                .build()
        )
    }
    lc.isLocationComponentEnabled = true
    lc.cameraMode = CameraMode.NONE
    lc.renderMode = RenderMode.NORMAL
}
