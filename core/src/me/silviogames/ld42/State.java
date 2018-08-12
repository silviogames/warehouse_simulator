package me.silviogames.ld42;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;


public abstract class State
{

    public Main main;
    IntIntMap button_layer = new IntIntMap( ); //button id and which layer to draw them to
    private Array< Button > buttons = new Array< Button >( );
    private Array< ScrollingText > scroll = new Array< ScrollingText >( );

    public State( Main main )
    {
        this.main = main;
    }

    public abstract void dispose();

    public void u( float delta )
    {
        for ( Button b : buttons )
        {
            if ( b.update( ( int ) Main.pointer.x, ( int ) Main.pointer.y, Main.mousedown, delta ) )
            {
                Main.mousedown = false;
            }
        }
        for ( ScrollingText sc : scroll )
        {
            sc.update( delta );
        }

        update( delta );
    }

    protected abstract void update( float delta );

    public void r()
    {
        render( );

        for ( Button b : buttons )
        {
            int layer = button_layer.get( b.get_id( ), 0 );
            if ( layer == 0 )
            {
                b.render( Main.batch );
            }
        }

        for ( ScrollingText sc : scroll )
        {
            if ( !sc.hide )
            {
                sc.render( );
            }
        }

    }

    protected abstract void render();

    public void r2()
    {
        render2( );

        for ( Button b : buttons )
        {
            int layer = button_layer.get( b.get_id( ), 0 );
            if ( layer > 0 )
            {
                b.render( Main.batch );
            }
        }

    }

    protected abstract void render2();

    protected void addButtons( Button... b )
    {
        for ( Button be : b )
        {
            if ( be != null )
            {
                addButton( be );
            }
        }
    }

    protected void addScrollText( ScrollingText sc )
    {
        this.scroll.add( sc );
    }

    protected void removeScrollText( ScrollingText sc )
    {
        this.scroll.removeValue( sc, true );
    }

    protected void removeButton( Button b )
    {
        button_layer.remove( b.get_id( ), 0 );
        this.buttons.removeValue( b, true );
    }

    public void panned( float dx, float dy )
    {
        //implement in states
    }

    protected Button backButton()
    {
        Button buttonBack = new Button( 0, 149 - 26, 30, 26, "back", false );
        addButton( buttonBack );
        buttonBack.set_text_size( 1.5f );
        return buttonBack;
    }

    protected void addButton( Button b )
    {
        this.buttons.add( b );
        button_layer.put( b.get_id( ), 0 );
    }

    protected void addButton( Button b, int layer )
    {
        if ( layer < 0 ) layer = 0;
        this.buttons.add( b );
        button_layer.put( b.get_id( ), layer );
    }

    protected Button bottom_back_button()
    {
        Button back = new Button( Main.world_width / 2, 2, 100, 26, "back", true );
        back.set_text_size( 1.3f );
        addButton( back );
        return back;
    }

    protected Button bottom_button( String text )
    {
        Button back = new Button( Main.world_width / 2, 2, 100, 26, text, true );
        back.set_text_size( 1.3f );
        addButton( back );
        return back;
    }

    protected Button bottom_left_button( String text )
    {
        Button back = new Button( 2, 2, 42, 26, text, false );
        back.set_text_size( 1.2f );
        addButton( back );
        return back;
    }

    protected Button bottom_right_button( String text )
    {
        Button bottom_right = new Button( 148, 2, 42, 26, text, false );
        bottom_right.set_text_size( 1.2f );
        addButton( bottom_right );
        return bottom_right;
    }

    protected Button bottom_left_back()
    {
        Button back = new Button( 2, 2, 42, 26, "back", false );
        back.set_text_size( 1.2f );
        addButton( back );
        return back;
    }

}
