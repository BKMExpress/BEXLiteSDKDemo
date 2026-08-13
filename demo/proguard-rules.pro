# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ================================================================================================
# BEX LITE SDK - Consumer ProGuard Rules
# (Copied from `sdk/consumer-rules.pro` because `implementation(files(...aar))` doesn't wire
# consumer rules via Gradle module metadata.)
# ================================================================================================
# ================================================================================================
# BEX Payment SDK - Consumer ProGuard Rules
# ================================================================================================
# These rules are automatically applied to apps that integrate this SDK via Maven

# ================================================================================================
# SDK PUBLIC API - Must be kept for consumer app integration
# ================================================================================================
-keep public class com.bkm.mobil.sdk.lite.api.** { *; }
-keep public interface com.bkm.mobil.sdk.lite.api.** { *; }

# Keep all public API methods and constructors
-keepclassmembers class com.bkm.mobil.sdk.lite.api.** {
    public <init>(...);
    public <methods>;
    public <fields>;
}

# ================================================================================================
# CALLBACK INTERFACES - Used by consumer apps
# ================================================================================================
-keep interface com.bkm.mobil.sdk.lite.api.PaymentCallback {
    *;
}

# Keep callback methods from being obfuscated
-keepclassmembers interface com.bkm.mobil.sdk.lite.api.PaymentCallback {
    void onSuccess(com.bkm.mobil.sdk.lite.api.PaymentResult);
    void onError(com.bkm.mobil.sdk.lite.api.BexSdkError);
    void onCardSelected(com.bkm.mobil.sdk.lite.api.CardSelectionResult);
}

# ================================================================================================
# DATA CLASSES - Used in public API
# ================================================================================================
-keep class com.bkm.mobil.sdk.lite.api.PaymentResult {
    *;
}

-keep class com.bkm.mobil.sdk.lite.api.BexSdkError** {
    *;
}

-keep class com.bkm.mobil.sdk.lite.api.SdkInitParams {
    *;
}

-keep class com.bkm.mobil.sdk.lite.api.PaymentSdkConfig {
    *;
}

-keep class com.bkm.mobil.sdk.lite.api.PaymentSdkTheme** {
    *;
}

# ================================================================================================
# UI COMPONENTS - Secure card input views used directly by consumer apps
# ================================================================================================
# XML / View-based component
-keep public class com.bkm.mobil.sdk.lite.ui.BEXSecureCardNumberEditText { *; }

# Jetpack Compose component and its state holder (top-level Kotlin functions compile to *Kt classes)
-keep public class com.bkm.mobil.sdk.lite.ui.BEXSecureCardNumberFieldKt { *; }
-keep public class com.bkm.mobil.sdk.lite.ui.BEXSecureCardNumberState { *; }
-keep public class com.bkm.mobil.sdk.lite.ui.BEXSecureCardNumberStateKt { *; }

# Value class wrapper for the encrypted PAN
-keep public class com.bkm.mobil.sdk.lite.api.BexTokenizedPan { *; }

# ================================================================================================
# ENTRY POINT - PaymentSDK object
# ================================================================================================
-keep class com.bkm.mobil.sdk.lite.api.PaymentSDK {
    public *;
}

-keepclassmembers class com.bkm.mobil.sdk.lite.api.PaymentSDK {
    public static void init(...);
    public static com.bkm.mobil.sdk.lite.api.BexPaymentClient getPaymentClient();
}

# ================================================================================================
# SERIALIZATION - Keep serialization annotations and members
# ================================================================================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin Serialization
-keep @kotlinx.serialization.Serializable class com.bkm.mobil.sdk.lite.api.** { *; }

# ================================================================================================
# KOTLIN METADATA - Required for SDK functionality
# ================================================================================================
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations

# ================================================================================================
# REFLECTION - Keep classes that might be accessed via reflection
# ================================================================================================
# Keep data class copy methods and component functions
-keepclassmembers class com.bkm.mobil.sdk.lite.api.** {
    public ** copy(...);
    public ** component*();
}

# ================================================================================================
# NETWORKING - Required when app is minified (Retrofit, OkHttp, Gson)
# ================================================================================================
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class com.google.gson.** { *; }
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep SDK internal network and model classes (used by Retrofit/Gson at runtime)
-keep class com.bkm.mobil.sdk.lite.internal.network.** { *; }
-keep class com.bkm.mobil.sdk.lite.internal.model.** { *; }
-keep class com.bkm.mobil.sdk.lite.internal.repository.PaymentRepository { *; }
-keep class com.bkm.mobil.sdk.lite.internal.di.ServiceLocator { *; }

# Keep DataStore and AuthTokenProvider (SDK init and auth token storage)
-keep class com.bkm.mobil.sdk.lite.internal.datastore.** { *; }
-keep class androidx.datastore.** { *; }

# Keep BexPaymentClient and result types
-keep interface com.bkm.mobil.sdk.lite.api.BexPaymentClient { *; }
-keep class com.bkm.mobil.sdk.lite.api.model.** { *; }
-keep class com.bkm.mobil.sdk.lite.api.result.** { *; }

# ================================================================================================
# DEBUGGING - Keep source file and line numbers for crash reports
# ================================================================================================
-keepattributes SourceFile,LineNumberTable