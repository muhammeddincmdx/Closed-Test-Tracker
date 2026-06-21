package com.mdstudio.closedtesttracker

private data class UiCopy4(val de: String, val ja: String, val pt: String, val id: String)
private fun ui(de: String, ja: String, pt: String, id: String) = UiCopy4(de, ja, pt, id)

private val extendedUiStrings = mapOf(
    "14 days" to ui("14 Tage", "14日間", "14 dias", "14 hari"),
    "14-day summary" to ui("14-Tage-Übersicht", "14日間の概要", "Resumo de 14 dias", "Ringkasan 14 hari"),
    "A new Closed Test Tracker version is ready. Tap to update." to ui("Eine neue Version von Closed Test Tracker ist verfügbar. Zum Aktualisieren tippen.", "Closed Test Trackerの新しいバージョンがあります。タップして更新してください。", "Uma nova versão do Closed Test Tracker está pronta. Toque para atualizar.", "Versi baru Closed Test Tracker tersedia. Ketuk untuk memperbarui."),
    "Ad could not load because of a network issue." to ui("Die Anzeige konnte wegen eines Netzwerkproblems nicht geladen werden.", "ネットワークの問題により広告を読み込めませんでした。", "O anúncio não pôde ser carregado por um problema de rede.", "Iklan gagal dimuat karena masalah jaringan."),
    "Ad could not load. Try again later." to ui("Die Anzeige konnte nicht geladen werden. Versuche es später erneut.", "広告を読み込めませんでした。後でもう一度お試しください。", "O anúncio não pôde ser carregado. Tente novamente mais tarde.", "Iklan gagal dimuat. Coba lagi nanti."),
    "Ad is loading, try again in a few seconds." to ui("Die Anzeige wird geladen. Versuche es in einigen Sekunden erneut.", "広告を読み込み中です。数秒後にもう一度お試しください。", "O anúncio está carregando. Tente novamente em alguns segundos.", "Iklan sedang dimuat. Coba lagi dalam beberapa detik."),
    "Ad is ready." to ui("Die Anzeige ist bereit.", "広告の準備ができました。", "O anúncio está pronto.", "Iklan siap."),
    "Add apps using the + icon at the top. If you already started testing before, set the current day and continue." to ui("Füge Apps über das Plus-Symbol oben hinzu. Wenn der Test bereits begonnen hat, stelle den aktuellen Tag ein.", "上部の＋アイコンからアプリを追加します。すでにテストを開始している場合は、現在の日数を設定して続けてください。", "Adicione apps pelo ícone + no topo. Se o teste já começou, defina o dia atual e continue.", "Tambahkan aplikasi lewat ikon + di atas. Jika tes sudah dimulai, atur hari saat ini lalu lanjutkan."),
    "After selecting the day" to ui("Nach Auswahl des Tages", "日数を選択した後", "Depois de selecionar o dia", "Setelah memilih hari"),
    "After that" to ui("Danach", "その後", "Depois disso", "Setelah itu"),
    "Android returns incomplete daily history for this app. Total time is calculated from the broad range total; the graph and text summary show only daily records available from the device." to ui("Android liefert für diese App keinen vollständigen Tagesverlauf. Die Gesamtzeit wird aus dem Gesamtbereich berechnet; Grafik und Text zeigen nur verfügbare Tagesdaten.", "このアプリではAndroidから完全な日別履歴を取得できません。合計時間は広い期間の集計から計算され、グラフと文章には端末から取得できた日別記録のみ表示されます。", "O Android fornece um histórico diário incompleto para este app. O total usa o período amplo; gráfico e texto mostram apenas os registros disponíveis.", "Android memberikan riwayat harian yang tidak lengkap untuk aplikasi ini. Total dihitung dari rentang luas; grafik dan teks hanya menampilkan catatan yang tersedia."),
    "Built to keep Google Play closed tests organized and track the 14-day streak with daily usage minutes in one place." to ui("Organisiert geschlossene Google-Play-Tests und verfolgt die 14-Tage-Serie samt täglicher Nutzungszeit an einem Ort.", "Google Playのクローズドテストを整理し、14日間の継続状況と毎日の利用時間を一か所で確認できます。", "Organiza testes fechados do Google Play e acompanha a sequência de 14 dias com minutos diários em um só lugar.", "Mengatur pengujian tertutup Google Play dan melacak rangkaian 14 hari beserta menit penggunaan harian di satu tempat."),
    "Change the date display used in charts and lists." to ui("Ändere das Datumsformat in Diagrammen und Listen.", "グラフと一覧で使用する日付表示を変更します。", "Altere o formato de data usado em gráficos e listas.", "Ubah tampilan tanggal pada grafik dan daftar."),
    "Change the search or check already added apps." to ui("Ändere die Suche oder prüfe bereits hinzugefügte Apps.", "検索条件を変更するか、追加済みのアプリを確認してください。", "Altere a pesquisa ou verifique os apps já adicionados.", "Ubah pencarian atau periksa aplikasi yang sudah ditambahkan."),
    "Choose another filter or add a new app." to ui("Wähle einen anderen Filter oder füge eine neue App hinzu.", "別のフィルターを選ぶか、新しいアプリを追加してください。", "Escolha outro filtro ou adicione um novo app.", "Pilih filter lain atau tambahkan aplikasi baru."),
    "Could not open share sheet" to ui("Das Teilen-Menü konnte nicht geöffnet werden.", "共有画面を開けませんでした。", "Não foi possível abrir o menu de compartilhamento.", "Menu berbagi tidak dapat dibuka."),
    "Daily and total time is being read from the device." to ui("Tägliche und gesamte Nutzungszeit werden vom Gerät gelesen.", "毎日および合計の利用時間を端末から取得しています。", "O tempo diário e total está sendo lido do dispositivo.", "Waktu harian dan total sedang dibaca dari perangkat."),
    "Day.Month" to ui("Tag.Monat", "日.月", "Dia.Mês", "Hari.Bulan"),
    "Donate / Buy me a coffee" to ui("Spenden / Kaffee ausgeben", "寄付・コーヒーで支援", "Doar / Pagar um café", "Donasi / Traktir kopi"),
    "Enable Usage Access from phone settings. This permission is required to track daily usage times." to ui("Aktiviere den Nutzungszugriff in den Geräteeinstellungen. Er ist nötig, um tägliche Nutzungszeiten zu verfolgen.", "端末の設定で使用状況へのアクセスを許可してください。毎日の利用時間を追跡するために必要です。", "Ative o acesso ao uso nas configurações. Essa permissão é necessária para acompanhar o uso diário.", "Aktifkan akses penggunaan di pengaturan ponsel. Izin ini diperlukan untuk melacak waktu penggunaan harian."),
    "For web/PWA shortcuts, time may be counted under the browser. Android does not expose per-site usage as separate apps, so some web-style apps may show 0 min." to ui("Bei Web-/PWA-Verknüpfungen kann die Zeit dem Browser zugeordnet werden. Android zeigt die Nutzung einzelner Websites nicht separat; manche Web-Apps können daher 0 Min. anzeigen.", "Web/PWAショートカットの利用時間はブラウザ側に計上される場合があります。Androidはサイト別利用時間を個別アプリとして公開しないため、0分と表示されることがあります。", "Em atalhos web/PWA, o tempo pode ser contado no navegador. O Android não separa o uso por site, então alguns apps web podem mostrar 0 min.", "Untuk pintasan web/PWA, waktu dapat tercatat pada browser. Android tidak memisahkan penggunaan tiap situs, sehingga sebagian aplikasi web dapat menunjukkan 0 menit."),
    "General summary" to ui("Gesamtübersicht", "全体概要", "Resumo geral", "Ringkasan umum"),
    "Hello, I would like support about the app." to ui("Hallo, ich benötige Unterstützung zur App.", "こんにちは。アプリについてサポートをお願いします。", "Olá, gostaria de ajuda com o aplicativo.", "Halo, saya membutuhkan bantuan terkait aplikasi."),
    "If the system language is not supported, the app automatically uses English." to ui("Wenn die Systemsprache nicht unterstützt wird, verwendet die App automatisch Englisch.", "システム言語が対応していない場合は、自動的に英語を使用します。", "Se o idioma do sistema não for compatível, o app usará inglês automaticamente.", "Jika bahasa sistem tidak didukung, aplikasi otomatis menggunakan bahasa Inggris."),
    "Last 14 days" to ui("Letzte 14 Tage", "直近14日間", "Últimos 14 dias", "14 hari terakhir"),
    "Month.Day" to ui("Monat.Tag", "月.日", "Mês.Dia", "Bulan.Hari"),
    "No apps added yet" to ui("Noch keine Apps hinzugefügt", "アプリはまだ追加されていません", "Nenhum app adicionado ainda", "Belum ada aplikasi ditambahkan"),
    "No new update is currently available." to ui("Derzeit ist kein neues Update verfügbar.", "現在、新しいアップデートはありません。", "Nenhuma nova atualização está disponível no momento.", "Saat ini belum ada pembaruan baru."),
    "No suitable ad is available right now. Try again later." to ui("Zurzeit ist keine passende Anzeige verfügbar. Versuche es später erneut.", "現在利用できる広告がありません。後でもう一度お試しください。", "Nenhum anúncio adequado está disponível agora. Tente mais tarde.", "Belum ada iklan yang sesuai. Coba lagi nanti."),
    "No usage data yet" to ui("Noch keine Nutzungsdaten", "利用データはまだありません", "Ainda não há dados de uso", "Belum ada data penggunaan"),
    "Off. Enable it for reminders." to ui("Aus. Für Erinnerungen aktivieren.", "オフです。通知を受けるには有効にしてください。", "Desativado. Ative para receber lembretes.", "Nonaktif. Aktifkan untuk pengingat."),
    "On. Tap to review reminder settings." to ui("Ein. Tippe, um die Erinnerungseinstellungen zu prüfen.", "オンです。タップして通知設定を確認します。", "Ativado. Toque para revisar os lembretes.", "Aktif. Ketuk untuk meninjau pengaturan pengingat."),
    "Open the Google Play page to review what's new and install the update." to ui("Öffne Google Play, um Neuerungen anzusehen und das Update zu installieren.", "Google Playを開いて新機能を確認し、アップデートをインストールします。", "Abra o Google Play para ver as novidades e instalar a atualização.", "Buka Google Play untuk melihat hal baru dan memasang pembaruan."),
    "Play Store" to ui("Play Store", "Play ストア", "Play Store", "Play Store"),
    "Share app" to ui("App teilen", "アプリを共有", "Compartilhar app", "Bagikan aplikasi"),
    "Share app with friends" to ui("App mit Freunden teilen", "友達にアプリを共有", "Compartilhar app com amigos", "Bagikan aplikasi dengan teman"),
    "Share screenshot" to ui("Screenshot teilen", "スクリーンショットを共有", "Compartilhar captura de tela", "Bagikan tangkapan layar"),
    "Share the Closed Test Tracker link." to ui("Teile den Link zu Closed Test Tracker.", "Closed Test Trackerのリンクを共有します。", "Compartilhe o link do Closed Test Tracker.", "Bagikan tautan Closed Test Tracker."),
    "Support by watching ads" to ui("Durch Werbung unterstützen", "広告視聴で支援", "Apoiar assistindo a anúncios", "Dukung dengan menonton iklan"),
    "Tap to add" to ui("Zum Hinzufügen tippen", "タップして追加", "Toque para adicionar", "Ketuk untuk menambahkan"),
    "Tap to choose" to ui("Zum Auswählen tippen", "タップして選択", "Toque para escolher", "Ketuk untuk memilih"),
    "Term of usage & Privacy Policy" to ui("Nutzungsbedingungen und Datenschutz", "利用規約とプライバシーポリシー", "Termos de uso e Política de Privacidade", "Ketentuan penggunaan dan Kebijakan Privasi"),
    "Thanks for your support!" to ui("Danke für deine Unterstützung!", "ご支援ありがとうございます！", "Obrigado pelo apoio!", "Terima kasih atas dukungan Anda!"),
    "The ad request looks invalid. Publishing settings should be checked." to ui("Die Anzeigenanfrage ist ungültig. Prüfe die Veröffentlichungseinstellungen.", "広告リクエストが無効です。公開設定を確認してください。", "A solicitação do anúncio parece inválida. Verifique as configurações de publicação.", "Permintaan iklan tampaknya tidak valid. Periksa pengaturan publikasi."),
    "The streak keeps running until you finish." to ui("Die Serie läuft weiter, bis du den Test beendest.", "テストを完了するまで継続日数は増え続けます。", "A sequência continua até você finalizar.", "Rangkaian terus berjalan sampai Anda menyelesaikannya."),
    "Today loading" to ui("Heute wird geladen", "今日の利用時間を読み込み中", "Carregando hoje", "Memuat penggunaan hari ini"),
    "Tracking starts. You can see streak day and usage minutes in both summary and list. Tap the icon to open the app directly." to ui("Das Tracking beginnt. Serientag und Nutzungsminuten erscheinen in Übersicht und Liste. Tippe auf das Symbol, um die App zu öffnen.", "追跡が始まります。継続日数と利用時間は概要と一覧で確認でき、アイコンをタップするとアプリを直接開けます。", "O acompanhamento começa. O dia da sequência e os minutos aparecem no resumo e na lista. Toque no ícone para abrir o app.", "Pelacakan dimulai. Hari rangkaian dan menit penggunaan terlihat di ringkasan dan daftar. Ketuk ikon untuk membuka aplikasi."),
    "Usage Access is required to show minutes." to ui("Zum Anzeigen der Minuten ist Nutzungszugriff erforderlich.", "利用時間を表示するには使用状況へのアクセスが必要です。", "O acesso ao uso é necessário para mostrar os minutos.", "Akses penggunaan diperlukan untuk menampilkan menit."),
    "Usage access is off" to ui("Nutzungszugriff ist deaktiviert", "使用状況へのアクセスがオフです", "O acesso ao uso está desativado", "Akses penggunaan nonaktif"),
    "When you open the app for the first time" to ui("Beim ersten Öffnen der App", "初めてアプリを開いたとき", "Ao abrir o app pela primeira vez", "Saat pertama kali membuka aplikasi"),
    "Which test day is this app on today? Choose between 1 and 20." to ui("An welchem Testtag ist diese App heute? Wähle zwischen 1 und 20.", "このアプリは今日テスト何日目ですか？1〜20日から選択してください。", "Em qual dia de teste este app está hoje? Escolha entre 1 e 20.", "Hari pengujian ke berapa untuk aplikasi ini? Pilih antara 1 dan 20."),
    "You can switch between a line chart and a written summary." to ui("Du kannst zwischen Liniendiagramm und Textübersicht wechseln.", "折れ線グラフと文章の概要を切り替えられます。", "Você pode alternar entre gráfico de linhas e resumo escrito.", "Anda dapat beralih antara grafik garis dan ringkasan tertulis.")
)

internal fun extendedUiCopy(language: AppLanguage, english: String): String? {
    val item = extendedUiStrings[english] ?: return null
    return when (language) {
        AppLanguage.DE -> item.de
        AppLanguage.JA -> item.ja
        AppLanguage.PT -> item.pt
        AppLanguage.ID -> item.id
        else -> null
    }
}

internal fun extendedUiEnglishKeys(): Set<String> = extendedUiStrings.keys
