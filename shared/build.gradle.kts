plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    android {
        namespace = "com.prudhvi.weather.shared"
        compileSdk = 37
        minSdk = 24
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Keep the shared domain layer platform-neutral. Platform clients and
            // hardware implementations are injected from Android and iOS.
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
