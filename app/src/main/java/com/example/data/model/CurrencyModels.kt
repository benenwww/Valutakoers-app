package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flagEmoji: String
)

@JsonClass(generateAdapter = true)
data class ExchangeRateApiResponse(
    val result: String? = null,
    val provider: String? = null,
    @Json(name = "time_last_update_unix") val timeLastUpdateUnix: Long? = null,
    @Json(name = "rates") val rates: Map<String, Double> = emptyMap()
)

object CurrencyCatalog {
    val allCurrencies = listOf(
        CurrencyInfo("EUR", "Euro", "€", "🇪🇺"),
        CurrencyInfo("TRY", "Turkse lira", "₺", "🇹🇷"),
        CurrencyInfo("USD", "Amerikaanse dollar", "$", "🇺🇸"),
        CurrencyInfo("GBP", "Britse pond", "£", "🇬🇧"),
        CurrencyInfo("CHF", "Zwitserse frank", "Fr", "🇨🇭"),
        CurrencyInfo("JPY", "Japanse yen", "¥", "🇯🇵"),
        CurrencyInfo("CAD", "Canadese dollar", "CA$", "🇨🇦"),
        CurrencyInfo("AUD", "Australische dollar", "AU$", "🇦🇺"),
        CurrencyInfo("PLN", "Poolse zloty", "zł", "🇵🇱"),
        CurrencyInfo("SEK", "Zweedse kroon", "kr", "🇸🇪"),
        CurrencyInfo("NOK", "Noorse kroon", "kr", "🇳🇴"),
        CurrencyInfo("DKK", "Deense kroon", "kr", "🇩🇰"),
        CurrencyInfo("CZK", "Tsjechische kroon", "Kč", "🇨🇿"),
        CurrencyInfo("HUF", "Hongaarse forint", "Ft", "🇭🇺"),
        CurrencyInfo("RON", "Roemeense leu", "lei", "🇷🇴"),
        CurrencyInfo("BGN", "Bulgaarse lev", "лв", "🇧🇬"),
        CurrencyInfo("INR", "Indiase roepie", "₹", "🇮🇳"),
        CurrencyInfo("CNY", "Chinese yuan", "¥", "🇨🇳"),
        CurrencyInfo("BRL", "Braziliaanse real", "R$", "🇧🇷"),
        CurrencyInfo("MXN", "Mexicaanse peso", "Mex$", "🇲🇽"),
        CurrencyInfo("AED", "VAE-dirham", "د.إ", "🇦🇪"),
        CurrencyInfo("SAR", "Saoedische riyal", "﷼", "🇸🇦"),
        CurrencyInfo("MAD", "Marokkaanse dirham", "DH", "🇲🇦"),
        CurrencyInfo("KRW", "Zuid-Koreaanse won", "₩", "🇰🇷"),
        CurrencyInfo("SGD", "Singaporese dollar", "S$", "🇸🇬"),
        CurrencyInfo("HKD", "Hongkongse dollar", "HK$", "🇭🇰"),
        CurrencyInfo("NZD", "Nieuw-Zeelandse dollar", "NZ$", "🇳🇿"),
        CurrencyInfo("ZAR", "Zuid-Afrikaanse rand", "R", "🇿🇦"),
        CurrencyInfo("THB", "Thaise baht", "฿", "🇹🇭"),
        CurrencyInfo("IDR", "Indonesische roepia", "Rp", "🇮🇩"),
        CurrencyInfo("MYR", "Maleisische ringgit", "RM", "🇲🇾"),
        CurrencyInfo("PHP", "Filipijnse peso", "₱", "🇵🇭"),
        CurrencyInfo("ILS", "Israëlische sjekel", "₪", "🇮🇱"),
        CurrencyInfo("EGP", "Egyptische pond", "E£", "🇪🇬"),
        CurrencyInfo("CLP", "Chileense peso", "CLP$", "🇨🇱"),
        CurrencyInfo("COP", "Colombiaanse peso", "COL$", "🇨🇴"),
        CurrencyInfo("ARS", "Argentijnse peso", "$", "🇦🇷"),
        CurrencyInfo("ISK", "IJslandse kroon", "kr", "🇮🇸"),
        CurrencyInfo("HRK", "Kroatische kuna", "kn", "🇭🇷")
    )

    private val mapByCode = allCurrencies.associateBy { it.code }

    fun getCurrencyInfo(code: String): CurrencyInfo {
        return mapByCode[code.uppercase()] ?: CurrencyInfo(
            code = code.uppercase(),
            name = code.uppercase(),
            symbol = code.uppercase(),
            flagEmoji = "🌐"
        )
    }

    // Default seed rates relative to EUR (approximate baseline for immediate offline use on first start)
    val defaultSeedRatesToEur: Map<String, Double> = mapOf(
        "EUR" to 1.0,
        "TRY" to 54.9451, // 100 EUR = 5.494,51 TRY
        "USD" to 1.09,
        "GBP" to 0.86,
        "CHF" to 0.94,
        "JPY" to 164.5,
        "CAD" to 1.48,
        "AUD" to 1.66,
        "PLN" to 4.31,
        "SEK" to 11.45,
        "NOK" to 11.75,
        "DKK" to 7.46,
        "CZK" to 25.10,
        "HUF" to 398.2,
        "RON" to 4.97,
        "BGN" to 1.95,
        "INR" to 91.2,
        "CNY" to 7.85,
        "BRL" to 5.95,
        "MXN" to 21.3,
        "AED" to 4.00,
        "SAR" to 4.09,
        "MAD" to 10.82,
        "KRW" to 1485.0,
        "SGD" to 1.45,
        "HKD" to 8.52,
        "NZD" to 1.82,
        "ZAR" to 19.8,
        "THB" to 39.4,
        "IDR" to 17200.0,
        "MYR" to 4.82,
        "PHP" to 62.4,
        "ILS" to 4.05,
        "EGP" to 52.8
    )
}
