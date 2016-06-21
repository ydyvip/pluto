package net.wohlfart.pluto.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.wohlfart.pluto.ai.btree.AbstractBehaviorLeaf;
import net.wohlfart.pluto.ai.btree.ITask;
import net.wohlfart.pluto.ai.btree.ITask.AbstractLeafTask;
import net.wohlfart.pluto.scene.SceneGraph;
import net.wohlfart.pluto.scene.properties.HasRotation;
import net.wohlfart.pluto.stage.loader.EntityElement;
import net.wohlfart.pluto.stage.loader.EntityProperty;
import net.wohlfart.pluto.util.Utils;

/**
 * adds spin behavior to a entity
 */
@EntityElement(type = "Spin")
public class SpinBehavior extends AbstractBehaviorLeaf {

    static final Quaternion TMP_QUATERNION = new Quaternion();

    private Vector3 axis;

    private float angle = Float.NaN;

    private Quaternion spinPerSec;

    @Override
    public ITask createTask(Entity entity, ITask parent) {
        assert !Float.isNaN(angle) : "angle is null";
        assert axis != null : "axis is null";
        return new TaskImpl().initialize(this, entity, parent);
    }

    public SpinBehavior withRotation(Quaternion spinPerSec) {
        this.spinPerSec = spinPerSec;
        angle = this.spinPerSec.nor().getAxisAngle(axis = new Vector3());
        return this;
    }

    @EntityProperty(name = "axis", type = "Vector3")
    public SpinBehavior withAxis(Vector3 axis) {
        this.axis = axis;
        update();
        return this;
    }

    @EntityProperty(name = "angle", type = "Float")
    public SpinBehavior withAngle(float angle) {
        this.angle = angle;
        update();
        return this;
    }

    private void update() {
        if (this.axis != null && !Float.isNaN(this.angle)) {
            spinPerSec = Utils.createQuaternion(axis, angle).nor();
        }
    }

    static class TaskImpl extends AbstractLeafTask {

        private SpinBehavior behavior;

        @Override
        public void tick(float delta, SceneGraph graph) {
            calculate(delta);
            getParent().reportState(State.RUNNING);
        }

        public ITask initialize(SpinBehavior behavior, Entity entity, ITask parent) {
            this.behavior = behavior;
            return super.initialize(entity, parent);
        }

        // calculate and apply the rotation in behavior.spinPerSec to the entity
        private void calculate(float deltaTime) {
            assert behavior.spinPerSec != null : "behavior.spinPerSec must not be null";

            // scale the rotation to deltaTime into tmpQuaternion
            Utils.scale(SpinBehavior.TMP_QUATERNION.set(behavior.spinPerSec), deltaTime);
            behavior.angle = SpinBehavior.TMP_QUATERNION.getAxisAngle(behavior.axis);

            // transform the axis into object space
            getEntity().getComponent(HasRotation.class).getRotation().transform(behavior.axis);
            SpinBehavior.TMP_QUATERNION.set(behavior.axis, behavior.angle);

            // add to the current rotation of the object
            getEntity().getComponent(HasRotation.class).getRotation().mulLeft(SpinBehavior.TMP_QUATERNION);
        }

    }

}
