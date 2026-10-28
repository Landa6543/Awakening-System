const fs = require('fs');

const en = JSON.parse(fs.readFileSync('src/main/resources/assets/awakening_system/lang/en_us.json', 'utf8'));
const ru = JSON.parse(fs.readFileSync('src/main/resources/assets/awakening_system/lang/ru_ru.json', 'utf8'));
const es = JSON.parse(fs.readFileSync('src/main/resources/assets/awakening_system/lang/es_es.json', 'utf8'));

const testKeys = [
  'item.awakening_system.plum_blossom_sword.lore',
  'item.awakening_system.plum_blossom_sword.origin',
  'item.awakening_system.plum_blossom_sword.passive',
  'item.awakening_system.plum_blossom_sword.active'
];

for (const k of testKeys) {
  console.log(k);
  console.log('  EN:', en[k]);
  console.log('  RU:', ru[k]);
  console.log('  ES:', es[k]);
}
