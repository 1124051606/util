package com.xw.web.app.action;

import com.xw.web.app.action.annotation.RedisLockHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by toutou on 2019/1/27.
 */
@RestController
@Slf4j
public class RedisController {

    @Autowired
    RedisLockHelper redisLockHelper;
    int surplusCount = 100;
    /**
     * 超时时间 5s
     */
    private static final int TIMEOUT = 1*1000;

    @PostMapping(value = "/seckilling")
    public String Seckilling(String targetId){
        //加锁
        long time = System.currentTimeMillis() + TIMEOUT;
        if(!redisLockHelper.lock(targetId,String.valueOf(time))){
            return "排队人数太多，请稍后再试.";
        }
        System.out.println(System.currentTimeMillis());
        // 查询该商品库存，为0则活动结束 e.g. getStockByTargetId
        if(surplusCount==0){
            return "活动结束.";
        }else {
            // 下单 e.g. buyStockByTargetId

            //减库存 不做处理的话，高并发下会出现超卖的情况，下单数，大于减库存的情况。虽然这里减了，但由于并发，减的库存还没存到map中去。新的并发拿到的是原来的库存
            surplusCount =surplusCount-1;
            try{
                Thread.sleep(15000);//模拟减库存的处理时间
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            // 减库存操作数据库 e.g. updateStockByTargetId

            // buyStockByTargetId 和 updateStockByTargetId 可以同步完成(或者事物)，保证原子性。
        }

        //解锁
        redisLockHelper.unlock(targetId,String.valueOf(time));

        return "恭喜您，秒杀成功。"+surplusCount;
    }
}
