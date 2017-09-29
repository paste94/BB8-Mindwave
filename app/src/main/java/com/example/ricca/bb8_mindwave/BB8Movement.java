package com.example.ricca.bb8_mindwave;

class BB8Movement {
    private static BB8Movement instance = null;
    private double speed;

    static BB8Movement getInstance() {
        if(instance == null){
            instance = new BB8Movement();
        }
        return instance;
    }

    private BB8Movement() {
        speed = 0;
    }

    void setSpeed(double speed){
        this.speed = speed;
    }

    double getSpeed(){
        return this.speed;
    }
}
