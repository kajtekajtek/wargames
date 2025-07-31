package wargames.events.subscribers;

import wargames.commands.*;
import wargames.events.*;
import wargames.models.Rank;

public class Secretary implements Subscriber {

    private final String messagePrefix = "Secretary: ";
    private final String messageSuffix = "\n";

    private final String beforeCommandSubject = "%s is about to execute %s";
    private final String afterCommandSubject  = "%s executed %s";
    private final String defaultEventSubject  = "%s event occured";

    private final String recruitSoldiersDetails = ": %d soldiers of rank %s";
    private final String drillSoldiersDetails   = ": %d soldiers for %d gold";

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

        if (event instanceof CommandEvent) {
            messageSubject = prepareEventSubject((CommandEvent) event);
        } else {
            messageSubject = prepareEventSubject(event);
        }

        return messageSubject;
    }

    private String prepareEventSubject(CommandEvent cmdEv) {
        String eventSubject;

        eventSubject = String.format(
                cmdEv instanceof BeforeCommandEvent ? 
                beforeCommandSubject : afterCommandSubject,
                cmdEv.getGeneralName(), 
                cmdEv.getCommandName()
            );

        return eventSubject;
    }

    private String prepareEventSubject(Event ev) {
        String eventSubject;

        eventSubject = String.format(
                defaultEventSubject, 
                ev.getClass().getSimpleName()
            );

        return eventSubject;
    }

    private String prepareMessageDetails(Event event) {
        if (!(event instanceof CommandEvent)) {
            return "";
        }

        CommandEvent cmdEvent = (CommandEvent) event;
        Command      command  = cmdEvent.getCommand();

        String messageDetails;
        if (command instanceof RecruitSoldiersCommand) {
            messageDetails = prepareCommandDetails(
                (RecruitSoldiersCommand) command
            );

        } else if (command instanceof DrillSoldiersCommand) {
            messageDetails = prepareCommandDetails(
                (DrillSoldiersCommand) command
            );

        } else {
            messageDetails = "";
        }

        return messageDetails;
    }

    private String prepareCommandDetails(RecruitSoldiersCommand cmd) {
        String commandDetails;
        int  recruitedQuantity = cmd.getQuantity();
        Rank recruitedRank     = cmd.getRank();

        commandDetails = String.format(
            recruitSoldiersDetails, recruitedQuantity, recruitedRank
        );

        return commandDetails;
    }

    private String prepareCommandDetails(DrillSoldiersCommand cmd) {
        String commandDetails;
        int drilledQuantity = cmd.getQuantity();
        int drillCost       = cmd.getCost();

        commandDetails = String.format(
            drillSoldiersDetails, drilledQuantity, drillCost
        );

        return commandDetails;
    }

    private void printMessageToSystemOut(String message) {
        System.out.print(message);
    }

}
