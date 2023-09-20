package demo.system;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.es.*;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.component.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
public class ChaseSystem extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet entities;

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Transform.class, Follow.class);
    }

    public void update(SimTime time) {
        entities.applyChanges();

        for(Entity e : entities) {
            Follow follow = e.get(Follow.class);
            EntityId target = follow.getTarget();

            Transform transform = e.get(Transform.class);
            Transform targetPosition = ed.getComponent(target, Transform.class);

            Vector3f loc1 = transform.getPosition();
            Vector3f loc2 = targetPosition.getPosition();

            // 计算Facing
            Vector3f dir = loc2.subtract(loc1);
            dir.normalizeLocal();

            // 计算Transform
            ed.setComponents(e.getId(), new Transform(loc1, new Quaternion().lookAt(dir, Vector3f.UNIT_Y)));
        }
    }

    @Override
    protected void terminate() {
        entities.release();
        entities = null;
    }
}
