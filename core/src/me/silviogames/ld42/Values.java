package me.silviogames.ld42;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class Values
{
    //global game values class

    //GAME VALUES
    public static final float diagonal_delta = MathUtils.cosDeg( 45 );
    public static final int in_out_box_length = 4;
    public static final int box_pick_up_distance = 8;
    public static final int box_place_distance = 12;
    public static final int day_length = 30; //in real time seconds; //// TODO: 12.08.2018 CHANGE BEFORE RELEASE!!!
    public static final int warehouse_starting_width = 15;
    public static final int warehouse_starting_height = 12;
    public static final float scan_time = 0.6f;
    public static final int full_box_delivery_money = 100; //actual reward is dependant of delivery time
    public static final float request_time = 35f; //the time you have to send a box out after request was created. after that time you get a penalty for late delivery
    public static final float camera_speed = 150;
    public static final int robot_memory_size = 3; //player should be able to upgrade this
    public static final int new_robot_tilex = 7;
    public static final int new_robot_tiley = 1;
    public static final int robot_prize = 1500;
    public static final int shelf_prive = 5000;
    public static final int price_per_warehouse_tile = 100;
    public static final int memory_price = 1000;
    //colors
    public static final Color color_tile_highlight = from_255( 163, 206, 39, 200 );
    public static final Color color_tile_highlight_invalid = new Color( 0.5f, 0.2f, 0.2f, 0.3f );
    public static final Color color_ui_back = from_255( 48, 52, 164 );
    public static final Color color_ui_front = from_255( 167, 185, 249 );
    public static final Color color_ui_front2 = from_255( 89, 102, 221 );
    public static final Color color_ui_red = from_255( 198, 116, 152 );
    public static final Color color_expand_silu = from_255( 10, 200, 10, 100 );

    public static String start_text = "this is your " + Text.color( Color.GOLD ) + "warehouse. ^ new boxes appear on the " + Text.color( Color.GOLD ) + "inbox. ^ store them on the " + Text.color( Color.GOLD ) + "shelf until they are requested for delivery. ^ " +
            "then you have to find the correct box and put it into the " + Text.color( Color.GOLD ) + "outbox.";


    public static Color from_255( int r, int g, int b, int a )
    {
        return new Color( r / 255f, g / 255f, b / 255f, a / 255f );
    }

    public static Color from_255( int r, int g, int b )
    {
        return new Color( r / 255f, g / 255f, b / 255f, 1 );
    }

}
