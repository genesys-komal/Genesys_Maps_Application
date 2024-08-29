import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.example.mapapplication.helpers.toPoint
import com.example.mapapplication.viewModels.MainViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap

class CustomOverlayView(
    context: Context,
    private val map: MapboxMap,viewModel: MainViewModel
) : View(context) {

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        style = Paint.Style.FILL
    }

    private var hoverLatLng: LatLng? = null

    init {
        // Set up touch listener
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val screenLatLng = map.projection.fromScreenLocation(
                    PointF(event.x,event.y)

                )
                hoverLatLng = screenLatLng
                invalidate() // Redraw the view
                true
            } else {
                false
            }
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        hoverLatLng?.let { latLng ->
            // Convert latLng to screen coordinates
            val projection = map.projection
            val screenPos = projection.toScreenLocation(latLng)

            // Draw a circle at the screen position
            canvas.drawCircle(screenPos.x.toFloat(), screenPos.y.toFloat(), 20f, paint)

            // Draw text
            canvas.drawText(
                "Lat: ${latLng.latitude}, Lon: ${latLng.longitude}",
                screenPos.x.toFloat(),
                screenPos.y.toFloat() - 30f,
                textPaint
            )
        }
    }
}
