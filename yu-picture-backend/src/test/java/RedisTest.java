import com.yupi.yupicturebackend.YuPictureBackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

// 这里指定了启动类，IDEA就不会找不到配置了
@SpringBootTest(classes = YuPictureBackendApplication.class)
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisConnect() {
        // 写入测试数据
        stringRedisTemplate.opsForValue().set("test:redis", "连接成功啦！");
        // 读取数据
        String result = stringRedisTemplate.opsForValue().get("test:redis");
        // 打印结果
        System.out.println("===== Redis连接测试结果 =====");
        System.out.println("测试结果：" + result);
        System.out.println("✅ Redis连接成功！");
    }
}