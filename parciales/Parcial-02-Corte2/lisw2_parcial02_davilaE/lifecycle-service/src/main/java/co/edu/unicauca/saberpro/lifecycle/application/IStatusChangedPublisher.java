package co.edu.unicauca.saberpro.lifecycle.application;

public interface IStatusChangedPublisher {

    void publish(QuestionStatusChanged event);
}
