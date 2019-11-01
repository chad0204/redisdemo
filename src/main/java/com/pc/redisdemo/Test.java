package com.pc.redisdemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author pengchao
 * @since 15:58 2019-10-21
 */
public class Test {

    public static void main(String[] args) {

        List<Person> list = Arrays.asList(new Person(1L,"aa"),new Person(2L,"bb"),new Person(3L,"cc"));


        List<Person> tempList = new ArrayList<>();
        int num = 10000/list.size();
        for (int i = 0; i<num; i++ ) {

            for (int j=0;j<list.size();j++) {
                Person p = list.get(j);
                tempList.add(p);
            }
        }

        int j = 0;
        for (int i=0; i< tempList.size(); i++) {
            tempList.get(i).setName(tempList.get(i).getName()+j++);
        }

        System.out.println();





    }
}

class Person {
    Long id;
    String name;

    public Person(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
