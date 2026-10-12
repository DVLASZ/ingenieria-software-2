package co.edu.unicauca.saberpro.state;

/** Aprobada: pregunta validada pedagógicamente y habilitada para simulacros. Es un estado final. */
public class ApprovedState implements IQuestionState {

    @Override
    public String name() {
        return "Aprobada";
    }

    @Override
    public boolean isPublishable() {
        return true;
    }
}
