package demo.component;

import com.simsilica.es.EntityComponent;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/17
 */
public class CoolDown implements EntityComponent {

    private final double coolDown;

    private double time;

    public CoolDown(double coolDown) {
        this.coolDown = coolDown;
    }

    public boolean isCoolDown() {
        return time >= coolDown;
    }

    public void reset() {
        time = 0;
    }

    public void update(double tpf) {
        time += tpf;
    }
}