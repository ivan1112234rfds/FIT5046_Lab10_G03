import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.activitymanager.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "PlacesAutocomplete"

@Composable
fun PlacesAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (String, LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val placesManager = remember { PlacesManager(context) }
    val predictions by placesManager.predictions.collectAsState()
    var isDropdownVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)

                if (newValue.length >= 2) {
                    placesManager.searchPlaces(newValue)
                    isDropdownVisible = true
                } else {
                    isDropdownVisible = false
                    placesManager.clearPredictions()
                }
            },
            label = { Text("搜索地点") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (isDropdownVisible && predictions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(predictions) { prediction ->
                        ListItem(
                            headlineContent = {
                                Text(prediction.getFullText(null).toString())
                            },
                            modifier = Modifier
                                .clickable {
                                    val fullText = prediction.getFullText(null).toString()
                                    onValueChange(fullText)
                                    isDropdownVisible = false

                                    placesManager.getPlaceDetails(prediction.placeId) { latLng ->
                                        if (latLng != null) {
                                            onLocationSelected(fullText, latLng)
                                        }
                                    }
                                }
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

class PlacesManager(context: android.content.Context) {
    private val _predictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val predictions = _predictions.asStateFlow()

    private val placesClient: PlacesClient

    init {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, context.getString(R.string.google_maps_key))
        }
        placesClient = Places.createClient(context)
    }

    fun searchPlaces(query: String) {
        val autoCompletePlacesRequest = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(autoCompletePlacesRequest)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                _predictions.value = predictions
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "some exception happened" + exception.message)
                _predictions.value = emptyList()
            }
    }

    fun getPlaceDetails(placeId: String, callback: (LatLng?) -> Unit) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                callback(response.place.latLng)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching place details: ${exception.message}")
                callback(null)
            }
    }

    fun clearPredictions() {
        _predictions.value = emptyList()
    }
}