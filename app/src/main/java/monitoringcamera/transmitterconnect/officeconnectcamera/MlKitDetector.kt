package monitoringcamera.transmitterconnect.officeconnectcamera

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class MlKitDetector {
    private val TAG = "MlKitDetector"

    private val objectOptions = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .build()
    private val objectDetector = ObjectDetection.getClient(objectOptions)

    private val labelerOptions = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.5f)
        .build()
    private val labeler = ImageLabeling.getClient(labelerOptions)

    data class DetectionResult(
        val personBoxes: List<android.graphics.Rect> = emptyList(),
        val hasPet: Boolean = false,
        val hasVehicle: Boolean = false,
        val topLabel: String = ""
    )

    fun detect(bitmap: Bitmap): DetectionResult {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)

            val objectTask = objectDetector.process(image)
            val labelTask = labeler.process(image)

            val objects = Tasks.await(objectTask)
            val labels = Tasks.await(labelTask)

            var petConf = 0f
            var personConf = 0f
            var vehicleConf = 0f
            var topLabel = ""
            var topConf = 0f

            for (label in labels) {
                val text = label.text.lowercase()
                val conf = label.confidence
                
                if (conf > topConf) {
                    topConf = conf
                    topLabel = text
                }

                if (text.contains("dog") || text.contains("cat") || text.contains("bird") || 
                    text.contains("pet") || text.contains("animal") || text.contains("mammal") ||
                    text.contains("canidae") || text.contains("felidae")) {
                    if (conf > petConf) petConf = conf
                }
                
                if (text.contains("person") || text.contains("human") || text.contains("man") || 
                    text.contains("woman") || text.contains("boy") || text.contains("girl") ||
                    text.contains("clothing")) {
                    if (conf > personConf) personConf = conf
                }

                if (text.contains("car") || text.contains("vehicle") || text.contains("truck") || 
                    text.contains("bus") || text.contains("motorcycle") || text.contains("bicycle") ||
                    text.contains("land vehicle")) {
                    if (conf > vehicleConf) vehicleConf = conf
                }
            }

            val personBoxes = mutableListOf<android.graphics.Rect>()
            
            // Critical: If pet is the dominant classification, don't report it as a person.
            val isPet = petConf > 0.65f && petConf > personConf
            val isPerson = personConf > 0.65f && personConf >= petConf
            val isVehicle = vehicleConf > 0.65f

            if (isPerson) {
                for (obj in objects) {
                    var looksLikePerson = false
                    for (l in obj.labels) {
                        val t = l.text.lowercase()
                        if ((t == "person" || t == "human") && l.confidence > 0.4f) {
                            looksLikePerson = true
                        }
                    }
                    // If labeler is very sure of a person, accept generic object boxes as person boxes
                    if (looksLikePerson || (obj.labels.isEmpty() && personConf > 0.85f)) {
                        personBoxes.add(obj.boundingBox)
                    }
                }
            }

            DetectionResult(
                personBoxes = personBoxes,
                hasPet = isPet,
                hasVehicle = isVehicle,
                topLabel = topLabel
            )
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit detection error: ${e.message}")
            DetectionResult()
        }
    }
}
