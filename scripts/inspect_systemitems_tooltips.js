const fs = require('fs');

const content = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/SystemItems.java', 'utf8');
const lines = content.split('\n');

const results = [];
let currentItem = '';

for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const itemMatch = line.match(/public static final DeferredHolder<Item,\s*Item>\s+([A-Z0-9_]+)\s*=/);
    if (itemMatch) {
        currentItem = itemMatch[1];
    }
    if (line.includes('void appendHoverText')) {
        let hoverBlock = [];
        for (let j = i; j < Math.min(lines.length, i + 30); j++) {
            hoverBlock.push(lines[j]);
            if (lines[j].includes('}')) {
                // check if it's the end of appendHoverText
                if (lines[j].trim() === '}' || lines[j].trim() === '});') {
                    break;
                }
            }
        }
        results.push({
            item: currentItem,
            line: i + 1,
            block: hoverBlock.join('\n')
        });
    }
}

console.log('Found ' + results.length + ' appendHoverText blocks in SystemItems.java:');
results.forEach(r => {
    console.log(`=== Item: ${r.item} (line ${r.line}) ===`);
    console.log(r.block);
});
