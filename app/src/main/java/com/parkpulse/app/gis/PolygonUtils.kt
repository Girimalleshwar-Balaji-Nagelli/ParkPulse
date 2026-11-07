package com.parkpulse.app.gis

import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

object PolygonUtils {

    private const val MIN_AREA_SQM = 1.0
    private const val R = DistanceCalculator.EARTH_RADIUS_M
    private const val EPS = 1e-6
    const val SQM_PER_ACRE = 4046.8564224

    /**
     * Returns an open ring (first point NOT repeated at the end) or null if the
     * boundary is unusable: invalid coordinates, fewer than 3 distinct points,
     * or a degenerate (zero-area) shape.
     */
    fun normalizeRing(points: List<GeoPoint>): List<GeoPoint>? {
        if (points.isEmpty() || points.any { !it.isValid }) return null
        val open = if (points.size > 1 && points.first() == points.last()) points.dropLast(1) else points
        val ring = open.distinct()
        if (ring.size < 3) return null
        if (areaSquareMeters(ring) < MIN_AREA_SQM) return null
        return ring
    }

    /** Ring with the first point repeated at the end (GeoJSON requirement). */
    fun closed(ring: List<GeoPoint>): List<GeoPoint> =
        if (ring.first() == ring.last()) ring else ring + ring.first()

    /** Planar area on a local equirectangular projection. Accurate for park-sized polygons. */
    fun areaSquareMeters(ring: List<GeoPoint>): Double {
        if (ring.size < 3) return 0.0
        val origin = GeoPoint(ring.map { it.latitude }.average(), ring.map { it.longitude }.average())
        val xy = ring.map { project(it, origin) }
        var sum = 0.0
        for (i in xy.indices) {
            val a = xy[i]
            val b = xy[(i + 1) % xy.size]
            sum += a.x * b.y - b.x * a.y
        }
        return abs(sum) / 2.0
    }

    fun sqmToAcres(sqm: Double): Double = sqm / SQM_PER_ACRE

    /** Closed perimeter in metres of an OPEN ring (the closing edge is added). */
    fun perimeterMeters(ring: List<GeoPoint>): Double {
        if (ring.size < 2) return 0.0
        var sum = 0.0
        for (i in ring.indices) {
            sum += DistanceCalculator.meters(ring[i], ring[(i + 1) % ring.size])
        }
        return sum
    }

    /**
     * True if any two edges of the OPEN ring cross, touch, or overlap, or if two adjacent
     * edges fold back onto each other. Edges are tested on a local metre projection.
     */
    fun isSelfIntersecting(ring: List<GeoPoint>): Boolean {
        val n = ring.size
        if (n < 3) return false
        val origin = ring[0]
        val p = ring.map { project(it, origin) }
        for (i in 0 until n) {
            val a = p[i]
            val b = p[(i + 1) % n]
            val c = p[(i + 2) % n]
            if (foldsBack(a, b, c)) return true
            for (j in i + 2 until n) {
                if (i == 0 && j == n - 1) continue // adjacent via the closing edge
                if (segmentsIntersect(a, b, p[j], p[(j + 1) % n])) return true
            }
        }
        return false
    }

    /** Ray-casting point-in-polygon. */
    fun contains(ring: List<GeoPoint>, point: GeoPoint): Boolean {
        var inside = false
        var j = ring.lastIndex
        for (i in ring.indices) {
            val pi = ring[i]
            val pj = ring[j]
            val crosses = (pi.latitude > point.latitude) != (pj.latitude > point.latitude) &&
                    point.longitude < (pj.longitude - pi.longitude) * (point.latitude - pi.latitude) /
                    (pj.latitude - pi.latitude) + pi.longitude
            if (crosses) inside = !inside
            j = i
        }
        return inside
    }

    /** Closest point on the polygon edge to [from]. Call only when [from] is outside the ring. */
    fun nearestPointOnBoundary(ring: List<GeoPoint>, from: GeoPoint): GeoPoint {
        val xy = ring.map { project(it, from) } // `from` is the origin (0,0)
        var bestD = Double.MAX_VALUE
        var bx = 0.0
        var by = 0.0
        for (i in xy.indices) {
            val a = xy[i]
            val b = xy[(i + 1) % xy.size]
            val dx = b.x - a.x
            val dy = b.y - a.y
            val len2 = dx * dx + dy * dy
            val t = if (len2 == 0.0) 0.0 else ((-a.x * dx - a.y * dy) / len2).coerceIn(0.0, 1.0)
            val px = a.x + t * dx
            val py = a.y + t * dy
            val d = px * px + py * py
            if (d < bestD) {
                bestD = d; bx = px; by = py
            }
        }
        return unproject(bx, by, from)
    }

    fun formatArea(sqm: Double): String {
        val loc = Locale.getDefault()
        return if (sqm >= 10_000) String.format(loc, "%.2f ha (%,.0f m²)", sqm / 10_000.0, sqm)
        else String.format(loc, "%,.0f m²", sqm)
    }

    private class XY(val x: Double, val y: Double)

    private fun project(p: GeoPoint, origin: GeoPoint): XY {
        val cosLat = cos(Math.toRadians(origin.latitude))
        return XY(
            Math.toRadians(p.longitude - origin.longitude) * R * cosLat,
            Math.toRadians(p.latitude - origin.latitude) * R,
        )
    }

    private fun unproject(x: Double, y: Double, origin: GeoPoint): GeoPoint {
        val cosLat = cos(Math.toRadians(origin.latitude))
        return GeoPoint(
            origin.latitude + Math.toDegrees(y / R),
            origin.longitude + Math.toDegrees(x / (R * cosLat)),
        )
    }

    private fun orient(a: XY, b: XY, c: XY): Double =
        (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)

    private fun sgn(v: Double): Int = when {
        v > EPS -> 1
        v < -EPS -> -1
        else -> 0
    }

    private fun onSegment(a: XY, b: XY, c: XY): Boolean =
        c.x <= max(a.x, b.x) + EPS && c.x >= min(a.x, b.x) - EPS &&
                c.y <= max(a.y, b.y) + EPS && c.y >= min(a.y, b.y) - EPS

    private fun segmentsIntersect(p1: XY, p2: XY, p3: XY, p4: XY): Boolean {
        val o1 = sgn(orient(p1, p2, p3))
        val o2 = sgn(orient(p1, p2, p4))
        val o3 = sgn(orient(p3, p4, p1))
        val o4 = sgn(orient(p3, p4, p2))
        if (o1 != o2 && o3 != o4) return true
        if (o1 == 0 && onSegment(p1, p2, p3)) return true
        if (o2 == 0 && onSegment(p1, p2, p4)) return true
        if (o3 == 0 && onSegment(p3, p4, p1)) return true
        if (o4 == 0 && onSegment(p3, p4, p2)) return true
        return false
    }

    /** Adjacent edges a->b and b->c that run along the same line in opposite directions. */
    private fun foldsBack(a: XY, b: XY, c: XY): Boolean {
        val cross = orient(a, b, c)
        val dot = (b.x - a.x) * (c.x - b.x) + (b.y - a.y) * (c.y - b.y)
        return sgn(cross) == 0 && dot < 0
    }
}