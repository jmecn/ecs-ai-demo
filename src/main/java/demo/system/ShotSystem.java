package demo.system;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
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
        entities = ed.getEntities(Transform.class, Follow.class, CoolDown.class);
    }

    public void update(SimTime time) {
        entities.applyChanges();

        for(Entity e : entities) {
            Transform transform = e.get(Transform.class);

            Vector3f loc1 = transform.getPosition();
            Quaternion rotation = transform.getRotation();
            Vector3f dir = rotation.mult(Vector3f.UNIT_Z).normalizeLocal();

            // 计算CoolDown
            CoolDown cd = e.get(CoolDown.class);
            cd.update(time.getTpf());

            if (cd.isCoolDown()) {
                // 创建子弹
                ed.createEntity();
                ed.setComponents(ed.createEntity(),
                        new Model(Constants.BULLET),
                        new Transform(loc1.clone(), rotation.clone()),
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
