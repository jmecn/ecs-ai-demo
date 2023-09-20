package demo.system;

import com.jme3.math.Vector3f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.Constants;
import demo.component.Transform;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
public class CrossBorderSystem extends AbstractGameSystem {

    private final float radius;

    private EntityData ed;
    private EntitySet entities;

    public CrossBorderSystem() {
        this(Constants.MAP_SIZE);
    }

    public CrossBorderSystem(float size) {
        this.radius = size * 0.5f;
    }

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Transform.class);
    }

    public void update(SimTime time) {
        entities.applyChanges();
        for (Entity entity : entities) {
            update(entity);
        }
    }

    private void update(Entity entity) {
        Transform transform = entity.get(Transform.class);
        Vector3f location = transform.getPosition();
        boolean needUpdate = false;
        if (location.x > radius) {
            location.x = -radius;
            needUpdate = true;
        } else if (transform.getPosition().x < -radius) {
            location.x = radius;
            needUpdate = true;
        }

        if (location.z > radius) {
            location.z = -radius;
            needUpdate = true;
        } else if (transform.getPosition().z < -radius) {
            location.z = radius;
            needUpdate = true;
        }

        if (needUpdate) {
            ed.setComponent(entity.getId(),transform);
        }
    }

    @Override
    protected void terminate() {
        entities.clear();
    }
}