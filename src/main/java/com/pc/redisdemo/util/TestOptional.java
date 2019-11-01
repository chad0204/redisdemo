package com.pc.redisdemo.util;

import java.util.Optional;

/**
 * TODO
 *
 * @author pengchao
 * @since 18:49 2019-10-25
 */
public class TestOptional {

    public static void main(String[] args) {


        /**
         *  5. orElse orElseThrow
         */
        Person person = new Person();
        person.setId(2333L);
//        if(person.getName()==null) {
//            throw new RuntimeException();
//        }
//        String name = person.getName()!=null ? person.getName() : "default";

        Optional<Person> optionalPerson = Optional.of(person);

        //如果getName为null,设置一个有默认值的对象
        Person defaultPerson = new Person();
        defaultPerson.setName("default");

        Optional<Person> op = Optional.of(person);

//        String name = Optional.of(person.getName()).orElse(defaultPerson).getName();


        System.out.println();

    }
}


class Person {
    private String name;
    private Integer age;
    private Long id;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
