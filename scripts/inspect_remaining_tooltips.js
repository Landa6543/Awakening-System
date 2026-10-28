const fs = require('fs');
const path = require('path');

const filesToInspect = [
  // Group A: Artifacts & Curios
  'src/main/java/com/soybe/awakeningsystem/item/DoctorsRobeItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/RobeOfTheBeautyItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/WildfireCrownItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/TeyvatDelusionItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/TeyvatVisionItem.java',

  // Group B: Consumables & Foods
  'src/main/java/com/soybe/awakeningsystem/item/VigorPotionItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/OliveElixirItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/ConstellationNectarItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/AlarmClockItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/AnkleWeightsItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/BitterJuiceItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/CloverSparkItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/DarkStaminaElixirItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/EliteCaratItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/GachaTicketItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/GoldenGachaTicketItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/GoldHorseshoeItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/PleasingParfaitItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/SilverHorseshoeItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/uma/TrainingWhistleItem.java',

  // Group C: Gacha / Currencies / Resins / Keys
  'src/main/java/com/soybe/awakeningsystem/item/PrimogemItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/GenesisCrystalItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/IntertwinedFateItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/AcquaintFateItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/StellarJadeItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/StarRailSpecialPassItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CosmicFragmentItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CondensedResinItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/OriginalResinItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/TransientResinItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CrownOfInsightItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/HerosWitItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/OculusItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/OculusResonanceStoneItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/TeyvatSigilItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/TeyvatShrineKeyItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/ClandestineVaultKeyItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/ForgeBilletItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/SystemUpgradeSmithingTemplateItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/GalacticBigLottoItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/GachaCapsuleItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CasinoChipItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CasinoTrophyItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CasinoVipPassItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CaratItem.java',

  // Group D: Light Cones
  'src/main/java/com/soybe/awakeningsystem/item/HsrLightConeItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/LightConeAlongThePassingShoreItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/LightConeBeforeDawnItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/LightConeNightOnTheMilkyWayItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/LightConePatienceIsAllYouNeedItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/LightConeWhereDreamsShouldRestItem.java',

  // Group E: Special items
  'src/main/java/com/soybe/awakeningsystem/item/LunarAuthorityItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/TrilunarGuideItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/TrackMarkerWandItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/PrimogemPickaxeItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/VisionsMenuItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CompanionContractItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CommandSpellItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/FableScrollItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/FableStarFragmentItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/PlushieItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/CustomPlayerPlushieItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/PraiseOfLoftyMoralityItem.java',
  'src/main/java/com/soybe/awakeningsystem/item/OmnisciencePouchItem.java'
];

const results = {};
for (const relPath of filesToInspect) {
  if (!fs.existsSync(relPath)) continue;
  const content = fs.readFileSync(relPath, 'utf8');
  const match = content.match(/void appendHoverText[\s\S]*?\n    \}/);
  if (match) {
    results[path.basename(relPath)] = match[0];
  }
}

fs.writeFileSync('scripts/tooltips_dump.json', JSON.stringify(results, null, 2), 'utf8');
console.log('Dumped tooltips of ' + Object.keys(results).length + ' files.');
