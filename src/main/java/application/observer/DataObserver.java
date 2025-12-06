package application.observer;

/**
 * Interfaccia Observer: Definisce il metodo di aggiornamento.
 */
public interface DataObserver {
    /**
     * Chiamato dal Subject per notificare un cambiamento.
     *
     * @param event Può essere un oggetto specifico (es. username) che indica cosa è cambiato.
     */
    void update(Object event);
}