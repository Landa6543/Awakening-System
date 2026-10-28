const fs = require('fs');
const path = require('path');

const itemDir = 'src/main/java/com/soybe/awakeningsystem/item';
const files = fs.readdirSync(itemDir).filter(f => f.endsWith('.java'));

const weapons = [];
const foods = [];
const pills = [];
const otherItems = [];

for (const file of files) {
  const full = path.join(itemDir, file);
  const content = fs.readFileSync(full, 'utf8');

  const isWeapon = content.includes('AwakeningWeaponItem') || content.includes('SwordItem') || content.includes('BowItem') || content.includes('WeaponLevelManager') || file.includes('Sword') || file.includes('Scythe') || file.includes('Claymore') || file.includes('Dagger') || file.includes('Bow');
  const isFood = file.includes('Food') || content.includes('FoodProperties') || file.includes('Fruit') || file.includes('Peach');
  const isPill = file.includes('Pill');

  if (isWeapon) weapons.push(file);
  else if (isFood) foods.push(file);
  else if (isPill) pills.push(file);
  else otherItems.push(file);
}

console.log('Weapons (' + weapons.length + '):', weapons);
console.log('Foods (' + foods.length + '):', foods);
console.log('Pills (' + pills.length + '):', pills);
