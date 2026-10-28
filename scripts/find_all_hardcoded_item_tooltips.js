const fs = require('fs');
const path = require('path');

function getJavaFiles(dir) {
  let results = [];
  const list = fs.readdirSync(dir, { withFileTypes: true });
  for (const item of list) {
    const full = path.join(dir, item.name);
    if (item.isDirectory()) {
      if (item.name !== 'build' && item.name !== '.gradle') {
        results = results.concat(getJavaFiles(full));
      }
    } else if (item.name.endsWith('.java')) {
      results.push(full);
    }
  }
  return results;
}

const targetDirs = [
  'src/main/java/com/soybe/awakeningsystem/item',
  'src/main/java/com/soybe/awakeningsystem/artifact',
  'src/main/java/com/soybe/awakeningsystem' // to catch SystemItems.java
];

const allFiles = [];
for (const td of targetDirs) {
  if (fs.existsSync(td)) {
    const stat = fs.statSync(td);
    if (stat.isDirectory()) {
      const jFiles = fs.readdirSync(td)
        .filter(f => f.endsWith('.java'))
        .map(f => path.join(td, f));
      allFiles.push(...jFiles);
    }
  }
}
// Also subdirectories of item (like item/uma, item/pouch)
const itemSubdirs = ['src/main/java/com/soybe/awakeningsystem/item/uma', 'src/main/java/com/soybe/awakeningsystem/item/pouch'];
for (const sd of itemSubdirs) {
  if (fs.existsSync(sd)) {
    const jFiles = fs.readdirSync(sd).filter(f => f.endsWith('.java')).map(f => path.join(sd, f));
    allFiles.push(...jFiles);
  }
}

const results = [];

for (const file of allFiles) {
  const content = fs.readFileSync(file, 'utf8');
  if (!content.includes('appendHoverText')) continue;

  // Find all appendHoverText occurrences (there could be multiple in SystemItems.java)
  const methodRegex = /void\s+appendHoverText\s*\([^)]*\)\s*\{([\s\S]*?)\n\s*(?:public|private|protected|\@|\})/g;
  let match;
  while ((match = methodRegex.exec(content)) !== null) {
    const body = match[1];
    const literalMatches = [...body.matchAll(/Component\.literal\(\s*"([^"]+)"\s*\)/g)];
    const textLiterals = literalMatches
      .map(m => m[1])
      .filter(s => /[a-zA-ZáéíóúñÁÉÍÓÚÑ]/.test(s) && !s.startsWith('§8[§') && !s.startsWith('§8──') && s !== ' ');
    if (textLiterals.length > 0) {
      results.push({ file: path.relative('.', file), count: textLiterals.length, sample: textLiterals.slice(0, 3) });
    }
  }
}

console.log('Total files with hardcoded literals in appendHoverText:', results.length);
results.forEach(r => {
  console.log(`${r.file} (${r.count} literals) -> ${r.sample[0]}`);
});
