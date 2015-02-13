package user;

import age.Age;
import age.internal.SkillState;
import de.ust.skill.common.java.api.SkillFile.Mode;

public class Main {
    public static void main(String[] args) throws Exception {
        long last = System.currentTimeMillis();
        {
            SkillState sf = SkillState.open("age-test.sf", Mode.Create);
            for (Age a : sf.Ages())
                System.out.println(a.prettyString());
        }
        System.out.println("done (" + (System.currentTimeMillis() - last) + "ms)");
        last = System.currentTimeMillis();
        {
            SkillState sf = SkillState.open("test/ageUnrestricted.sf", Mode.Read);
            for (Age a : sf.Ages())
                System.out.println(a.prettyString());
        }
        System.out.println("done (" + (System.currentTimeMillis() - last) + "ms)");
        for (int i = 0; i < 100; i++) {
            System.gc();
            last = System.currentTimeMillis();
            {
                SkillState sf = SkillState.open("test/age16.sf", Mode.Read);
                System.out.println(sf.Ages().size());
            }
            System.out.println("done (" + (System.currentTimeMillis() - last) + "ms)");
        }
    }
}
