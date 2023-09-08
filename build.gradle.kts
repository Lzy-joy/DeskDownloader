import org.jetbrains.compose.ComposePlugin.DesktopDependencies.linux_x64
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.ella"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://repo1.maven.org/maven2/")
    maven("https://developer.huawei.com/repo/")
    maven("https://jitpack.io")
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/google")
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EllaDownloader"
            packageVersion = "1.0.0"
            macOS {
                dockName = "EllaDownloader"
                iconFile.set(project.file("src/main/resources/icon/ic_launcher.icns"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/icon/ic_launcher.png"))
            }
            windows {
                shortcut = true
                dirChooser = true
                iconFile.set(project.file("src/main/resources/icon/ic_launcher.ico"))
            }
        }
    }
}
