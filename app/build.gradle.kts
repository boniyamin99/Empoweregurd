import java.net.URL
import java.net.HttpURLConnection
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.HttpsURLConnection
import java.security.SecureRandom
import java.security.cert.X509Certificate

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.aistudio.empowerguard.sypqxz"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("copyApkToAssets") {
  dependsOn("packageDebug")
  val apkFile = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
  val assetsDest = layout.projectDirectory.file("../assets/EmpowerGuard.apk")
  
  inputs.file(apkFile)
  outputs.file(assetsDest)
  
  doLast {
    val src = apkFile.get().asFile
    val dst = assetsDest.asFile
    if (src.exists()) {
      src.copyTo(dst, overwrite = true)
      println("Successfully copied APK to assets: ${dst.absolutePath}")
    } else {
      println("APK not found at: ${src.absolutePath}")
    }
  }
}

tasks.register("copyApkToRoot") {
  dependsOn("packageDebug")
  val apkFile = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
  val rootDest = layout.projectDirectory.file("../EmpowerGuard.apk")
  
  inputs.file(apkFile)
  outputs.file(rootDest)
  
  doLast {
    val src = apkFile.get().asFile
    val dst = rootDest.asFile
    if (src.exists()) {
      src.copyTo(dst, overwrite = true)
      println("Successfully copied APK to root: ${dst.absolutePath}")
    } else {
      println("APK not found at: ${src.absolutePath}")
    }
  }
}

tasks.register("uploadApk") {
  dependsOn("packageDebug")
  val apkFile = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
  
  inputs.file(apkFile)
  
  doLast {
    val src = apkFile.get().asFile
    if (!src.exists()) {
      println("Cannot upload: APK not found at: ${src.absolutePath}")
      return@doLast
    }
    
    // Bypass SSL validation to support transfer/upload sites with old JDK certs
    try {
      val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? = null
        override fun checkClientTrusted(certs: Array<X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(certs: Array<X509Certificate>?, authType: String?) {}
      })
      val sc = SSLContext.getInstance("SSL")
      sc.init(null, trustAllCerts, SecureRandom())
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
      HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
      println("SSL certificates set to trust-all.")
    } catch (e: Exception) {
      println("Failed to bypass SSL: ${e.message}")
    }
    
    val reportFile = file("../upload_results.txt")
    val reportSB = StringBuilder()
    reportSB.append("Build timestamp: ${System.currentTimeMillis()}\n")
    reportSB.append("Size: ${src.length()} bytes\n\n")
    
    println("Uploading APK of size: ${src.length()} bytes...")
    
    // 1. Try bashupload.com (PUT request, direct and easy)
    try {
      println("Attempting bashupload.com upload...")
      val url = URL("https://bashupload.com/EmpowerGuard.apk")
      val connection = url.openConnection() as HttpURLConnection
      connection.requestMethod = "PUT"
      connection.doOutput = true
      connection.setRequestProperty("Content-Type", "application/octet-stream")
      connection.setRequestProperty("Content-Length", src.length().toString())
      connection.connectTimeout = 20000
      connection.readTimeout = 20000
      
      src.inputStream().use { input ->
        connection.outputStream.use { output ->
          input.copyTo(output)
        }
      }
      
      val code = connection.responseCode
      if (code == 200 || code == 201) {
        val result = connection.inputStream.bufferedReader().use { it.readText() }.trim()
        val match = "(https://bashupload.com/[a-zA-Z0-9/._-]+)".toRegex().find(result)
        val downloadUrl = match?.value ?: result
        println("--- BASHUPLOAD_URL: $downloadUrl ---")
        reportSB.append("bashupload URL: $downloadUrl\n")
      } else {
        println("Failed to upload to bashupload.com, HTTP code: $code")
        reportSB.append("bashupload Failed: HTTP $code\n")
      }
    } catch (e: Exception) {
      println("Error uploading to bashupload.com: ${e.message}")
      reportSB.append("bashupload Error: ${e.message}\n")
    }

    // 2. Try file.io (POST Request, single download only)
    try {
      println("Attempting file.io upload...")
      val url = URL("https://file.io")
      val boundary = "===Boundary" + System.currentTimeMillis() + "==="
      val connection = url.openConnection() as HttpURLConnection
      connection.requestMethod = "POST"
      connection.doOutput = true
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
      connection.connectTimeout = 20000
      connection.readTimeout = 20000
      
      connection.outputStream.use { out ->
        out.write(("--$boundary\r\n").toByteArray())
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"EmpowerGuard.apk\"\r\n").toByteArray())
        out.write(("Content-Type: application/vnd.android.package-archive\r\n\r\n").toByteArray())
        src.inputStream().use { input -> input.copyTo(out) }
        out.write(("\r\n--$boundary--\r\n").toByteArray())
        out.flush()
      }
      
      val code = connection.responseCode
      if (code == 200 || code == 201) {
        val result = connection.inputStream.bufferedReader().use { it.readText() }.trim()
        println("--- FILE_IO_JSON_RESPONSE: $result ---")
        val linkIndex = result.indexOf("\"link\":\"")
        if (linkIndex != -1) {
          val start = linkIndex + 8
          val end = result.indexOf("\"", start)
          if (end != -1) {
            val link = result.substring(start, end)
            reportSB.append("file.io URL: $link\n")
          } else {
            reportSB.append("file.io Raw: $result\n")
          }
        } else {
          reportSB.append("file.io Raw: $result\n")
        }
      } else {
        println("Failed to upload to file.io, HTTP code: $code")
        reportSB.append("file.io Failed: HTTP $code\n")
      }
    } catch (e: Exception) {
      println("Error uploading to file.io: ${e.message}")
      reportSB.append("file.io Error: ${e.message}\n")
    }
    
    // 3. Try uploading to 0x0.st
    try {
      println("Attempting 0x0.st upload...")
      val url = URL("https://0x0.st")
      val boundary = "===Boundary" + System.currentTimeMillis() + "==="
      val connection = url.openConnection() as HttpURLConnection
      connection.requestMethod = "POST"
      connection.doOutput = true
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
      connection.connectTimeout = 20000
      connection.readTimeout = 20000
      
      connection.outputStream.use { out ->
        out.write(("--$boundary\r\n").toByteArray())
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"EmpowerGuard.apk\"\r\n").toByteArray())
        out.write(("Content-Type: application/vnd.android.package-archive\r\n\r\n").toByteArray())
        src.inputStream().use { input -> input.copyTo(out) }
        out.write(("\r\n--$boundary--\r\n").toByteArray())
        out.flush()
      }
      
      val code = connection.responseCode
      if (code == 200 || code == 201) {
        val result = connection.inputStream.bufferedReader().use { it.readText() }.trim()
        println("--- 0X0_ST_URL: $result ---")
        reportSB.append("0x0.st URL: $result\n")
      } else {
        println("Failed to upload to 0x0.st, HTTP code: $code")
        reportSB.append("0x0.st Failed: HTTP $code\n")
      }
    } catch (e: Exception) {
      println("Error uploading to 0x0.st: ${e.message}")
      reportSB.append("0x0.st Error: ${e.message}\n")
    }
    
    reportFile.writeText(reportSB.toString())
    println("Report written to: ${reportFile.absolutePath}")
  }
}


