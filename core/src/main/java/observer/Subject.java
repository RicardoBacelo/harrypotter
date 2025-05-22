package observer;

public interface Subject {
    //Objetos que reagem a eventos
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(float playerX, float playerY);
}
