package konser.model;

public abstract class Entity implements Identifiable {

    protected int id;

    protected Entity(int id) {
        this.id = id;
    }

@Override
public int getId() {
    return id;
}

}
