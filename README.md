# Closed Test Tracker

Closed Test Tracker, Google Play kapali testlerini duzenli takip etmek icin gelistirilmis Kotlin + Jetpack Compose Android uygulamasidir.

## Ozellikler

- Telefonda yuklu kullanici uygulamalarini listeleme
- Arama ve A-Z / yeni / eski siralama ile uygulama secme
- Eklenen uygulamalar icin 14 gunluk test serisi takibi
- Baslangic gununu elle ayarlama
- Android Usage Access izniyle gunluk ve toplam kullanim dakikalarini gosterme
- Uygulama ikonuna dokunarak ilgili uygulamayi acma
- Tamamlanan 14 gunluk serileri belirtme
- Kullanim eksikse hatirlatici bildirimleri
- Turkce, Ingilizce, Fransizca, Ispanyolca, Cince, Hintce ve Rusca dil secenekleri
- Sistem, acik ve koyu tema secenekleri
- Buy Me a Coffee bagis baglantisi
- Play Store yayinci adini internet varsa Play Store sayfasindan okumayi deneme

## Teknolojiler

- Kotlin
- Jetpack Compose
- Material 3
- Room
- WorkManager
- UsageStatsManager

## Kurulum

1. Repoyu klonla.
2. Android Studio ile klasoru ac.
3. Gradle sync tamamlandiktan sonra uygulamayi cihaza kur.

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Windows:

```powershell
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Gerekli Izinler

Uygulama kullanim dakikalarini gostermek icin Android ayarlarindan Usage Access izni verilmelidir.

Bildirim hatirlaticilari icin Android 13 ve ustunde bildirim izni gerekir.

## Notlar

Android, web/PWA kisayollarinin site bazli kullanim suresini uygulamalara vermez. Bu nedenle bazi web tabanli kisayollarin suresi tarayici paketine yazilabilir veya 0 dakika gorunebilir.

Android, cihaz icinden Play Store yayinci adini dogrudan saglamaz. Uygulama internet varsa Play Store sayfasindan yayinci adini okumayi dener; sayfa erisilemiyorsa alan bos kalabilir.

## Destek

Destek e-postasi: mdstudiohelp@gmail.com

Bagis: https://www.buymeacoffee.com/mdx0
