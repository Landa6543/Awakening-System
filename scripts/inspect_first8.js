const fs = require('fs');
const dump = JSON.parse(fs.readFileSync('scripts/tooltips_dump.json', 'utf8'));

const first8 = [
  'DoctorsRobeItem.java',
  'RobeOfTheBeautyItem.java',
  'WildfireCrownItem.java',
  'TeyvatDelusionItem.java',
  'TeyvatVisionItem.java',
  'VigorPotionItem.java',
  'OliveElixirItem.java',
  'ConstellationNectarItem.java'
];

for (const name of first8) {
  console.log('=== ' + name + ' ===');
  console.log(dump[name] || 'NOT FOUND');
}
