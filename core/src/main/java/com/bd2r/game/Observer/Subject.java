package com.bd2r.game.Observer;

public interface Subject {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(float playerX, float playerY);
}
