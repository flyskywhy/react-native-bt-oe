# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\test\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers class com.oe.lucky.lucky.views.FrameLayout { *; }

-dontwarn com.oe.**

#  MESH & MESH-RELATED.
-dontwarn org.spongycastle.**
-keep class org.spongycastle.** { *; }
-dontwarn com.csr.**
-keep class com.csr.** { *; }
