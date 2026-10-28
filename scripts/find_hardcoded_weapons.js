const fs = require('fs');
const path = require('path');

const itemDir = 'src/main/java/com/soybe/awakeningsystem/item';
const weapons = [
  'AdamantiteWarHammerItem.java',
  'AsrahanHolySwordItem.java',
  'AstralGuandaoItem.java',
  'BlackDragonScytheItem.java',
  'CreatorClaymoreItem.java',
  'DarkRepulserItem.java',
  'DawnsBalladItem.java',
  'DreadScytheItem.java',
  'ElucidatorItem.java',
  'FlyingSwordItem.java',
  'GenshinBowItem.java',
  'GenshinWeaponItem.java',
  'GiganticGreatswordItem.java',
  'HeavenShakingSwordItem.java',
  'JadeFeijianItem.java',
  'LaDoncellaSwordItem.java',
  'LambentLightItem.java',
  'PlumBlossomSwordItem.java',
  'PrimogemSwordItem.java',
  'RadiantLightSwordItem.java',
  'ShadowDaggersItem.java',
  'SkyDemonSwordItem.java',
  'SoulSolidSwordItem.java',
  'StaffOfHomaItem.java',
  'TranscendentThornItem.java',
  'UnbreakableFaithItem.java'
];

const hardcoded = [];
for (const w of weapons) {
  const full = path.join(itemDir, w);
  if (!fs.existsSync(full)) continue;
  const content = fs.readFileSync(full, 'utf8');
  if (content.includes('appendHoverText')) {
    const match = content.match(/void\s+appendHoverText\s*\([^)]*\)\s*\{([\s\S]*?)\n\s{4}\}/);
    if (match && match[1].includes('Component.literal(')) {
      hardcoded.push(w);
    }
  }
}
console.log('Weapons with Component.literal in appendHoverText:', hardcoded);
