package com.hollysmart.db;

import android.content.Context;
import android.util.Log;

import com.hollysmart.beans.HistTreeBean;
import com.hollysmart.utils.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by cai on 2017/12/13
 */

public class HisTreeDao {


    private Dao<HistTreeBean, String> resBeanListDao;
    private DatabaseHelper helper;

    public HisTreeDao(Context context) {
        try {
            helper = DatabaseHelper.getHelper(context);
            resBeanListDao = helper.getDao(HistTreeBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public boolean SaveTreeInTransaction(final List<?> beans) {
        boolean result = false;
        //创建事务管理器
        TransactionManager transactionManager = new TransactionManager(helper.getConnectionSource());
        //一个调用的事件
        Callable<Boolean> callable = new Callable<Boolean>() {  //java.util.concurrent.Callable;
            @Override
            public Boolean call() throws Exception {//如果异常被抛出 事件管理 就知道保存数据失败要回滚
                saveTreeAndUser(beans);
                return true;
            }
        };
        try {
            result = transactionManager.callInTransaction(callable);//执行事件
        } catch (SQLException e) {
            result = false;
            Log.w("YKF", "事务保存异常");
            e.printStackTrace();
        }
        return result;
    }


    public void saveTreeAndUser(List<?> beans) throws Exception {
        /**这里面千万不要捕获掉异常 必须上抛 这样TransactionManager才知道有没有产生异常**/
        for (int i = 0; i < beans.size(); i++) {
            HistTreeBean bean = (HistTreeBean) beans.get(i);
            bean.setId(i);
            if (Utils.isEmpty(bean.getId() + "")) {
                resBeanListDao.update(bean);
            } else {
                resBeanListDao.create(bean);
            }
        }
    }

    public List<HistTreeBean> getData() {
        try {
            return resBeanListDao.queryBuilder().query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean clearAllData() {
        try {
            resBeanListDao.deleteBuilder().delete();
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        return true;
    }
}






















