package top.zhuky.audit;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @author zhuky
 * @Date: 2020/05/11/16:17
 * @Description:
 */
@Slf4j
@Service
public class Fair {

    @Autowired
    HikariDataSource hikariDataSource;

    @Async
    public void fairEveryday(int pro1, int pro2, int begin, int end){
        Connection connection = getConn();

        log.debug("开始产品"+pro1+"和"+pro2);
        try{
            //3、创建执行存储过程的语句对象
            String sql = "{call pg_fairtradeaudit.p_fair_everyday(?,?,?,?,?,?)}";
            CallableStatement call = connection.prepareCall(sql);
            //4、设置参数
            call.setInt(1, pro1);
            call.setInt(2, pro2);
            call.setInt(3, begin);
            call.setInt(4, end);
            call.registerOutParameter(5, JDBCType.INTEGER);
            call.registerOutParameter(6, JDBCType.VARCHAR);
            //5、执行存储过程
            call.execute();
            //6、获取数据并输出结果
            int errorId = call.getInt(5);
            String errorMessage = call.getString(6);

            call.close();
            if(errorId != 0){
                log.error("产品"+pro1+"和"+pro2+"执行"+begin+"~"+end+"出错："+errorMessage);
            }
            log.debug("结束产品"+pro1+"和"+pro2);
            call.close();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public Connection getConn(){
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.7.211:1521:orcl", "msth", "1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
