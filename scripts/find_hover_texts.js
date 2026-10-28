const fs = require('fs');
const path = require('path');

function findHoverTexts(dir) {
  const files = fs.readdirSync(dir, { withFileTypes: true });
  for (const f of files) {
    const full = path.join(dir, f.name);
    if (f.isDirectory()) {
      if (f.name !== 'build' && f.name !== '.gradle') findHoverTexts(full);
    } else if (f.name.endsWith('.java')) {
      const content = fs.readFileSync(full, 'utf8');
      if (content.includes('appendHoverText') || content.includes('ItemTooltipEvent')) {
        console.log(full);
      }
    }
  }
}

findHoverTexts('src/main/java');
