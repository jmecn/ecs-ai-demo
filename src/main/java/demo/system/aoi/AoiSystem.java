package demo.system.aoi;

import com.google.common.base.Stopwatch;
import com.jme3.math.Vector3f;
import com.simsilica.es.*;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import demo.Constants;
import demo.component.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/15
 */
@Slf4j
public class AoiSystem extends AbstractGameSystem {

    private final int cellSize;

    private final Map<Long, Area> areas = new HashMap<>();

    private final Map<EntityId, Aoi> entityAoiMap = new HashMap<>();

    private int cacheMiss = 0;
    private int cacheHit = 0;
    private final Map<String, Set<Entity>> cachedNearByEntities = new HashMap<>();

    private EntityData ed;
    private EntitySet entities;

    public AoiSystem() {
        this(Constants.CELL_SIZE);
    }

    public AoiSystem(int cellSize) {
        this.cellSize = cellSize;
    }

    @Override
    protected void initialize() {
        ed = getSystem(EntityData.class);
        entities = ed.getEntities(Transform.class, Aoi.class);

        for (Entity entity : entities) {
            updateAoi(entity);
        }
    }

    public void update(SimTime time) {

        entities.applyChanges();
        for (Entity entity : entities.getAddedEntities()) {
            updateAoi(entity);
        }
        for (Entity entity : entities.getChangedEntities()) {
            updateAoi(entity);
        }
        for (Entity entity : entities.getRemovedEntities()) {
            leave(entity);
        }

        // 清空缓存
        invalidateNearByCache();
    }

    private void updateAoi(Entity entity) {
        Transform transform = entity.get(Transform.class);
        Aoi aoi = entity.get(Aoi.class);

        // 计算当前位置所在的区域
        int x = (int) (transform.getPosition().x / cellSize);
        int y = (int) (transform.getPosition().y / cellSize);
        int z = (int) (transform.getPosition().z / cellSize);
        long key = Area.toLong(x, y, z);

        Long curAreaId = aoi.getAreaId();
        if (curAreaId != null && curAreaId == key) {
            // 没有变化
            return;
        }

        // 离开旧区域
        if (curAreaId != null) {
            leave(entity);
        }

        // 进入新区域
        Area area = areas.get(key);
        if (area == null) {
            if (log.isDebugEnabled()) {
                log.debug("create area:({}, {})", x, z);
            }
            area = new Area(key, x, y, z);
            areas.put(key, area);
        }

        area.enter(entity, aoi.getGroup());

        // 更新 Aoi 组件
        aoi.setArea(key, x, y, z);
        ed.setComponent(entity.getId(), aoi);

        entityAoiMap.put(entity.getId(), aoi);

        if (log.isDebugEnabled()) {
            log.debug("enter:{}, ({}, {})", entity.getId(), aoi.getX(), aoi.getZ());
        }
    }

    private void leave(Entity entity) {
        Aoi aoi = entityAoiMap.get(entity.getId());
        if (aoi == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("leave:{}, ({}, {})", entity.getId(), aoi.getX(), aoi.getZ());
        }

        Long curAreaId = aoi.getAreaId();
        if (curAreaId != null) {
            Area area = areas.get(curAreaId);
            if (area != null) {
                area.leave(entity, aoi.getGroup());
            }
        }

        // 更新 Aoi 组件
        aoi.setArea(null, 0, 0, 0);
        entityAoiMap.remove(entity.getId());
    }

    private void invalidateNearByCache() {

        if (!cachedNearByEntities.isEmpty()) {
            cachedNearByEntities.clear();

            if (log.isDebugEnabled()) {
                if (cacheHit + cacheMiss > 0) {
                    log.debug("cache hit rate:{}%, total query:{}", cacheHit * 100.0f / (cacheHit + cacheMiss), cacheHit + cacheMiss);
                }
            }

            cacheHit = 0;
            cacheMiss = 0;
        }
    }

    public Set<Entity> getNearByGroupEntities(Aoi aoi, String group) {
        String groupKey = String.format("%s:%x", group, aoi.getAreaId());

        // 缓存当前帧对最邻近区域的查询结果，减少查询次数。
        if (cachedNearByEntities.containsKey(groupKey)) {
            cacheHit++;
            return cachedNearByEntities.get(groupKey);
        }
        cacheMiss++;

        Set<Entity> set = new HashSet<>();

        // 计算当前位置所在的区域
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = aoi.getX() + i;
                int y = aoi.getY();
                int z = aoi.getZ() + j;
                long key = Area.toLong(x, y, z);
                Area area = areas.get(key);
                if (area == null) {
                    continue;
                }
                Set<Entity> members = area.getEntities(group);
                if (members == null || members.isEmpty()) {
                    continue;
                }
                set.addAll(members);
            }
        }

        // 缓存这一帧的查询结果
        cachedNearByEntities.put(groupKey, set);
        return set;
    }

    @Override
    protected void terminate() {
        entities.release();
        entities = null;
    }
}
