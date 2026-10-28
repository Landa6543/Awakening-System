const fs = require('fs');

const themes = [
    { modelName: 'locked_chest_overworld', texture: 'awakening_system:entity/chest/locked_chest_overworld' },
    { modelName: 'locked_chest_nether',    texture: 'awakening_system:entity/chest/locked_chest_nether' },
    { modelName: 'locked_chest_end',       texture: 'awakening_system:entity/chest/locked_chest_end' },
    { modelName: 'locked_chest_teyvat',    texture: 'awakening_system:entity/chest/locked_chest_teyvat' },
    { modelName: 'locked_chest_isekai',    texture: 'awakening_system:entity/chest/locked_chest_isekai' },
    { modelName: 'locked_chest_murim',     texture: 'awakening_system:entity/chest/locked_chest_murim' },
    { modelName: 'locked_treasure_chest',  texture: 'awakening_system:entity/chest/locked_treasure_chest' }
];

function createChestModel(texture) {
    return {
        "credit": "Awakening System 3D Chest Model matching placed chest entity",
        "texture_size": [64, 64],
        "textures": {
            "particle": texture,
            "texture": texture
        },
        "display": {
            "gui": {
                "rotation": [30, 225, 0],
                "translation": [0, 0, 0],
                "scale": [0.625, 0.625, 0.625]
            },
            "ground": {
                "rotation": [0, 0, 0],
                "translation": [0, 3, 0],
                "scale": [0.25, 0.25, 0.25]
            },
            "fixed": {
                "rotation": [0, 0, 0],
                "translation": [0, 0, 0],
                "scale": [0.5, 0.5, 0.5]
            },
            "thirdperson_righthand": {
                "rotation": [75, 45, 0],
                "translation": [0, 2.5, 0],
                "scale": [0.375, 0.375, 0.375]
            },
            "firstperson_righthand": {
                "rotation": [0, 45, 0],
                "translation": [0, 0, 0],
                "scale": [0.4, 0.4, 0.4]
            },
            "firstperson_lefthand": {
                "rotation": [0, 225, 0],
                "translation": [0, 0, 0],
                "scale": [0.4, 0.4, 0.4]
            }
        },
        "elements": [
            {
                "name": "bottom",
                "from": [1, 0, 1],
                "to": [15, 9, 15],
                "faces": {
                    "north": { "uv": [14, 33, 28, 42], "texture": "#texture" },
                    "east":  { "uv": [28, 33, 42, 42], "texture": "#texture" },
                    "south": { "uv": [42, 33, 56, 42], "texture": "#texture" },
                    "west":  { "uv": [0, 33, 14, 42],  "texture": "#texture" },
                    "up":    { "uv": [14, 19, 28, 33], "texture": "#texture" },
                    "down":  { "uv": [28, 19, 42, 33], "texture": "#texture" }
                }
            },
            {
                "name": "lid",
                "from": [1, 9, 1],
                "to": [15, 14, 15],
                "faces": {
                    "north": { "uv": [14, 14, 28, 19], "texture": "#texture" },
                    "east":  { "uv": [28, 14, 42, 19], "texture": "#texture" },
                    "south": { "uv": [42, 14, 56, 19], "texture": "#texture" },
                    "west":  { "uv": [0, 14, 14, 19],  "texture": "#texture" },
                    "up":    { "uv": [14, 0, 28, 14],  "texture": "#texture" },
                    "down":  { "uv": [28, 0, 42, 14],  "texture": "#texture" }
                }
            },
            {
                "name": "latch",
                "from": [7, 7, 0.4],
                "to": [9, 11, 1.4],
                "faces": {
                    "north": { "uv": [1, 1, 3, 5], "texture": "#texture" },
                    "east":  { "uv": [3, 1, 4, 5], "texture": "#texture" },
                    "south": { "uv": [4, 1, 6, 5], "texture": "#texture" },
                    "west":  { "uv": [0, 1, 1, 5], "texture": "#texture" },
                    "up":    { "uv": [1, 0, 3, 1], "texture": "#texture" },
                    "down":  { "uv": [3, 0, 5, 1], "texture": "#texture" }
                }
            },
            {
                "name": "padlock",
                "from": [6, 4, 0.2],
                "to": [10, 8.5, 0.8],
                "faces": {
                    "north": { "uv": [41, 45, 45, 49], "texture": "#texture" },
                    "east":  { "uv": [45, 45, 46, 49], "texture": "#texture" },
                    "south": { "uv": [45, 45, 49, 49], "texture": "#texture" },
                    "west":  { "uv": [40, 45, 41, 49], "texture": "#texture" },
                    "up":    { "uv": [41, 44, 45, 45], "texture": "#texture" },
                    "down":  { "uv": [45, 44, 49, 45], "texture": "#texture" }
                }
            },
            {
                "name": "padlock_shackle",
                "from": [6.5, 8.5, 0.2],
                "to": [9.5, 10.5, 0.8],
                "faces": {
                    "north": { "uv": [41, 54, 44, 56], "texture": "#texture" },
                    "east":  { "uv": [44, 54, 45, 56], "texture": "#texture" },
                    "south": { "uv": [44, 54, 47, 56], "texture": "#texture" },
                    "west":  { "uv": [40, 54, 41, 56], "texture": "#texture" },
                    "up":    { "uv": [41, 54, 44, 55], "texture": "#texture" }
                }
            }
        ]
    };
}

themes.forEach(t => {
    const json = createChestModel(t.texture);
    const blockPath = `src/main/resources/assets/awakening_system/models/block/${t.modelName}.json`;
    fs.writeFileSync(blockPath, JSON.stringify(json, null, 2), 'utf8');

    const itemPath = `src/main/resources/assets/awakening_system/models/item/${t.modelName}.json`;
    const itemJson = { "parent": `awakening_system:block/${t.modelName}` };
    fs.writeFileSync(itemPath, JSON.stringify(itemJson, null, 2), 'utf8');

    console.log(`Updated model for ${t.modelName}`);
});

// Also update locked_treasure_chest_locked and unlocked models
const tcLockedPath = 'src/main/resources/assets/awakening_system/models/block/locked_treasure_chest_locked.json';
const tcUnlockedPath = 'src/main/resources/assets/awakening_system/models/block/locked_treasure_chest_unlocked.json';
fs.writeFileSync(tcLockedPath, JSON.stringify({ "parent": "awakening_system:block/locked_treasure_chest" }, null, 2), 'utf8');
fs.writeFileSync(tcUnlockedPath, JSON.stringify({ "parent": "awakening_system:block/locked_treasure_chest" }, null, 2), 'utf8');
console.log('Updated locked_treasure_chest_locked and unlocked aliases.');
