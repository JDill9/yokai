import org.gradle.api.JavaVersion as GradleJavaVersion

object AndroidConfig {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 23

    // Kept at 35 (like Mihon and Yokai stable) so Android 16+ keeps honoring
    // per-series orientation locks; targeting 36 makes the system ignore
    // orientation restrictions on large screens.
    const val TARGET_SDK = 35

    const val NDK = "27.2.12479018"
    val JavaVersion = GradleJavaVersion.VERSION_17
}
