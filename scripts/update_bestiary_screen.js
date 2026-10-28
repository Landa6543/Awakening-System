const fs = require('fs');
const path = require('path');

const filePath = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'soybe', 'awakeningsystem', 'SystemBestiaryScreen.java');
const backupPath = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'soybe', 'awakeningsystem', 'SystemBestiaryScreen.java.bak');

const p1 = JSON.parse(fs.readFileSync(path.join(__dirname, 'bestiary_part1.json'), 'utf8'));
const p2 = JSON.parse(fs.readFileSync(path.join(__dirname, 'bestiary_part2.json'), 'utf8'));
const allEntries = [...p1, ...p2];

let content = fs.readFileSync(filePath, 'utf8');
fs.writeFileSync(backupPath, content, 'utf8');

// 1. Update BestiaryEntry record
const oldRecordRegex = /public record BestiaryEntry\([\s\S]*?\)\s*\{\}/;
const newRecord = `public record BestiaryEntry(
            String id,
            String defaultName,
            String defaultCategory,
            int categoryColor,
            String defaultTitle,
            LivingEntity entity,
            int scale,
            String hp,
            String dmg,
            String armor,
            String speed,
            List<String> defaultAbilities,
            String defaultTactics,
            String defaultDrops,
            String defaultLore
    ) {
        public String name() {
            String k = "awakening_system.bestiary." + id + ".name";
            return net.minecraft.client.resources.language.I18n.exists(k) ? net.minecraft.client.resources.language.I18n.get(k) : defaultName;
        }

        public String category() {
            String k = "awakening_system.bestiary." + id + ".category";
            return net.minecraft.client.resources.language.I18n.exists(k) ? net.minecraft.client.resources.language.I18n.get(k) : defaultCategory;
        }

        public String title() {
            String k = "awakening_system.bestiary." + id + ".title";
            return net.minecraft.client.resources.language.I18n.exists(k) ? net.minecraft.client.resources.language.I18n.get(k) : defaultTitle;
        }

        public List<String> abilities() {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < defaultAbilities.size(); i++) {
                String k = "awakening_system.bestiary." + id + ".ability." + (i + 1);
                list.add(net.minecraft.client.resources.language.I18n.exists(k) ? net.minecraft.client.resources.language.I18n.get(k) : defaultAbilities.get(i));
            }
            return list;
        }

        public String tactics() {
            String k = "awakening_system.bestiary." + id + ".tactics";
            return net.minecraft.client.resources.language.I18n.exists(k) ? net.minecraft.client.resources.language.I18n.get(k) : defaultTactics;
        }

        public String drops() {
            String k = "awakening_system.bestiary." + id + ".drops";
            return net.minecraft.client.resources.language.I18n.exists(k) ? net.minecraft.client.resources.language.I18n.get(k) : defaultDrops;
        }

        public String lore() {
            String k = "awakening_system.bestiary." + id + ".lore";
            return net.minecraft.client.resources.language.I18n.exists(k) ? net.minecraft.client.resources.language.I18n.get(k) : defaultLore;
        }
    }`;

if (!oldRecordRegex.test(content)) {
  console.error('Failed to match old BestiaryEntry record!');
  process.exit(1);
}
content = content.replace(oldRecordRegex, newRecord);

// 2. Prepend IDs in initEntries
let entryIndex = 0;
content = content.replace(/entries\.add\(new BestiaryEntry\(\s*\r?\n\s*"/g, (match) => {
  if (entryIndex >= allEntries.length) {
    console.error('Found more entries than expected at index', entryIndex);
    return match;
  }
  const id = allEntries[entryIndex].id;
  entryIndex++;
  // match was e.g. "entries.add(new BestiaryEntry(\n                \""
  // we want "entries.add(new BestiaryEntry(\n                \"" + id + "\", \""
  return match.slice(0, -1) + `"${id}", "`;
});
console.log('Replaced entries count:', entryIndex);

// 3. Update renderMonsterList: entry.name -> entry.name(), entry.category -> entry.category()
content = content.replace(
  'g.drawString(font, entry.name, x + 11, ey + 3, isSel ? 0xFFFFFFFF : 0xFFDDECF2, false);',
  'g.drawString(font, entry.name(), x + 11, ey + 3, isSel ? 0xFFFFFFFF : 0xFFDDECF2, false);'
);
content = content.replace(
  'g.drawString(font, "§8[" + entry.category + "] §7" + entry.hp.split(" ")[0] + " HP", x + 11, ey + 13, 0xFF8AAAB8, false);',
  'g.drawString(font, "§8[" + entry.category() + "] §7" + entry.hp.split(" ")[0] + " HP", x + 11, ey + 13, 0xFF8AAAB8, false);'
);

// 4. Update renderHoloPedestal: [ 3D PROJECTION ] and scale
content = content.replace(
  'g.drawCenteredString(font, "§8[ 3D PROJECTION ]", pedCenterX, y + 6, 0xFF5D8496);',
  'g.drawCenteredString(font, Component.translatable("awakening_system.screen.bestiary.projection"), pedCenterX, y + 6, 0xFF5D8496);'
);
content = content.replace(
  'g.drawCenteredString(font, "§e" + cur.scale + "x SCALE", pedCenterX, y + h - 14, 0xFF8AAAB8);',
  'g.drawCenteredString(font, Component.translatable("awakening_system.screen.bestiary.scale", cur.scale), pedCenterX, y + h - 14, 0xFF8AAAB8);'
);

fs.writeFileSync(filePath, content, 'utf8');
console.log('SystemBestiaryScreen.java updated successfully!');
