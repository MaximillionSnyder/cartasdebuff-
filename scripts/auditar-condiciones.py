#!/usr/bin/env python3
# scripts/auditar-condiciones.py — ver qué condiciones enseña la app en crudo.
#
#   python3 scripts/auditar-condiciones.py
#
# Lee las tablas del propio Condiciones.kt (no una copia) y las aplica a todo
# el dato: dice qué términos salen en crudo, qué valores faltan en las tablas y
# deja una hoja de evidencia (las skills que usan cada clave con su descripción
# oficial) para traducir con prueba. La salida va a $AUDIT_SALIDA (por defecto
# ~/.cache/audit-condiciones). El guardián de que no quede ninguna es
# CondicionesDatosTest, que corre en CI.
"""Auditor de condiciones: extrae las tablas REALES de Condiciones.kt (emparejando
parentesis, no con regex frágiles), las aplica a todo el dato y saca:
  1) que terminos salen en crudo,
  2) que valores de cada clave no estan en las tablas,
  3) hoja de evidencia por clave (skills que la usan + descripcion oficial EN).
"""
import json
import re
import sys
import collections

import os
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
KT = RAIZ / "app/src/main/java/com/maximillionsnyder/cartasdebuff/domain/Condiciones.kt"
DATOS = RAIZ / "data/skills.json"
SALIDA = Path(os.environ.get("AUDIT_SALIDA", Path.home() / ".cache/audit-condiciones"))
SALIDA.mkdir(parents=True, exist_ok=True)
src = open(KT, encoding="utf-8").read()


def bloque_parentesis(texto, k):
    """texto[k] == '('; devuelve el contenido hasta el parentesis que cierra."""
    prof = 0
    for i in range(k, len(texto)):
        if texto[i] == "(":
            prof += 1
        elif texto[i] == ")":
            prof -= 1
            if prof == 0:
                return texto[k + 1:i]
    raise ValueError("parentesis sin cerrar")


def cuerpo(nombre, llamada="mapOf"):
    i = src.index(f"private val {nombre}")
    j = src.index(llamada + "(", i)
    return bloque_parentesis(src, j + len(llamada))


def simples(texto):
    return {k: v for k, v in re.findall(r'"((?:[^"\\]|\\.)*)"\s+to\s+"((?:[^"\\]|\\.)*)"', texto)}


NUMERICAS = set(re.findall(r'"([^"]+)"', cuerpo("NUMERICAS", "setOf")))
ETIQUETAS = simples(cuerpo("ETIQUETAS"))
ETIQUETAS_FASE = simples(cuerpo("ETIQUETAS_FASE"))
FASES = simples(cuerpo("FASES"))

VALORES = {}
_bloque = cuerpo("VALORES")
for m in re.finditer(r'"([^"]+)"\s+to\s+mapOf\(', _bloque):
    clave = m.group(1)
    ini = m.end() - 1  # el '(' de mapOf(
    VALORES[clave] = simples(bloque_parentesis(_bloque, ini))

OPERADORES = ["==", "!=", ">=", "<=", ">", "<"]
simbolo = {"==": "=", "!=": "≠", ">=": "≥", "<=": "≤"}


def leer_termino(t):
    t = t.strip()
    if not t:
        return None
    op = next((o for o in OPERADORES if o in t), None)
    if op is None:
        return (t, False)
    clave, valor = t.split(op, 1)[0].strip(), t.split(op, 1)[1].strip()

    if clave in ETIQUETAS_FASE:
        if valor not in FASES or op not in ("==", "!="):
            return (t, False)
        texto = ETIQUETAS_FASE[clave].replace("%s", FASES[valor])
        return (texto if op == "==" else f"no {texto}", True)
    if clave == "corner_random":
        if not valor.lstrip("-").isdigit() or op != "==":
            return (t, False)
        return (f"en la curva {valor} (cualquiera)", True)
    if clave not in ETIQUETAS:
        return (t, False)
    et = ETIQUETAS[clave]
    if clave == "corner" and op in ("==", "!=") and valor not in ("0", "1"):
        if not valor.lstrip("-").isdigit():
            return (t, False)
        return (f"en la curva {valor}" if op == "==" else f"no en la curva {valor}", True)
    nombre = VALORES.get(clave, {}).get(valor)
    if nombre is not None and op == "==":
        texto = f"{et}: {nombre}"
    elif nombre is not None and op == "!=":
        texto = f"{et}: distinto de {nombre}"
    elif clave in NUMERICAS:
        texto = f"{et} {simbolo.get(op, op)} {valor}"
    elif valor == "1" and op == "==":
        texto = et
    elif valor == "0" and op == "==":
        texto = f"no {et}"
    elif valor == "1" and op == "!=":
        texto = f"no {et}"
    elif valor == "0" and op == "!=":
        texto = et
    else:
        texto = f"{et} {simbolo.get(op, op)} {valor}"
    return (texto, True)


def condiciones_de(s):
    out = []
    for c in s.get("conditions") or []:
        if isinstance(c, dict):
            for campo in ("condition", "precondition"):
                if c.get(campo):
                    out.append((campo, c[campo]))
    for c in (s.get("geneVersion") or {}).get("condition_groups") or []:
        if isinstance(c, dict) and c.get("condition"):
            out.append(("gene", c["condition"]))
    return out


skills = json.load(open(DATOS, encoding="utf-8"))
crudos = collections.Counter()
ejemplo = {}
por_clave = collections.defaultdict(list)
total = 0
usadas = collections.Counter()
valores_vistos = collections.defaultdict(set)

for s in skills:
    for campo, expr in condiciones_de(s):
        for t in re.split(r"[&@]", expr):
            t = t.strip()
            if not t:
                continue
            total += 1
            texto, ok = leer_termino(t)
            op = next((o for o in OPERADORES if o in t), None)
            if op:
                clave, valor = t.split(op, 1)[0].strip(), t.split(op, 1)[1].strip()
                usadas[clave] += 1
                valores_vistos[clave].add((op, valor))
                if not ok:
                    por_clave[clave].append((s, campo, op, valor))
            if not ok:
                crudos[t] += 1
                ejemplo.setdefault(t, s)

informe = []
informe.append(f"Terminos: {total}, claves distintas: {len(usadas)}, EN CRUDO: {sum(crudos.values())} ({len(crudos)} distintos)")
informe.append("")
informe.append("=== SIN TRADUCIR ===")
for (t), n in crudos.most_common():
    s = ejemplo[t]
    informe.append(f"  {n:4d}x  {t:52s} p.ej. {s['name']['en']} ({s['id']})")
informe.append("")
informe.append("=== VALORES QUE NO ESTAN EN LA TABLA (claves con tabla) ===")
for clave in sorted(VALORES):
    vistos = sorted({v for _, v in valores_vistos.get(clave, set())}, key=lambda x: (len(x), x))
    faltan = [v for v in vistos if v not in VALORES[clave]]
    informe.append(f"  {clave:24s} tabla={sorted(VALORES[clave])} datos={vistos}" + (f"   FALTAN={faltan}" if faltan else ""))
informe.append("")
informe.append("=== CLAVES NUMERICAS/ENUM SIN TABLA DE VALORES ===")
for clave in sorted(usadas):
    if clave in VALORES or clave in ETIQUETAS_FASE:
        continue
    vistos = sorted({v for op, v in valores_vistos[clave]})
    ops = sorted({op for op, _ in valores_vistos[clave]})
    if any(op == "==" and not v.isdigit() for op, v in valores_vistos[clave]):
        continue
    informe.append(f"  {clave:34s} ops={ops} valores={vistos}")
informe.append("")
informe.append("=== TABLAS CON ENTRADAS QUE NO SE USAN ===")
for nombre, d in (("ETIQUETAS", ETIQUETAS), ("ETIQUETAS_FASE", ETIQUETAS_FASE)):
    sobra = sorted(k for k in d if k not in usadas)
    informe.append(f"  {nombre}: {sobra or 'ninguna'}")
sobra_num = sorted(k for k in NUMERICAS if k not in usadas)
informe.append(f"  NUMERICAS: {sobra_num or 'ninguna'}")
falta_num = sorted(k for k in usadas if k not in ETIQUETAS and k not in NUMERICAS and k not in ETIQUETAS_FASE)
informe.append(f"  claves del dato que no estan en ninguna tabla: {falta_num}")

open(SALIDA / "informe.txt", "w", encoding="utf-8").write("\n".join(informe) + "\n")
print("\n".join(informe[:4]))

# --- hoja de evidencia: por clave sin traducir, las skills y su descripcion EN ---
ev = []
for clave in sorted(por_clave):
    usos = por_clave[clave]
    ev.append(f"\n### {clave}  ({len(usos)} usos)")
    vistas = set()
    for s, campo, op, valor in usos:
        desc = ((s.get("desc") or {}).get("en") or "").strip().replace("\n", " ")
        if not desc:
            desc = "(sin EN)"
        clave_desc = desc[:150]
        if clave_desc in vistas:
            continue
        vistas.add(clave_desc)
        ev.append(f"  - {s['name']['en']} ({s['id']}) [{campo}] {op}{valor}")
        ev.append(f"      EN: {clave_desc}")
        if len(vistas) >= 3:
            break
open(SALIDA / "evidencia.txt", "w", encoding="utf-8").write("\n".join(ev) + "\n")
print(f"\ninforme: {SALIDA}/informe.txt   evidencia: {SALIDA}/evidencia.txt ({len(por_clave)} claves sin traducir)")
sys.exit(0)
