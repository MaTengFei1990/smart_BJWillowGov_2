package com.hollysmart.db;
import android.content.Context;

import com.hollysmart.formlib.beans.LastTreeDataBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.utils.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by cai on 2017/12/13
 */

public class LastTreeResDataDao {


    private Dao<LastTreeDataBean, String> resDataDao;
    private DatabaseHelper helper;

    public LastTreeResDataDao(Context context) {
        try {
            helper = DatabaseHelper.getHelper(context);
            resDataDao = helper.getDao(LastTreeDataBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


     /**
     * 修改或增加数据 列表
     */
    public boolean addOrUpdate(List<?> beans) {
        for (LastTreeDataBean bean : (List<LastTreeDataBean>) beans) {
            try {
                if (resDataDao.idExists(bean.getId()+""))
                    resDataDao.update(bean);
                else
                    resDataDao.create(bean);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 修改或增加数据 对象
     */
    public boolean addOrUpdate(LastTreeDataBean bean) {
        try {
            if (resDataDao.idExists(bean.getId()+""))
                resDataDao.update(bean);
            else
                resDataDao.create(bean);
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * 修改或增加数据 对象
     */
    public LastTreeDataBean getLastId() {
        try {
            LastTreeDataBean resDataBean = resDataDao.queryBuilder().orderBy("id", false).queryForFirst();
            return resDataBean;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<LastTreeDataBean> getData() {

        try {
            return resDataDao.queryBuilder().query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 根据景区id 查询景点；
     * @param jqId
     * @return
     */
    public List<LastTreeDataBean> getData(String jqId ) {
        try {
           return resDataDao.queryBuilder().where()
                    .eq("fdTaskId", jqId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /***
     * 根据景区id 查询景点；
     * @param jqId
     * @return
     */
    public List<LastTreeDataBean> getData(String jqId ,boolean parentidIsNull) {
        try {
            if (parentidIsNull) {

                return resDataDao.queryBuilder().where()
                        .eq("fdTaskId", jqId)
                        .and()
                        .isNull("fd_parentid")
                        .query();
            } else {
                return resDataDao.queryBuilder().where()
                        .eq("fdTaskId", jqId)
                        .and()
                        .isNotNull("fd_parentid")
                        .query();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<LastTreeDataBean> getData(String jqId ,String  parentid) {
        try {

                return resDataDao.queryBuilder().where()
                        .eq("fdTaskId", jqId)
                        .and()
                        .eq("fd_parentid",parentid)
                        .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 获取未同步的资源
     * @param jqId
     * @return
     */

    public List<LastTreeDataBean> getSyncData(String jqId ) {
        try {
            Where where = resDataDao.queryBuilder().orderBy("createdAt", false).where();

            if (!Utils.isEmpty(jqId)){
                where.eq("fdTaskId", jqId).and().eq("isUpload",true);
            }
            return where.query();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 根据id 获取数据
     *
     * @param uuid
     * @return
     */
    public List<LastTreeDataBean> getUuidDate(String uuid) {
        try {
            return resDataDao.queryBuilder().where().eq("id", uuid).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 根据id 获取数据
     *
     * @return
     */
    public List<LastTreeDataBean> getUnUpLoadDataList() {
        try {
            return resDataDao.queryBuilder().where().eq("isUpload",false).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * 根据id 获取数据
     *
     * @param id
     * @return
     */
    public boolean deletDataById(String id) {
        try {
            DeleteBuilder builder = resDataDao.deleteBuilder();
            builder.where().in("id", id);
            builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }





    public boolean deletAll(){
        try {
            resDataDao.deleteBuilder().delete();
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean deletByAreaId(String AreaId){
        try {
            DeleteBuilder builder = resDataDao.deleteBuilder();
            builder.where().in("fdTaskId", AreaId);
            builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}






















