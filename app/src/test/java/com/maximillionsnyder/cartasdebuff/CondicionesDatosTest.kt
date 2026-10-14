package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.domain.leerCondiciones
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/* Guardián de las condiciones: recorre TODAS las condiciones del asset real y
   comprueba que ninguna se enseña en crudo. Si GameTora añade una clave nueva,
   "npm run fetch" la mete en el dato y este test cae en CI: hay que traducirla
   contrastando la descripción oficial de las skills que la usan, no a ojo.
   (Antes de esto, 265 términos de 87 claves salían tal cual.) */
class CondicionesDatosTest {

    private fun asset(): File = listOf(
        File("src/main/assets/data/skills.json"),
        File("app/src/main/assets/data/skills.json"),
    ).firstOrNull { it.isFile }
        ?: error("no encuentro skills.json (¿desde qué directorio corre el test?)")

    private fun expresionesDe(skill: Map<String, kotlinx.serialization.json.JsonElement>): List<String> {
        val salida = mutableListOf<String>()
        skill["conditions"]?.jsonArray?.forEach { c ->
            c.jsonObject["condition"]?.jsonPrimitive?.contentOrNull?.let { salida.add(it) }
            c.jsonObject["precondition"]?.jsonPrimitive?.contentOrNull?.let { salida.add(it) }
        }
        skill["geneVersion"]?.jsonObject?.get("condition_groups")?.jsonArray?.forEach { c ->
            c.jsonObject["condition"]?.jsonPrimitive?.contentOrNull?.let { salida.add(it) }
        }
        return salida
    }

    @Test
    fun todasLasCondicionesDelDatoSalenTraducidas() {
        val raiz = Json.parseToJsonElement(asset().readText()).jsonArray
        val crudos = linkedMapOf<String, String>()
        var terminos = 0
        for (elemento in raiz) {
            val skill = elemento.jsonObject
            val nombre = skill["name"]?.jsonObject?.get("en")?.jsonPrimitive?.contentOrNull ?: "?"
            for (expresion in expresionesDe(skill)) {
                for (termino in leerCondiciones(expresion)) {
                    terminos++
                    if (!termino.traducido && !crudos.containsKey(termino.texto)) {
                        crudos[termino.texto] = nombre
                    }
                }
            }
        }
        // Si el asset se leyera a medias, el test pasaría por vacío.
        assertTrue("sólo se leyeron $terminos términos: ¿asset incompleto?", terminos > 5000)
        assertTrue(
            "hay ${crudos.size} condiciones que la ficha enseña en crudo:\n" +
                crudos.entries.joinToString("\n") { "  ${it.key}   (p.ej. ${it.value})" },
            crudos.isEmpty(),
        )
    }
}
