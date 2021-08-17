package com.mslinksya.pets.io.data;

import com.mslinksya.pets.io.data.model.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventRepository {
    private static HashMap<String, Event> eventList = new HashMap<>();

    public static List<Event> getEvents() {
        ArrayList<Event> result = new ArrayList<>();
        for (String eventID : eventList.keySet()) {
            result.add(eventList.get(eventID));
        }
        return result;
    }

    public static void updateEvents(List<Event> eventList) {
        EventRepository.eventList.clear();
        for (Event e : eventList) {
            EventRepository.eventList.put(e.getID(), e);
        }
    }

    public static void deleteEvent(String eventID) {
        eventList.remove(eventID);
    }

    public static void editPet(String eventID, String petID) {
        eventList.get(eventID).setDetectedPet(petID);
    }

    public static Event getEvent(String eventID) {
        return eventList.get(eventID);
    }
}
