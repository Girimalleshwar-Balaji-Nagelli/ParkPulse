package com.parkpulse.app.presentation.map.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.parkpulse.app.gis.PolygonUtils
import com.parkpulse.app.presentation.map.MapPark
import com.parkpulse.app.domain.model.ParkStatus
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

internal object MapIds {
    const val PARKS_SOURCE = "parks-source"
    const val PARKS_FILL = "parks-fill"
    const val PARKS_OUTLINE = "parks-outline"
    const val PARKS_SELECTED = "parks-selected"
}

internal class StatusHex(val open: String, val repair: String, val closed: String) {
    fun of(status: ParkStatus) = when (status) {
        ParkStatus.OPEN -> open
        ParkStatus.NEEDS_REPAIR -> repair
        ParkStatus.CLOSED -> closed
    }
}

internal fun Color.toHex(): String = String.format("#%06X", 0xFFFFFF and toArgb())

/**
 * Self-contained style: the JSON is local, so the style (and therefore the polygons) loads with no
 * network. Raster tiles are fetched when online and served from MapLibre's ambient cache when seen before.
 * NOTE: tile.openstreetmap.org is fine for development only. Switch to a provider you have rights to
 * (MapTiler, Stadia, self-hosted) before release.
 */
internal fun rasterStyleJson(backgroundHex: String): String = """
{
  "version": 8,
  "name": "ParkPulse",
  "sources": {
    "base": {
      "type": "raster",
      "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "maxzoom": 19,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    { "id": "bg", "type": "background", "paint": { "background-color": "$backgroundHex" } },
    { "id": "base-raster", "type": "raster", "source": "base" }
  ]
}
""".trimIndent()

internal fun installParkLayers(style: Style, selectedOutlineHex: String) {
    style.addSource(GeoJsonSource(MapIds.PARKS_SOURCE, FeatureCollection.fromFeatures(emptyList<Feature>())))

    style.addLayer(
        FillLayer(MapIds.PARKS_FILL, MapIds.PARKS_SOURCE).withProperties(
            fillColor(Expression.toColor(Expression.get("color"))),
            fillOpacity(0.45f),
        )
    )
    style.addLayer(
        LineLayer(MapIds.PARKS_OUTLINE, MapIds.PARKS_SOURCE).withProperties(
            lineColor(Expression.toColor(Expression.get("color"))),
            lineWidth(2f),
        )
    )
    style.addLayer(
        LineLayer(MapIds.PARKS_SELECTED, MapIds.PARKS_SOURCE).withProperties(
            lineColor(selectedOutlineHex),
            lineWidth(4f),
        ).withFilter(selectedFilter(null))
    )
}

internal fun selectedFilter(parkId: String?): Expression =
    Expression.eq(Expression.get("id"), Expression.literal(parkId ?: ""))

internal fun List<MapPark>.toFeatureCollection(colors: StatusHex): FeatureCollection {
    val features = map { park ->
        val ring = PolygonUtils.closed(park.ring).map { Point.fromLngLat(it.longitude, it.latitude) }
        Feature.fromGeometry(Polygon.fromLngLats(listOf(ring))).apply {
            addStringProperty("id", park.id)
            addStringProperty("color", colors.of(park.status))
        }
    }
    return FeatureCollection.fromFeatures(features)
}