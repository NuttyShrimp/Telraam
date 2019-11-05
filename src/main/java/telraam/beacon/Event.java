package telraam.beacon;

public abstract class Event<B> {
    abstract void handle(EventHandler<B> h);

    public static class Data<B> extends Event<B> {
        public B inner;

        public Data(B data) {
            inner = data;
        }

        void handle(EventHandler<B> h) {
            h.data(inner);
        }
    }

    public static class Error<B> extends Event<B> {
        public Exception inner;

        public Error(Exception e) {
            inner = e;
        }

        void handle(EventHandler<B> h) {
            h.error(inner);
        }
    }

    public static class Exit<B> extends Event<B> {
        public Exit() {
        }

        void handle(EventHandler<B> h) {
            h.exit();
        }
    }

    public interface EventHandler<B> {
        void exit();

        void error(Exception e);

        void data(B b);
    }
}
