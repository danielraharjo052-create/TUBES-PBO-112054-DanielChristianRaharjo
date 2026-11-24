package konser.model;

import java.time.LocalDateTime;

public class Concert extends Entity {

    private final String name;

    private final String city;

    private final String venue;

    private final LocalDateTime startTime;

    private final LocalDateTime endTime;

    public Concert(int id, String name, String city, String venue, LocalDateTime startTime, LocalDateTime endTime) {

        super(id);

        this.name = name;

        this.city = city;

        this.venue = venue;

        this.startTime = startTime;

        this.endTime = endTime;

    }

public String getName() {
    return name;
}

public String getCity() {
    return city;
}

public String getVenue() {
    return venue;
}

public LocalDateTime getStartTime() {
    return startTime;
}

public LocalDateTime getEndTime() {
    return endTime;
}

@Override public String toString() {
    return id + " | " + name + " | " + city + " | " + venue + " | " + startTime.toString().replace('T', ' ');
}

}
