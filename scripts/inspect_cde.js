const fs = require('fs');
const dump = JSON.parse(fs.readFileSync('scripts/tooltips_dump.json', 'utf8'));

const groupsCDE = [
  // Group C
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
  'SystemUpgradeSmithingTemplateItem.java',
  'GalacticBigLottoItem.java',
  'GachaCapsuleItem.java',
  'CasinoChipItem.java',
  'CasinoTrophyItem.java',
  'CasinoVipPassItem.java',
  'CaratItem.java',

  // Group D: Light cones
  'HsrLightConeItem.java',
  'LightConeAlongThePassingShoreItem.java',
  'LightConeBeforeDawnItem.java',
  'LightConeNightOnTheMilkyWayItem.java',
  'LightConePatienceIsAllYouNeedItem.java',
  'LightConeWhereDreamsShouldRestItem.java',

  // Group E: Special
  'LunarAuthorityItem.java',
  'TrilunarGuideItem.java',
  'TrackMarkerWandItem.java',
  'PrimogemPickaxeItem.java',
  'VisionsMenuItem.java',
  'CompanionContractItem.java',
  'CommandSpellItem.java',
  'FableScrollItem.java',
  'FableStarFragmentItem.java',
  'PlushieItem.java',
  'CustomPlayerPlushieItem.java',
  'PraiseOfLoftyMoralityItem.java',
  'OmnisciencePouchItem.java'
];

for (const name of groupsCDE) {
  console.log('=== ' + name + ' ===');
  console.log(dump[name] || 'NOT FOUND');
}
