package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class CountryCurrencyInfo(
    val countryCode: String,
    val countryNameNl: String,
    val currencyCode: String,
    val flagEmoji: String
)

object CountryCurrencyCatalog {
    val countryMap = mapOf(
        "TR" to CountryCurrencyInfo("TR", "Turkije", "TRY", "🇹🇷"),
        "GB" to CountryCurrencyInfo("GB", "het Verenigd Koninkrijk", "GBP", "🇬🇧"),
        "US" to CountryCurrencyInfo("US", "de Verenigde Staten", "USD", "🇺🇸"),
        "JP" to CountryCurrencyInfo("JP", "Japan", "JPY", "🇯🇵"),
        "CH" to CountryCurrencyInfo("CH", "Zwitserland", "CHF", "🇨🇭"),
        "PL" to CountryCurrencyInfo("PL", "Polen", "PLN", "🇵🇱"),
        "SE" to CountryCurrencyInfo("SE", "Zweden", "SEK", "🇸🇪"),
        "NO" to CountryCurrencyInfo("NO", "Noorwegen", "NOK", "🇳🇴"),
        "DK" to CountryCurrencyInfo("DK", "Denemarken", "DKK", "🇩🇰"),
        "CZ" to CountryCurrencyInfo("CZ", "Tsjechië", "CZK", "🇨🇿"),
        "HU" to CountryCurrencyInfo("HU", "Hongarije", "HUF", "🇭🇺"),
        "RO" to CountryCurrencyInfo("RO", "Roemenië", "RON", "🇷🇴"),
        "BG" to CountryCurrencyInfo("BG", "Bulgarije", "BGN", "🇧🇬"),
        "CA" to CountryCurrencyInfo("CA", "Canada", "CAD", "🇨🇦"),
        "AU" to CountryCurrencyInfo("AU", "Australië", "AUD", "🇦🇺"),
        "AE" to CountryCurrencyInfo("AE", "de VAE", "AED", "🇦🇪"),
        "SA" to CountryCurrencyInfo("SA", "Saoedi-Arabië", "SAR", "🇸🇦"),
        "MA" to CountryCurrencyInfo("MA", "Marokko", "MAD", "🇲🇦"),
        "KR" to CountryCurrencyInfo("KR", "Zuid-Korea", "KRW", "🇰🇷"),
        "SG" to CountryCurrencyInfo("SG", "Singapore", "SGD", "🇸🇬"),
        "HK" to CountryCurrencyInfo("HK", "Hongkong", "HKD", "🇭🇰"),
        "TH" to CountryCurrencyInfo("TH", "Thailand", "THB", "🇹🇭"),
        "ID" to CountryCurrencyInfo("ID", "Indonesië", "IDR", "🇮🇩"),
        "MY" to CountryCurrencyInfo("MY", "Maleisië", "MYR", "🇲🇾"),
        "BR" to CountryCurrencyInfo("BR", "Brazilië", "BRL", "🇧🇷"),
        "MX" to CountryCurrencyInfo("MX", "Mexico", "MXN", "🇲🇽"),
        "EG" to CountryCurrencyInfo("EG", "Egypte", "EGP", "🇪🇬"),
        "IN" to CountryCurrencyInfo("IN", "India", "INR", "🇮🇳"),
        "CN" to CountryCurrencyInfo("CN", "China", "CNY", "🇨🇳"),
        // Eurozone
        "NL" to CountryCurrencyInfo("NL", "Nederland", "EUR", "🇳🇱"),
        "DE" to CountryCurrencyInfo("DE", "Duitsland", "EUR", "🇩🇪"),
        "BE" to CountryCurrencyInfo("BE", "België", "EUR", "🇧🇪"),
        "FR" to CountryCurrencyInfo("FR", "Frankrijk", "EUR", "🇫🇷"),
        "ES" to CountryCurrencyInfo("ES", "Spanje", "EUR", "🇪🇸"),
        "IT" to CountryCurrencyInfo("IT", "Italië", "EUR", "🇮🇹"),
        "PT" to CountryCurrencyInfo("PT", "Portugal", "EUR", "🇵🇹"),
        "GR" to CountryCurrencyInfo("GR", "Griekenland", "EUR", "🇬🇷"),
        "AT" to CountryCurrencyInfo("AT", "Oostenrijk", "EUR", "🇦🇹"),
        "IE" to CountryCurrencyInfo("IE", "Ierland", "EUR", "🇮🇪"),
        "FI" to CountryCurrencyInfo("FI", "Finland", "EUR", "🇫🇮")
    )

    fun getByCountryCode(code: String): CountryCurrencyInfo? {
        return countryMap[code.uppercase()]
    }
}

class LocationDetector(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    suspend fun detectCurrentCountry(): CountryCurrencyInfo? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext null
        }

        try {
            val location = getLastKnownLocation()
            if (location != null) {
                val countryCode = getCountryCodeFromCoordinates(location.latitude, location.longitude)
                if (!countryCode.isNullOrBlank()) {
                    return@withContext CountryCurrencyCatalog.getByCountryCode(countryCode)
                }
            }
        } catch (e: Exception) {
            Log.e("LocationDetector", "Error retrieving location", e)
        }

        // Fallback: Telephony or Locale
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simCountry = telephonyManager?.simCountryIso
            if (!simCountry.isNullOrBlank()) {
                val found = CountryCurrencyCatalog.getByCountryCode(simCountry)
                if (found != null) return@withContext found
            }
        } catch (_: Exception) {}

        return@withContext null
    }

    private suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { cont ->
        try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc)
                    } else {
                        // Fallback to last location
                        fusedClient.lastLocation
                            .addOnSuccessListener { lastLoc -> cont.resume(lastLoc) }
                            .addOnFailureListener { cont.resume(null) }
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        } catch (e: SecurityException) {
            cont.resume(null)
        }
    }

    @Suppress("DEPRECATION")
    private fun getCountryCodeFromCoordinates(lat: Double, lon: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].countryCode
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationDetector", "Geocoder failed", e)
            null
        }
    }
}
