package me.silviogames.ld42;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;

public class Res
{
    //resource management class
    public static TextureRegion pixel, alphabet, button_background, strip_ground_tiles, strip_boxes, sprite_robot, sprite_human;

    //ui
    public static TextureRegion ui_request;

    public static TextureRegion[] boxes, ground_tiles, robot, human;
    public static int volume = 5;
    public static Music music;
    protected static ObjectMap< Sounds, Sound > soundMap = new ObjectMap< Sounds, Sound >( );
    static TextureAtlas tex;

    public static void load()
    {
        tex = new TextureAtlas( "tex/tex.atlas" );
        alphabet = tex.findRegion( "5x6font" );
        pixel = new TextureRegion( alphabet, 2, 0, 1, 1 );
        strip_boxes = tex.findRegion( "boxes" );

        button_background = tex.findRegion( "button_background" );
        strip_ground_tiles = tex.findRegion( "ground_tiles" );
        sprite_robot = tex.findRegion( "robot" );
        sprite_human = tex.findRegion( "human" );

        //ui
        ui_request = tex.findRegion( "request_ui" );


        boxes = new TextureRegion[ 3 ];
        boxes[ 0 ] = new TextureRegion( strip_boxes, 0, 0, 6, 6 ); //small
        boxes[ 1 ] = new TextureRegion( strip_boxes, 7, 0, 6, 6 ); //medium
        boxes[ 2 ] = new TextureRegion( strip_boxes, 14, 0, 6, 6 ); //large

        robot = new TextureRegion[ 6 ];
        robot[ 0 ] = new TextureRegion( sprite_robot, 0, 0, 10, 10 );
        robot[ 1 ] = new TextureRegion( sprite_robot, 11, 0, 10, 10 );
        robot[ 2 ] = new TextureRegion( sprite_robot, 22, 0, 10, 10 );
        robot[ 3 ] = new TextureRegion( sprite_robot, 33, 0, 10, 10 );
        robot[ 4 ] = new TextureRegion( sprite_robot, 44, 0, 10, 10 );
        robot[ 5 ] = new TextureRegion( sprite_robot, 55, 0, 10, 10 );

        human = new TextureRegion[ 8 ];
        human[ 0 ] = new TextureRegion( sprite_human, 0, 0, 6, 10 );
        human[ 1 ] = new TextureRegion( sprite_human, 7, 0, 6, 10 );
        human[ 2 ] = new TextureRegion( sprite_human, 14, 0, 6, 10 );
        human[ 3 ] = new TextureRegion( sprite_human, 21, 0, 6, 10 );
        human[ 4 ] = new TextureRegion( sprite_human, 28, 0, 6, 10 );
        human[ 5 ] = new TextureRegion( sprite_human, 35, 0, 6, 10 );
        human[ 6 ] = new TextureRegion( sprite_human, 42, 0, 6, 10 );
        human[ 7 ] = new TextureRegion( sprite_human, 49, 0, 6, 10 );

        ground_tiles = new TextureRegion[ 1 ];
        ground_tiles[ 0 ] = new TextureRegion( strip_ground_tiles, 0, 0, 6, 6 );

        fixBleeding( ground_tiles );

        music = Gdx.audio.newMusic( Gdx.files.internal( "sounds/music.mp3" ) );
        music.setLooping( true );
        music.setVolume( volume / 10f );

        Sounds.load( );
    }

    public static void dispose()
    {


        for ( ObjectMap.Entry< Sounds, Sound > e : soundMap )
        {
            if ( e.value != null ) e.value.dispose( );
        }
    }

    private static void fixBleeding( TextureRegion... array )
    {
        for ( TextureRegion tr : array )
        {
            fixBleeding( tr );
        }
    }

    public static void fixBleeding( TextureRegion region )
    {
        float fix = 0.01f;

        float x = region.getRegionX( );
        float y = region.getRegionY( );
        float width = region.getRegionWidth( );
        float height = region.getRegionHeight( );
        float invTexWidth = 1f / region.getTexture( ).getWidth( );
        float invTexHeight = 1f / region.getTexture( ).getHeight( );
        region.setRegion( ( x + fix ) * invTexWidth, ( y + fix ) * invTexHeight, ( x + width - fix ) * invTexWidth, ( y + height - fix ) * invTexHeight ); // Trims
        // region
    }

    public enum Sounds
    {

        WEIRD( true ),
        NEW_BOX( true ),
        CANT_PLACE( false ),
        BOX_OUT( true ),
        SCAN( true );

        private final boolean variation;

        Sounds( boolean variation )
        {
            this.variation = variation;
        }

        public static void load()
        {
            int load = 0;
            for ( Sounds s : values( ) )
            {
                try
                {
                    soundMap.put( s, Gdx.audio.newSound( Gdx.files.internal( "sounds/" + s.toString( ).toLowerCase( ) + ".wav" ) ) );
                    load++;
                } catch ( Exception e )
                {
                    System.out.println( "could not load sound: " + s.toString( ) );
                }
            }
            System.out.println( load + " sounds loaded" );
        }

        public void play( int sound_chx, int sound_chy, int chx, int chy )
        {
            float vol = 5;
            float pan = 0;

            vol /= 5f;
            if ( vol <= 0 ) vol = 0;
            if ( soundMap.get( this ) != null && vol > 0f )
            {
                soundMap.get( this ).play( vol, variation ? MathUtils.random( 0.8f, 1.2f ) : 1, pan );
            }
        }

        public void play()
        {
            if ( soundMap.get( this ) != null )
            {
                soundMap.get( this ).play( ( ( ( float ) volume ) / 10f ), variation ? MathUtils.random( 0.8f, 1.2f ) : 1, 0 );
            }
        }

    }

}
