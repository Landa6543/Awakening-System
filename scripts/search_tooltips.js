const fs = require('fs');
const path = require('path');

function searchInDir(dir, patterns) {
  const files = fs.readdirSync(dir, { withFileTypes: true });
  for (const f of files) {
    const full = path.join(dir, f.name);
    if (f.isDirectory()) {
      if (f.name !== 'build' && f.name !== '.gradle') searchInDir(full, patterns);
    } else if (f.name.endsWith('.java') || f.name.endsWith('.json')) {
      const content = fs.readFileSync(full, 'utf8');
      for (const p of patterns) {
        if (content.includes(p)) {
          console.log(`Found "${p}" in: ${full}`);
        }
      }
    }
  }
}

searchInDir('src/main', [
  'Shikomizue',
  'Final del Gladiador',
  'Especialidad de Mondstadt',
  'Vínculo de Sangre',
  'PROGRESO DEL ARMA',
  'Frío del Inframundo'
]);
