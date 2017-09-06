package com.example.ricca.bb8_mindwave;

/**
 * Created by ricca on 06/09/17.
 */

class BB8Movement {
    private static BB8Movement instance = null;
    private double speed;
    private float rotation;

    static BB8Movement getInstance() {
        if(instance == null){
            instance = new BB8Movement();
        }
        return instance;
    }

    private BB8Movement() {
        speed = 0;
        rotation = 0;
    }

    public void setSpeed(double speed){
        this.speed = speed;
    }

    public double getSpeed(){
        return this.speed;
    }

    public float getRotation(){
        return this.rotation;
    }
}
