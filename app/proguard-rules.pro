# Adicione regras específicas do projeto abaixo

# Preservar membros da classe para interfaces JavaScript no WebView, se usado
# Descomente e adicione o nome totalmente qualificado da classe da interface JavaScript
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preservar as informações de linha para depuração de pilhas de chamadas
-keepattributes SourceFile,LineNumberTable

# Se você mantiver as informações de linha, pode renomear o atributo de arquivo-fonte para ocultar o nome original
#-renamesourcefileattribute SourceFile

# Regras do Firebase
# Preservar classes do Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Evitar warnings para classes do Firebase
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Regras do Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }

# Regras do Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }

# Regras do Firebase Remote Config
-keep class com.google.firebase.remoteconfig.** { *; }

# Regras do Firebase Performance Monitoring (se usado)
-keep class com.google.firebase.perf.** { *; }

# Regras do Firestore (se você usar)
-keep class com.google.firebase.firestore.** { *; }

# Regras para o Lifecycle do Android
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Regras para ViewBinding
-keep class androidx.viewbinding.** { *; }

# Otimizações do ProGuard
# Preserve algumas classes e membros críticos que são usados com Reflection ou através de outras técnicas de manipulação de código
-keepclassmembers class * {
    public <init>(...);
}

# Regras específicas para WebView
# Preservar qualquer classe que possa ser usada no WebView
#-keep class com.example.yourapp.MyJavaScriptInterface { *; }