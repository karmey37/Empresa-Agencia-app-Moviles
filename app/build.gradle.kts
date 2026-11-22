import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

// Datos de la firma: se leen de keystore.properties (en la raíz del proyecto)
val keystoreProps = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "cl.duoc.agenciaoportunidades"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.agenciaoportunidades"
        minSdk = 24
        targetSdk = 37
        versionCode = 2          // sube 1 en cada entrega
        versionName = "1.1.0"    // versión que ve el usuario

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // Solo firma si existe keystore.properties; si no, assembleRelease genera un APK sin firmar
            if (keystoreProps.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.coil.compose)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)
}

kover {
    reports {
        verify {
            rule("Cobertura mínima de la lógica") {
                minBound(80)
            }
        }
        filters {
            includes {
                classes(
                    "cl.duoc.agenciaoportunidades.util.ValidacionesKt*",
                    "cl.duoc.agenciaoportunidades.util.ReglasNegocioKt*",
                    "cl.duoc.agenciaoportunidades.util.AlertasKt*",
                    "cl.duoc.agenciaoportunidades.data.remote.OrganizacionesRemoteRepository*",
                    "cl.duoc.agenciaoportunidades.ui.directorio.DirectorioViewModel*",
                    "cl.duoc.agenciaoportunidades.ui.directorio.DirectorioValidador*",
                    "cl.duoc.agenciaoportunidades.data.externa.FeriadosRepository*",
                    "cl.duoc.agenciaoportunidades.ui.feriados.FeriadosViewModel*",
                    "cl.duoc.agenciaoportunidades.ui.feriados.FeriadosLogicaKt*"
                )
            }
        }
    }
}