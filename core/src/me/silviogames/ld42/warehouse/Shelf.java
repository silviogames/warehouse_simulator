package me.silviogames.ld42.warehouse;

import me.silviogames.ld42.ID_Type;
import me.silviogames.ld42.Main;

public class Shelf
{

    int tilex, tiley, width, height;
    int id;

    //placed shelf class for collision

    public Shelf( int start_x, int start_y, int width, int height )
    {
        this.id = Main.id_handler.new_ID( ID_Type.SHELF );
        this.tilex = start_x;
        this.tiley = start_y;
        this.width = width;
        this.height = height;
    }

    public boolean contains( int other_tilex, int other_tiley )
    {
        if ( other_tilex >= tilex && other_tilex < tilex + width && other_tiley >= tiley && other_tiley < tiley + height )
        {
            return true;
        } else
        {
            return false;
        }
    }


}
