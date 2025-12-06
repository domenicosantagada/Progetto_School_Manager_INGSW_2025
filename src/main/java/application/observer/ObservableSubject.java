package application.observer;


/**
 * Interfaccia Subject: Definisce i metodi per gestire (registrare, de-registrare) gli Observer.
 */
public interface ObservableSubject {

    // metodo per registrare un observer
    void attach(DataObserver observer);

    // metodo per de-registrare un observer
    void detach(DataObserver observer);

    // metodo per notificare tutti gli observer di un cambiamento
    void notifyObservers(Object event);
}