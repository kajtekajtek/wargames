package wargames.events.publisher;

import wargames.events.*;
import wargames.events.subscribers.*;

public interface Publisher {
    void addSubscriber(Subscriber s);
    void removeSubscriber(Subscriber s);
    void updateSubscribers(Event e);
}
