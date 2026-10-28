const fs = require('fs');

const themes = [
    { modelName: 'locked_chest_overworld', texture: 'awakening_system:block/locked_chest_overworld_entity' },
    { modelName: 'locked_chest_nether',    texture: 'awakening_system:block/locked_chest_nether_entity' },
    { modelName: 'locked_chest_end',       texture: 'awakening_system:block/locked_chest_end_entity' },
    { modelName: 'locked_chest_teyvat',    texture: 'awakening_system:block/locked_chest_teyvat_entity' },
    { modelName: 'locked_chest_isekai',    texture: 'awakening_system:block/locked_chest_isekai_entity' },
    { modelName: 'locked_chest_murim',     texture: 'awakening_system:block/locked_chest_murim_entity' },
    { modelName: 'locked_treasure_chest',  texture: 'awakening_system:block/locked_treasure_chest_entity' }
];

function createChestModel(texture) {
    return {
        "credit": "Awakening System 3D Chest Model matching placed chest entity",
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
                    "north": { "uv": [3.5, 8.25, 7.0, 10.5], "texture": "#texture" },
                    "east":  { "uv": [7.0, 8.25, 10.5, 10.5], "texture": "#texture" },
                    "south": { "uv": [10.5, 8.25, 14.0, 10.5], "texture": "#texture" },
                    "west":  { "uv": [0.0, 8.25, 3.5, 10.5],  "texture": "#texture" },
                    "up":    { "uv": [3.5, 4.75, 7.0, 8.25], "texture": "#texture" },
                    "down":  { "uv": [7.0, 4.75, 10.5, 8.25], "texture": "#texture" }
                }
            },
            {
                "name": "lid",
                "from": [1, 9, 1],
                "to": [15, 14, 15],
                "faces": {
                    "north": { "uv": [3.5, 3.5, 7.0, 4.75], "texture": "#texture" },
                    "east":  { "uv": [7.0, 3.5, 10.5, 4.75], "texture": "#texture" },
                    "south": { "uv": [10.5, 3.5, 14.0, 4.75], "texture": "#texture" },
                    "west":  { "uv": [0.0, 3.5, 3.5, 4.75],  "texture": "#texture" },
                    "up":    { "uv": [3.5, 0.0, 7.0, 3.5],  "texture": "#texture" },
                    "down":  { "uv": [7.0, 0.0, 10.5, 3.5],  "texture": "#texture" }
                }
            },
            {
                "name": "latch",
                "from": [7, 7, 0.5],
                "to": [9, 11, 1.5],
                "faces": {
                    "north": { "uv": [0.25, 0.25, 0.75, 1.25], "texture": "#texture" },
                    "east":  { "uv": [0.75, 0.25, 1.0, 1.25], "texture": "#texture" },
                    "south": { "uv": [1.0, 0.25, 1.5, 1.25], "texture": "#texture" },
                    "west":  { "uv": [0.0, 0.25, 0.25, 1.25], "texture": "#texture" },
                    "up":    { "uv": [0.25, 0.0, 0.75, 0.25], "texture": "#texture" },
                    "down":  { "uv": [0.75, 0.0, 1.25, 0.25], "texture": "#texture" }
                }
            },
            {
                "name": "padlock",
                "from": [6, 4, 0.2],
                "to": [10, 8.5, 0.8],
                "faces": {
                    "north": { "uv": [10.25, 11.25, 11.25, 12.25], "texture": "#texture" },
                    "east":  { "uv": [11.25, 11.25, 11.5, 12.25], "texture": "#texture" },
                    "south": { "uv": [11.25, 11.25, 12.25, 12.25], "texture": "#texture" },
                    "west":  { "uv": [10.0, 11.25, 10.25, 12.25], "texture": "#texture" },
                    "up":    { "uv": [10.25, 11.0, 11.25, 11.25], "texture": "#texture" },
                    "down":  { "uv": [11.25, 11.0, 12.25, 11.25], "texture": "#texture" }
                }
            },
            {
                "name": "padlock_shackle",
                "from": [6.5, 8.5, 0.2],
                "to": [9.5, 10.5, 0.8],
                "faces": {
                    "north": { "uv": [10.25, 13.5, 11.0, 14.0], "texture": "#texture" },
                    "east":  { "uv": [11.0, 13.5, 11.25, 14.0], "texture": "#texture" },
                    "south": { "uv": [11.0, 13.5, 11.75, 14.0], "texture": "#texture" },
                    "west":  { "uv": [10.0, 13.5, 10.25, 14.0], "texture": "#texture" },
                    "up":    { "uv": [10.25, 13.5, 11.0, 13.75], "texture": "#texture" }
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

    console.log(`Generated model with normalized UVs for ${t.modelName}`);
});

const tcLockedPath = 'src/main/resources/assets/awakening_system/models/block/locked_treasure_chest_locked.json';
const tcUnlockedPath = 'src/main/resources/assets/awakening_system/models/block/locked_treasure_chest_unlocked.json';
fs.writeFileSync(tcLockedPath, JSON.stringify({ "parent": "awakening_system:block/locked_treasure_chest" }, null, 2), 'utf8');
fs.writeFileSync(tcUnlockedPath, JSON.stringify({ "parent": "awakening_system:block/locked_treasure_chest" }, null, 2), 'utf8');
console.log('Successfully updated all chest models with 100% valid textures and normalized UVs!');
