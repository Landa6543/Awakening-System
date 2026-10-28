const fs = require('fs');
const path = require('path');

const itemDir = 'src/main/java/com/soybe/awakeningsystem/item';
const foods = [
  'CelestialPineNutItem.java',
  'EldoriaFruitItem.java',
  'HeavenlyLongevityPeachItem.java',
  'ManaFoodItem.java',
  'MandrakeRootItem.java',
  'SnowLotusItem.java',
  'SpiritPlumFruitItem.java',
  'TeyvatFishItem.java',
  'TeyvatFoodItem.java',
  'ThousandYearGinsengItem.java',
  'YggdrasilHeartSeedItem.java'
];

const hardcoded = [];
for (const f of foods) {
  const full = path.join(itemDir, f);
  if (!fs.existsSync(full)) continue;
  const content = fs.readFileSync(full, 'utf8');
  if (content.includes('appendHoverText')) {
    const match = content.match(/void\s+appendHoverText\s*\([^)]*\)\s*\{([\s\S]*?)\n\s{4}\}/);
    if (match && match[1].includes('Component.literal(')) {
      hardcoded.push(f);
      console.log('=== ' + f + ' ===');
      console.log(match[1].trim());
    } else {
      console.log('=== ' + f + ' (translatable or other) ===');
    }
  } else {
    console.log('=== ' + f + ' (NO appendHoverText) ===');
  }
}
console.log('Foods with Component.literal:', hardcoded);
