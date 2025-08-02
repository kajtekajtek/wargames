package wargames.events.subscribers;

import wargames.commands.*;
import wargames.events.*;
import wargames.models.Rank;

public class Secretary implements Subscriber {

    private final String messagePrefix = "Secretary: ";
    private final String messageSuffix = "\n";

    private final String eventSubjectTemplate     = "%s event occured";
    private final String beforeCmdSubjectTemplate = "%s is about to execute %s";
    private final String afterCmdSubjectTemplate  = "%s executed %s";

    private final String recruitmentDetailsTemplate = ": %d soldiers of rank %s";
    private final String drillDetailsTemplate       = ": %d soldiers for %d gold";
    private final String preAttackDetailsTemplate   = ": attacking %s";
    private final String wonAttackDetailsTemplate   = ": won with %s";
    private final String lostAttackDetailsTemplate  = ": lost with %s";
    private final String drewAttackDetailsTemplate  = ": drew with %s";

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
                beforeCmdSubjectTemplate : afterCmdSubjectTemplate,
                cmdEv.getGeneralName(), 
                cmdEv.getCommandName()
            );

        return eventSubject;
    }

    private String prepareEventSubject(Event ev) {
        String eventSubject;

        eventSubject = String.format(
                eventSubjectTemplate, 
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

        } else if (command instanceof AttackCommand) {
            messageDetails = prepareCommandDetails(
                (AttackCommand) command
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
            recruitmentDetailsTemplate, recruitedQuantity, recruitedRank
        );

        return commandDetails;
    }

    private String prepareCommandDetails(DrillSoldiersCommand cmd) {
        String commandDetails;
        int drilledQuantity = cmd.getQuantity();
        int drillCost       = cmd.getCost();

        commandDetails = String.format(
            drillDetailsTemplate, drilledQuantity, drillCost
        );

        return commandDetails;
    }

    private String prepareCommandDetails(AttackCommand cmd) {
        String commandDetails, detailsTemplate;
        String attackedName = cmd.getAttacked().getName();
        
        if (!cmd.isAttackOver()) {
            detailsTemplate = preAttackDetailsTemplate;    

        } else {
            if (cmd.isDraw()) {
                detailsTemplate = drewAttackDetailsTemplate;

            } else {
                String winnerName = cmd.getWinner().getName();
                detailsTemplate = winnerName == attackedName
                                  ? lostAttackDetailsTemplate
                                  : wonAttackDetailsTemplate;
                }
        }

        commandDetails = String.format(
            detailsTemplate, attackedName
        );

        return commandDetails;
    }

    private void printMessageToSystemOut(String message) {
        System.out.print(message);
    }

}
