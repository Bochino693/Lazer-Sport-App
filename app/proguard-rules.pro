# Regras de ofuscacao do Lazer & Sport.
#
# Estao prontas mas nao em uso: isMinifyEnabled esta false em
# build.gradle.kts ate alguem rodar um APK de release no aparelho.
# Ofuscacao quebra em runtime, nao no build.
#
# Retrofit, OkHttp, Hilt e Room ja trazem consumer rules proprias. O que
# esta aqui cobre o que e especifico deste app.

# ---------------------------------------------------------------- modelos
# Os DTOs sao lidos por reflexao pelo kotlinx.serialization. Sem isto, os
# nomes dos campos somem e toda resposta da API vira erro de parsing.
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.**

-keepclassmembers class br.com.lazersport.app.** {
    *** Companion;
}
-keepclasseswithmembers class br.com.lazersport.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class br.com.lazersport.app.data.**$$serializer { *; }
-keep class br.com.lazersport.app.data.** { *; }

# ---------------------------------------------------------------- retrofit
# Os tipos genericos das suspend functions sao lidos em runtime.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation interface br.com.lazersport.app.data.ApiService
-keep,allowobfuscation interface br.com.lazersport.app.data.ApiComercio
-keep,allowobfuscation interface br.com.lazersport.app.data.ApiStatus

# ---------------------------------------------------------------- compose
-dontwarn androidx.compose.**

# ---------------------------------------------------------------- ruido
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
