package wargames.events.subscribers;

import wargames.commands.*;
import wargames.events.*;
import wargames.models.Rank;

public class Secretary implements Subscriber {

    private final String messagePrefix = "Secretary: ";
    private final String messageSuffix = "\n";

    private final String beforeCommandMessage = "%s is about to execute %s";
    private final String afterCommandMessage  = "%s executed %s";
    private final String defaultEventMessage  = "%s event occured";

    private final String recruitSoldiersMessage = ": %d soldiers of rank %s";
    private final String drillSoldiersMessage = ": drilled %d soldiers for %d gold";

    @Override
    public void update(Event event) {
        String message = prepareMessage(event);
        printMessageToSystemOut(message);
    }

    private String prepareMessage(Event event) {
        String message = ""; 

        message += messagePrefix;
        message += prepareMessageBody(event);
        message += messageSuffix;

        return message;
    }

    private String prepareMessageBody(Event event) {
        String messageBody = "";
        
        messageBody += prepareMessageSubject(event);
        messageBody += prepareMessageDetails(event);

        return messageBody;
    }
    
    private String prepareMessageSubject(Event event) {
        String messageSubject;

        if (event instanceof BeforeCommandEvent) {
            BeforeCommandEvent e = (BeforeCommandEvent) event;
            messageSubject = String.format(
                beforeCommandMessage, e.getGeneralName(), e.getCommandName()
            );

        } else if (event instanceof AfterCommandEvent) {
            AfterCommandEvent e = (AfterCommandEvent) event;
            messageSubject = String.format(
                afterCommandMessage, e.getGeneralName(), e.getCommandName()
            );

        } else {
            messageSubject = String.format(
                defaultEventMessage, event.getClass().getSimpleName()
            );
        }

        return messageSubject;
    }

    private String prepareMessageDetails(Event event) {
        String messageDetails = "";

        if (!(event instanceof CommandEvent)) {
            return messageDetails;
        }

        CommandEvent cmdEvent = (CommandEvent) event;
        Command      command  = cmdEvent.getCommand();

        if (command instanceof RecruitSoldiersCommand) {
            RecruitSoldiersCommand rsCommand = (RecruitSoldiersCommand) command;
            int  recruitedQuantity = rsCommand.getQuantity();
            Rank recruitedRank     = rsCommand.getRank();
            messageDetails = String.format(
                recruitSoldiersMessage, recruitedQuantity, recruitedRank
            );

        } else {
            messageDetails = "";
        }

        return messageDetails;
    }

    private void printMessageToSystemOut(String message) {
        System.out.print(message);
    }

}
