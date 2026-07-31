import org.gradle.api.JavaVersion as GradleJavaVersion

object AndroidConfig {
    const val COMPILE_SDK = 36
    // Raised from 23: the July 2026 keiyoushi extension builds rely on JVM default
    // interface methods (kotlinx.serialization 1.8+). With minSdk < 24, D8 desugars
    // those methods away and every extension crashes with AbstractMethodError
    // (typeParametersSerializers). Mihon uses minSdk 26 for the same reason.
    const val MIN_SDK = 26

    // Kept at 35 (like Mihon and Yokai stable) so Android 16+ keeps honoring
    // per-series orientation locks; targeting 36 makes the system ignore
    // orientation restrictions on large screens.
    const val TARGET_SDK = 35

    const val NDK = "27.2.12479018"
    val JavaVersion = GradleJavaVersion.VERSION_17
}
