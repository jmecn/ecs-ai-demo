package demo;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/16
 */
public final class Constants {
    private Constants() {
    }

    public static final float MAP_SIZE = 256f;

    public static final int CELL_SIZE = 8;

    public static final int BLUE_TEAM_COUNT = 1000;

    public static final int ORANGE_TEAM_COUNT = 1000;

    public static final float BLUE_AOI_RADIUS = 0.6f;
    public static final float BLUE_AOI_RADIUS_SQUARED = BLUE_AOI_RADIUS * BLUE_AOI_RADIUS;
    public static final float BULLET_AOI_RADIUS = 0.2f;

    public static final double COOL_DOWN = 0.1f;
    public static final double BULLET_LIFE = 0.5f;
    public static final float BULLET_SPEED = 8f;
    public static final float BLUE_TEAM_SPEED = 6f;
    public static final float ORANGE_TEAM_SPEED = 2f;
    public static final float ORANGE_AOI_RADIUS = 5f;
    public static final String BLUE_TEAM = "blue_team";
    public static final String ORANGE_TEAM = "orange_team";
    public static final String BULLET = "bullet";
    public static final String DEBUG_SEGMENT = "debug_segment";
    public static final String DEBUG_RADIUS = "debug_radius";
}