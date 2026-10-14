package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.domain.leerCondiciones
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
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

    /* Ojo con JsonNull: `elemento.jsonObject` LANZA si el elemento es un
       JsonNull (y geneVersion es null en 1629 de las 1921 skills), así que
       aquí todo va con `as?` en vez de con accesores que revientan. */
    private fun texto(elemento: JsonObject, clave: String): String? =
        (elemento[clave] as? JsonPrimitive)?.contentOrNull

    private fun expresionesDe(skill: JsonObject): List<String> {
        val salida = mutableListOf<String>()
        (skill["conditions"] as? JsonArray)?.forEach { c ->
            val grupo = c as? JsonObject ?: return@forEach
            texto(grupo, "condition")?.let { salida.add(it) }
            texto(grupo, "precondition")?.let { salida.add(it) }
        }
        val gene = skill["geneVersion"] as? JsonObject
        (gene?.get("condition_groups") as? JsonArray)?.forEach { c ->
            val grupo = c as? JsonObject ?: return@forEach
            texto(grupo, "condition")?.let { salida.add(it) }
        }
        return salida
    }

    @Test
    fun todasLasCondicionesDelDatoSalenTraducidas() {
        val raiz = Json.parseToJsonElement(asset().readText()) as? JsonArray
            ?: error("el asset no es una lista de skills")
        val crudos = linkedMapOf<String, String>()
        var terminos = 0
        for (elemento in raiz) {
            val skill = elemento as? JsonObject ?: continue
            val nombre = (skill["name"] as? JsonObject)?.let { texto(it, "en") } ?: "?"
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
