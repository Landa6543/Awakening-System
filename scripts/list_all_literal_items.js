const fs = require('fs');
const path = require('path');

function getJavaFiles(dir) {
  let results = [];
  const list = fs.readdirSync(dir, { withFileTypes: true });
  for (const item of list) {
    const full = path.join(dir, item.name);
    if (item.isDirectory()) {
      if (item.name !== 'build' && item.name !== '.gradle') results.push(...getJavaFiles(full));
    } else if (item.name.endsWith('.java')) {
      results.push(full);
    }
  }
  return results;
}

const files = getJavaFiles('src/main/java/com/soybe/awakeningsystem');
const itemsWithLiterals = [];

for (const file of files) {
  const content = fs.readFileSync(file, 'utf8');
  if (!content.includes('appendHoverText')) continue;

  const methodRegex = /void\s+appendHoverText\s*\([^)]*\)\s*\{([\s\S]*?)\n\s*(?:public|private|protected|\@|\})/g;
  let match;
  while ((match = methodRegex.exec(content)) !== null) {
    const body = match[1];
    const literalMatches = [...body.matchAll(/Component\.literal\(\s*"([^"\\]*(?:\\.[^"\\]*)*)"\s*\)/g)];
    const textLiterals = literalMatches
      .map(m => m[1])
      .filter(s => /[a-zA-ZáéíóúñÁÉÍÓÚÑ]/.test(s) && !s.startsWith('§8[§') && !s.startsWith('§8──') && s !== ' ');
    if (textLiterals.length > 0) {
      itemsWithLiterals.push({ file: path.relative('.', file), count: textLiterals.length, literals: textLiterals });
    }
  }
}

console.log('Total files with hardcoded literals:', itemsWithLiterals.length);
itemsWithLiterals.forEach((it, i) => console.log(i + 1, it.file, '(' + it.count + ' strings)'));
