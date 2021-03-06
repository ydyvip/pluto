package net.wohlfart.pluto.scene;

/**
 * implements how an entity is performing its update
 */
public interface UpdateMethod {

    UpdateMethod NULL = new UpdateMethod() {
        @Override
        public void update(float delta) {
            // do nothing
        }
    };

    void update(float delta);

}
