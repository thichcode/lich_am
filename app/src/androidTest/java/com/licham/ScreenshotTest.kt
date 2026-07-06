package com.licham

import android.Manifest
import android.os.Environment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import androidx.test.uiautomator.UiDevice

@RunWith(AndroidJUnit4::class)
class ScreenshotTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Test
    fun takeScreenshots() {
        val device = UiDevice.getInstance(getInstrumentation())
        val screenshotDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "screenshots"
        )
        screenshotDir.mkdirs()

        // Screenshot 1: Home screen
        takeScreenshot(device, screenshotDir, "home_screen")
        
        // Open calendar tab
        onView(withText("Lịch tháng")).perform(androidx.test.espresso.action.ViewActions.click())
        takeScreenshot(device, screenshotDir, "calendar_screen")
        
        // Open good days tab
        onView(withText("Ngày tốt")).perform(androidx.test.espresso.action.ViewActions.click())
        takeScreenshot(device, screenshotDir, "good_days_screen")
        
        // Open prayer tab
        onView(withText("Văn khấn")).perform(androidx.test.espresso.action.ViewActions.click())
        takeScreenshot(device, screenshotDir, "prayers_screen")
    }

    private fun takeScreenshot(device: UiDevice, directory: File, name: String) {
        val filename = "${name}_${System.currentTimeMillis()}.png"
        device.takeScreenshot(File(directory, filename))
    }
}
