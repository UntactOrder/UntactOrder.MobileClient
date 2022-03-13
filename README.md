# [EN]UntactOrder: Non-Contact Order<br/>[KO]언택트오더: 비대면 주문
> UntactOrder Android Client(주문 고객용 안드로이드 클라이언트 앱) <TargetSDK 32, MinSDK 24>
> 
![AC로고](/app/src/androidClient/ic_launcher-playstore.png)
![AC로고1](/app/src/androidClient/res/mipmap-xxxhdpi/ic_launcher.png)

> UntactOrder Order Assistant(자영업자용 주문 관리 클라이언트 앱) <TargetSDK 32, MinSDK 24>
> 
![OA로고](/app/src/orderAssistant/ic_launcher-playstore.png)
![OA로고1](/app/src/orderAssistant/res/mipmap-xxxhdpi/ic_launcher.png)

### 개발 환경
* IntelliJ IDEA 2021.3 with Android Extention (or up)
* Gradle 7.4-rc-1 (or up)
* Android Gradle Plugin 7.0.4 (or up)
* Android 12L (Android API 32 Platform JetBrains Runtime) (or up)

### 사용 언어
* Java/Kotlin
* XML
* etc

### 필요 모듈
* etc... (build.gradle 참조)

# 빌드 방법
## 유의사항
- 프로젝트 경로에 한글 끼워넣지 말자 (계정명이 한글이면... 드라이브 최상위에 클론 해서 빌드 해보고 안되면 ㅠㅠ)
- 그래들이 . 붙어 있는 경로에서 빌드가 안되는거 같으니 레포 클론할 때 AndroidClientApps로 폴더명 바꾸는거 잊지 말자

### [빌드 전 사전 작업]
<pre>(1). 개발 환경 세팅 필요 (상단 개발 환경 설명된 부분 참고)</pre>
<pre>(2). git clone

git clone https://github.com/UntactOrder/UntactOrder.AndroidClientApps.git AndroidClientApps
</pre>
<pre>(3). git branch(분기) 변경 - main/beta/dev </pre>
<pre>(4). 프로젝트 루트에 local.properties 생성
아래와 같은 내용으로 파일 생성하고, __??__ 형태의 부분 수정하기
__________________________________________________________

## This file is automatically generated by Android Studio.
# Do not modify this file -- YOUR CHANGES WILL BE ERASED!
#
# This file should *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
#
# Location of the SDK. This is only used by Gradle.
# For customization when using a Version Control System, please read the
# header note.
sdk.dir=C\:\\Users\\__YOUR_WINDOWS_USER_NAME__\\AppData\\Local\\Android\\Sdk
#
# Apk Key-Signing Configurations
sign_key_config.debug.key_store.dir=__YOUR_SIGNKEY_KEYSTORE_DIR__
sign_key_config.debug.key_store.password=__YOUR_SIGNKEY_KEYSTORE_PW__
sign_key_config.debug.key.alias=__YOUR_SIGNKEY_KEY_ALIAS__
sign_key_config.debug.key.password=__YOUR_SIGNKEY_KEY_PW__
#
# SSO SDK Keys
kakao.sdk.native_app_key=__YOUR_KAKAO_SDK_NATIVE_APP_KEY__
naver.sdk.client_id=__YOUR_NAVER_SDK_CLIENT_ID__
naver.sdk.client_secret=__YOUR_NAVER_SDK_CLIENT_SECRET__

</pre>
<pre>(5). firebase 프로젝트 설정 페이지에서 google-services.json 파일 받아 app 모듈 루트에 집어넣기 </pre>


### 1. ??
* ???
<pre>????</pre>
