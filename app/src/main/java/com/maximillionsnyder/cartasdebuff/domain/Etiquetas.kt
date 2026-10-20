package com.maximillionsnyder.cartasdebuff.domain

/* Tablas de etiquetas de las fichas: los códigos que trae GameTora (tipos de
   skill, de dónde sale cada skill, tipo y obtención de una carta de apoyo) se
   enseñan en el idioma elegido. Viven en domain y no en la UI porque son texto
   puro y así se pueden probar: EtiquetasDatosTest recorre los assets y falla si
   aparece un código nuevo sin traducir en alguno de los cuatro idiomas, igual
   que hace CondicionesDatosTest con las condiciones.

   Las claves son los códigos del dato y no se traducen; los textos están
   contrastados con los assets (descripciones en en/ja/ko/tw), no inventados.
   Ojo con los falsos amigos: "dir" es DIRT (tierra), no arena; las
   descripciones oficiales dicen "if it's a dirt race" / "in Dirt races". Y el
   mismo 1 corta / 2 milla / 3 media / 4 larga de las condiciones. */

/* Los cuatro idiomas de una fila, en el mismo orden que IDIOMAS (en, ja, ko, tw). */
private fun textosEtiqueta(en: String, ja: String, ko: String, tw: String): Map<String, String> =
    mapOf("en" to en, "ja" to ja, "ko" to ko, "tw" to tw)

/* En los datos, 逃げ/先行/差し/追込 son Runner/Leader/Betweener/Chaser en las
   descripciones viejas y Front Runner/Pace Chaser/Late Surger/End Closer en las
   nuevas; se usan los nombres largos, que son los del juego global. El coreano
   de los datos llama 선입 a 差し (no 차입) y el chino 領頭/前列/居中/後追. */
private val TABLA_TIPO: Map<String, Map<String, String>> = mapOf(
    "nac" to textosEtiqueta("No condition", "条件なし", "조건 없음", "無條件"),
    "run" to textosEtiqueta("Front Runner", "逃げ", "도주", "領頭"),
    "ldr" to textosEtiqueta("Pace Chaser", "先行", "선행", "前列"),
    "btw" to textosEtiqueta("Late Surger", "差し", "선입", "居中"),
    "cha" to textosEtiqueta("End Closer", "追込", "추입", "後追"),
    "sho" to textosEtiqueta("Short", "短距離", "단거리", "短距離"),
    "mil" to textosEtiqueta("Mile", "マイル", "마일", "一哩"),
    "med" to textosEtiqueta("Medium", "中距離", "중거리", "中距離"),
    "lng" to textosEtiqueta("Long", "長距離", "장거리", "長距離"),
    "dir" to textosEtiqueta("Dirt", "ダート", "더트", "沙地"),
    "tur" to textosEtiqueta("Turf", "芝", "잔디", "草地"),
    "l_0" to textosEtiqueta("Opening Leg", "序盤", "초반", "前期"),
    "l_1" to textosEtiqueta("Middle Leg", "中盤", "중반", "中期"),
    "l_2" to textosEtiqueta("Final Leg", "終盤", "종반", "後期"),
    "l_3" to textosEtiqueta("Last Spurt", "ラストスパート", "라스트 스퍼트", "最後衝刺"),
    "cor" to textosEtiqueta("Corner", "コーナー", "코너", "彎道"),
    "str" to textosEtiqueta("Straight", "直線", "직선", "直線"),
    "f_c" to textosEtiqueta("Final Corner", "最終コーナー", "최종 코너", "最後彎道"),
    "f_s" to textosEtiqueta("Final Straight", "最終直線", "최종 직선", "最後直線"),
    "slo" to textosEtiqueta("Slope", "坂", "경사", "坡度"),
    "dbf" to textosEtiqueta("Debuff", "デバフ", "디버프", "負面效果"),
)

private val ETIQUETAS_TIPO = Textos(TABLA_TIPO)

fun etiquetaTipo(tipo: String, idioma: String): String = ETIQUETAS_TIPO[tipo, idioma]

/* La rareza viene como número: 1 normal, 2 rara, 3 y 5 únicas, 4 y 6 evoluciones. */
private val TABLA_RAREZA: Map<String, Map<String, String>> = mapOf(
    "1" to textosEtiqueta("Normal", "ノーマル", "노멀", "普通"),
    "2" to textosEtiqueta("Rare", "レア", "레어", "稀有"),
    "3" to textosEtiqueta("Unique", "ユニーク", "유니크", "獨特"),
    "4" to textosEtiqueta("Evolution", "進化", "진화", "進化"),
    "5" to textosEtiqueta("Unique", "ユニーク", "유니크", "獨特"),
    "6" to textosEtiqueta("Evolution", "進化", "진화", "進化"),
)

private val ETIQUETAS_RAREZA = Textos(TABLA_RAREZA)

/* El "?" es el neutro de siempre para una rareza que no llega. */
fun etiquetaRareza(rarity: Int?, idioma: String): String = when (rarity) {
    1 -> ETIQUETAS_RAREZA["1", idioma]
    2 -> ETIQUETAS_RAREZA["2", idioma]
    3 -> ETIQUETAS_RAREZA["3", idioma]
    4 -> ETIQUETAS_RAREZA["4", idioma]
    5 -> ETIQUETAS_RAREZA["5", idioma]
    6 -> ETIQUETAS_RAREZA["6", idioma]
    else -> "?"
}

private val TABLA_KIND: Map<String, Map<String, String>> = mapOf(
    "unique" to textosEtiqueta("Unique", "固有", "고유", "固有"),
    "innate" to textosEtiqueta("Innate", "初期", "초기", "初期"),
    "awakening" to textosEtiqueta("Awakening", "覚醒", "각성", "覺醒"),
    "evolution" to textosEtiqueta("Evolution", "進化", "진화", "進化"),
)

private val ETIQUETAS_KIND = Textos(TABLA_KIND)

fun etiquetaKind(kind: String, idioma: String): String = ETIQUETAS_KIND[kind, idioma]

/* El tipo de una carta de apoyo es el palo de entrenamiento (スピード, スタミナ,
   パワー, 根性, 賢さ, 友人, グループ). En los datos el chino usa 持久力 para el
   aguante y 毅力 para el coraje, y el coreano 지능 para el ingenio. */
private val TABLA_APOYO: Map<String, Map<String, String>> = mapOf(
    "speed" to textosEtiqueta("Speed", "スピード", "스피드", "速度"),
    "stamina" to textosEtiqueta("Stamina", "スタミナ", "스태미나", "持久力"),
    "power" to textosEtiqueta("Power", "パワー", "파워", "力量"),
    "guts" to textosEtiqueta("Guts", "根性", "근성", "毅力"),
    "intelligence" to textosEtiqueta("Wit", "賢さ", "지능", "智力"),
    "friend" to textosEtiqueta("Friend", "友人", "친구", "朋友"),
    "group" to textosEtiqueta("Group", "グループ", "그룹", "團體"),
)

private val ETIQUETAS_APOYO = Textos(TABLA_APOYO)

fun etiquetaTipoApoyo(type: String?, idioma: String): String =
    type?.let { ETIQUETAS_APOYO[it, idioma] } ?: "?"

val TIPOS_APOYO: List<String> =
    listOf("speed", "stamina", "power", "guts", "intelligence", "friend", "group")

private val TABLA_OBTENCION: Map<String, Map<String, String>> = mapOf(
    "gacha" to textosEtiqueta("Gacha", "ガチャ", "가챠", "轉蛋"),
    "main_story" to textosEtiqueta("Main Story", "メインストーリー", "메인 스토리", "主線故事"),
    "story_event" to textosEtiqueta("Story Event", "ストーリーイベント", "스토리 이벤트", "故事活動"),
    "promo" to textosEtiqueta("Promotion", "プロモ", "프로모션", "宣傳"),
    "limited_quest" to textosEtiqueta("Limited Quest", "限定クエスト", "한정 퀘스트", "限定任務"),
    "shop" to textosEtiqueta("Shop", "ショップ", "상점", "商店"),
)

private val ETIQUETAS_OBTENCION = Textos(TABLA_OBTENCION)

fun etiquetaObtencion(obtained: String?, idioma: String): String =
    obtained?.let { ETIQUETAS_OBTENCION[it, idioma] } ?: "?"

/* Todas las filas de las tablas, con un nombre legible delante, para el test que
   exige que cada una esté en los cuatro idiomas (no vale caer al inglés). */
val FILAS_ETIQUETAS: List<Pair<String, Map<String, String>>> =
    TABLA_TIPO.entries.map { it.key to it.value } +
        TABLA_RAREZA.entries.map { "rareza=${it.key}" to it.value } +
        TABLA_KIND.entries.map { it.key to it.value } +
        TABLA_APOYO.entries.map { "apoyo=${it.key}" to it.value } +
        TABLA_OBTENCION.entries.map { "obtencion=${it.key}" to it.value }
