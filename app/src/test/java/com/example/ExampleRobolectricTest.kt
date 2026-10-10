package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CompositionGridType
import com.example.model.FilterType
import com.example.data.FilterPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Frame – Smart Camera", appName)
  }

  @Test
  fun `verify composition grids available`() {
    val grids = CompositionGridType.values()
    assertEquals(13, grids.size)
  }

  @Test
  fun `verify filter color matrices generation`() {
    val matrix = FilterPresets.getColorMatrix(FilterType.CINEMATIC, 0.8f)
    assertNotNull(matrix)
  }
}
