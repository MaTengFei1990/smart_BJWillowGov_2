package com.hollysmart.formlib.adapters.viewholder;

import com.hollysmart.formlib.beans.DongTaiFormBean;

import java.util.List;

public class ViewHolderUtils {


    public static DongTaiFormBean getItemStatusByPosition(int position,List<DongTaiFormBean> biaoGeBeanList) {

        int countss = 0;


        for (int i = 0; i < biaoGeBeanList.size(); i++) {


            DongTaiFormBean dongTaiFormBean = biaoGeBeanList.get(i);

            String propertyLabel = dongTaiFormBean.getPropertyLabel();

            if (propertyLabel != null && propertyLabel.equals("0")) {

                dongTaiFormBean.setPosition(countss);
                dongTaiFormBean.setGroupindex(i);
                countss++;

            } else {

                List<DongTaiFormBean> propertys = dongTaiFormBean.getCgformFieldList();

                if (propertys != null && propertys.size() > 0) {
                    dongTaiFormBean.setPosition(countss);
                    countss++;

                    for (int j = 0; j < propertys.size(); j++) {

                        DongTaiFormBean dongTaiFormBean1 = propertys.get(j);

                        dongTaiFormBean1.setGroupindex(i);
                        dongTaiFormBean1.setPosition(countss);
                        countss++;

                    }


                } else {


                    dongTaiFormBean.setPosition(countss);
                    dongTaiFormBean.setGroupindex(i);
                    countss++;

                }

            }


        }


        for (int i = 0; i < biaoGeBeanList.size(); i++) {


            DongTaiFormBean dongTaiFormBean = biaoGeBeanList.get(i);

            List<DongTaiFormBean> propertys = dongTaiFormBean.getCgformFieldList();


            String propertyLabel = dongTaiFormBean.getPropertyLabel();

            if (propertyLabel != null && propertyLabel.equals("0")) {


                if (position == dongTaiFormBean.getPosition()) {
                    return dongTaiFormBean;
                }


            } else {

                if (propertys != null && propertys.size() > 0) {

                    if (position == dongTaiFormBean.getPosition()) {
                        return dongTaiFormBean;
                    }

                    for (int j = 0; j < propertys.size(); j++) {

                        DongTaiFormBean dongTaiFormBean1 = propertys.get(j);

                        if (position == dongTaiFormBean1.getPosition()) {
                            return dongTaiFormBean1;
                        }

                    }


                } else {

                    if (position == dongTaiFormBean.getPosition()) {
                        return dongTaiFormBean;
                    }


                }
            }


        }


        return null;


    }
}
