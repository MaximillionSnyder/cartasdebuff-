package com.maximillionsnyder.cartasdebuff.ui.i18n

import com.maximillionsnyder.cartasdebuff.domain.Textos
import java.util.Locale

/* Textos propios de la interfaz (pestañas, filtros, Ajustes, reportes) en las
   cuatro lenguas del juego. Las etiquetas de las fichas (tipos, rarezas, kinds)
   viven en domain/Etiquetas.kt y se piden con `etiquetaX(clave, idioma)`; aquí
   solo está lo que la app escribe por su cuenta.

   Se escribe como lista de pares, no como mapa: así el test puede ver si dos
   claves se repiten (un mapa se comería la segunda sin avisar). Los huecos de
   formato son siempre numerados (`%1$s`) y el test exige los mismos en las
   cuatro lenguas. */

internal val ENTRADAS_TX_UI: List<Pair<String, Map<String, String>>> = listOf(
    /* Arranque y navegación. */
    "cargando_datos" to mapOf(
        "en" to "Reading the skill, card and scenario tables…",
        "ja" to "スキル・カード・シナリオのデータを読み込み中…",
        "ko" to "스킬, 카드, 시나리오 데이터를 읽는 중…",
        "tw" to "正在讀取技能、卡片與劇本資料…",
    ),
    "tab_skills" to mapOf(
        "en" to "Skills",
        "ja" to "スキル",
        "ko" to "스킬",
        "tw" to "技能",
    ),
    "tab_personajes" to mapOf(
        "en" to "Characters",
        "ja" to "キャラクター",
        "ko" to "캐릭터",
        "tw" to "角色",
    ),
    "tab_apoyos" to mapOf(
        "en" to "Support",
        "ja" to "サポート",
        "ko" to "서포트",
        "tw" to "支援",
    ),
    "tab_ajustes" to mapOf(
        "en" to "Settings",
        "ja" to "設定",
        "ko" to "설정",
        "tw" to "設定",
    ),

    /* Selector de idioma. */
    "cambiar_idioma" to mapOf(
        "en" to "Change language",
        "ja" to "言語を変更",
        "ko" to "언어 변경",
        "tw" to "變更語言",
    ),

    /* Filas de skill, personaje y apoyo. */
    "sin_fuentes" to mapOf(
        "en" to "No sources",
        "ja" to "入手元なし",
        "ko" to "출처 없음",
        "tw" to "沒有來源",
    ),
    "fuente_personajes" to mapOf(
        "en" to "%1\$s cards",
        "ja" to "%1\$sキャラ",
        "ko" to "%1\$s캐릭터",
        "tw" to "%1\$s角色",
    ),
    "fuente_eventos_personaje" to mapOf(
        "en" to "%1\$s char. ev.",
        "ja" to "%1\$sキャライベ",
        "ko" to "%1\$s캐릭터 이벤트",
        "tw" to "%1\$s角色事件",
    ),
    "fuente_hints" to mapOf(
        "en" to "%1\$s hints",
        "ja" to "%1\$sヒント",
        "ko" to "%1\$s 힌트",
        "tw" to "%1\$s提示",
    ),
    "fuente_eventos_apoyo" to mapOf(
        "en" to "%1\$s sup. ev.",
        "ja" to "%1\$sサポイベ",
        "ko" to "%1\$s서포트 이벤트",
        "tw" to "%1\$s支援事件",
    ),
    "fuente_escenarios" to mapOf(
        "en" to "%1\$s scen.",
        "ja" to "%1\$sシナリオ",
        "ko" to "%1\$s시나리오",
        "tw" to "%1\$s劇本",
    ),
    "favorita" to mapOf(
        "en" to "Favorite",
        "ja" to "お気に入り",
        "ko" to "즐겨찾기",
        "tw" to "最愛",
    ),
    "sin_en" to mapOf(
        "en" to "no EN",
        "ja" to "EN未実装",
        "ko" to "EN 미출시",
        "tw" to "EN未推出",
    ),
    "skills_uno" to mapOf(
        "en" to "1 skill",
        "ja" to "スキル1個",
        "ko" to "스킬 1개",
        "tw" to "技能 1 個",
    ),
    "skills_varios" to mapOf(
        "en" to "%1\$s skills",
        "ja" to "スキル%1\$s個",
        "ko" to "스킬 %1\$s개",
        "tw" to "技能 %1\$s 個",
    ),
    "volver" to mapOf(
        "en" to "Back",
        "ja" to "戻る",
        "ko" to "뒤로",
        "tw" to "返回",
    ),
    "sin_resultados" to mapOf(
        "en" to "No results",
        "ja" to "該当なし",
        "ko" to "결과 없음",
        "tw" to "沒有結果",
    ),
    "sin_resultados_detalle" to mapOf(
        "en" to "Try another name or clear the filters.",
        "ja" to "別の名前で試すか、フィルターを解除してください。",
        "ko" to "다른 이름으로 검색하거나 필터를 해제해 보세요.",
        "tw" to "試試其他名稱，或清除篩選條件。",
    ),

    /* Búsqueda y filtros. */
    "borrar_busqueda" to mapOf(
        "en" to "Clear search",
        "ja" to "検索をクリア",
        "ko" to "검색어 지우기",
        "tw" to "清除搜尋",
    ),
    "filtros_titulo" to mapOf(
        "en" to "Filters",
        "ja" to "フィルター",
        "ko" to "필터",
        "tw" to "篩選",
    ),
    "filtro_limpiar" to mapOf(
        "en" to "Clear",
        "ja" to "クリア",
        "ko" to "초기화",
        "tw" to "清除",
    ),
    "filtros_ocultar" to mapOf(
        "en" to "Hide filters",
        "ja" to "フィルターを閉じる",
        "ko" to "필터 숨기기",
        "tw" to "隱藏篩選",
    ),
    "filtros_mostrar" to mapOf(
        "en" to "Show filters",
        "ja" to "フィルターを表示",
        "ko" to "필터 표시",
        "tw" to "顯示篩選",
    ),
    "activos_uno" to mapOf(
        "en" to "1 active",
        "ja" to "1件適用中",
        "ko" to "1개 적용",
        "tw" to "1 項啟用",
    ),
    "activos_varios" to mapOf(
        "en" to "%1\$s active",
        "ja" to "%1\$s件適用中",
        "ko" to "%1\$s개 적용",
        "tw" to "%1\$s 項啟用",
    ),
    "volver_arriba" to mapOf(
        "en" to "Back to top",
        "ja" to "先頭へ戻る",
        "ko" to "맨 위로",
        "tw" to "回到頂端",
    ),

    /* Ajustes: cabecera y pestañas. */
    "ajustes_subtitulo" to mapOf(
        "en" to "Customize the app and review the loaded data.",
        "ja" to "アプリをカスタマイズして、読み込んだデータを確認できます。",
        "ko" to "앱을 설정하고 불러온 데이터를 확인하세요.",
        "tw" to "自訂應用程式並查看已載入的資料。",
    ),
    "ajustes_pestana_preferencias" to mapOf(
        "en" to "Preferences",
        "ja" to "環境設定",
        "ko" to "환경 설정",
        "tw" to "偏好設定",
    ),
    "ajustes_pestana_reportes" to mapOf(
        "en" to "Reports",
        "ja" to "レポート",
        "ko" to "보고서",
        "tw" to "報告",
    ),

    /* Ajustes: tarjetas. */
    "ajustes_apariencia" to mapOf(
        "en" to "Appearance",
        "ja" to "外観",
        "ko" to "화면",
        "tw" to "外觀",
    ),
    "ajustes_inicio" to mapOf(
        "en" to "Startup",
        "ja" to "起動",
        "ko" to "시작",
        "tw" to "啟動",
    ),
    "ajustes_datos" to mapOf(
        "en" to "Data",
        "ja" to "データ",
        "ko" to "데이터",
        "tw" to "資料",
    ),
    "ajustes_acerca" to mapOf(
        "en" to "About",
        "ja" to "アプリ情報",
        "ko" to "앱 정보",
        "tw" to "關於",
    ),
    "ajustes_tema" to mapOf(
        "en" to "Theme",
        "ja" to "テーマ",
        "ko" to "테마",
        "tw" to "主題",
    ),
    "ajustes_listas" to mapOf(
        "en" to "Lists",
        "ja" to "リスト",
        "ko" to "목록",
        "tw" to "清單",
    ),
    "ajustes_listas_desc" to mapOf(
        "en" to "Smaller icons and more rows per screen.",
        "ja" to "アイコンが小さくなり、1画面により多くの行が表示されます。",
        "ko" to "아이콘이 작아져 한 화면에 더 많은 줄이 보입니다.",
        "tw" to "圖示較小，每頁顯示更多列。",
    ),
    "ajustes_idioma_datos" to mapOf(
        "en" to "Data language",
        "ja" to "データの言語",
        "ko" to "데이터 언어",
        "tw" to "資料語言",
    ),
    "ajustes_pantalla_inicial" to mapOf(
        "en" to "Start screen",
        "ja" to "起動画面",
        "ko" to "시작 화면",
        "tw" to "起始畫面",
    ),
    "ajustes_pantalla_inicial_desc" to mapOf(
        "en" to "Applied the next time you open the app.",
        "ja" to "次回アプリ起動時に適用されます。",
        "ko" to "앱을 다시 열 때 적용됩니다.",
        "tw" to "下次開啟應用程式時套用。",
    ),
    "datos_cartas_personaje" to mapOf(
        "en" to "Character cards",
        "ja" to "キャラクターカード",
        "ko" to "캐릭터 카드",
        "tw" to "角色卡片",
    ),
    "datos_cartas_apoyo" to mapOf(
        "en" to "Support cards",
        "ja" to "サポートカード",
        "ko" to "서포트 카드",
        "tw" to "支援卡片",
    ),
    "datos_escenarios" to mapOf(
        "en" to "Scenarios",
        "ja" to "シナリオ",
        "ko" to "시나리오",
        "tw" to "劇本",
    ),
    "ajustes_fuente_datos" to mapOf(
        "en" to "Data source",
        "ja" to "データ提供元",
        "ko" to "데이터 출처",
        "tw" to "資料來源",
    ),
    "ajustes_fuente_datos_desc" to mapOf(
        "en" to "Datamined tables from GameTora",
        "ja" to "GameTora のデータ解析テーブル",
        "ko" to "GameTora 데이터마이닝 테이블",
        "tw" to "GameTora 的資料探勘表格",
    ),
    "ajustes_cache" to mapOf(
        "en" to "Image cache",
        "ja" to "画像キャッシュ",
        "ko" to "이미지 캐시",
        "tw" to "圖片快取",
    ),
    "ajustes_cache_desc" to mapOf(
        "en" to "%1\$s in memory · %2\$s on disk",
        "ja" to "%1\$s メモリ · %2\$s ディスク",
        "ko" to "%1\$s 메모리 · %2\$s 디스크",
        "tw" to "%1\$s 記憶體 · %2\$s 磁碟",
    ),
    "cache_borrada_una" to mapOf(
        "en" to "1 image deleted",
        "ja" to "画像を1枚削除しました",
        "ko" to "이미지 1장 삭제됨",
        "tw" to "已刪除 1 張圖片",
    ),
    "cache_borradas_varias" to mapOf(
        "en" to "%1\$s images deleted",
        "ja" to "画像を%1\$s枚削除しました",
        "ko" to "이미지 %1\$s장 삭제됨",
        "tw" to "已刪除 %1\$s 張圖片",
    ),
    "ajustes_version" to mapOf(
        "en" to "Version",
        "ja" to "バージョン",
        "ko" to "버전",
        "tw" to "版本",
    ),
    "ajustes_compartir_app" to mapOf(
        "en" to "Share app",
        "ja" to "アプリを共有",
        "ko" to "앱 공유",
        "tw" to "分享應用程式",
    ),
    "ajustes_compartir_app_desc" to mapOf(
        "en" to "Send the download link",
        "ja" to "ダウンロードリンクを送る",
        "ko" to "다운로드 링크 보내기",
        "tw" to "傳送下載連結",
    ),
    "ajustes_buscar_actualizaciones" to mapOf(
        "en" to "Check for updates",
        "ja" to "アップデートを確認",
        "ko" to "업데이트 확인",
        "tw" to "檢查更新",
    ),
    "ajustes_buscar_actualizaciones_desc" to mapOf(
        "en" to "Latest version published on GitHub",
        "ja" to "GitHub で公開中の最新バージョン",
        "ko" to "GitHub에 공개된 최신 버전",
        "tw" to "GitHub 上發布的最新版本",
    ),
    "ajustes_restablecer" to mapOf(
        "en" to "Reset settings",
        "ja" to "設定をリセット",
        "ko" to "설정 초기화",
        "tw" to "重設設定",
    ),
    "ajustes_restablecer_desc" to mapOf(
        "en" to "Back to the system theme, English data, comfortable lists and the Skills start screen.",
        "ja" to "テーマはシステム、データは英語、リストはゆったり、起動画面はスキルに戻ります。",
        "ko" to "테마는 시스템, 데이터는 영어, 목록은 여유 있게, 시작 화면은 스킬로 돌아갑니다.",
        "tw" to "回到系統主題、英文資料、舒適清單與技能起始畫面。",
    ),
    "ajustes_restablecer_pregunta" to mapOf(
        "en" to "Reset settings?",
        "ja" to "設定をリセットしますか？",
        "ko" to "설정을 초기화할까요?",
        "tw" to "要重設設定嗎？",
    ),
    "ajustes_restablecer_detalle" to mapOf(
        "en" to "Your theme, language, list and start screen preferences will be deleted.",
        "ja" to "テーマ・言語・リスト・起動画面の設定が削除されます。",
        "ko" to "테마, 언어, 목록, 시작 화면 설정이 삭제됩니다.",
        "tw" to "將刪除你的主題、語言、清單與起始畫面偏好。",
    ),
    "ajustes_restablecer_confirmar" to mapOf(
        "en" to "Reset",
        "ja" to "リセット",
        "ko" to "초기화",
        "tw" to "重設",
    ),
    "ajustes_restablecidos" to mapOf(
        "en" to "Settings reset",
        "ja" to "設定をリセットしました",
        "ko" to "설정을 초기화했습니다",
        "tw" to "設定已重設",
    ),
    "cancelar" to mapOf(
        "en" to "Cancel",
        "ja" to "キャンセル",
        "ko" to "취소",
        "tw" to "取消",
    ),

    /* Ajustes: reportes de crash. */
    "reportes_actualizar" to mapOf(
        "en" to "Refresh",
        "ja" to "更新",
        "ko" to "새로고침",
        "tw" to "重新整理",
    ),
    "reportes_prueba" to mapOf(
        "en" to "Test report",
        "ja" to "テストレポート",
        "ko" to "테스트 보고서",
        "tw" to "測試報告",
    ),
    "reportes_borrar_todos" to mapOf(
        "en" to "Delete all",
        "ja" to "すべて削除",
        "ko" to "모두 삭제",
        "tw" to "全部刪除",
    ),
    "reportes_contador" to mapOf(
        "en" to "%1\$s report(s) saved",
        "ja" to "%1\$s件のレポートを保存",
        "ko" to "%1\$s개 보고서 저장됨",
        "tw" to "已儲存 %1\$s 份報告",
    ),
    "reportes_vacio" to mapOf(
        "en" to "No crashes recorded. If the app closes by itself, the details show up here.",
        "ja" to "クラッシュの記録はありません。アプリが勝手に終了した場合、詳細がここに表示されます。",
        "ko" to "기록된 크래시가 없습니다. 앱이 저절로 종료되면 여기에 상세 내용이 표시됩니다.",
        "tw" to "沒有當機紀錄。若應用程式自行關閉，詳細內容會顯示在這裡。",
    ),
    "reportes_sin_detalle" to mapOf(
        "en" to "No details",
        "ja" to "詳細なし",
        "ko" to "상세 없음",
        "tw" to "沒有詳細資料",
    ),
    "reportes_tocar_detalle" to mapOf(
        "en" to "Tap to see the full details",
        "ja" to "タップで詳細を表示",
        "ko" to "탭하여 전체 내용 보기",
        "tw" to "點一下查看完整內容",
    ),
    "reportes_dialogo_titulo" to mapOf(
        "en" to "Crash %1\$s",
        "ja" to "クラッシュ %1\$s",
        "ko" to "크래시 %1\$s",
        "tw" to "當機 %1\$s",
    ),
    "reportes_portapapeles" to mapOf(
        "en" to "Crash report",
        "ja" to "クラッシュレポート",
        "ko" to "크래시 보고서",
        "tw" to "當機報告",
    ),
    "compartir" to mapOf(
        "en" to "Share",
        "ja" to "共有",
        "ko" to "공유",
        "tw" to "分享",
    ),
    "copiar" to mapOf(
        "en" to "Copy",
        "ja" to "コピー",
        "ko" to "복사",
        "tw" to "複製",
    ),
    "cerrar" to mapOf(
        "en" to "Close",
        "ja" to "閉じる",
        "ko" to "닫기",
        "tw" to "關閉",
    ),
    "enlace_no_abierto" to mapOf(
        "en" to "Could not open the link",
        "ja" to "リンクを開けませんでした",
        "ko" to "링크를 열 수 없습니다",
        "tw" to "無法開啟連結",
    ),
    "compartir_app_texto" to mapOf(
        "en" to "Cartas Debuff, a skill and card viewer for Uma Musume:\n%1\$s",
        "ja" to "Cartas Debuff — ウマ娘のスキル・カードビューア:\n%1\$s",
        "ko" to "Cartas Debuff — 우마무스메 스킬·카드 뷰어:\n%1\$s",
        "tw" to "Cartas Debuff — 賽馬娘技能與卡片檢視器:\n%1\$s",
    ),
    "compartir_reporte_asunto" to mapOf(
        "en" to "Crash report — Cartas Debuff",
        "ja" to "クラッシュレポート — Cartas Debuff",
        "ko" to "크래시 보고서 — Cartas Debuff",
        "tw" to "當機報告 — Cartas Debuff",
    ),
    "compartir_reporte" to mapOf(
        "en" to "Share report",
        "ja" to "レポートを共有",
        "ko" to "보고서 공유",
        "tw" to "分享報告",
    ),

    /* Reporte de crash que la app guarda y el usuario comparte. */
    "crash_cabecera" to mapOf(
        "en" to "Cartas Debuff — crash report",
        "ja" to "Cartas Debuff — クラッシュレポート",
        "ko" to "Cartas Debuff — 크래시 보고서",
        "tw" to "Cartas Debuff — 當機報告",
    ),
    "crash_fecha" to mapOf(
        "en" to "Date: %1\$s",
        "ja" to "日時: %1\$s",
        "ko" to "날짜: %1\$s",
        "tw" to "日期: %1\$s",
    ),
    "crash_version" to mapOf(
        "en" to "Version: %1\$s",
        "ja" to "バージョン: %1\$s",
        "ko" to "버전: %1\$s",
        "tw" to "版本: %1\$s",
    ),
    "crash_android" to mapOf(
        "en" to "Android: %1\$s (SDK %2\$s)",
        "ja" to "Android: %1\$s (SDK %2\$s)",
        "ko" to "Android: %1\$s (SDK %2\$s)",
        "tw" to "Android: %1\$s (SDK %2\$s)",
    ),
    "crash_dispositivo" to mapOf(
        "en" to "Device: %1\$s",
        "ja" to "端末: %1\$s",
        "ko" to "기기: %1\$s",
        "tw" to "裝置: %1\$s",
    ),
    "crash_hilo" to mapOf(
        "en" to "Thread: %1\$s",
        "ja" to "スレッド: %1\$s",
        "ko" to "스레드: %1\$s",
        "tw" to "執行緒: %1\$s",
    ),
    "crash_prueba_mensaje" to mapOf(
        "en" to "Test report generated from Settings",
        "ja" to "設定画面から生成したテストレポート",
        "ko" to "설정에서 생성한 테스트 보고서",
        "tw" to "從設定產生的測試報告",
    ),
)

/* La tabla que consume la app. */
val TX_UI: Textos = Textos(ENTRADAS_TX_UI.toMap())

/* Igual que `t`, pero con el idioma explícito: lo usan los sitios que no están
   dentro de una composición (Toasts, intents y el reporte de crash, que se
   escribe desde el hilo que revienta). */
fun textoUi(clave: String, idioma: String, vararg args: Any): String {
    val texto = TX_UI[clave, idioma]
    return if (args.isEmpty()) texto else String.format(Locale.US, texto, *args)
}
