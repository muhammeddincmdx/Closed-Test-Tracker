package com.mdstudio.closedtesttracker

private data class CopySet(
    val tr: String,
    val fr: String,
    val es: String,
    val zh: String,
    val hi: String,
    val ru: String,
    val ar: String,
    val de: String,
    val ja: String,
    val pt: String,
    val id: String
) {
    fun get(language: AppLanguage): String = when (language) {
        AppLanguage.TR -> tr
        AppLanguage.EN -> error("English copy is supplied by the caller")
        AppLanguage.FR -> fr
        AppLanguage.ES -> es
        AppLanguage.ZH -> zh
        AppLanguage.HI -> hi
        AppLanguage.RU -> ru
        AppLanguage.AR -> ar
        AppLanguage.DE -> de
        AppLanguage.JA -> ja
        AppLanguage.PT -> pt
        AppLanguage.ID -> id
    }
}

private fun c(
    tr: String, fr: String, es: String, zh: String, hi: String, ru: String,
    ar: String, de: String, ja: String, pt: String, id: String
) = CopySet(tr, fr, es, zh, hi, ru, ar, de, ja, pt, id)

/** Copy added after the original eight-language UI was built. */
private val fullCopy = mapOf(
    "Screen must be active for this action." to c(
        "Bu işlem için ekran açık olmalıdır.", "L'écran doit être actif pour cette action.", "La pantalla debe estar activa para esta acción.",
        "执行此操作时屏幕必须保持开启。", "इस क्रिया के लिए स्क्रीन सक्रिय होनी चाहिए।", "Для этого действия экран должен быть активен.",
        "يجب أن تكون الشاشة نشطة لتنفيذ هذا الإجراء.", "Für diese Aktion muss der Bildschirm aktiv sein.", "この操作には画面を表示しておく必要があります。",
        "A tela deve estar ativa para esta ação.", "Layar harus aktif untuk tindakan ini."
    ),
    "Share new test" to c(
        "Yeni testi paylaş", "Partager un nouveau test", "Compartir una nueva prueba", "分享新测试", "नया टेस्ट साझा करें", "Поделиться новым тестом",
        "مشاركة اختبار جديد", "Neuen Test teilen", "新しいテストを共有", "Compartilhar novo teste", "Bagikan tes baru"
    ),
    "Share a new test app" to c("Yeni test uygulaması paylaş", "Partager une nouvelle application de test", "Compartir una nueva app de prueba", "分享新的测试应用", "नया टेस्ट ऐप साझा करें", "Поделиться новым тестовым приложением", "مشاركة تطبيق اختبار جديد", "Neue Test-App teilen", "新しいテストアプリを共有", "Compartilhar novo app de teste", "Bagikan aplikasi uji baru"),
    "Tracking" to c("Takip", "Suivi", "Seguimiento", "跟踪", "ट्रैकिंग", "Отслеживание", "التتبع", "Tracking", "追跡", "Acompanhamento", "Pelacakan"),
    "Loading apps" to c("Uygulamalar yükleniyor", "Chargement des applications", "Cargando aplicaciones", "正在加载应用", "ऐप लोड हो रहे हैं", "Загрузка приложений", "جارٍ تحميل التطبيقات", "Apps werden geladen", "アプリを読み込み中", "Carregando apps", "Memuat aplikasi"),
    "Your test workspace is ready" to c(
        "Test alanın hazır", "Votre espace de test est prêt", "Tu espacio de pruebas está listo", "测试空间已准备就绪", "आपका टेस्ट कार्यक्षेत्र तैयार है", "Пространство для тестов готово",
        "مساحة الاختبار جاهزة", "Dein Testbereich ist bereit", "テスト環境の準備ができました", "Seu espaço de testes está pronto", "Ruang pengujian siap"
    ),
    "Start your first 14-day test" to c(
        "İlk 14 günlük testini başlat", "Commencez votre premier test de 14 jours", "Inicia tu primera prueba de 14 días", "开始首次 14 天测试", "अपना पहला 14-दिवसीय टेस्ट शुरू करें", "Начните первый 14-дневный тест",
        "ابدأ أول اختبار لمدة 14 يومًا", "Starte deinen ersten 14-Tage-Test", "最初の14日間テストを開始", "Inicie seu primeiro teste de 14 dias", "Mulai tes 14 hari pertama"
    ),
    "Choose test apps from your phone, track daily usage, and never miss a testing day." to c(
        "Telefonundan test uygulamalarını seç, günlük kullanımı takip et ve hiçbir test gününü kaçırma.", "Choisissez les applications à tester, suivez leur utilisation quotidienne et ne manquez aucun jour.", "Elige las apps de prueba, controla el uso diario y no pierdas ningún día.", "从手机中选择测试应用，跟踪每日使用情况，不错过任何测试日。", "फोन से टेस्ट ऐप चुनें, दैनिक उपयोग ट्रैक करें और कोई टेस्ट दिन न चूकें।", "Выберите приложения, отслеживайте ежедневное использование и не пропускайте дни теста.",
        "اختر تطبيقات الاختبار من هاتفك وتابع الاستخدام اليومي دون تفويت أي يوم.", "Wähle Test-Apps, verfolge die tägliche Nutzung und verpasse keinen Testtag.", "端末からテストアプリを選び、毎日の利用を記録してテスト日を逃さないようにします。", "Escolha os apps de teste, acompanhe o uso diário e não perca nenhum dia.", "Pilih aplikasi uji, lacak penggunaan harian, dan jangan lewatkan hari pengujian."
    ),
    "Choose test apps" to c("Test uygulamalarını seç", "Choisir les applications", "Elegir apps de prueba", "选择测试应用", "टेस्ट ऐप चुनें", "Выбрать приложения", "اختر تطبيقات الاختبار", "Test-Apps auswählen", "テストアプリを選択", "Escolher apps de teste", "Pilih aplikasi uji"),
    "Add apps directly from your launcher list." to c("Uygulamaları doğrudan telefonundaki listeden ekle.", "Ajoutez les applications depuis la liste du téléphone.", "Añade apps directamente desde la lista del teléfono.", "直接从手机应用列表添加。", "फोन की ऐप सूची से सीधे ऐप जोड़ें।", "Добавляйте приложения прямо из списка на телефоне.", "أضف التطبيقات مباشرة من قائمة هاتفك.", "Füge Apps direkt aus der Liste deines Geräts hinzu.", "端末のアプリ一覧から直接追加します。", "Adicione apps diretamente da lista do telefone.", "Tambahkan aplikasi langsung dari daftar ponsel."),
    "Enable Usage Access" to c("Kullanım Erişimini aç", "Activer l'accès à l'utilisation", "Activar acceso de uso", "启用使用情况访问权限", "उपयोग एक्सेस चालू करें", "Разрешить доступ к использованию", "فعّل الوصول إلى الاستخدام", "Nutzungszugriff aktivieren", "使用状況へのアクセスを許可", "Ativar acesso ao uso", "Aktifkan akses penggunaan"),
    "Automatically read today and total usage minutes." to c("Bugünkü ve toplam kullanım dakikalarını otomatik oku.", "Lisez automatiquement les minutes du jour et le total.", "Lee automáticamente los minutos de hoy y el total.", "自动读取今日和总使用分钟数。", "आज और कुल उपयोग मिनट अपने आप पढ़ें।", "Автоматически считывайте минуты за сегодня и всего.", "اقرأ دقائق اليوم والإجمالي تلقائيًا.", "Lies heutige und gesamte Nutzungsminuten automatisch.", "今日と合計の利用時間を自動取得します。", "Leia automaticamente os minutos de hoje e o total.", "Baca menit hari ini dan total secara otomatis."),
    "Track the 14-day streak" to c("14 günlük seriyi takip et", "Suivre la série de 14 jours", "Seguir la racha de 14 días", "跟踪 14 天连续测试", "14-दिवसीय स्ट्रीक ट्रैक करें", "Отслеживать серию из 14 дней", "تابع سلسلة 14 يومًا", "14-Tage-Serie verfolgen", "14日間の継続を追跡", "Acompanhar a sequência de 14 dias", "Lacak rangkaian 14 hari"),
    "Stay consistent with the widget and daily reminders." to c("Widget ve günlük hatırlatmalarla düzenli kal.", "Restez régulier grâce au widget et aux rappels.", "Mantén la constancia con el widget y los recordatorios.", "通过小组件和每日提醒保持连续测试。", "विजेट और दैनिक रिमाइंडर के साथ नियमित रहें।", "Сохраняйте регулярность с виджетом и напоминаниями.", "حافظ على الاستمرارية باستخدام الأداة والتذكيرات اليومية.", "Bleib mit Widget und täglichen Erinnerungen konsequent.", "ウィジェットと毎日の通知で継続できます。", "Mantenha a constância com o widget e lembretes.", "Tetap konsisten dengan widget dan pengingat harian."),
    "Usage Access is ready" to c("Kullanım Erişimi hazır", "L'accès à l'utilisation est prêt", "El acceso de uso está listo", "使用情况访问权限已就绪", "उपयोग एक्सेस तैयार है", "Доступ к использованию настроен", "الوصول إلى الاستخدام جاهز", "Nutzungszugriff ist bereit", "使用状況へのアクセスは設定済みです", "O acesso ao uso está pronto", "Akses penggunaan siap"),
    "Usage Access is not enabled yet" to c("Kullanım Erişimi henüz açık değil", "L'accès à l'utilisation n'est pas encore activé", "El acceso de uso aún no está activado", "尚未启用使用情况访问权限", "उपयोग एक्सेस अभी चालू नहीं है", "Доступ к использованию ещё не разрешён", "لم يتم تفعيل الوصول إلى الاستخدام بعد", "Nutzungszugriff ist noch nicht aktiviert", "使用状況へのアクセスがまだ許可されていません", "O acesso ao uso ainda não está ativado", "Akses penggunaan belum diaktifkan"),
    "Add test apps" to c("Test uygulaması ekle", "Ajouter des applications", "Añadir apps de prueba", "添加测试应用", "टेस्ट ऐप जोड़ें", "Добавить приложения", "إضافة تطبيقات اختبار", "Test-Apps hinzufügen", "テストアプリを追加", "Adicionar apps de teste", "Tambah aplikasi uji"),
    "Set up Usage Access" to c("Kullanım Erişimini ayarla", "Configurer l'accès à l'utilisation", "Configurar acceso de uso", "设置使用情况访问权限", "उपयोग एक्सेस सेट करें", "Настроить доступ к использованию", "إعداد الوصول إلى الاستخدام", "Nutzungszugriff einrichten", "使用状況へのアクセスを設定", "Configurar acesso ao uso", "Atur akses penggunaan"),
    "Get Pro" to c("Pro'yu al", "Obtenir Pro", "Obtener Pro", "获取 Pro", "Pro प्राप्त करें", "Получить Pro", "احصل على Pro", "Pro holen", "Proを購入", "Obter Pro", "Dapatkan Pro"),
    "Pro is active. Ads are removed and all Pro tools are unlocked." to c("Pro etkin. Reklamlar kaldırıldı ve tüm Pro araçları açık.", "Pro est actif. Les publicités sont supprimées et tous les outils sont déverrouillés.", "Pro está activo. Se eliminaron los anuncios y se desbloquearon todas las herramientas.", "Pro 已启用。广告已移除，所有 Pro 工具均已解锁。", "Pro सक्रिय है। विज्ञापन हट गए हैं और सभी Pro टूल अनलॉक हैं।", "Pro активен. Реклама удалена, все инструменты разблокированы.", "Pro نشط. تمت إزالة الإعلانات وفتح جميع الأدوات.", "Pro ist aktiv. Werbung ist entfernt und alle Pro-Werkzeuge sind freigeschaltet.", "Proが有効です。広告が削除され、すべてのPro機能が利用できます。", "O Pro está ativo. Os anúncios foram removidos e todas as ferramentas estão liberadas.", "Pro aktif. Iklan dihapus dan semua alat Pro terbuka."),
    "One-time purchase for templates, tester notes and testing resources." to c("Şablonlar, testçi notları ve test kaynakları için tek seferlik satın alma.", "Achat unique pour les modèles, notes de testeurs et ressources.", "Compra única para plantillas, notas de testers y recursos.", "一次购买即可使用模板、测试者备注和测试资源。", "टेम्पलेट, टेस्टर नोट और टेस्ट संसाधनों के लिए एकमुश्त खरीद।", "Разовая покупка шаблонов, заметок о тестировщиках и материалов.", "شراء لمرة واحدة للقوالب وملاحظات المختبرين وموارد الاختبار.", "Einmalkauf für Vorlagen, Testernotizen und Testressourcen.", "テンプレート、テスターのメモ、テスト資料を買い切りで利用できます。", "Compra única para modelos, notas de testadores e recursos.", "Pembelian sekali untuk templat, catatan penguji, dan sumber pengujian."),
    "Ad is ready. Tap to watch." to c("Reklam hazır. İzlemek için dokun.", "La publicité est prête. Touchez pour regarder.", "El anuncio está listo. Toca para verlo.", "广告已准备好，点击观看。", "विज्ञापन तैयार है। देखने के लिए टैप करें।", "Реклама готова. Нажмите для просмотра.", "الإعلان جاهز. اضغط للمشاهدة.", "Die Anzeige ist bereit. Tippe zum Ansehen.", "広告の準備ができました。タップして視聴します。", "O anúncio está pronto. Toque para assistir.", "Iklan siap. Ketuk untuk menonton."),
    "Preparing ad..." to c("Reklam hazırlanıyor...", "Préparation de la publicité...", "Preparando anuncio...", "正在准备广告…", "विज्ञापन तैयार हो रहा है...", "Подготовка рекламы...", "جارٍ تجهيز الإعلان...", "Anzeige wird vorbereitet...", "広告を準備中…", "Preparando anúncio...", "Menyiapkan iklan..."),
    "You can watch an ad to support the app." to c("Uygulamayı desteklemek için reklam izleyebilirsin.", "Vous pouvez regarder une publicité pour soutenir l'application.", "Puedes ver un anuncio para apoyar la aplicación.", "你可以观看广告来支持本应用。", "ऐप का समर्थन करने के लिए विज्ञापन देख सकते हैं।", "Вы можете посмотреть рекламу, чтобы поддержать приложение.", "يمكنك مشاهدة إعلان لدعم التطبيق.", "Du kannst eine Anzeige ansehen, um die App zu unterstützen.", "広告を視聴してアプリを支援できます。", "Você pode assistir a um anúncio para apoiar o app.", "Anda dapat menonton iklan untuk mendukung aplikasi."),
    "Tracked apps appear here as icons." to c("Takip edilen uygulamalar burada simge olarak görünür.", "Les applications suivies apparaissent ici sous forme d'icônes.", "Las apps seguidas aparecen aquí como iconos.", "跟踪的应用会以图标显示在这里。", "ट्रैक किए गए ऐप यहाँ आइकन के रूप में दिखेंगे।", "Отслеживаемые приложения появятся здесь в виде значков.", "تظهر التطبيقات المتابعة هنا كأيقونات.", "Verfolgte Apps erscheinen hier als Symbole.", "追跡中のアプリがアイコンで表示されます。", "Os apps acompanhados aparecem aqui como ícones.", "Aplikasi yang dilacak muncul di sini sebagai ikon.")
)

// These strings already have the first eight translations at their call sites.
private val addedLanguageCopy = mapOf(
    "Share screenshot" to arrayOf("Screenshot teilen", "スクリーンショットを共有", "Compartilhar captura", "Bagikan tangkapan layar"),
    "Could not open share sheet" to arrayOf("Teilen-Menü konnte nicht geöffnet werden", "共有画面を開けませんでした", "Não foi possível abrir o menu de compartilhamento", "Menu berbagi tidak dapat dibuka"),
    "Share app" to arrayOf("App teilen", "アプリを共有", "Compartilhar app", "Bagikan aplikasi"),
    "General summary" to arrayOf("Gesamtübersicht", "全体概要", "Resumo geral", "Ringkasan umum"),
    "Ad is ready." to arrayOf("Anzeige ist bereit.", "広告の準備ができました。", "O anúncio está pronto.", "Iklan siap."),
    "Ad is loading, try again in a few seconds." to arrayOf("Anzeige wird geladen. Versuche es in einigen Sekunden erneut.", "広告を読み込み中です。数秒後にもう一度お試しください。", "O anúncio está carregando. Tente novamente em alguns segundos.", "Iklan sedang dimuat. Coba lagi dalam beberapa detik."),
    "Thanks for your support!" to arrayOf("Danke für deine Unterstützung!", "ご支援ありがとうございます！", "Obrigado pelo apoio!", "Terima kasih atas dukungan Anda!"),
    "Term of usage & Privacy Policy" to arrayOf("Nutzungsbedingungen & Datenschutz", "利用規約とプライバシーポリシー", "Termos de uso e Política de Privacidade", "Ketentuan penggunaan & Kebijakan Privasi"),
    "Share app with friends" to arrayOf("App mit Freunden teilen", "友達にアプリを共有", "Compartilhar app com amigos", "Bagikan aplikasi dengan teman"),
    "Share the Closed Test Tracker link." to arrayOf("Teile den Link zu Closed Test Tracker.", "Closed Test Trackerのリンクを共有します。", "Compartilhe o link do Closed Test Tracker.", "Bagikan tautan Closed Test Tracker."),
    "Support by watching ads" to arrayOf("Durch Werbung unterstützen", "広告視聴でサポート", "Apoiar assistindo a anúncios", "Dukung dengan menonton iklan"),
    "When you open the app for the first time" to arrayOf("Beim ersten Öffnen der App", "初めてアプリを開いたとき", "Ao abrir o app pela primeira vez", "Saat pertama kali membuka aplikasi"),
    "After that" to arrayOf("Danach", "その後", "Depois disso", "Setelah itu"),
    "After selecting the day" to arrayOf("Nach Auswahl des Tages", "日数を選択した後", "Depois de selecionar o dia", "Setelah memilih hari"),
    "Tap to choose" to arrayOf("Zum Auswählen tippen", "タップして選択", "Toque para escolher", "Ketuk untuk memilih")
    ,"No suitable ad is available right now. Try again later." to arrayOf("Zurzeit ist keine passende Anzeige verfügbar. Versuche es später erneut.", "現在利用できる広告がありません。後でもう一度お試しください。", "Nenhum anúncio adequado está disponível agora. Tente mais tarde.", "Belum ada iklan yang sesuai. Coba lagi nanti."),
    "Ad could not load because of a network issue." to arrayOf("Die Anzeige konnte wegen eines Netzwerkproblems nicht geladen werden.", "ネットワークの問題により広告を読み込めませんでした。", "O anúncio não pôde ser carregado por um problema de rede.", "Iklan gagal dimuat karena masalah jaringan."),
    "The ad request looks invalid. Publishing settings should be checked." to arrayOf("Die Anzeigenanfrage ist ungültig. Prüfe die Veröffentlichungseinstellungen.", "広告リクエストが無効です。公開設定を確認してください。", "A solicitação do anúncio é inválida. Verifique as configurações de publicação.", "Permintaan iklan tidak valid. Periksa pengaturan publikasi."),
    "Ad could not load. Try again later." to arrayOf("Die Anzeige konnte nicht geladen werden. Versuche es später erneut.", "広告を読み込めませんでした。後でもう一度お試しください。", "O anúncio não pôde ser carregado. Tente mais tarde.", "Iklan gagal dimuat. Coba lagi nanti."),
    "Today loading" to arrayOf("Heute wird geladen", "今日の利用時間を読み込み中", "Carregando hoje", "Memuat penggunaan hari ini"),
    "Enable Usage Access from phone settings. This permission is required to track daily usage times." to arrayOf("Aktiviere den Nutzungszugriff in den Geräteeinstellungen. Er wird für die tägliche Nutzungszeit benötigt.", "端末の設定で使用状況へのアクセスを許可してください。毎日の利用時間を追跡するために必要です。", "Ative o acesso ao uso nas configurações do telefone. Essa permissão é necessária para acompanhar o uso diário.", "Aktifkan akses penggunaan di pengaturan ponsel. Izin ini diperlukan untuk melacak waktu penggunaan harian."),
    "Add apps using the + icon at the top. If you already started testing before, set the current day and continue." to arrayOf("Füge Apps über das Plus-Symbol oben hinzu. Wenn der Test bereits läuft, stelle den aktuellen Tag ein.", "上部の＋アイコンからアプリを追加します。すでにテストを開始している場合は、現在の日数を設定してください。", "Adicione apps pelo ícone + no topo. Se o teste já começou, defina o dia atual e continue.", "Tambahkan aplikasi dengan ikon + di atas. Jika tes sudah dimulai, atur hari saat ini lalu lanjutkan."),
    "Tracking starts. You can see streak day and usage minutes in both summary and list. Tap the icon to open the app directly." to arrayOf("Das Tracking beginnt. Serientag und Nutzungsminuten erscheinen in Übersicht und Liste. Tippe auf das Symbol, um die App zu öffnen.", "追跡が始まります。継続日数と利用時間は概要と一覧で確認できます。アイコンをタップするとアプリを直接開けます。", "O acompanhamento começa. O dia da sequência e os minutos aparecem no resumo e na lista. Toque no ícone para abrir o app.", "Pelacakan dimulai. Hari rangkaian dan menit penggunaan terlihat di ringkasan dan daftar. Ketuk ikon untuk membuka aplikasi.")
)

private val proCopy = mapOf(
    "One-time purchase. No subscription." to c("Tek seferlik satın alma. Abonelik yok.", "Achat unique. Sans abonnement.", "Compra única. Sin suscripción.", "一次购买，无需订阅。", "एकमुश्त खरीद। कोई सदस्यता नहीं।", "Разовая покупка. Без подписки.", "شراء لمرة واحدة. بدون اشتراك.", "Einmalkauf. Kein Abo.", "買い切りです。定期購入ではありません。", "Compra única. Sem assinatura.", "Pembelian sekali. Tanpa langganan."),
    "No ads" to c("Reklam yok", "Sans publicité", "Sin anuncios", "无广告", "विज्ञापन नहीं", "Без рекламы", "بدون إعلانات", "Keine Werbung", "広告なし", "Sem anúncios", "Tanpa iklan"),
    "Testing resources and practical guides" to c("Test kaynakları ve pratik rehberler", "Ressources et guides pratiques", "Recursos y guías prácticas", "测试资源和实用指南", "टेस्ट संसाधन और व्यावहारिक गाइड", "Материалы и практические руководства", "موارد اختبار وأدلة عملية", "Testressourcen und praktische Leitfäden", "テスト資料と実用ガイド", "Recursos e guias práticos", "Sumber dan panduan praktis"),
    "3 custom sharing templates" to c("3 özel paylaşım şablonu", "3 modèles de partage personnalisés", "3 plantillas personalizadas", "3 个自定义分享模板", "3 कस्टम शेयर टेम्पलेट", "3 пользовательских шаблона", "3 قوالب مشاركة مخصصة", "3 eigene Freigabevorlagen", "3つの共有テンプレート", "3 modelos personalizados", "3 templat berbagi khusus"),
    "Tester usernames, profiles and notes" to c("Testçi kullanıcı adları, profilleri ve notları", "Noms, profils et notes des testeurs", "Usuarios, perfiles y notas de testers", "测试者用户名、资料和备注", "टेस्टर यूज़रनेम, प्रोफ़ाइल और नोट", "Имена, профили и заметки тестировщиков", "أسماء المختبرين وملفاتهم وملاحظاتهم", "Testernamen, Profile und Notizen", "テスター名、プロフィール、メモ", "Usuários, perfis e notas de testadores", "Nama pengguna, profil, dan catatan penguji"),
    "Later" to c("Daha sonra", "Plus tard", "Más tarde", "稍后", "बाद में", "Позже", "لاحقًا", "Später", "後で", "Mais tarde", "Nanti"),
    "Pay once to remove ads and unlock Pro tools." to c("Reklamları kaldırmak ve Pro araçlarını açmak için bir kez ödeme yap.", "Payez une fois pour supprimer les publicités et débloquer les outils Pro.", "Paga una vez para quitar anuncios y desbloquear Pro.", "一次付费即可移除广告并解锁 Pro 工具。", "विज्ञापन हटाने और Pro टूल अनलॉक करने के लिए एक बार भुगतान करें।", "Заплатите один раз, чтобы убрать рекламу и открыть Pro.", "ادفع مرة واحدة لإزالة الإعلانات وفتح أدوات Pro.", "Einmal zahlen, Werbung entfernen und Pro freischalten.", "一度の支払いで広告を削除し、Pro機能を利用できます。", "Pague uma vez para remover anúncios e liberar o Pro.", "Bayar sekali untuk menghapus iklan dan membuka Pro."),
    "Testing resources" to c("Test kaynakları", "Ressources de test", "Recursos de prueba", "测试资源", "टेस्ट संसाधन", "Материалы для тестирования", "موارد الاختبار", "Testressourcen", "テスト資料", "Recursos de teste", "Sumber pengujian"),
    "14-day testing checklist" to c("14 günlük test kontrol listesi", "Liste de contrôle de 14 jours", "Lista de prueba de 14 días", "14 天测试清单", "14-दिवसीय टेस्ट चेकलिस्ट", "Чек-лист теста на 14 дней", "قائمة اختبار لمدة 14 يومًا", "14-Tage-Testcheckliste", "14日間テストのチェックリスト", "Checklist de teste de 14 dias", "Daftar periksa tes 14 hari"),
    "Use the app daily, record feedback, and keep release notes." to c("Uygulamayı her gün kullan, geri bildirimleri kaydet ve sürüm notlarını tut.", "Utilisez l'application chaque jour, notez les retours et conservez les notes de version.", "Usa la app a diario, registra comentarios y conserva las notas de versión.", "每天使用应用，记录反馈并保留版本说明。", "ऐप रोज़ उपयोग करें, फ़ीडबैक और रिलीज़ नोट दर्ज करें।", "Используйте приложение ежедневно, записывайте отзывы и заметки к выпуску.", "استخدم التطبيق يوميًا وسجّل الملاحظات واحتفظ بملاحظات الإصدار.", "Nutze die App täglich, erfasse Feedback und führe Versionsnotizen.", "毎日アプリを使用し、フィードバックとリリースノートを記録します。", "Use o app diariamente, registre feedback e mantenha notas da versão.", "Gunakan aplikasi setiap hari, catat umpan balik dan catatan rilis."),
    "Finding testers" to c("Testçi bulma", "Trouver des testeurs", "Encontrar testers", "寻找测试者", "टेस्टर ढूँढना", "Поиск тестировщиков", "العثور على مختبرين", "Tester finden", "テスターを探す", "Encontrar testadores", "Mencari penguji"),
    "Share Google Group and opt-in links in one post; ask testers for real daily use and short feedback for 14 days." to c("Google Group ve katılım bağlantılarını tek gönderide paylaş; testçilerden 14 gün boyunca gerçek kullanım ve kısa geri bildirim iste.", "Partagez les liens Google Group et d'inscription dans un seul message et demandez 14 jours d'utilisation réelle avec un bref retour.", "Comparte los enlaces de Google Group y participación en una publicación y pide 14 días de uso real y comentarios breves.", "在一篇帖子中分享 Google Group 和加入测试链接，并请测试者连续 14 天真实使用并提供简短反馈。", "Google Group और ऑप्ट-इन लिंक एक पोस्ट में साझा करें और 14 दिनों तक वास्तविक उपयोग व संक्षिप्त फ़ीडबैक माँगें।", "Поделитесь ссылками Google Group и участия в одном сообщении и попросите 14 дней реального использования и краткие отзывы.", "شارك روابط Google Group والانضمام في منشور واحد واطلب استخدامًا فعليًا وملاحظات قصيرة لمدة 14 يومًا.", "Teile Google-Group- und Opt-in-Links in einem Beitrag und bitte 14 Tage lang um echte Nutzung und kurzes Feedback.", "Google Groupと参加リンクを1つの投稿で共有し、14日間の実利用と短いフィードバックを依頼します。", "Compartilhe os links do Google Group e adesão em uma publicação e peça 14 dias de uso real e feedback curto.", "Bagikan tautan Google Group dan keikutsertaan dalam satu posting, lalu minta penggunaan nyata dan umpan balik singkat selama 14 hari."),
    "Feedback workflow" to c("Geri bildirim düzeni", "Processus de retour", "Flujo de comentarios", "反馈流程", "फ़ीडबैक प्रक्रिया", "Процесс обратной связи", "سير عمل الملاحظات", "Feedback-Ablauf", "フィードバック手順", "Fluxo de feedback", "Alur umpan balik"),
    "Record device model, Android version, reproduction steps and screenshots together." to c("Cihaz modelini, Android sürümünü, tekrar adımlarını ve ekran görüntülerini birlikte kaydet.", "Enregistrez ensemble le modèle, la version Android, les étapes de reproduction et les captures.", "Registra juntos el modelo, la versión de Android, los pasos y las capturas.", "同时记录设备型号、Android 版本、复现步骤和截图。", "डिवाइस मॉडल, Android संस्करण, दोहराने के चरण और स्क्रीनशॉट साथ में दर्ज करें।", "Записывайте модель устройства, версию Android, шаги воспроизведения и снимки экрана вместе.", "سجّل طراز الجهاز وإصدار Android وخطوات إعادة المشكلة ولقطات الشاشة معًا.", "Erfasse Gerätemodell, Android-Version, Reproduktionsschritte und Screenshots gemeinsam.", "端末モデル、Androidバージョン、再現手順、スクリーンショットをまとめて記録します。", "Registre modelo, versão do Android, passos de reprodução e capturas juntos.", "Catat model perangkat, versi Android, langkah reproduksi, dan tangkapan layar bersama."),
    "Before production" to c("Yayın öncesi", "Avant la production", "Antes de producción", "发布前", "प्रोडक्शन से पहले", "Перед выпуском", "قبل الإنتاج", "Vor der Veröffentlichung", "本番公開前", "Antes da produção", "Sebelum produksi"),
    "Review crashes, ANRs, Data safety, permissions and store listing before production." to c("Yayın öncesinde çökmeleri, ANR'ları, Veri güvenliği beyanını, izinleri ve mağaza sayfasını kontrol et.", "Avant la production, vérifiez les plantages, ANR, la sécurité des données, les autorisations et la fiche Store.", "Antes de producción, revisa fallos, ANR, Seguridad de datos, permisos y ficha de la tienda.", "发布前检查崩溃、ANR、数据安全、权限和商店详情。", "प्रोडक्शन से पहले क्रैश, ANR, डेटा सुरक्षा, अनुमतियाँ और स्टोर लिस्टिंग जाँचें।", "Перед выпуском проверьте сбои, ANR, раздел безопасности данных, разрешения и страницу магазина.", "راجع الأعطال وANR وأمان البيانات والأذونات وصفحة المتجر قبل الإنتاج.", "Prüfe vor der Veröffentlichung Abstürze, ANRs, Datensicherheit, Berechtigungen und Store-Eintrag.", "公開前にクラッシュ、ANR、データセーフティ、権限、ストア情報を確認します。", "Antes da produção, revise falhas, ANRs, Segurança de dados, permissões e ficha da loja.", "Sebelum produksi, tinjau crash, ANR, Keamanan data, izin, dan listing toko."),
    "Template name" to c("Şablon adı", "Nom du modèle", "Nombre de plantilla", "模板名称", "टेम्पलेट नाम", "Название шаблона", "اسم القالب", "Vorlagenname", "テンプレート名", "Nome do modelo", "Nama templat"),
    "Template text" to c("Şablon metni", "Texte du modèle", "Texto de plantilla", "模板文本", "टेम्पलेट टेक्स्ट", "Текст шаблона", "نص القالب", "Vorlagentext", "テンプレート本文", "Texto do modelo", "Teks templat"),
    "Save template" to c("Şablonu kaydet", "Enregistrer le modèle", "Guardar plantilla", "保存模板", "टेम्पलेट सहेजें", "Сохранить шаблон", "حفظ القالب", "Vorlage speichern", "テンプレートを保存", "Salvar modelo", "Simpan templat"),
    "Tester list" to c("Testçi listesi", "Liste des testeurs", "Lista de testers", "测试者列表", "टेस्टर सूची", "Список тестировщиков", "قائمة المختبرين", "Testerliste", "テスター一覧", "Lista de testadores", "Daftar penguji"),
    "Reddit username" to c("Reddit kullanıcı adı", "Nom Reddit", "Usuario de Reddit", "Reddit 用户名", "Reddit यूज़रनेम", "Имя в Reddit", "اسم مستخدم Reddit", "Reddit-Benutzername", "Redditユーザー名", "Usuário do Reddit", "Nama pengguna Reddit"),
    "Profile link" to c("Profil bağlantısı", "Lien du profil", "Enlace de perfil", "资料链接", "प्रोफ़ाइल लिंक", "Ссылка на профиль", "رابط الملف الشخصي", "Profillink", "プロフィールリンク", "Link do perfil", "Tautan profil"),
    "Note" to c("Not", "Note", "Nota", "备注", "नोट", "Заметка", "ملاحظة", "Notiz", "メモ", "Nota", "Catatan"),
    "Regular/reliable tester" to c("Düzenli/güvenilir testçi", "Testeur régulier/fiable", "Tester regular/fiable", "稳定可靠的测试者", "नियमित/विश्वसनीय टेस्टर", "Регулярный/надёжный тестировщик", "مختبر منتظم وموثوق", "Regelmäßiger/zuverlässiger Tester", "定期的で信頼できるテスター", "Testador regular/confiável", "Penguji rutin/tepercaya"),
    "Reliable tester" to c("Güvenilir testçi", "Testeur fiable", "Tester fiable", "可靠的测试者", "विश्वसनीय टेस्टर", "Надёжный тестировщик", "مختبر موثوق", "Zuverlässiger Tester", "信頼できるテスター", "Testador confiável", "Penguji tepercaya"),
    "Review carefully" to c("Dikkatli değerlendir", "Examiner avec attention", "Revisar con cuidado", "谨慎评估", "ध्यान से समीक्षा करें", "Проверить внимательно", "راجع بعناية", "Sorgfältig prüfen", "慎重に確認", "Revisar com cuidado", "Tinjau dengan cermat"),
    "Add tester" to c("Testçiyi ekle", "Ajouter le testeur", "Añadir tester", "添加测试者", "टेस्टर जोड़ें", "Добавить тестировщика", "إضافة مختبر", "Tester hinzufügen", "テスターを追加", "Adicionar testador", "Tambah penguji")
)

internal fun translatedCopy(language: AppLanguage, english: String): String? {
    if (language == AppLanguage.EN) return english
    extendedUiCopy(language, english)?.let { return it }
    Regex("""^Custom post templates \((\d+)/3\)$""").matchEntire(english)?.let {
        val count = it.groupValues[1]
        return when (language) {
            AppLanguage.TR -> "Özel paylaşım şablonları ($count/3)"
            AppLanguage.FR -> "Modèles personnalisés ($count/3)"
            AppLanguage.ES -> "Plantillas personalizadas ($count/3)"
            AppLanguage.ZH -> "自定义分享模板 ($count/3)"
            AppLanguage.HI -> "कस्टम शेयर टेम्पलेट ($count/3)"
            AppLanguage.RU -> "Пользовательские шаблоны ($count/3)"
            AppLanguage.AR -> "قوالب مشاركة مخصصة ($count/3)"
            AppLanguage.DE -> "Eigene Freigabevorlagen ($count/3)"
            AppLanguage.JA -> "共有テンプレート ($count/3)"
            AppLanguage.PT -> "Modelos personalizados ($count/3)"
            AppLanguage.ID -> "Templat berbagi khusus ($count/3)"
            AppLanguage.EN -> english
        }
    }
    Regex("""^Today (\d+) (.+) \| Total (\d+) (.+)$""").matchEntire(english)?.let {
        val today = it.groupValues[1]
        val total = it.groupValues[3]
        return when (language) {
            AppLanguage.TR -> "Bugün $today dk | Toplam $total dk"
            AppLanguage.FR -> "Aujourd'hui $today min | Total $total min"
            AppLanguage.ES -> "Hoy $today min | Total $total min"
            AppLanguage.ZH -> "今天 $today 分钟 | 总计 $total 分钟"
            AppLanguage.HI -> "आज $today मिनट | कुल $total मिनट"
            AppLanguage.RU -> "Сегодня $today мин | Всего $total мин"
            AppLanguage.AR -> "اليوم $today د | الإجمالي $total د"
            AppLanguage.DE -> "Heute $today Min. | Gesamt $total Min."
            AppLanguage.JA -> "今日 $today 分 | 合計 $total 分"
            AppLanguage.PT -> "Hoje $today min | Total $total min"
            AppLanguage.ID -> "Hari ini $today mnt | Total $total mnt"
            AppLanguage.EN -> english
        }
    }
    Regex("""^Today (\d+) (.+) \| Total loading$""").matchEntire(english)?.let {
        val today = it.groupValues[1]
        return when (language) {
            AppLanguage.TR -> "Bugün $today dk | Toplam yükleniyor"
            AppLanguage.FR -> "Aujourd'hui $today min | Chargement du total"
            AppLanguage.ES -> "Hoy $today min | Cargando total"
            AppLanguage.ZH -> "今天 $today 分钟 | 正在加载总计"
            AppLanguage.HI -> "आज $today मिनट | कुल लोड हो रहा है"
            AppLanguage.RU -> "Сегодня $today мин | Загрузка итога"
            AppLanguage.AR -> "اليوم $today د | جارٍ تحميل الإجمالي"
            AppLanguage.DE -> "Heute $today Min. | Gesamt wird geladen"
            AppLanguage.JA -> "今日 $today 分 | 合計を読み込み中"
            AppLanguage.PT -> "Hoje $today min | Carregando total"
            AppLanguage.ID -> "Hari ini $today mnt | Memuat total"
            AppLanguage.EN -> english
        }
    }
    fullCopy[english]?.let { return it.get(language) }
    proCopy[english]?.let { return it.get(language) }
    advancedProCopy(language, english)?.let { return it }
    addedLanguageCopy[english]?.let { values ->
        return when (language) {
            AppLanguage.DE -> values[0]
            AppLanguage.JA -> values[1]
            AppLanguage.PT -> values[2]
            AppLanguage.ID -> values[3]
            else -> null
        }
    }
    return null
}
