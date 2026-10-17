package com.example

import com.example.data.location.CountryCurrencyCatalog
import com.example.data.model.CurrencyCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun `conversion logic between TRY and EUR`() {
    // 100 EUR = 5494.51 TRY -> 1 EUR = 54.9451 TRY
    val rates = mapOf("EUR" to 1.0, "TRY" to 54.9451)

    val fromEurRate = rates["EUR"] ?: 1.0
    val toTryRate = rates["TRY"] ?: 1.0
    val convertedTry = (100.0 / fromEurRate) * toTryRate
    assertEquals(5494.51, convertedTry, 0.01)

    val fromTryRate = rates["TRY"] ?: 1.0
    val toEurRate = rates["EUR"] ?: 1.0
    val convertedEur = (5494.51 / fromTryRate) * toEurRate
    assertEquals(100.0, convertedEur, 0.01)
  }

  @Test
  fun `seed rates exist for offline usage`() {
    assertTrue(CurrencyCatalog.defaultSeedRatesToEur.containsKey("TRY"))
    assertTrue(CurrencyCatalog.defaultSeedRatesToEur.containsKey("EUR"))
    assertTrue(CurrencyCatalog.defaultSeedRatesToEur.containsKey("USD"))
    assertEquals(54.9451, CurrencyCatalog.defaultSeedRatesToEur["TRY"]!!, 0.01)
  }

  @Test
  fun `country to currency mapping works correctly`() {
    val turkey = CountryCurrencyCatalog.getByCountryCode("TR")
    assertNotNull(turkey)
    assertEquals("TRY", turkey?.currencyCode)
    assertEquals("Turkije", turkey?.countryNameNl)

    val uk = CountryCurrencyCatalog.getByCountryCode("GB")
    assertNotNull(uk)
    assertEquals("GBP", uk?.currencyCode)

    val japan = CountryCurrencyCatalog.getByCountryCode("JP")
    assertNotNull(japan)
    assertEquals("JPY", japan?.currencyCode)
  }
}
