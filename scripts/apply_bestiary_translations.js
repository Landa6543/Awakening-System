const fs = require('fs');
const path = require('path');

const langDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'awakening_system', 'lang');

const p1 = JSON.parse(fs.readFileSync(path.join(__dirname, 'bestiary_part1.json'), 'utf8'));
const p2 = JSON.parse(fs.readFileSync(path.join(__dirname, 'bestiary_part2.json'), 'utf8'));
const allEntries = [...p1, ...p2];

const screenExtra = {
  en: {
    "awakening_system.screen.bestiary.projection": "§8[ 3D PROJECTION ]",
    "awakening_system.screen.bestiary.scale": "§e%sx SCALE"
  },
  es: {
    "awakening_system.screen.bestiary.projection": "§8[ PROYECCIÓN 3D ]",
    "awakening_system.screen.bestiary.scale": "§eESCALA %sx"
  },
  ru: {
    "awakening_system.screen.bestiary.projection": "§8[ 3D ПРОЕКЦИЯ ]",
    "awakening_system.screen.bestiary.scale": "§eМАСШТАБ %sx"
  }
};

function buildDict(lang) {
  const dict = {};
  // Extra screen keys
  Object.assign(dict, screenExtra[lang]);

  for (const entry of allEntries) {
    const id = entry.id;
    const ldata = entry[lang];
    dict[`awakening_system.bestiary.${id}.name`] = ldata.name;
    dict[`awakening_system.bestiary.${id}.category`] = ldata.category;
    dict[`awakening_system.bestiary.${id}.title`] = ldata.title;
    for (let i = 0; i < ldata.abilities.length; i++) {
      dict[`awakening_system.bestiary.${id}.ability.${i + 1}`] = ldata.abilities[i];
    }
    dict[`awakening_system.bestiary.${id}.tactics`] = ldata.tactics;
    dict[`awakening_system.bestiary.${id}.drops`] = ldata.drops;
    dict[`awakening_system.bestiary.${id}.lore`] = ldata.lore;
  }
  return dict;
}

const enDict = buildDict('en');
const esDict = buildDict('es');
const ruDict = buildDict('ru');

console.log('Generated keys: EN =', Object.keys(enDict).length, ', ES =', Object.keys(esDict).length, ', RU =', Object.keys(ruDict).length);

function updateFile(filename, newKeys) {
  const filePath = path.join(langDir, filename);
  if (!fs.existsSync(filePath)) {
    console.error('File does not exist:', filePath);
    return;
  }
  const raw = fs.readFileSync(filePath, 'utf8');
  const json = JSON.parse(raw);

  let added = 0;
  for (const [k, v] of Object.entries(newKeys)) {
    if (json[k] !== v) {
      json[k] = v;
      added++;
    }
  }

  // Write with 2 spaces indent and newline
  fs.writeFileSync(filePath, JSON.stringify(json, null, 2) + '\n', 'utf8');
  console.log(`Updated ${filename}: ${added} keys updated/added. Total keys: ${Object.keys(json).length}`);
}

updateFile('en_us.json', enDict);
updateFile('ru_ru.json', ruDict);

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
  updateFile(esFile, esDict);
}

console.log('All language files successfully updated!');
