/**
 * Created by Eric on 2016/11/25.
 */
//the abstract class of translator
public abstract class Translator implements Comparable{
    String name;
    int votes;
    boolean isEnable;
    public int compareTo(Object t) {
        if(votes<((Translator)t).votes)
            return 1;
        else if(votes>((Translator)t).votes)
            return -1;
        else
            return 0;
    }
    public abstract WORD getTranslation(String text)throws Exception;
}
