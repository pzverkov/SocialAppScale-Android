# R8 keep rules for release builds.
# Hilt, Room, Retrofit, OkHttp, and Coil ship their own consumer rules in their
# artifacts, so only the rules R8 cannot infer on its own are declared here.

# Keep line numbers for readable crash stack traces, but obfuscate the file name.
# Pair with the mapping.txt that R8 emits to deobfuscate.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Generic signatures and annotations are read reflectively by Retrofit (return
# types) and kotlinx.serialization (descriptors).
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# --- kotlinx.serialization ---
# The compiler plugin generates a $$serializer for every @Serializable type and
# wires it up via reflection. R8 sees no direct reference, so keep them explicitly
# for this app's model classes.
-keepclassmembers @kotlinx.serialization.Serializable class com.pzverkov.socialapp.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class com.pzverkov.socialapp.**
-keep,includedescriptorclasses class com.pzverkov.socialapp.**$$serializer { *; }

# --- Networking transitive deps ---
# OkHttp references optional security providers that are absent on Android.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
