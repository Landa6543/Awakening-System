const fs = require('fs');

const en = JSON.parse(fs.readFileSync('src/main/resources/assets/awakening_system/lang/en_us.json', 'utf8'));
const ru = JSON.parse(fs.readFileSync('src/main/resources/assets/awakening_system/lang/ru_ru.json', 'utf8'));
const es = JSON.parse(fs.readFileSync('src/main/resources/assets/awakening_system/lang/es_es.json', 'utf8'));

const java = fs.readFileSync('src/main/java/com/soybe/awakeningsystem/artifact/ArtifactSet.java', 'utf8');

// Match enum constants: ID_NAME(\n "id", ...
const setRegex = /([A-Z_]+)\(\s*"([^"]+)",\s*"([^"]+)"/g;
let match;
while ((match = setRegex.exec(java)) !== null) {
  const enumName = match[1];
  const id = match[2];
  const esName = match[3];

  const nameKey = `awakening_system.artifact_set.${id}.name`;
  const b2Key = `awakening_system.artifact_set.${id}.bonus2p`;
  const b4Key = `awakening_system.artifact_set.${id}.bonus4p`;

  const enHas = en[nameKey] && en[b2Key] && en[b4Key];
  const ruHas = ru[nameKey] && ru[b2Key] && ru[b4Key];
  const esHas = es[nameKey] && es[b2Key] && es[b4Key];

  console.log(id, 'EN:', !!enHas, 'RU:', !!ruHas, 'ES:', !!esHas);
}
