package me.mykindos.betterpvp.clans.clans.map.renderer;


import com.google.inject.Inject;
import me.mykindos.betterpvp.clans.clans.Clan;
import me.mykindos.betterpvp.clans.clans.ClanManager;
import me.mykindos.betterpvp.clans.clans.map.MapHandler;
import me.mykindos.betterpvp.clans.clans.map.data.ChunkData;
import me.mykindos.betterpvp.clans.clans.map.data.MapSettings;
import net.minecraft.world.level.material.MaterialColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ClanMapRenderer extends MapRenderer {

    private final MapHandler mapHandler;
    private final ClanManager clanManager;

    @Inject
    public ClanMapRenderer(MapHandler mapHandler, ClanManager clanManager) {
        super(true);
        this.mapHandler = mapHandler;
        this.clanManager = clanManager;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (player.getInventory().getItemInMainHand().getType() != Material.FILLED_MAP) return;

        MapSettings mapSettings = mapHandler.mapSettingsMap.get(player.getUniqueId());
        MapSettings.Scale s = mapSettings.getScale();

        final boolean hasMoved = mapHandler.hasMoved(player);
        if (!(hasMoved || mapSettings.isUpdate())) {
            return;
        }

        final MapCursorCollection cursors = mapCanvas.getCursors();
        while (cursors.size() > 0) {
            cursors.removeCursor(cursors.getCursor(0));
        }

        int scale = 1 << s.getValue();

        int centerX = player.getLocation().getBlockX();
        int centerZ = player.getLocation().getBlockZ();

        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                mapCanvas.setPixelColor(i, j, mapCanvas.getBasePixelColor(i, j));
            }
        }

        if (s == MapSettings.Scale.FAR) {
            centerX = 0;
            centerZ = 0;
        }


        for (ChunkData chunkData : mapHandler.clanMapData.get(player.getUniqueId())) {
            if (!chunkData.getWorld().equals(player.getWorld().getName())) {
                continue;
            }
            final Clan clan = clanManager.getObject(chunkData.getClan()).orElse(null);
            if (clan == null) {
                continue;
            }
            int bx = chunkData.getX() << 4; //Chunk's actual world coord;
            int bz = chunkData.getZ() << 4; //Chunk's actual world coord;

            int pX = (bx - centerX) / scale + 64; //Gets the pixel location;
            int pZ = (bz - centerZ) / scale + 64; //Gets the pixel location;

            final boolean admin = clan.isAdmin();

            byte chunkDataColor = chunkData.getColor().getPackedId(MaterialColor.Brightness.NORMAL);

            for (int cx = 0; cx < 16 / scale; cx++) {
                for (int cz = 0; cz < 16 / scale; cz++) {
                    if (pX + cx >= 0 && pX + cx < 128 && pZ + cz >= 0 && pZ + cz < 128) { //Checking if its in the maps bounds;
                        if (s.ordinal() <= MapView.Scale.CLOSE.ordinal() || admin) {

                            mapCanvas.setPixel(pX + cz, pZ + cz,chunkDataColor);
                        }
                        if (s.ordinal() < MapView.Scale.NORMAL.ordinal() || admin) {
                            if (cx == 0) {
                                if (!chunkData.getBlockFaceSet().contains(BlockFace.WEST)) {
                                    mapCanvas.setPixel(pX + cx, pZ + cz, chunkDataColor);
                                }
                            }
                            if (cx == (16 / scale) - 1) {
                                if (!chunkData.getBlockFaceSet().contains(BlockFace.EAST)) {
                                    mapCanvas.setPixel(pX + cx, pZ + cz, chunkDataColor);
                                }
                            }
                            if (cz == 0) {
                                if (!chunkData.getBlockFaceSet().contains(BlockFace.NORTH)) {
                                    mapCanvas.setPixel(pX + cx, pZ + cz, chunkDataColor);
                                }
                            }
                            if (cz == (16 / scale) - 1) {
                                if (!chunkData.getBlockFaceSet().contains(BlockFace.SOUTH)) {
                                    mapCanvas.setPixel(pX + cx, pZ + cz, chunkDataColor);
                                }
                            }
                        } else {
                            mapCanvas.setPixel(pX + cx, pZ + cz, chunkDataColor);
                        }
                    }
                }
            }
        }
        mapHandler.updateLastMoved(player);
        mapSettings.setUpdate(false);
    }
}