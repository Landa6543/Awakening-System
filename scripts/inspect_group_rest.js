const fs = require('fs');
const dump = JSON.parse(fs.readFileSync('scripts/tooltips_dump.json', 'utf8'));

const list = [
  'PrimogemItem.java',
  'GenesisCrystalItem.java',
  'IntertwinedFateItem.java',
  'AcquaintFateItem.java',
  'StellarJadeItem.java',
  'StarRailSpecialPassItem.java',
  'CosmicFragmentItem.java',
  'CondensedResinItem.java',
  'OriginalResinItem.java',
  'TransientResinItem.java',
  'CrownOfInsightItem.java',
  'HerosWitItem.java',
  'OculusItem.java',
  'OculusResonanceStoneItem.java',
  'TeyvatSigilItem.java',
  'TeyvatShrineKeyItem.java',
  'ClandestineVaultKeyItem.java',
  'ForgeBilletItem.java',
  'GachaCapsuleItem.java',
  'CasinoChipItem.java',
  'CasinoTrophyItem.java',
  'CasinoVipPassItem.java',
  'CaratItem.java',
  'ElementalGemItem.java',
  'LightConeAlongThePassingShoreItem.java',
  'LightConeBeforeDawnItem.java',
  'LightConeNightOnTheMilkyWayItem.java',
  'LightConePatienceIsAllYouNeedItem.java',
  'LightConeWhereDreamsShouldRestItem.java',
  'HsrLightConeItem.java',
  'LunarAuthorityItem.java',
  'TrilunarGuideItem.java',
  'TrackMarkerWandItem.java',
  'PrimogemPickaxeItem.java',
  'CompanionContractItem.java'
];

for (const f of list) {
    console.log(`=== ${f} ===`);
    console.log(dump[f] || 'NOT FOUND');
}
