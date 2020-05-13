package com.pc.cacheloader.runner;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.pc.cacheloader.cache.CacheTask;
import com.pc.cacheloader.cache.DepositTask;
import com.pc.cacheloader.constants.TVMsg;
import com.pc.cacheloader.loader.AbstractLoader;
import com.pc.cacheloader.model.CacheMsg;
import com.pc.cacheloader.util.LoaderMonitor;
import com.pc.cacheloader.util.ScheduleLoad;
import com.pc.cacheloader.util.ScheduleRiseLoad;
import com.pc.cacheloader.util.ZkClientTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.pc.cacheloader.constants.Constants.*;

/**
 * 初始化类
 *
 * @author dongxie
 * @date 11:16 2020-05-11
 */
@Component
@Slf4j
@Configuration
@EnableScheduling
public class LoaderRunner implements CommandLineRunner, ApplicationContextAware {

    @Setter
    @Getter
    private ApplicationContext applicationContext;

    @Autowired
    private LoaderMonitor loaderMonitor;

    //存储spring中的loader
    private List<AbstractLoader> loaders = new ArrayList<>();

    @Value("${zk.force.update}")
    private Boolean forceUpdate = false;

    @Value("${log.debug}")
    private Boolean logDebug = false;

    @Resource
    @Qualifier("asyncChannelProcess")
    private AsyncChannelProcess<TVMsg> asyncChannelProcess;

    @Autowired
    private ZkClientTemplate zkClientTemplate;


    //全局的AsyncChannelProcess,队列中保存的不是task，是还未封装成task的TVMsg
    @Bean(name = "asyncChannelProcess")
    public AsyncChannelProcess getTvMsgAsyncChannelProcess() {
        return AsyncChannelProcess.<TVMsg>newBuilder()
                .setAddBlockTimeout(24, TimeUnit.HOURS)
                .setMaxProcessCount(10)
                .setProcess(new Function<TVMsg, Boolean>() {
                    @Override
                    public Boolean apply(TVMsg msg) {
                        saveImpl(msg);
                        return true;
                    }
                })
                .build();
    }


    //全局执行器AsyncChannelProcess的执行方法
    private void saveImpl(TVMsg msg) {
        if (logDebug)
            log.info("recv data {}", JSON.toJSONString(msg));
        if (CollectionUtils.isEmpty(loaders)) {
            return;
        }
        for (AbstractLoader abstractLoader : loaders) {
            if (abstractLoader.supportType().isInstance(msg.getT())) {//根据类型找到外部队列中的任务对应的loader
                abstractLoader.consumerMsg(msg);
                return;
            }
        }
        log.info("unKnow type {}", JSON.toJSONString(msg));
    }


    @PostConstruct
    public void init() {
        //获取所有loader
        Map<String, AbstractLoader> map = applicationContext.getBeansOfType(AbstractLoader.class);
        ExecutorService executorService = Executors.newFixedThreadPool(map.size());
        EventLoopExecutor[] eventLoopExcutors = new EventLoopExecutor[map.size()];

        if (!map.isEmpty()) {
            //给每个loader分配一个eventLoopExcutors
            for(int i=0;i<map.size();i++) {
                eventLoopExcutors[i] = new EventLoopExecutor(30,applicationContext);
                AbstractLoader loader = ((AbstractLoader) map.values().toArray()[i]);
                //注册事件驱动
                loader.registerExecutor(eventLoopExcutors[i]);
                loader.initHandle();//初始化处理链
                loader.initCache();//初始化缓存
                //启动事件驱动器，执行缓存操作
                executorService.execute(eventLoopExcutors[i]);
                loaders.add(loader);
            }


        }

    }

    //一天执行一次
    @Scheduled(cron = "${loader.schedule.normal}")
    public void normalSchedule() {
        log.info("normal schedule load ..");
        initDate();
        //通过注解获取业务loader
        Map<String, Object> beansWithAnnotationMap = this.applicationContext.getBeansWithAnnotation(ScheduleLoad.class);
        if (!beansWithAnnotationMap.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Object object : beansWithAnnotationMap.values()) {
                ((AbstractLoader) object).normalDataLoad();
            }
            loaderMonitor.register(ZK_UPDATE_PATH, now.format(DATE_TIME_FORMATTER));
            loaderMonitor.register(ZK_RISE_PATH, now.format(DATE_TIME_FORMATTER));
        }
    }

    @Scheduled(fixedRateString = "${loader.schedule.rise}")
    public void riseSchedule() {
        LocalDateTime now = LocalDateTime.now();
        //判断今天是否全量加载过
        String time = loaderMonitor.getMonitorMapValue(ZK_UPDATE_PATH);
        if (StringUtils.isEmpty(time) || LocalDateTime.parse(time, DATE_TIME_FORMATTER).isBefore(LocalDateTime.of(LocalDate.now(),
                LocalTime.MIN))) {
            loaderMonitor.register(ZK_RISE_PATH, now.format(DATE_TIME_FORMATTER));
            return;
        }
        String lastRiseTime = loaderMonitor.getMonitorMapValue(ZK_RISE_PATH);
        //通过注解获取loader
        Map<String, Object> beansWithAnnotationMap =
                this.applicationContext.getBeansWithAnnotation(ScheduleRiseLoad.class);
        if (!beansWithAnnotationMap.isEmpty()) {
            for (Object object : beansWithAnnotationMap.values()) {
                ((AbstractLoader) object).riseDataLoad(LocalDateTime.parse(lastRiseTime,
                        DATE_TIME_FORMATTER), now);
            }
        }
        loaderMonitor.register(ZK_RISE_PATH, now.format(DATE_TIME_FORMATTER));
    }

    private void initDate() {
        if (isIndexExist("ids"))
            clearIndex();
        if (!CollectionUtils.isEmpty(loaders)) {
            for (AbstractLoader loader : loaders) {
                loader.normalDataLoad();
            }
        }
    }

    private void clearIndex() {
        System.out.println("存在index就清空es");
    }

    private boolean isIndexExist(String ids) {
        System.out.println("es存在index");
        return true;
    }


    @Override
    public void run(String... args) throws Exception {
//强制更新一次
        if (forceUpdate) {
            log.info("forceUpdate delete zk path");
            zkClientTemplate.deleteNode(ZK_UPDATE_PATH);
        }
        //判断今天是否更新过数据
        String time = loaderMonitor.getMonitorMapValue(ZK_UPDATE_PATH);
        if (StringUtils.isEmpty(time)) {
            initDate();
            loaderMonitor.register(ZK_UPDATE_PATH, LocalDateTime.now().format(DATE_TIME_FORMATTER));
        } else {
            //判断今天是否已经全量更新
            if (!LocalDateTime.parse(time, DATE_TIME_FORMATTER).isAfter(LocalDateTime.of(LocalDate.now(),
                    LocalTime.MIN))) {
                initDate();
                loaderMonitor.register(ZK_UPDATE_PATH,
                        LocalDateTime.now().format(DATE_TIME_FORMATTER));
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
