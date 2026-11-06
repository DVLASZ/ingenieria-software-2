package co.edu.unicauca.saberpro.question.infrastructure.jpa.mapper;

import co.edu.unicauca.saberpro.domain.builder.IQuestionBuilder;
import co.edu.unicauca.saberpro.domain.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.record.AnswerOptionRecord;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.record.QuestionRecord;
import org.springframework.stereotype.Component;

/** convierte en las dos direcciones entre el dominio puro y las entidades JPA */
@Component
public class QuestionDomainMapper {

    public QuestionRecord toRecord(Question question) {
        QuestionRecord record = new QuestionRecord(question.getId(), question.getStatement(),
                question.getJustification(), question.getStatus());
        question.getOptions().forEach(o -> record.addOption(new AnswerOptionRecord(o.text(), o.correct())));
        return record;
    }

    public Question toDomain(QuestionRecord record) {
        IQuestionBuilder builder = new QuestionBuilder()
                .id(record.getId())
                .statement(record.getStatement())
                .justification(record.getJustification())
                .status(record.getStatus());
        record.getOptions().forEach(o -> builder.addOption(o.getText(), o.isCorrect()));
        return builder.build();
    }
}
