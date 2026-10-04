package com.webtoapp.core.activation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DeviceIdGeneratorTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun prefs() = context.getSharedPreferences("device_id_prefs", Context.MODE_PRIVATE)

    private fun clearPrefs() {
        prefs().edit().clear().commit()
    }

    private fun legacyHash(androidId: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(androidId.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(32)
    }

    private fun legacyHmac(data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec("WTA_DeviceId_Integrity_2024".toByteArray(), "HmacSHA256"))
        return mac.doFinal(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `computeDeviceId differs by packageName for the same android id`() {
        val androidId = "same-android-id-1234"
        val idA = DeviceIdGenerator.computeDeviceId(androidId, "com.app.one")
        val idB = DeviceIdGenerator.computeDeviceId(androidId, "com.app.two")

        assertThat(idA).isNotEqualTo(idB)
        assertThat(idA).hasLength(32)
        assertThat(idB).hasLength(32)
    }

    @Test
    fun `computeDeviceId is stable for same inputs`() {
        val androidId = "stable-android-id-9999"
        val pkg = "com.stable.app"

        val first = DeviceIdGenerator.computeDeviceId(androidId, pkg)
        val second = DeviceIdGenerator.computeDeviceId(androidId, pkg)
        val third = DeviceIdGenerator.computeDeviceId(androidId, pkg)

        assertThat(first).isEqualTo(second)
        assertThat(second).isEqualTo(third)
    }

    @Test
    fun `computeDeviceId treats empty packageName as empty salt`() {
        val androidId = "android-id-empty-pkg"
        val withEmpty = DeviceIdGenerator.computeDeviceId(androidId, "")
        val withBlank = DeviceIdGenerator.computeDeviceId(androidId, "   ")

        assertThat(withEmpty).isEqualTo(withBlank)
    }

    @Test
    fun `legacy id is preserved across migration to new format`() {
        clearPrefs()
        val androidId = "legacy-android-id-7777"
        val legacyId = legacyHash(androidId)
        val legacyHmac = legacyHmac(legacyId)
        prefs()
            .edit()
            .putString("device_id", legacyId)
            .putString("device_id_hmac", legacyHmac)
            .apply()

        val result = DeviceIdGenerator.getDeviceId(context, "com.webtoapp")

        assertThat(result).isEqualTo(legacyId)
        assertThat(prefs().getString("device_id", null)).isEqualTo(legacyId)
        assertThat(prefs().getString("device_id_hmac", null)).isEqualTo(legacyHmac)
        assertThat(prefs().getString("device_id_package", null)).isEqualTo("<legacy-no-package>")
    }

    @Test
    fun `legacy marker is not overwritten on subsequent calls`() {
        clearPrefs()
        val legacyId = legacyHash("legacy-android-id-marker")
        val legacyHmac = legacyHmac(legacyId)
        prefs()
            .edit()
            .putString("device_id", legacyId)
            .putString("device_id_hmac", legacyHmac)
            .putString("device_id_package", "<legacy-no-package>")
            .apply()

        val result = DeviceIdGenerator.getDeviceId(context, "com.webtoapp")

        assertThat(result).isEqualTo(legacyId)
        assertThat(prefs().getString("device_id_hmac", null)).isEqualTo(legacyHmac)
    }

    @Test
    fun `migrated legacy id differs from newly generated id for same package`() {
        val androidId = "android-id-compare-123"
        val legacyId = legacyHash(androidId)
        val newId = DeviceIdGenerator.computeDeviceId(androidId, "com.webtoapp")

        assertThat(legacyId).isNotEqualTo(newId)
    }
}
