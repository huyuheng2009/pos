/**
 * 项目: pos
 * 包名：pos
 * 文件名: CacheServiceTest
 * 创建时间: 2014/11/4 10:49
 * 支付界科技有限公司版权所有，保留所有权利
 */
package pos;

import com.yogapay.core.domain.SysDict;
import com.yogapay.core.service.CacheService;
import com.yogapay.junit.AbstractTest;
import org.junit.*;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * @Todo:
 * @Author: Zhanggc
 */
public class CacheServiceTest extends AbstractTest {

    @Resource
    CacheService cacheService;

    @org.junit.Test
    public void findByNameKey() throws SQLException {
        SysDict sysDict = cacheService.findByNameKey("MERCHANT_ACCOUNT","JIFEN_RULE");
        System.out.println(sysDict.getDictValue());
    }

}
