package com.zzy.mall.product.web;

import com.zzy.mall.product.entity.CategoryEntity;
import com.zzy.mall.product.service.CategoryService;
import com.zzy.mall.product.vo.Catalog2VO;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/","/index.html","/home","/home.html"})
    public String index(Model model) {
        // 查询出所有一级分类的信息
        List<CategoryEntity> list = categoryService.getLevel1Category();
        model.addAttribute("categories", list);
        // classpath:/templates
        //.html
        return "index";
    }

    // index/catalog.json
    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2VO>> getCatalog2JSON(){
        Map<String, List<Catalog2VO>> map = categoryService.getCatalog2JSON();
        return map;
    }

    /**
     * 1.锁会自动续期 如果业务时间超长 运行期间Redisson会自动给锁添加三十秒 不用担心业务时间过长 锁自动过期造成的数据安全问题
     * 2.加锁的业务只要执行完成 就不会给当前的锁续期 即使不去主动的释放锁 锁在默认三十秒后也会自动删除
     *
     * 如果我们制定了锁的过期时间 在源码中会直接帮我们创建一个过期时间是指定值的锁到期时间 时间到期后就会把该锁给删除
     * 如果我们没有指定锁的过期时间 在执行的时候首先会设置锁的过期时间为三十秒 然后会创建异步任务 每隔十秒执行一次任务来续期
     *
     * 从性能方面考虑 我们应该指定锁的过期时间
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        RLock myLock = redissonClient.getLock("myLock");
        // 上锁
        //myLock.lock();
        // 获取锁 给定过期时间是10s 指定过期时间后 自动续期就不会自动生效了 我们设置的过期时间一定要满足业务场景
        myLock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功...业务处理..." + Thread.currentThread().getName());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放锁
            myLock.unlock();
            System.out.println("释放锁成功..." + Thread.currentThread().getName());
        }
        return "hello";
    }

    @GetMapping("/writer")
    public String writer(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        // 获取写锁
        RLock wLock = readWriteLock.writeLock();
        String uuid = null;
        // 加写锁
        wLock.lock();
        try {
            System.out.println("加写锁成功....");
            uuid = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set("msg", uuid);
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            // 释放锁
            System.out.println("释放写锁成功....");
            wLock.unlock();
        }
        return uuid;
    }

    @GetMapping("/reader")
    @ResponseBody
    public String reader(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        String s = null;
        try {
            System.out.println("加读锁成功....");
            s = stringRedisTemplate.opsForValue().get("msg");
        }finally {
            System.out.println("释放读锁成功....");
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor(){
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5l);
        try {
            door.await(); // 等待数量降低到0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "关门熄灯...";
    }

    @GetMapping("/goHome/{id}")
    @ResponseBody
    public String goHome(@PathVariable("id") Long id){
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown(); // 递减的操作
        return id + "下班走人";
    }

    @GetMapping("/park")
    @ResponseBody
    public String park(){
        RSemaphore park = redissonClient.getSemaphore("park");
        boolean b = true;
        try {
            //park.acquire(); // 获取信号量 阻塞到获取成功
            b = park.tryAcquire(); // 返回获取成功还是失败
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "停车是否成功:" + b;
    }

    @GetMapping("/release")
    @ResponseBody
    public String release(){
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();
        return "释放了一个车位";
    }

}
