/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.silviogames.ld42;

/**
 * Timer class is used for time measurement. it has to be updated every game
 * cycle to work properly
 *
 * @author Silvio
 */
public class Timer
{

    private float finalTime, currentTime, factor = 0, adder = 0;

    public Timer( float timeInSeconds )
    {
        this.finalTime = timeInSeconds;
        this.currentTime = 0;
    }

    public Timer( float timeInSeconds, boolean ready )
    {
        this.finalTime = timeInSeconds;
        this.currentTime = ready ? finalTime : 0;
    }

    public Timer( float limit, float factor )
    {
        this.finalTime = limit;
        this.currentTime = 0;
        this.factor = factor;
    }

    public boolean update( float delta )
    {
        boolean returner = false;
        this.currentTime = this.currentTime + delta;
        if ( currentTime >= finalTime )
        {
            returner = true;
            if ( factor > 0 )
            {
                finalTime *= factor;
            }
            if ( adder > 0 )
            {
                finalTime += adder;
            }
            currentTime = finalTime - currentTime;
        }
        return returner;
    }

    public float get_current_time()
    {
        return currentTime;
    }

    public float getPercent()
    {
        return currentTime / finalTime;
    }

    public float getNotPercent()
    {
        return ( float ) ( finalTime - currentTime ) / finalTime;
    }

    public void reset()
    {
        this.currentTime = 0;
    }

    public void reset( float newTimeInMs )
    {
        this.currentTime = 0;
        this.finalTime = newTimeInMs;
    }

    public void changeFinalTime( float timeInMs )
    {
        this.finalTime = timeInMs;
    }

    public float rest_time()
    {
        return Math.abs( finalTime - currentTime );
    }

    public void ready()
    {
        this.currentTime = finalTime;
    }

    public void setFactor( float factor )
    {
        this.factor = factor;
    }

    public void setAdder( float adder )
    {
        this.adder = adder;
    }

}
