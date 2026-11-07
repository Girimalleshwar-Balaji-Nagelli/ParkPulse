package com.parkpulse.app.presentation.createpark.components

import android.annotation.SuppressLint
import android.graphics.PointF
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.parkpulse.app.gis.GeoPoint
import com.parkpulse.app.presentation.map.components.rasterStyleJson
import com.parkpulse.app.presentation.map.components.rememberMapViewWithLifecycle
import com.parkpulse.app.presentation.map.components.toHex
import com.parkpulse.app.presentation.theme.ParksColors
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import kotlin.math.hypot

private const val SRC_POLYGON = "draft-polygon-src"
private const val SRC_LINE = "draft-line-src"
private const val SRC_VERTICES = "draft-vertices-src"
private const val LAYER_FILL = "draft-fill"
private const val LAYER_LINE = "draft-line"
private const val LAYER_VERTICES = "draft-vertices"

private const val START_LAT = 18.5204 // Pune centre: camera start only, not park data
private const val START_LNG = 73.8567
private const val START_ZOOM = 13.0

/**
 * Tap empty map  -> onAddPoint
 * Press a vertex and drag -> onMovePoint (map gestures are bypassed while dragging)
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
internal fun BoundaryEditorMap(
    points: List<GeoPoint>,
    isValid: Boolean,
    onAddPoint: (GeoPoint) -> Unit,
    onMovePoint: (Int, GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hitRadiusPx = with(LocalDensity.current) { 28.dp.toPx() }
    val mapView = rememberMapViewWithLifecycle()
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var loadedStyle by remember { mutableStateOf<Style?>(null) }

    val currentPoints by rememberUpdatedState(points)
    val currentAdd by rememberUpdatedState(onAddPoint)
    val currentMove by rememberUpdatedState(onMovePoint)

    val backgroundHex = ParksColors.BackgroundMint.toHex()
    val validHex = ParksColors.PrimaryGreen.toHex()
    val invalidHex = ParksColors.ErrorRed.toHex()
    val strokeHex = ParksColors.White.toHex()

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                var dragIndex = -1

                setOnTouchListener { v, event ->
                    val m = map
                    if (m == null) return@setOnTouchListener false
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            val idx = nearestVertex(m, currentPoints, event.x, event.y, hitRadiusPx)
                            if (idx >= 0) {
                                dragIndex = idx
                                v.parent?.requestDisallowInterceptTouchEvent(true)
                                true
                            } else false
                        }
                        MotionEvent.ACTION_MOVE -> if (dragIndex >= 0) {
                            val ll = m.projection.fromScreenLocation(PointF(event.x, event.y))
                            currentMove(dragIndex, GeoPoint(ll.latitude, ll.longitude))
                            true
                        } else false
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (dragIndex >= 0) {
                            dragIndex = -1
                            true
                        } else false
                        else -> dragIndex >= 0
                    }
                }

                getMapAsync { m ->
                    m.uiSettings.isCompassEnabled = true
                    m.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(START_LAT, START_LNG))
                        .zoom(START_ZOOM)
                        .build()
                    m.setStyle(Style.Builder().fromJson(rasterStyleJson(backgroundHex))) { style ->
                        installDraftLayers(style, validHex, strokeHex)
                        m.addOnMapClickListener { ll ->
                            currentAdd(GeoPoint(ll.latitude, ll.longitude))
                            true
                        }
                        map = m
                        loadedStyle = style
                    }
                }
            }
        },
    )

    LaunchedEffect(loadedStyle, points) {
        val style = loadedStyle ?: return@LaunchedEffect
        val pts = points.map { Point.fromLngLat(it.longitude, it.latitude) }
        val empty = FeatureCollection.fromFeatures(emptyList<Feature>())

        style.getSourceAs<GeoJsonSource>(SRC_POLYGON)?.setGeoJson(
            if (pts.size >= 3) {
                FeatureCollection.fromFeature(
                    Feature.fromGeometry(Polygon.fromLngLats(listOf(pts + pts.first())))
                )
            } else empty
        )
        style.getSourceAs<GeoJsonSource>(SRC_LINE)?.setGeoJson(
            if (pts.size >= 2) {
                val line = if (pts.size >= 3) pts + pts.first() else pts
                FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(line)))
            } else empty
        )
        style.getSourceAs<GeoJsonSource>(SRC_VERTICES)?.setGeoJson(
            FeatureCollection.fromFeatures(
                pts.mapIndexed { i, p -> Feature.fromGeometry(p).apply { addNumberProperty("index", i) } }
            )
        )
    }

    LaunchedEffect(loadedStyle, isValid) {
        val style = loadedStyle ?: return@LaunchedEffect
        val color = if (isValid) validHex else invalidHex
        style.getLayerAs<FillLayer>(LAYER_FILL)?.setProperties(fillColor(color))
        style.getLayerAs<LineLayer>(LAYER_LINE)?.setProperties(lineColor(color))
        style.getLayerAs<CircleLayer>(LAYER_VERTICES)?.setProperties(circleColor(color))
    }
}

private fun installDraftLayers(style: Style, colorHex: String, strokeHex: String) {
    val empty = FeatureCollection.fromFeatures(emptyList<Feature>())
    style.addSource(GeoJsonSource(SRC_POLYGON, empty))
    style.addSource(GeoJsonSource(SRC_LINE, empty))
    style.addSource(GeoJsonSource(SRC_VERTICES, empty))

    style.addLayer(
        FillLayer(LAYER_FILL, SRC_POLYGON).withProperties(fillColor(colorHex), fillOpacity(0.35f))
    )
    style.addLayer(
        LineLayer(LAYER_LINE, SRC_LINE).withProperties(lineColor(colorHex), lineWidth(3f))
    )
    style.addLayer(
        CircleLayer(LAYER_VERTICES, SRC_VERTICES).withProperties(
            circleRadius(9f),
            circleColor(colorHex),
            circleStrokeColor(strokeHex),
            circleStrokeWidth(2.5f),
        )
    )
}

private fun nearestVertex(
    map: MapLibreMap,
    points: List<GeoPoint>,
    x: Float,
    y: Float,
    maxDistPx: Float,
): Int {
    var best = -1
    var bestDist = maxDistPx
    points.forEachIndexed { i, p ->
        val sp = map.projection.toScreenLocation(LatLng(p.latitude, p.longitude))
        val d = hypot(sp.x - x, sp.y - y)
        if (d <= bestDist) {
            bestDist = d
            best = i
        }
    }
    return best
}