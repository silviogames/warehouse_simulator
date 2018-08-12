package me.silviogames.ld42.Entity;

import com.badlogic.gdx.graphics.Color;

public enum Entity_Type
{
    HUMAN( false, 50, 6, 10, Color.OLIVE , false),
    ROBOT( false, 25, 6, 10, Color.FIREBRICK , false),
    BOX( false, 5, 4, 4, Color.GOLD , false);

    public final int walk_speed, debug_art_width, debug_art_height;
    public final Color debug_box_color;
    public final boolean static_object, render_debug_box;

    Entity_Type( boolean static_object, int walk_speed, int daw, int dah, Color c, boolean render_debug_box )
    {
        this.walk_speed = walk_speed;
        this.debug_art_width = daw;
        this.debug_art_height = dah;
        this.debug_box_color = c;
        this.static_object = static_object;
        this.render_debug_box = render_debug_box;
    }

}
