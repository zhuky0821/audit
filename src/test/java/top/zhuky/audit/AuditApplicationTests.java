package top.zhuky.audit;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@Slf4j
class AuditApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    DataSource dataSource;

    @Autowired
    Fair fair;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    void testAudit(){

        try{
            Connection connection = dataSource.getConnection();

            //查询产品
            List<Integer> products = new ArrayList<Integer>();
            String sql_product = "select product_id from tproduct";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql_product);
            while (resultSet.next()){
                int prod = resultSet.getInt(1);
                //System.out.println("加入产品"+prod);
                products.add(prod);
            }


            CountDownLatch countDownLatch = new CountDownLatch(300);
            for(Integer pro1:products){
                threadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            fair.fairEveryday(pro1, -1, 20190101, 20200101);
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            Long l = countDownLatch.getCount();
                            //log.debug("countDownLatch="+l);
                            countDownLatch.countDown();
                        }
                    }
                });
            }

            try {
                countDownLatch.await();
                log.debug("所有线程结束");
            } catch (Exception e) {
                log.error("阻塞异常");
            }

            //fair.fairEveryday(51, 150, 20190101, 20190102);


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testGetConn(){
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.7.211:1521:orcl", "msth", "1");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
