package com.hollysmart.formlib.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.beans.DictionaryBean;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.beans.cgformRuleBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.dialog.BsSelectDialog;
import com.hollysmart.dialog.RoadPlateBsSelectDialog;
import com.hollysmart.dialog.TimePickerDialog;
import com.hollysmart.formlib.activitys.GaoDeMapRangeActivity;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.views.linearlayoutforlistview.MyLinearLayoutForListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.qqtheme.framework.picker.AddressPicker;

import static android.content.Context.VIBRATOR_SERVICE;


/**
 * Created by cai on 2017/12/5  基础表格
 */
public class BiaoGeRecyclerAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<DongTaiFormBean> biaoGeBeanList;
    private TimePickerDialog timePickerDialog;

    private BlockBean roadbean;
    private ResDataBean tree_resDataBean;
    private ProjectBean projectBean;

    private boolean isCheck = false; //是否是查看，true查看，不能编辑；
    private DictionaryBean cancelbean;

    private boolean isNewAdd;

    private List<ResDataBean>treeList=new ArrayList<>();

    JDPicInfo picBeannull = new JDPicInfo(0, null, null, null, 1, "false");

    private HashMap<String, List<DictionaryBean>> map = new HashMap<>();

    public BiaoGeRecyclerAdapter2(Context mContext, List<DongTaiFormBean> biaoGeBeanList, boolean isCheck) {
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.biaoGeBeanList = biaoGeBeanList;
        this.isCheck = isCheck;
        for (DongTaiFormBean bean : biaoGeBeanList) {
            String propertyLabel = bean.getPropertyLabel();
            List<DongTaiFormBean> childlist = bean.getCgformFieldList();
            if (Utils.isEmpty(propertyLabel) && childlist != null && childlist.size() > 0) {
                bean.setPropertyLabel("0");
            }

            if (Utils.isEmpty(propertyLabel) && bean.getShowType().equals("switch")) {
                bean.setPropertyLabel("0");
            }
            if (childlist != null && childlist.size() > 0) {

                for (DongTaiFormBean childbean : childlist) {

                    String childpropertyLabel = childbean.getPropertyLabel();

                    List<DongTaiFormBean> childcgformFieldList = childbean.getCgformFieldList();
                    if (Utils.isEmpty(childpropertyLabel) && childcgformFieldList != null && childcgformFieldList.size() > 0) {
                        childbean.setPropertyLabel("0");
                    }

                    if (Utils.isEmpty(childpropertyLabel) && childbean.getShowType().equals("switch")) {
                        childbean.setPropertyLabel("0");
                    }
                }
            }


        }
        cancelbean = new DictionaryBean();
        cancelbean.setValue("");
        cancelbean.setLabel("取消");
    }


    /***
     * 树木的
     * @param mContext
     * @param biaoGeBeanList
     * @param isCheck
     * @param roadbean
     * @param tree_resDataBean
     */

    public BiaoGeRecyclerAdapter2(Context mContext, List<DongTaiFormBean> biaoGeBeanList, boolean isCheck, BlockBean roadbean, ResDataBean tree_resDataBean, ProjectBean projectBean) {
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
        this.biaoGeBeanList = biaoGeBeanList;
        this.roadbean = roadbean;
        this.tree_resDataBean = tree_resDataBean;
        this.projectBean = projectBean;
        this.isCheck = isCheck;
        for (DongTaiFormBean bean : biaoGeBeanList) {
            String propertyLabel = bean.getPropertyLabel();
            List<DongTaiFormBean> childlist = bean.getCgformFieldList();
            if (Utils.isEmpty(propertyLabel) && childlist != null && childlist.size() > 0) {
                bean.setPropertyLabel("0");
            }

            if (Utils.isEmpty(propertyLabel) && bean.getShowType().equals("switch")) {
                bean.setPropertyLabel("0");
            }
            if (childlist != null && childlist.size() > 0) {

                for (DongTaiFormBean childbean : childlist) {

                    String childpropertyLabel = childbean.getPropertyLabel();

                    List<DongTaiFormBean> childcgformFieldList = childbean.getCgformFieldList();
                    if (Utils.isEmpty(childpropertyLabel) && childcgformFieldList != null && childcgformFieldList.size() > 0) {
                        childbean.setPropertyLabel("0");
                    }

                    if (Utils.isEmpty(childpropertyLabel) && childbean.getShowType().equals("switch")) {
                        childbean.setPropertyLabel("0");
                    }
                }
            }

            if (!isCheck&&bean.getJavaField().equals("tree_number")) {
                if (Utils.isEmpty(bean.getPropertyLabel())) {
                    bean.setPropertyLabel("1");
                }
            }


        }
        cancelbean = new DictionaryBean();
        cancelbean.setValue("");
        cancelbean.setLabel("取消");
    }

    public boolean isNewAdd() {
        return isNewAdd;
    }

    public void setNewAdd(boolean newAdd) {
        isNewAdd = newAdd;
    }

    public List<ResDataBean> getTreeList() {
        return treeList;
    }

    public void setTreeList(List<ResDataBean> treeList) {
        this.treeList = treeList;
    }

    public void setMap(HashMap<String, List<DictionaryBean>> map) {
        this.map = map;
    }

    private static int VIEWTYPE_DANHANG = 0;
    private static int VIEWTYPE_DANHANG_LIST = 1;
    private static int VIEWTYPE_DANHANG_TIME_SELECT = 2;
    private static int VIEWTYPE_CONTENT_CHILD_LIST = 3;//包含子表单
    private static int VIEWTYPE_CONTENT_IMAGE_CONTENT = 4;//包含图片
    private static int VIEWTYPE_CONTENT_MARKER_CONTENT = 5;//地图定位
    private static int VIEWTYPE_CONTENT_PLANE_CONTENT = 6;//面
    private static int VIEWTYPE_CONTENT_LINE_CONTENT = 8;//线
    private static int VIEWTYPE_CONTENT_SWITCH_CONTENT = 9;//开关
    private static int VIEWTYPE_CONTENT_MULTISTAGELIST_CONTENT = 10;//multistageList

    private static int VIEWTYPE_CONTENT_CHECKBOX = 11;//多选

    /**
     * propertyType
     * 0   单文本
     * 1   多行文本
     * 2   字典
     * 3   子表单
     * 4   动态数组
     * expression
     * ""  无
     * 验证type 1：数字 2：英文和数字 3：邮箱 4：电话号码和固话 5：身份证号 6：ip地址 7：邮政编码 8：数字和小数点
     *
     * @return -1         无
     * 0          单行
     * 1          单行 有父标签
     * 2          单行 选择
     * 3          单行 选择 有父标签
     * 4          多行
     * 5          子表单
     * 6          字典
     * 6          照片选择； image
     * <p>
     * marker:地图定位 plane:面  line：线  image:图片
     */
    @Override
    public int getItemViewType(int position) {

        DongTaiFormBean bean = getItemStatusByPosition(position);

        if (bean == null) {
            return 0;
        }


        if (bean.getShowType() == null) {

            return VIEWTYPE_DANHANG;

        }


        if (bean.getShowType().equals("text")) {
            return VIEWTYPE_DANHANG;
        }
        if (bean.getShowType().equals("datetime")) {

            return VIEWTYPE_DANHANG_TIME_SELECT;
        }
        if (bean.getShowType().equals("list")) {

            return VIEWTYPE_DANHANG_LIST;
        }
        if (bean.getShowType().equals("image")) {

            return VIEWTYPE_CONTENT_IMAGE_CONTENT;
        }
        if (bean.getShowType().equals("marker")) {

            return VIEWTYPE_CONTENT_MARKER_CONTENT;
        }
        if (bean.getShowType().equals("plane")) {

            return VIEWTYPE_CONTENT_PLANE_CONTENT;
        }
        if (bean.getShowType().equals("line")) {

            return VIEWTYPE_CONTENT_LINE_CONTENT;
        }
        if (bean.getShowType().equals("switch")) {

            return VIEWTYPE_CONTENT_SWITCH_CONTENT;
        }
        if (bean.getShowType().equals("multistageList")) {

            return VIEWTYPE_CONTENT_MULTISTAGELIST_CONTENT;
        }
        if (bean.getShowType().equals("checkbox")) {

            return VIEWTYPE_CONTENT_CHECKBOX;
        }

        return 0;
    }


    @Override
    public int getItemCount() {
        int ParentitemCount = 0;
        int childItemCount = 0;


        for (int i = 0; i < biaoGeBeanList.size(); i++) {

            DongTaiFormBean dongTaiFormBean = biaoGeBeanList.get(i);

            if (dongTaiFormBean.getCgformFieldList() != null && dongTaiFormBean.getCgformFieldList().size() > 0) {

                String propertyLabel = dongTaiFormBean.getPropertyLabel();

                if (propertyLabel != null && propertyLabel.equals("0")) {

                } else {

                    childItemCount = childItemCount + dongTaiFormBean.getCgformFieldList().size();
                }

            }


        }

        ParentitemCount = biaoGeBeanList.size();


        return ParentitemCount + childItemCount;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEWTYPE_DANHANG) {
            return new DanhangViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_danhang, parent, false));
        } else if (viewType == VIEWTYPE_DANHANG_TIME_SELECT) {
            return new DanhangXuanZeViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_danhang_xuanze, parent, false));
        } else if (viewType == VIEWTYPE_DANHANG_LIST) {
            return new DanhangXuanZelistViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_danhang_list, parent, false));
        } else if (viewType == VIEWTYPE_CONTENT_IMAGE_CONTENT) {
            return new ImageContentViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_image_content, parent, false));
        } else if (viewType == VIEWTYPE_CONTENT_LINE_CONTENT) {
            return new MapContentViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_danhang_xuanze, parent, false));
        } else if (viewType == VIEWTYPE_CONTENT_MARKER_CONTENT) {
            return new MapContentViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_danhang_xuanze, parent, false));
        } else if (viewType == VIEWTYPE_CONTENT_PLANE_CONTENT) {
            return new MapContentViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_danhang_xuanze, parent, false));
        } else if (viewType == VIEWTYPE_CONTENT_SWITCH_CONTENT) {
            return new SwitchContentViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_switch_content, parent, false));
        } else if (viewType == VIEWTYPE_CONTENT_MULTISTAGELIST_CONTENT) {
            return new MultistageListViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_danhang_list, parent, false));
        } else if (viewType == VIEWTYPE_CONTENT_CHECKBOX) {
            return new CheckBoxViewHolder(mLayoutInflater.inflate(R.layout.item_biaoge_danhang_list, parent, false));
        }

        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        DongTaiFormBean bean = getItemStatusByPosition(position);

        if (bean == null) {
            position = position;
        }


        if (holder instanceof DanhangViewHolder) {
            danhang((DanhangViewHolder) holder, bean);
        } else if (holder instanceof DanhangParentViewHolder) {
            danhangParent((DanhangParentViewHolder) holder, bean);
        } else if (holder instanceof DanhangXuanZeViewHolder) {
            danhangXuanze((DanhangXuanZeViewHolder) holder, bean);
        } else if (holder instanceof DanhangXuanZelistViewHolder) {
            danhangXuanzelist((DanhangXuanZelistViewHolder) holder, bean);
        } else if (holder instanceof ImageContentViewHolder) {
            imageContent((ImageContentViewHolder) holder, bean, position);
        } else if (holder instanceof MapContentViewHolder) {
            mapContent((MapContentViewHolder) holder, bean);
        } else if (holder instanceof SwitchContentViewHolder) {
            switchContent((SwitchContentViewHolder) holder, bean);
        } else if (holder instanceof MultistageListViewHolder) {
            MultistageList((MultistageListViewHolder) holder, bean);
        } else if (holder instanceof CheckBoxViewHolder) {
            checkBoxView((CheckBoxViewHolder) holder, bean);
        }


    }


    //0  单行
    private class DanhangViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_bitian;
        private TextView tv_name;
        private EditText et_value;
        private TextView tv_tishi;

        public DanhangViewHolder(View itemView) {
            super(itemView);
            tv_bitian = itemView.findViewById(R.id.tv_bitian);
            tv_name = itemView.findViewById(R.id.tv_name);
            et_value = itemView.findViewById(R.id.et_value);
            tv_tishi = itemView.findViewById(R.id.tv_tishi);
            et_value.addTextChangedListener(tw);
        }

        private TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
//                biaoGeBeanList.get(getLayoutPosition()).setPropertyLabel(s.toString());
//                getItemStatusByPosition(getLayoutPosition()).setPropertyLabel(s.toString());
                DongTaiFormBean itemStatusByPosition = getItemStatusByPosition(getLayoutPosition());
                itemStatusByPosition.setPropertyLabel(s.toString());
            }
        };
    }

    private void danhang(final DanhangViewHolder holder, final DongTaiFormBean bean) {
        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
            holder.et_value.requestFocus();
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }

        holder.tv_name.setText(bean.getContent());
        if (!Utils.isEmpty(bean.getPropertyLabel())) {
            String showName = "";
            String resName=bean.getPropertyLabel();

            if (bean.getContent() != null) {
                if (bean.getContent().equals("树木编号")) {
                    if (!Utils.isEmpty(resName)) {


                        String[] resNames = resName.split("-");
                        if(resNames.length>3){
                            showName = resNames[2] + "-" + resNames[3];

                        }else {
                            showName=resName;
                        }

                        holder.et_value.setText(showName);

                    }
                }else {
                    holder.et_value.setText(bean.getPropertyLabel());
                }
            }

        } else {
            if (bean.getContent() != null) {
                if (bean.getContent().equals("树木编号")) {
                    holder.et_value.setHint("格式:xcq(区名)-xcaj(道路名)-00001");
                } else {
                    holder.et_value.setHint("");
                }
            }
            holder.et_value.setText("");

        }
        holder.et_value.clearFocus();

        if (isCheck) {
            holder.et_value.setEnabled(false);

        } else {
            /// 是否可修改  0：不能修改 1：可以修改
            if (!Utils.isEmpty(bean.getIsEdit())) {

                if ("0".equals(bean.getIsEdit())) {

                    holder.et_value.setEnabled(false);
                } else {
                    holder.et_value.setEnabled(true);
                }

            }
        }





        List<cgformRuleBean> cgformRuleList = bean.getCgformRuleList();
        Matcher m = null;
        cgformRuleBean cgformRuleBean = null;
        if (cgformRuleList != null && cgformRuleList.size() > 0) {
            cgformRuleBean = cgformRuleList.get(0);


//            1：数字 2：英文和数字 3：邮箱 4：电话号码和固话 5：身份证号 6：ip地址 7：邮政编码 8：数字和小数点

            if ("1".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else if ("2".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            } else if ("3".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if ("4".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            } else if ("5".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
            } else if ("6".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
            } else if ("7".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
            } else if ("8".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }

        } else {
            holder.et_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        }


        holder.et_value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Mlog.d("hasFocus = " + hasFocus);

                List<cgformRuleBean> cgformRuleList = bean.getCgformRuleList();
                Matcher m = null;
                cgformRuleBean cgformRuleBean = null;
                if (cgformRuleList != null && cgformRuleList.size() > 0) {
                    cgformRuleBean = cgformRuleList.get(0);
                    String par = cgformRuleBean.getPattern();
                    if (!Utils.isEmpty(par)) {

                        Pattern p = Pattern.compile(par);
                        m = p.matcher(bean.getPropertyLabel());

                    } else {
                        m = null;
                    }


                }


                if (!hasFocus && !Utils.isEmpty(holder.et_value.getText().toString()) && m != null) {

                    if (!m.matches()) {
                        holder.et_value.setText("");
                        holder.et_value.clearFocus();
                        Utils.showDialog(mContext, cgformRuleBean.getError());
                    }


                }
            }
        });

    }


    //1  单行 有父标签
    private class DanhangParentViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_bitian;
        private TextView tv_name;
        private EditText et_value;
        private TextView tv_tishi;

        public DanhangParentViewHolder(View itemView) {
            super(itemView);
            tv_bitian = (TextView) itemView.findViewById(R.id.tv_bitian);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            et_value = (EditText) itemView.findViewById(R.id.et_value);
            tv_tishi = (TextView) itemView.findViewById(R.id.tv_tishi);
            et_value.addTextChangedListener(tw);
        }

        private TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                biaoGeBeanList.get(getLayoutPosition()).setPropertyLabel(s.toString());
            }
        };
    }

    private void danhangParent(final DanhangParentViewHolder holder, final DongTaiFormBean bean) {
        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }


        holder.tv_name.setText(bean.getContent());

        if (bean.getPropertyLabel() != null) {
            holder.et_value.setText(bean.getPropertyLabel().toString());
        } else {
            holder.et_value.setText("");
        }

        if (isCheck) {
            holder.et_value.setEnabled(false);

        } else {
            holder.et_value.setEnabled(true);
        }

        /// 是否可修改  0：不能修改 1：可以修改

        if (!Utils.isEmpty(bean.getIsEdit())) {

            if ("0".equals(bean.getIsEdit())) {

                holder.et_value.setEnabled(false);
            } else {
                holder.et_value.setEnabled(true);
            }

        }

        List<cgformRuleBean> cgformRuleList = bean.getCgformRuleList();
        Matcher m = null;
        cgformRuleBean cgformRuleBean = null;
        if (cgformRuleList != null && cgformRuleList.size() > 0) {
            cgformRuleBean = cgformRuleList.get(0);


//            1：数字 2：英文和数字 3：邮箱 4：电话号码和固话 5：身份证号 6：ip地址 7：邮政编码 8：数字和小数点

            if ("1".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else if ("2".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            } else if ("3".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if ("4".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            } else if ("5".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
            } else if ("6".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
            } else if ("7".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
            } else if ("8".equals(cgformRuleBean.getType())) {
                holder.et_value.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }

        } else {
            holder.et_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        }

        holder.et_value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Mlog.d("hasFocus = " + hasFocus);

                List<cgformRuleBean> cgformRuleList = bean.getCgformRuleList();
                Matcher m = null;
                cgformRuleBean cgformRuleBean = null;
                if (cgformRuleList != null && cgformRuleList.size() > 0) {
                    cgformRuleBean = cgformRuleList.get(0);
                    String par = cgformRuleBean.getPattern();

                    Pattern p = Pattern.compile(par);
                    m = p.matcher(bean.getPropertyLabel());

                }


                if (!hasFocus && !Utils.isEmpty(holder.et_value.getText().toString()) && m != null) {

                    if (!m.matches()) {
                        holder.et_value.setText("");
                        holder.et_value.clearFocus();
                        Utils.showDialog(mContext, cgformRuleBean.getError());
                    }


                }
            }
        });

    }


    //2  单行 选择
    private class DanhangXuanZelistViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_bitian;
        private TextView tv_name;
        private TextView tv_value;
        private TextView tv_tishi;
        private LinearLayout ll_value;
        private ImageView iv_arrorw;

        public DanhangXuanZelistViewHolder(View itemView) {
            super(itemView);
            tv_bitian = itemView.findViewById(R.id.tv_bitian);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_tishi = itemView.findViewById(R.id.tv_tishi);
            tv_value = itemView.findViewById(R.id.tv_value);
            ll_value = itemView.findViewById(R.id.ll_value);
            iv_arrorw = itemView.findViewById(R.id.iv_arrorw);
        }
    }

    private void danhangXuanzelist(final DanhangXuanZelistViewHolder holder, final DongTaiFormBean bean) {
        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

        if (bean.getPropertyLabel() != null) {

            holder.tv_value.setText(bean.getPropertyLabel());

        } else {
            holder.tv_value.setText("");
        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }

        if (isCheck) {
            holder.ll_value.setEnabled(false);
            holder.iv_arrorw.setVisibility(View.INVISIBLE);

        } else {
            holder.ll_value.setEnabled(true);
            holder.iv_arrorw.setVisibility(View.VISIBLE);
        }


        holder.tv_name.setText(bean.getContent());
        holder.ll_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<DictionaryBean> dictionaryBeans = map.get(bean.getDictText());
                if (!dictionaryBeans.contains(cancelbean)) {

                    dictionaryBeans.add(cancelbean);
                }

                if (dictionaryBeans != null && dictionaryBeans.size() > 0) {

                    if (bean.getJavaField().equals("road_plate")) {

                         new RoadPlateBsSelectDialog(new RoadPlateBsSelectDialog.BsSelectIF() {
                            @Override
                            public void onBsSelect(int type, int index) {

                                if (index == dictionaryBeans.size() - 1) {
                                    holder.tv_value.setText("");


                                    String oldPropertylabel = bean.getPropertyLabel();


                                    if (Utils.isEmpty(oldPropertylabel)) {

                                        bean.setPropertyLabel("");

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }

                                    } else if (!oldPropertylabel.equals("")) {

                                        bean.setPropertyLabel("");

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                            notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }


                                    }

                                } else {

                                    String oldPropertylabel = bean.getPropertyLabel();

                                    DictionaryBean dictionaryBean = dictionaryBeans.get(index);
                                    holder.tv_value.setText(dictionaryBean.getLabel());


                                    if (Utils.isEmpty(oldPropertylabel)) {

                                        bean.setPropertyLabel(dictionaryBean.getLabel());

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }

                                    } else if (!oldPropertylabel.equals(dictionaryBean.getLabel())) {

                                        bean.setPropertyLabel(dictionaryBean.getLabel());

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                            notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }


                                    }

                                    notifyDataSetChanged();


                                }



                            }

                        }).showPopuWindow(mContext,0, dictionaryBeans.get(0).getDescription(), map.get(bean.getDictText()));


                    }else {
                        new BsSelectDialog(new BsSelectDialog.BsSelectIF() {
                            @Override
                            public void onBsSelect(int type, int index) {
                                if (index == dictionaryBeans.size() - 1) {
                                    holder.tv_value.setText("");


                                    String oldPropertylabel = bean.getPropertyLabel();


                                    if (Utils.isEmpty(oldPropertylabel)) {

                                        bean.setPropertyLabel("");

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }

                                    } else if (!oldPropertylabel.equals("")) {

                                        bean.setPropertyLabel("");

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                            notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }

                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                        }


                                    }

                                } else {

                                    DictionaryBean dictionaryBean = dictionaryBeans.get(index);
                                    holder.tv_value.setText(dictionaryBean.getLabel());

                                    final EditText inputServer = new EditText(mContext);

                                    if (bean.getJavaField().equals("tree_species")) {

                                        if (dictionaryBean.getLabel().equals("其他")) {


                                            AlertDialog.Builder editdialog = new AlertDialog.Builder(mContext);
                                            editdialog.setTitle("请输入树种").setView(inputServer)
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            editdialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String text = inputServer.getText().toString();
                                                    holder.tv_value.setText(text);

                                                    String oldPropertylabel = bean.getPropertyLabel();


                                                    if (Utils.isEmpty(oldPropertylabel)) {

                                                        bean.setPropertyLabel(text);

                                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                        }

                                                    } else if (!oldPropertylabel.equals(text)) {

                                                        bean.setPropertyLabel(text);

                                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                                            notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                        }

                                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                        }


                                                    }
                                                }

                                            });
                                            editdialog.show();


                                        } else {
                                            String oldPropertylabel = bean.getPropertyLabel();


                                            if (Utils.isEmpty(oldPropertylabel)) {

                                                bean.setPropertyLabel(dictionaryBean.getLabel());

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }

                                            } else if (!oldPropertylabel.equals(dictionaryBean.getLabel())) {

                                                bean.setPropertyLabel(dictionaryBean.getLabel());

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                                    notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }


                                            }

                                            if (dictionaryBean.getLabel().equals("空坑")) {

                                                for (DongTaiFormBean formbean : biaoGeBeanList) {


                                                    if (formbean.getJavaField().equals("tree_category")) {
                                                        formbean.setFieldMustInput(false);

                                                    }
                                                    if (formbean.getJavaField().equals("tree_growing")) {
                                                        formbean.setFieldMustInput(false);
                                                    }


                                                }

                                                notifyDataSetChanged();

                                            } else {

                                                for (DongTaiFormBean formbean : biaoGeBeanList) {


                                                    if (formbean.getJavaField().equals("tree_category")) {
                                                        formbean.setFieldMustInput(true);

                                                    }
                                                    if (formbean.getJavaField().equals("tree_growing")) {
                                                        formbean.setFieldMustInput(true);
                                                    }


                                                }

                                                notifyDataSetChanged();

                                            }

                                        }


                                    }  else if (bean.getJavaField().equals("tree_category")) {


                                        String oldPropertylabel = bean.getPropertyLabel();


                                        if (Utils.isEmpty(oldPropertylabel)) {

                                            bean.setPropertyLabel(dictionaryBean.getLabel());

                                            if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                            }

                                        } else if (!oldPropertylabel.equals(dictionaryBean.getLabel())) {

                                            bean.setPropertyLabel(dictionaryBean.getLabel());

                                            if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                                notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                            }

                                            if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                            }


                                        }

                                        if (dictionaryBean.getLabel().contains("阔叶")) {

                                            for (DongTaiFormBean formbean : biaoGeBeanList) {


                                                if (formbean.getJavaField().equals("tree_evergreenSpecifications")) {
                                                    formbean.setIsEdit("0");
                                                    formbean.setPropertyLabel("");

                                                }
                                                if (formbean.getJavaField().equals("tree_deciduousSpecifications")) {
                                                    formbean.setIsEdit("1");
                                                    formbean.setPropertyLabel("");

                                                }


                                            }

                                            notifyDataSetChanged();

                                        }
                                        if (dictionaryBean.getLabel().contains("针叶")) {

                                            for (DongTaiFormBean formbean : biaoGeBeanList) {


                                                if (formbean.getJavaField().equals("tree_deciduousSpecifications")) {
                                                    formbean.setIsEdit("0");
                                                    formbean.setPropertyLabel("");

                                                }
                                                if (formbean.getJavaField().equals("tree_evergreenSpecifications")) {
                                                    formbean.setIsEdit("1");
                                                    formbean.setPropertyLabel("");

                                                }


                                            }


                                            notifyDataSetChanged();

                                        }

                                    } else if (bean.getJavaField().equals("tree_location")) {


                                        if (dictionaryBean.getLabel().equals("其他")) {


                                            AlertDialog.Builder editdialog = new AlertDialog.Builder(mContext);
                                            editdialog.setTitle("请输入位置").setView(inputServer)
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                            editdialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String text = inputServer.getText().toString();
                                                    holder.tv_value.setText(text);

                                                    String oldPropertylabel = bean.getPropertyLabel();


                                                    if (Utils.isEmpty(oldPropertylabel)) {

                                                        bean.setPropertyLabel(text);

                                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                        }

                                                    } else if (!oldPropertylabel.equals(text)) {

                                                        bean.setPropertyLabel(text);

                                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                                            notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                        }

                                                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                        }


                                                    }
                                                }

                                            });
                                            editdialog.show();


                                        } else {

                                            String oldPropertylabel = bean.getPropertyLabel();


                                            if (Utils.isEmpty(oldPropertylabel)) {

                                                bean.setPropertyLabel(dictionaryBean.getLabel());

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }

                                            } else if (!oldPropertylabel.equals(dictionaryBean.getLabel())) {

                                                bean.setPropertyLabel(dictionaryBean.getLabel());

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                                    notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }


                                            }
                                        }
                                    } else if (bean.getJavaField().equals("tree_injection")) {


                                        if (dictionaryBean.getLabel().equals("是")) {
                                            DatePickerDialog.OnDateSetListener dateListener =
                                                    new DatePickerDialog.OnDateSetListener() {
                                                        @Override
                                                        public void onDateSet(DatePicker datePicker,
                                                                              int year, int month, int dayOfMonth) {
                                                            //Calendar月份是从0开始,所以month要加1
                                                            Mlog.d("你选择了" + year + "年" +
                                                                    (month + 1) + "月" + dayOfMonth + "日");

                                                            String text = year + "年" + (month + 1) + "月" + dayOfMonth + "日";
                                                            holder.tv_value.setText(text);

                                                            String oldPropertylabel = bean.getPropertyLabel();


                                                            if (Utils.isEmpty(oldPropertylabel)) {

                                                                bean.setPropertyLabel(text);

                                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                                }

                                                            } else if (!oldPropertylabel.equals(text)) {

                                                                bean.setPropertyLabel(text);

                                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                                                    notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                                }

                                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                                }


                                                            }
                                                        }
                                                    };
                                            Calendar calendar = Calendar.getInstance();
                                            DatePickerDialog datePickerDialog = new DatePickerDialog(mContext,
                                                    dateListener,
                                                    calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    calendar.get(Calendar.DAY_OF_MONTH));
                                            datePickerDialog.show();

                                        } else {

                                            String oldPropertylabel = bean.getPropertyLabel();


                                            if (Utils.isEmpty(oldPropertylabel)) {

                                                bean.setPropertyLabel(dictionaryBean.getLabel());

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }

                                            } else if (!oldPropertylabel.equals(dictionaryBean.getLabel())) {

                                                bean.setPropertyLabel(dictionaryBean.getLabel());

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                                    notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }

                                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                                }


                                            }
                                        }
                                    }else {

                                        String oldPropertylabel = bean.getPropertyLabel();


                                        if (Utils.isEmpty(oldPropertylabel)) {

                                            bean.setPropertyLabel(dictionaryBean.getLabel());

                                            if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                            }

                                        } else if (!oldPropertylabel.equals(dictionaryBean.getLabel())) {

                                            bean.setPropertyLabel(dictionaryBean.getLabel());

                                            if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                                notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                            }

                                            if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                                notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                            }


                                        }

                                        notifyDataSetChanged();

                                    }


                                }

                            }
                        }).showPopuWindow_DictListData(mContext, 0, dictionaryBeans.get(0).getDescription(), map.get(bean.getDictText()));

                    }


                }

            }
        });

    }


    private class DanhangXuanZeViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_bitian;
        private TextView tv_name;
        private TextView tv_value;
        private TextView tv_tishi;
        private ImageView iv_arrorw;
//        private LinearLayout ll_value;

        public DanhangXuanZeViewHolder(View itemView) {
            super(itemView);
            tv_bitian = itemView.findViewById(R.id.tv_bitian);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_value = itemView.findViewById(R.id.tv_value);
            tv_tishi = itemView.findViewById(R.id.tv_tishi);
            iv_arrorw = itemView.findViewById(R.id.iv_arrorw);
//            ll_value =  itemView.findViewById(R.id.ll_value);
        }
    }

    private void danhangXuanze(final DanhangXuanZeViewHolder holder, final DongTaiFormBean bean) {
        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

//        if (bean.getPropertyLabel() != null) {
//            holder.tv_value.setText(bean.getPropertyLabel().toString());
//        } else {
//            holder.tv_value.setText("");
//        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }

        if (isCheck) {
            holder.tv_value.setEnabled(false);
            holder.iv_arrorw.setVisibility(View.INVISIBLE);

        } else {
            holder.tv_value.setEnabled(true);
            holder.iv_arrorw.setVisibility(View.VISIBLE);
        }


        holder.tv_name.setText(bean.getContent());

        if (!Utils.isEmpty(bean.getIsEdit())) {

            /// 是否可修改  0：不能修改 1：可以修改 @property (nonatomic, copy) NSMutableString

            if ("0".equals(bean.getIsEdit())) {

                if (!Utils.isEmpty(bean.getPropertyLabel())) {
                    holder.tv_value.setText(bean.getPropertyLabel().toString());
                } else {
                    String currentDate = Utils.formatCurrentDate();

                    holder.tv_value.setText(currentDate);
                    bean.setPropertyLabel(currentDate);
                }

            } else {

                if (bean.getPropertyLabel() != null) {
                    holder.tv_value.setText(bean.getPropertyLabel().toString());
                } else {
                    holder.tv_value.setText("");
                }


                holder.tv_value.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timePickerDialog = new TimePickerDialog(mContext, new TimePickerDialog.TimePickerDialogInterface() {
                            @Override
                            public void positiveListener() {
                                String date = timePickerDialog.getYear() + "-" + timePickerDialog.getMonth()
                                        + "-" + timePickerDialog.getDay();
                                holder.tv_value.setText(date);
                                bean.setPropertyLabel(date);
                            }

                            @Override
                            public void negativeListener() {
                            }
                        });
                        timePickerDialog.showDatePickerDialog();
                    }
                });
            }

        }


//        holder.tv_value.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                timePickerDialog = new TimePickerDialog(mContext, new TimePickerDialog.TimePickerDialogInterface() {
//                    @Override
//                    public void positiveListener() {
//                        String date = timePickerDialog.getYear() + "-" + timePickerDialog.getMonth()
//                                + "-" + timePickerDialog.getDay();
//                        holder.tv_value.setText(date);
//                        bean.setPropertyLabel(date);
//                    }
//
//                    @Override
//                    public void negativeListener() {
//                    }
//                });
//                timePickerDialog.showDatePickerDialog();
//            }
//        });

    }


    /***
     * image
     */

    private class ImageContentViewHolder extends RecyclerView.ViewHolder {
        private MyLinearLayoutForListView ll_jingdian_pic;
        private TextView tv_name;
        private TextView tv_tishi;
        private TextView tv_bitian;
        private TextView tv_value;
        private TextView tv_hint_tishi;

        public ImageContentViewHolder(View itemView) {
            super(itemView);
            ll_jingdian_pic = itemView.findViewById(R.id.ll_jingdian_pic);
            tv_name = itemView.findViewById(R.id.tv_name);

            tv_name = itemView.findViewById(R.id.tv_name);
            tv_bitian = itemView.findViewById(R.id.tv_bitian);

            tv_value = itemView.findViewById(R.id.tv_value);
            tv_tishi = itemView.findViewById(R.id.tv_tishi);
            tv_hint_tishi = itemView.findViewById(R.id.tv_hint_tishi);
        }
    }


    private void imageContent(final ImageContentViewHolder holder, final DongTaiFormBean bean, int position) {

        List<JDPicInfo> list = new ArrayList<>();
        ItemPicAdater itemPicAdater = new ItemPicAdater(mContext, list, bean, picBeannull, isCheck);

        if (!Utils.isEmpty(bean.getContent())) {
            if (bean.getContent().equals("道路照片")) {
                itemPicAdater.setMaxSize(3);
            } else {
                itemPicAdater.setMaxSize(9);

            }

        }

        list.clear();
        List<JDPicInfo> picinfosList = bean.getPic();
        if (picinfosList != null && picinfosList.size() > 0) {
            for (int i = 0; i < picinfosList.size(); i++) {
                JDPicInfo jdPicInfo = picinfosList.get(i);

                if (!list.contains(jdPicInfo)) {
                    list.add(0, jdPicInfo);

                }
            }

        }
        if (listContainNull(list) == null) {
            if (isCheck) {
            } else {
                list.add(list.size(), picBeannull);

            }

        } else {
            JDPicInfo jdPicInfo = listContainNull(list);
            list.remove(jdPicInfo);

            if (isCheck) {
            } else {
                list.add(list.size(), picBeannull);

            }

        }
        holder.ll_jingdian_pic.removeAllViews();
        holder.ll_jingdian_pic.setAdapter(itemPicAdater);
        holder.tv_name.setText(bean.getContent());


        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }


        if (!Utils.isEmpty(bean.getContent())) {
            if (bean.getContent().equals("道路照片")) {
                if (isCheck) {
                    holder.tv_hint_tishi.setVisibility(View.GONE);
                } else {

                    holder.tv_hint_tishi.setVisibility(View.VISIBLE);
                }
            } else {
                holder.tv_hint_tishi.setVisibility(View.GONE);

            }

        }

    }


    private JDPicInfo listContainNull(List<JDPicInfo> jdPicslist) {

        if (jdPicslist != null && jdPicslist.size() > 0) {

            for (int i = 0; i < jdPicslist.size(); i++) {

                JDPicInfo jdPicInfo = jdPicslist.get(i);

                if (jdPicInfo.getIsAddFlag() == 1) {
                    return jdPicInfo;
                }

            }

        }

        return null;


    }


    /***
     * map
     */

    private class MapContentViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_bitian;
        private TextView tv_name;
        private TextView tv_value;
        private TextView tv_tishi;
        private LinearLayout ll_value;
        private ImageView iv_arrorw;

        public MapContentViewHolder(View itemView) {
            super(itemView);
            tv_bitian = itemView.findViewById(R.id.tv_bitian);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_value = itemView.findViewById(R.id.tv_value);
            tv_tishi = itemView.findViewById(R.id.tv_tishi);
            ll_value = itemView.findViewById(R.id.ll_value);
            iv_arrorw = itemView.findViewById(R.id.iv_arrorw);
        }
    }

    private void mapContent(final MapContentViewHolder holder, final DongTaiFormBean bean) {

        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

        if (bean.getPropertyLabel() != null) {
            holder.tv_value.setText(bean.getPropertyLabel().toString());
        } else {
            holder.tv_value.setText("");
        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }


        holder.tv_name.setText(bean.getContent());

        holder.ll_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bean.getShowType().equals("marker")) {

                    Intent intent = new Intent(mContext, GaoDeMapRangeActivity.class);
                    intent.putExtra("falg", bean.getShowType());
                    intent.putExtra("bean", bean);
                    intent.putExtra("isCheck", isCheck);
                    if (roadbean != null) {
                        intent.putExtra("roadbean", roadbean);
                    }
                    if (tree_resDataBean != null) {
                        intent.putExtra("tree_resDataBean", tree_resDataBean);
                    }
                    if (projectBean != null) {
                        intent.putExtra("projectBean", projectBean);
                    }
                    if (treeList != null) {
                        intent.putExtra("treeList", (Serializable)treeList);
                    }
                    intent.putExtra("isNewAdd",isNewAdd);
                    Activity activity = (Activity) mContext;
                    activity.startActivityForResult(intent, 6);
                }
                if (bean.getShowType().equals("plane")) {
                    Intent intent = new Intent(mContext, GaoDeMapRangeActivity.class);
                    intent.putExtra("falg", bean.getShowType());
                    intent.putExtra("bean", bean);
                    intent.putExtra("isCheck", isCheck);
                    if (roadbean != null) {
                        intent.putExtra("roadbean", roadbean);
                    }
                    if (tree_resDataBean != null) {
                        intent.putExtra("tree_resDataBean", tree_resDataBean);
                    }
                    if (projectBean != null) {
                        intent.putExtra("projectBean", projectBean);
                    }
                    if (treeList != null) {
                        intent.putExtra("treeList",(Serializable) treeList);
                    }
                    intent.putExtra("isNewAdd",isNewAdd);
                    Activity activity = (Activity) mContext;
                    activity.startActivityForResult(intent, 6);
                }
                if (bean.getShowType().equals("line")) {
                    Intent intent = new Intent(mContext, GaoDeMapRangeActivity.class);
                    intent.putExtra("falg", bean.getShowType());
                    intent.putExtra("bean", bean);
                    intent.putExtra("isCheck", isCheck);
                    if (roadbean != null) {
                        intent.putExtra("roadbean", roadbean);
                    }
                    if (tree_resDataBean != null) {
                        intent.putExtra("tree_resDataBean", tree_resDataBean);
                    }
                    if (projectBean != null) {
                        intent.putExtra("projectBean", projectBean);
                    }
                    if (treeList != null) {
                        intent.putExtra("treeList",(Serializable) treeList);
                    }
                    intent.putExtra("isNewAdd",isNewAdd);
                    Activity activity = (Activity) mContext;
                    activity.startActivityForResult(intent, 6);
                }


            }
        });

    }


    /***
     * switch
     */

    private class SwitchContentViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_bitian;
        private TextView tv_name;
        private TextView tv_tishi;
        private ImageView iv_switch;

        public SwitchContentViewHolder(View itemView) {
            super(itemView);
            tv_bitian = itemView.findViewById(R.id.tv_bitian);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_tishi = itemView.findViewById(R.id.tv_tishi);
            iv_switch = itemView.findViewById(R.id.iv_switch);
        }
    }

    private void switchContent(final SwitchContentViewHolder holder, final DongTaiFormBean bean) {

        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

        if (!Utils.isEmpty(bean.getPropertyLabel())) {
            if (bean.getPropertyLabel().equals("1")) {
                holder.iv_switch.setImageResource(R.mipmap.check_on);
            }

            if (bean.getPropertyLabel().equals("0")) {
                holder.iv_switch.setImageResource(R.mipmap.check_off);
            }


        } else {
            holder.iv_switch.setImageResource(R.mipmap.check_off);

        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }


        holder.tv_name.setText(bean.getContent());

        if (isCheck) {
            holder.iv_switch.setEnabled(false);

        } else {
            holder.iv_switch.setEnabled(true);
        }
        holder.iv_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate(mContext);
                String oldPropertylabel = bean.getPropertyLabel();


                if (Utils.isEmpty(oldPropertylabel)) {

                    bean.setPropertyLabel("1");

                    if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                        notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                    }

                } else {

                    if (oldPropertylabel.equals("0")) {

                        bean.setPropertyLabel("1");

                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                            notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                        }


                    } else {

                        bean.setPropertyLabel("0");

                        if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                            notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                        }


                    }
                }


                notifyDataSetChanged();


            }
        });

    }


    //2  单行 选择
    private class MultistageListViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_bitian;
        private TextView tv_name;
        private TextView tv_value;
        private TextView tv_tishi;
        private LinearLayout ll_value;
        private ImageView iv_arrorw;

        public MultistageListViewHolder(View itemView) {
            super(itemView);
            tv_bitian = itemView.findViewById(R.id.tv_bitian);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_tishi = itemView.findViewById(R.id.tv_tishi);
            tv_value = itemView.findViewById(R.id.tv_value);
            ll_value = itemView.findViewById(R.id.ll_value);
            iv_arrorw = itemView.findViewById(R.id.iv_arrorw);
        }
    }

    private void MultistageList(final MultistageListViewHolder holder, final DongTaiFormBean bean) {
        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

        if (bean.getPropertyLabel() != null) {

            holder.tv_value.setText(bean.getPropertyLabel());

        } else {
            holder.tv_value.setText("");
        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }

        if (isCheck) {
            holder.ll_value.setEnabled(false);
            holder.iv_arrorw.setVisibility(View.INVISIBLE);

        } else {
            holder.ll_value.setEnabled(true);
            holder.iv_arrorw.setVisibility(View.VISIBLE);
        }


        holder.tv_name.setText(bean.getContent());
        holder.ll_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<DictionaryBean> dictionaryBeans = map.get(bean.getDictText());
                if (dictionaryBeans != null && dictionaryBeans.size() > 0) {

                    ArrayList<AddressPicker.Province> data = new ArrayList<AddressPicker.Province>();


                    for (int i = 0; i < dictionaryBeans.size(); i++) {

                        AddressPicker.Province province = new AddressPicker.Province();

                        province.setAreaName(dictionaryBeans.get(i).getLabel());

                        ArrayList<AddressPicker.City> cities = new ArrayList<AddressPicker.City>();


                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<DictionaryBean> dictList = mGson.fromJson(dictionaryBeans.get(i).getStrChildlist(),
                                new TypeToken<List<DictionaryBean>>() {
                                }.getType());

                        for (int j = 0; j < dictList.size(); j++) {

                            AddressPicker.City city = new AddressPicker.City();

                            city.setAreaName(dictList.get(j).getLabel());
                            cities.add(city);
                        }


                        province.setCities(cities);
                        data.add(province);
                    }


                    Activity activity = (Activity) mContext;


                    AddressPicker picker = new AddressPicker(activity, data);
                    picker.setHideCounty(true);
                    picker.setOnAddressPickListener(new AddressPicker.OnAddressPickListener() {
                        @Override
                        public void onAddressPicked(String province, String city, String county) {


                            String adaa = province.trim() + " " + city.trim();

                            holder.tv_value.setText( adaa);
                            bean.setPropertyLabel(adaa);
                        }
                    });
                    picker.show();
                }

            }
        });

    }

    //多选
    private class CheckBoxViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_bitian;
        private TextView tv_name;
        private TextView tv_value;
        private TextView tv_tishi;
        private LinearLayout ll_value;
        private ImageView iv_arrorw;

        public CheckBoxViewHolder(View itemView) {
            super(itemView);
            tv_bitian = itemView.findViewById(R.id.tv_bitian);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_tishi = itemView.findViewById(R.id.tv_tishi);
            tv_value = itemView.findViewById(R.id.tv_value);
            ll_value = itemView.findViewById(R.id.ll_value);
            iv_arrorw = itemView.findViewById(R.id.iv_arrorw);
        }
    }

    private void checkBoxView(final CheckBoxViewHolder holder, final DongTaiFormBean bean) {
        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

        if (bean.getPropertyLabel() != null) {

            holder.tv_value.setText(bean.getPropertyLabel());

        } else {
            holder.tv_value.setText("");
        }

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }

        if (isCheck) {
            holder.ll_value.setEnabled(false);
            holder.iv_arrorw.setVisibility(View.INVISIBLE);

        } else {
            holder.ll_value.setEnabled(true);
            holder.iv_arrorw.setVisibility(View.VISIBLE);
        }


        holder.tv_name.setText(bean.getContent());
        holder.ll_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<DictionaryBean> dictionaryBeans = map.get(bean.getDictText());

                if (dictionaryBeans != null && dictionaryBeans.size() > 0) {
                    new BsSelectDialog(new BsSelectDialog.MulitiCheckBoxIF() {
                        @Override
                        public void checkBoxResult(String checkResult) {


                            holder.tv_value.setText(checkResult);


                            String oldPropertylabel = bean.getPropertyLabel();


                            if (Utils.isEmpty(oldPropertylabel)) {

                                bean.setPropertyLabel(checkResult);

                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                }

                            } else if (!oldPropertylabel.equals(checkResult)) {

                                bean.setPropertyLabel(checkResult);

                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("1")) {


                                    notifyItemRangeInserted(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                }

                                if (bean.getCgformFieldList() != null && bean.getPropertyLabel().equals("0")) {


                                    notifyItemRangeRemoved(bean.getPosition() + 1, bean.getCgformFieldList().size());

                                }


                            }


                        }
                    }).showMUltiCheckBoxDialog(mContext, 0, dictionaryBeans.get(0).getDescription(), map.get(bean.getDictText()));

                }

            }
        });

    }


    private void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }


    private DongTaiFormBean getItemStatusByPosition(int position) {

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
