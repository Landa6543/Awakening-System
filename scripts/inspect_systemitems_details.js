const fs = require('fs');

const content = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/SystemItems.java', 'utf8');
const lines = content.split('\n');

const items = {};
let curItem = '';

for (let i = 0; i < lines.length; i++) {
    const m = lines[i].match(/public static final DeferredHolder<Item,\s*Item>\s+([A-Z0-9_]+)\s*=/);
    if (m) curItem = m[1];
    if (lines[i].includes('void appendHoverText')) {
        let block = [];
        let j = i;
        while (j < lines.length) {
            block.push(lines[j]);
            if (lines[j].trim() === '}' || lines[j].trim() === '});') {
                break;
            }
            j++;
        }
        items[curItem] = block.join('\n');
    }
}

fs.writeFileSync('scripts/systemitems_hover_dump.json', JSON.stringify(items, null, 2), 'utf8');
console.log('Dumped ' + Object.keys(items).length + ' item tooltips from SystemItems.java');
