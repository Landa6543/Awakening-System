const fs = require('fs');
const dump = JSON.parse(fs.readFileSync('scripts/tooltips_dump.json', 'utf8'));

const groupAB = [
  'DoctorsRobeItem.java',
  'RobeOfTheBeautyItem.java',
  'WildfireCrownItem.java',
  'TeyvatDelusionItem.java',
  'TeyvatVisionItem.java',
  'VigorPotionItem.java',
  'OliveElixirItem.java',
  'ConstellationNectarItem.java',
  'AlarmClockItem.java',
  'AnkleWeightsItem.java',
  'BitterJuiceItem.java',
  'CloverSparkItem.java',
  'DarkStaminaElixirItem.java',
  'EliteCaratItem.java',
  'GachaTicketItem.java',
  'GoldenGachaTicketItem.java',
  'GoldHorseshoeItem.java',
  'PleasingParfaitItem.java',
  'SilverHorseshoeItem.java',
  'TrainingWhistleItem.java'
];

for (const name of groupAB) {
  console.log('=== ' + name + ' ===');
  console.log(dump[name] || 'NOT FOUND');
}
