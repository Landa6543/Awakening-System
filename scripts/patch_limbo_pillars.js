const fs = require('fs');
const filePath = 'src/main/java/com/soybe/awakeningsystem/limbo/LimboWorldManager.java';
let content = fs.readFileSync(filePath, 'utf8');

// Find start of comment before buildLimboDoorPillars
const marker = 'public static void buildLimboDoorPillars(ServerLevel level, BlockPos doorPos, int index)';
const startIdx = content.lastIndexOf('    /**', content.indexOf(marker));
// Find end of the method
const endIdx = content.indexOf('    /**\n     * Coloca la estructura blanca', startIdx);

const replacementStr = `    private static StructureTemplate pillarDoorTemplate = null;

    private static StructureTemplate getPillarDoorTemplate(ServerLevel level) {
        if (pillarDoorTemplate != null) return pillarDoorTemplate;
        try {
            ResourceLocation templateId = ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "pillar_door");
            StructureTemplateManager stm = level.getStructureManager();
            Optional<StructureTemplate> opt = stm.get(templateId);
            if (opt.isPresent()) {
                pillarDoorTemplate = opt.get();
                return pillarDoorTemplate;
            }
            try (InputStream in = LimboWorldManager.class.getResourceAsStream("/data/awakening_system/structures/pillar_door.nbt")) {
                if (in != null) {
                    CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
                    pillarDoorTemplate = new StructureTemplate();
                    pillarDoorTemplate.load(level.holderLookup(Registries.BLOCK), tag);
                    return pillarDoorTemplate;
                }
            }
        } catch (Exception e) {
            AwakeningSystemMod.LOGGER.error("Error al cargar pillar_door.nbt para las puertas del Limbo: ", e);
        }
        return null;
    }

    /**
     * Construye las dos torres monumentales (Pillar Door) flanqueando la Puerta del Limbo
     * utilizando la estructura auténtica creada en pillar_door.nbt.
     */
    public static void buildLimboDoorPillars(ServerLevel level, BlockPos doorPos, int index) {
        StructureTemplate template = getPillarDoorTemplate(level);
        if (template != null) {
            Vec3i size = template.getSize();
            BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
            Rotation rotation = switch (index) {
                case 0 -> Rotation.NONE;                  // NORTE (-Z)
                case 1 -> Rotation.CLOCKWISE_180;         // SUR (+Z)
                case 2 -> Rotation.CLOCKWISE_90;          // ESTE (+X)
                case 3 -> Rotation.COUNTERCLOCKWISE_90;   // OESTE (-X)
                default -> Rotation.NONE;
            };

            BlockPos origin = doorPos.offset(-pivot.getX(), 0, -pivot.getZ());
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(rotation)
                    .setRotationPivot(pivot)
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(false)
                    .setKnownShape(true);

            template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);
            AwakeningSystemMod.LOGGER.info("Pilares monumentales de la Puerta del Limbo generados en puerta " + index + " (" + doorPos + ")");
        }
    }

\n`;

content = content.slice(0, startIdx) + replacementStr + content.slice(endIdx);
fs.writeFileSync(filePath, content, 'utf8');
console.log('Successfully updated LimboWorldManager.java with pillar_door.nbt!');
