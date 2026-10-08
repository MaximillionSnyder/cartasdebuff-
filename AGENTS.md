# AGENTS.md — cartasdebuff

## Build y releases (GitHub Actions)

No compilar localmente: lo hace `.github/workflows/android.yml`.

- Push a `main` / PR: corre los tests unitarios y compila los APKs (quedan como
  artefactos del run).
- Tag `v*`: además publica la release con `app-release.apk` adjunto.

```bash
git tag -a v0.5 -m "Versión 0.5" && git push origin v0.5
```

## Datos

`npm run fetch` descarga las tablas de GameTora a `data/` y las copia a
`app/src/main/assets/data/` (`npm run assets` solo copia).
