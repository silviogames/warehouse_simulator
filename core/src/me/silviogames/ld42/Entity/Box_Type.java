package me.silviogames.ld42.Entity;

import com.badlogic.gdx.math.MathUtils;

public enum Box_Type
{
    SMALL( 5, 1.3f ), MEDIUM( 10, 1.2f ), LARGE( 20, 1.1f );

    public final int weight;
    float speed;

    Box_Type( int weight, float speed )
    {
        this.weight = weight;
        this.speed = speed;
    }

    public static Box_Type random()
    {
        return values( )[ MathUtils.random( 0, 2 ) ];
    }

}
