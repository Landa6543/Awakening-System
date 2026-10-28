const fs = require('fs');

const path = 'src/main/resources/assets/awakening_system/sounds.json';
const sounds = JSON.parse(fs.readFileSync(path, 'utf8'));

const limboKeys = [
    'limbo_entity_ambient',
    'limbo_entity_following',
    'spider_head_ambient',
    'spider_head_following',
    'stilt_walker_ambient',
    'stilt_walker_following',
    'limbo_ophanim_ambient',
    'limbo_ophanim_hurt',
    'limbo_ophanim_death',
    'limbo_ophanim_charge',
    'limbo_ophanim_shoot'
];

limboKeys.forEach(key => {
    if (sounds[key] && sounds[key].sounds) {
        sounds[key].sounds = sounds[key].sounds.map(s => {
            if (typeof s === 'string') {
                return { name: s, attenuation_distance: 64 };
            } else if (typeof s === 'object') {
                s.attenuation_distance = 64;
                return s;
            }
            return s;
        });
        console.log(`Updated attenuation_distance for ${key} to 64`);
    }
});

fs.writeFileSync(path, JSON.stringify(sounds, null, 2), 'utf8');
console.log('Updated sounds.json successfully!');
