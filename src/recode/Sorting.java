/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recode;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author TitarX
 */
public class Sorting implements Comparator<String>
{

    public static final int ASC=1;
    public static final int DESC=-1;
    private int typeSorting=1;

    @Override
    public int compare(String o1,String o2)
    {
        Collator collator=Collator.getInstance(new Locale(System.getProperty("user.country"),System.getProperty("user.language")));
        switch(typeSorting)
        {
            case ASC:
                return collator.compare(o1,o2);
            case DESC:
                return (~collator.compare(o1,o2))+1;
            default:
                return collator.compare(o1,o2);
        }
    }

    public void sort(int typeSorting,List<String> list)
    {
        this.typeSorting=typeSorting;
        Collections.sort(list,this);
    }

    public void sort(List<String> list)
    {
        Collections.sort(list,this);
    }

    public int getTypeSorting()
    {
        return typeSorting;
    }

    public void setSorting(int typeSorting)
    {
        this.typeSorting=typeSorting;
    }
}
