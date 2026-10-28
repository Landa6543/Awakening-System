const fs = require('fs');
const p1 = JSON.parse(fs.readFileSync('scripts/bestiary_part1.json', 'utf8'));
const p2 = JSON.parse(fs.readFileSync('scripts/bestiary_part2.json', 'utf8'));
const all = [...p1, ...p2];
const javaText = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/SystemBestiaryScreen.java', 'utf8');

const regex = /entries\.add\(new BestiaryEntry\(\s*"([^"]+)",\s*"([^"]+)"/g;
let match;
let javaEntries = [];
while ((match = regex.exec(javaText)) !== null) {
  javaEntries.push({ name: match[1], category: match[2] });
}
console.log('Java entries count:', javaEntries.length);
console.log('JSON entries count:', all.length);

let ok = true;
for (let i = 0; i < Math.max(javaEntries.length, all.length); i++) {
  const je = javaEntries[i];
  const js = all[i];
  const matchName = je && js && (je.name === js.es.name || je.name === js.en.name);
  if (!matchName) {
    console.log('MISMATCH at', i, 'JSON id:', js ? js.id : 'N/A', 'es name:', js ? js.es.name : 'N/A', '| Java name:', je ? je.name : 'N/A');
    ok = false;
  }
}
if (ok) console.log('ALL 52 ENTRIES MATCH EXACTLY IN ORDER!');
