package demo.system;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.common.Decay;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.Constants;
import demo.component.*;
import demo.system.aoi.Aoi;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/17
 */
public class ShotSystem extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet entities;

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Position.class, Follow.class);
    }

    public void update(SimTime time) {
        entities.applyChanges();

        for(Entity e : entities) {
            Follow follow = e.get(Follow.class);
            EntityId target = follow.getTarget();

            Position position = e.get(Position.class);
            Position targetPosition = ed.getComponent(target, Position.class);

            Vector3f loc1 = position.getLocation();
            Vector3f loc2 = targetPosition.getLocation();

            // 计算Facing
            Vector3f dir = loc2.subtract(loc1);
            dir.normalizeLocal();
            ed.setComponents(e.getId(), new Rotation(new Quaternion().lookAt(dir, Vector3f.UNIT_Y)));

            // 计算CoolDown
            CoolDown cd = ed.getComponent(e.getId(), CoolDown.class);
            if (cd == null) {
                cd = new CoolDown(Constants.COOL_DOWN);
                ed.setComponent(e.getId(), cd);
            }
            cd.update(time.getTpf());

            if (cd.isCoolDown()) {
                // 创建子弹
                ed.createEntity();
                ed.setComponents(ed.createEntity(),
                        new Model(Constants.BULLET),
                        new Position(loc1.clone()),
                        new Rotation(new Quaternion().lookAt(dir, Vector3f.UNIT_Y)),
                        new Velocity(dir.mult(Constants.BULLET_SPEED)),
                        new Decay(time.getTime(), time.getFutureTime(Constants.BULLET_LIFE)),
                        new Aoi(Constants.BULLET, Constants.BULLET_AOI_RADIUS));
                cd.reset();
            }
        }
    }

    @Override
    protected void terminate() {
        entities.release();
        entities = null;
    }
}
