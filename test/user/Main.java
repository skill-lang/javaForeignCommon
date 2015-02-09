package user;

import age.Age;
import age.internal.SkillState;

public class Main {
    public static void main(String[] args) throws Exception {
        SkillState sf = SkillState.open("age-test.sf");
        for (Age a : sf.Ages())
            System.out.println(a.prettyString());
    }
}
