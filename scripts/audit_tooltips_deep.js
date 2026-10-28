const fs = require('fs');
const path = require('path');

function walk(dir) {
    let results = [];
    const list = fs.readdirSync(dir);
    list.forEach(file => {
        file = path.join(dir, file);
        const stat = fs.statSync(file);
        if (stat && stat.isDirectory()) results = results.concat(walk(file));
        else if (file.endsWith('.java')) results.push(file);
    });
    return results;
}

const files = walk('src/main/java');
const withLiteral = [];

for (const f of files) {
    const content = fs.readFileSync(f, 'utf8');
    const idx = content.indexOf('void appendHoverText');
    if (idx !== -1) {
        // Find method block
        const methodMatch = content.match(/void appendHoverText[\s\S]*?\n    \}/g);
        if (methodMatch) {
            for (const m of methodMatch) {
                const litMatches = [...m.matchAll(/Component\.literal\("([^"]+)"\)/g)];
                const nonSymbols = litMatches.filter(l => /[a-zA-ZáéíóúñÁÉÍÓÚÑ]/.test(l[1]));
                if (nonSymbols.length > 0) {
                    withLiteral.push({ file: f.replace(/\\/g, '/'), count: nonSymbols.length, samples: nonSymbols.slice(0, 3).map(x => x[1]) });
                }
            }
        }
    }
}

console.log('Total files with text Component.literal in appendHoverText:', withLiteral.length);
withLiteral.forEach(x => console.log(`${x.file} (${x.count}) -> ${x.samples[0]}`));
