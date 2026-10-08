import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const ORIGEN = path.join(root, 'data');
const DESTINO = path.join(root, 'app', 'src', 'main', 'assets', 'data');

const ARCHIVOS = ['skills.json', 'character-cards.json', 'support-cards.json', 'scenarios.json'];

fs.mkdirSync(DESTINO, { recursive: true });
for (const archivo of ARCHIVOS) {
  const origen = path.join(ORIGEN, archivo);
  if (!fs.existsSync(origen)) {
    console.error(`Falta ${path.relative(root, origen)}. Corré "npm run fetch" primero.`);
    process.exitCode = 1;
    continue;
  }
  fs.copyFileSync(origen, path.join(DESTINO, archivo));
  console.log(`Copiado ${archivo}`);
}
