package com.pc.redistemplatecluster;

import com.pc.redistemplatecluster.redisTemplate.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.*;
import java.util.concurrent.TimeUnit;
/**
 * 使用原生jedis集群，就不能使用redisTemplate
 *
 * 其实springboot的redisTemplate底层用的还是jedisCluster
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTemplateClusterApplicationTests {

    @Autowired
    RedisUtil redisUtil;


    @Autowired
    private StringRedisTemplate redisTemplate;



    @Test
    public void testSemaphoreLock() {
        for (int i = 0; i < 1000; i++) {

            Thread t = new Thread(() -> {
//                System.out.println(Thread.currentThread().getName() + " 启动运行");

                while(true){
                    String indentifier = redisUtil.acquireSemaphore("sem_name", 2, 1000*60);
                    if (indentifier == null) {
                        System.out.println(Thread.currentThread().getName() + " 获取信号量失败，取消任务. Semaphore  Semaphore  Semaphore  Semaphore");
                    }else{
                        System.out.println(Thread.currentThread().getName() + "  执行任务");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        redisUtil.releaseSemaphore("sem_name", indentifier);
                        break;
                    }
                }
            });

            t.start();

        }

        System.out.println();
    }

    @Test
    public void testCluster() {
        redisUtil.del("ddd");
        redisUtil.set("ddd","redisTemplate");
        System.out.println();
    }


    @Test
    public void testLock() throws InterruptedException {

        redisUtil.lock("lock",20);

        TimeUnit.SECONDS.sleep(10);


        System.out.println(redisUtil.lock("lock",20));

        System.out.println(redisUtil.release("lock"));

        TimeUnit.SECONDS.sleep(11);
        System.out.println(redisUtil.lock("lock",20));
    }


    @Test
    public void testString() {
        //自增
//        Long value = redisTemplate.opsForValue().increment("increment");

        //
//        redisTemplate.opsForValue().increment("delta",2);
//
//        redisTemplate.opsForValue().increment("key",2.0);
//
//        redisTemplate.opsForValue().decrement("key",3);
//
//
//        redisTemplate.opsForValue().append("aa","bb");
//
//        redisTemplate.opsForValue().get("aa",1,2);


        redisTemplate.opsForValue().setIfAbsent("absent","bb",10,TimeUnit.SECONDS);//不存在则set

        redisTemplate.opsForValue().setIfPresent("absent","bb",10,TimeUnit.SECONDS);//存在才set，




        String str = redisTemplate.opsForValue().get("increment");

        System.out.println();

    }



    @Test
    public void testList() throws InterruptedException {
        List<String> list = Arrays.asList("aa","bb","cc");

        redisUtil.lSet("testList",list);


//        new Thread(() -> {
//           for(int i = 0;i<100;i++) {
//               try {
//                   TimeUnit.SECONDS.sleep(1);
//               } catch (InterruptedException e) {
//                   e.printStackTrace();
//               }
//               redisTemplate.opsForList().leftPush("list",i+"");
//           }
//        }).start();
////
//        new Thread(() -> {
//            for(int i = 0;i<100;i++) {
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println(redisTemplate.opsForList().rightPop("list",500,TimeUnit.MILLISECONDS));
//
//            }
//        }).start();


        System.out.println(redisTemplate.opsForList().rightPop("list",5,TimeUnit.SECONDS));//阻塞5s,5s内有元素弹出，否则返回null

        TimeUnit.SECONDS.sleep(1000);

    }

    @Test
    public void testSet() {

        redisTemplate.opsForSet().add("set","aa","bb","cc","dd","11");


        redisTemplate.opsForSet().add("otherSet","11","22","33","44","aa");

        //差集  存在于set,不存在与otherSet的元素
        redisTemplate.opsForSet().differenceAndStore("set","otherSet","destSet");

        //交集  同时存在于set和otherSet的元素
        redisTemplate.opsForSet().intersectAndStore("set","otherSet","interSet");

        //并集  返回那些至少存在与一个集合的元素
        redisTemplate.opsForSet().unionAndStore("set","otherSet","unionSet");



        System.out.println(redisTemplate.opsForSet().members("unionSet"));

        System.out.println(redisTemplate.opsForSet().size("set"));

    }

    @Test
    public void testHash() {

        for(int i=0;i<10;i++) {
            redisTemplate.opsForHash().put("hash","filed"+i,"value"+i);
        }


        Map<Object,Object> map = redisTemplate.opsForHash().entries("hash");//hgetall

        List<Object> valueList = redisTemplate.opsForHash().multiGet("hash",Arrays.asList("filed0","filed9"));//hMGet



        Map<String,String> filedMap = new HashMap<>();
        filedMap.put("aa","11");
        filedMap.put("bb","22");
        redisTemplate.opsForHash().putAll("hash",filedMap);//hMSet

        redisTemplate.opsForHash().keys("hash");//hKeys

        redisTemplate.opsForHash().values("hash");

        redisTemplate.opsForHash().size("hash");//hlen
        System.out.println(map);

    }


    /**
     * 有序集合，按照分值排序
     */
    @Test
    public void testZSet() {
        String key = "runoobkey";

        //ZADD
        redisTemplate.opsForZSet().add(key,"redis",1);
        redisTemplate.opsForZSet().add(key,"mysql",2);
        redisTemplate.opsForZSet().add(key,"mongodb",3);
        redisTemplate.opsForZSet().add(key,"mongodb",2);//覆盖


        //zrange
        Set<String> set = redisTemplate.opsForZSet().range("runoobkey",0,-1);//-1获取全部，-1表示倒数第一个成员
        Set<ZSetOperations.TypedTuple<String>> setWithScore = redisTemplate.opsForZSet().rangeWithScores("runoobkey",0,10);//带分值
        redisTemplate.delete(key);
        //ZADD key score1 member1 [score2 member2]
        redisTemplate.opsForZSet().add(key,setWithScore);//批量新增

        //ZCARD key
        System.out.println(redisTemplate.opsForZSet().zCard(key));
        System.out.println(redisTemplate.opsForZSet().size(key));

        //ZCOUNT key min max ,返回分值为min到max的成员数量
        System.out.println(redisTemplate.opsForZSet().count(key,1,3));

        // ZINCRBY key increment member,
        Double newScore = redisTemplate.opsForZSet().incrementScore(key,"redis",10);
        System.out.println(newScore);

        //ZINTERSTORE destination numkeys key
        String student1 = "{student}-1";//如果key被散列到不同的master的slot上，将不能聚合，在定义key时用括号加相同内容，可以保证计算的hash在同一个slot.
        String student2 = "{student}-2";
        String student3 = "{student}-3";
        redisTemplate.opsForZSet().add(student1,"路飞",100);
        redisTemplate.opsForZSet().add(student1,"索隆",200);
        redisTemplate.opsForZSet().add(student1,"娜美",300);
        redisTemplate.opsForZSet().add(student1,"山治",400);

        redisTemplate.opsForZSet().add(student2,"路飞",10);
        redisTemplate.opsForZSet().add(student2,"索隆",20);
        redisTemplate.opsForZSet().add(student2,"娜美",30);
        redisTemplate.opsForZSet().add(student2,"乔巴",40);

        redisTemplate.opsForZSet().add(student3,"路飞",1);
        redisTemplate.opsForZSet().add(student3,"索隆",1);

//        redisTemplate.opsForZSet().intersectAndStore(student1,Arrays.asList(student2,student3),"{student}-sum");//交集，分值相加
//        Set<ZSetOperations.TypedTuple<String>> sum1 = redisTemplate.opsForZSet().rangeWithScores("{student}-sum",0,-1);

//        redisTemplate.opsForZSet().intersectAndStore(student1,Arrays.asList(student2,student3),"{student}-max", RedisZSetCommands.Aggregate.MAX);////交集，最大
//        Set<ZSetOperations.TypedTuple<String>> max = redisTemplate.opsForZSet().rangeWithScores("{student}-max",0,-1);

//        redisTemplate.opsForZSet().intersectAndStore(student1,Arrays.asList(student2,student3),"{student}-min", RedisZSetCommands.Aggregate.MIN);////交集，最小
//        Set<ZSetOperations.TypedTuple<String>> max = redisTemplate.opsForZSet().rangeWithScores("{student}-min",0,-1);


        redisTemplate.opsForZSet()
                .intersectAndStore(student1,
                        Arrays.asList(student2,student3),
                        "{student}-wight",
                        RedisZSetCommands.Aggregate.MAX,
                        RedisZSetCommands.Weights.of(1,10,200));//必须匹配key数量
        Set<ZSetOperations.TypedTuple<String>> wight = redisTemplate.opsForZSet().rangeWithScores("{student}-wight",0,-1);//每个key的分值都乘对应权重


        //上面的记得删哦


        //zlexcount ,在有序集合中计算指定字典区间内成员数量,暂无


        /*
         * zrange 索引区间（负值表示倒数）
         *
         * zrangbyscore 分值区间
         *
         * zrangbylex 字典区间（这个排序只有在有相同分数的情况下才能使用，如果有不同的分数则返回值不确定）
         */

        //悬赏金,不实现也可以使用默认的，new DefaultTypedTuple<Object>("name",6);
        Set<ZSetOperations.TypedTuple<String>> rewardOffered = new HashSet<>();
        ZSetOperations.TypedTuple<String> rewardOffered1 = new AnswerVoInZset("索隆", 6);
        ZSetOperations.TypedTuple<String> rewardOffered2 = new AnswerVoInZset("山治", 15);
        ZSetOperations.TypedTuple<String> rewardOffered3 = new AnswerVoInZset("娜美", 3);
        rewardOffered.add(rewardOffered1);
        rewardOffered.add(rewardOffered2);
        rewardOffered.add(rewardOffered3);
        redisTemplate.opsForZSet().add("rewardOffered",rewardOffered);



        //zrangeByLex，分数相同才有效
        redisTemplate.opsForZSet().add("zsetlex","h",0);
        redisTemplate.opsForZSet().add("zsetlex","f",0);
        redisTemplate.opsForZSet().add("zsetlex","g",0);
        redisTemplate.opsForZSet().add("zsetlex","e",0);
        redisTemplate.opsForZSet().add("zsetlex","c",0);
        redisTemplate.opsForZSet().add("zsetlex","d",0);
        redisTemplate.opsForZSet().add("zsetlex","b",0);
        redisTemplate.opsForZSet().add("zsetlex","a",0);

        RedisZSetCommands.Range range = new RedisZSetCommands.Range();//a到d之间的
        range.gt("a");
        range.lt("d");
        RedisZSetCommands.Limit limit = new RedisZSetCommands.Limit();//限制数量,offset开始，count个
        limit.count(6);
        limit.offset(0);
        Set<String> lex = redisTemplate.opsForZSet().rangeByLex("zsetlex",range);
        Set<String> lexLimit = redisTemplate.opsForZSet().rangeByLex("zsetlex", range,limit);

        //zrank，返回成员索引值
        redisTemplate.opsForZSet().rank("zsetlex","f");

        //zrem,移除成员
        redisTemplate.opsForZSet().remove("zsetlex","f");


        System.out.println();



    }








}
