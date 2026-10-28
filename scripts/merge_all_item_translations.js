const fs = require('fs');
const path = require('path');

const langDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'awakening_system', 'lang');

const files = [
  'item_translations_weapons.json',
  'item_translations_artifacts.json',
  'item_translations_foods.json',
  'item_translations_pills.json'
];

const merged = { en: {}, es: {}, ru: {} };

for (const f of files) {
  const p = path.join(__dirname, f);
  if (!fs.existsSync(p)) {
    console.error('File not found:', p);
    continue;
  }
  const content = JSON.parse(fs.readFileSync(p, 'utf8'));
  Object.assign(merged.en, content.en);
  Object.assign(merged.es, content.es);
  Object.assign(merged.ru, content.ru);
}

console.log('Total merged keys to add:');
console.log('  EN:', Object.keys(merged.en).length);
console.log('  ES:', Object.keys(merged.es).length);
console.log('  RU:', Object.keys(merged.ru).length);

function updateFile(filename, newKeys) {
  const filePath = path.join(langDir, filename);
  if (!fs.existsSync(filePath)) {
    console.error('File does not exist:', filePath);
    return;
  }
  const raw = fs.readFileSync(filePath, 'utf8');
  const json = JSON.parse(raw);

  let updated = 0;
  for (const [k, v] of Object.entries(newKeys)) {
    if (json[k] !== v) {
      json[k] = v;
      updated++;
    }
  }

  fs.writeFileSync(filePath, JSON.stringify(json, null, 2) + '\n', 'utf8');
  console.log(`Updated ${filename}: ${updated} keys updated/added. Total keys: ${Object.keys(json).length}`);
}

updateFile('en_us.json', merged.en);
updateFile('ru_ru.json', merged.ru);

const esFiles = [
  'es_es.json',
  'es_ar.json',
  'es_cl.json',
  'es_co.json',
  'es_ec.json',
  'es_mx.json',
  'es_pe.json',
  'es_uy.json',
  'es_ve.json'
];

for (const esFile of esFiles) {
  updateFile(esFile, merged.es);
}

console.log('All language files successfully updated with item translations!');
