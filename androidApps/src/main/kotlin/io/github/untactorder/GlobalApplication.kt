package io.github.untactorder

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import io.github.untactorder.manual.ManualDisplayActivity
import io.github.untactorder.auth.UsimUtil
import java.io.File
import kotlin.system.exitProcess


/**
 * GlobalApplication
 *
 * @author irack000
 */
class GlobalApplication : Application() {
    companion object {
        var USIM_ENABLED: Boolean = false
        var PHONE_NUMBER: String = ""
    }
    private val USIM_PREF_CONTAINER = "usim_info"
    private val USIM_STATUS_KEY = "usim_status"
    private val PHONE_NUMBER_KEY = "phone_number"

    private val TAG = javaClass.simpleName

    override fun onCreate() {
        super.onCreate()

        // if release mode, check if the apk's key is generated by PlayStore.
        if (!isPackageSignValid()) {
            val shutdownIntent = Intent(this, ManualDisplayActivity::class.java)
            startActivity(shutdownIntent)  // => just make FATAL EXCEPTION
        }

        // if phone number is changed, destroy all data.
        checkUsimInfo()

        // init Kakao SDK
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // init Naver ID Login SDK
        //NaverIdLoginSDK.initialize(this, BuildConfig.NAVER_OAUTH_CLIENT_ID,
        //    BuildConfig.NAVER_OAUTH_CLIENT_SECRET, resources.getString(R.string.app_name))
    }

    /**
     * @see "https://stackoverflow.com/questions/66896154/getinstallerpackagenamestring-string-is-deprecated-deprecated-in-java"
     */
    private fun getInstallerPackageName(): String? {
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return this.packageManager.getInstallSourceInfo(this.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                return this.packageManager.getInstallerPackageName(this.packageName)
            }
        }
        return null
    }

    /**
     * check if the apk's key is generated by PlayStore.
     * @see "https://philosopher-chan.tistory.com/372"
     */
    private fun isPackageSignValid(): Boolean {
        if (!BuildConfig.DEBUG) {
            val packageName = getInstallerPackageName()
            // 플레이 스토어에서 설치했을 경우 "com.android.vending" 으로 시작
            if (packageName == null || !packageName.startsWith("com.android.vending")) {
                printLog(TAG, getString(R.string.error_not_downloaded_from_playstore), true, Toast.LENGTH_LONG)
                return false
            }
        }
        return true
    }

    fun checkUsimInfo() {
        try {
            val usimUtil = UsimUtil(this)  // exception will be thrown if permission is not granted.

            var usimStatus = false
            var phoneNumber = ""
            if (usimUtil.isUsimEnabled()) {
                usimStatus = true
                val phoneNumbers = usimUtil.getFormattedPhoneNumberList()
                printLog(TAG, "${phoneNumbers.size} phone numbers are found")
                if (phoneNumbers.isNotEmpty()) {
                    phoneNumber = phoneNumbers[0]  // 듀얼심 사용자의 경우 첫번째 전화번호만 사용 (테스트 필요)
                } else {
                    phoneNumber = usimUtil.getFormattedLine1Number()
                }
            }
            val usimPref = getEncryptedSharedPreferences(USIM_PREF_CONTAINER)
            if (!usimPref.contains(USIM_STATUS_KEY) || !usimPref.contains(PHONE_NUMBER_KEY)) {
                printLog(TAG, "USIM info is not found. Initializing...")
                val usimPrefEditor = usimPref.edit()
                USIM_ENABLED = usimStatus
                PHONE_NUMBER = phoneNumber
                usimPrefEditor.putBoolean(USIM_STATUS_KEY, usimStatus)
                usimPrefEditor.putString(PHONE_NUMBER_KEY, phoneNumber)
                usimPrefEditor.apply()  // async apply
            } else {
                USIM_ENABLED = usimPref.getBoolean(USIM_STATUS_KEY, false)
                PHONE_NUMBER = usimPref.getString(PHONE_NUMBER_KEY, "").toString()
                if (USIM_ENABLED != usimStatus || PHONE_NUMBER != phoneNumber) {
                    printLog(TAG, "USIM_ENABLED: $USIM_ENABLED, PHONE_NUMBER: $PHONE_NUMBER")
                    printLog(TAG, "usimStatus: $usimStatus, phoneNumber: $phoneNumber")
                    printLog(TAG, getString(R.string.error_usim_info_changed), true, Toast.LENGTH_LONG)
                    clearApplicationDataNQuit()
                }
            }
            printLog(TAG, "USIM_ENABLED: $USIM_ENABLED, PHONE_NUMBER: $PHONE_NUMBER")
        } catch (e: RuntimeException) {
            printLog(TAG, "RuntimeException: ${e.message}")
        }
    }
}

/**
 * generate encrypted shared preferences
 * @return encrypted preferences
 */
fun Context.getEncryptedSharedPreferences(containerName: String): SharedPreferences {
    val masterKey = MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        this,
        containerName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

/**
 * dp to pixel
 * @return pixel value in integer
 */
fun AppCompatActivity.dpToPixel(dp: Int): Int =
    (dp * resources.displayMetrics.density).toInt()

/**
 * do an operation after the target view is drawn on the screen by setting global layout listener
 */
fun setOnGlobalLayoutListener(view: View, operation: () -> Unit) {
    view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            operation()
            removeOnGlobalLayoutListener(view.viewTreeObserver, this)
        }
    })
}

/**
 * remove the global layout listener
 */
fun removeOnGlobalLayoutListener(observer: ViewTreeObserver?, listener: ViewTreeObserver.OnGlobalLayoutListener) {
    observer?.removeOnGlobalLayoutListener(listener)
}

/**
 * clear application data
 * @see "https://www.examplefiles.net/cs/59563"
 */
fun Context.clearApplicationCache() {
    val cache: String? = cacheDir.parent
    if (cache != null) {
        val applicationDirectory = File(cache)
        if (applicationDirectory.exists()) {
            val fileNames = applicationDirectory.list()
            if (fileNames != null) {
                for (fileName in fileNames) {
                    if (fileName != "lib") {
                        deleteFile(File(applicationDirectory, fileName))
                    }
                }
            }
        }
    }

}

fun deleteFile(file: File?): Boolean {
    var deletedAll = true
    if (file != null) {
        if (file.isDirectory) {
            val children = file.list()
            if (children != null) {
                for (child in children) {
                    deletedAll = deleteFile(File(file, child)) && deletedAll
                }
            }
        } else {
            deletedAll = file.delete()
        }
    }
    return deletedAll
}

fun Context.clearApplicationDataNQuit(usePackageManager: Boolean = true) {
    try {
        if (!usePackageManager) throw Exception("usePackageManager is false")
        Runtime.getRuntime().exec("pm clear $packageName")
    } catch (e: Exception) {
        e.printStackTrace()
        (getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
    }
}

/**
 * restartApplication
 * @see "https://stackoverflow.com/questions/6609414/how-do-i-programmatically-restart-an-android-app"
 */
fun Activity.restartApplication() {
    val restartIntent = packageManager.getLaunchIntentForPackage(packageName)
    finishAffinity()                  // Finishes all activities.
    startActivity(restartIntent)      // Start the launch activity
    exitProcess(1)            // System finishes and automatically relaunches us.
}

/**
 * restartActivity
 */
fun Activity.restartActivity() {
    val restartIntent = Intent(this, this::class.java)
    startActivity(restartIntent)
    finishAffinity()
}

/**
 * can print log on console, logcat, and toast
 */
fun Any.printLog(tag: String, message: String, showToast: Boolean = false, toastLength: Int = Toast.LENGTH_SHORT) {
    if (BuildConfig.DEBUG) {
        if (this is Context) Log.d(tag, message) else println("$tag: $message")
    }
    if (this is Context && showToast) {  // smart cast
        Toast.makeText(this, message, toastLength).show()
    }
}
