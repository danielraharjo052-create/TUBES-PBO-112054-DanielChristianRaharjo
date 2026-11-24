package konser.model;

public class DataWrapper<T> {

    private final T data;

    public DataWrapper(T data) {
        this.data = data;
    }

public T getData() {
    return data;
}

}
