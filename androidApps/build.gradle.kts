plugins {
    id("com.android.application")
    kotlin("android")

	id("com.google.gms.google-services")  // Firebase SDK
}

// 민감한 API 키 등을 숨기기 위해 local.properties 사용
// 참고 : https://blog.mindorks.com/using-local-properties-file-to-avoid-api-keys-check-in-into-version-control-system
// 참고 : https://medium.com/affirmativedev/%EC%A4%91%EC%9A%94%ED%95%9C-%EA%B0%92%EB%93%A4%EC%9D%84-%EC%88%A8%EA%B2%A8%EB%B4%85%EC%8B%9C%EB%8B%A4-e5be00d2e921
Properties localProperties = new Properties()
localProperties.load(new FileInputStream(rootProject.file("local.properties")))
HashMap<String, String> debugKeyInfo = new HashMap<>()
debugKeyInfo.put("keyStoreDir", localProperties.getProperty('sign_key_config.debug.key_store.dir'))
debugKeyInfo.put("keyStorePassword", localProperties.getProperty('sign_key_config.debug.key_store.password'))
debugKeyInfo.put("keyAlias", localProperties.getProperty('sign_key_config.debug.key.alias'))
debugKeyInfo.put("keyPassword", localProperties.getProperty('sign_key_config.debug.key.password'))
def KAKAO_NATIVE_APP_KEY = localProperties.getProperty('kakao.sdk.native_app_key')
def NAVER_CLIENT_ID = localProperties.getProperty('naver.sdk.client_id')
def NAVER_CLIENT_SECRET = localProperties.getProperty('naver.sdk.client_secret')

android {
    compileSdk = 32
    buildTools = "32.0.0"

    testOptions {
        unitTests.returnDefaultValues = true
    }

    signingConfigs {
        debug {
            storeFile file(debugKeyInfo.get("keyStoreDir"))
            storePassword debugKeyInfo.get("keyStorePassword")
            keyAlias debugKeyInfo.get("keyAlias")
            keyPassword debugKeyInfo.get("keyPassword")
        }
    }

    defaultConfig {
        applicationId = "io.github.untactorder"
        minSdk = 24
        targetSdk = 32

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // KAKAO NATIVE APP KEY & NAVER CLIENT ID를 local.properties로부터 가져오기
        // 속성별 설명 참고 : https://yongyi1587.tistory.com/42
        //resValue("string", "KAKAO_NATIVE_APP_KEY", KAKAO_NATIVE_APP_KEY)
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"" + KAKAO_NATIVE_APP_KEY + "\"")
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = "kakao"+KAKAO_NATIVE_APP_KEY
        buildConfigField("String", "NAVER_OAUTH_CLIENT_ID", "\"" + NAVER_CLIENT_ID + "\"")
        buildConfigField("String", "NAVER_OAUTH_CLIENT_SECRET", "\"" + NAVER_CLIENT_SECRET + "\"")

        // ETC
        buildConfigField("String", "NETCONFIG_PUBLIC_IP_API", "\"" + "https://api.ipify.org" + "\"")  // 외부 IP 정보 API
    }

    // Build Variants 관련 설명
    // 참고 : https://parkho79.tistory.com/107

    // Specifies one flavor dimension.
    // 참고 : https://developer.android.com/studio/build/build-variants#build-types
    flavorDimensions "version"
    // Assigns this product flavor to the "version" flavor dimension.
    // If you are using only one dimension, this property is optional,
    // and the plugin automatically assigns all the module's flavors to
    // that dimension.
    productFlavors {
        androidClient {
            dimension "version"
            applicationIdSuffix ".androidclient"
            versionCode 7
            versionName "1.0.0.7"
            signingConfig signingConfigs.debug
        }
        orderAssistant {
            dimension "version"
            applicationIdSuffix ".orderassistant"
            versionCode 2
            versionName "1.0.0.2"
            signingConfig signingConfigs.debug
        }
    }

    sourceSets {
        androidClient {
            // AndroidClient - UntactOrder
            manifest.srcFile "src/androidClient/AndroidManifest.xml"
            java.srcDirs += ["src/androidClient/java", "src/androidClient/kotlin", "src/main/kotlin"]
            res.srcDirs += "src/androidClient/res"
        }
        orderAssistant {
            // OrderAssistant - UntactOrder
            manifest.srcFile "src/orderAssistant/AndroidManifest.xml"
            java.srcDirs += ["src/orderAssistant/kotlin", "src/main/kotlin"]
            res.srcDirs += "src/orderAssistant/res"
        }
    }

    buildTypes {
        getByName("debug") {
            debuggable = true  // 디버그 모드에서는 플레이 콘솔에 업로드 불가능

            // proguard settings
            isMinifyEnabled = true
            proguardFile getDefaultProguardFile("proguard-android-optimize.txt")  // basic settings
            proguardFile "proguard-rules.pro"
            proguardFile "proguard-debug.pro"  // debug-specific settings

            // 여기에 디버그 서버 주소를 입력하세요
            //buildConfigField("String", "SERVER", "\"" + SOMETHING + "\"")
        }

        getByName("release") {
            // proguard settings
            isMinifyEnabled = true
            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"

            // 여기에 릴리즈 서버 주소를 입력하세요
            //buildConfigField("String", "SERVER", "\"" + SOMETHING + "\"")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    // Configure only for each module that uses Java 8
    // language features (either in its source code or
    // through dependencies).
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    // For Kotlin projects
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":common"))

    // Android 기본 의존성 모듈
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha03")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("androidx.core:core-splashscreen:1.0.0-beta01")  // Android 12 SplashScreen compat library

    // Kakao SDK 사용을 위한 의존성 모듈 | API 21: Android 5.0(Lollipop) 이상이어야 함
    implementatio("com.kakao.sdk:v2-user:2.8.4") // 카카오 로그인
    //androidClientImplementation("com.kakao.sdk:v2-talk:2.8.4") // 친구, 메시지(카카오톡)
    //androidClientImplementation("com.kakao.sdk:v2-link:2.8.4") // 메시지(카카오링크)
    //androidClientImplementation("com.kakao.sdk:v2-navi:2.8.4") // 카카오내비
    //androidClientImplementation("com.kakao.sdk:v2-story:2.8.4") // 카카오스토리

    // Naver SDK 사용을 위한 의존성 모듈 | API 21: Android 5.0(Lollipop) 이상이어야 함
    implementation("com.navercorp.nid:oauth:5.0.1")

    // Import the Firebase BoM
    implementation(platform('com.google.firebase:firebase-bom:29.0.3'))
    // Add the dependency for the Firebase SDK for Google Analytics
    // When using the BoM, don't specify versions in Firebase dependencies
    androidClientImplementation("com.google.firebase:firebase-analytics-ktx")
    // Declare the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    // SMS Retriever API
    // Reference : https://developers.google.com/identity/sms-retriever/overview
    // Reference : https://machine-woong.tistory.com/452
    implementation("com.google.android.gms:play-services-auth:20.1.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.1")

    // 기타 모듈
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.44")  // 유심 관련 라이브러리
    androidClientImplementation("com.journeyapps:zxing-android-embedded:4.3.0")  // qr 관련 라이브러리
    androidClientImplementation("com.google.zxing:core:3.4.1")  // qr 관련 라이브러리
    implementation("com.google.code.gson:gson:2.9.0")  // json 관련 라이브러리
}