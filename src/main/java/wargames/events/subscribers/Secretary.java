package wargames.events.subscribers;

import wargames.commands.*;
import wargames.events.*;

public class Secretary implements Subscriber {

    private final String messagePrefix = "Secretary: ";
    private final String messageSuffix = "\n";

    private final String beforeCommandMessage = "%s is about to execute %s";
    private final String afterCommandMessage  = "%s executed %s";

    private final String recruitSoldiersMessage = ": %d soldiers of rank %s";

    @Override
    public void update(Event event) {
        String message = prepareMessage(event);
        printMessageToSystemOut(message);
    }

    private String prepareMessage(Event event) {
        String message = messagePrefix;

        String messageBody;
        if (event instanceof BeforeCommandEvent) {
            messageBody = getMessageBody((BeforeCommandEvent) event);
        } else if (event instanceof AfterCommandEvent) {
            messageBody = getMessageBody((AfterCommandEvent) event);
        } else {
            messageBody = String.format(
                "unknown event type: %s", event.getClass().getSimpleName()
            );
        }
        message += messageBody;

        if (event instanceof CommandEvent) {
            String subMessage;
            subMessage = prepareSubMessage((CommandEvent) event);
            message += subMessage;
        }

        message += messageSuffix;
        
        return message;
    }
    
    private String getMessageBody(BeforeCommandEvent event) {
        String messageBody = String.format(
            beforeCommandMessage, event.getGeneralName(), event.getCommandName()
        );

        return messageBody;
    }

    private String getMessageBody(AfterCommandEvent event) {
         String messageBody = String.format(
            afterCommandMessage, event.getGeneralName(), event.getCommandName()
        );

        return messageBody;
    }

    private String prepareSubMessage(CommandEvent event) {
        String subMessage;
        if (event.getCommand() instanceof RecruitSoldiersCommand) {
            RecruitSoldiersCommand command = (RecruitSoldiersCommand) event.getCommand();
            subMessage = String.format(recruitSoldiersMessage, command.getQuantity(), command.getRank());
        } else {
            subMessage = "";
        }

        return subMessage;
    }

    private void printMessageToSystemOut(String message) {
        System.out.print(message);
    }

}
