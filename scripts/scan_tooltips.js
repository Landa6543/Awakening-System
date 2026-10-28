const fs = require('fs');
const path = require('path');

function scanDir(dir) {
    const files = fs.readdirSync(dir);
    for (const f of files) {
        const full = path.join(dir, f);
        if (fs.statSync(full).isDirectory()) {
            scanDir(full);
        } else if (full.endsWith('.java')) {
            const content = fs.readFileSync(full, 'utf8');
            if (content.includes('void appendHoverText')) {
                const hoverMatch = content.match(/void appendHoverText[\s\S]*?\n    \}/);
                if (hoverMatch && hoverMatch[0].includes('Component.literal') && /[áéíóúñ¿¡]|\b(Daño|Habilidad|Efecto|Nivel|Rango|Clic|Espada|Píldora|Arma|Fuerza|Vida|Cura|Pasiva|Activa)\b/i.test(hoverMatch[0])) {
                    console.log(full.replace(/\\/g, '/'));
                }
            }
        }
    }
}
scanDir('src/main/java/com/soybe/awakeningsystem/item');
scanDir('src/main/java/com/soybe/awakeningsystem/artifact');
