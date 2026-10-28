const fs = require('fs');
const dump = JSON.parse(fs.readFileSync('scripts/tooltips_dump.json', 'utf8'));

const files = process.argv.slice(2);
for (const f of files) {
    console.log(`=== ${f} ===`);
    console.log(dump[f] || 'NOT FOUND');
}
