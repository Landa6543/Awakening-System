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

const files = getJavaFiles('src/main/java');
const report = [];

for (const file of files) {
  const content = fs.readFileSync(file, 'utf8');
  if (!content.includes('appendHoverText')) continue;

  // Extract appendHoverText method block
  const methodRegex = /void\s+appendHoverText\s*\([^)]*\)\s*\{([\s\S]*?)\n\s{4}\}/g;
  let match;
  while ((match = methodRegex.exec(content)) !== null) {
    const body = match[1];
    // Find all Component.literal("...")
    const literalRegex = /Component\.literal\(\s*"([^"]+)"\s*\)/g;
    let lMatch;
    const literals = [];
    while ((lMatch = literalRegex.exec(body)) !== null) {
      const str = lMatch[1];
      // Check if string contains Spanish characters, Spanish words, or letters (not just empty or symbols like [█░])
      if (/[a-zA-ZáéíóúñÁÉÍÓÚÑ]/.test(str) && !str.startsWith('§8[§') && !str.startsWith('§8──')) {
        literals.push(str);
      }
    }
    if (literals.length > 0) {
      report.push({ file, literals });
    }
  }
}

console.log('Found', report.length, 'files with hardcoded literals in appendHoverText:\n');
report.forEach(r => {
  console.log('=== ' + r.file + ' === (' + r.literals.length + ' literals)');
  r.literals.slice(0, 5).forEach(l => console.log('   ' + l));
  if (r.literals.length > 5) console.log('   ... (' + (r.literals.length - 5) + ' more)');
});
