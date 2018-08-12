package me.silviogames.ld42.warehouse;

import com.badlogic.gdx.graphics.Color;

import me.silviogames.ld42.Values;

public enum Tile_Type
{
    FLOOR( Color.SLATE, true, false, false),
    SHELF( Values.from_255( 108, 69, 39 ), false, true, false ),
    SHELF_WALL( Values.from_255( 92, 47, 32 ), false, false, false ),
    IN_BOX( Color.DARK_GRAY, false, false, false ),
    OUT_BOX( Color.DARK_GRAY, false, true, false );

    public final Color debug_color;
    public final boolean walkable, place_boxes, has_texture;

    Tile_Type( Color c, boolean walkable, boolean place_boxes, boolean has_texture )
    {
        this.debug_color = c;
        this.walkable = walkable;
        this.place_boxes = place_boxes;
        this.has_texture = has_texture;
    }
}
