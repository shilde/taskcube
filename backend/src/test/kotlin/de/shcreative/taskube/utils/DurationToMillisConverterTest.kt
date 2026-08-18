package de.shcreative.taskube.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DurationToMillisConverterTest {

    private val converter = DurationToMillisConverter()

    @Test
    fun `convertToDatabaseColumn converts duration to milliseconds`() {
        assertEquals(5_000L, converter.convertToDatabaseColumn(5.seconds))
    }

    @Test
    fun `convertToDatabaseColumn returns null for null input`() {
        assertNull(converter.convertToDatabaseColumn(null))
    }

    @Test
    fun `convertToEntityAttribute converts milliseconds to duration`() {
        assertEquals(5_000.milliseconds, converter.convertToEntityAttribute(5_000L))
    }

    @Test
    fun `convertToEntityAttribute returns null for null input`() {
        assertNull(converter.convertToEntityAttribute(null))
    }

    @Test
    fun `roundtrip preserves duration value`() {
        val original = 12_345.milliseconds
        assertEquals(original, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original)))
    }
}
