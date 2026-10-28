const fs = require('fs');
const path = require('path');

const itemDir = 'src/main/java/com/soybe/awakeningsystem/item';
const pills = [
  'BlackTortoisePillItem.java',
  'CrimsonPhoenixPillItem.java',
  'FoundationBuildingPillItem.java',
  'HeavenlyEnlightenmentPillItem.java',
  'MarrowCleansingPillItem.java',
  'NineRevolutionsPillItem.java',
  'PillOfPrimordialRebirthItem.java',
  'RealmBreakthroughPillItem.java',
  'RisingDragonPillItem.java',
  'SoulTransformationPillItem.java',
  'TribulationShieldPillItem.java',
  'WhiteTigerPillItem.java'
];

for (const p of pills) {
  const full = path.join(itemDir, p);
  if (!fs.existsSync(full)) continue;
  const content = fs.readFileSync(full, 'utf8');
  if (content.includes('appendHoverText')) {
    const match = content.match(/void\s+appendHoverText\s*\([^)]*\)\s*\{([\s\S]*?)\n\s{4}\}/);
    if (match) {
      console.log('=== ' + p + ' ===');
      console.log(match[1].trim());
    }
  }
}
